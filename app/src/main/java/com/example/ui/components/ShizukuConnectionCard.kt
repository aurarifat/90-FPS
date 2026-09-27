package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShizukuStatus
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningOrange

/**
 * Material 3 Shizuku Connection Status Component.
 * Checks for permissions, displays live connection state, and provides
 * one-tap authorization and diagnostic controls.
 */
@Composable
fun ShizukuConnectionCard(
    shizukuStatus: ShizukuStatus,
    onRequestPermission: () -> Unit,
    onRefreshStatus: () -> Unit,
    onOpenShizukuApp: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val stateColor by animateColorAsState(
        targetValue = when {
            shizukuStatus.canExecuteAdbCommands -> SuccessGreen
            shizukuStatus.isRunning -> NeonYellow
            shizukuStatus.isInstalled -> WarningOrange
            else -> DangerRed
        },
        animationSpec = tween(400),
        label = "shizukuStateColor"
    )

    val statusBadgeText = when {
        shizukuStatus.canExecuteAdbCommands -> "AUTHORIZED"
        shizukuStatus.isRunning -> "AUTH REQUIRED"
        shizukuStatus.isInstalled -> "SERVICE STOPPED"
        else -> "NOT INSTALLED"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shizuku_connection_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(stateColor.copy(alpha = 0.15f))
                            .border(1.dp, stateColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                shizukuStatus.canExecuteAdbCommands -> Icons.Default.CheckCircle
                                shizukuStatus.isRunning -> Icons.Default.Security
                                else -> Icons.Default.Warning
                            },
                            contentDescription = "Shizuku Status",
                            tint = stateColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Shizuku ADB Integration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = if (shizukuStatus.version > 0) "Shizuku API v${shizukuStatus.version}" else "Wireless Debugging Protocol",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        text = statusBadgeText,
                        indicatorColor = stateColor
                    )
                    IconButton(
                        onClick = onRefreshStatus,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("refresh_shizuku_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Shizuku",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Diagnostic Indicator Pill Rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagnosticPill(
                    label = "Service",
                    value = if (shizukuStatus.isRunning) "Running" else "Stopped",
                    isActive = shizukuStatus.isRunning,
                    activeColor = SuccessGreen,
                    inactiveColor = DangerRed,
                    modifier = Modifier.weight(1f)
                )

                DiagnosticPill(
                    label = "Permission",
                    value = if (shizukuStatus.isPermissionGranted) "Granted" else "Pending",
                    isActive = shizukuStatus.isPermissionGranted,
                    activeColor = SuccessGreen,
                    inactiveColor = if (shizukuStatus.isRunning) NeonYellow else TextGray,
                    modifier = Modifier.weight(1f)
                )

                DiagnosticPill(
                    label = "ADB Status",
                    value = if (shizukuStatus.canExecuteAdbCommands) "Ready" else "Standby",
                    isActive = shizukuStatus.canExecuteAdbCommands,
                    activeColor = CyberCyan,
                    inactiveColor = TextGray,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // State Explanation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, stateColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = stateColor,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            shizukuStatus.canExecuteAdbCommands ->
                                "Shizuku is fully connected and authorized. Display refresh rate dual-locking and Android Game Mode overrides are active."
                            shizukuStatus.isRunning && !shizukuStatus.isPermissionGranted ->
                                "Shizuku service is running! Tap 'Authorize Now' below to grant system settings permission to this app."
                            shizukuStatus.isInstalled && !shizukuStatus.isRunning ->
                                "Shizuku app is installed, but the service is stopped. Open Shizuku and start via Wireless Debugging, then tap Refresh."
                            else ->
                                "Shizuku is not detected. Tap 'Open Shizuku' to install or launch the official Shizuku manager."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row (Authorize is ALWAYS interactive when needed!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!shizukuStatus.canExecuteAdbCommands) {
                    // AUTHORIZE BUTTON: ALWAYS CLICKABLE!
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (shizukuStatus.isRunning) NeonYellow else DarkSurfaceVariant,
                            contentColor = if (shizukuStatus.isRunning) Color.Black else NeonYellow
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = if (!shizukuStatus.isRunning) BorderStroke(1.dp, NeonYellow.copy(alpha = 0.7f)) else null,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("request_shizuku_perm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (shizukuStatus.isRunning) "Authorize Now" else "Check & Authorize",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Already Authorized Banner Button
                    OutlinedButton(
                        onClick = onRefreshStatus,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Authorized & Active", fontWeight = FontWeight.Bold)
                    }
                }

                // Open Shizuku App Shortcut
                OutlinedButton(
                    onClick = onOpenShizukuApp,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("open_shizuku_app_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Shizuku", maxLines = 1)
                }

                // Setup Guide Button
                IconButton(
                    onClick = onOpenGuide,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        .testTag("shizuku_guide_icon_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Wireless Debugging Guide",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticPill(
    label: String,
    value: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, if (isActive) activeColor.copy(alpha = 0.4f) else CardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isActive) activeColor else inactiveColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isActive) activeColor else TextWhite
            )
        }
    }
}
