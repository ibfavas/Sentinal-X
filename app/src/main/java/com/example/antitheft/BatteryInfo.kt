package com.example.antitheft

import android.content.Context
import android.os.BatteryManager

fun getBatteryPercentage(context: Context): Int {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    return batteryLevel
}
