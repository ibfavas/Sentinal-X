package com.example.antitheft.pages

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.*
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
                .background(Color.Black)
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
                    "Emergency Email" to R.drawable.ic_email,
                    "Registered Faces" to R.drawable.userpic,
                    "Pin" to R.drawable.ic_pin,
                    "Pattern" to R.drawable.ic_pattern,
                    "Gesture Control" to R.drawable.ic_gesture,
                    "Fingerprint" to R.drawable.ic_fingerprint
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
                                modifier = Modifier
                                    .weight(1f) // Ensure items are evenly spaced
                                    .aspectRatio(1f), // Square cards
                                onClick = {

                                    when (field) {
                                        "Face Registration" -> navController.navigate("face_registration")
                                        "Registered Faces" -> navController.navigate("registered_faces")
                                        "Emergency Contacts" -> navController.navigate("emergency_contacts")
                                        "Emergency Email" -> navController.navigate("emergency_email")
                                        "Pin" -> navController.navigate("pin_lock")
                                        "Pattern" -> navController.navigate("pattern_lock")
                                        "Gesture Control" -> navController.navigate("gesture_control")
                                        "Fingerprint" -> navController.navigate("fingerprint_lock")
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
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
            )
            Spacer(modifier = Modifier.height(4.dp)) // Reduce space between icon and text
            Text(
                text = title,
                style = MaterialTheme.typography.body1,
                color = Color.White,
                fontSize = 12.sp, // Smaller font size for better fit
                textAlign = TextAlign.Center
            )
        }
    }
}
