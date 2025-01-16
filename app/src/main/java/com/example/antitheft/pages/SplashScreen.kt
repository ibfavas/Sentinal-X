package com.example.antitheft.pages

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.antitheft.AuthViewModel
import com.example.antitheft.R
import com.example.antitheft.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    val scale = remember {
        Animatable(initialValue = 0f)
    }

    val isUserLoggedIn = remember {
        authViewModel.isUserLoggedIn()
    }

    val context = LocalContext.current
    val isStealthModeEnabled = getStealthModeState(context)

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.3f,
            animationSpec = tween(
                durationMillis = 400,
                easing = {
                    OvershootInterpolator(2f).getInterpolation(it)
                }
            )
        )
        delay(2000L)

        // First check the login status and then stealth mode
        if (isUserLoggedIn) {
            navController.navigate("home") {
                popUpTo("splash_screen") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash_screen") { inclusive = true }
            }
        }

        // After login decision, navigate based on stealth mode status
        if (!isUserLoggedIn && isStealthModeEnabled) {
            navController.navigate("calculator")
        }
    }

    // Wrap the splash screen in a theme that does not affect Stealth Mode UI
    AppTheme(darkTheme = isStealthModeEnabled) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
        ) {
            Image(
                painter = painterResource(R.drawable.splashscreen),
                contentDescription = "Logo",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
