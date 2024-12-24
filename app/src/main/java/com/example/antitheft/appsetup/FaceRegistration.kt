package com.example.antitheft.appsetup

import android.content.Context
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.io.FileOutputStream
import android.os.Environment
import androidx.compose.material.CircularProgressIndicator
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.face.FaceLandmark
import java.io.File
import kotlin.math.pow

@OptIn(ExperimentalGetImage::class)
@Composable
fun FaceRegistration(navController: NavHostController) {
    val context = LocalContext.current
    var isFaceCaptured by remember { mutableStateOf(false) }
    var isFaceDataSaved by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var faceInsideOvalStartTime by remember { mutableStateOf<Long?>(null) }
    var isFaceInsideOval by remember { mutableStateOf(false) }

    // Oval parameters (larger oval)
    val ovalCenterX = 500f // Adjust based on screen size
    val ovalCenterY = 800f // Adjust based on screen size
    val ovalRadiusX = 350f // Horizontal radius (larger)
    val ovalRadiusY = 500f // Vertical radius (larger)

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val faceDetectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    val faceDetector = FaceDetection.getClient(faceDetectorOptions)
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    DisposableEffect(context) {
        val cameraProvider = cameraProviderFuture.get()
        onDispose {
            cameraProvider.unbindAll()
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build()
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    preview.surfaceProvider = previewView.surfaceProvider

                    val imageAnalysis = ImageAnalysis.Builder().build()
                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                            faceDetector.process(inputImage)
                                .addOnSuccessListener { faces ->
                                    if (faces.isNotEmpty()) {
                                        val detectedFace = faces.first()
                                        val faceCenterX = (detectedFace.boundingBox.left + detectedFace.boundingBox.right) / 2f
                                        val faceCenterY = (detectedFace.boundingBox.top + detectedFace.boundingBox.bottom) / 2f

                                        val mappedOffset = mapCameraCoordinatesToScreenCoordinates(
                                            previewView,
                                            faceCenterX,
                                            faceCenterY,
                                            mediaImage.width,
                                            mediaImage.height
                                        )


                                        val isWithinOval =
                                            ((mappedOffset.x - ovalCenterX).pow(2) / ovalRadiusX.pow(2)) +
                                                    ((mappedOffset.y - ovalCenterY).pow(2) / ovalRadiusY.pow(2)) <= 1

                                        if (isWithinOval) {
                                            isFaceInsideOval = true
                                            if (faceInsideOvalStartTime == null) {
                                                faceInsideOvalStartTime = System.currentTimeMillis()
                                            } else {
                                                val elapsedTime = System.currentTimeMillis() - faceInsideOvalStartTime!!
                                                if (elapsedTime >= 3000L && !isFaceDataSaved) {
                                                    isProcessing = true
                                                    saveFaceData(detectedFace, context, navController) // Pass navController
                                                    isFaceDataSaved = true
                                                    isFaceCaptured = true
                                                }
                                            }
                                        } else {
                                            isFaceInsideOval = false
                                            faceInsideOvalStartTime = null
                                        }
                                    } else {
                                        isFaceInsideOval = false
                                        faceInsideOvalStartTime = null
                                        isFaceCaptured = false
                                    }
                                    imageProxy.close()
                                }
                                .addOnFailureListener {
                                    imageProxy.close()
                                }
                        }
                    }


                    cameraProvider.bindToLifecycle(
                        ctx as LifecycleOwner, cameraSelector, preview, imageAnalysis
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }
        )

        // Oval boundary overlay (changes color when face is inside)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOval(
                color = if (isFaceInsideOval) Color.Green else Color.Red,
                topLeft = Offset(ovalCenterX - ovalRadiusX, ovalCenterY - ovalRadiusY),
                size = Size(ovalRadiusX * 2, ovalRadiusY * 2),
                style = Stroke(width = 4f)
            )
        }

        // Circular progress indicator
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

fun saveFaceData(face: Face, context: Context, navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        val faceEmbedding = extractFaceEmbedding(face)
        if (faceEmbedding.isNotEmpty()) {
            try {
                val dcimDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "SentinelX/Faces"
                )

                if (!dcimDir.exists()) {
                    dcimDir.mkdirs()
                }

                val fileName = "face_data_${userId}_${System.currentTimeMillis()}.txt"
                val file = File(dcimDir, fileName)

                FileOutputStream(file).use { fos ->
                    fos.write(faceEmbedding.toByteArray())
                }
                Toast.makeText(context, "Face data saved to Download/SentinalX/Faces", Toast.LENGTH_SHORT).show()

                // Automatically go back after saving the data
                navController.popBackStack()

            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save face data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Not all facial features detected", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "No valid face detected", Toast.LENGTH_SHORT).show()
    }
}


fun extractFaceEmbedding(face: Face): String {
    val requiredLandmarks = listOf(
        FaceLandmark.LEFT_EYE, FaceLandmark.RIGHT_EYE, FaceLandmark.NOSE_BASE,
        FaceLandmark.MOUTH_BOTTOM, FaceLandmark.MOUTH_LEFT, FaceLandmark.MOUTH_RIGHT, FaceLandmark.LEFT_CHEEK,
        FaceLandmark.RIGHT_CHEEK
    )

    val positions = mutableListOf<String>()

    for (landmark in requiredLandmarks) {
        val position = face.getLandmark(landmark)?.position
        if (position != null) {
            positions.add(position.toString())
        }
    }

    return if (positions.size == requiredLandmarks.size && face.boundingBox.width() > 50 && face.boundingBox.height() > 50) {
        positions.joinToString(",")
    } else {
        ""
    }
}

fun mapCameraCoordinatesToScreenCoordinates(
    previewView: PreviewView,
    x: Float,
    y: Float,
    imageWidth: Int,
    imageHeight: Int
): Offset {
    val scaleX = previewView.width.toFloat() / imageHeight
    val scaleY = previewView.height.toFloat() / imageWidth

    val mappedX = x * scaleX
    val mappedY = y * scaleY

    return Offset(mappedX, mappedY)
}

