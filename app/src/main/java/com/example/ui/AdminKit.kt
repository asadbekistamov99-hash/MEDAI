@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.MedAIElevation
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MedAITint

/** uz / ru / en picker used by every Admin* composable. Anything other than "uz"/"ru" is English. */
internal fun adminT(lang: String, uz: String, ru: String, en: String): String =
    when (lang) {
        "uz" -> uz
        "ru" -> ru
        else -> en
    }

/** Current UI language as a state, read from the same source the other screens use. */
@Composable
internal fun adminLang(viewModel: AppViewModel): String {
    val lang by viewModel.currentLanguage.collectAsState()
    return lang
}

internal data class AdminTabItem(
    val icon: ImageVector,
    val label: String,
    val count: Int = 0,
    val tone: MedAIBadgeTone = MedAIBadgeTone.Info,
)

/** Horizontally scrollable section switcher: 48dp pill segments with an optional count badge. */
@Composable
internal fun AdminTabBar(items: List<AdminTabItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = MedAITheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEachIndexed { i, item ->
            val sel = i == selected
            val shape = RoundedCornerShape(MedAICorners.pill)
            val requester = remember { BringIntoViewRequester() }
            LaunchedEffect(sel) { if (sel) requester.bringIntoView() }
            Row(
                Modifier
                    .bringIntoViewRequester(requester)
                    .heightIn(min = 48.dp)
                    .clip(shape)
                    .background(if (sel) c.brand else c.surface)
                    .border(1.dp, if (sel) c.brand else c.borderStrong, shape)
                    .selectable(selected = sel, role = Role.Tab, onClick = { onSelect(i) })
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(item.icon, contentDescription = null, tint = if (sel) c.onBrand else c.textSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(item.label, style = MaterialTheme.typography.labelLarge, color = if (sel) c.onBrand else c.textPrimary, maxLines = 1)
                if (item.count > 0) {
                    Spacer(Modifier.width(8.dp))
                    MedAIBadge(item.count.toString(), item.tone)
                }
            }
        }
    }
}

/** Small summary tile: tinted icon disc, value, label. Put 2-3 in an [AdminTileRow]. */
@Composable
internal fun RowScope.AdminMetricTile(icon: ImageVector, tint: MedAITint, value: String, label: String) {
    val c = MedAITheme.colors
    MedAICard(Modifier.weight(1f).fillMaxHeight(), contentPadding = 10.dp) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(tint.bg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
    }
}

@Composable
internal fun AdminTileRow(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

/** Compact 48dp action button for dense cards. Fill and text colours are passed as theme roles. */
@Composable
internal fun AdminActionButton(
    text: String,
    onClick: () -> Unit,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    outline: Color? = null,
) {
    val shape = RoundedCornerShape(MedAICorners.control)
    Row(
        modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(bg)
            .then(if (outline != null) Modifier.border(1.dp, outline, shape) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = fg, textAlign = TextAlign.Center)
    }
}

/** A switch row whose whole width is the toggle target (well over 56x48dp). */
@Composable
internal fun AdminSwitchRow(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    tint: MedAITint = MedAITheme.colors.tintTeal,
    danger: Boolean = false,
) {
    val c = MedAITheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(tint.bg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        Spacer(Modifier.width(12.dp))
        val on = if (danger) c.danger else c.brand
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (danger) c.onDanger else c.onBrand,
                checkedTrackColor = on,
                uncheckedThumbColor = c.textSecondary,
                uncheckedTrackColor = c.surfaceSunken,
                uncheckedBorderColor = c.borderStrong,
            ),
        )
    }
}

@Composable
internal fun AdminCardHeader(icon: ImageVector, tint: MedAITint, title: String, modifier: Modifier = Modifier) {
    val c = MedAITheme.colors
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(tint.bg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun AdminDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(MedAITheme.colors.divider))
}

/** Form dialog styled like [MedAIDialogContent] (rounded 28dp, raised surface) that can hold fields. */
@Composable
internal fun AdminFormDialog(
    title: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = MedAITheme.colors
    val shape = RoundedCornerShape(MedAICorners.sheet)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .widthIn(max = 400.dp)
                .shadow(MedAIElevation.floating, shape)
                .clip(shape)
                .background(c.surfaceRaised)
                .padding(24.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = c.textPrimary)
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            Spacer(Modifier.height(20.dp))
            if (destructive) MedAIDangerButton(confirmText, onConfirm, Modifier.fillMaxWidth())
            else MedAIPrimaryButton(confirmText, onConfirm, Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            MedAITextButton(dismissText, onDismiss, Modifier.fillMaxWidth())
        }
    }
}
