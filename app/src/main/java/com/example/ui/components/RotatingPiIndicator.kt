package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MonospaceFont
import com.example.ui.theme.NeonCyan

@Composable
fun RotatingPiIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    color: Color = NeonCyan,
    pulseSpeedMillis: Int = 2400
) {
    val transition = rememberInfiniteTransition(label = "pi_rotation")

    // Outer clockwise rotation
    val outerAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseSpeedMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_angle"
    )

    // Inner counter-clockwise rotation
    val innerAngle by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (pulseSpeedMillis * 0.75f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_angle"
    )

    // Pulsing alpha for the Pi glyph
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (pulseSpeedMillis * 0.5f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pi_alpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = (this.size.minDimension / 2f) - 6f
            val innerRadius = outerRadius * 0.72f

            // Outer dashed cyber ring
            rotate(outerAngle, pivot = center) {
                drawCircle(
                    color = color.copy(alpha = 0.25f),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 18f, 10f, 18f), 0f)
                    )
                )
                // 4 Reticle tick corner marks
                val tickLength = 12f
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - outerRadius - tickLength),
                    end = Offset(center.x, center.y - outerRadius + tickLength),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y + outerRadius - tickLength),
                    end = Offset(center.x, center.y + outerRadius + tickLength),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }

            // Inner cyber ring
            rotate(innerAngle, pivot = center) {
                drawCircle(
                    color = color.copy(alpha = 0.5f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(
                        width = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                )
            }
        }

        // Center futuristic Mathematical Pi (π) Symbol
        Text(
            text = "π",
            color = color.copy(alpha = pulseAlpha),
            fontFamily = MonospaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.38f).sp
        )
    }
}
