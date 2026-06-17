package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.Settings
import com.example.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class DefaultSettingsRepository(private val context: Context) : SettingsRepository {

    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    private val LANGUAGE = stringPreferencesKey("language")
    private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    private val SHARE_DATA = booleanPreferencesKey("share_data")

    override suspend fun getSettings(): Flow<Settings> {
        return context.dataStore.data.map { preferences ->
            Settings(
                isDarkMode = preferences[IS_DARK_MODE] ?: true,
                language = preferences[LANGUAGE] ?: "en",
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                shareDataForResearch = preferences[SHARE_DATA] ?: false
            )
        }
    }

    override suspend fun updateSettings(settings: Settings) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = settings.isDarkMode
            preferences[LANGUAGE] = settings.language
            preferences[NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[SHARE_DATA] = settings.shareDataForResearch
        }
    }
}
