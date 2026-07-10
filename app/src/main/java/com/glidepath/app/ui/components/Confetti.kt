package com.glidepath.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.glidepath.app.ui.theme.LocalGlide
import com.glidepath.app.ui.util.rememberReducedMotion
import kotlin.random.Random

private data class ConfettiPiece(val xFraction: Float, val phase: Float, val useAccent: Boolean, val widthDp: Float, val spins: Float)

/**
 * Restrained falling confetti for the goal-complete moment (§7.6): a handful of accent/track2
 * pieces drifting down. Renders nothing when reduced motion is on.
 */
@Composable
fun Confetti(pieceCount: Int = 14, modifier: Modifier = Modifier) {
    if (rememberReducedMotion()) return
    val glide = LocalGlide.current
    val pieces = remember {
        List(pieceCount) {
            ConfettiPiece(
                xFraction = Random.nextFloat(),
                phase = Random.nextFloat(),
                useAccent = Random.nextBoolean(),
                widthDp = 6f + Random.nextFloat() * 5f,
                spins = 1f + Random.nextFloat() * 2f,
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "fall",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        pieces.forEach { piece ->
            val progress = (t + piece.phase) % 1f
            val x = piece.xFraction * size.width
            val y = progress * (size.height + 40f) - 20f
            val w = piece.widthDp.dp.toPx()
            val h = w * 0.5f
            val color = if (piece.useAccent) glide.accent else glide.track2
            val alpha = (1f - progress).coerceIn(0f, 1f)
            rotate(degrees = progress * 360f * piece.spins, pivot = Offset(x, y)) {
                drawRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(x - w / 2, y - h / 2),
                    size = Size(w, h),
                )
            }
        }
    }
}
