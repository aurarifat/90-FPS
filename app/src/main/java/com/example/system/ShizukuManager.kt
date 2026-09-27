package com.example.system

import android.content.Context
import android.content.pm.PackageManager
import com.example.model.ShizukuStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class ShizukuManager(private val context: Context) {

    private val _status = MutableStateFlow(ShizukuStatus())
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    private val REQUEST_CODE_SHIZUKU = 9001

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        updateStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        updateStatus()
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE_SHIZUKU) {
            updateStatus()
        }
    }

    fun init() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (_: Throwable) {
        }
        updateStatus()
    }

    fun destroy() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (_: Throwable) {
        }
    }

    fun updateStatus() {
        val isInstalled = checkShizukuInstalled()
        var isRunning = false
        var isPreV11 = false
        var version = 0
        var isGranted = false

        if (isInstalled) {
            try {
                isRunning = Shizuku.pingBinder()
                if (isRunning) {
                    isPreV11 = Shizuku.isPreV11()
                    version = Shizuku.getVersion()
                    isGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                }
            } catch (_: Throwable) {
                isRunning = false
            }
        }

        val message = when {
            !isInstalled -> "Shizuku is not installed. Install Shizuku to authorize ADB system settings."
            !isRunning -> "Shizuku service is not running. Start via Wireless Debugging."
            !isGranted -> "Shizuku permission not granted. Tap 'Authorize' to grant."
            else -> "Shizuku authorized (v$version). Wireless Debugging active."
        }

        _status.value = ShizukuStatus(
            isInstalled = isInstalled,
            isRunning = isRunning,
            isPermissionGranted = isGranted,
            version = version,
            isPreV11 = isPreV11,
            canExecuteAdbCommands = isRunning && isGranted,
            statusMessage = message
        )
    }

    private fun checkShizukuInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun requestPermission() {
        if (!_status.value.isRunning) return
        try {
            if (Shizuku.isPreV11()) {
                // Pre-v11 not supported
            } else {
                Shizuku.requestPermission(REQUEST_CODE_SHIZUKU)
            }
        } catch (_: Throwable) {
        }
    }

    /**
     * Executes an authorized system settings command via Shizuku.
     * Only supported operations like reading or setting display refresh rates are allowed.
     */
    suspend fun executeSupportedCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        if (!_status.value.canExecuteAdbCommands) {
            return@withContext Result.failure(IllegalStateException("Shizuku ADB service is not authorized or running."))
        }

        // Validate command safety whitelist
        if (!isCommandSafe(command)) {
            return@withContext Result.failure(SecurityException("Command is not in the safe authorized whitelist."))
        }

        try {
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            ).apply { isAccessible = true }

            val process = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as? Process
                ?: return@withContext Result.failure(IllegalStateException("Failed to create Shizuku process"))

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = StringBuilder()
            val errorOutput = StringBuilder()

            var line: String? = reader.readLine()
            while (line != null) {
                output.appendLine(line)
                line = reader.readLine()
            }

            var errLine: String? = errorReader.readLine()
            while (errLine != null) {
                errorOutput.appendLine(errLine)
                errLine = errorReader.readLine()
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Result.success(output.toString().trim())
            } else {
                val err = errorOutput.toString().trim()
                Result.failure(Exception(if (err.isNotBlank()) err else "Process exited with code $exitCode"))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private fun isCommandSafe(cmd: String): Boolean {
        val trimmed = cmd.trim()
        // Allowed safe commands
        val allowedPrefixes = listOf(
            "settings get system min_refresh_rate",
            "settings get system peak_refresh_rate",
            "settings get system user_refresh_rate",
            "settings put system min_refresh_rate",
            "settings put system peak_refresh_rate",
            "settings put system user_refresh_rate",
            "settings delete system min_refresh_rate",
            "settings delete system peak_refresh_rate",
            "settings delete system user_refresh_rate",
            "pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS",
            "cmd game mode",
            "cmd game set",
            "cmd game list",
            "dumpsys display"
        )
        return allowedPrefixes.any { trimmed.startsWith(it) }
    }
}
