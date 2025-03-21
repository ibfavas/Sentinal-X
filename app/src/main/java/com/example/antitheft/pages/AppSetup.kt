package com.example.antitheft.pages

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.antitheft.AuthState
import com.example.antitheft.AuthViewModel
import com.example.antitheft.R
import com.example.antitheft.ui.NavScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppSetup(
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

    // Define colors for each field
    val fieldColors = mapOf(
        "Face Registration" to Color(0xFFBBDEFB), // Light Blue
        "Emergency Contacts" to Color(0xFFFFCDD2), // Light Red
        "Telegram ID" to Color(0xFFC8E6C9), // Light Green
        "Registered Faces" to Color(0xFFFFF9C4), // Light Yellow
        "Pin" to Color(0xFFE1BEE7), // Light Purple
        "Fingerprint" to Color(0xFFB2DFDB), // Light Teal
        "Password" to Color(0xFFFFCCBC), // Light Orange
        "Coming Soon" to Color(0xFFF8BBD0), // Light Pink
    )


    // Using DrawerScaffold for AppSetup page
    DrawerScaffold(
        title = "App Setup",
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
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Reduce vertical spacing
            ) {

                val fields = listOf(
                    "Face Registration" to R.drawable.ic_face_registration,
                    "Emergency Contacts" to R.drawable.ic_emergency_contacts,
                    "Telegram ID" to R.drawable.ic_email,
                    "Registered Faces" to R.drawable.userpic,
                    "Pin" to R.drawable.ic_pin,
                    "Fingerprint" to R.drawable.ic_fingerprint,
                    "Password" to R.drawable.ic_pattern,
                    "Coming Soon" to R.drawable.ic_soon,
                )

                // Use Grid Layout Logic
                val rows = fields.chunked(2) // Group fields into rows of 2
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // Reduce horizontal spacing
                    ) {
                        row.forEach { (field, icon) ->
                            FieldCard(
                                title = field,
                                iconRes = icon,
                                backgroundColor = fieldColors[field] ?: Color.Gray, // Set color from map or default to gray
                                modifier = Modifier
                                    .weight(1f) // Ensure items are evenly spaced
                                    .aspectRatio(1f), // Square cards
                                onClick = {
                                    when (field) {
                                        "Face Registration" -> navController.navigate("face_registration")
                                        "Registered Faces" -> navController.navigate("registered_faces")
                                        "Emergency Contacts" -> navController.navigate("emergency_contacts")
                                        "Telegram ID" -> navController.navigate("telegram_id")
                                        "Pin" -> navController.navigate("pin_lock")
                                        "Fingerprint" -> navController.navigate("fingerprint_lock")
                                        "Password" -> navController.navigate("password_lock")
                                        "Coming Soon" -> Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show()
                                        else -> navController.navigate("setup/$field")
                                    }
                                }
                            )
                        }

                        // Add a spacer if the row has less than 2 items
                        if (row.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FieldCard(
    title: String,
    iconRes: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(40.dp), // Adjusted icon size
                tint = Color.Black // Set icon color to black
            )

            Spacer(modifier = Modifier.height(4.dp)) // Reduce space between icon and text
            Text(
                text = title,
                style = MaterialTheme.typography.body1,
                color = Color.Black,
                fontSize = 12.sp, // Smaller font size for better fit
                textAlign = TextAlign.Center
            )
        }
    }
}
