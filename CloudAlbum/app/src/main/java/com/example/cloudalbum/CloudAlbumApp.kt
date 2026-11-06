package com.example.cloudalbum

import android.app.Application
import com.example.cloudalbum.di.AppContainer

class CloudAlbumApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}