package com.nextgenware.dimmer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nextgenware.dimmer.data.datastore.SettingsDataStore
import com.nextgenware.dimmer.data.repository.BrightnessRepository
import com.nextgenware.dimmer.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repository = BrightnessRepository(SettingsDataStore(context))
        val scope = CoroutineScope(Dispatchers.IO)

        when (intent.action) {
            Constants.ACTION_TOGGLE_DIMMER -> {
                scope.launch {
                    val current = repository.isDimmerEnabled.first()
                    repository.setDimmerEnabled(!current)
                }
            }
            Constants.ACTION_INCREASE_BRIGHTNESS -> {
                scope.launch {
                    val current = repository.brightness.first()
                    repository.saveBrightness((current + Constants.BRIGHTNESS_STEP).coerceIn(Constants.MIN_BRIGHTNESS, Constants.MAX_BRIGHTNESS))
                }
            }
            Constants.ACTION_DECREASE_BRIGHTNESS -> {
                scope.launch {
                    val current = repository.brightness.first()
                    repository.saveBrightness((current - Constants.BRIGHTNESS_STEP).coerceIn(Constants.MIN_BRIGHTNESS, Constants.MAX_BRIGHTNESS))
                }
            }
        }
    }
}
