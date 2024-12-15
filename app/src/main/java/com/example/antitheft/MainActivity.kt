package com.example.antitheft

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.antitheft.ui.theme.AntiTheftTheme
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Request permissions on app start
        PermissionUtils.requestPermissionsIfNeeded(
            activity = this,
            onPermissionsGranted = {
                // Permissions granted, proceed with the app setup
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            },
            onPermissionsDenied = {
                // Permissions denied, show explanation or handle accordingly
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()

                // Optionally open app settings to manually enable permissions
                openAppSettings(this)
            }
        )

        // Setting the Content View with Composables
        val authViewModel: AuthViewModel by viewModels()
        setContent {
            AntiTheftTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(authViewModel = authViewModel, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    // Open App Settings to let the user enable permissions manually
    private fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }
}
