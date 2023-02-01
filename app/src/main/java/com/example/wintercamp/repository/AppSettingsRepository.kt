package com.example.wintercamp.repository

interface AppSettingsRepository {
    fun setRebootOption(status: Boolean)
    fun getRebootStatus(): Boolean
}