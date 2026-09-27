package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.local.entity.GameProfile
import com.example.data.repository.ActivityLogRepository
import com.example.data.repository.GameProfileRepository
import com.example.system.HardwareDetector
import com.example.system.OptimizationEngine
import com.example.system.ShizukuManager
import com.example.system.ThermalManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameDetectionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var monitorJob: Job? = null

    private lateinit var profileRepository: GameProfileRepository
    private lateinit var logRepository: ActivityLogRepository
    private lateinit var hardwareDetector: HardwareDetector
    private lateinit var thermalManager: ThermalManager
    private lateinit var shizukuManager: ShizukuManager
    private lateinit var optimizationEngine: OptimizationEngine

    private var currentForegroundPackage: String? = null
    private var activeProfile: GameProfile? = null

    companion object {
        const val CHANNEL_ID = "fps_booster_game_service"
        const val NOTIFICATION_ID = 9002
        const val ACTION_STOP_SERVICE = "com.example.fpsbooster.ACTION_STOP_SERVICE"
        const val ACTION_RESTORE_SETTINGS = "com.example.fpsbooster.ACTION_RESTORE_SETTINGS"

        var isRunning = false
            private set

        fun startService(context: Context) {
            val intent = Intent(context, GameDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, GameDetectionService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        val db = AppDatabase.getInstance(this)
        profileRepository = GameProfileRepository(db.gameProfileDao())
        logRepository = ActivityLogRepository(db.activityLogDao())
        hardwareDetector = HardwareDetector(this)
        thermalManager = ThermalManager(this)
        thermalManager.startListening()
        shizukuManager = ShizukuManager(this)
        shizukuManager.init()
        optimizationEngine = OptimizationEngine(this, shizukuManager, logRepository)

        createNotificationChannel()
        val notification = buildNotification("Monitoring game launches...", null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_RESTORE_SETTINGS -> {
                serviceScope.launch {
                    optimizationEngine.restoreDefaultSettings()
                    activeProfile = null
                    updateNotification("Settings restored to system defaults", null)
                }
            }
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val deviceInfo = hardwareDetector.detectDeviceInfo()

            while (isActive) {
                val fgPackage = getForegroundPackage(usageStatsManager)
                if (fgPackage != null && fgPackage != currentForegroundPackage) {
                    val prevPackage = currentForegroundPackage
                    currentForegroundPackage = fgPackage

                    // Check if foreground package is a monitored game
                    val profile = profileRepository.getProfile(fgPackage)

                    if (profile != null && profile.autoActivate) {
                        // Game launched
                        activeProfile = profile
                        val thermal = thermalManager.thermalState.value
                        val result = optimizationEngine.applyProfile(profile, deviceInfo, thermal)
                        updateNotification(
                            "Game Active: ${profile.appName}",
                            "Display locked to ${profile.requestedRefreshRate.toInt()}Hz (${profile.targetFps} FPS Target)"
                        )
                    } else if (activeProfile != null && fgPackage != packageName) {
                        // Game closed / exited to another app or launcher
                        val closedGame = activeProfile?.appName ?: "Game"
                        optimizationEngine.restoreDefaultSettings()
                        activeProfile = null
                        updateNotification("Monitoring game launches", "$closedGame closed. Restored display defaults.")
                    }
                }
                delay(2500)
            }
        }
    }

    private fun getForegroundPackage(usageStatsManager: UsageStatsManager?): String? {
        if (usageStatsManager == null) return null
        val time = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(time - 10000, time)
        val event = UsageEvents.Event()
        var lastForegroundApp: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastForegroundApp = event.packageName
            }
        }
        return lastForegroundApp
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Game Booster Detection Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors foreground game launches and applies display refresh profiles."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, text: String?): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val restoreIntent = Intent(this, GameDetectionService::class.java).apply {
            action = ACTION_RESTORE_SETTINGS
        }
        val restorePendingIntent = PendingIntent.getService(
            this,
            1,
            restoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, GameDetectionService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text ?: "Automatic refresh rate adjustment active")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_revert, "Restore Defaults", restorePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, text: String?) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification(title, text))
    }

    override fun onDestroy() {
        isRunning = false
        monitorJob?.cancel()
        serviceScope.launch {
            if (activeProfile != null) {
                optimizationEngine.restoreDefaultSettings()
            }
        }
        thermalManager.stopListening()
        shizukuManager.destroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
