package com.example.ui.screens.games

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.GameProfile
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
fun GamesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profiles by viewModel.gameProfiles.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()

    var showPicker by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<GameProfile?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPicker = true },
                containerColor = NeonYellow,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_game_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Game")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                title = "Configured Game Profiles (${profiles.size})",
                trailing = {
                    Button(
                        onClick = { showPicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = NeonYellow
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_game_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Game")
                    }
                }
            )

            if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(36.dp))
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Games,
                                contentDescription = null,
                                tint = NeonYellow,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Games Configured",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your installed games to assign per-game display refresh rates and launch optimizations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonYellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Select Installed Game", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(profiles, key = { it.packageName }) { profile ->
                        val matchingApp = installedApps.find { it.packageName == profile.packageName }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("game_profile_card_${profile.packageName}"),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (matchingApp?.icon != null) {
                                        val bitmap = remember(profile.packageName) {
                                            try {
                                                matchingApp.icon.toBitmap(96, 96).asImageBitmap()
                                            } catch (_: Throwable) {
                                                null
                                            }
                                        }
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap,
                                                contentDescription = profile.appName,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(DarkSurfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Games, contentDescription = null, tint = NeonYellow)
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(DarkSurfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Games, contentDescription = null, tint = NeonYellow)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = profile.appName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = profile.packageName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextGray
                                        )
                                    }

                                    IconButton(onClick = { editingProfile = profile }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = TextGray)
                                    }
                                    IconButton(onClick = { viewModel.deleteProfile(profile) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Profile", tint = DangerRed)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Profile Specs Pills Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatusBadge(
                                        text = if (profile.targetFps == 0) "Auto FPS" else "${profile.targetFps} FPS Target",
                                        indicatorColor = NeonYellow
                                    )
                                    StatusBadge(
                                        text = if (profile.requestedRefreshRate == 0f) "Default Hz" else "${profile.requestedRefreshRate.toInt()} Hz Display",
                                        indicatorColor = CyberCyan
                                    )
                                    StatusBadge(
                                        text = if (profile.autoActivate) "Auto-Boost" else "Manual Only",
                                        indicatorColor = if (profile.autoActivate) SuccessGreen else TextGray
                                    )
                                }

                                if (profile.customNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Note: ${profile.customNotes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextGray
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.applyProfile(profile)
                                            val launchIntent = context.packageManager.getLaunchIntentForPackage(profile.packageName)
                                            if (launchIntent != null) {
                                                context.startActivity(launchIntent)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonYellow, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Boost & Launch", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.applyProfile(profile) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                    ) {
                                        Text("Apply Profile Only")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        AppPickerBottomSheet(
            apps = installedApps,
            isLoading = isLoadingApps,
            onAppSelected = { app ->
                showPicker = false
                val newProfile = GameProfile(
                    packageName = app.packageName,
                    appName = app.appName,
                    targetFps = deviceInfo.maxSupportedRefreshRate.toInt(),
                    requestedRefreshRate = deviceInfo.maxSupportedRefreshRate,
                    isGame = app.isGameCategory
                )
                editingProfile = newProfile
            },
            onDismiss = { showPicker = false }
        )
    }

    editingProfile?.let { profile ->
        ProfileEditorDialog(
            initialProfile = profile,
            deviceInfo = deviceInfo,
            onSave = { updated ->
                viewModel.saveProfile(updated)
                editingProfile = null
            },
            onDismiss = { editingProfile = null }
        )
    }
}
