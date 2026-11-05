package com.example.zerobook

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.zerobook.work.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * @HiltAndroidApp tạo container dependency gốc của app.
 * Configuration.Provider: tự khởi tạo WorkManager với HiltWorkerFactory
 * để Worker được inject dependency (cần tắt bộ khởi tạo mặc định trong AndroidManifest).
 */
@HiltAndroidApp
class ZeroBookApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
    }
}
