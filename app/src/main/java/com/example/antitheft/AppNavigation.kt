package com.example.antitheft

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.antitheft.pages.AppSetup
import com.example.antitheft.pages.Calculator
import com.example.antitheft.pages.DataBackup
import com.example.antitheft.pages.Help
import com.example.antitheft.pages.HomePage
import com.example.antitheft.pages.LoginPage
import com.example.antitheft.pages.PrivacyPolicyScreen
import com.example.antitheft.pages.Profile
import com.example.antitheft.pages.Settings
import com.example.antitheft.pages.SignupPage
import com.example.antitheft.pages.SplashScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    context : Context,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    stealthModeEnabled: Boolean,
) {
    val navController = rememberNavController()

    // Reactively manage start destination based on Stealth Mode
    val startDestination = remember(stealthModeEnabled) {
        if (stealthModeEnabled) "calculator" else "splash_screen"
    }

    NavHost(navController = navController, startDestination = startDestination) {
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
        composable("fingerprint_lock") {
            FingerprintLock(navController = navController)
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(navController = navController)
        }
        composable("calculator") {
            Calculator(navController = navController, context = context)
        }
    }
}
