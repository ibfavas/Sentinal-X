package com.example.antitheft.appsetup

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.io.File

@Composable
fun PinLock(navController: NavHostController) {
    val context = LocalContext.current
    val dcimDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "SentinelX/Pin"
    )
    var stage by remember { mutableStateOf(if (dcimDir.exists()) "change_pin" else "register") }
    var enteredPin by remember { mutableStateOf("") }
    var confirmedPin by remember { mutableStateOf("") }
    var oldPin by remember { mutableStateOf("") }
    val maxPinLength = 4

    fun savePin(pin: String) {
        dcimDir.parentFile?.mkdirs() // Create parent directories if they don't exist
        dcimDir.writeText(pin)
    }

    fun validateOldPin(): Boolean {
        return dcimDir.readText() == oldPin
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
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
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = when (stage) {
                    "register" -> "Enter a new PIN"
                    "confirm_register" -> "Confirm your PIN"
                    "change_pin" -> "Enter your old PIN"
                    "new_pin" -> "Enter a new PIN"
                    "confirm_new_pin" -> "Confirm your new PIN"
                    else -> ""
                },
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Your PIN contains at least 4 digits.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Display the entered PIN as dots
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                repeat(maxPinLength) { index ->
                    val isFilled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (isFilled) Color.White else Color.Gray,
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    )
                }
            }

            // Numeric Keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("X", "0", "OK")
            )
            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { key ->
                        KeyButton(
                            key = key,
                            onClick = {
                                when (key) {
                                    "X" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                    "OK" -> {
                                        when (stage) {
                                            "register" -> {
                                                if (enteredPin.length == maxPinLength) {
                                                    confirmedPin = enteredPin
                                                    enteredPin = ""
                                                    stage = "confirm_register"
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Please enter a 4-digit PIN.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            "confirm_register" -> {
                                                if (enteredPin == confirmedPin) {
                                                    savePin(enteredPin)
                                                    Toast.makeText(
                                                        context,
                                                        "PIN successfully registered!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.popBackStack() // Navigate back
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "PINs do not match. Try again.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    enteredPin = ""
                                                    stage = "register"
                                                }
                                            }
                                            "change_pin" -> {
                                                if (enteredPin.length == maxPinLength) {
                                                    oldPin = enteredPin
                                                    enteredPin = ""
                                                    if (validateOldPin()) {
                                                        stage = "new_pin"
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Incorrect old PIN.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        stage = "change_pin"
                                                    }
                                                }
                                            }
                                            "new_pin" -> {
                                                if (enteredPin.length == maxPinLength) {
                                                    confirmedPin = enteredPin
                                                    enteredPin = ""
                                                    stage = "confirm_new_pin"
                                                }
                                            }
                                            "confirm_new_pin" -> {
                                                if (enteredPin == confirmedPin) {
                                                    savePin(enteredPin)
                                                    Toast.makeText(
                                                        context,
                                                        "PIN successfully changed!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.popBackStack() // Navigate back
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "PINs do not match. Try again.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    enteredPin = ""
                                                    stage = "new_pin"
                                                }
                                            }
                                        }
                                    }
                                    else -> if (enteredPin.length < maxPinLength) enteredPin += key
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeyButton(key: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .padding(8.dp)
    ) {
        TextButton(
            onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.White,
                containerColor = Color(0xFF1F1F1F)
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = key,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
