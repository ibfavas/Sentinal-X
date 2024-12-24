package com.example.antitheft.appsetup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable

fun GestureControl(navController: NavHostController) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()
        .background(Color.Black)
    ){

    }


}