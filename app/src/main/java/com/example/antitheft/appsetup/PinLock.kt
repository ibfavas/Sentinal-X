package com.example.antitheft.appsetup

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val dcimDir = File(context.getExternalFilesDir(null), "SentinelX/Pin").apply {
        parentFile?.mkdirs() // Ensure parent directories exist
    }
    val pinFile = File(dcimDir, "pin.txt")

    var stage by remember {
        mutableStateOf(
            if (pinFile.exists()) "change_pin" else "register"
        )
    }
    var enteredPin by remember { mutableStateOf("") }
    var confirmedPin by remember { mutableStateOf("") }
    var oldPin by remember { mutableStateOf("") }
    val maxPinLength = 4

    fun savePin(pin: String) {
        try {
            pinFile.writeText(pin)
            Toast.makeText(context, "PIN saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save PIN.", Toast.LENGTH_SHORT).show()
        }
    }

    fun validateOldPin(): Boolean {
        return try {
            pinFile.readText().trim() == oldPin
        } catch (e: Exception) {
            false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                color = MaterialTheme.colorScheme.onBackground,
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
                                if (isFilled) MaterialTheme.colorScheme.onBackground else Color.Gray,
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