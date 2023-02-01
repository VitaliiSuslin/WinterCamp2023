package com.example.wintercamp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.wintercamp.Injector
import com.example.wintercamp.location.service.LocationService
import com.example.wintercamp.R
import com.example.wintercamp.repository.impl.PreferencesKey

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("BootCR", "Started")
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Log.e("BootCR", "Intent.ACTION_BOOT_COMPLETED")
            if (context.getSharedPreferences(PreferencesKey.APP_SETTINGS, Context.MODE_PRIVATE)
                    .getBoolean(PreferencesKey.REBOOT_ON_CLOSE, false)){
                Log.e("BootCR", "ACTION_RESTART")
                LocationService.start(context)
            }
        }
    }
}