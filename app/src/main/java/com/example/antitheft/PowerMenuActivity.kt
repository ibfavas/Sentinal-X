package com.example.antitheft

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Semi-transparent background
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFF202020), shape = RoundedCornerShape(16.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                PowerButton("Emergency", Color.Red) { showToast(activity, "Emergency Clicked") }
                Spacer(modifier = Modifier.width(32.dp))
                PowerButton("Lock now", Color.DarkGray) { showToast(activity, "Lock Clicked") }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                PowerButton("Power off", Color.DarkGray) { showToast(activity, "Power Off Clicked") }
                Spacer(modifier = Modifier.width(32.dp))
                PowerButton("Restart", Color.DarkGray) { showToast(activity, "Restart Clicked") }
            }
        }
    }
}

@Composable
fun PowerButton(text: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(color, shape = RoundedCornerShape(50.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 16.sp)
    }
}

fun showToast(activity: Activity, message: String) {
    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
}

@Preview(showBackground = true)
@Composable
fun PreviewPowerMenu() {
    PowerMenuScreen(activity = Activity())
}
