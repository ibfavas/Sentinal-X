package com.example.antitheft.pages

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.antitheft.AuthViewModel
import com.example.antitheft.R
import com.example.antitheft.ui.NavScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun DataBackup(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("Loading...") }
    var userImageUri by remember { mutableStateOf<Uri?>(null) }

    // Fetch user details from Firebase
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            try {
                val document = db.collection("users").document(userId).get().await()
                if (document.exists()) {
                    userName = document.getString("name") ?: "Unknown User"
                    userImageUri = document.getString("profileImageUrl")?.let { Uri.parse(it) }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show()
            navController.navigate("login") {
                popUpTo("data_backup") { inclusive = true }
            }
        }
    }

    val localImageUri = fetchLocalImage(context)
    val profileImageUri = localImageUri ?: userImageUri

    val drawerItems = listOf(
        NavScreens.HomePage,
        NavScreens.Profile,
        NavScreens.DataBackup,
        NavScreens.AppSetup,
        NavScreens.Help,
        NavScreens.Settings
    )

    // Using DrawerScaffold
    DrawerScaffold(
        title = "Data Backup",
        navController = navController,
        drawerItems = drawerItems,
        userName = userName,
        userImageUri = profileImageUri,
        onLogout = {
            authViewModel.signout() // Perform the sign out action
            // Navigate to login screen after sign out
            navController.navigate("login") {
                popUpTo("data_backup") { inclusive = true }
            }
        }
    ) { innerPadding ->

        // Column for backup options
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .offset(y = 15.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BackupOption(
                title = "Google Drive",
                iconRes = R.drawable.ic_gdrive,
                onClick = { /* Add Google Drive backup logic */ }
            )
            BackupOption(
                title = "OneDrive",
                iconRes = R.drawable.ic_onedrive,
                onClick = { /* Add OneDrive backup logic */ }
            )
            BackupOption(
                title = "Mega",
                iconRes = R.drawable.ic_mega,
                onClick = { /* Add Mega backup logic */ }
            )
            BackupOption(
                title = "Local Backup",
                iconRes = R.drawable.ic_local_backup,
                onClick = { /* Add Local Backup logic */ }
            )
            BackupOption(
                title = "iCloud",
                iconRes = R.drawable.ic_icloud,
                onClick = { /* Add iCloud backup logic */ }
            )
            BackupOption(
                title = "Dropbox",
                iconRes = R.drawable.ic_dropbox,
                onClick = { /* Add Dropbox backup logic */ }
            )
        }
    }
}

@Composable
fun BackupOption(title: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}
