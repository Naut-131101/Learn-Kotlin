package com.example.zerobook.data.repository

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.zerobook.data.local.ShelfBookDao
import com.example.zerobook.data.local.toDomain
import com.example.zerobook.data.local.toShelfEntity
import com.example.zerobook.data.remote.FirestoreShelfDataSource
import com.example.zerobook.model.Book
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.model.ShelfBook
import com.example.zerobook.work.SyncWorker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Chiến lược offline-first:
 * 1. Mọi thay đổi ghi vào Room trước (UI cập nhật ngay, kể cả khi mất mạng), đánh dấu pendingSync.
 * 2. Xếp lịch SyncWorker; WorkManager chỉ chạy khi có mạng và tự thử lại nếu lỗi.
 * 3. SyncWorker đẩy thay đổi lên Firestore rồi kéo dữ liệu từ máy khác về.
 */
@Singleton
class ShelfRepositoryImpl @Inject constructor(
    private val dao: ShelfBookDao,
    private val remote: FirestoreShelfDataSource,
    private val auth: FirebaseAuth,
    private val workManager: WorkManager,
) : ShelfRepository {

    override fun observeShelf(): Flow<List<ShelfBook>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeShelfBook(bookId: String): Flow<ShelfBook?> =
        dao.observeById(bookId).map { it?.toDomain() }

    override suspend fun addOrUpdateStatus(book: Book, status: ReadingStatus) {
        val now = System.currentTimeMillis()
        val existing = dao.getById(book.id)?.takeUnless { it.isDeleted }
        val finishedAt = when {
            status != ReadingStatus.READ -> null
            existing != null && existing.status == ReadingStatus.READ -> existing.finishedAt ?: now
            else -> now
        }
        dao.upsert(
            book.toShelfEntity(
                status = status,
                rating = existing?.rating ?: 0,
                note = existing?.note.orEmpty(),
                addedAt = existing?.addedAt ?: now,
                finishedAt = finishedAt,
                updatedAt = now,
            ),
        )
        requestSync()
    }

    override suspend fun updateReview(bookId: String, rating: Int, note: String) {
        val existing = dao.getById(bookId) ?: return
        dao.upsert(
            existing.copy(
                rating = rating.coerceIn(0, 5),
                note = note,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true,
            ),
        )
        requestSync()
    }

    override suspend fun remove(bookId: String) {
        val existing = dao.getById(bookId) ?: return
        // Xoá mềm: giữ lại dòng để SyncWorker biết cần xoá trên Firestore
        dao.upsert(
            existing.copy(
                isDeleted = true,
                pendingSync = true,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        requestSync()
    }

    override fun requestSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        // Unique work: nhiều lần gọi liên tiếp không tạo ra nhiều worker chạy song song
        workManager.enqueueUniqueWork(SYNC_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    override suspend fun syncNow(): Boolean {
        val uid = auth.currentUser?.uid ?: return true
        return try {
            pushPendingChanges(uid)
            pullRemoteChanges(uid)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Sync failed", e)
            false
        }
    }

    private suspend fun pushPendingChanges(uid: String) {
        for (entity in dao.getPending()) {
            if (entity.isDeleted) {
                remote.delete(uid, entity.bookId)
            } else {
                remote.upsert(uid, entity)
            }
            // Chỉ đánh dấu "đã đồng bộ" nếu người dùng không sửa thêm trong lúc đang upload
            val current = dao.getById(entity.bookId) ?: continue
            if (current.updatedAt == entity.updatedAt) {
                if (entity.isDeleted) {
                    dao.deleteById(entity.bookId)
                } else {
                    dao.upsert(current.copy(pendingSync = false))
                }
            }
        }
    }

    private suspend fun pullRemoteChanges(uid: String) {
        val remoteBooks = remote.fetchAll(uid)
        val remoteIds = remoteBooks.map { it.bookId }.toSet()

        for (remoteBook in remoteBooks) {
            val local = dao.getById(remoteBook.bookId)
            // Bản nào sửa sau thì thắng; thay đổi chưa đồng bộ ở máy này luôn được ưu tiên
            if (local == null || (!local.pendingSync && remoteBook.updatedAt > local.updatedAt)) {
                dao.upsert(remoteBook)
            }
        }

        // Sách đã bị xoá từ máy khác → xoá ở máy này
        dao.getAll()
            .filter { !it.pendingSync && it.bookId !in remoteIds }
            .forEach { dao.deleteById(it.bookId) }
    }

    override suspend fun clearLocal() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        dao.clear()
    }

    private companion object {
        const val TAG = "ShelfRepository"
        const val SYNC_WORK_NAME = "shelf_sync"
    }
}
