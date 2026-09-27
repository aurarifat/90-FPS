package com.example.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.GameProfile
import com.example.model.DeviceInfo
import com.example.model.FpsMode
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningOrange

@Composable
fun ProfileEditorDialog(
    initialProfile: GameProfile,
    deviceInfo: DeviceInfo,
    onSave: (GameProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var targetFps by remember { mutableIntStateOf(initialProfile.targetFps) }
    var requestedRate by remember { mutableFloatStateOf(initialProfile.requestedRefreshRate) }
    var lockMinAndMax by remember { mutableStateOf(initialProfile.lockMinAndMaxRate) }
    var requestGameMode by remember { mutableStateOf(initialProfile.requestGameMode) }
    var stabilityProfile by remember { mutableStateOf(initialProfile.stabilityProfile) }
    var autoActivate by remember { mutableStateOf(initialProfile.autoActivate) }
    var aggressiveTrim by remember { mutableStateOf(initialProfile.aggressiveMemoryTrim) }
    var notes by remember { mutableStateOf(initialProfile.customNotes) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("profile_editor_dialog"),
        containerColor = DarkSurface,
        title = {
            Column {
                Text(
                    text = "Profile: ${initialProfile.appName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = initialProfile.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "TARGET FPS & DISPLAY RATE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonYellow
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Fps Modes Selection
                val modes = listOf(
                    FpsMode.AUTO,
                    FpsMode.FPS_30,
                    FpsMode.FPS_60,
                    FpsMode.FPS_90,
                    FpsMode.FPS_120
                )

                modes.forEach { mode ->
                    val isSupported = mode == FpsMode.AUTO || mode == FpsMode.FPS_30 ||
                            deviceInfo.supportedRefreshRates.any { it >= mode.refreshRate - 1f }
                    val isSelected = targetFps == mode.targetFps

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonYellow.copy(alpha = 0.15f) else DarkSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) NeonYellow else CardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = isSupported) {
                                targetFps = mode.targetFps
                                requestedRate = mode.refreshRate
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSupported) TextWhite else TextGray.copy(alpha = 0.5f)
                                )
                                if (!isSupported) {
                                    Text(
                                        text = "Hardware panel cannot display ${mode.targetFps}Hz",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarningOrange
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonYellow)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 90 FPS Stability Enhancements
                Text(
                    text = "90 FPS STABILITY CONTROLS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Dual Lock Switch (Min & Max Rate)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lock Min & Peak Refresh Rates",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextWhite
                        )
                        Text(
                            text = "Prevents Android from fluctuating between 60Hz and 90Hz on touch lifts (stops micro-stutters)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                    Switch(
                        checked = lockMinAndMax,
                        onCheckedChange = { lockMinAndMax = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonYellow,
                            checkedTrackColor = NeonYellow.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("lock_min_max_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Android Game Mode Request
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Request Android Game Mode (Performance)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextWhite
                        )
                        Text(
                            text = "Instructs OS scheduler to grant higher CPU/GPU priority and reduce background interference",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                    Switch(
                        checked = requestGameMode,
                        onCheckedChange = { requestGameMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonYellow,
                            checkedTrackColor = NeonYellow.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auto Activate Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Apply on Launch",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextWhite
                        )
                        Text(
                            text = "Applies profile automatically when game opens and restores defaults when closed",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                    Switch(
                        checked = autoActivate,
                        onCheckedChange = { autoActivate = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonYellow,
                            checkedTrackColor = NeonYellow.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("auto_activate_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Aggressive Memory Trim Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trim Memory on Launch",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextWhite
                        )
                        Text(
                            text = "Reclaims JVM garbage and flushes caches to give the game maximum RAM headroom",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                    Switch(
                        checked = aggressiveTrim,
                        onCheckedChange = { aggressiveTrim = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonYellow,
                            checkedTrackColor = NeonYellow.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Notes TextField
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Game Graphics Tuning Notes") },
                    placeholder = { Text("e.g. In-game: Smooth graphics + Extreme FPS for rock solid 90") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedLabelColor = NeonYellow,
                        unfocusedLabelColor = TextGray,
                        focusedIndicatorColor = NeonYellow,
                        unfocusedIndicatorColor = CardBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialProfile.copy(
                        targetFps = targetFps,
                        requestedRefreshRate = requestedRate,
                        lockMinAndMaxRate = lockMinAndMax,
                        requestGameMode = requestGameMode,
                        stabilityProfile = stabilityProfile,
                        autoActivate = autoActivate,
                        aggressiveMemoryTrim = aggressiveTrim,
                        customNotes = notes
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonYellow,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text("Cancel")
            }
        }
    )
}
