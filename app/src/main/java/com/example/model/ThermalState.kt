package com.example.model

enum class ThermalSeverity(val label: String, val isWarning: Boolean) {
    NONE("Normal (Optimal)", false),
    LIGHT("Light Warming", false),
    MODERATE("Moderate Heat", false),
    SEVERE("Severe Throttling", true),
    CRITICAL("Critical Overheat", true),
    EMERGENCY("Emergency Cooldown", true),
    SHUTDOWN("Thermal Shutdown Imminent", true),
    UNKNOWN("Unavailable", false)
}

data class ThermalState(
    val severity: ThermalSeverity = ThermalSeverity.NONE,
    val batteryTemperatureCelsius: Float = 28.5f,
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val isThrottlingActive: Boolean = false,
    val thermalHeadroom: Float? = null // 0.0 to 1.0 where supported on API 30+
)
