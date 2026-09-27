package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GamingGauge(
    currentValue: Float,
    maxValue: Float,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
    size: Dp = 210.dp,
    highlightColor: Color = NeonYellow,
    subLabel: String? = null
) {
    val progress = (currentValue / maxValue.coerceAtLeast(1f)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 650),
        label = "gaugeProgress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.toPx() - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            val startAngle = 135f
            val sweepAngle = 270f

            // Background track
            drawArc(
                color = CardBorder,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active progress arc with gradient
            val activeSweep = sweepAngle * animatedProgress
            if (activeSweep > 0.5f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(CyberCyan, highlightColor, highlightColor),
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Outer decorative tick marks
            val centerOffset = center
            val radius = (size.toPx() / 2) - 4.dp.toPx()
            for (i in 0..12) {
                val tickAngle = Math.toRadians((startAngle + (sweepAngle * (i / 12f))).toDouble())
                val startX = (centerOffset.x + (radius - 8.dp.toPx()) * cos(tickAngle)).toFloat()
                val startY = (centerOffset.y + (radius - 8.dp.toPx()) * sin(tickAngle)).toFloat()
                val endX = (centerOffset.x + radius * cos(tickAngle)).toFloat()
                val endY = (centerOffset.y + radius * sin(tickAngle)).toFloat()

                val tickColor = if (i / 12f <= animatedProgress) highlightColor else CardBorder
                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Center Gauge Data
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextGray,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (currentValue >= 10f) currentValue.toInt().toString() else String.format("%.1f", currentValue),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                ),
                color = TextWhite
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = highlightColor
            )
            if (subLabel != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
    }
}
