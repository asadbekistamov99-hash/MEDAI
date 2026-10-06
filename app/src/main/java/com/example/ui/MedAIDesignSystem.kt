package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.CanvasWash
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DividerSoft
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LightGreen
import com.example.ui.theme.MedicalBorder
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.Spacing
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceSunken
import com.example.ui.theme.Teal500
import com.example.ui.theme.Teal700
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange

/**
 * The MedAI component kit.
 *
 * Everything here is built from the same primitives the rest of the app already uses, so it
 * works on the project's Compose BOM without new dependencies. The naming rule is `MedAI*`
 * so nothing in this file can collide with the ~60 screen-local composables in the same
 * package.
 *
 * The point of the kit is consistency, not novelty: a section header looks the same on the
 * home screen and in admin, a stat tile has the same internal rhythm everywhere, and a
 * destructive action is always red-gradient with a trash icon.
 */

/** Severity of an [MedAIInfoBanner]. */
enum class MedAITone { Info, Success, Warning, Error }

private fun MedAITone.accent(): Color = when (this) {
    MedAITone.Info -> AccentCyan
    MedAITone.Success -> SuccessGreen
    MedAITone.Warning -> WarningOrange
    MedAITone.Error -> ErrorRed
}

private fun MedAITone.icon(): ImageVector = when (this) {
    MedAITone.Info -> Icons.Default.Info
    MedAITone.Success -> Icons.Default.CheckCircle
    MedAITone.Warning -> Icons.Default.WarningAmber
    MedAITone.Error -> Icons.Default.ErrorOutline
}

private fun MedAITone.tint(): Color = when (this) {
    MedAITone.Info -> AccentCyan.copy(alpha = 0.08f)
    MedAITone.Success -> SuccessGreen.copy(alpha = 0.08f)
    MedAITone.Warning -> WarningOrange.copy(alpha = 0.10f)
    MedAITone.Error -> ErrorRed.copy(alpha = 0.07f)
}

/**
 * App canvas. Use as the background of any full-screen scaffold: a barely-there vertical
 * wash instead of a flat fill, which stops large scrollable areas from looking like paper.
 */
@Composable
fun MedAIScreenCanvas(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasWash),
        content = { content() },
    )
}

/**
 * Section heading with an optional trailing action. The eyebrow/label pair is the main
 * hierarchy device on a long scrolling screen: a coloured 3dp rule plus an uppercase label,
 * then the section title.
 */
@Composable
fun MedAISectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    horizontalPadding: androidx.compose.ui.unit.Dp = Spacing.xl,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(PrimaryGreen),
        )
        Spacer(Modifier.width(Spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryGreen,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(Spacing.sm))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = trailing,
            )
        }
    }
}

/**
 * Elevated hero header used by full-screen feature pages. A gradient band with a back
 * affordance, a title block and an optional accent chip — replaces the flat white app bar on
 * every secondary screen so those pages stop looking like settings menus.
 */
@Composable
fun MedAIGradientHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BrandGradient)
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(Spacing.md))
        }
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(MedAICorners.control))
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(Spacing.md))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (actions != null) {
            Spacer(Modifier.width(Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
    }
}

/** Small rounded label. [selected] switches between a filled and an outlined treatment. */
@Composable
fun MedAIChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    color: Color = PrimaryGreen,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    val background by animateColorAsState(
        targetValue = if (selected) color else color.copy(alpha = 0.10f),
        animationSpec = tween(180),
        label = "chipBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else color,
        animationSpec = tween(180),
        label = "chipFg",
    )
    val base = Modifier
        .clip(RoundedCornerShape(MedAICorners.pill))
        .background(background)
        .then(
            if (selected) Modifier
            else Modifier.border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(MedAICorners.pill))
        )
        .padding(horizontal = 14.dp, vertical = 7.dp)
    Row(
        modifier = modifier.then(if (onClick != null) base.clickable(onClick = onClick) else base),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
        )
    }
}

/** A labelled number. The workhorse of every dashboard and summary block. */
@Composable
fun MedAIStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = PrimaryGreen,
    caption: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.98f else 1f,
        label = "statScale",
    )
    MedicalCard(
        modifier = modifier.scale(scale),
        onClick = onClick,
        borderStroke = BorderStroke(1.dp, DividerSoft),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(Spacing.sm))
                }
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (caption != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Tappable entry in a feature list or on the home grid. */
@Composable
fun MedAIFeatureTile(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    color: Color = PrimaryGreen,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val alpha = if (enabled) 1f else 0.45f
    MedicalCard(
        modifier = modifier,
        onClick = if (enabled) onClick else null,
        borderStroke = BorderStroke(1.dp, DividerSoft),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(color.copy(alpha = 0.12f * alpha)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color.copy(alpha = alpha),
                        modifier = Modifier.size(21.dp),
                    )
                }
                Spacer(Modifier.width(Spacing.md))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = alpha),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailingIcon != null) {
                Spacer(Modifier.width(Spacing.sm))
                Icon(
                    trailingIcon,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = alpha * 0.7f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Inline status message. Replaces ad-hoc coloured boxes scattered through the screens. */
@Composable
fun MedAIInfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: MedAITone = MedAITone.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val accent = tone.accent()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MedAICorners.control))
            .background(tone.tint())
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(MedAICorners.control))
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = tone.icon(),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(MedAICorners.pill))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

/**
 * Placeholder for a list with nothing in it. Every empty list in the app had grown its own
 * one-off "no data" block; this makes them all identical.
 */
@Composable
fun MedAIEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.section, horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(LightGreen),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon ?: Icons.Default.Info,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        if (message != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.lg))
            MedAIButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.width(200.dp),
            )
        }
    }
}

/**
 * Horizontal tab switcher. Implemented with plain composables rather than M3's
 * `SingleChoiceSegmentedButtonRow` so it does not need an experimental opt-in and so it can
 * animate the selection indicator.
 */
@Composable
fun MedAISegmentedTabs(
    labels: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MedAICorners.control))
            .background(SurfaceSunken)
            .border(1.dp, DividerSoft, RoundedCornerShape(MedAICorners.control))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(
                targetValue = if (selected) PrimaryGreen else Color.Transparent,
                animationSpec = tween(180),
                label = "tabBg",
            )
            val fg by animateColorAsState(
                targetValue = if (selected) Color.White else TextSecondary,
                animationSpec = tween(180),
                label = "tabFg",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(bg)
                    .clickable { onSelect(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Circular progress with the percentage in the middle. */
@Composable
fun MedAIProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 96.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 10.dp,
    color: Color = PrimaryGreen,
    trackColor: Color = LightGreen,
    centerLabel: String? = null,
    centerCaption: String? = null,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "ring",
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (centerLabel != null) {
                Text(
                    text = centerLabel,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
            }
            if (centerCaption != null) {
                Text(
                    text = centerCaption,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

/** Horizontal meter, for the "3 of 7 doses taken" style rows. */
@Composable
fun MedAIMeter(
    progress: Float,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 8.dp,
    color: Color = PrimaryGreen,
    trackColor: Color = LightGreen,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "meter",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(MedAICorners.pill))
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(MedAICorners.pill))
                .background(
                    Brush.horizontalGradient(listOf(color, DarkGreen)),
                ),
        )
    }
}

/** A key/value row for detail and summary lists. */
@Composable
fun MedAIDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
    }
}

/** Hairline separator matched to the card border colour. */
@Composable
fun MedAIDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerSoft),
    )
}

/**
 * Primary action. Gradient fill, coloured shadow, press-scale, and a disabled state that
 * keeps the same geometry so the layout never jumps.
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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = tween(120),
        label = "btnScale",
    )
    val shape = RoundedCornerShape(MedAICorners.control)
    val fill = when {
        !enabled -> Brush.horizontalGradient(listOf(MedicalBorder, MedicalBorder))
        brush != null -> brush
        else -> Brush.horizontalGradient(listOf(Teal500, Teal700))
    }
    Box(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = shape,
                ambientColor = PrimaryGreen.copy(alpha = 0.35f),
                spotColor = PrimaryGreen.copy(alpha = 0.35f),
            )
            .clip(shape)
            .background(fill)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) Color.White else TextSecondary,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) Color.White else TextSecondary,
                fontSize = 15.sp,
            )
        }
    }
}
