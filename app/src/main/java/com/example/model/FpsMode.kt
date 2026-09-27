package com.example.model

/**
 * FPS & Refresh Rate modes. Only modes physically supported by the device panel will be enabled.
 */
enum class FpsMode(val targetFps: Int, val refreshRate: Float, val label: String) {
    AUTO(0, 0f, "Auto (Adaptive)"),
    FPS_30(30, 60f, "30 FPS (Power Saver)"),
    FPS_60(60, 60f, "60 FPS (Standard)"),
    FPS_90(90, 90f, "90 FPS (High Refresh)"),
    FPS_120(120, 120f, "120 FPS (Ultra Smooth)");

    companion object {
        fun fromFps(fps: Int): FpsMode {
            return entries.find { it.targetFps == fps } ?: AUTO
        }

        fun fromRefreshRate(rate: Float): FpsMode {
            return entries.find { kotlin.math.abs(it.refreshRate - rate) < 1f } ?: AUTO
        }
    }
}
