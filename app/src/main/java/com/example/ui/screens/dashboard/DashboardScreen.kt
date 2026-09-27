package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ThermalSeverity
import com.example.ui.components.GamingGauge
import com.example.ui.components.SectionHeader
import com.example.ui.components.ShizukuConnectionCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TelemetryCard
import com.example.ui.screens.settings.WirelessDebuggingGuideDialog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningOrange
import com.example.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToGames: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val thermalState by viewModel.thermalState.collectAsStateWithLifecycle()
    val shizukuStatus by viewModel.shizukuStatus.collectAsStateWithLifecycle()
    val telemetry by viewModel.livePerformance.collectAsStateWithLifecycle()
    val isDualRateLocked by viewModel.isDualRateLocked.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showGuide by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Device & Shizuku Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${deviceInfo.brand} ${deviceInfo.model}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Android ${deviceInfo.androidVersion} (API ${deviceInfo.apiLevel}) • ${deviceInfo.socModel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            StatusBadge(
                text = if (shizukuStatus.canExecuteAdbCommands) "Shizuku Active" else "Shizuku Offline",
                indicatorColor = if (shizukuStatus.canExecuteAdbCommands) SuccessGreen else DangerRed,
                modifier = Modifier.testTag("shizuku_badge")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Central Performance & Refresh Rate Gauge
        GamingGauge(
            currentValue = deviceInfo.currentRefreshRate,
            maxValue = if (deviceInfo.maxSupportedRefreshRate > 60f) deviceInfo.maxSupportedRefreshRate else 90f,
            label = "Display Refresh Rate",
            unit = "Hz",
            subLabel = "Max Supported: ${deviceInfo.maxSupportedRefreshRate.toInt()} Hz",
            highlightColor = if (deviceInfo.currentRefreshRate >= 89f) NeonYellow else CyberCyan,
            modifier = Modifier.testTag("dashboard_gauge")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Panel Reality Alert (Crucial for Infinix XPad 20 and 60Hz panels)
        if (!deviceInfo.is90HzPhysicallySupported) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hardware_limit_banner"),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Hardware Alert",
                        tint = WarningOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Display Hardware Limit: ${deviceInfo.maxSupportedRefreshRate.toInt()} Hz",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = WarningOrange
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "This device panel physically supports [${deviceInfo.supportedRefreshRates.joinToString()} Hz]. 90 Hz display output is physically impossible on this screen. Changing display modes does not force games to render beyond hardware capabilities.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 90 FPS Stability Controller Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stability_controller_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDualRateLocked) NeonYellow.copy(alpha = 0.6f) else CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "90 FPS Frame Pacing Stabilizer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isDualRateLocked) "Dual-Lock Active: Min & Peak fixed to eliminate display switching stutters" else "Display governor is dynamic (drops to 60Hz when touch stops)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }

                    StatusBadge(
                        text = if (isDualRateLocked) "Locked (Stable)" else "Dynamic (Unstable)",
                        indicatorColor = if (isDualRateLocked) SuccessGreen else WarningOrange
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.stabilize90Fps() },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("stabilize_90_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Stabilize 90 FPS Now",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.restoreDefaults() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("restore_defaults_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Rate")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 90 FPS Stability Advisory Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stability_advisory_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NeonYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Why is 90 FPS Unstable on Midrange Hardware?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. Display Frequency Hopping: Android drops from 90Hz to 60Hz whenever you lift your finger. Our 'Stabilize' button locks both min and peak to 90Hz to eliminate this.\n" +
                            "2. Thermal Throttling: Sustaining 90 FPS requires ~50% more GPU power than 60 FPS. If battery temp hits 42°C, the SoC governor throttles clock speeds.\n" +
                            "3. Solution: In your game's graphics settings, set Graphics to 'Smooth / Low' while setting Frame Rate to '90 FPS / Extreme'. This frees GPU headroom so 90 FPS stays rock solid.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Undo & Safe Memory Boost Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.undoLastAction() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("undo_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo Last Action")
            }

            OutlinedButton(
                onClick = { viewModel.performSafeOptimization() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("safe_clean_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonYellow),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text("Safe Clean Memory")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Telemetry Section
        SectionHeader(title = "Live Telemetry & Diagnostics")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryCard(
                title = "CPU Load",
                value = "${telemetry.cpuUsagePercent}%",
                subtitle = "Active cores load",
                icon = Icons.Default.Memory,
                modifier = Modifier.weight(1f),
                accentColor = if (telemetry.cpuUsagePercent > 75) DangerRed else CyberCyan
            )

            TelemetryCard(
                title = "RAM Usage",
                value = "${telemetry.ramUsagePercent}%",
                subtitle = "${telemetry.usedRamMb}MB / ${telemetry.totalRamMb}MB",
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f),
                accentColor = NeonYellow
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isThermalHot = thermalState.severity != ThermalSeverity.NONE || thermalState.batteryTemperatureCelsius >= 42f
            TelemetryCard(
                title = "Thermal Status",
                value = thermalState.severity.label,
                subtitle = if (thermalState.isThrottlingActive) "Throttling protection engaged" else "Normal operation",
                icon = Icons.Default.Thermostat,
                modifier = Modifier.weight(1f),
                accentColor = if (isThermalHot) WarningOrange else SuccessGreen
            )

            TelemetryCard(
                title = "Battery & Temp",
                value = "${thermalState.batteryTemperatureCelsius}°C",
                subtitle = "${thermalState.batteryLevel}% ${if (thermalState.isCharging) "(Charging)" else ""}",
                icon = Icons.Default.Security,
                modifier = Modifier.weight(1f),
                accentColor = if (thermalState.batteryTemperatureCelsius >= 40f) DangerRed else CyberCyan
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shizuku Connection Status Component (if not connected)
        if (!shizukuStatus.canExecuteAdbCommands) {
            ShizukuConnectionCard(
                shizukuStatus = shizukuStatus,
                onRequestPermission = { viewModel.requestShizukuPermission() },
                onRefreshStatus = { viewModel.refreshShizukuStatus() },
                onOpenShizukuApp = { viewModel.openShizukuApp(context) },
                onOpenGuide = { showGuide = true }
            )
        }
    }

    if (showGuide) {
        WirelessDebuggingGuideDialog(onDismiss = { showGuide = false })
    }
}
