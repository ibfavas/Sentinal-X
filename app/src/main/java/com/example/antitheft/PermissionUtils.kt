import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    private const val REQUEST_CODE_PERMISSIONS = 1234

    // Function to request permissions
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestPermissionsIfNeeded(
        activity: Activity,
        onPermissionsGranted: () -> Unit,
        onPermissionsDenied: () -> Unit
    ) {
        // List of permissions you want to check
        val requiredPermissions = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.USE_BIOMETRIC
        )

        // Add media-related permissions conditionally based on API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            requiredPermissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            requiredPermissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val deniedPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isEmpty()) {
            // All permissions are granted
            onPermissionsGranted()
        } else {
            // Request missing permissions
            ActivityCompat.requestPermissions(
                activity,
                deniedPermissions.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    // Handle the result of the permission request
    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray,
        onPermissionsGranted: () -> Unit,
        onPermissionsDenied: () -> Unit
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onPermissionsGranted() // All permissions granted
            } else {
                onPermissionsDenied() // Some permissions denied
// Optionally, check if any permission was permanently denied and inform the user.
            }
        }
    }
}