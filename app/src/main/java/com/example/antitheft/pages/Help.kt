package com.example.antitheft.pages

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    // Questions and answers for the Help page
    val faqItems = listOf(
        "What are the permissions required for the app to work properly?" to "You have to give the app access to Network, Camera, Location etc. which are asked by the app during initial startup.",
        "How do I back up my data?" to "Navigate to the Data Backup section. There you will find several cloud backup options. Follow the steps to upload your data to the cloud.",
        "How does the anti-theft feature work?" to "The app monitors unauthorized access and triggers alerts if suspicious activity is detected. If it detects some unauthorized user trying to access the device, it will capture the intruder's selfie and will send it to emergency contacts along with the location.",
        "How do I change my profile picture?" to "Go to your Profile page, and click on your current profile picture to update it.",
        "Is there support for pin and pattern locks?" to "Yes there is! Go to the App Setup section from the navigation drawer and you will find options to set up pin and pattern lock along with face recognition support.",
        "What is Stealth Mode?" to "It is a feature which enables the user to disguise the app as another app. It helps to enhance the security of the application.",
        "Can I use the app on multiple devices?" to "Yes, but you need to sign in with the same account on all devices.",
        "Why am I not receiving alerts?" to "Ensure notifications are enabled, and the app has permission to access background activities.",
        "What is a fake shutdown?" to "The fake shutdown feature prevents unauthorized users from shutting down the device. Instead, it displays a shutdown animation while keeping the device active.",
        "How do I delete my account?" to "Navigate to the Settings page, click on 'Account Settings,' and choose the 'Delete Account' option. Note that this action is irreversible.",
        "What should I do if I encounter a bug?" to "Contact support or report the issue via the 'Feedback' option in the Settings page."
    )

    // Color palette for questions
    val colors = listOf(
        Color(0xFF06A4EC), // Blue
        Color(0xFF2196F3),
    )

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
                .background(brush = androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(Color.Black, Color.DarkGray)))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(faqItems.withIndex().toList()) { (index, faq) ->
                    val questionColor = colors[index % colors.size]
                    FAQItem(
                        question = faq.first,
                        answer = faq.second,
                        backgroundColor = questionColor
                    )
                }
            }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String, backgroundColor: Color) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp)) // Rounded corners
            .clickable { isExpanded = !isExpanded }
            .animateContentSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = question,
            color = Color.White,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (isExpanded) {
            Text(
                text = answer,
                color = Color.White,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        }
    }
}

