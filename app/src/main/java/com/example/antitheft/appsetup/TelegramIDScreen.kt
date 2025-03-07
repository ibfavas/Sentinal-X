package com.example.antitheft.appsetup

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.io.File
import java.io.FileWriter

@Composable
fun TelegramIDScreen(navController: NavHostController) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val focusState = remember { mutableStateOf(false) }

    // State to store the list of Telegram IDs
    var telegramIds by remember { mutableStateOf(listOf<String>()) }

    // Directory for saving Telegram IDs
    val storageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "SentinelX/Telegram"
    ).apply {
        if (!exists()) mkdirs()
    }

    // State for input field
    var telegramId by remember { mutableStateOf("") }

    // Load Telegram IDs from file when the screen is displayed
    LaunchedEffect(true) {
        telegramIds = loadTelegramIdsFromFile(storageDir)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Telegram IDs",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(16.dp)
            )

            // Input field for Telegram ID
            OutlinedTextField(
                value = telegramId,
                onValueChange = { telegramId = it },
                label = { Text("Enter Telegram ID", color = androidx.compose.material3.MaterialTheme.colorScheme.onTertiary) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {}),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent, shape = RoundedCornerShape(8.dp))
                    .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.onTertiary, RoundedCornerShape(8.dp))
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState.value = it.isFocused },
                singleLine = true,
                textStyle = TextStyle(color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Transparent,
                    cursorColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add Telegram ID button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.tertiary, shape = CircleShape)
                    .clickable {
                        if (telegramId.isNotEmpty()) {
                            // Save the Telegram ID to the file
                            saveTelegramIdToFile(storageDir, telegramId)

                            // Update the list
                            telegramIds = telegramIds + telegramId

                            Toast.makeText(context, "Telegram ID Added", Toast.LENGTH_SHORT).show()

                            // Clear input field
                            telegramId = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Telegram ID",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display saved Telegram IDs
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(telegramIds) { id ->
                    TelegramIDGridItem(telegramId = id, onDelete = {
                        // Remove ID from the list and update the file
                        telegramIds = telegramIds.filterNot { it == id }
                        deleteTelegramIdFromFile(storageDir, id)
                    })
                }
            }
        }
    }
}

@Composable
fun TelegramIDGridItem(telegramId: String, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .background(androidx.compose.material3.MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(8.dp))
            .clickable {}
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = telegramId,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Delete button
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Telegram ID",
                tint = Color.Red,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onDelete() }
                    .padding(top = 8.dp)
            )
        }
    }
}

// Save a Telegram ID to a file
fun saveTelegramIdToFile(directory: File, telegramId: String) {
    val file = File(directory, "telegram_ids.txt")
    val writer = FileWriter(file, true)
    writer.append("$telegramId\n")
    writer.close()
}

// Delete a Telegram ID from the file
fun deleteTelegramIdFromFile(directory: File, telegramId: String) {
    val file = File(directory, "telegram_ids.txt")
    val tempFile = File(directory, "temp_telegram_ids.txt")
    tempFile.createNewFile()

    file.bufferedReader().use { reader ->
        tempFile.bufferedWriter().use { writer ->
            reader.forEachLine { line ->
                if (line.trim() != telegramId.trim()) {
                    writer.write(line)
                    writer.newLine()
                }
            }
        }
    }

    file.delete()
    tempFile.renameTo(file)
}

// Load Telegram IDs from a file
fun loadTelegramIdsFromFile(directory: File): List<String> {
    val file = File(directory, "telegram_ids.txt")
    val telegramIds = mutableListOf<String>()

    if (file.exists()) {
        file.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.isNotEmpty()) {
                    telegramIds.add(line.trim())
                }
            }
        }
    }

    return telegramIds
}
