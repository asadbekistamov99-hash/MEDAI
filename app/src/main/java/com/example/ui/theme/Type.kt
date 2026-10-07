package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Type scale.
 *
 * Two rules that separate a professional app from a default-Material one:
 *  - negative letter-spacing on large text, positive on small caps labels, so headings feel
 *    tight and eyebrows feel deliberate;
 *  - line heights ~1.3–1.5x the font size, because Uzbek Cyrillic-free Latin text at 14sp
 *    with Material's 1.2x default reads cramped on a phone.
 *
 * `FontFamily.Default` resolves to Roboto on Android, which is what we want — shipping a
 * display font would mean adding ~200KB of binaries for very little gain in a health app.
 */
private val TrimmedLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun style(
    size: Int,
    line: Int,
    weight: FontWeight,
    tracking: Double = 0.0,
) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
    lineHeightStyle = TrimmedLineHeight,
)

val Typography = Typography(
    displayLarge = style(34, 42, FontWeight.Bold, -0.5),
    displayMedium = style(30, 38, FontWeight.Bold, -0.4),
    displaySmall = style(26, 34, FontWeight.Bold, -0.3),

    headlineLarge = style(28, 36, FontWeight.Bold, -0.4),
    headlineMedium = style(23, 30, FontWeight.Bold, -0.25),
    headlineSmall = style(20, 27, FontWeight.SemiBold, -0.2),

    titleLarge = style(19, 26, FontWeight.SemiBold, -0.1),
    titleMedium = style(16, 22, FontWeight.SemiBold, 0.0),
    titleSmall = style(14, 20, FontWeight.Medium, 0.1),

    bodyLarge = style(16, 24, FontWeight.Normal, 0.1),
    bodyMedium = style(14, 21, FontWeight.Normal, 0.1),
    bodySmall = style(12, 18, FontWeight.Normal, 0.15),

    labelLarge = style(14, 20, FontWeight.SemiBold, 0.2),
    labelMedium = style(12, 16, FontWeight.SemiBold, 0.3),
    labelSmall = style(12, 16, FontWeight.Medium, 0.4),
)

/** Styles that Material's scale has no slot for. */
object MedAIText {
    /** Big number in a summary card ("82"). */
    val MetricLarge = style(48, 52, FontWeight.Bold, -1.0)
    val MetricMedium = style(32, 36, FontWeight.ExtraBold, -0.5)
    val MetricSmall = style(16, 22, FontWeight.Bold, 0.0)
    /** Section eyebrow: small, tracked, always on a surface that gives it 4.5:1. */
    val Eyebrow = style(12, 16, FontWeight.SemiBold, 0.8)
}
