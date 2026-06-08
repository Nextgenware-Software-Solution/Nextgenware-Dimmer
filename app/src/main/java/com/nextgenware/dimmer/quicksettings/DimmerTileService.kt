package com.nextgenware.dimmer.quicksettings

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.nextgenware.dimmer.data.datastore.SettingsDataStore
import com.nextgenware.dimmer.data.repository.BrightnessRepository
import com.nextgenware.dimmer.service.OverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class DimmerTileService : TileService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var repository: BrightnessRepository

    override fun onCreate() {
        super.onCreate()
        repository = BrightnessRepository(SettingsDataStore(applicationContext))
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    private fun updateTile() {
        serviceScope.launch {
            val isEnabled = repository.isDimmerEnabled.first()
            val tile = qsTile ?: return@launch
            tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = "Screen Dimmer"
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch {
            val isEnabled = repository.isDimmerEnabled.first()
            val newState = !isEnabled
            repository.setDimmerEnabled(newState)
            
            val intent = Intent(applicationContext, OverlayService::class.java)
            if (newState) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
            
            updateTile()
        }
    }
}
