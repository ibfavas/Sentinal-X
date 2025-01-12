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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.antitheft.ui.theme.AppTheme
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

    AppTheme(darkTheme = isDarkTheme) {
        AppNavigation(authViewModel = authViewModel, themeViewModel = themeViewModel)
    }
}
