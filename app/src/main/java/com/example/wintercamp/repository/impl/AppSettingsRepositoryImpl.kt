package com.example.wintercamp.repository.impl

import android.content.Context
import com.example.wintercamp.repository.AppSettingsRepository

class AppSettingsRepositoryImpl(private val context: Context) : AppSettingsRepository {

    override fun setRebootOption(status: Boolean) {
        context.getSharedPreferences(PreferencesKey.APP_SETTINGS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PreferencesKey.REBOOT_ON_CLOSE, status)
            .apply()
    }

    override fun getRebootStatus(): Boolean {
        return context.getSharedPreferences(PreferencesKey.APP_SETTINGS, Context.MODE_PRIVATE)
            .getBoolean(PreferencesKey.REBOOT_ON_CLOSE, false)
    }
}

object PreferencesKey{
    const val APP_SETTINGS = "app_settings"
    const val REBOOT_ON_CLOSE = "reboot_on_close"
}

