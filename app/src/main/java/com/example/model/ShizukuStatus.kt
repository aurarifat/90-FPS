package com.example.model

data class ShizukuStatus(
    val isInstalled: Boolean = false,
    val isRunning: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val version: Int = 0,
    val isPreV11: Boolean = false,
    val canExecuteAdbCommands: Boolean = false,
    val statusMessage: String = "Checking Shizuku status..."
)
