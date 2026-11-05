package com.example.zerobook.work

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Xếp lịch ReminderWorker chạy mỗi ngày vào giờ người dùng chọn. */
@Singleton
class ReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
) {
    fun schedule(hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var nextRun = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!nextRun.isAfter(now)) nextRun = nextRun.plusDays(1)
        val initialDelay = Duration.between(now, nextRun).toMillis()

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        // CANCEL_AND_REENQUEUE: đổi giờ nhắc thì huỷ lịch cũ, tạo lịch mới với độ trễ mới
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request,
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "reading_reminder"
    }
}
