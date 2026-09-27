package com.example.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shizukuStatus by viewModel.shizukuStatus.collectAsStateWithLifecycle()
    val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var showGuide by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader(title = "Shizuku ADB Integration")

        // Shizuku Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shizuku_settings_card"),
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
                    Column {
                        Text(
                            text = "Shizuku Service Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = shizukuStatus.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }

                    StatusBadge(
                        text = if (shizukuStatus.canExecuteAdbCommands) "Connected" else "Disconnected",
                        indicatorColor = if (shizukuStatus.canExecuteAdbCommands) SuccessGreen else DangerRed
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.requestShizukuPermission() },
                        enabled = shizukuStatus.isRunning && !shizukuStatus.isPermissionGranted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonYellow,
                            contentColor = Color.Black,
                            disabledContainerColor = DarkSurfaceVariant,
                            disabledContentColor = TextGray
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("request_shizuku_perm_button")
                    ) {
                        Text("Authorize", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showGuide = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonYellow),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("open_wireless_guide_button")
                    ) {
                        Icon(Icons.Default.DeveloperMode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wireless Guide")
                    }

                    IconButton(onClick = { viewModel.refreshHardwareInfo() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Refresh", tint = TextWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(title = "Background Automation")

        // Game Detection Service Card
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto Game Detection Service",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Monitors foreground apps to automatically apply saved display rates when games launch.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }

                    Switch(
                        checked = isServiceRunning,
                        onCheckedChange = { viewModel.toggleDetectionService() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonYellow,
                            checkedTrackColor = NeonYellow.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("service_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Permission shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Usage Access")
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Notifications")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(title = "App & Device Information")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Application", "90 FPS BOOSTER v1.0")
                InfoRow("Hardware Target", "${deviceInfo.brand} ${deviceInfo.model}")
                InfoRow("Chipset", deviceInfo.socModel)
                InfoRow("RAM Capacity", "${deviceInfo.totalRamGb.toInt()} GB LPDDR4X")
                InfoRow("Physical Refresh Rate", "${deviceInfo.supportedRefreshRates.joinToString()} Hz")
                InfoRow("Android Version", "Android ${deviceInfo.androidVersion} (API ${deviceInfo.apiLevel})")
                InfoRow("Shizuku Status", if (shizukuStatus.canExecuteAdbCommands) "Authorized" else "Unavailable")

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Disclaimer: Actual gaming FPS depends entirely on physical hardware capabilities, thermal dissipation, GPU throttling, and game engine architecture. This utility adjusts supported display refresh rates and system parameters using official Android and Shizuku APIs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            }
        }
    }

    if (showGuide) {
        WirelessDebuggingGuideDialog(onDismiss = { showGuide = false })
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextGray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextWhite)
    }
}
