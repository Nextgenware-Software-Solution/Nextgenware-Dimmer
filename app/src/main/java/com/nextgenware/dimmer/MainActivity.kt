package com.nextgenware.dimmer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextgenware.dimmer.data.datastore.SettingsDataStore
import com.nextgenware.dimmer.data.repository.BrightnessRepository
import com.nextgenware.dimmer.service.OverlayService
import com.nextgenware.dimmer.ui.screens.HomeScreen
import com.nextgenware.dimmer.ui.theme.NextgenwareDimmerTheme
import com.nextgenware.dimmer.viewmodel.BrightnessViewModel
import com.nextgenware.dimmer.viewmodel.BrightnessViewModelFactory

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val settingsDataStore = SettingsDataStore(applicationContext)
        val repository = BrightnessRepository(settingsDataStore)
        val factory = BrightnessViewModelFactory(repository)

        setContent {
            NextgenwareDimmerTheme {
                val viewModel: BrightnessViewModel = viewModel(factory = factory)
                val isEnabled by viewModel.isDimmerEnabled.collectAsState()

                // Start/Stop service based on state
                LaunchedEffect(isEnabled) {
                    val intent = Intent(this@MainActivity, OverlayService::class.java)
                    if (isEnabled) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
                    } else {
                        // We don't necessarily want to stop the service here if we want the notification to persist
                        // but for the basic MVP, stopping it is fine.
                        // stopService(intent)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text("Screen Dimmer Pro") })
                    }
                ) { innerPadding ->
                    HomeScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
