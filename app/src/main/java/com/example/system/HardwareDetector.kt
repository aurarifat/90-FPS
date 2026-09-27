package com.example.system

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.Display
import android.view.WindowManager
import com.example.model.DeviceInfo
import java.io.File
import java.io.RandomAccessFile

class HardwareDetector(private val context: Context) {

    fun detectDeviceInfo(): DeviceInfo {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val defaultDisplay = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
            ?: (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay

        val currentRefreshRate = defaultDisplay?.refreshRate ?: 60f

        val supportedRefreshRates = mutableListOf<Float>()
        var displayResolution = "1920x1080"

        if (defaultDisplay != null) {
            val modes = defaultDisplay.supportedModes
            for (mode in modes) {
                supportedRefreshRates.add(mode.refreshRate)
            }
            defaultDisplay.mode?.let { activeMode ->
                displayResolution = "${activeMode.physicalWidth}x${activeMode.physicalHeight}"
            }
        }

        if (supportedRefreshRates.isEmpty()) {
            supportedRefreshRates.add(60f)
        }

        val distinctRates = supportedRefreshRates.map { kotlin.math.round(it * 10f) / 10f }.distinct().sorted()
        val is90HzSupported = distinctRates.any { it >= 89f }

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalRam = memInfo.totalMem
        val availRam = memInfo.availMem

        val dataPath = Environment.getDataDirectory().path
        val statFs = StatFs(dataPath)
        val totalStorage = statFs.totalBytes
        val availStorage = statFs.availableBytes

        val soc = detectSocModel()
        val isHelioG88OrTargetClass = soc.contains("G88", ignoreCase = true) ||
                soc.contains("MT6769", ignoreCase = true) ||
                Build.HARDWARE.contains("mt6769", ignoreCase = true) ||
                Build.MODEL.contains("XPad", ignoreCase = true)

        return DeviceInfo(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            brand = Build.BRAND.replaceFirstChar { it.uppercase() },
            deviceCode = Build.DEVICE,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            socModel = soc,
            totalRamBytes = totalRam,
            availableRamBytes = availRam,
            totalStorageBytes = totalStorage,
            availableStorageBytes = availStorage,
            currentRefreshRate = currentRefreshRate,
            supportedRefreshRates = distinctRates,
            displayResolution = displayResolution,
            is90HzPhysicallySupported = is90HzSupported,
            isHelioG88OrTargetClass = isHelioG88OrTargetClass
        )
    }

    private fun detectSocModel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val socField = Build.SOC_MODEL
                if (!socField.isNullOrBlank() && socField != "unknown") {
                    return socField
                }
            } catch (_: Throwable) {
            }
        }

        // Try reading /proc/cpuinfo
        try {
            val file = File("/proc/cpuinfo")
            if (file.exists() && file.canRead()) {
                val reader = RandomAccessFile(file, "r")
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line.startsWith("Hardware", ignoreCase = true)) {
                        val parts = line.split(":")
                        if (parts.size > 1) {
                            val hw = parts[1].trim()
                            reader.close()
                            return hw
                        }
                    }
                    line = reader.readLine()
                }
                reader.close()
            }
        } catch (_: Throwable) {
        }

        return if (Build.HARDWARE.isNotBlank() && Build.HARDWARE != "unknown") {
            Build.HARDWARE
        } else {
            "MediaTek / ARMv8"
        }
    }
}
