package com.example.model

import android.graphics.drawable.Drawable

/**
 * Model representing an application installed on the user device.
 */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val isGameCategory: Boolean = false
)
