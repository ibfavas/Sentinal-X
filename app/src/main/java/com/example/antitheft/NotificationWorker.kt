package com.example.antitheft

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Send the notification
        sendNotification()
        return Result.success()
    }

    private fun sendNotification() {
        val channelId = "notification_channel"
        val notificationId = 1
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android 8.0 and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Anti-Theft Alert")
            .setContentText("Check your app for the latest activity.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
fun scheduleNotificationWork(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(8, TimeUnit.HOURS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "NotificationWork",
        androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Avoid replacing an existing work
        workRequest
    )
}
fun saveNotificationState(context: Context, isEnabled: Boolean) {
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    sharedPreferences.edit().putBoolean("notifications_enabled", isEnabled).apply()
}

fun getNotificationState(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("notifications_enabled", false)
}
