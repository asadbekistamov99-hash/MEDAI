package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryGreen,
    tertiary = SuccessGreen,
    background = MedicalBackground,
    surface = MedicalCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = TextSecondary,
    outline = MedicalBorder,
    outlineVariant = Color(0xFFF1F5F9)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Forced clean, bright, vibrant light mode
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
}

