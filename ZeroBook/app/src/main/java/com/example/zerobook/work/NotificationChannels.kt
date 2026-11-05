package com.example.zerobook.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.zerobook.R

/** Từ Android 8 (API 26), mọi thông báo phải thuộc về một channel. minSdk = 26 nên luôn tạo được. */
object NotificationChannels {
    const val REMINDER_CHANNEL_ID = "reading_reminder"

    fun create(context: Context) {
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            context.getString(R.string.notification_channel_reminder_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_reminder_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
