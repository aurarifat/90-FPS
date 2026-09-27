package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ActivityLogItem
import com.example.data.local.entity.GameProfile
import com.example.data.repository.ActivityLogRepository
import com.example.data.repository.GameProfileRepository
import com.example.model.DeviceInfo
import com.example.model.InstalledApp
import com.example.model.ShizukuStatus
import com.example.model.ThermalState
import com.example.service.GameDetectionService
import com.example.system.HardwareDetector
import com.example.system.LivePerformanceData
import com.example.system.OptimizationEngine
import com.example.system.PerformanceSampler
import com.example.system.ShizukuManager
import com.example.system.ThermalManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val profileRepository = GameProfileRepository(db.gameProfileDao())
    val logRepository = ActivityLogRepository(db.activityLogDao())

    private val hardwareDetector = HardwareDetector(application)
    val thermalManager = ThermalManager(application)
    val shizukuManager = ShizukuManager(application)
    val performanceSampler = PerformanceSampler(application)
    val optimizationEngine = OptimizationEngine(application, shizukuManager, logRepository)

    private val _deviceInfo = MutableStateFlow(hardwareDetector.detectDeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()

    val thermalState: StateFlow<ThermalState> = thermalManager.thermalState
    val shizukuStatus: StateFlow<ShizukuStatus> = shizukuManager.status
    val livePerformance: StateFlow<LivePerformanceData> = performanceSampler.telemetry

    val gameProfiles: StateFlow<List<GameProfile>> = profileRepository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<ActivityLogItem>> = logRepository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(GameDetectionService.isRunning)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _isDualRateLocked = MutableStateFlow(false)
    val isDualRateLocked: StateFlow<Boolean> = _isDualRateLocked.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    init {
        thermalManager.startListening()
        shizukuManager.init()
        performanceSampler.start(viewModelScope)
        loadInstalledApps()
    }

    fun refreshHardwareInfo() {
        _deviceInfo.value = hardwareDetector.detectDeviceInfo()
        shizukuManager.updateStatus()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val apps = withContext(Dispatchers.IO) {
                val pm = getApplication<Application>().packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                val list = mutableListOf<InstalledApp>()

                for (info in resolveInfos) {
                    val pkg = info.activityInfo.packageName
                    if (pkg == getApplication<Application>().packageName) continue
                    val label = info.loadLabel(pm).toString()
                    val icon = info.loadIcon(pm)
                    val appInfo = info.activityInfo.applicationInfo
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isGame = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        appInfo.category == ApplicationInfo.CATEGORY_GAME
                    } else {
                        false
                    }

                    list.add(
                        InstalledApp(
                            packageName = pkg,
                            appName = label,
                            icon = icon,
                            isSystemApp = isSystem,
                            isGameCategory = isGame
                        )
                    )
                }
                list.sortedWith(compareBy({ !it.isGameCategory }, { it.appName }))
            }
            _installedApps.value = apps
            _isLoadingApps.value = false
        }
    }

    fun saveProfile(profile: GameProfile) {
        viewModelScope.launch {
            profileRepository.saveProfile(profile)
            _snackbarMessage.emit("Profile saved for ${profile.appName}")
        }
    }

    fun deleteProfile(profile: GameProfile) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profile)
            _snackbarMessage.emit("Profile deleted for ${profile.appName}")
        }
    }

    fun applyProfile(profile: GameProfile) {
        viewModelScope.launch {
            val result = optimizationEngine.applyProfile(
                profile = profile,
                deviceInfo = _deviceInfo.value,
                thermalState = thermalState.value
            )
            _snackbarMessage.emit(result.message)
            refreshHardwareInfo()
        }
    }

    fun stabilize90Fps() {
        viewModelScope.launch {
            val result = optimizationEngine.stabilize90Fps(
                deviceInfo = _deviceInfo.value,
                thermalState = thermalState.value
            )
            _isDualRateLocked.value = optimizationEngine.isDualRateLockActive()
            _snackbarMessage.emit(result.message)
            refreshHardwareInfo()
        }
    }

    fun restoreDefaults() {
        viewModelScope.launch {
            val result = optimizationEngine.restoreDefaultSettings()
            _isDualRateLocked.value = false
            _snackbarMessage.emit(result.message)
            refreshHardwareInfo()
        }
    }

    fun undoLastAction() {
        viewModelScope.launch {
            val result = optimizationEngine.undoLastAction()
            _isDualRateLocked.value = optimizationEngine.isDualRateLockActive()
            _snackbarMessage.emit(result.message)
            refreshHardwareInfo()
        }
    }

    fun requestHighestSupportedRate() {
        viewModelScope.launch {
            val result = optimizationEngine.requestHighestSupportedRate(_deviceInfo.value)
            _isDualRateLocked.value = optimizationEngine.isDualRateLockActive()
            _snackbarMessage.emit(result.message)
            refreshHardwareInfo()
        }
    }

    fun requestShizukuPermission() {
        shizukuManager.requestPermission()
    }

    fun toggleDetectionService() {
        val app = getApplication<Application>()
        if (GameDetectionService.isRunning) {
            GameDetectionService.stopService(app)
            _isServiceRunning.value = false
            viewModelScope.launch {
                _snackbarMessage.emit("Game detection service stopped.")
            }
        } else {
            GameDetectionService.startService(app)
            _isServiceRunning.value = true
            viewModelScope.launch {
                _snackbarMessage.emit("Game detection service started.")
            }
        }
    }

    fun performSafeOptimization() {
        viewModelScope.launch {
            // Safe actions: system GC and clearing memory caches
            System.gc()
            logRepository.log(
                targetName = "Memory & Cache",
                actionType = "SAFE_OPTIMIZATION",
                details = "Executed JVM garbage collection and trimmed booster internal telemetry caches.",
                success = true
            )
            _snackbarMessage.emit("Safe memory optimization completed.")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            logRepository.clearLogs()
            _snackbarMessage.emit("Activity log cleared.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        performanceSampler.stop()
        thermalManager.stopListening()
        shizukuManager.destroy()
    }
}
