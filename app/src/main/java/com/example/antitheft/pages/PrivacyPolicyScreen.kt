package com.example.antitheft.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Set the background color for the entire screen
            .padding(16.dp), // Add padding for spacing
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top // Align text towards top
    ) {
        // Title with color
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary, // Set title text color from the theme
            modifier = Modifier.padding(bottom = 16.dp) // Add padding below the title
        )

        // Privacy Policy Content with text color and padding
        Text(
            text = """
            Privacy Policy

            This is the privacy policy of the Anti-Theft App.
            We value your privacy and are committed to protecting your personal information.
            
            Information We Collect:
            - Personal Data: We may collect your name, email address, and other contact details for communication purposes.
            - Device Information: We may collect data about your device, including model, operating system version, and device identifiers.
            - Usage Data: We may track your interactions with the app, including actions taken, time spent in the app, and features used.
            
            How We Use Your Information:
            - To improve the app's functionality and user experience.
            - To provide customer support and respond to your inquiries.
            - To send notifications related to app updates and security alerts.
            
            How We Protect Your Information:
            - We use encryption to protect sensitive data.
            - We store data securely and limit access to authorized personnel only.
            - We do not share your personal data with third parties without your consent, unless required by law.
            
            Third-Party Services:
            The Anti-Theft App may use third-party services that may collect information used to identify you. These third-party services have their own privacy policies, and we encourage you to review them. We are not responsible for the content or practices of these third-party services.
            
            Data Retention:
            We retain your data only for as long as necessary to fulfill the purposes outlined in this privacy policy. If you delete your account or uninstall the app, we will remove your personal information from our systems in accordance with applicable laws.

            Your Rights:
            You have the right to access, update, or delete your personal data at any time. If you would like to exercise any of these rights, please contact us at [your email address].
            
            Changes to This Privacy Policy:
            We may update this privacy policy from time to time. We will notify you of any significant changes by posting the updated policy in the app. We encourage you to review this privacy policy periodically.
            
            Contact Us:
            If you have any questions or concerns about this privacy policy or how we handle your data, please contact us at [your email address].
            
            Last Updated: [date]
        """.trimIndent(),
            color = MaterialTheme.colorScheme.onBackground, // Set text color for content
            style = MaterialTheme.typography.bodyLarge, // Set the style for the content text
            modifier = Modifier.padding(bottom = 16.dp) // Add some padding for better readability
        )

        // Back Button with text color
        Button(
            onClick = { navController.popBackStack() },
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                text = "Go Back",
                color = MaterialTheme.colorScheme.onPrimary // Set the color of the back button text
            )
        }
    }
}
