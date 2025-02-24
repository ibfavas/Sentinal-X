package com.example.antitheft.appsetup

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.antitheft.R
import com.example.antitheft.ThemeViewModel
import java.io.File

@Composable
fun PasswordLock(navController: NavHostController, viewModel: ThemeViewModel) {
    val context = LocalContext.current
    val passwordDir = File(context.getExternalFilesDir(null), "SentinelX/Password").apply {
        parentFile?.mkdirs() // Ensure parent directories exist
    }
    val passwordFile = File(passwordDir, "password.txt")

    var stage by remember {
        mutableStateOf(
            if (passwordFile.exists()) "change_password" else "register"
        )
    }
    var enteredPassword by remember { mutableStateOf("") }
    var confirmedPassword by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val backgroundResource = if (isDarkTheme) R.drawable.profile_back else R.drawable.whiteback

    fun savePassword(password: String) {
        try {
            passwordFile.writeText(password)
            Toast.makeText(context, "Password saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save password.", Toast.LENGTH_SHORT).show()
        }
    }

    fun validateOldPassword(): Boolean {
        return try {
            passwordFile.readText().trim() == oldPassword
        } catch (e: Exception) {
            false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painterResource(backgroundResource),
                contentScale = ContentScale.FillBounds
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Lock Icon and Instruction Text
            Text(
                text = "\uD83D\uDD12", // Lock Emoji
                fontSize = 40.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Password instruction text
            Text(
                text = when (stage) {
                    "register" -> "Enter a new password"
                    "confirm_register" -> "Confirm your password"
                    "change_password" -> "Enter your old password"
                    "new_password" -> "Enter a new password"
                    "confirm_new_password" -> "Confirm your new password"
                    else -> ""
                },
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Password input field with device keyboard
            TextField(
                value = enteredPassword,
                onValueChange = { enteredPassword = it },
                label = { Text("Password") },
                placeholder = {
                    Text(
                        text = when (stage) {
                            "register" -> "Enter a new password"
                            "confirm_register" -> "Confirm your password"
                            "change_password" -> "Enter your old password"
                            "new_password" -> "Enter a new password"
                            "confirm_new_password" -> "Confirm your new password"
                            else -> ""
                        }
                    )
                },
                visualTransformation = PasswordVisualTransformation(), // Hide password text
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        when (stage) {
                            "register" -> {
                                if (enteredPassword.isNotEmpty()) {
                                    confirmedPassword = enteredPassword
                                    enteredPassword = ""
                                    stage = "confirm_register"
                                } else {
                                    Toast.makeText(context, "Please enter a password.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            "confirm_register" -> {
                                if (enteredPassword == confirmedPassword) {
                                    savePassword(enteredPassword)
                                    navController.popBackStack() // Navigate back
                                } else {
                                    Toast.makeText(context, "Passwords do not match. Try again.", Toast.LENGTH_SHORT).show()
                                    enteredPassword = ""
                                    stage = "register"
                                }
                            }
                            "change_password" -> {
                                if (enteredPassword.isNotEmpty()) {
                                    oldPassword = enteredPassword
                                    enteredPassword = ""
                                    if (validateOldPassword()) {
                                        stage = "new_password"
                                    } else {
                                        Toast.makeText(context, "Incorrect old password.", Toast.LENGTH_SHORT).show()
                                        stage = "change_password"
                                    }
                                }
                            }
                            "new_password" -> {
                                if (enteredPassword.isNotEmpty()) {
                                    confirmedPassword = enteredPassword
                                    enteredPassword = ""
                                    stage = "confirm_new_password"
                                }
                            }
                            "confirm_new_password" -> {
                                if (enteredPassword == confirmedPassword) {
                                    savePassword(enteredPassword)
                                    navController.popBackStack() // Navigate back
                                } else {
                                    Toast.makeText(context, "Passwords do not match. Try again.", Toast.LENGTH_SHORT).show()
                                    enteredPassword = ""
                                    stage = "new_password"
                                }
                            }
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}