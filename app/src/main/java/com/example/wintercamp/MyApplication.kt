package com.example.wintercamp

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Injector.init(this)
    }
}