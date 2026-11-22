package com.example.slicinggame

import android.app.Application
import android.content.Context

class AppContext : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        Settings.init(this)
    }

    companion object {
        lateinit var instance: AppContext
            private set
        val ctx: Context get() = instance.applicationContext
    }
}
