package com.glidepath.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Root theme. Resolves [palette] + [mode] to a concrete [GlideColors], exposes it via
 * [LocalGlide], and mirrors it into a Material 3 scheme so any stray M3 component stays on-brand.
 * Dynamic Color / Material You is intentionally NOT used — our palettes always win.
 */
@Composable
fun GlideTheme(
    palette: GlidePalette = GlidePalette.PINEWOOD,
    mode: GlideMode = GlideMode.DARK,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        GlideMode.LIGHT -> false
        GlideMode.DARK -> true
        GlideMode.SYSTEM -> isSystemInDarkTheme()
    }
    val glide = glideColorsOf(palette, dark)

    val scheme = if (dark) {
        darkColorScheme(
            primary = glide.accent,
            onPrimary = glide.onAccent,
            background = glide.bg,
            onBackground = glide.text,
            surface = glide.surface,
            onSurface = glide.text,
            surfaceVariant = glide.track,
            onSurfaceVariant = glide.muted,
            outline = glide.line,
        )
    } else {
        lightColorScheme(
            primary = glide.accent,
            onPrimary = glide.onAccent,
            background = glide.bg,
            onBackground = glide.text,
            surface = glide.surface,
            onSurface = glide.text,
            surfaceVariant = glide.track,
            onSurfaceVariant = glide.muted,
            outline = glide.line,
        )
    }

    CompositionLocalProvider(LocalGlide provides glide) {
        MaterialTheme(
            colorScheme = scheme,
            typography = GlideTypography,
            content = content,
        )
    }
}
