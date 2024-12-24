package com.example.antitheft.appsetup

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun <NavHostController> RegisteredFaces(navController: NavHostController) {

    val context = LocalContext.current
    val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SentinelX/Faces")

    // Retrieve the saved face data files
    val faceFiles = downloadsDir.listFiles()?.toList() ?: emptyList()

    // List of face names
    var faces by remember { mutableStateOf<List<String>>(emptyList()) }

    // Read face data from files
    LaunchedEffect(downloadsDir) {
        faces = faceFiles.map { it.name }
    }

    // Delete face function
    fun deleteFace(fileName: String) {
        val file = File(downloadsDir, fileName)
        if (file.exists()) {
            if (file.delete()) {
                faces = faces.filter { it != fileName }
                Toast.makeText(context, "Face deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to delete face", Toast.LENGTH_SHORT).show()
            }
        }
    }
   Box(
       modifier = Modifier
           .fillMaxSize()
           .background(color = Color.Black)
   )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Registered Faces",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (faces.isEmpty()) {
            Text("No registered faces found.", color = Color.White, fontSize = 16.sp,
                modifier = Modifier.offset(y=25.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(faces) { index, fileName ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(
                                color = Color.DarkGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Face text on the left
                            Text(
                                text = "Face ${index + 1}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Delete button on the right with white color
                            Button(
                                onClick = { deleteFace(fileName) },
                                colors = androidx.compose.material.ButtonDefaults.buttonColors(backgroundColor = Color.White)
                            ) {
                                Text("Delete", color = Color.Black) // Ensure the text color contrasts with the button
                            }

                        }
                    }
                }
            }

        }
    }
}
