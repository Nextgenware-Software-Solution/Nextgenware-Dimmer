package com.nextgenware.dimmer.data.repository

import com.nextgenware.dimmer.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow

class BrightnessRepository(private val settingsDataStore: SettingsDataStore) {

    val brightness: Flow<Float> = settingsDataStore.brightness
    val isDimmerEnabled: Flow<Boolean> = settingsDataStore.isDimmerEnabled

    suspend fun saveBrightness(brightness: Float) {
        settingsDataStore.saveBrightness(brightness)
    }

    suspend fun setDimmerEnabled(enabled: Boolean) {
        settingsDataStore.setDimmerEnabled(enabled)
    }
}
