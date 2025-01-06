package com.example.antitheft

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.antitheft.pages.fetchLocalImage
import com.example.antitheft.pages.fetchUserDataFromFirebase

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val imageView: ImageView = findViewById(R.id.profileImageView)

        // First, try to load the image from local storage
        val localImageUri = fetchLocalImage(this)
        localImageUri?.let {
            // Display the local image if available
            imageView.setImageURI(it)
        } ?: run {
            // If no local image, fetch from Firebase
            fetchUserDataFromFirebase(this) { name, age, dob, profileImageUri ->
                profileImageUri?.let {
                    // Set the Firebase profile image URI
                    imageView.setImageURI(it)
                } ?: run {
                    // If no image from Firebase, set a placeholder
                    imageView.setImageResource(R.drawable.userpic)
                }
            }
        }
    }
}
