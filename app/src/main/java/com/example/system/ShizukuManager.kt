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
        var isRunning = false
        var isPreV11 = false
        var version = 0
        var isGranted = false

        // 1. Direct Ping: Always test if Shizuku binder is alive first
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

        // 2. Installed status: true if running, or if package exists, or launch intent resolves
        val isInstalled = isRunning || checkShizukuInstalled()

        val message = when {
            !isInstalled -> "Shizuku is not installed. Tap 'Install / Open' to setup Shizuku."
            !isRunning -> "Shizuku app detected but service is stopped. Open Shizuku and start via Wireless Debugging."
            !isGranted -> "Shizuku service is active! Tap 'Authorize' to grant ADB system access."
            else -> "Shizuku authorized (v$version). Wireless Debugging active & ready."
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
        // Check 1: Direct ping
        try {
            if (Shizuku.pingBinder()) return true
        } catch (_: Throwable) {
        }

        // Check 2: PackageManager lookup
        try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            return true
        } catch (_: Throwable) {
        }

        // Check 3: Launch Intent
        try {
            val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
            if (intent != null) return true
        } catch (_: Throwable) {
        }

        return false
    }

    fun requestPermission(): Boolean {
        updateStatus()
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.isPreV11()) {
                    return false
                }
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    updateStatus()
                    return true
                }
                Shizuku.requestPermission(REQUEST_CODE_SHIZUKU)
                return true
            } else {
                updateStatus()
                return false
            }
        } catch (_: Throwable) {
            updateStatus()
            return false
        }
    }

    fun openShizukuApp(ctx: Context = context): Boolean {
        return try {
            val intent = ctx.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(intent)
                true
            } else {
                val webIntent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://shizuku.rikka.app/")
                ).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(webIntent)
                false
            }
        } catch (_: Throwable) {
            false
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
