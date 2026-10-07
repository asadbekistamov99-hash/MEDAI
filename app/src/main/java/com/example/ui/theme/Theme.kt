package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * App theme.
 *
 * Material's colour scheme is derived from [MedAIColors], so M3 widgets (dialogs, switches,
 * text selection) follow the same light/dark palette as the MedAI components.
 *
 * `darkTheme` follows the system setting; every screen reads [MedAITheme.colors].
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) MedAIDarkColors else MedAILightColors
    ProvideMedAIColors(colors) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(),
            typography = Typography,
            shapes = MedAIShapes,
            content = content,
        )
    }
}
