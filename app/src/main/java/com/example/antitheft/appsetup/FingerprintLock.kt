package com.example.antitheft.appsetup

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.antitheft.BiometricAuthenticationActivity

@Composable
fun FingerprintLock(navController: NavHostController) {
    val context = LocalContext.current
    val isAuthenticated = remember { mutableStateOf(false) }

    // Register an ActivityResultLauncher using rememberLauncherForActivityResult
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val isSuccess = result.data?.getBooleanExtra("auth_success", false) ?: false
            if (isSuccess) {
                Toast.makeText(context, "Authentication Successful", Toast.LENGTH_SHORT).show()
                isAuthenticated.value = true
            } else {
                Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
                // Navigate back if authentication fails
                navController.popBackStack()
            }
        } else {
            Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
            // Navigate back if the user cancels authentication
            navController.popBackStack()
        }
    }

    // Launch the biometric authentication activity only when the composable is displayed
    LaunchedEffect(Unit) {
        if (!isAuthenticated.value) {
            // Launch the fingerprint authentication prompt
            launcher.launch(Intent(context, BiometricAuthenticationActivity::class.java))
        }
    }

    // Perform navigation once authenticated
    LaunchedEffect(isAuthenticated.value) {
        if (isAuthenticated.value) {
            navController.navigate("home") {
                popUpTo("fingerprint_lock") { inclusive = true }
            }
        }
        else{
            navController.popBackStack()
            }
        }

}
