package com.example.antitheft.pages

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.work.WorkManager
import com.example.antitheft.AuthState
import com.example.antitheft.AuthViewModel
import com.example.antitheft.MovementDetector
import com.example.antitheft.ThemeViewModel
import com.example.antitheft.getNotificationState
import com.example.antitheft.getSoundAlertState
import com.example.antitheft.handleSoundAlertToggle
import com.example.antitheft.saveNotificationState
import com.example.antitheft.scheduleNotificationWork
import com.example.antitheft.ui.NavScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    var userName by remember { mutableStateOf("Loading...") }
    var userImageUri by remember { mutableStateOf<Uri?>(null) }

    var isSoundAlertEnabled by remember { mutableStateOf(getSoundAlertState(context)) }
    var isSoundAlertPlaying by remember { mutableStateOf(false) }

    var isNotificationsEnabled by remember { mutableStateOf(getNotificationState(context)) }

    var isStealthModeEnabled by remember { mutableStateOf(getStealthModeState(context)) }

// Update stealth mode state if changed
    LaunchedEffect(isStealthModeEnabled) {
        saveStealthModeState(context, isStealthModeEnabled)
    }

    val movementDetector = remember(context) {
        MovementDetector(context) {
            // Trigger sound alert when movement is detected
            if (isSoundAlertEnabled && !isSoundAlertPlaying) {
                // Play sound only if it's not already playing
                    isSoundAlertPlaying = true
            }
        }
    }

    // Add the send feedback logic
    val sendFeedbackEmail: () -> Unit = {
        val recipient = "feedback@example.com"  // Replace with your actual recipient email
        val subject = "App Feedback"
        val body = ""

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$recipient")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        // Try to open Gmail explicitly
        try {
            val gmailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            gmailIntent.setPackage("com.google.android.gm") // Gmail package name
            gmailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            gmailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            gmailIntent.putExtra(Intent.EXTRA_TEXT, body)
            context.startActivity(gmailIntent) // Attempt to open Gmail directly
        } catch (e: Exception) {
            // If Gmail is not installed, open the default email app
            context.startActivity(Intent.createChooser(intent, "Choose an Email client"))
        }
    }

    // Start or stop the movement detector depending on the toggle state
    LaunchedEffect(isSoundAlertEnabled) {
        if (isSoundAlertEnabled) {
            movementDetector.start()
        } else {
            movementDetector.stop()
            // Ensure the sound alert is stopped properly when toggled off
            if (isSoundAlertPlaying) {
                movementDetector.stopSoundAlert() // Stop sound if playing
                isSoundAlertPlaying = false // Reset the flag
            }
        }
    }

    // Fetch user details from Firebase
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("name") ?: "Unknown User"
                        userImageUri = document.getString("profileImageUrl")?.let(Uri::parse)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show()
            navController.navigate("login") {
                popUpTo("app_setup") { inclusive = true }
            }
        }
    }

    // Handle Authentication State
    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login") {
                    popUpTo("app_setup") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                val errorMessage = state.message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    // Fetch the locally stored profile image
    val localImageUri = fetchLocalImage(context)

    // Use the local image URI if available, otherwise use the Firebase URI
    val profileImageUri = localImageUri ?: userImageUri

    // Drawer items
    val drawerItems = listOf(
        NavScreens.HomePage,
        NavScreens.Profile,
        NavScreens.DataBackup,
        NavScreens.AppSetup,
        NavScreens.Help,
        NavScreens.Settings
    )

    // Using DrawerScaffold for AppSetup page
    DrawerScaffold(
        title = "Settings",
        navController = navController,
        drawerItems = drawerItems,
        userName = userName,
        userImageUri = profileImageUri,
        onLogout = { authViewModel.signout() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // Welcome Text
                Text(
                    text = "Settings",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Settings fields with lighter hex colors
                SettingsToggleItem(
                    icon = Icons.Default.Brightness6,
                    title = "Day/Night Mode",
                    description = "Switch between light and dark themes",
                    isChecked = isDarkTheme,
                    onToggle = { themeViewModel.toggleTheme() },
                    iconColor = if (isDarkTheme) Color(0xFFECE29C) else Color(0xFF6B6B6B)
                )

                SettingsToggleItem(
                    icon = Icons.Default.VisibilityOff,
                    title = "Stealth Mode",
                    description = "Enable or disable stealth mode",
                    isChecked = isStealthModeEnabled, // Use the local mutable state
                    onToggle = { isEnabled ->
                        // Immediately update local state and persist it
                        isStealthModeEnabled = isEnabled
                        saveStealthModeState(context, isEnabled) // Persist the change
                        // Additional behavior: Optionally, you could also trigger any actions related to stealth mode being toggled
                        if (isEnabled) {
                            // For example, enable stealth mode-related features
                        } else {
                            // For example, disable stealth mode-related features
                        }
                    },
                    iconColor = Color(0xFFF39087) // Light Red
                )



                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    description = "Enable or disable notifications",
                    isChecked = isNotificationsEnabled,
                    onToggle = { isEnabled ->
                        isNotificationsEnabled = isEnabled
                        saveNotificationState(context, isEnabled)

                        if (isEnabled) {
                            scheduleNotificationWork(context) // Start the periodic notifications
                        } else {
                            WorkManager.getInstance(context).cancelUniqueWork("NotificationWork") // Stop the notifications
                        }
                    },
                    iconColor = Color(0xFF95D598) // Light Green
                )


                SettingsListItem(
                    icon = Icons.Default.Palette,
                    title = "Themes",
                    description = "Choose app themes",
                    onClick = {
                        Toast.makeText(context, "Upcoming Feature", Toast.LENGTH_SHORT).show()
                        // Navigate to Themes screen or other logic
                    },
                    iconColor = Color(0xFF98B7D7) // Light Blue
                )


                SettingsToggleItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound Alert",
                    description = "Enable or disable sound alerts when rigorous movement is detected",
                    isChecked = isSoundAlertEnabled,
                    onToggle = { newState ->
                        isSoundAlertEnabled = newState
                        handleSoundAlertToggle(context, newState)
                    },
                    iconColor = Color(0xFFA9E7EF) // Light Cyan
                )



                SettingsToggleItem(
                    icon = Icons.Default.Lock,
                    title = "Fake Shutdown",
                    description = "Enable or disable fake shutdown when unknown user is identified",
                    isChecked = true,
                    onToggle = {  },
                    iconColor = Color(0xFFDFC3E5) // Light Magenta
                )


                Spacer(modifier = Modifier.height(10.dp))

                // Help section
                Text(
                    text = "Help",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                SettingsListItem(
                    icon = Icons.Default.Feedback,
                    title = "Feedback",
                    description = "Send feedback",
                    onClick = sendFeedbackEmail, // Call the function to send email
                    iconColor = Color(0xFFE1DCA9)
                )
                SettingsListItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    description = "View privacy policy",
                    onClick = {
                        // Navigate to Privacy Policy screen
                        navController.navigate("privacy_policy")
                    },
                    iconColor = Color(0xFFB9DEBA) // Light Green
                )


                Spacer(modifier = Modifier.height(20.dp))

                // Save Settings field
                SettingsListItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Delete your Account",
                    description = "Delete your account permanently.",
                    onClick = { /* Save settings logic */ },
                    iconColor = Color(0xFFDE8282)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y=24.dp)
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = "App Version: 1.0.0", // Replace with your app's actual version
                        color = Color.Gray,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp), // Increased size for the icon
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 18.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground) // Increased font size
                Text(
                    text = description,
                    style = MaterialTheme.typography.caption.copy(fontSize = 14.sp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Switch(checked = isChecked, onCheckedChange = onToggle)
    }
}

@Composable
fun SettingsListItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp), // Increased size for the icon
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 18.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground) // Increased font size
            Text(
                text = description,
                style = MaterialTheme.typography.caption.copy(fontSize = 14.sp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

private const val PREFS_NAME_STEALTH = "StealthModePreferences"
private const val KEY_STEALTH_MODE = "stealth_mode"

fun saveStealthModeState(context: Context, isEnabled: Boolean) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME_STEALTH, Context.MODE_PRIVATE) // Using a different prefs file
    sharedPreferences.edit().putBoolean(KEY_STEALTH_MODE, isEnabled).apply()
}

fun getStealthModeState(context: Context): Boolean {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME_STEALTH, Context.MODE_PRIVATE) // Using a different prefs file
    return sharedPreferences.getBoolean(KEY_STEALTH_MODE, false)
}
