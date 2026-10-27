package com.example.bepviet

import android.app.Application
import com.example.bepviet.di.AppContainer
import com.example.bepviet.di.DefaultAppContainer

class BepVietApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}