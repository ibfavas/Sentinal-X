
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        // Applying Google services plugin
        classpath(libs.google.services)

        // This is for Kotlin support
        classpath(libs.kotlin.gradle.plugin)

        classpath(libs.gradle) // Ensure you are using a supported version
        classpath(libs.hilt.android.gradle.plugin)

    }
}

// Apply plugins for all modules below
plugins {
    // Apply the necessary plugins for your modules
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Applying the Google Services plugin for Firebase-related configurations
    id("com.google.gms.google-services") version "4.4.2" apply false
}