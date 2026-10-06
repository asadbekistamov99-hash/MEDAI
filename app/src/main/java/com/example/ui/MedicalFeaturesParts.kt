package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.MedAIElevation
import com.example.ui.theme.MedAITheme

/*
 * Small pieces shared by the five medical-feature screens (symptoms, AI doctor, tips, drugs, lab).
 * Kept `internal` and prefixed `Med` so they never collide with other screens' private helpers.
 */

/** Picks the string for the current language (uz / ru, anything else is English). */
internal fun medText(lang: String, uz: String, ru: String, en: String): String =
    when (lang) {
        "uz" -> uz
        "ru" -> ru
        else -> en
    }

/** Section heading used inside a screen's scrolling column. */
@Composable
internal fun MedSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MedAITheme.colors.textPrimary,
        modifier = modifier.semantics { heading() },
    )
}

/** Small round avatar that marks a message as coming from the AI. */
@Composable
internal fun MedAiAvatar(modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val c = MedAITheme.colors
    Box(modifier.size(size).clip(CircleShape).background(c.brandSoft), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.SmartToy, contentDescription = null, tint = c.onBrandSoft, modifier = Modifier.size(size * 0.55f))
    }
}

/** Round icon disc in a tint pair. */
@Composable
internal fun MedIconDisc(
    icon: ImageVector,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(size * 0.5f))
    }
}

/**
 * Dialog with a scrollable body, for content that MedAIDialog (plain message text) cannot hold:
 * markdown, inputs, results. Same surface, radius and shadow as MedAIDialogContent.
 */
@Composable
internal fun MedContentDialog(
    title: String,
    closeText: String,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = MedAITheme.colors
    val shape = RoundedCornerShape(MedAICorners.sheet)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .widthIn(max = 400.dp)
                .heightIn(max = 640.dp)
                .shadow(MedAIElevation.floating, shape)
                .clip(shape)
                .background(c.surfaceRaised)
                .border(1.dp, c.border, shape)
                .padding(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    MedIconDisc(icon, c.brandSoft, c.onBrandSoft, size = 40.dp)
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
            }
            Spacer(Modifier.size(16.dp))
            Column(
                Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            Spacer(Modifier.size(16.dp))
            MedAIPrimaryButton(closeText, onDismiss, Modifier.fillMaxWidth())
        }
    }
}

@Composable
internal fun MedHairline(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(MedAITheme.colors.divider))
}

/** Icon button with a real 48dp target (Material's IconButton is 40dp). */
@Composable
internal fun MedIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(com.example.ui.theme.MinTouch)
            .clip(CircleShape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint)
    }
}
