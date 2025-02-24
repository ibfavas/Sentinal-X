package com.example.antitheft

import PermissionUtils
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.antitheft.pages.getStealthModeState
import com.example.antitheft.ui.theme.AppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Request permissions
        PermissionUtils.requestPermissionsIfNeeded(
            activity = this,
            onPermissionsGranted = {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            },
            onPermissionsDenied = {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                openAppSettings(this)
            }
        )

        // ViewModels
        val authViewModel: AuthViewModel by viewModels()
        val themeViewModel: ThemeViewModel by viewModels {
            ThemeViewModelFactory(applicationContext)
        }

        // Set the content view
        setContent {
            AppContent(authViewModel = authViewModel, themeViewModel = themeViewModel)
        }
    }

    // Open the app settings to manually grant permissions if denied
    private fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppContent(authViewModel: AuthViewModel, themeViewModel: ThemeViewModel) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    // Use SystemUiController to change the status bar color based on the theme
    val systemUiController = rememberSystemUiController()

    val context = LocalContext.current
    val stealthModeEnabled = getStealthModeState(context)

    // Set status bar color and icons based on the theme
    val statusBarColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.onBackground // Dark mode status bar color
    } else {
        MaterialTheme.colorScheme.background // Light mode status bar color
    }

    // Apply the status bar color
    systemUiController.setSystemBarsColor(
        color = statusBarColor,
        darkIcons = !isDarkTheme // Dark icons for light theme, light icons for dark theme
    )


    AppTheme(darkTheme = isDarkTheme) {
        AppNavigation(authViewModel = authViewModel, context = context, themeViewModel = themeViewModel, stealthModeEnabled = stealthModeEnabled)
    }
}
