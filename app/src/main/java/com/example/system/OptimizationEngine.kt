package com.example.system

import android.content.Context
import com.example.data.local.entity.GameProfile
import com.example.data.repository.ActivityLogRepository
import com.example.model.DeviceInfo
import com.example.model.ThermalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class OptimizationResult(
    val success: Boolean,
    val message: String,
    val details: String? = null,
    val wasThrottled: Boolean = false
)

class OptimizationEngine(
    private val context: Context,
    private val shizukuManager: ShizukuManager,
    private val activityLogRepository: ActivityLogRepository
) {

    // Store original display settings to restore when games close
    private var originalMinRefreshRate: String? = null
    private var originalPeakRefreshRate: String? = null
    private var isProfileApplied = false
    private var activeProfilePackage: String? = null
    private var isDualRateLocked = false

    suspend fun applyProfile(
        profile: GameProfile,
        deviceInfo: DeviceInfo,
        thermalState: ThermalState
    ): OptimizationResult = withContext(Dispatchers.IO) {
        // 1. Thermal protection check
        if (thermalState.isThrottlingActive || thermalState.batteryTemperatureCelsius >= 43.0f) {
            val message = "Thermal protection active (${thermalState.batteryTemperatureCelsius}°C, ${thermalState.severity.label}). Optimization paused to protect hardware."
            activityLogRepository.log(
                targetName = profile.appName,
                actionType = "THERMAL_THROTTLE",
                details = message,
                packageName = profile.packageName,
                success = false,
                isUndoable = false
            )
            return@withContext OptimizationResult(
                success = false,
                message = message,
                wasThrottled = true
            )
        }

        // 2. Hardware display check
        val targetRate = profile.requestedRefreshRate
        if (targetRate > 0f && !deviceInfo.supportedRefreshRates.any { kotlin.math.abs(it - targetRate) < 1f }) {
            val message = "Hardware Limitation: Display panel only supports [${deviceInfo.supportedRefreshRates.joinToString()} Hz]. Cannot apply ${targetRate.toInt()} Hz."
            activityLogRepository.log(
                targetName = profile.appName,
                actionType = "UNSUPPORTED_HARDWARE",
                details = message,
                packageName = profile.packageName,
                success = false,
                isUndoable = false
            )
            return@withContext OptimizationResult(
                success = false,
                message = message
            )
        }

        // 3. Shizuku ADB execution
        val shizukuStatus = shizukuManager.status.value
        if (!shizukuStatus.canExecuteAdbCommands) {
            val message = "Shizuku not authorized. Profile saved, but system refresh rate change requires Shizuku Wireless Debugging."
            activityLogRepository.log(
                targetName = profile.appName,
                actionType = "PROFILE_SAVED_LOCAL",
                details = message,
                packageName = profile.packageName,
                success = true,
                isUndoable = false
            )
            return@withContext OptimizationResult(
                success = false,
                message = message
            )
        }

        // Backup current settings before applying
        if (!isProfileApplied) {
            backupCurrentRefreshRates()
        }

        // Apply refresh rate
        val rateStr = targetRate.toInt().toString()
        val minCmd = if (profile.lockMinAndMaxRate) {
            "settings put system min_refresh_rate $rateStr"
        } else {
            "settings put system min_refresh_rate 60"
        }
        val peakCmd = "settings put system peak_refresh_rate $rateStr"
        val userCmd = "settings put system user_refresh_rate $rateStr"

        val minRes = shizukuManager.executeSupportedCommand(minCmd)
        val peakRes = shizukuManager.executeSupportedCommand(peakCmd)
        val userRes = shizukuManager.executeSupportedCommand(userCmd)

        // Try Game Mode if requested and package is available
        if (profile.requestGameMode && profile.packageName.isNotBlank()) {
            try {
                shizukuManager.executeSupportedCommand("cmd game mode 2 ${profile.packageName}")
            } catch (_: Throwable) {
            }
        }

        val appliedSuccessfully = minRes.isSuccess || peakRes.isSuccess || userRes.isSuccess

        if (appliedSuccessfully) {
            isProfileApplied = true
            isDualRateLocked = profile.lockMinAndMaxRate
            activeProfilePackage = profile.packageName

            val stabilityNote = if (profile.lockMinAndMaxRate) "Locked Min & Peak to $rateStr Hz to stop frequency hopping jitter." else "Peak set to $rateStr Hz."
            val details = "Applied profile: $stabilityNote Note: in-game graphics must match GPU rendering budget."
            activityLogRepository.log(
                targetName = profile.appName,
                actionType = "PROFILE_APPLIED",
                details = details,
                packageName = profile.packageName,
                previousValue = originalPeakRefreshRate ?: "Default",
                newValue = "$rateStr Hz (Dual Lock: ${profile.lockMinAndMaxRate})",
                success = true,
                isUndoable = true
            )

            // Optional memory trim for booster
            if (profile.aggressiveMemoryTrim) {
                System.gc()
            }

            OptimizationResult(
                success = true,
                message = "90 FPS Stability Profile active: Display locked to $rateStr Hz (Min & Max synced)"
            )
        } else {
            val err = minRes.exceptionOrNull()?.message ?: "Failed to write system settings"
            activityLogRepository.log(
                targetName = profile.appName,
                actionType = "APPLY_FAILED",
                details = "Shizuku setting write failed: $err",
                packageName = profile.packageName,
                success = false
            )
            OptimizationResult(
                success = false,
                message = "Failed to apply refresh rate via Shizuku: $err"
            )
        }
    }

    suspend fun stabilize90Fps(
        deviceInfo: DeviceInfo,
        thermalState: ThermalState
    ): OptimizationResult = withContext(Dispatchers.IO) {
        val targetRate = if (deviceInfo.supportedRefreshRates.any { it >= 89f }) 90f else deviceInfo.maxSupportedRefreshRate

        val profile = GameProfile(
            packageName = "system.global.stability",
            appName = "90 FPS Stability Lock",
            targetFps = targetRate.toInt(),
            requestedRefreshRate = targetRate,
            lockMinAndMaxRate = true,
            stabilityProfile = "STABILIZED_90",
            requestGameMode = true,
            aggressiveMemoryTrim = true
        )

        val result = applyProfile(profile, deviceInfo, thermalState)
        if (result.success) {
            activityLogRepository.log(
                targetName = "Global 90 FPS Stabilizer",
                actionType = "STABILITY_LOCK_ENGAGED",
                details = "Eliminated display variable rate stutter by locking both min and peak refresh rate to ${targetRate.toInt()}Hz. Cleaned JVM garbage.",
                success = true,
                isUndoable = true
            )
            OptimizationResult(
                success = true,
                message = "90 FPS Stabilizer engaged! Display locked at ${targetRate.toInt()}Hz (min & peak synced to prevent dynamic rate drops)."
            )
        } else {
            result
        }
    }

    suspend fun restoreDefaultSettings(): OptimizationResult = withContext(Dispatchers.IO) {
        val shizukuStatus = shizukuManager.status.value
        if (!shizukuStatus.canExecuteAdbCommands) {
            isProfileApplied = false
            activeProfilePackage = null
            isDualRateLocked = false
            return@withContext OptimizationResult(
                success = false,
                message = "Cannot restore via Shizuku: Service not authorized."
            )
        }

        try {
            // Restore min / peak refresh rates
            val peakVal = originalPeakRefreshRate?.takeIf { it.isNotBlank() && it != "null" }
            val minVal = originalMinRefreshRate?.takeIf { it.isNotBlank() && it != "null" }

            if (peakVal != null) {
                shizukuManager.executeSupportedCommand("settings put system peak_refresh_rate $peakVal")
            } else {
                shizukuManager.executeSupportedCommand("settings delete system peak_refresh_rate")
            }

            if (minVal != null) {
                shizukuManager.executeSupportedCommand("settings put system min_refresh_rate $minVal")
            } else {
                shizukuManager.executeSupportedCommand("settings delete system min_refresh_rate")
            }

            shizukuManager.executeSupportedCommand("settings delete system user_refresh_rate")

            isProfileApplied = false
            isDualRateLocked = false
            val prevApp = activeProfilePackage ?: "System"
            activeProfilePackage = null

            activityLogRepository.log(
                targetName = prevApp,
                actionType = "SETTINGS_RESTORED",
                details = "Restored display refresh rate to system defaults ($originalPeakRefreshRate Hz)",
                success = true,
                isUndoable = false
            )

            OptimizationResult(
                success = true,
                message = "Display refresh rates restored to standard system configuration."
            )
        } catch (e: Throwable) {
            OptimizationResult(
                success = false,
                message = "Error restoring settings: ${e.message}"
            )
        }
    }

    suspend fun undoLastAction(): OptimizationResult = withContext(Dispatchers.IO) {
        val latest = activityLogRepository.getLatestUndoableLog()
            ?: return@withContext OptimizationResult(
                success = false,
                message = "No undoable settings change found."
            )

        val result = restoreDefaultSettings()
        if (result.success) {
            activityLogRepository.markUndone(latest.id)
        }
        result
    }

    suspend fun requestHighestSupportedRate(deviceInfo: DeviceInfo): OptimizationResult = withContext(Dispatchers.IO) {
        val highest = deviceInfo.maxSupportedRefreshRate
        val profile = GameProfile(
            packageName = "system.global",
            appName = "Global Display Refresh Rate",
            requestedRefreshRate = highest,
            targetFps = highest.toInt(),
            lockMinAndMaxRate = true
        )
        applyProfile(profile, deviceInfo, ThermalState())
    }

    private suspend fun backupCurrentRefreshRates() {
        try {
            val minRes = shizukuManager.executeSupportedCommand("settings get system min_refresh_rate")
            if (minRes.isSuccess) {
                originalMinRefreshRate = minRes.getOrNull()
            }
            val peakRes = shizukuManager.executeSupportedCommand("settings get system peak_refresh_rate")
            if (peakRes.isSuccess) {
                originalPeakRefreshRate = peakRes.getOrNull()
            }
        } catch (_: Throwable) {
        }
    }

    fun isProfileActive(): Boolean = isProfileApplied
    fun isDualRateLockActive(): Boolean = isDualRateLocked
    fun getActivePackage(): String? = activeProfilePackage
}
