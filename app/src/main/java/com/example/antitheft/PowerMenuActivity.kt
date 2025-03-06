package com.example.antitheft

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antitheft.pages.FakeShutdownScreen

class PowerMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PowerMenuScreen(this)
        }
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
                devicePolicyManager.lockNow()  // Puts the device to sleep
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
                        showShutdownScreen = true;
                    }
                }
            }
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

@Preview(showBackground = true)
@Composable
fun PreviewPowerMenu() {
    PowerMenuScreen(activity = Activity())
}