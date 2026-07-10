package com.glidepath.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.glidepath.app.ui.theme.GlideShapes
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide

/**
 * Shared 3×4 number keypad: 1–9, blank, 0, ⌫ (§7.3). Keys are surface tiles with mono digits.
 * [onDigit] receives '0'..'9'; [onBackspace] removes the last entered digit.
 */
@Composable
fun NumberKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫"),
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Key(label = key, modifier = Modifier.weight(1f)) {
                        when (key) {
                            "" -> Unit
                            "⌫" -> onBackspace()
                            else -> onDigit(key[0])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Key(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val glide = LocalGlide.current
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(GlideShapes.tile)
            .background(if (label.isEmpty()) Color.Transparent else glide.surface)
            .clickable(enabled = label.isNotEmpty(), onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (label.isNotEmpty()) Text(label, style = GlideType.mono.copy(color = glide.text))
    }
}
