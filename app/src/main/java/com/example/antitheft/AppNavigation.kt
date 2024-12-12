package com.example.antitheft

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.antitheft.pages.HomePage
import com.example.antitheft.pages.LoginPage
import com.example.antitheft.pages.SignupPage
import com.example.antitheft.pages.SplashScreen

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
    })

}