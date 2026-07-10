package com.glidepath.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.glidepath.app.ui.theme.GlideMode
import com.glidepath.app.ui.theme.GlidePalette
import com.glidepath.app.ui.theme.GlideShapes
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide
import com.glidepath.app.ui.theme.glideColorsOf

/** Appearance (§7.8): mode segmented control + 6-palette grid with a live swatch preview. */
@Composable
fun AppearanceScreen(
    palette: GlidePalette,
    mode: GlideMode,
    onSelect: (GlidePalette, GlideMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glide = LocalGlide.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(glide.bg)
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "‹",
                style = GlideType.bigTitle.copy(color = glide.text),
                modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp),
            )
            Text("Appearance", style = GlideType.bigTitle.copy(color = glide.text))
        }
        Spacer(Modifier.height(20.dp))

        // Mode segmented control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(GlideShapes.chip)
                .background(glide.surface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            GlideMode.entries.forEach { m ->
                val selected = m == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) glide.accent else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onSelect(palette, m) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        m.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = GlideType.secondary.copy(color = if (selected) glide.onAccent else glide.muted),
                        fontWeight = FontWeight.W700,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(GlidePalette.entries) { p ->
                PaletteTile(p, selected = p == palette, currentMode = mode) { onSelect(p, mode) }
            }
        }
    }
}

@Composable
private fun PaletteTile(palette: GlidePalette, selected: Boolean, currentMode: GlideMode, onClick: () -> Unit) {
    val glide = LocalGlide.current
    // Preview each palette in dark unless the app is explicitly in light mode.
    val previewDark = currentMode != GlideMode.LIGHT
    val swatch = glideColorsOf(palette, previewDark)
    Column(
        modifier = Modifier
            .clip(GlideShapes.card)
            .background(glide.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) glide.accent else glide.line,
                shape = GlideShapes.card,
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        // Mini swatch: palette bg field with accent + surface dots.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(swatch.bg),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(20.dp).clip(CircleShape).background(swatch.accent))
                Box(Modifier.size(20.dp).clip(CircleShape).background(swatch.surface))
                Box(Modifier.size(20.dp).clip(CircleShape).background(swatch.track2))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(palette.displayName, style = GlideType.body.copy(color = glide.text), fontWeight = FontWeight.W700)
                Text(palette.descriptor, style = GlideType.caption.copy(color = glide.muted))
            }
            if (selected) Text("✓", style = GlideType.body.copy(color = glide.accent))
        }
    }
}
