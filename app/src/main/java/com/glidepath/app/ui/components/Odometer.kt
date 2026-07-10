package com.glidepath.app.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glidepath.app.domain.model.currencySymbol
import com.glidepath.app.domain.model.formatMoney
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide
import com.glidepath.app.ui.util.rememberReducedMotion

/**
 * The hero counter. Animates from its previous value to [amountPennies] over 900ms EaseOutCubic
 * (snaps when reduced motion is on). The currency symbol is smaller and accent-coloured; the
 * number uses Space Mono tabular figures so its width does not jitter as digits change.
 */
@Composable
fun Odometer(
    amountPennies: Long,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    val glide = LocalGlide.current
    val reduced = rememberReducedMotion()
    val animated by animateIntAsState(
        targetValue = amountPennies.toInt(),
        animationSpec = if (reduced) tween(0) else tween(durationMillis = 900, easing = GlideEaseOut),
        label = "odometer",
    )

    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        BasicText(
            text = currencySymbol(currencyCode),
            style = GlideType.heroCounter.copy(color = glide.accent, fontSize = 30.sp),
            modifier = Modifier.padding(top = 6.dp, end = 2.dp),
        )
        BasicText(
            text = formatMoney(animated.toLong(), currencyCode, withSymbol = false),
            style = GlideType.heroCounter.copy(color = glide.text, textAlign = TextAlign.Start),
        )
    }
}
