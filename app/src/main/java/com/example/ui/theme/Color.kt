package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * MedAI design tokens.
 *
 * One rule for this file: every constant that existed before the visual refresh keeps its
 * name and its meaning. There are ~900 references to these tokens across the 16 screens, so
 * re-tuning a value here is the cheapest way to lift the whole app at once — renaming one
 * would mean touching every call site for no visual gain.
 *
 * The palette is a clinical teal: calm enough for a health app, saturated enough that the
 * primary actions read as tappable. Premium violet is reserved exclusively for paid state so
 * "does this cost money?" is answerable at a glance. Every foreground/background pair below
 * was picked to clear WCAG AA (4.5:1) on the light canvas.
 */

// --- Brand ramp: teal ---------------------------------------------------------------------
// Tonal ramp from near-black to near-white. Screens pick a step; they never invent a colour.
val Teal950 = Color(0xFF04302C)
val Teal900 = Color(0xFF075E55)
val Teal800 = Color(0xFF0A7568)
val Teal700 = Color(0xFF0B8B7C)
val Teal600 = Color(0xFF0D9488) // primary
val Teal500 = Color(0xFF14B8A6)
val Teal400 = Color(0xFF2DD4BF)
val Teal200 = Color(0xFF99F6E4)
val Teal100 = Color(0xFFCCFBF1)
val Teal50 = Color(0xFFF0FDFA)

// --- Legacy aliases (unchanged names, retuned values) ------------------------------------
val PrimaryGreen = Teal800 // primary actions, active tab, focus (5.6:1 on white: safe as text and as a fill)
val DarkGreen = Teal900 // gradient end for primary surfaces
val LightGreen = Teal50 // soft tinted surface behind content
val SecondaryGreen = Teal500 // supporting accent
val AccentCyan = Color(0xFF0EA5E9) // informational / links / charts
val SuccessGreen = Color(0xFF047857) // emerald-700: 5.5:1 on white
val WarningOrange = Color(0xFFB45309) // amber-700: 5.0:1 on white
val ErrorRed = Color(0xFFC62828) // 5.6:1 on white, and white-on-red passes too
val PremiumPurple = Color(0xFF7C3AED) // paid state only
val PremiumLight = Color(0xFFF5F3FF)

// --- Neutrals: cool slate, never pure grey ------------------------------------------------
val Ink900 = Color(0xFF0B1220)
val Ink800 = Color(0xFF0F172A)
val Ink700 = Color(0xFF1E293B)
val Ink500 = Color(0xFF526175)
val Ink400 = Color(0xFF5F6F85) // darkest "muted" that still clears 4.5:1 on the canvas
val Ink200 = Color(0xFFD9E2EA)
val Ink100 = Color(0xFFE8EEF3)
val Ink50 = Color(0xFFF5F8FA)
val Canvas = Color(0xFFF4F8F8) // app canvas: a hair off-white with a teal cast

val TextPrimary = Ink800
val TextSecondary = Ink500
val MedicalBackground = Canvas
val MedicalCard = Color(0xFFFFFFFF)
val MedicalBorder = Color(0xFFDCE7E5)
val MedicalCardBorder = Color(0xFFE7F0EE)
val CardShadowColor = Color(0xFF0B1220)

// --- Extra surfaces used by the newer component kit ---------------------------------------
val SurfaceSunken = Color(0xFFF7FAF9)
val SurfaceRaised = Color(0xFFFFFFFF)
val DividerSoft = Color(0xFFEDF3F2)

/** Chart / data-viz series. Ordered for maximum adjacent-pair separation. */
val ChartSeries = listOf(
    Color(0xFF0D9488),
    Color(0xFF7C3AED),
    Color(0xFF0EA5E9),
    Color(0xFFD97706),
    Color(0xFFDB2777),
    Color(0xFF059669),
)

// --- Gradients -----------------------------------------------------------------------------
// Defined once so headers, buttons and hero cards share the exact same light direction
// (top-left to bottom-right). Mismatched gradient angles across screens read as "cheap".
val BrandGradient = Brush.linearGradient(colors = listOf(Color(0xFF0F766E), Color(0xFF0A5A53)))
val BrandGradientWide = Brush.linearGradient(
    colors = listOf(Color(0xFF0F766E), Color(0xFF0C655E), Color(0xFF0A5A53))
)
val PremiumGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
)
val DangerGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF05252), ErrorRed)
)
val HeroScrim = Brush.verticalGradient(
    colors = listOf(Color(0x00000000), Color(0x66000000))
)
val CanvasWash = Brush.verticalGradient(
    colors = listOf(Color(0xFFF1F7F7), Canvas)
)

// --- Material 3 fallbacks kept for the generated-theme names ------------------------------
val Purple80 = Teal500
val PurpleGrey80 = Teal100
val Pink80 = PremiumPurple
val Purple40 = Teal700
val PurpleGrey40 = Teal200
val Pink40 = Teal600
