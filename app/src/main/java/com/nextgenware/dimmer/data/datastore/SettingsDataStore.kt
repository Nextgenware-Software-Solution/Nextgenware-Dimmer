package com.nextgenware.dimmer.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val BRIGHTNESS_KEY = floatPreferencesKey("brightness")
        val DIMMER_ENABLED_KEY = booleanPreferencesKey("dimmer_enabled")
        val NOTIFICATION_LOCKED_KEY = booleanPreferencesKey("notification_locked")
        val NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("notification_enabled")
        val NOTIFICATION_SWIPED_KEY = booleanPreferencesKey("notification_swiped")
    }

    val brightness: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[BRIGHTNESS_KEY] ?: 0.5f
    }

    val isDimmerEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DIMMER_ENABLED_KEY] ?: false
    }

    val isNotificationLocked: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_LOCKED_KEY] ?: true
    }

    val isNotificationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ENABLED_KEY] ?: true
    }

    val isNotificationSwiped: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_SWIPED_KEY] ?: false
    }

    suspend fun saveBrightness(brightness: Float) {
        context.dataStore.edit { preferences ->
            preferences[BRIGHTNESS_KEY] = brightness
        }
    }

    suspend fun setDimmerEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DIMMER_ENABLED_KEY] = enabled
        }
    }

    suspend fun setNotificationLocked(locked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_LOCKED_KEY] = locked
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED_KEY] = enabled
            // When toggling preference, reset swiped state
            preferences[NOTIFICATION_SWIPED_KEY] = false
        }
    }

    suspend fun setNotificationSwiped(swiped: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_SWIPED_KEY] = swiped
        }
    }
}
