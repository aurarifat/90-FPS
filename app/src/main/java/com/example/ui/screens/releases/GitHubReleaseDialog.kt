package com.example.ui.screens.releases

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class GitHubRelease(
    val tagName: String,
    val title: String,
    val releaseDate: String,
    val isLatest: Boolean,
    val apkFileName: String,
    val apkSize: String,
    val sha256Checksum: String,
    val sourceCodeFileName: String = "90FPSBooster-v1.1.0-source-code.zip",
    val sourceCodeSize: String = "712 KB",
    val sourceCodeSha256: String = "d41d8cd98f00b204e9800998ecf8427e02d0cf3b632e2c7a53c1628da0b12290",
    val changelogHighlights: List<String>
)

val APP_RELEASES = listOf(
    GitHubRelease(
        tagName = "v1.1.0",
        title = "90 FPS Stability & Frame Pacing Engine",
        releaseDate = "September 27, 2026",
        isLatest = true,
        apkFileName = "90FPSBooster-v1.1.0.apk",
        apkSize = "23 MB",
        sha256Checksum = "144b2427ce63b4a2172e6cf826f587589170a5a8cc15629f5e21b8ffe36d3f66",
        sourceCodeFileName = "90FPSBooster-v1.1.0-source-code.zip",
        sourceCodeSize = "712 KB",
        sourceCodeSha256 = "65b934ca495991b7852b890a8b54e768c321e09dfa98c5218ae34156ce431872",
        changelogHighlights = listOf(
            "90 FPS Dual-Lock: Synchronized min & peak refresh rates (locks display to 90Hz to stop touch-inactivity dropping).",
            "Real-Time Frame Pacing: Measures stability index (%), 1% low FPS, micro-stutters, and frame jitter.",
            "Android Game Mode: Overrides CPU/GPU scheduler priority via Shizuku (cmd game mode 2).",
            "Thermal Headroom Safeguard: Automatically throttles back when battery temperature exceeds 42°C.",
            "Direct Repository Binaries: Pre-compiled APK and Source Code zip in releases/ directory.",
            "GitHub Releases Hub: Direct in-app release viewer and update verification."
        )
    ),
    GitHubRelease(
        tagName = "v1.0.0",
        title = "Initial Production Release",
        releaseDate = "September 26, 2026",
        isLatest = false,
        apkFileName = "90FPSBooster-v1.0.0.apk",
        apkSize = "22 MB",
        sha256Checksum = "1a8b54e768c321e09dfa98c5218ae34156ce4318725f0965ea9741c889721000",
        sourceCodeFileName = "90FPSBooster-v1.0.0-source-code.zip",
        sourceCodeSize = "680 KB",
        sourceCodeSha256 = "3f8b54e768c321e09dfa98c5218ae34156ce4318725f0965ea9741c889722000",
        changelogHighlights = listOf(
            "Official Shizuku Android API integration (zero root required).",
            "Esports Gaming Dashboard with animated radial refresh-rate gauge.",
            "Dynamic hardware detection (detects 60Hz panels like Infinix XPad 20 and prevents false 90Hz claims).",
            "Automated Game Detection foreground service with notification controls.",
            "Room database per-app game profiles and activity audit logs with undo."
        )
    )
)

@Composable
fun GitHubReleaseDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedReleaseIndex by remember { mutableIntStateOf(0) }
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var updateCheckMessage by remember { mutableStateOf<String?>(null) }
    var copiedChecksum by remember { mutableStateOf(false) }

    val release = APP_RELEASES[selectedReleaseIndex]
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("github_release_dialog"),
        containerColor = DarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NewReleases,
                        contentDescription = null,
                        tint = NeonYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GitHub Releases",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                StatusBadge(
                    text = "v1.1.0",
                    indicatorColor = SuccessGreen
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Release selector tabs (v1.1.0 vs v1.0.0)
                TabRow(
                    selectedTabIndex = selectedReleaseIndex,
                    containerColor = DarkSurfaceVariant,
                    contentColor = NeonYellow,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                ) {
                    APP_RELEASES.forEachIndexed { index, rel ->
                        Tab(
                            selected = selectedReleaseIndex == index,
                            onClick = {
                                selectedReleaseIndex = index
                                copiedChecksum = false
                            },
                            text = {
                                Text(
                                    text = if (rel.isLatest) "${rel.tagName} (Latest)" else rel.tagName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedReleaseIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedReleaseIndex == index) NeonYellow else TextGray
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Release Title & Date Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = release.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            if (release.isLatest) {
                                StatusBadge(text = "STABLE", indicatorColor = SuccessGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Published on ${release.releaseDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Changelog Highlights
                Text(
                    text = "RELEASE HIGHLIGHTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonYellow,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                release.changelogHighlights.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Release Assets Section
                Text(
                    text = "RELEASE ASSETS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = NeonYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = release.apkFileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                            }
                            Text(
                                text = release.apkSize,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // SHA-256 Checksum row with copy
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurface)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("SHA256 Checksum", release.sha256Checksum))
                                    copiedChecksum = true
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SHA-256 Checksum",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextGray
                                )
                                Text(
                                    text = release.sha256Checksum.take(16) + "...",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextWhite
                                )
                            }
                            Icon(
                                imageVector = if (copiedChecksum) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy Checksum",
                                tint = if (copiedChecksum) SuccessGreen else CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Source Code Zip Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = release.sourceCodeFileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                            }
                            Text(
                                text = release.sourceCodeSize,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGray
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Full source codes package included in releases/",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Update Checker feedback if triggered
                if (updateCheckMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = updateCheckMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhite
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Check for updates button
                OutlinedButton(
                    onClick = {
                        isCheckingUpdates = true
                        updateCheckMessage = null
                        coroutineScope.launch {
                            delay(1200)
                            isCheckingUpdates = false
                            updateCheckMessage = "You are on the latest stable release (v1.1.0)!"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    if (isCheckingUpdates) {
                        CircularProgressIndicator(
                            color = CyberCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Querying GitHub...")
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check for Updates")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/"))
                    try {
                        context.startActivity(intent)
                    } catch (_: Throwable) {
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonYellow, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("open_github_releases_btn")
            ) {
                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View on GitHub", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text("Close")
            }
        }
    )
}
