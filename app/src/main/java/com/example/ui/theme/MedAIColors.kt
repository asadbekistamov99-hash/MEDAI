package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** A tinted background with the foreground that is guaranteed readable on it. */
data class MedAITint(val bg: Color, val fg: Color)

/**
 * Semantic colour roles.
 *
 * Screens and components ask for a ROLE ("the secondary text colour", "the danger fill"), never
 * a palette step, so one definition per theme is enough to switch the whole app. Every
 * foreground/background pair below is asserted against WCAG AA in `MedAIColorsContrastTest`:
 * 4.5:1 for text, 3:1 for the outline of an input control.
 *
 * The legacy constants in Color.kt (PrimaryGreen, TextPrimary, ...) are kept for the screens
 * that have not been migrated yet. They hold the LIGHT values only.
 */
data class MedAIColors(
    val isDark: Boolean,
    // surfaces
    val canvas: Color,
    val surface: Color,
    val surfaceSunken: Color,
    val surfaceRaised: Color,
    val border: Color,
    val borderStrong: Color,
    val divider: Color,
    // text
    val textPrimary: Color,
    val textSecondary: Color,
    // brand
    val brand: Color,
    val onBrand: Color,
    val brandStrong: Color,
    val brandSoft: Color,
    val onBrandSoft: Color,
    // hero (gradient, white-on-it text)
    val heroStart: Color,
    val heroEnd: Color,
    val onHero: Color,
    val onHeroMuted: Color,
    // paid state only
    val premium: Color,
    val onPremium: Color,
    val premiumSoft: Color,
    val onPremiumSoft: Color,
    // status
    val success: Color, val successSoft: Color, val onSuccessSoft: Color,
    val warning: Color, val warningSoft: Color, val onWarningSoft: Color,
    val danger: Color, val onDanger: Color, val dangerSoft: Color, val onDangerSoft: Color,
    val info: Color, val infoSoft: Color, val onInfoSoft: Color,
    // category tints for tiles / icon discs
    val tintTeal: MedAITint,
    val tintPeach: MedAITint,
    val tintSky: MedAITint,
    val tintViolet: MedAITint,
    val scrim: Color,
) {
    val heroBrush: Brush get() = Brush.linearGradient(listOf(heroStart, heroEnd))
}

val MedAILightColors = MedAIColors(
    isDark = false,
    canvas = Color(0xFFF4F8F8),
    surface = Color(0xFFFFFFFF),
    surfaceSunken = Color(0xFFEEF4F3),
    surfaceRaised = Color(0xFFFFFFFF),
    border = Color(0xFFE1EAE8),
    borderStrong = Color(0xFF7B8A9A),
    divider = Color(0xFFEDF3F2),
    textPrimary = Color(0xFF0B1220),
    textSecondary = Color(0xFF526175),
    brand = Color(0xFF0A7568),
    onBrand = Color(0xFFFFFFFF),
    brandStrong = Color(0xFF075E55),
    brandSoft = Color(0xFFE0F2EF),
    onBrandSoft = Color(0xFF075E55),
    heroStart = Color(0xFF0F766E),
    heroEnd = Color(0xFF0A5A53),
    onHero = Color(0xFFFFFFFF),
    onHeroMuted = Color(0xFFE6F4F2),
    premium = Color(0xFF6D28D9),
    onPremium = Color(0xFFFFFFFF),
    premiumSoft = Color(0xFFF5F3FF),
    onPremiumSoft = Color(0xFF4C1D95),
    success = Color(0xFF047857), successSoft = Color(0xFFD1FAE5), onSuccessSoft = Color(0xFF065F46),
    warning = Color(0xFFB45309), warningSoft = Color(0xFFFEF3C7), onWarningSoft = Color(0xFF92400E),
    danger = Color(0xFFC62828), onDanger = Color(0xFFFFFFFF), dangerSoft = Color(0xFFFEE2E2), onDangerSoft = Color(0xFF991B1B),
    info = Color(0xFF0369A1), infoSoft = Color(0xFFE0F2FE), onInfoSoft = Color(0xFF075985),
    tintTeal = MedAITint(Color(0xFFD7F3EC), Color(0xFF064E46)),
    tintPeach = MedAITint(Color(0xFFFFE9D6), Color(0xFF7C2D12)),
    tintSky = MedAITint(Color(0xFFDDEBFF), Color(0xFF1E3A8A)),
    tintViolet = MedAITint(Color(0xFFEDE9FE), Color(0xFF4C1D95)),
    scrim = Color(0x990B1220),
)

val MedAIDarkColors = MedAIColors(
    isDark = true,
    canvas = Color(0xFF0A1412),
    surface = Color(0xFF111E1C),
    surfaceSunken = Color(0xFF0D1917),
    surfaceRaised = Color(0xFF172826),
    border = Color(0xFF22332F),
    borderStrong = Color(0xFF6A807A),
    divider = Color(0xFF1B2B28),
    textPrimary = Color(0xFFE6EFEC),
    textSecondary = Color(0xFF9FB3AD),
    brand = Color(0xFF2DD4BF),
    onBrand = Color(0xFF04302C),
    brandStrong = Color(0xFF5EEAD4),
    brandSoft = Color(0xFF163A35),
    onBrandSoft = Color(0xFF99F6E4),
    heroStart = Color(0xFF0F766E),
    heroEnd = Color(0xFF083D38),
    onHero = Color(0xFFFFFFFF),
    onHeroMuted = Color(0xFFE6F4F2),
    premium = Color(0xFFA78BFA),
    onPremium = Color(0xFF1E0A4A),
    premiumSoft = Color(0xFF2A2050),
    onPremiumSoft = Color(0xFFDDD6FE),
    success = Color(0xFF34D399), successSoft = Color(0xFF0F3A2C), onSuccessSoft = Color(0xFFA7F3D0),
    warning = Color(0xFFFBBF24), warningSoft = Color(0xFF3B2A0A), onWarningSoft = Color(0xFFFDE68A),
    danger = Color(0xFFF87171), onDanger = Color(0xFF450A0A), dangerSoft = Color(0xFF3B1515), onDangerSoft = Color(0xFFFCA5A5),
    info = Color(0xFF38BDF8), infoSoft = Color(0xFF0C2D44), onInfoSoft = Color(0xFFBAE6FD),
    tintTeal = MedAITint(Color(0xFF15332E), Color(0xFF99F6E4)),
    tintPeach = MedAITint(Color(0xFF3A2414), Color(0xFFFDBA74)),
    tintSky = MedAITint(Color(0xFF14284A), Color(0xFFBFDBFE)),
    tintViolet = MedAITint(Color(0xFF2A2050), Color(0xFFDDD6FE)),
    scrim = Color(0xB3000000),
)

internal val LocalMedAIColors = staticCompositionLocalOf { MedAILightColors }

/** Entry point for components: `MedAITheme.colors.textSecondary`. */
object MedAITheme {
    val colors: MedAIColors
        @Composable @ReadOnlyComposable get() = LocalMedAIColors.current
}

internal fun MedAIColors.toMaterialScheme(): ColorScheme =
    if (isDark) darkColorScheme(
        primary = brand, onPrimary = onBrand,
        primaryContainer = brandSoft, onPrimaryContainer = onBrandSoft,
        secondary = brand, onSecondary = onBrand,
        secondaryContainer = brandSoft, onSecondaryContainer = onBrandSoft,
        tertiary = info, onTertiary = Color(0xFF082F49),
        tertiaryContainer = infoSoft, onTertiaryContainer = onInfoSoft,
        error = danger, onError = onDanger, errorContainer = dangerSoft, onErrorContainer = onDangerSoft,
        background = canvas, onBackground = textPrimary,
        surface = surface, onSurface = textPrimary,
        surfaceVariant = surfaceSunken, onSurfaceVariant = textSecondary,
        surfaceTint = brand,
        inverseSurface = textPrimary, inverseOnSurface = canvas,
        outline = borderStrong, outlineVariant = border, scrim = scrim,
    ) else lightColorScheme(
        primary = brand, onPrimary = onBrand,
        primaryContainer = brandSoft, onPrimaryContainer = onBrandSoft,
        secondary = brand, onSecondary = onBrand,
        secondaryContainer = brandSoft, onSecondaryContainer = onBrandSoft,
        tertiary = info, onTertiary = Color.White,
        tertiaryContainer = infoSoft, onTertiaryContainer = onInfoSoft,
        error = danger, onError = onDanger, errorContainer = dangerSoft, onErrorContainer = onDangerSoft,
        background = canvas, onBackground = textPrimary,
        surface = surface, onSurface = textPrimary,
        surfaceVariant = surfaceSunken, onSurfaceVariant = textSecondary,
        surfaceTint = brand,
        inverseSurface = Ink800, inverseOnSurface = Color(0xFFF1F5F9),
        outline = borderStrong, outlineVariant = border, scrim = scrim,
    )

@Composable
internal fun ProvideMedAIColors(colors: MedAIColors, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMedAIColors provides colors, content = content)
}
