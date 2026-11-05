package com.example.zerobook.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zerobook.data.repository.ShelfRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Đồng bộ kệ sách với Firestore ở chế độ nền.
 * @HiltWorker + @AssistedInject: context và params do WorkManager truyền vào,
 * còn ShelfRepository do Hilt inject.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val shelfRepository: ShelfRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = when {
        shelfRepository.syncNow() -> Result.success()
        runAttemptCount < MAX_ATTEMPTS -> Result.retry()
        else -> Result.failure()
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}
