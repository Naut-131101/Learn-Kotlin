package com.example.zerobook.work

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zerobook.MainActivity
import com.example.zerobook.R
import com.example.zerobook.data.repository.ShelfRepository
import com.example.zerobook.model.ReadingStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/** Chạy mỗi ngày một lần, hiện thông báo nhắc đọc sách. */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val shelfRepository: ShelfRepository,
) : CoroutineWorker(appContext, params) {

    // Đã kiểm tra quyền ngay đầu hàm nên tắt cảnh báo MissingPermission của lint
    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        if (!canPostNotifications()) return Result.success()

        val readingBook = shelfRepository.observeShelf().first()
            .firstOrNull { it.status == ReadingStatus.READING }
        val text = if (readingBook != null) {
            applicationContext.getString(R.string.reminder_text_continue, readingBook.book.title)
        } else {
            applicationContext.getString(R.string.reminder_text_default)
        }

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_book)
            .setContentTitle(applicationContext.getString(R.string.reminder_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val NOTIFICATION_ID = 1001
    }
}
