package com.example.antitheft

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.antitheft.pages.FakeShutdownScreen
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class PowerMenuActivity : ComponentActivity() {
    private var isFaceVerified by mutableStateOf(false)
    private var capturedFaceImage: Bitmap? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (isFaceVerified) {
                // If face matches, close the activity
                LaunchedEffect(Unit) {
                    finish()
                }
            } else {
                // If face doesn't match, allow the user to proceed
                PowerMenuScreen(this)
                FaceCaptureScreen(
                    onFaceVerified = { isMatch ->
                        isFaceVerified = isMatch
                        if (!isMatch && capturedFaceImage != null) {
                            //sendWhatsAppAlert(this, capturedFaceImage!!) // Send WhatsApp alert with the captured face
                            sendTelegramAlert(this, capturedFaceImage!!)
                        }
                    },
                    onFaceCaptured = { bitmap ->
                        capturedFaceImage = bitmap // Save the captured face image
                    }
                )
            }
        }
    }
}

@Composable
fun FaceCaptureScreen(
    onFaceVerified: (Boolean) -> Unit,
    onFaceCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isProcessing = true
        captureFace(context, lifecycleOwner) { detectedFace, imageProxy ->
            if (detectedFace != null) {
                val isMatch = compareFaceWithRegisteredFaces(detectedFace, context)
                onFaceVerified(isMatch)
                if (!isMatch) {
                    val faceImage = captureFaceImage(detectedFace, context, imageProxy)
                    if (faceImage != null) {
                        onFaceCaptured(faceImage)
                        //sendWhatsAppAlert(context, faceImage) // ✅ Send it in the background
                        sendTelegramAlert(context, faceImage)
                    }
                }
            } else {
                onFaceVerified(false)
            }
            isProcessing = false
        }
    }
}


@OptIn(ExperimentalGetImage::class)
private fun captureFace(context: Context, lifecycleOwner: LifecycleOwner, onFaceDetected: (Face?, ImageProxy) -> Unit) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val cameraExecutor = Executors.newSingleThreadExecutor()
    val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
    )

    var isFaceCaptured = false // Prevent multiple captures

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            if (isFaceCaptured) {
                imageProxy.close()
                return@setAnalyzer
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                faceDetector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            val detectedFace = faces[0]
                            onFaceDetected(detectedFace, imageProxy)
                            isFaceCaptured = true // Mark as captured
                        } else {
                            onFaceDetected(null, imageProxy)
                        }
                    }
                    .addOnFailureListener {
                        onFaceDetected(null, imageProxy)
                    }
            } else {
                imageProxy.close()
            }
        }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
    }, ContextCompat.getMainExecutor(context))
}


private fun captureFaceImage(face: Face, context: Context, imageProxy: ImageProxy): Bitmap? {
    val bitmap = imageProxy.toBitmap() // Convert ImageProxy to Bitmap correctly
    if (bitmap == null) {
        Log.e("FaceCapture", "Failed to convert ImageProxy to Bitmap")
        imageProxy.close()
        return null
    }

    // Rotate image properly based on rotation degrees from imageProxy
    val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)

    // Crop the rotated image to the detected face
    val croppedBitmap = cropFaceFromImage(face, rotatedBitmap)
    imageProxy.close() // Close ImageProxy after processing
    return croppedBitmap
}

private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(rotationDegrees.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}


private fun cropFaceFromImage(face: Face, bitmap: Bitmap): Bitmap? {
    val faceBoundingBox = face.boundingBox

    // Adjust the bounding box to ensure it's within the bitmap's dimensions
    val left = faceBoundingBox.left.coerceIn(0, bitmap.width)
    val top = faceBoundingBox.top.coerceIn(0, bitmap.height)
    val right = (faceBoundingBox.right).coerceAtMost(bitmap.width)
    val bottom = (faceBoundingBox.bottom).coerceAtMost(bitmap.height)

    // Ensure the width and height are positive
    val width = right - left
    val height = bottom - top
    if (width <= 0 || height <= 0) {
        Log.e("FaceCapture", "Invalid face bounding box dimensions: width=$width, height=$height")
        return null
    }

    // Crop the face from the bitmap
    val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, width, height)
    if (croppedBitmap == null) {
        Log.e("FaceCapture", "Bitmap.createBitmap returned null")
        return null
    }

    // Rotate the cropped bitmap based on the face's rotation
    val rotationDegrees = face.headEulerAngleZ
    if (rotationDegrees != 0f) {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees)
        return Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.width, croppedBitmap.height, matrix, true)
    }

    return croppedBitmap
}

private fun compareFaceWithRegisteredFaces(capturedFace: Face, context: Context): Boolean {
    val registeredFacesDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "SentinelX/Faces"
    )

    if (!registeredFacesDir.exists()) return false

    val registeredFaceFiles = registeredFacesDir.listFiles()
    if (registeredFaceFiles.isNullOrEmpty()) return false

    val capturedFaceEmbedding = extractFaceEmbedding(capturedFace)
    for (file in registeredFaceFiles) {
        val registeredFaceEmbedding = file.readText()
        if (isFaceMatch(capturedFaceEmbedding, registeredFaceEmbedding)) {
            return true
        }
    }
    return false
}

private fun extractFaceEmbedding(face: Face): String {
    // List of required facial landmarks
    val requiredLandmarks = listOf(
        FaceLandmark.LEFT_EYE,
        FaceLandmark.RIGHT_EYE,
        FaceLandmark.NOSE_BASE,
        FaceLandmark.MOUTH_LEFT,
        FaceLandmark.MOUTH_RIGHT,
        FaceLandmark.MOUTH_BOTTOM,
        FaceLandmark.LEFT_CHEEK,
        FaceLandmark.RIGHT_CHEEK
    )

    // Collect the positions of the required landmarks
    val positions = mutableListOf<String>()
    for (landmark in requiredLandmarks) {
        val position = face.getLandmark(landmark)?.position
        if (position != null) {
            positions.add("${position.x},${position.y}")
        } else {
            // If any landmark is missing, return an empty string
            return ""
        }
    }

    // Combine all positions into a single string
    return positions.joinToString(",")
}

private fun isFaceMatch(embedding1: String, embedding2: String): Boolean {
    // Parse the embeddings into lists of floats
    val vector1 = parseEmbedding(embedding1)
    val vector2 = parseEmbedding(embedding2)

    // If the embeddings are invalid, return false
    if (vector1.isEmpty() || vector2.isEmpty() || vector1.size != vector2.size) {
        return false
    }

    // Calculate cosine similarity
    val similarity = cosineSimilarity(vector1, vector2)

    // Define a threshold for face matching (e.g., 0.8)
    val threshold = 0.8

    // Return true if the similarity exceeds the threshold
    return similarity >= threshold
}

private fun parseEmbedding(embedding: String): List<Float> {
    return try {
        embedding.split(",").map { it.toFloat() }
    } catch (e: Exception) {
        emptyList() // Return an empty list if parsing fails
    }
}

private fun cosineSimilarity(vector1: List<Float>, vector2: List<Float>): Double {
    // Compute the dot product
    var dotProduct = 0.0
    for (i in vector1.indices) {
        dotProduct += vector1[i] * vector2[i]
    }

    // Compute the magnitudes (Euclidean norms) of the vectors
    val magnitude1 = Math.sqrt(vector1.map { it * it }.sum().toDouble())
    val magnitude2 = Math.sqrt(vector2.map { it * it }.sum().toDouble())

    // Avoid division by zero
    if (magnitude1 == 0.0 || magnitude2 == 0.0) {
        return 0.0
    }

    // Return the cosine similarity
    return dotProduct / (magnitude1 * magnitude2)
}

//private fun isWhatsAppInstalled(context: Context): Boolean {
//    return try {
//        context.packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES) != null ||
//                context.packageManager.getPackageInfo("com.whatsapp.w4b", PackageManager.GET_ACTIVITIES) != null
//    } catch (e: PackageManager.NameNotFoundException) {
//        false
//    }
//}


// private fun sendWhatsAppAlert(context: Context, faceImage: Bitmap) {
//    val faceImageFile = saveImageToStorage(context, faceImage)
//    if (faceImageFile == null) {
//        Log.e("FaceCapture", "Failed to save face image.")
//        return
//    }
//
//    val contactsDir = File(context.getExternalFilesDir(null), "SentinelX/Contacts")
//    val contacts = loadContactsFromFile(contactsDir)
//
//    if (contacts.isEmpty()) {
//        Log.e("WhatsAppAlert", "No emergency contacts found.")
//        return
//    }
//
//    // Run the message sending in a background coroutine
//    CoroutineScope(Dispatchers.IO).launch {
//        for (contact in contacts) {
//            val phoneNumber = contact.phoneNumber
//            val message = "Unauthorized access detected! See attached image."
//
//            val intent = Intent(Intent.ACTION_SEND).apply {
//                type = "image/*"
//                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", faceImageFile))
//                putExtra(Intent.EXTRA_TEXT, message)
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            }
//
//            // Check which version of WhatsApp is installed
//            when {
//                isAppInstalled(context, "com.whatsapp") -> intent.setPackage("com.whatsapp")
//                isAppInstalled(context, "com.whatsapp.w4b") -> intent.setPackage("com.whatsapp.w4b")
//                else -> {
//                    Log.e("WhatsAppAlert", "WhatsApp is not installed.")
//                    return@launch
//                }
//            }
//
//            // Send the message in the background
//            try {
//                context.startActivity(intent)
//                Log.d("WhatsAppAlert", "Message sent to $phoneNumber")
//            } catch (e: ActivityNotFoundException) {
//                Log.e("WhatsAppAlert", "Failed to send WhatsApp message: ${e.message}")
//            }
//        }
//    }
//}
//
//private fun isAppInstalled(context: Context, packageName: String): Boolean {
//    return try {
//        context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES) != null
//    } catch (e: PackageManager.NameNotFoundException) {
//        false
//    }
//}

private fun saveImageToStorage(context: Context, bitmap: Bitmap): File? {
    val storageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "SentinelX/IntruderImages"
    )
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFile = File(storageDir, "face_$timeStamp.jpg")

    return try {
        FileOutputStream(imageFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        }
        Log.d("FaceCapture", "Face image saved: ${imageFile.absolutePath}")
        imageFile
    } catch (e: Exception) {
        Log.e("FaceCapture", "Error saving image: ${e.message}")
        null
    }
}

@Composable
fun PowerMenuScreen(activity: Activity) {
    val devicePolicyManager = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val componentName = ComponentName(activity, MyDeviceAdminReceiver::class.java)

    var showShutdownScreen by remember { mutableStateOf(false) }

    if (showShutdownScreen) {
        FakeShutdownScreen(activity) {
            if (devicePolicyManager.isAdminActive(componentName)) {
                devicePolicyManager.lockNow()  // Lock the device
            } else {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app needs admin access to lock the screen.")
                }
                activity.startActivity(intent)
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(Color(0xFF202020), shape = RoundedCornerShape(16.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    PowerButton("Emergency", Color.Red, Icons.Default.CrisisAlert) {
                        val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:112") }
                        activity.startActivity(intent)
                    }
                    Spacer(modifier = Modifier.width(64.dp))
                    PowerButton("Lock now", Color.DarkGray, Icons.Default.Lock) {
                        if (devicePolicyManager.isAdminActive(componentName)) {
                            devicePolicyManager.lockNow()
                        } else {
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app needs admin access to lock the device.")
                            }
                            activity.startActivity(intent)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row {
                    PowerButton("Power off", Color.DarkGray, Icons.Default.PowerSettingsNew) {
                        showShutdownScreen = true  // Show fake shutdown animation
                    }
                    Spacer(modifier = Modifier.width(64.dp))
                    PowerButton("Restart", Color.DarkGray, Icons.Default.Replay) {
                        showShutdownScreen = true
                    }
                }
            }
        }
    }
}

fun sendTelegramAlert(context: Context, faceImage: Bitmap) {
    val token = "" // Your bot token
    val chatId = "" // Your chat ID
    val imageFile = saveImageToStorage(context, faceImage)

    if (imageFile == null) {
        Log.e("TelegramAlert", "Failed to save face image.")
        return
    }

    val url = "https://api.telegram.org/bot$token/sendPhoto"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("photo", imageFile.name, imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                .addFormDataPart("caption", "🚨 *Unauthorized Access Detected!* 🚨\nCheck the attached image.") // Message caption
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            Log.d("TelegramAlert", "Response: ${response.body?.string()}")
        } catch (e: Exception) {
            Log.e("TelegramAlert", "Failed to send Telegram alert: ${e.message}")
        }
    }
}



@Composable
fun PowerButton(text: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(color, shape = RoundedCornerShape(50.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, color = Color.White, fontSize = 16.sp)
    }
}
