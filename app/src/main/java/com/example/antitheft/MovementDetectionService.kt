package com.example.antitheft

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat

class MovementDetectionService : Service() {

    private lateinit var movementDetector: MovementDetector

    override fun onCreate() {
        super.onCreate()

        // Initialize the MovementDetector
        movementDetector = MovementDetector(
            context = this,
            onMovementDetected = {
                Toast.makeText(this, "Movement Detected!", Toast.LENGTH_SHORT).show()
            }
        )

        // Start movement detection
        movementDetector.start()

        // Start foreground service with a notification
        startForegroundServiceWithNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop movement detection
        movementDetector.stop()
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "movement_detection_service_channel"
        val channelName = "Movement Detection Service"

        // Create notification channel for Android 8.0 and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Use NotificationCompat.Builder for compatibility with lower API levels
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Anti-Theft Active")
            .setContentText("Monitoring movement...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Adjust priority
            .build()

        // Start the service as a foreground service
        startForeground(1, notification)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null // Not used for this service
    }
}
fun startMovementDetectionService(context: Context) {
    val intent = Intent(context, MovementDetectionService::class.java)
    context.startService(intent)
}
fun stopMovementDetectionService(context: Context) {
    val intent = Intent(context, MovementDetectionService::class.java)
    context.stopService(intent)
}
fun handleSoundAlertToggle(context: Context, isEnabled: Boolean) {
    if (isEnabled) {
        startMovementDetectionService(context)
    } else {
        stopMovementDetectionService(context)
    }
    saveSoundAlertState(context, isEnabled)
}
