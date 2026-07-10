package com.glidepath.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.glidepath.app.domain.model.GoalType
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide
import com.glidepath.app.ui.util.rememberReducedMotion

/** Warm one-line copy per milestone (§7.5). */
private fun milestoneCopy(percent: Int): Pair<String, String> = when (percent) {
    25 -> "A quarter of the way" to "The hardest part is starting, and you already have."
    50 -> "Halfway there" to "The far end is closer than the start now."
    75 -> "Three quarters down" to "Home stretch. You can see the runway from here."
    else -> "So close" to "Keep going."
}

/**
 * Full-screen milestone celebration (§7.5). One restrained moment: a mini runway settled past
 * the marker, the big percent, a warm line, and a dismiss button. Pops in unless reduced motion.
 */
@Composable
fun MilestoneOverlay(percent: Int, type: GoalType, onDismiss: () -> Unit) {
    val glide = LocalGlide.current
    val reduced = rememberReducedMotion()
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = if (reduced) spring() else spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "milestonePop",
    )
    val (title, line) = milestoneCopy(percent)

    val milestoneT = percent / 100f
    // The plane starts just short of the marker and glides past it.
    var planeProgress by remember { mutableStateOf(if (reduced) milestoneT else (milestoneT - 0.12f).coerceAtLeast(0f)) }
    LaunchedEffect(percent) {
        if (!reduced) planeProgress = (milestoneT + 0.05f).coerceAtMost(1f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(glide.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 40.dp)
            .scale(if (reduced) 1f else scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        RunwayPath(type = type, progress = planeProgress, pulseMilestone = milestoneT)
        Spacer(Modifier.height(28.dp))
        Text("$percent%", style = GlideType.milestonePercent.copy(color = glide.accent))
        Spacer(Modifier.height(12.dp))
        Text(title, style = GlideType.bigTitle.copy(color = glide.text), textAlign = TextAlign.Center)
        Text(
            line,
            style = GlideType.body.copy(color = glide.muted),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(Modifier.height(32.dp))
        PrimaryButton("Keep going", onDismiss, modifier = Modifier.fillMaxWidth())
    }
}
