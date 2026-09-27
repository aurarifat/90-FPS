package com.example.ui.screens.compatibility

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ActivityLogItem
import com.example.model.DeviceInfo
import com.example.model.ShizukuStatus
import com.example.model.ThermalState
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CompatibilityScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val shizukuStatus by viewModel.shizukuStatus.collectAsStateWithLifecycle()
    val thermalState by viewModel.thermalState.collectAsStateWithLifecycle()
    val logs by viewModel.activityLogs.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Compatibility", "Checklist", "Activity Log")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = NeonYellow,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) NeonYellow else TextGray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> CompatibilityTab(deviceInfo, shizukuStatus, thermalState)
            1 -> ChecklistTab(viewModel, deviceInfo, thermalState)
            2 -> ActivityLogTab(logs, onUndo = { viewModel.undoLastAction() }, onClear = { viewModel.clearLogs() })
        }
    }
}

@Composable
private fun CompatibilityTab(
    deviceInfo: DeviceInfo,
    shizukuStatus: ShizukuStatus,
    thermalState: ThermalState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "Hardware & Feature Analysis")
        }

        item {
            // Display Refresh Rate Hardware Check
            DiagnosticItemCard(
                title = "Display Panel Hardware Rate",
                status = if (deviceInfo.is90HzPhysicallySupported) "90Hz Supported" else "60Hz Hardware Limit",
                isPassed = deviceInfo.is90HzPhysicallySupported,
                description = if (deviceInfo.is90HzPhysicallySupported) {
                    "Your physical display panel supports [${deviceInfo.supportedRefreshRates.joinToString()} Hz]. High refresh rate gaming is possible if game engines support it."
                } else {
                    "Hardware Limitation: This device's physical panel only supports [${deviceInfo.supportedRefreshRates.joinToString()} Hz]. No software, booster, or root command can force a 60Hz panel to display 90 frames per second."
                },
                icon = Icons.Default.Tv
            )
        }

        item {
            // SoC / Processor Check
            DiagnosticItemCard(
                title = "Chipset & GPU Architecture",
                status = "${deviceInfo.socModel} (${deviceInfo.totalRamGb.toInt()}GB RAM)",
                isPassed = true,
                description = "SoC detected: ${deviceInfo.socModel}. Helio G88 features an octa-core CPU (2x Cortex-A75 @ 2.0GHz, 6x Cortex-A55 @ 1.8GHz) and Mali-G52 MC2 GPU. While solid for casual games, heavy 3D titles (e.g. Genshin, Warzone) may struggle to maintain 60 FPS under sustained thermal load.",
                icon = Icons.Default.Memory
            )
        }

        item {
            // Shizuku Privileged Authorization Check
            DiagnosticItemCard(
                title = "Shizuku ADB Authorization",
                status = if (shizukuStatus.canExecuteAdbCommands) "Authorized (v${shizukuStatus.version})" else "Not Authorized",
                isPassed = shizukuStatus.canExecuteAdbCommands,
                description = if (shizukuStatus.canExecuteAdbCommands) {
                    "Shizuku service is running via Wireless Debugging. Display refresh rates can be adjusted safely without rooting your device."
                } else {
                    "Shizuku is required to modify Android system display refresh rate settings without root. Follow the Shizuku setup guide in Settings."
                },
                icon = Icons.Default.CheckCircle
            )
        }

        item {
            // Thermal Headroom Check
            DiagnosticItemCard(
                title = "Thermal Headroom & Safety",
                status = "${thermalState.batteryTemperatureCelsius}°C • ${thermalState.severity.label}",
                isPassed = !thermalState.isThrottlingActive,
                description = if (thermalState.isThrottlingActive) {
                    "Thermal warning: Device is running warm. Android's thermal manager actively throttles CPU/GPU frequencies to prevent battery damage and screen degradation."
                } else {
                    "Thermal status is optimal. Battery temperature is within normal safe operational tolerances."
                },
                icon = Icons.Default.Thermostat
            )
        }

        item {
            // 90 FPS Instability Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonYellow.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = NeonYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How to Fix Unstable 90 FPS (Frame Drops & Stutters)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "If your device supports 90 FPS but stutters or drops frames, here are the 3 causes and solutions:\n\n" +
                                "1. Dynamic Refresh Rate Drops: Android's display power saver drops the panel to 60Hz whenever touch input pauses for 1-2 seconds. Solution: Use our 'Stabilize 90 FPS' button to lock both min & peak refresh rates to 90Hz via Shizuku.\n\n" +
                                "2. GPU Rendering Bottleneck: Rendering 90 FPS requires computing 90 full frames every second (11.1ms per frame). On chipsets like Helio G88 / Mali-G52, high graphics settings overwhelm the GPU. Solution: Lower your in-game Graphics setting to 'Smooth / Low' while keeping Frame Rate set to 'Extreme / 90 FPS'.\n\n" +
                                "3. Thermal Throttling: If the battery reaches 41°C-42°C, Android forcibly cuts CPU/GPU clocks by 25-30%. Solution: Remove thick cases while gaming, avoid playing while fast-charging, and play in a cool room.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItemCard(
    title: String,
    status: String,
    isPassed: Boolean,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isPassed) SuccessGreen else WarningOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                StatusBadge(
                    text = status,
                    indicatorColor = if (isPassed) SuccessGreen else WarningOrange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
        }
    }
}

@Composable
private fun ChecklistTab(
    viewModel: MainViewModel,
    deviceInfo: DeviceInfo,
    thermalState: ThermalState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "Safe Optimization Checklist")
            Text(
                text = "Safe practices to maximize gaming responsiveness without risky commands or killing vital system processes.",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
        }

        item {
            ChecklistItemCard(
                title = "1. Trim Booster Caches & Memory",
                subtitle = "Reclaims JVM garbage and clears temporary telemetry buffers.",
                actionLabel = "Run Safe Clean",
                onAction = { viewModel.performSafeOptimization() }
            )
        }

        item {
            ChecklistItemCard(
                title = "2. Check Battery Saver Mode",
                subtitle = "Android Battery Saver locks display refresh rates to 60Hz or lower. Disable Battery Saver before gaming for highest fluidity.",
                actionLabel = null,
                onAction = null
            )
        }

        item {
            ChecklistItemCard(
                title = "3. Close Background Apps Manually",
                subtitle = "Use Android Recent Apps overview to swipe away unused heavy apps (browsers, social media) to free up RAM for games.",
                actionLabel = null,
                onAction = null
            )
        }

        item {
            ChecklistItemCard(
                title = "4. Thermal Headroom Maintenance",
                subtitle = "Avoid playing 3D games while fast-charging or under direct sunlight. Elevated thermal levels force Android to throttle GPU clock speeds.",
                actionLabel = null,
                onAction = null
            )
        }
    }
}

@Composable
private fun ChecklistItemCard(
    title: String,
    subtitle: String,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(actionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ActivityLogTab(
    logs: List<ActivityLogItem>,
    onUndo: () -> Unit,
    onClear: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                title = "Activity Log (${logs.size})",
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onUndo,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.testTag("log_undo_button")
                ) {
                    Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo")
                }

                IconButton(onClick = onClear) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs", tint = TextGray)
                }
            }
        }

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No recent activity logs recorded", color = TextGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (log.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (log.success) SuccessGreen else DangerRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.targetName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }

                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextGray
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = log.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )

                            if (log.isUndoable) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Undoable change (Previous: ${log.previousValue ?: "Default"})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
