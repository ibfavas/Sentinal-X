package com.example.antitheft.pages

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun FakeShutdownScreen(activity: Activity, onShutdownComplete: () -> Unit) {
    var isBlackout by remember { mutableStateOf(false) }

    val screenAlpha by animateFloatAsState(
        targetValue = if (isBlackout) 0f else 1f,
        animationSpec = tween(2000)
    )

    LaunchedEffect(Unit) {
        delay(3000) // Show the shutdown UI for 3 seconds
        isBlackout = true
        delay(500) // Short delay before final blackout
        onShutdownComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = screenAlpha)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Shutting down...",
                color = Color.White,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 5.dp
            )
        }
    }
}
