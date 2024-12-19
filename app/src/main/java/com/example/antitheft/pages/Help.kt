package com.example.antitheft.pages

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.antitheft.AuthState
import com.example.antitheft.AuthViewModel
import com.example.antitheft.ui.NavScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Help(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    var userName by remember { mutableStateOf("Loading...") }
    var userImageUri by remember { mutableStateOf<Uri?>(null) }

    // Fetch user details from Firebase
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("name") ?: "Unknown User"
                        userImageUri = document.getString("profileImageUrl")?.let(Uri::parse)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show()
            navController.navigate("login") {
                popUpTo("app_setup") { inclusive = true }
            }
        }
    }

    // Handle Authentication State
    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login") {
                    popUpTo("app_setup") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                val errorMessage = state.message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    val localImageUri = fetchLocalImage(context)

    val profileImageUri = localImageUri ?: userImageUri

    // Drawer items
    val drawerItems = listOf(
        NavScreens.HomePage,
        NavScreens.Profile,
        NavScreens.DataBackup,
        NavScreens.AppSetup,
        NavScreens.Help,
        NavScreens.Settings
    )

    // Using DrawerScaffold for AppSetup page
    DrawerScaffold(
        title = "Help",
        navController = navController,
        drawerItems = drawerItems,
        userName = userName,
        userImageUri = profileImageUri,
        onLogout = { authViewModel.signout() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to the Help Page",
                    color = Color.White,
                    style = MaterialTheme.typography.body1
                )

                // You can add other setup UI elements here (e.g., settings options, form inputs, etc.)
            }
        }
    }
}
