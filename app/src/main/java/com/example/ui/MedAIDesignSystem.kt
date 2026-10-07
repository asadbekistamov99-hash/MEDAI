package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.MedAIText
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MinTouch
import com.example.ui.theme.Spacing

/*
 * Older helpers that screens still call. They share the same theme roles as MedAIKit.kt, so
 * they follow light/dark automatically. (The previous gradient header, stat tile, feature
 * tile, segmented tabs, progress ring, meter and detail row had no remaining callers and were
 * removed; MedAIKit.kt has the current equivalents.)
 */

/** Severity of an [MedAIInfoBanner]. */
enum class MedAITone { Info, Success, Warning, Error }

/** Inline status message with a soft background and always-readable text. */
@Composable
fun MedAIInfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: MedAITone = MedAITone.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = MedAITheme.colors
    val accent: Color
    val soft: Color
    val onSoft: Color
    val icon: ImageVector
    when (tone) {
        MedAITone.Info -> { accent = c.info; soft = c.infoSoft; onSoft = c.onInfoSoft; icon = Icons.Default.Info }
        MedAITone.Success -> { accent = c.success; soft = c.successSoft; onSoft = c.onSuccessSoft; icon = Icons.Default.CheckCircle }
        MedAITone.Warning -> { accent = c.warning; soft = c.warningSoft; onSoft = c.onWarningSoft; icon = Icons.Default.WarningAmber }
        MedAITone.Error -> { accent = c.danger; soft = c.dangerSoft; onSoft = c.onDangerSoft; icon = Icons.Default.ErrorOutline }
    }
    val shape = RoundedCornerShape(MedAICorners.control)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(soft)
            .border(1.dp, accent.copy(alpha = 0.3f), shape)
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Spacing.md))
        Text(text, style = MaterialTheme.typography.bodySmall, color = onSoft, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.width(Spacing.sm))
            Box(
                Modifier
                    .heightIn(min = MinTouch)
                    .clip(RoundedCornerShape(MedAICorners.pill))
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = onSoft, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Section heading: optional eyebrow, title, optional trailing action. */
@Composable
fun MedAISectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    horizontalPadding: Dp = Spacing.xl,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    val c = MedAITheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(eyebrow.uppercase(), style = MedAIText.Eyebrow, color = c.brand)
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
        }
    }
}

/** Foreground that is readable on [bg]: near-black on light fills, white on dark ones. */
private fun onFill(bg: Color): Color = if (bg.luminance() > 0.45f) Color(0xFF0B1220) else Color.White

/** Small rounded label; [selected] switches between a filled and a tinted treatment. */
@Composable
fun MedAIChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    color: Color = Color.Unspecified,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    val c = MedAITheme.colors
    val base = if (color == Color.Unspecified) c.brand else color
    val bg by animateColorAsState(if (selected) base else base.copy(alpha = 0.12f), tween(180), label = "chipBg")
    // Unselected text sits on a faint tint of the surface, so it uses the role colour as-is on
    // brand/danger/etc. and falls back to primary text for light custom colours.
    val fg by animateColorAsState(
        if (selected) onFill(base) else if (base.luminance() > 0.55f) c.textPrimary else base,
        tween(180), label = "chipFg",
    )
    val shape = RoundedCornerShape(MedAICorners.pill)
    val outer = if (onClick != null) {
        modifier.defaultMinSize(minHeight = MinTouch).toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
    } else modifier
    Box(outer, contentAlignment = Alignment.Center) {
        Row(
            Modifier
                .clip(shape)
                .background(bg)
                .then(if (selected) Modifier else Modifier.border(1.dp, base.copy(alpha = 0.35f), shape))
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Placeholder for a list with nothing in it. */
@Composable
fun MedAIEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = MedAITheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.section, horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(c.brandSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon ?: Icons.Default.Info, contentDescription = null, tint = c.onBrandSoft, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, textAlign = TextAlign.Center)
        if (message != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(message, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.lg))
            MedAIPrimaryButton(text = actionLabel, onClick = onAction, modifier = Modifier.widthIn(min = 200.dp))
        }
    }
}

/**
 * Primary action. Solid brand fill by default; pass [brush] for a special fill (the Premium
 * upgrade button uses the violet gradient).
 */
@Composable
fun MedAIButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    brush: Brush? = null,
) {
    if (brush == null) {
        MedAIPrimaryButton(text = text, onClick = onClick, modifier = modifier, icon = icon, enabled = enabled)
        return
    }
    val c = MedAITheme.colors
    val shape = RoundedCornerShape(MedAICorners.control)
    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(if (enabled) brush else Brush.linearGradient(listOf(c.surfaceSunken, c.surfaceSunken)))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = if (enabled) Color.White else c.textSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text, style = MaterialTheme.typography.labelLarge, fontSize = 15.sp,
                color = if (enabled) Color.White else c.textSecondary, textAlign = TextAlign.Center, maxLines = 2,
            )
        }
    }
}
