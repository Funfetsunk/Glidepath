package com.glidepath.app.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.glidepath.app.domain.model.GoalType
import com.glidepath.app.ui.theme.LocalGlide
import com.glidepath.app.ui.util.rememberReducedMotion
import kotlin.math.atan2

/** EaseOutCubic used across Glidepath motion: cubic-bezier(.22,1,.36,1) (§6/§8). */
val GlideEaseOut = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

private val MilestoneStops = listOf(0.25f, 0.5f, 0.75f)

/**
 * The signature runway. A plane rides a curved glide slope, banking to the tangent, with
 * milestone dots at 25/50/75/100%. Debt descends toward touchdown; savings climbs to the target.
 * [progress] (0..1) animates over 900ms EaseOutCubic, or snaps when reduced motion is on.
 *
 * [pulseMilestone] (a t in 0..1) draws a pulsing ring at that milestone — used by the milestone
 * celebration. The pulse is static (a single soft ring) when reduced motion is on.
 */
@Composable
fun RunwayPath(
    type: GoalType,
    progress: Float,
    modifier: Modifier = Modifier,
    pulseMilestone: Float? = null,
) {
    val glide = LocalGlide.current
    val reduced = rememberReducedMotion()
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (reduced) tween(0) else tween(durationMillis = 900, easing = GlideEaseOut),
        label = "runwayProgress",
    )
    val pulse by if (pulseMilestone != null && !reduced) {
        rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1200, easing = GlideEaseOut), RepeatMode.Restart),
            label = "pulseRing",
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(modifier = modifier.fillMaxWidth().height(150.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            val path = curvePath(type, size.width, size.height)
            val measure = PathMeasure().apply { setPath(path, forceClosed = false) }
            val length = measure.length
            if (length <= 0f) return@Canvas

            // 1. Area fill under the curve down to the baseline.
            val fill = Path().apply {
                addPath(path)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(fill, glide.accentSoft)

            val strokeW = 3.dp.toPx()
            // 2. Full runway ahead.
            drawPath(path, glide.track2, style = Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round))

            // 3. Traveled portion.
            val traveled = Path()
            if (animated > 0f && measure.getSegment(0f, length * animated, traveled, true)) {
                drawPath(traveled, glide.accent, style = Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }

            // 4. Milestone dots.
            MilestoneStops.forEach { t ->
                val pos = measure.getPosition(length * t)
                val reached = animated >= t
                drawCircle(
                    color = if (reached) glide.accent else glide.track2,
                    radius = 4.dp.toPx(),
                    center = pos,
                )
            }
            // 100% marker: rounded square.
            val endPos = measure.getPosition(length)
            val half = 5.5.dp.toPx()
            drawRoundRect(
                color = if (animated >= 1f) glide.accent else glide.track2,
                topLeft = Offset(endPos.x - half, endPos.y - half),
                size = androidx.compose.ui.geometry.Size(half * 2, half * 2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            )

            // 6. Dashed ground line.
            drawGroundLine(glide.track2.copy(alpha = 0.5f))

            // Pulsing ring at the celebrated milestone.
            if (pulseMilestone != null) {
                val ringPos = measure.getPosition(length * pulseMilestone.coerceIn(0f, 1f))
                if (reduced) {
                    drawCircle(glide.accent.copy(alpha = 0.4f), radius = 12.dp.toPx(), center = ringPos, style = Stroke(width = 2.dp.toPx()))
                } else {
                    val ringRadius = (6.dp.toPx()) + pulse * 14.dp.toPx()
                    drawCircle(glide.accent.copy(alpha = (1f - pulse) * 0.6f), radius = ringRadius, center = ringPos, style = Stroke(width = 2.dp.toPx()))
                }
            }

            // 5. Plane marker, banked to the tangent.
            val planePos = measure.getPosition(length * animated)
            val tangent = measure.getTangent(length * animated)
            val angle = Math.toDegrees(atan2(tangent.y.toDouble(), tangent.x.toDouble())).toFloat()
            rotate(degrees = angle, pivot = planePos) {
                drawPlane(planePos, glide.accent, glide.bezel)
            }
        }
    }
}

/** Normalized cubic Bézier per §6, scaled to the canvas. */
private fun curvePath(type: GoalType, w: Float, h: Float): Path = Path().apply {
    if (type == GoalType.DEBT) {
        moveTo(0.02f * w, 0.147f * h)
        cubicTo(0.40f * w, 0.20f * h, 0.633f * w, 0.613f * h, 0.98f * w, 0.853f * h)
    } else {
        moveTo(0.02f * w, 0.853f * h)
        cubicTo(0.367f * w, 0.813f * h, 0.60f * w, 0.40f * h, 0.98f * w, 0.147f * h)
    }
}

private fun DrawScope.drawGroundLine(color: Color) {
    val y = size.height * (128f / 150f)
    val dash = 6.dp.toPx()
    var x = 0f
    while (x < size.width) {
        drawLine(color, Offset(x, y), Offset((x + dash).coerceAtMost(size.width), y), strokeWidth = 1.5.dp.toPx())
        x += dash * 2
    }
}

private fun DrawScope.drawPlane(center: Offset, fill: Color, shadow: Color) {
    val r = 7.dp.toPx()
    val tri = Path().apply {
        moveTo(center.x + r, center.y)
        lineTo(center.x - r * 0.8f, center.y - r * 0.7f)
        lineTo(center.x - r * 0.4f, center.y)
        lineTo(center.x - r * 0.8f, center.y + r * 0.7f)
        close()
    }
    drawPath(tri.apply { translate(Offset(1.dp.toPx(), 1.dp.toPx())) }, shadow.copy(alpha = 0.35f))
    val tri2 = Path().apply {
        moveTo(center.x + r, center.y)
        lineTo(center.x - r * 0.8f, center.y - r * 0.7f)
        lineTo(center.x - r * 0.4f, center.y)
        lineTo(center.x - r * 0.8f, center.y + r * 0.7f)
        close()
    }
    drawPath(tri2, fill)
}
