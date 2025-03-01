package com.example.antitheft.appsetup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.io.File
import java.io.FileWriter

data class Contact(val name: String, val phoneNumber: String)

@Composable
fun EmergencyContacts(navController: NavHostController) {
    val context = LocalContext.current

    // Directory for saving contacts (app-specific storage)
    val contactsDir = File(context.getExternalFilesDir(null), "SentinelX/Contacts").apply {
        if (!exists()) mkdirs()
    }

    // State to store the list of contacts
    var contacts by remember { mutableStateOf(listOf<Contact>()) }

    // Load contacts from the file when the screen is displayed
    LaunchedEffect(true) {
        contacts = loadContactsFromFile(contactsDir)
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
            // Title with Icon for Emergency Contacts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = "Emergency Contacts Icon",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Emergency Contacts",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.h5
                )
            }

            // Display contacts in a grid layout
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 columns
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Render each contact as a grid item
                items(contacts) { contact ->
                    ContactGridItem(contact = contact, onDelete = {
                        // Remove contact and update the list
                        contacts = contacts.filterNot { it == contact }
                        deleteContactFromFile(contactsDir, contact)
                    })
                }
            }
        }

        // Floating action button (plus button) at the bottom right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(80.dp)
                .offset(x=((-20).dp),y=((-20).dp))
                .background(color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary, shape = CircleShape)
                .clickable {
                    pickContact(context) { name, phone ->
                        val formattedPhone =
                            if (!phone.startsWith("+91")) "+91$phone" else phone
                        val newContact = Contact(name, formattedPhone)

                        // Save contact to local storage
                        saveContactToFile(contactsDir, newContact)

                        // Update contact list
                        contacts = contacts + newContact

                        Toast.makeText(context, "Contact Added: $name", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Contact",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun ContactGridItem(contact: Contact, onDelete: () -> Unit) {
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
                text = contact.name,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.h6
            )
            if (isExpanded) {
                Text(
                    text = contact.phoneNumber,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Delete button
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Contact",
                tint = Color.Red,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onDelete() }
                    .padding(top = 8.dp)
            )
        }
    }
}

fun pickContact(context: Context, onContactPicked: (String, String) -> Unit) {
    val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
    val resultLauncher = (context as? androidx.activity.ComponentActivity)?.activityResultRegistry?.register(
        "pickContact",
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) { // Use android.app.Activity.RESULT_OK
            val uri: Uri? = result.data?.data
            uri?.let {
                val cursor = context.contentResolver.query(it, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val name =
                            it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                        val phone =
                            it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        onContactPicked(name, phone)
                    }
                }
            }
        }
    }

    resultLauncher?.launch(intent)
}

fun saveContactToFile(directory: File, contact: Contact) {
    val file = File(directory, "contacts.txt")
    val writer = FileWriter(file, true)
    writer.append("${contact.name},${contact.phoneNumber}\n")
    writer.close()
}

fun deleteContactFromFile(directory: File, contact: Contact) {
    val file = File(directory, "contacts.txt")
    val tempFile = File(directory, "temp_contacts.txt")
    tempFile.createNewFile()

    file.bufferedReader().use { reader ->
        tempFile.bufferedWriter().use { writer ->
            reader.forEachLine { line ->
                if (!line.contains(contact.name) || !line.contains(contact.phoneNumber)) {
                    writer.write(line)
                    writer.newLine()
                }
            }
        }
    }

    file.delete()
    tempFile.renameTo(file)
}

fun loadContactsFromFile(directory: File): List<Contact> {
    val file = File(directory, "contacts.txt")
    val contacts = mutableListOf<Contact>()

    if (file.exists()) {
        file.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val phoneNumber = parts[1].trim()
                    contacts.add(Contact(name, phoneNumber))
                }
            }
        }
    }

    return contacts
}