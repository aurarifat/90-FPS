package com.example.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.example.model.ThermalSeverity
import com.example.model.ThermalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThermalManager(private val context: Context) {

    private val _thermalState = MutableStateFlow(ThermalState())
    val thermalState: StateFlow<ThermalState> = _thermalState.asStateFlow()

    private var powerManager: PowerManager? = null
    private var thermalListener: PowerManager.OnThermalStatusChangedListener? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                val tempC = tempRaw / 10.0f
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val currentState = _thermalState.value
                val isHot = tempC >= 42.0f || currentState.severity.isWarning

                _thermalState.value = currentState.copy(
                    batteryTemperatureCelsius = tempC,
                    batteryLevel = level,
                    isCharging = isCharging,
                    isThrottlingActive = isHot
                )
            }
        }
    }

    fun startListening() {
        powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            thermalListener = PowerManager.OnThermalStatusChangedListener { status ->
                val severity = mapThermalStatus(status)
                val isThrottling = severity.isWarning || _thermalState.value.batteryTemperatureCelsius >= 42.0f
                _thermalState.value = _thermalState.value.copy(
                    severity = severity,
                    isThrottlingActive = isThrottling
                )
            }
            try {
                powerManager?.addThermalStatusListener(context.mainExecutor, thermalListener!!)
                val currentStatus = powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE
                _thermalState.value = _thermalState.value.copy(
                    severity = mapThermalStatus(currentStatus)
                )
            } catch (_: Throwable) {
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
    }

    fun stopListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && thermalListener != null) {
            try {
                powerManager?.removeThermalStatusListener(thermalListener!!)
            } catch (_: Throwable) {
            }
        }
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Throwable) {
        }
    }

    private fun mapThermalStatus(status: Int): ThermalSeverity {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return ThermalSeverity.NONE
        return when (status) {
            PowerManager.THERMAL_STATUS_NONE -> ThermalSeverity.NONE
            PowerManager.THERMAL_STATUS_LIGHT -> ThermalSeverity.LIGHT
            PowerManager.THERMAL_STATUS_MODERATE -> ThermalSeverity.MODERATE
            PowerManager.THERMAL_STATUS_SEVERE -> ThermalSeverity.SEVERE
            PowerManager.THERMAL_STATUS_CRITICAL -> ThermalSeverity.CRITICAL
            PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalSeverity.EMERGENCY
            PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalSeverity.SHUTDOWN
            else -> ThermalSeverity.UNKNOWN
        }
    }
}
