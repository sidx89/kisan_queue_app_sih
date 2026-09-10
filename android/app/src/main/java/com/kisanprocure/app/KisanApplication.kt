package com.kisanprocure.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.kisanprocure.app.data.local.KisanDatabase
import com.kisanprocure.app.utils.AppConfig
import com.kisanprocure.app.utils.ErrorReporter

class KisanApplication : Application() {

    companion object {
        lateinit var instance: KisanApplication
            private set
        const val TAG = "KisanApp"
    }

    // Lazy singletons accessible app-wide
    val database by lazy { KisanDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize app config (reads BuildConfig / shared prefs)
        AppConfig.init(this)

        // Initialize error reporter (sets up uncaught exception handler)
        ErrorReporter.init(this)

        // Create notification channels
        createNotificationChannels()

        Log.i(TAG, "KisanProcure Application started — API: ${AppConfig.apiBaseUrl}")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    "kisan_queue_channel",
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(R.string.notification_channel_desc)
                    enableVibration(true)
                },
                NotificationChannel(
                    "kisan_system_channel",
                    "System Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "System maintenance and update alerts"
                }
            )
            val notifManager = getSystemService(NotificationManager::class.java)
            channels.forEach { notifManager.createNotificationChannel(it) }
        }
    }
}
