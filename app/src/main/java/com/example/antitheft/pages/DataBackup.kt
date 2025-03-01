package com.example.antitheft.pages

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

            // Google Drive Option
            BackupOption(
                title = "Google Drive",
                iconRes = R.drawable.ic_gdrive,
                onClick = {
                    openGoogleDrive(context)  // Open Google Drive app
                }
            )

            // OneDrive Option
            BackupOption(
                title = "OneDrive",
                iconRes = R.drawable.ic_onedrive,
                onClick = {
                    openOneDrive(context)  // Open OneDrive app
                }
            )

            // Dropbox Option
            BackupOption(
                title = "Dropbox",
                iconRes = R.drawable.ic_dropbox,
                onClick = {
                    openDropbox(context)  // Open Dropbox app
                }
            )

            // Mega Option
            BackupOption(
                title = "Mega",
                iconRes = R.drawable.ic_mega,
                onClick = {
                    openMega(context)  // Open Mega app
                }
            )
            BackupOption(
                title = "iCloud",
                iconRes = R.drawable.ic_icloud,
                onClick = { /* Local backup logic or no action */ }
            )
            BackupOption(
                title = "Local Backup",
                iconRes = R.drawable.ic_local_backup,
                onClick = {  } // Trigger backup
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
        shape = RoundedCornerShape(12.dp),
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
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}

// Function to open Google Drive upload page
fun openGoogleDrive(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://drive.google.com/drive/u/0/folders") // Directly open the Google Drive folder
    }
    context.startActivity(intent) // Open Google Drive upload page in browser or app
}

// Function to open OneDrive upload page
fun openOneDrive(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://onedrive.live.com/") // Direct OneDrive web page
    }
    context.startActivity(intent) // Open OneDrive upload page in browser or app
}

// Function to open Dropbox upload page
fun openDropbox(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://www.dropbox.com/") // Direct Dropbox page
    }
    context.startActivity(intent) // Open Dropbox upload page in browser or app
}

// Function to open Mega app or Mega webpage if app is not installed
fun openMega(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage("com.megacorp.megasync") // Package name for Mega app
    if (intent != null) {
        context.startActivity(intent) // Open the Mega app
    } else {
        // If Mega app is not installed, open the Mega webpage
        val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://mega.io/") // Mega web page
        }
        context.startActivity(fallbackIntent) // Open Mega web page in browser
    }
}

