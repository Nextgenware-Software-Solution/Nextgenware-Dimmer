package com.nextgenware.dimmer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextgenware.dimmer.data.datastore.SettingsDataStore
import com.nextgenware.dimmer.data.repository.BrightnessRepository
import com.nextgenware.dimmer.service.OverlayService
import com.nextgenware.dimmer.ui.screens.HomeScreen
import com.nextgenware.dimmer.ui.theme.NextgenwareDimmerTheme
import com.nextgenware.dimmer.viewmodel.BrightnessViewModel
import com.nextgenware.dimmer.viewmodel.BrightnessViewModelFactory

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val settingsDataStore = SettingsDataStore(applicationContext)
        val repository = BrightnessRepository(settingsDataStore)
        val factory = BrightnessViewModelFactory(repository)

        setContent {
            NextgenwareDimmerTheme {
                val viewModel: BrightnessViewModel = viewModel(factory = factory)
                val isDimmerEnabled by viewModel.isDimmerEnabled.collectAsState()
                val isNotificationEnabled by viewModel.isNotificationEnabled.collectAsState()

                // Manage service lifecycle based on settings
                LaunchedEffect(isDimmerEnabled, isNotificationEnabled) {
                    if (isDimmerEnabled || isNotificationEnabled) {
                        val intent = Intent(this@MainActivity, OverlayService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
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
