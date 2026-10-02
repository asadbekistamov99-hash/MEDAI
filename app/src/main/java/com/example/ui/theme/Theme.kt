package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Colour roles.
 *
 * `background` is the canvas behind every screen; `surface` is the card/panel colour that sits
 * on it. Keeping the delta between the two small (a hair off-white) is what makes the cards
 * read as elevation rather than as stickers pasted on a coloured page.
 */
private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal900,

    secondary = SecondaryGreen,
    onSecondary = Color.White,
    secondaryContainer = Teal50,
    onSecondaryContainer = Teal800,

    tertiary = AccentCyan,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF075985),

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),

    background = MedicalBackground,
    onBackground = TextPrimary,
    surface = MedicalCard,
    onSurface = TextPrimary,
    surfaceVariant = Teal50,
    onSurfaceVariant = TextSecondary,
    surfaceTint = PrimaryGreen,
    inverseSurface = Ink800,
    inverseOnSurface = Color(0xFFF1F5F9),

    outline = MedicalBorder,
    outlineVariant = DividerSoft,
    scrim = Color(0x990B1220),
)

/**
 * Kept for a future dark theme. Not wired in yet: the screens still hardcode
 * `TextPrimary`/`MedicalCard` in ~900 places, so flipping the scheme alone would produce dark
 * text on dark cards. Wiring dark mode properly means moving those to theme roles first.
 */
val MedAIDarkColorScheme = darkColorScheme(
    primary = Teal400,
    onPrimary = Teal950,
    primaryContainer = Teal900,
    onPrimaryContainer = Teal100,
    secondary = Teal400,
    onSecondary = Teal950,
    tertiary = AccentCyan,
    onTertiary = Color(0xFF082F49),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    background = Color(0xFF07110F),
    onBackground = Color(0xFFE2EDEA),
    surface = Color(0xFF0D1A18),
    onSurface = Color(0xFFE2EDEA),
    surfaceVariant = Color(0xFF14231F),
    onSurfaceVariant = Color(0xFF9FB3AD),
    outline = Color(0xFF24352F),
    outlineVariant = Color(0xFF1A2A25),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // light-only by design; see MedAIDarkColorScheme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = MedAIShapes,
        content = content,
    )
}
