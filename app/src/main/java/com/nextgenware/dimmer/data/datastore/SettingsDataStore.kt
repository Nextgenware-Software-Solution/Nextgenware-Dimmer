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
    }

    val brightness: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[BRIGHTNESS_KEY] ?: 0.5f
    }

    val isDimmerEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DIMMER_ENABLED_KEY] ?: false
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
}
