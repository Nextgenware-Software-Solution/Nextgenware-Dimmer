package com.nextgenware.dimmer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
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
    private var isForeground = false

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "dimmer_persistent_channel_v8"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dataStore = SettingsDataStore(applicationContext)
        repository = BrightnessRepository(dataStore)

        createNotificationChannel()
        observeSettings()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun safeStartForeground(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            isForeground = true
        } catch (e: Exception) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun observeSettings() {
        serviceScope.launch {
            combine(
                repository.brightness,
                repository.isDimmerEnabled,
                repository.isNotificationLocked,
                repository.isNotificationEnabled,
                repository.isNotificationSwiped
            ) { brightness, enabled, locked, notificationEnabled, isSwiped ->
                DataSnapshot(brightness, enabled, locked, notificationEnabled, isSwiped)
            }.collect { snapshot ->
                // Update Overlay
                if (snapshot.enabled) {
                    showOverlay(snapshot.brightness)
                } else {
                    hideOverlay()
                }

                // Update Notification Controller
                if (snapshot.notificationEnabled) {
                    if (snapshot.locked && snapshot.isSwiped) {
                        // DETECTED SWIPE WHILE LOCKED: Immediately restore the 'not swiped' state to bring it back
                        repository.setNotificationSwiped(false)
                    } else if (!snapshot.isSwiped) {
                        updateNotification(snapshot.brightness, snapshot.enabled, snapshot.locked)
                    } else {
                        // SWIPED AND UNLOCKED: Downgrade from foreground to background service
                        if (isForeground) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                stopForeground(STOP_FOREGROUND_REMOVE)
                            } else {
                                @Suppress("DEPRECATION")
                                stopForeground(true)
                            }
                            isForeground = false
                        }
                    }
                } else {
                    // Notification manually disabled in settings
                    if (isForeground) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                        } else {
                            @Suppress("DEPRECATION")
                            stopForeground(true)
                        }
                        isForeground = false
                    }
                    if (!snapshot.enabled) stopSelf()
                }
            }
        }
    }

    private data class DataSnapshot(
        val brightness: Float,
        val enabled: Boolean,
        val locked: Boolean,
        val notificationEnabled: Boolean,
        val isSwiped: Boolean
    )

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
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Dimmer Pro Controls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Brightness controls and status"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(brightness: Float, enabled: Boolean, locked: Boolean): Notification {
        val brightnessPercent = (brightness * 100).toInt()
        val toggleText = if (enabled) "OFF" else "ON"

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(this, 100, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        // Using Broadcast Intents for all actions for maximum reliability
        val toggleIntent = Intent(this, NotificationActionReceiver::class.java).apply { action = Constants.ACTION_TOGGLE_DIMMER }
        val togglePendingIntent = PendingIntent.getBroadcast(this, 101, toggleIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val increaseIntent = Intent(this, NotificationActionReceiver::class.java).apply { action = Constants.ACTION_INCREASE_BRIGHTNESS }
        val increasePendingIntent = PendingIntent.getBroadcast(this, 102, increaseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val decreaseIntent = Intent(this, NotificationActionReceiver::class.java).apply { action = Constants.ACTION_DECREASE_BRIGHTNESS }
        val decreasePendingIntent = PendingIntent.getBroadcast(this, 103, decreaseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Swiped Away Detection
        val deleteIntent = Intent(this, NotificationActionReceiver::class.java).apply { action = Constants.ACTION_NOTIFICATION_DISMISSED }
        val deletePendingIntent = PendingIntent.getBroadcast(this, 104, deleteIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Dimmer Pro")
            .setContentText("Dimming Level: $brightnessPercent%")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(mainPendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .setOngoing(locked)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(if (locked) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_input_delete, "-", decreasePendingIntent)
            .addAction(android.R.drawable.ic_media_play, toggleText, togglePendingIntent)
            .addAction(android.R.drawable.ic_input_add, "+", increasePendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        val notification = builder.build()
        if (locked) {
            notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT or Notification.FLAG_NO_CLEAR
        }
        return notification
    }

    private fun updateNotification(brightness: Float, enabled: Boolean, locked: Boolean) {
        val notification = createNotification(brightness, enabled, locked)
        safeStartForeground(notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
    }
}
