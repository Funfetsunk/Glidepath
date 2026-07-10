package com.glidepath.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glidepath.app.ui.theme.GlideShapes
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide

/** Uppercase muted section label (§5). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    val glide = LocalGlide.current
    Text(
        text = text.uppercase(),
        style = GlideType.sectionLabel.copy(color = glide.muted),
        modifier = modifier,
    )
}

/** Full-width primary action button (§7): accent bg, onAccent text, radius 18, dims when disabled. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val glide = LocalGlide.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(GlideShapes.button)
            .alpha(if (enabled) 1f else 0.5f)
            .background(glide.accent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 17.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = GlideType.body.copy(color = glide.onAccent),
            fontWeight = androidx.compose.ui.text.font.FontWeight.W700,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
    }
}

/** A surface card with the standard 16dp radius and padding. */
@Composable
fun GlideCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val glide = LocalGlide.current
    Column(
        modifier = modifier
            .clip(GlideShapes.card)
            .background(glide.surface)
            .border(BorderStroke(1.dp, glide.line), GlideShapes.card)
            .padding(padding),
        content = content,
    )
}

/** A labelled stat tile: small muted label over a value line. */
@Composable
fun StatCard(
    label: String,
    value: String,
    secondary: String? = null,
    modifier: Modifier = Modifier,
) {
    val glide = LocalGlide.current
    GlideCard(modifier = modifier) {
        SectionLabel(label)
        Text(
            text = value,
            style = GlideType.body.copy(color = glide.text),
            fontWeight = androidx.compose.ui.text.font.FontWeight.W700,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (secondary != null) {
            Text(
                text = secondary,
                style = GlideType.secondary.copy(color = glide.muted),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** Small accent-tinted chip, e.g. the DEBT/SAVINGS pill. */
@Composable
fun AccentChip(text: String, modifier: Modifier = Modifier) {
    val glide = LocalGlide.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(glide.accentSoft)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text = text, style = GlideType.caption.copy(color = glide.accent))
    }
}
