package com.example.wintercamp

import android.app.Application
import com.example.wintercamp.location.processor.LocationProcessor
import com.example.wintercamp.repository.AppSettingsRepository
import com.example.wintercamp.repository.impl.AppSettingsRepositoryImpl

object Injector {

    lateinit var application: Application

    fun init(application: Application) {
        this.application = application
    }


    val locationProcessor = LocationProcessor()

    val appSettingsRepository: AppSettingsRepository by lazy { AppSettingsRepositoryImpl(application) }
}