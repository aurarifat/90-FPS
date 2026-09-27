package com.example.ui.screens.monitor

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ThermalSeverity
import com.example.ui.components.GamingGauge
import com.example.ui.components.SectionHeader
import com.example.ui.components.TelemetryCard
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
fun MonitorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.livePerformance.collectAsStateWithLifecycle()
    val thermalState by viewModel.thermalState.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader(title = "Live Performance & Frame Timing")

        // Dual Gauge Row: Measured Render FPS & Panel Refresh Rate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GamingGauge(
                currentValue = telemetry.currentFps,
                maxValue = 120f,
                label = "Render FPS",
                unit = "FPS",
                subLabel = "Frame: ${telemetry.frameTimeMs} ms",
                size = 170.dp,
                highlightColor = NeonYellow,
                modifier = Modifier.testTag("render_fps_gauge")
            )

            GamingGauge(
                currentValue = deviceInfo.currentRefreshRate,
                maxValue = 120f,
                label = "Panel Rate",
                unit = "Hz",
                subLabel = "Hardware Active",
                size = 170.dp,
                highlightColor = CyberCyan,
                modifier = Modifier.testTag("panel_rate_gauge")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Frame-Time Stability Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("frametime_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = NeonYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Frame Time Variance (Choreographer)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Text(
                        text = "${telemetry.frameTimeMs} ms",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (telemetry.frameTimeMs <= 17f) SuccessGreen else WarningOrange
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Frame Time Progress Bar (0 to 33.3ms)
                val normalizedFrameTime = (telemetry.frameTimeMs / 33.3f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { normalizedFrameTime },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (telemetry.frameTimeMs <= 16.7f) SuccessGreen else if (telemetry.frameTimeMs <= 22f) WarningOrange else DangerRed,
                    trackColor = DarkSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("11.1ms (90Hz)", style = MaterialTheme.typography.labelSmall, color = TextGray)
                    Text("16.6ms (60Hz)", style = MaterialTheme.typography.labelSmall, color = TextGray)
                    Text("33.3ms (30Hz)", style = MaterialTheme.typography.labelSmall, color = TextGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 90 FPS Stability Metrics Section
        SectionHeader(title = "Frame Pacing & Stutter Analysis")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryCard(
                title = "Stability Index",
                value = "${telemetry.stabilityScorePercent}%",
                subtitle = if (telemetry.stabilityScorePercent >= 90) "Smooth frame pacing" else "Micro-stutters detected",
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f),
                accentColor = if (telemetry.stabilityScorePercent >= 90) SuccessGreen else WarningOrange
            )

            TelemetryCard(
                title = "1% Low FPS",
                value = "${telemetry.onePercentLowFps} FPS",
                subtitle = "Worst 1% frame drops",
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f),
                accentColor = if (telemetry.onePercentLowFps >= 75f) SuccessGreen else if (telemetry.onePercentLowFps >= 50f) WarningOrange else DangerRed
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryCard(
                title = "Frame Jitter",
                value = "±${telemetry.frameJitterMs} ms",
                subtitle = if (telemetry.frameJitterMs <= 2.5f) "Consistent delivery" else "High latency variance",
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f),
                accentColor = if (telemetry.frameJitterMs <= 2.5f) SuccessGreen else WarningOrange
            )

            TelemetryCard(
                title = "Stutters / Janks",
                value = "${telemetry.jankCount}",
                subtitle = "Dropped render frames",
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f),
                accentColor = if (telemetry.jankCount == 0) SuccessGreen else DangerRed
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Telemetry Cards
        SectionHeader(title = "Hardware Telemetry")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryCard(
                title = "CPU Utilization",
                value = "${telemetry.cpuUsagePercent}%",
                subtitle = "Helio G88 / Cortex Cores",
                icon = Icons.Default.Memory,
                modifier = Modifier.weight(1f),
                accentColor = if (telemetry.cpuUsagePercent > 80) DangerRed else CyberCyan
            )

            TelemetryCard(
                title = "RAM Memory",
                value = "${telemetry.usedRamMb} MB",
                subtitle = "of ${telemetry.totalRamMb} MB Total",
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
                subtitle = if (thermalState.isThrottlingActive) "Thermal throttling active" else "Normal operational status",
                icon = Icons.Default.Thermostat,
                modifier = Modifier.weight(1f),
                accentColor = if (isThermalHot) WarningOrange else SuccessGreen
            )

            TelemetryCard(
                title = "Battery Temperature",
                value = "${thermalState.batteryTemperatureCelsius}°C",
                subtitle = "${thermalState.batteryLevel}% Battery Level",
                icon = Icons.Default.Thermostat,
                modifier = Modifier.weight(1f),
                accentColor = if (thermalState.batteryTemperatureCelsius >= 42f) DangerRed else SuccessGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Honest Telemetry Boundary Disclaimer Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("telemetry_disclaimer_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NeonYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Telemetry Measurement Transparency",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "• Measured Render FPS: Calculated from Android Choreographer frame intervals.\n" +
                            "• Panel Refresh Rate: Real hardware display mode provided by DisplayManager.\n" +
                            "• In-Game Surface FPS: Android security strictly isolates 3D graphics contexts across applications. Unprivileged background apps cannot tap another game's private Vulkan/OpenGL render pipeline without ADB SurfaceFlinger dumpsys access. We do not display fabricated FPS numbers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
    }
}
