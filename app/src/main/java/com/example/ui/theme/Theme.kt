package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * App theme.
 *
 * Material's colour scheme is derived from [MedAIColors], so M3 widgets (dialogs, switches,
 * text selection) follow the same light/dark palette as the MedAI components.
 *
 * `darkTheme` defaults to false on purpose: screens that have not been migrated to
 * [MedAITheme.colors] still use the legacy light-only constants (PrimaryGreen, MedicalCard...),
 * and would show dark text on dark cards. Flip the default once the last screen is migrated.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
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
