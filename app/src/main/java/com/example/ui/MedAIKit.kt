package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.MedAIElevation
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MedAITint
import com.example.ui.theme.MinTouch
import com.example.ui.theme.Spacing

/*
 * MedAI component kit v2.
 *
 * Rules every component here follows:
 *  - colours come from MedAITheme.colors (so light/dark is automatic), never from constants;
 *  - anything tappable is at least 48dp tall (MinTouch), even when it LOOKS smaller (chips);
 *  - text never assumes a language: labels wrap to 2 lines or ellipsize, they do not clip;
 *  - feedback is a ~120ms press scale or a ~180ms colour fade. Nothing bounces.
 */

// ---------------------------------------------------------------------------------------
// Press feedback shared by every tappable surface
// ---------------------------------------------------------------------------------------
@Composable
private fun rememberPressScale(source: MutableInteractionSource, enabled: Boolean = true, pressed: Float = 0.97f): Float {
    val isPressed by source.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressed else 1f,
        animationSpec = tween(120),
        label = "pressScale",
    )
    return s
}

// ---------------------------------------------------------------------------------------
// Buttons
// ---------------------------------------------------------------------------------------
private enum class ButtonKind { Primary, Secondary, Danger }

@Composable
private fun MedAIButtonBase(
    kind: ButtonKind,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    icon: ImageVector?,
    enabled: Boolean,
    loading: Boolean,
) {
    val c = MedAITheme.colors
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val active = enabled && !loading
    val scale = rememberPressScale(source, active)

    val container by animateColorAsState(
        targetValue = when {
            !enabled -> c.surfaceSunken
            kind == ButtonKind.Primary -> if (pressed) c.brandStrong else c.brand
            kind == ButtonKind.Danger -> c.danger
            else -> if (pressed) c.brandSoft else Color.Transparent
        },
        animationSpec = tween(120), label = "btnBg",
    )
    val content = when {
        !enabled -> c.textSecondary
        kind == ButtonKind.Primary -> c.onBrand
        kind == ButtonKind.Danger -> c.onDanger
        else -> c.brand
    }
    val shape = RoundedCornerShape(MedAICorners.control)
    Box(
        modifier = modifier
            .scale(scale)
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(container)
            .then(if (kind == ButtonKind.Secondary) Modifier.border(1.5.dp, if (enabled) c.brand else c.borderStrong, shape) else Modifier)
            .clickable(enabled = active, interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = content)
                Spacer(Modifier.width(Spacing.sm))
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 15.sp,
                color = content,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The one main action on a screen. Solid brand fill; shows a spinner and ignores taps while [loading]. */
@Composable
fun MedAIPrimaryButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    icon: ImageVector? = null, enabled: Boolean = true, loading: Boolean = false,
) = MedAIButtonBase(ButtonKind.Primary, text, onClick, modifier, icon, enabled, loading)

/** Outlined; for the alternative to the primary action. */
@Composable
fun MedAISecondaryButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    icon: ImageVector? = null, enabled: Boolean = true,
) = MedAIButtonBase(ButtonKind.Secondary, text, onClick, modifier, icon, enabled, false)

/** Filled red. Reserved for SOS and destructive confirmations. */
@Composable
fun MedAIDangerButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    icon: ImageVector? = null, enabled: Boolean = true,
) = MedAIButtonBase(ButtonKind.Danger, text, onClick, modifier, icon, enabled, false)

/** Low-emphasis action ("Forgot password?"). 48dp tall hit area, no fill until pressed. */
@Composable
fun MedAITextButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    color: Color = Color.Unspecified, enabled: Boolean = true,
) {
    val c = MedAITheme.colors
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val bg by animateColorAsState(if (pressed && enabled) c.brandSoft else Color.Transparent, tween(120), label = "txtBg")
    Box(
        modifier = modifier
            .heightIn(min = MinTouch)
            .clip(RoundedCornerShape(MedAICorners.control))
            .background(bg)
            .clickable(enabled = enabled, interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 14.sp,
            color = if (color != Color.Unspecified) color else if (enabled) c.brand else c.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

// ---------------------------------------------------------------------------------------
// Surfaces
// ---------------------------------------------------------------------------------------
/** Flat white card with a hairline. Pass [onClick] to make it tappable (adds a press scale). */
@Composable
fun MedAICard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(MedAICorners.card),
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = MedAITheme.colors
    val source = remember { MutableInteractionSource() }
    val scale = rememberPressScale(source, onClick != null, 0.985f)
    Column(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.border, shape)
            .then(
                if (onClick != null) Modifier.clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
                else Modifier
            )
            .padding(contentPadding),
        content = content,
    )
}

/** The gradient summary card at the top of a screen. White text is guaranteed 4.5:1 on it. */
@Composable
fun MedAIHeroCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = MedAITheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MedAICorners.hero))
            .background(c.heroBrush)
            .padding(20.dp),
        content = content,
    )
}

/** Translucent metric chip that sits on a [MedAIHeroCard]. */
@Composable
fun MedAIMetricPill(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    val c = MedAITheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.16f)) // darker, not lighter: keeps white text above 4.5:1
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = c.onHero, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = c.onHero, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.onHeroMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Square-ish quick action: tinted icon disc over a label. Label may use two lines. */
@Composable
fun MedAIQuickTile(
    icon: ImageVector, label: String, tint: MedAITint, onClick: () -> Unit, modifier: Modifier = Modifier,
    danger: Boolean = false,
) {
    val c = MedAITheme.colors
    val source = remember { MutableInteractionSource() }
    val scale = rememberPressScale(source)
    val shape = RoundedCornerShape(MedAICorners.tile)
    Column(
        modifier = modifier
            .scale(scale)
            .heightIn(min = 96.dp)
            .clip(shape)
            .background(if (danger) c.danger else c.surface)
            .border(1.dp, if (danger) c.danger else c.border, shape)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp), // no side padding: four tiles must fit a 360dp phone
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(if (danger) Color.White.copy(alpha = 0.2f) else tint.bg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = if (danger) c.onDanger else tint.fg, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label, style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp), color = if (danger) c.onDanger else c.textPrimary,
            textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
        )
    }
}

data class MedAIQuickItem(
    val icon: ImageVector, val label: String, val tint: MedAITint, val onClick: () -> Unit, val tag: String = "",
    val danger: Boolean = false,
)

/**
 * Quick actions that adapt to the phone: four across when each tile has room for a long label
 * (>= 340dp of CONTENT width, i.e. phones from ~375dp), a 2x2 grid on smaller ones. Language-independent, so a long Russian word
 * never has to be hyphenated or shrunk below 12sp.
 */
@Composable
fun MedAIQuickTileGrid(items: List<MedAIQuickItem>, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val perRow = if (maxWidth >= 340.dp) 4 else 2
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.chunked(perRow).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(if (perRow == 4) 4.dp else 8.dp)) {
                    row.forEach { it ->
                        MedAIQuickTile(
                            it.icon, it.label, it.tint, it.onClick,
                            Modifier.weight(1f).then(if (it.tag.isNotEmpty()) Modifier.testTag(it.tag) else Modifier),
                            danger = it.danger,
                        )
                    }
                    repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** One row of a settings/services list. Put several inside a [MedAICard] with zero padding. */
@Composable
fun MedAIListRow(
    icon: ImageVector, title: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    subtitle: String? = null, tint: MedAITint = MedAITheme.colors.tintTeal, showDivider: Boolean = true,
) {
    val c = MedAITheme.colors
    Column(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tint.bg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontSize = 15.sp, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = c.textSecondary)
        }
        if (showDivider) Box(Modifier.padding(start = 70.dp).fillMaxWidth().height(1.dp).background(c.divider))
    }
}

// ---------------------------------------------------------------------------------------
// Chips and badges
// ---------------------------------------------------------------------------------------
/** Selectable filter. Looks 36dp tall, but the hit area is 48dp. */
@Composable
fun MedAIFilterChip(
    text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null,
) {
    val c = MedAITheme.colors
    val bg by animateColorAsState(if (selected) c.brand else c.surface, tween(180), label = "fcBg")
    val fg by animateColorAsState(if (selected) c.onBrand else c.textPrimary, tween(180), label = "fcFg")
    val borderColor by animateColorAsState(if (selected) c.brand else c.borderStrong, tween(180), label = "fcBd")
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = MinTouch, minHeight = MinTouch)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() }),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .heightIn(min = 36.dp)
                .clip(RoundedCornerShape(MedAICorners.pill))
                .background(bg)
                .border(1.dp, borderColor, RoundedCornerShape(MedAICorners.pill))
                .padding(horizontal = 14.dp, vertical = 6.dp),
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

enum class MedAIBadgeTone { Brand, Success, Warning, Danger, Info, Premium }

/** Non-interactive status label ("Good", "Premium", "Overdue"). Always soft bg + dark fg. */
@Composable
fun MedAIBadge(text: String, tone: MedAIBadgeTone, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val c = MedAITheme.colors
    val (bg, fg) = when (tone) {
        MedAIBadgeTone.Brand -> c.brandSoft to c.onBrandSoft
        MedAIBadgeTone.Success -> c.successSoft to c.onSuccessSoft
        MedAIBadgeTone.Warning -> c.warningSoft to c.onWarningSoft
        MedAIBadgeTone.Danger -> c.dangerSoft to c.onDangerSoft
        MedAIBadgeTone.Info -> c.infoSoft to c.onInfoSoft
        MedAIBadgeTone.Premium -> c.premiumSoft to c.onPremiumSoft
    }
    Row(
        modifier.clip(RoundedCornerShape(MedAICorners.pill)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ---------------------------------------------------------------------------------------
// Text input
// ---------------------------------------------------------------------------------------
/**
 * Labelled text field. The label sits ABOVE the box (it never floats or shrinks), so long
 * Russian labels wrap instead of being squeezed into a 12sp cut-off.
 * Border: 3:1 against the surface at rest, 2dp brand on focus, danger when [error] is set.
 */
@Composable
fun MedAITextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    error: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    showPasswordDescription: String = "Show password",
    hidePasswordDescription: String = "Hide password",
    testTag: String = "",
    visualTransformation: VisualTransformation? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions? = null,
) {
    val c = MedAITheme.colors
    var focused by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        when {
            error != null -> c.danger
            focused -> c.brand
            else -> c.borderStrong
        },
        tween(150), label = "tfBorder",
    )
    val shape = RoundedCornerShape(MedAICorners.control)
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = c.textPrimary, maxLines = 2)
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textPrimary),
            cursorBrush = SolidColor(c.brand),
            keyboardOptions = keyboardOptions ?: KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            visualTransformation = visualTransformation
                ?: if (isPassword && !revealed) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clip(shape)
                        .background(if (enabled) c.surface else c.surfaceSunken)
                        .border(if (focused || error != null) 2.dp else 1.dp, borderColor, shape)
                        .padding(start = 14.dp, end = if (isPassword || trailingContent != null) 2.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingIcon != null) {
                        Icon(leadingIcon, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                    }
                    Box(Modifier.weight(1f).padding(vertical = 14.dp)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        inner()
                    }
                    if (trailingContent != null) trailingContent()
                    if (isPassword) {
                        Box(
                            Modifier.size(MinTouch).clip(CircleShape).clickable(role = Role.Button) { revealed = !revealed },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (revealed) hidePasswordDescription else showPasswordDescription,
                                tint = c.textSecondary,
                            )
                        }
                    }
                }
            },
        )
        if (error != null) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = c.danger, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(Modifier.width(6.dp))
                Text(error, style = MaterialTheme.typography.bodySmall, color = c.danger)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------
// Navigation
// ---------------------------------------------------------------------------------------
/** Flat top bar: back button, title block, trailing actions. */
@Composable
fun MedAITopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backDescription: String = "Back",
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    val c = MedAITheme.colors
    Column(modifier.background(c.canvas)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                Box(
                    Modifier.size(MinTouch).clip(CircleShape).clickable(role = Role.Button, onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backDescription, tint = c.textPrimary) }
            } else {
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (actions != null) Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
    }
}

data class MedAIBottomItem(val icon: ImageVector, val label: String)

/** Four-tab bar: every tab keeps its label; the selected one gets a filled pill. */
@Composable
fun MedAIBottomBar(
    items: List<MedAIBottomItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MedAITheme.colors
    Column(modifier.background(c.surface)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
            items.forEachIndexed { i, item ->
                val selected = i == selectedIndex
                val source = remember { MutableInteractionSource() }
                val scale = rememberPressScale(source, pressed = 0.94f)
                val pill by animateColorAsState(if (selected) c.brand else Color.Transparent, tween(180), label = "navPill")
                val iconTint by animateColorAsState(if (selected) c.onBrand else c.textSecondary, tween(180), label = "navIcon")
                Column(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .scale(scale)
                        .testTag("bottom_item_$i")
                        .selectable(selected = selected, interactionSource = source, indication = null, role = Role.Tab, onClick = { onSelect(i) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(Modifier.height(32.dp).width(60.dp).clip(CircleShape).background(pill), contentAlignment = Alignment.Center) {
                        Icon(item.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                    }
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) c.brand else c.textSecondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------
// Dialog
// ---------------------------------------------------------------------------------------
/** The dialog body without a window, so it can also be shown inline (gallery, previews). */
@Composable
fun MedAIDialogContent(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    destructive: Boolean = false,
    icon: ImageVector? = null,
) {
    val c = MedAITheme.colors
    val shape = RoundedCornerShape(MedAICorners.sheet)
    Column(
        modifier
            .widthIn(max = 360.dp)
            .shadow(MedAIElevation.floating, shape)
            .clip(shape)
            .background(c.surfaceRaised)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            val tint = if (destructive) c.dangerSoft to c.onDangerSoft else c.brandSoft to c.onBrandSoft
            Box(Modifier.size(56.dp).clip(CircleShape).background(tint.first), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint.second, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, color = c.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        // Stacked, full-width buttons: a long Russian label never has to share a row.
        if (destructive) MedAIDangerButton(confirmText, onConfirm, Modifier.fillMaxWidth())
        else MedAIPrimaryButton(confirmText, onConfirm, Modifier.fillMaxWidth())
        if (dismissText != null && onDismiss != null) {
            Spacer(Modifier.height(4.dp))
            MedAITextButton(dismissText, onDismiss, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun MedAIDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    dismissText: String? = null,
    destructive: Boolean = false,
    icon: ImageVector? = null,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        MedAIDialogContent(
            title = title, message = message, confirmText = confirmText, onConfirm = onConfirm,
            dismissText = dismissText, onDismiss = onDismissRequest, destructive = destructive, icon = icon,
        )
    }
}

// ---------------------------------------------------------------------------------------
// Loading
// ---------------------------------------------------------------------------------------
@Composable
fun MedAISpinner(modifier: Modifier = Modifier, size: Dp = 28.dp) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        strokeWidth = 3.dp,
        color = MedAITheme.colors.brand,
        trackColor = MedAITheme.colors.brandSoft,
    )
}

/** Placeholder block for content that is loading. Pulses softly; pass animated=false for stills. */
@Composable
fun MedAISkeleton(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(12.dp), animated: Boolean = true) {
    val c = MedAITheme.colors
    val alpha = if (animated) {
        val t = rememberInfiniteTransition(label = "skeleton")
        val a by t.animateFloat(
            initialValue = 0.55f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "skeletonAlpha",
        )
        a
    } else 0.8f
    Box(modifier.alpha(alpha).clip(shape).background(c.surfaceSunken).border(1.dp, c.border, shape))
}
