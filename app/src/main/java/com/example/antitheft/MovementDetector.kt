package com.example.antitheft

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.MediaPlayer
import android.telephony.SmsManager
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File

class MovementDetector(
    private val context: Context,
    private val onMovementDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var isSoundAlertEnabled = getSoundAlertState(context)

    private var lastAlertTime = 0L // Time of the last sound alert
    private var isSoundAlertPlaying = false // Flag to track if sound is playing
    private var mediaPlayer: MediaPlayer? = null // To keep track of the current MediaPlayer instance

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        stopSoundAlert() // Stop sound if the detector is stopped
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isSoundAlertEnabled) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val accelX = event.values[0]
                val accelY = event.values[1]
                val accelZ = event.values[2]
                val accelMagnitude = Math.sqrt((accelX * accelX + accelY * accelY + accelZ * accelZ).toDouble())

                // Threshold for significant movement
                if (accelMagnitude > 50 && !isSoundAlertPlaying) {
                    // Avoid triggering multiple alerts in quick succession
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastAlertTime > 50000) { // 5 seconds cooldown
                        lastAlertTime = currentTime
                        isSoundAlertPlaying = true // Mark sound as playing
                        playSoundAlert(context) // Trigger sound alert

                        // Send SMS to contacts with location
                        sendSMSToContacts()
                        onMovementDetected() // Optional: Trigger additional movement detected logic
                    }
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val gyroX = event.values[0]
                val gyroY = event.values[1]
                val gyroZ = event.values[2]
                val gyroMagnitude = Math.sqrt((gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ).toDouble())

                // Threshold for significant angular velocity
                if (gyroMagnitude > 10.0 && !isSoundAlertPlaying) { // Adjust threshold as needed
                    // Avoid triggering multiple alerts in quick succession
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastAlertTime > 50000) { // 5 seconds cooldown
                        lastAlertTime = currentTime
                        isSoundAlertPlaying = true // Mark sound as playing
                        playSoundAlert(context) // Trigger sound alert
                        onMovementDetected() // Optional: Trigger additional movement detected logic
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setSoundAlertEnabled(isEnabled: Boolean) {
        isSoundAlertEnabled = isEnabled
        saveSoundAlertState(context, isEnabled)

        if (!isEnabled) {
            stopSoundAlert() // Stop sound when the toggle is turned off
        }
    }

    // Method to stop the sound playback
    fun stopSoundAlert() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.stop()  // Stop playback
                mp.release()  // Release resources
            }
            isSoundAlertPlaying = false  // Reset flag
            mediaPlayer = null // Ensure it's properly reset
        }
    }

    // Reset sound alert state when sound finishes playing
    fun resetSoundAlertState() {
        isSoundAlertPlaying = false
        mediaPlayer?.release()  // Release resources if any
        mediaPlayer = null  // Nullify to avoid future references to an invalid MediaPlayer
    }

    // Method to play sound alert
    fun playSoundAlert(context: Context) {
        try {
            // If there's already a sound playing, stop it before starting a new one
            stopSoundAlert()

            // Create a new MediaPlayer instance and start the sound alert
            mediaPlayer = MediaPlayer.create(context, R.raw.mingle) // Replace with your actual sound file
            mediaPlayer?.start()

            // Set a listener to reset the state after the sound finishes playing
            mediaPlayer?.setOnCompletionListener {
                resetSoundAlertState()
                it.release() // Release the MediaPlayer when done playing
            }

            // Optionally show a toast
            Toast.makeText(context, "Sound Alert Triggered", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error playing sound alert", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to send SMS to contacts
    private fun sendSMSToContacts() {
        val contacts = readContactsFromFile()

        // Get the device location
        getDeviceLocation(context) { location ->
            val locationMessage = if (location != null) {
                // Create a Google Maps link with the latitude and longitude
                "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
            } else {
                "Location unavailable"
            }

            // Create the SMS message with the location link
            val message = "An unusual movement occurred. Make sure your device is safe with you.\n\nTrack your device here: $locationMessage"

            // Send the SMS to each contact
            for (contact in contacts) {
                sendSMS(context, contact, message)
            }
        }
    }

    // Function to read contacts from file
    private fun readContactsFromFile(): List<String> {
        val contactsDir = File(context.getExternalFilesDir(null), "SentinelX/Contacts").apply {
            if (!exists()) mkdirs()
        }

        val contactsFile = File(contactsDir, "contacts.txt")
        return if (contactsFile.exists()) {
            contactsFile.readLines()
                .mapNotNull { line ->
                    // Split the line by comma and take the second part (phone number)
                    line.split(",").getOrNull(1)?.trim()
                }
        } else {
            emptyList()
        }
    }

    // Function to send SMS
    private fun sendSMS(context: Context, phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(context, "SMS sent to $phoneNumber", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send SMS to $phoneNumber: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to get device location
    private fun getDeviceLocation(context: Context, callback: (Location?) -> Unit) {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    callback(location) // Pass the location to the callback
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                    callback(null) // Pass null if location retrieval fails
                }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            callback(null) // Pass null if permission is denied
        }
    }
}

// SharedPreferences helper functions
fun saveSoundAlertState(context: Context, isEnabled: Boolean) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putBoolean("soundAlertEnabled", isEnabled)
    editor.apply()
}

fun getSoundAlertState(context: Context): Boolean {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("soundAlertEnabled", false) // Default to false
}