package com.example.antitheft

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PowerButtonReceiver : BroadcastReceiver() {
    private var lastPressTime: Long = 0
    private val DOUBLE_PRESS_INTERVAL = 500 // 500ms for double press

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastPressTime < DOUBLE_PRESS_INTERVAL) {
                // Double press detected
                val launchIntent = Intent(context, PowerMenuActivity::class.java)
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
            }
            lastPressTime = currentTime
        }
    }
}
