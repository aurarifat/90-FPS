package com.example.ui.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@Composable
fun WirelessDebuggingGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("wireless_debugging_dialog"),
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DeveloperMode,
                    contentDescription = null,
                    tint = NeonYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Shizuku Wireless Debugging",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
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
                    text = "Follow these official steps to start Shizuku without a PC or root access (Android 11+):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                GuideStepItem(
                    stepNumber = "1",
                    title = "Enable Developer Options",
                    description = "Go to Android Settings > About Phone, and tap 'Build Number' 7 times until Developer Mode is unlocked."
                )

                Spacer(modifier = Modifier.height(10.dp))

                GuideStepItem(
                    stepNumber = "2",
                    title = "Connect to Wi-Fi",
                    description = "Wireless Debugging requires an active Wi-Fi network connection."
                )

                Spacer(modifier = Modifier.height(10.dp))

                GuideStepItem(
                    stepNumber = "3",
                    title = "Enable Wireless Debugging",
                    description = "In Developer Options, toggle ON 'Wireless Debugging'. Tap into it and select 'Pair device with pairing code'."
                )

                Spacer(modifier = Modifier.height(10.dp))

                GuideStepItem(
                    stepNumber = "4",
                    title = "Pair & Start in Shizuku",
                    description = "Enter the 6-digit pairing code into the Shizuku pairing notification or app, then tap 'Start' inside Shizuku."
                )

                Spacer(modifier = Modifier.height(10.dp))

                GuideStepItem(
                    stepNumber = "5",
                    title = "Grant Shizuku in 90 FPS Booster",
                    description = "Return here and tap 'Authorize Shizuku' to allow safe display refresh rate modifications."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Shortcut button to open developer options
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Throwable) {
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_developer_options_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = NeonYellow
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Android Developer Options", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonYellow, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Got It", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun GuideStepItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(NeonYellow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
    }
}
