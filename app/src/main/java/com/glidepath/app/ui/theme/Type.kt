package com.glidepath.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.glidepath.app.R

/** Hanken Grotesk (variable weight axis) — all UI text. */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun hanken(weight: Int) = Font(
    resId = R.font.hanken_grotesk_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val HankenGrotesk = FontFamily(
    hanken(400), hanken(500), hanken(600), hanken(700), hanken(800),
)

/** Space Mono — every number. Weight 700 with tabular figures for a non-jittering counter. */
val SpaceMono = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.W400),
    Font(R.font.space_mono_bold, FontWeight.W700),
)

/**
 * Glidepath type scale (§5). Mono styles are for numbers only. The hero counter uses a tight
 * negative tracking and [TextMotion.Animated] so digit width does not jitter as it counts.
 */
object GlideType {
    val heroCounter = TextStyle(
        fontFamily = SpaceMono, fontWeight = FontWeight.W700, fontSize = 54.sp,
        letterSpacing = (-0.04).em, textMotion = TextMotion.Animated,
    )
    val screenTitle = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.W700, fontSize = 20.sp,
        letterSpacing = (-0.5).sp,
    )
    val bigTitle = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.W800, fontSize = 24.sp,
        letterSpacing = (-0.5).sp,
    )
    val sectionLabel = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.W700, fontSize = 12.sp,
        letterSpacing = 1.sp,
    )
    val body = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.W400, fontSize = 15.sp,
    )
    val secondary = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.W400, fontSize = 13.sp,
    )
    val caption = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.W500, fontSize = 12.sp,
    )
    val mono = TextStyle(
        fontFamily = SpaceMono, fontWeight = FontWeight.W700, fontSize = 22.sp,
        letterSpacing = (-0.02).em,
    )
    val milestonePercent = TextStyle(
        fontFamily = SpaceMono, fontWeight = FontWeight.W700, fontSize = 64.sp,
        letterSpacing = (-0.04).em,
    )
    val onboardingHeadline = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.W800, fontSize = 26.sp,
        letterSpacing = (-0.5).sp,
    )
}

/** Material typography backed by Hanken, so stray M3 components still match. */
val GlideTypography = Typography(
    displayLarge = GlideType.bigTitle,
    titleLarge = GlideType.screenTitle,
    bodyLarge = GlideType.body,
    bodyMedium = GlideType.secondary,
    labelSmall = GlideType.sectionLabel,
).let { base ->
    base.copy(
        headlineLarge = base.headlineLarge.copy(fontFamily = HankenGrotesk),
        headlineMedium = base.headlineMedium.copy(fontFamily = HankenGrotesk),
        titleMedium = base.titleMedium.copy(fontFamily = HankenGrotesk),
        labelLarge = base.labelLarge.copy(fontFamily = HankenGrotesk, fontWeight = FontWeight.W700),
    )
}
