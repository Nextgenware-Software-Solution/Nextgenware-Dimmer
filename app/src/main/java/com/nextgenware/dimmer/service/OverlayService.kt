package com.nextgenware.dimmer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.nextgenware.dimmer.MainActivity
import com.nextgenware.dimmer.data.datastore.SettingsDataStore
import com.nextgenware.dimmer.data.repository.BrightnessRepository
import com.nextgenware.dimmer.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var repository: BrightnessRepository

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "dimmer_service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dataStore = SettingsDataStore(applicationContext)
        repository = BrightnessRepository(dataStore)

        createNotificationChannel()
        // Initial foreground notification
        startForeground(NOTIFICATION_ID, createNotification(0.5f, false))

        observeSettings()
    }

    private fun observeSettings() {
        serviceScope.launch {
            combine(repository.brightness, repository.isDimmerEnabled) { brightness, enabled ->
                brightness to enabled
            }.collect { (brightness, enabled) ->
                if (enabled) {
                    showOverlay(brightness)
                } else {
                    hideOverlay()
                }
                updateNotification(brightness, enabled)
            }
        }
    }

    private fun showOverlay(brightness: Float) {
        if (overlayView == null) {
            overlayView = View(this).apply {
                setBackgroundColor(Color.BLACK)
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            windowManager.addView(overlayView, params)
        }
        overlayView?.alpha = brightness
    }

    private fun hideOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Dimmer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running overlay service"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(brightness: Float, enabled: Boolean): Notification {
        val brightnessPercent = (brightness * 100).toInt()
        val toggleText = if (enabled) "OFF" else "ON"

        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = Constants.ACTION_TOGGLE_DIMMER
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            this, 1, toggleIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val increaseIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = Constants.ACTION_INCREASE_BRIGHTNESS
        }
        val increasePendingIntent = PendingIntent.getBroadcast(
            this, 2, increaseIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val decreaseIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = Constants.ACTION_DECREASE_BRIGHTNESS
        }
        val decreasePendingIntent = PendingIntent.getBroadcast(
            this, 3, decreaseIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Dimmer")
            .setContentText("Dimming: $brightnessPercent%")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .addAction(android.R.drawable.ic_input_delete, "-", decreasePendingIntent)
            .addAction(android.R.drawable.ic_media_play, toggleText, togglePendingIntent)
            .addAction(android.R.drawable.ic_input_add, "+", increasePendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .build()
    }

    private fun updateNotification(brightness: Float, enabled: Boolean) {
        val notification = createNotification(brightness, enabled)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
    }
}
