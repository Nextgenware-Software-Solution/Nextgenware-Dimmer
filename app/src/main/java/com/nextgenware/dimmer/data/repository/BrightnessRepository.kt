package com.nextgenware.dimmer.data.repository

import com.nextgenware.dimmer.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow

class BrightnessRepository(private val settingsDataStore: SettingsDataStore) {

    val brightness: Flow<Float> = settingsDataStore.brightness
    val isDimmerEnabled: Flow<Boolean> = settingsDataStore.isDimmerEnabled
    val isNotificationLocked: Flow<Boolean> = settingsDataStore.isNotificationLocked
    val isNotificationEnabled: Flow<Boolean> = settingsDataStore.isNotificationEnabled
    val isNotificationSwiped: Flow<Boolean> = settingsDataStore.isNotificationSwiped

    suspend fun saveBrightness(brightness: Float) {
        settingsDataStore.saveBrightness(brightness)
    }

    suspend fun setDimmerEnabled(enabled: Boolean) {
        settingsDataStore.setDimmerEnabled(enabled)
    }

    suspend fun setNotificationLocked(locked: Boolean) {
        settingsDataStore.setNotificationLocked(locked)
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        settingsDataStore.setNotificationEnabled(enabled)
    }

    suspend fun setNotificationSwiped(swiped: Boolean) {
        settingsDataStore.setNotificationSwiped(swiped)
    }
}
