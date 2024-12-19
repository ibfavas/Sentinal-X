package com.example.antitheft

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
fun  AppNavigation(modifier: Modifier=Modifier,authViewModel: AuthViewModel){
    val navController = rememberNavController()

    NavHost(navController=navController, startDestination = "splash_screen", builder ={
        composable("splash_screen") {
        SplashScreen(modifier,navController,authViewModel)
        }
        composable("login") {
            LoginPage(modifier,navController,authViewModel)
        }
        composable("signup") {
            SignupPage(modifier,navController,authViewModel)
        }
        composable("home") {
            HomePage(modifier,navController,authViewModel)
        }
        composable("profile") {
            Profile(modifier,navController,authViewModel)
        }
        composable("data backup") {
            DataBackup(modifier,navController,authViewModel)
        }
        composable("app setup") {
            AppSetup(modifier,navController,authViewModel)
        }
        composable("help") {
            Help(modifier,navController,authViewModel)
        }
        composable("settings") {
            Settings(modifier,navController,authViewModel)
        }
    })

}