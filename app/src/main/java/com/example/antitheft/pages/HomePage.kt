package com.example.antitheft.pages

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.antitheft.AuthState
import com.example.antitheft.AuthViewModel
import com.example.antitheft.R
import com.example.antitheft.ui.NavScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
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
                    } else {
                        // Handle case where the document does not exist
                        userName = "Unknown User"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Handle Authentication State
    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                val errorMessage = state.message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    // Handle Back Button
    BackHandler {
        (context as? Activity)?.finish()
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

    DrawerScaffold(
        title = "Welcome to Sentinel X",
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
                    text = "Welcome to the Home Page",
                    color = Color.White,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}




@Composable
fun DrawerScaffold(
    title: String,
    navController: NavController,
    drawerItems: List<NavScreens>,
    userName: String,
    userImageUri: Uri?,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp)
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sentinel-X",
                        style = MaterialTheme.typography.h5.copy(color = Color.White),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile Picture
                    Image(
                        painter = userImageUri?.let { rememberAsyncImagePainter(it) }
                            ?: painterResource(id = R.drawable.userpic),
                        contentDescription = "User Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // User Name
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.subtitle1.copy(color = Color.White),
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    // Drawer Navigation Buttons
                    drawerItems.forEach { screen ->
                        Button(
                            onClick = {
                                val currentRoute = navController.currentBackStackEntry?.destination?.route
                                if (currentRoute != screen.screen) {
                                    navController.navigate(screen.screen) {
                                        launchSingleTop = true
                                        popUpTo(currentRoute ?: screen.screen) { inclusive = true }
                                    }
                                }
                                coroutineScope.launch {
                                    scaffoldState.drawerState.close()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = screen.screen.split(" ")
                                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                                color = Color.White
                            )

                        }

                    }

                    Spacer(modifier = Modifier.weight(1f)) // Push logout button to the bottom

                    // Logout Button
                    Button(
                        onClick = {
                            onLogout()
                            coroutineScope.launch {
                                scaffoldState.drawerState.close()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Logout", color = Color.White)
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(text = title, color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                scaffoldState.drawerState.open()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                backgroundColor = Color.Black
            )
        },
        drawerBackgroundColor = Color.Black,
        backgroundColor = Color.Black
    ) { innerPadding ->
        content(innerPadding)
    }
}
