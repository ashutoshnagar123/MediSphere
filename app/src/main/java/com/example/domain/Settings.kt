package com.example.domain

data class Settings(
    val isDarkMode: Boolean = false,
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val shareDataForResearch: Boolean = false
)

interface SettingsRepository {
    suspend fun getSettings(): kotlinx.coroutines.flow.Flow<Settings>
    suspend fun updateSettings(settings: Settings)
}
