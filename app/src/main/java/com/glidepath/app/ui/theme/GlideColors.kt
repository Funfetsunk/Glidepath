package com.glidepath.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colour roles for Glidepath. We model roles (bg/surface/accent/track…) rather than
 * raw Material slots so the six palettes can be swapped wholesale via [LocalGlide].
 *
 * @property accentSoft The accent at low alpha, used for chips and area fills.
 */
@Immutable
data class GlideColors(
    val bg: Color,
    val surface: Color,
    val line: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val track: Color,
    val track2: Color,
    val accentSoft: Color,
    val bezel: Color,
    val isDark: Boolean,
)

/** The selectable colour themes. Order drives the Appearance grid. */
enum class GlidePalette(val displayName: String, val descriptor: String) {
    PINEWOOD("Pinewood", "Green + gold"),
    HARBOR("Harbor", "Navy + amber"),
    OXBLOOD("Oxblood", "Wine + brass"),
    DEEPSEA("Deepsea", "Teal + coral"),
    MOSS("Moss", "Olive + clay"),
    SLATE("Slate", "Graphite + sky"),
}

/** Light / Dark / follow-system. */
enum class GlideMode { LIGHT, DARK, SYSTEM }

private fun hex(value: Long) = Color(value or 0xFF000000L)

/** accentSoft alpha: ~16% on dark, ~12% on light (per design spec §4). */
private fun softAccent(accent: Color, isDark: Boolean): Color =
    accent.copy(alpha = if (isDark) 0x29 / 255f else 0x1F / 255f)

/** Resolves the concrete [GlideColors] for a palette in a given mode. */
fun glideColorsOf(palette: GlidePalette, dark: Boolean): GlideColors =
    if (dark) palette.dark() else palette.light()

private fun GlidePalette.dark(): GlideColors = when (this) {
    GlidePalette.PINEWOOD -> darkColors(
        bg = 0x0D201B, surface = 0x16302A, line = 0x274B42, text = 0xEEF4F1, muted = 0x93B0A6,
        accent = 0xE6B455, onAccent = 0x1C2B12, track = 0x26463D, track2 = 0x3A5B52, bezel = 0x050F0C,
    )
    GlidePalette.HARBOR -> darkColors(
        bg = 0x0F1826, surface = 0x182437, line = 0x26344A, text = 0xEEF2F8, muted = 0x93A4BD,
        accent = 0xE8B24A, onAccent = 0x20180A, track = 0x26344A, track2 = 0x3A4A63, bezel = 0x070D16,
    )
    GlidePalette.OXBLOOD -> darkColors(
        bg = 0x221116, surface = 0x31191F, line = 0x472530, text = 0xF6ECEF, muted = 0xC69BA7,
        accent = 0xE0A86A, onAccent = 0x2A1A0C, track = 0x3D2029, track2 = 0x5A2F3B, bezel = 0x160A0E,
    )
    GlidePalette.DEEPSEA -> darkColors(
        bg = 0x0C1F20, surface = 0x123032, line = 0x204648, text = 0xE9F4F2, muted = 0x8FB3B0,
        accent = 0xF08D6B, onAccent = 0x2A140C, track = 0x204648, track2 = 0x315E60, bezel = 0x061313,
    )
    GlidePalette.MOSS -> darkColors(
        bg = 0x1A1D12, surface = 0x272B1A, line = 0x3C4229, text = 0xF0F2E6, muted = 0xA8AC92,
        accent = 0xE9A86A, onAccent = 0x2A1C0C, track = 0x353A24, track2 = 0x4C5334, bezel = 0x0E100A,
    )
    GlidePalette.SLATE -> darkColors(
        bg = 0x14171C, surface = 0x1E232B, line = 0x2F3742, text = 0xEEF1F5, muted = 0x98A2B0,
        accent = 0x6FB2E0, onAccent = 0x0A1A26, track = 0x2B333D, track2 = 0x414B58, bezel = 0x0A0D11,
    )
}

private fun GlidePalette.light(): GlideColors = when (this) {
    GlidePalette.PINEWOOD -> lightColors(
        bg = 0xE9F1ED, surface = 0xFFFFFF, line = 0xD7E4DE, text = 0x14231E, muted = 0x5A726B,
        accent = 0x9C6F1C, onAccent = 0xFFFFFF, track = 0xDCE8E2, track2 = 0xC3D4CC, bezel = 0x20302B,
    )
    GlidePalette.HARBOR -> lightColors(
        bg = 0xEEF1F6, surface = 0xFFFFFF, line = 0xD6DDE8, text = 0x141B26, muted = 0x5B6678,
        accent = 0xA9781C, onAccent = 0xFFFFFF, track = 0xDCE2EC, track2 = 0xC3CCDB, bezel = 0x1C2636,
    )
    GlidePalette.OXBLOOD -> lightColors(
        bg = 0xF6EEF0, surface = 0xFFFFFF, line = 0xECD9DE, text = 0x2A141B, muted = 0x8A6470,
        accent = 0xA86A2C, onAccent = 0xFFFFFF, track = 0xEEDDE2, track2 = 0xDCC3CA, bezel = 0x2A141B,
    )
    GlidePalette.DEEPSEA -> lightColors(
        bg = 0xE9F2F1, surface = 0xFFFFFF, line = 0xD3E4E2, text = 0x0F2422, muted = 0x547370,
        accent = 0xCF6A44, onAccent = 0xFFFFFF, track = 0xDBE9E7, track2 = 0xC0D6D3, bezel = 0x12302E,
    )
    GlidePalette.MOSS -> lightColors(
        bg = 0xF1F2E8, surface = 0xFFFFFF, line = 0xDFE1CF, text = 0x1E2114, muted = 0x666B52,
        accent = 0xA67A34, onAccent = 0xFFFFFF, track = 0xE6E8D6, track2 = 0xD0D3BC, bezel = 0x22261A,
    )
    GlidePalette.SLATE -> lightColors(
        bg = 0xEEF1F5, surface = 0xFFFFFF, line = 0xD8DEE6, text = 0x171B21, muted = 0x5C6672,
        accent = 0x2F7FB8, onAccent = 0xFFFFFF, track = 0xDDE3EA, track2 = 0xC5CDD7, bezel = 0x1A1F26,
    )
}

private fun darkColors(
    bg: Long, surface: Long, line: Long, text: Long, muted: Long,
    accent: Long, onAccent: Long, track: Long, track2: Long, bezel: Long,
): GlideColors {
    val accentColor = hex(accent)
    return GlideColors(
        bg = hex(bg), surface = hex(surface), line = hex(line), text = hex(text), muted = hex(muted),
        accent = accentColor, onAccent = hex(onAccent), track = hex(track), track2 = hex(track2),
        accentSoft = softAccent(accentColor, isDark = true), bezel = hex(bezel), isDark = true,
    )
}

private fun lightColors(
    bg: Long, surface: Long, line: Long, text: Long, muted: Long,
    accent: Long, onAccent: Long, track: Long, track2: Long, bezel: Long,
): GlideColors {
    val accentColor = hex(accent)
    return GlideColors(
        bg = hex(bg), surface = hex(surface), line = hex(line), text = hex(text), muted = hex(muted),
        accent = accentColor, onAccent = hex(onAccent), track = hex(track), track2 = hex(track2),
        accentSoft = softAccent(accentColor, isDark = false), bezel = hex(bezel), isDark = false,
    )
}

/** Provides the current [GlideColors] to the composable tree. Default = Pinewood dark. */
val LocalGlide = staticCompositionLocalOf { glideColorsOf(GlidePalette.PINEWOOD, dark = true) }
