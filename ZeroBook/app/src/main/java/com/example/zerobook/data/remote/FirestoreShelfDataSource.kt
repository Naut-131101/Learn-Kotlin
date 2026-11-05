package com.example.zerobook.data.remote

import com.example.zerobook.data.local.ShelfBookEntity
import com.example.zerobook.model.ReadingStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Đọc/ghi kệ sách trên Firestore tại đường dẫn: users/{uid}/books/{bookId}.
 *
 * Chuyển đổi bằng Map thủ công (không dùng toObject()) để không phụ thuộc reflection,
 * nên bản release bật R8 vẫn chạy đúng mà không cần thêm keep rules.
 */
class FirestoreShelfDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun books(uid: String) =
        firestore.collection("users").document(uid).collection("books")

    suspend fun upsert(uid: String, entity: ShelfBookEntity) {
        books(uid).document(entity.bookId).set(entity.toFirestoreMap()).await()
    }

    suspend fun delete(uid: String, bookId: String) {
        books(uid).document(bookId).delete().await()
    }

    /** Source.SERVER: bắt buộc lấy từ server, tránh nhận dữ liệu cache cũ khi mạng chập chờn. */
    suspend fun fetchAll(uid: String): List<ShelfBookEntity> =
        books(uid).get(Source.SERVER).await().documents.mapNotNull { it.toShelfEntity() }
}

private fun ShelfBookEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "authors" to authors,
    "description" to description,
    "thumbnailUrl" to thumbnailUrl,
    "pageCount" to pageCount,
    "publishedDate" to publishedDate,
    "publisher" to publisher,
    "status" to status.name,
    "rating" to rating,
    "note" to note,
    "addedAt" to addedAt,
    "finishedAt" to finishedAt,
    "updatedAt" to updatedAt,
)

private fun DocumentSnapshot.toShelfEntity(): ShelfBookEntity? {
    val title = getString("title") ?: return null
    val status = getString("status")
        ?.let { name -> ReadingStatus.entries.firstOrNull { it.name == name } }
        ?: return null
    return ShelfBookEntity(
        bookId = id,
        title = title,
        authors = getString("authors").orEmpty(),
        description = getString("description"),
        thumbnailUrl = getString("thumbnailUrl"),
        pageCount = getLong("pageCount")?.toInt(),
        publishedDate = getString("publishedDate"),
        publisher = getString("publisher"),
        status = status,
        rating = getLong("rating")?.toInt() ?: 0,
        note = getString("note").orEmpty(),
        addedAt = getLong("addedAt") ?: 0L,
        finishedAt = getLong("finishedAt"),
        updatedAt = getLong("updatedAt") ?: 0L,
        pendingSync = false,
        isDeleted = false,
    )
}
