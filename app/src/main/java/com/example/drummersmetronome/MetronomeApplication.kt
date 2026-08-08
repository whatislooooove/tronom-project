package com.example.drummersmetronome

import android.app.Application

class MetronomeApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
