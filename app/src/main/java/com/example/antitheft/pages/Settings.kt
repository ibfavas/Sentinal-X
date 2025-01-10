package com.example.antitheft.pages

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
import com.example.antitheft.AuthState
import com.example.antitheft.AuthViewModel
import com.example.antitheft.ui.NavScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    var userName by remember { mutableStateOf("Loading...") }
    var userImageUri by remember { mutableStateOf<Uri?>(null) }

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
                .background(Color.Black)
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
                    color = Color.White,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Settings fields with lighter hex colors
                SettingsToggleItem(
                    icon = Icons.Default.Brightness6,
                    title = "Day/Night Mode",
                    description = "Switch between day and night themes",
                    isChecked = true,
                    onToggle = { /* Handle toggle action */ },
                    iconColor = Color(0xFFECE29C) // Light Yellow
                )

                SettingsListItem(
                    icon = Icons.Default.VisibilityOff,
                    title = "Stealth Mode",
                    description = "Enable or disable stealth mode",
                    onClick = { /* Navigate or enable stealth mode */ },
                    iconColor = Color(0xFFF39087) // Light Red
                )

                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    description = "Enable or disable notifications",
                    isChecked = true,
                    onToggle = { /* Handle toggle action */ },
                    iconColor = Color(0xFF95D598) // Light Green
                )

                SettingsListItem(
                    icon = Icons.Default.Palette,
                    title = "Themes",
                    description = "Choose app themes",
                    onClick = { /* Navigate to Themes screen */ },
                    iconColor = Color(0xFF98B7D7) // Light Blue
                )

                SettingsToggleItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound Alert",
                    description = "Enable or disable sound alerts",
                    isChecked = true,
                    onToggle = { /* Handle toggle action */ },
                    iconColor = Color(0xFFA9E7EF) // Light Cyan
                )

                SettingsListItem(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    description = "Set up app lock for enhanced security",
                    onClick = { /* Navigate to App Lock screen */ },
                    iconColor = Color(0xFFDFC3E5) // Light Magenta
                )


                Spacer(modifier = Modifier.height(24.dp))

                // Help section
                Text(
                    text = "Help",
                    color = Color.White,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                SettingsListItem(
                    icon = Icons.Default.Feedback,
                    title = "Feedback",
                    description = "Send feedback",
                    onClick = { /* Navigate to Feedback screen */ },
                    iconColor = Color(0xFFE1DCA9)
                )
                SettingsListItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    description = "View privacy policy",
                    onClick = { /* Navigate to Privacy Policy screen */ },
                    iconColor = Color(0xFFB9DEBA)
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                        .offset(y=25.dp)
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
                Text(text = title, fontSize = 18.sp) // Increased font size
                Text(
                    text = description,
                    style = MaterialTheme.typography.caption.copy(fontSize = 14.sp)
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
            Text(text = title, fontSize = 18.sp) // Increased font size
            Text(
                text = description,
                style = MaterialTheme.typography.caption.copy(fontSize = 14.sp)
            )
        }
    }
}