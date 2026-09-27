package com.example.model

/**
 * Encapsulates detected device hardware specifications, display capabilities, and OS details.
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val deviceCode: String,
    val androidVersion: String,
    val apiLevel: Int,
    val socModel: String,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val totalStorageBytes: Long,
    val availableStorageBytes: Long,
    val currentRefreshRate: Float,
    val supportedRefreshRates: List<Float>,
    val displayResolution: String,
    val is90HzPhysicallySupported: Boolean,
    val isHelioG88OrTargetClass: Boolean
) {
    val totalRamGb: Float get() = totalRamBytes / (1024f * 1024f * 1024f)
    val availableRamGb: Float get() = availableRamBytes / (1024f * 1024f * 1024f)
    val totalStorageGb: Float get() = totalStorageBytes / (1024f * 1024f * 1024f)
    val availableStorageGb: Float get() = availableStorageBytes / (1024f * 1024f * 1024f)
    val maxSupportedRefreshRate: Float get() = supportedRefreshRates.maxOrNull() ?: 60f
}
