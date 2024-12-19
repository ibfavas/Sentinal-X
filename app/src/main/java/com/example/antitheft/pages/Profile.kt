package com.example.antitheft.pages

import android.Manifest
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.example.antitheft.AuthViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.antitheft.ui.NavScreens



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Profile(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Request permissions for reading external storage and camera
    val permissionStateStorage = rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES)
    val permissionStateCamera = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val permissionStateStorage2 = rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)

    // Launcher to handle image picker result (gallery)
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            if (uri.scheme == "content" || uri.scheme == "file") {
                // Valid URI, do nothing for now
            } else {
                Toast.makeText(context, "Invalid image URI", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher to handle camera capture
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let {
                uploadImageToFirebaseStorage(it, context)
            } ?: Toast.makeText(context, "Image URI is null", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to create a temporary URI for the camera
    fun createTempUri(context: Context): Uri {
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir).apply {
            deleteOnExit()
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }

    // Handle permission requests when the Composable is first launched
    LaunchedEffect(Unit) {
        if (!permissionStateStorage.status.isGranted) {
            permissionStateStorage.launchPermissionRequest()
        }
        if (!permissionStateStorage2.status.isGranted) {
            permissionStateStorage2.launchPermissionRequest()
        }
        if (!permissionStateCamera.status.isGranted) {
            permissionStateCamera.launchPermissionRequest()
        }

        // Fetch user profile data from Firestore and update UI state
        fetchUserDataFromFirebase(context) { fetchedName, fetchedAge, fetchedDob, fetchedUri ->
            name = fetchedName
            age = fetchedAge
            dob = fetchedDob
            imageUri = fetchedUri
        }
    }

    val localImageUri = fetchLocalImage(context)

    val profileImageUri = localImageUri ?: imageUri

    val drawerItems = listOf(
        NavScreens.HomePage,
        NavScreens.Profile,
        NavScreens.DataBackup,
        NavScreens.AppSetup,
        NavScreens.Help,
        NavScreens.Settings
    )

    DrawerScaffold(
        title = "Profile",
        navController = navController,
        drawerItems = drawerItems,
        userName = name,
        userImageUri = profileImageUri,
        onLogout = { authViewModel.signout() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile picture section with clickable options
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable {
                            // Show an alert dialog or bottom sheet to choose between camera and gallery
                            val options = arrayOf("Take Photo", "Choose from Gallery")
                            AlertDialog.Builder(context)
                                .setTitle("Select Option")
                                .setItems(options) { _, which ->
                                    when (which) {
                                        0 -> { // Take Photo
                                            if (permissionStateCamera.status.isGranted) {
                                                val tempUri = createTempUri(context)
                                                imageUri = tempUri
                                                cameraLauncher.launch(tempUri)
                                            } else {
                                                permissionStateCamera.launchPermissionRequest()
                                            }
                                        }
                                        1 -> { // Choose from Gallery
                                            if (permissionStateStorage.status.isGranted) {
                                                imagePickerLauncher.launch("image/*")
                                            } else {
                                                permissionStateStorage.launchPermissionRequest()
                                            }
                                        }
                                    }
                                }
                                .create()
                                .show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val localImageUri = fetchLocalImage(context)

                    localImageUri?.let {
                        Image(
                            painter = rememberImagePainter(it),  // Use the painter to load the image
                            contentDescription = "User Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Text(text = "Add Photo", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Form to update user details
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = user?.email ?: "Unknown",
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save profile changes button
                Button(onClick = {
                    // Save the profile information
                    updateUserInfo(name, age, dob, imageUri, context)

                    // Save the image locally if imageUri is not null
                    imageUri?.let {
                        storeImageLocally(it, context)  // Call function to store the image locally
                    }
                }) {
                    Text("Save Profile Changes")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password Change Section
                Text(text = "Change Password", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Old Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Button(
                    modifier = Modifier.padding(16.dp),
                    onClick = {
                        changePassword(
                            oldPassword,
                            newPassword,
                            confirmPassword,
                            context
                        )
                    }
                ) {
                    Text("Change Password")
                }
            }
        }
    }
}

fun updateUserInfo(name: String, age: String, dob: String, imageUri: Uri?, context: Context) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: return

    val db = FirebaseFirestore.getInstance()
    val userUpdates = hashMapOf(
        "name" to name,
        "age" to age,
        "dob" to dob
    )

    // Add profileImageUrl only if imageUri is not null
    imageUri?.let {
        userUpdates["profileImageUrl"] = it.toString()
    }

    db.collection("users").document(userId).set(userUpdates)
        .addOnSuccessListener {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
        }
}


fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String, context: Context) {
    val user = FirebaseAuth.getInstance().currentUser

    if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
        return
    }

    if (newPassword != confirmPassword) {
        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
        return
    }

    val credential = EmailAuthProvider.getCredential(user!!.email!!, oldPassword)
    user.reauthenticate(credential)
        .addOnCompleteListener {
            if (it.isSuccessful) {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Password change failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Reauthentication failed", Toast.LENGTH_SHORT).show()
            }
        }
}

fun copyUriToFile(uri: Uri, context: Context): File? {
    val contentResolver: ContentResolver = context.contentResolver
    val file = File(context.cacheDir, "temp_image") // Temporary file in app's cache directory

    try {
        val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return null
        val outputStream: OutputStream = FileOutputStream(file)

        // Copy the input stream to the output file
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } != -1) {
            outputStream.write(buffer, 0, length)
        }

        inputStream.close()
        outputStream.flush()
        outputStream.close()

        return file
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun uploadImageToFirebaseStorage(uri: Uri, context: Context) {
    val tempFile = copyUriToFile(uri, context) ?: run {
        Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
        return
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    val fileReference = storageReference.child("users/$userId/profile_pic")

    // Upload the file to Firebase Storage
    val uploadTask = fileReference.putFile(Uri.fromFile(tempFile))
    uploadTask.addOnSuccessListener {
        fileReference.downloadUrl.addOnSuccessListener { downloadUri ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).update("profileImageUrl", downloadUri.toString())
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to update profile picture: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun fetchUserDataFromFirebase(
    context: Context,
    onUserDataFetched: (name: String, age: String, dob: String, profileImageUri: Uri?) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()
    val userId = user.uid

    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val name = document.getString("name") ?: ""
                val age = document.getString("age") ?: ""
                val dob = document.getString("dob") ?: ""
                val profileImageUrl = document.getString("profileImageUrl")

                val profileImageUri = if (!profileImageUrl.isNullOrEmpty()) {
                    Uri.parse(profileImageUrl)
                } else {
                    null
                }

                // Pass the fetched data to the callback
                onUserDataFetched(name, age, dob, profileImageUri)
            } else {
                Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
        }
}
fun storeImageLocally(uri: Uri, context: Context) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "profile_pic.jpg") // Store the image as profile_pic.jpg in internal storage
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()

        Log.d("ImageStorage", "Image stored locally at ${file.absolutePath}")
    } catch (e: Exception) {
        Log.e("ImageStorage", "Failed to store image locally: ${e.message}")
    }
}
fun fetchLocalImage(context: Context): Uri? {
    val file = File(context.filesDir, "profile_pic.jpg") // Path to the locally stored image file
    return if (file.exists()) {
        Uri.fromFile(file)  // Convert the file to Uri
    } else {
        null  // Return null if the file doesn't exist
    }
}
