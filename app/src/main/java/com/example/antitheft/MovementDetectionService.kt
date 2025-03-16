package com.example.antitheft

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File

class MovementDetectionService : Service() {

    private lateinit var movementDetector: MovementDetector
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    override fun onCreate() {
        super.onCreate()

        // Initialize the SensorManager and sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Initialize the MovementDetector
        movementDetector = MovementDetector(
            context = this,
            onMovementDetected = {
                // Send SMS to contacts when movement is detected
                sendSMSToContacts()
                Toast.makeText(this, "Movement Detected! SMS sent to contacts.", Toast.LENGTH_SHORT).show()
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
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

    // Function to send SMS to contacts
    private fun sendSMSToContacts() {
        val contacts = readContactsFromFile()

        // Get the device location
        getDeviceLocation { location ->
            val locationMessage = if (location != null) {
                // Create a Google Maps link with the latitude and longitude
                "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
            } else {
                "https://maps.app.goo.gl/CgmQqk9VbDfi2k99A"
            }

            // Create the SMS message with the location link
            val message = "An unusual movement occurred. Make sure your device is safe with you.\n\nTrack your device here: $locationMessage"

            // Send the SMS to each contact
            for (contact in contacts) {
                sendSMS(contact, message)
            }
        }
    }

    // Function to get device location
    private fun getDeviceLocation(callback: (Location?) -> Unit) {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    callback(location) // Pass the location to the callback
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                    callback(null) // Pass null if location retrieval fails
                }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            callback(null) // Pass null if permission is denied
        }
    }

    // Function to read contacts from file
    private fun readContactsFromFile(): List<String> {
        val contactsDir = File(getExternalFilesDir(null), "SentinelX/Contacts").apply {
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
    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "SMS sent to $phoneNumber", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS to $phoneNumber: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

// Helper function to handle sound alert toggle
fun handleSoundAlertToggle(context: Context, isEnabled: Boolean) {
    if (isEnabled) {
        // Start the MovementDetectionService
        val intent = Intent(context, MovementDetectionService::class.java)
        context.startService(intent)
    } else {
        // Stop the MovementDetectionService
        val intent = Intent(context, MovementDetectionService::class.java)
        context.stopService(intent)
    }
    // Save the sound alert state
    saveSoundAlertState(context, isEnabled)
}