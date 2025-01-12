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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Email
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

data class EmergencyEmail(val name: String, val email: String)

@Composable
fun EmergencyEmail(navController: NavHostController) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val focusState = remember { mutableStateOf(false) }

    // State to store the list of emergency emails
    var emails by remember { mutableStateOf(listOf<EmergencyEmail>()) }

    // Directory for saving emails
    val dcimDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "SentinelX/Emails"
    ).apply {
        if (!exists()) mkdirs()
    }

    // State for the input fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Load emails from file when the screen is displayed
    LaunchedEffect(true) {
        emails = loadEmailsFromFile(dcimDir)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title with Icon for Emergency Emails
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "Emergency Emails Icon",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Emergency Emails",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.h5
                )
            }

            // Input fields for email and name with placeholders
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = androidx.compose.material3.MaterialTheme.colorScheme.onTertiary) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {}),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.onTertiary, RoundedCornerShape(8.dp))
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState.value = it.isFocused },
                    singleLine = true,
                    textStyle = TextStyle(color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground),  // Text color
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Transparent,  // Remove the purple outline
                        cursorColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = androidx.compose.material3.MaterialTheme.colorScheme.onTertiary) },  // Placeholder for email
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {}),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.onTertiary, RoundedCornerShape(8.dp))  // Border around the field
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState.value = it.isFocused },
                    singleLine = true,
                    textStyle = TextStyle(color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Transparent,  // Remove the purple outline
                        cursorColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

            }

            // Add email button
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(80.dp)
                    .background(color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary, shape = CircleShape)
                    .clickable {
                        if (name.isNotEmpty() && email.isNotEmpty()) {
                            val newEmail = EmergencyEmail(name, email)

                            // Save the email to the file
                            saveEmailToFile(dcimDir, newEmail)

                            // Update email list
                            emails = emails + newEmail

                            Toast.makeText(context, "Email Added: $name", Toast.LENGTH_SHORT).show()

                            // Clear the input fields
                            name = ""
                            email = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Email",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Display emails in a list
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(emails) { email ->
                    EmailGridItem(email = email, onDelete = {
                        // Remove email from list and update the file
                        emails = emails.filterNot { it == email }
                        deleteEmailFromFile(dcimDir, email)
                    })
                }
            }
        }
    }
}

@Composable
fun EmailGridItem(email: EmergencyEmail, onDelete: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(140.dp)
            .background(androidx.compose.material3.MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(8.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = email.name,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.h6
            )
            if (isExpanded) {
                Text(
                    text = email.email,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Delete button
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Email",
                tint = Color.Red,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onDelete() }
                    .padding(top = 8.dp)
            )
        }
    }
}

fun saveEmailToFile(directory: File, email: EmergencyEmail) {
    val file = File(directory, "emails.txt")
    val writer = FileWriter(file, true)
    writer.append("${email.name},${email.email}\n")
    writer.close()
}

fun deleteEmailFromFile(directory: File, email: EmergencyEmail) {
    val file = File(directory, "emails.txt")
    val tempFile = File(directory, "temp_emails.txt")
    tempFile.createNewFile()

    file.bufferedReader().use { reader ->
        tempFile.bufferedWriter().use { writer ->
            reader.forEachLine { line ->
                if (!line.contains(email.name) || !line.contains(email.email)) {
                    writer.write(line)
                    writer.newLine()
                }
            }
        }
    }

    file.delete()
    tempFile.renameTo(file)
}

fun loadEmailsFromFile(directory: File): List<EmergencyEmail> {
    val file = File(directory, "emails.txt")
    val emails = mutableListOf<EmergencyEmail>()

    if (file.exists()) {
        file.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val email = parts[1].trim()
                    emails.add(EmergencyEmail(name, email))
                }
            }
        }
    }

    return emails
}
