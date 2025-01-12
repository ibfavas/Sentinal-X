package com.example.antitheft

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.antitheft.appsetup.EmergencyContacts
import com.example.antitheft.appsetup.EmergencyEmail
import com.example.antitheft.appsetup.FaceRegistration
import com.example.antitheft.appsetup.FingerprintLock
import com.example.antitheft.appsetup.PasswordLock
import com.example.antitheft.appsetup.PinLock
import com.example.antitheft.appsetup.RegisteredFaces
import com.example.antitheft.appsetup.Theme
import com.example.antitheft.pages.AppSetup
import com.example.antitheft.pages.DataBackup
import com.example.antitheft.pages.Help
import com.example.antitheft.pages.HomePage
import com.example.antitheft.pages.LoginPage
import com.example.antitheft.pages.Profile
import com.example.antitheft.pages.Settings
import com.example.antitheft.pages.SignupPage
import com.example.antitheft.pages.SplashScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel // Add ThemeViewModel here
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(modifier, navController, authViewModel)
        }
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home") {
            HomePage(modifier, navController, authViewModel)
        }
        composable("profile") {
            Profile(modifier, navController, authViewModel, themeViewModel)
        }
        composable("data backup") {
            DataBackup(modifier, navController, authViewModel)
        }
        composable("app setup") {
            AppSetup(modifier, navController, authViewModel)
        }
        composable("help") {
            Help(modifier, navController, authViewModel)
        }
        composable("settings") {
            // Pass the ThemeViewModel to the Settings composable
            Settings(modifier, navController, authViewModel, themeViewModel)
        }
        composable("face_registration") {
            FaceRegistration(navController = navController)
        }
        composable("registered_faces") {
            RegisteredFaces(navController = navController)
        }
        composable("emergency_contacts") {
            EmergencyContacts(navController = navController)
        }
        composable("emergency_email") {
            EmergencyEmail(navController = navController)
        }
        composable("pin_lock") {
            PinLock(navController = navController)
        }
        composable("password_lock") {
            PasswordLock(navController = navController, viewModel = themeViewModel)
        }
        composable("theme") {
            Theme(navController = navController)
        }
        composable("fingerprint_lock") {
            FingerprintLock(navController = navController)
        }
    }
}
