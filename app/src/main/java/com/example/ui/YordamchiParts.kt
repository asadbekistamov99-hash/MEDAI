package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MedAITint
import com.example.ui.theme.MinTouch

/*
 * Small building blocks private to the MedAI Yordamchi screen.
 */

/** Four equal 48dp-tall segments, the selected one filled with brandSoft (same look as the Analytics tabs). */
@Composable
internal fun YordamchiTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val c = MedAITheme.colors
    Column(Modifier.fillMaxWidth().background(c.surface)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            labels.forEachIndexed { index, label ->
                val isSelected = index == selected
                val bg by animateColorAsState(if (isSelected) c.brandSoft else androidx.compose.ui.graphics.Color.Transparent, tween(180), label = "yordamchiTabBg")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = MinTouch)
                        .clip(RoundedCornerShape(MedAICorners.control))
                        .background(bg)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) c.onBrandSoft else c.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
    }
}

/** Icon disc + title (+ optional subtitle): the header of every card on this screen. */
@Composable
internal fun YordamchiCardTitle(icon: ImageVector, title: String, tint: MedAITint, subtitle: String? = null) {
    val c = MedAITheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(tint.bg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
    }
}

/**
 * Text input in the MedAITextField look, plus what the kit field cannot do: IME [keyboardActions],
 * a multi-line minimum height, an optional label and a trailing slot. Used where the original
 * screen relied on the keyboard's Search action or on a multi-line box.
 */
@Composable
internal fun YordamchiField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val c = MedAITheme.colors
    var focused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(if (focused) c.brand else c.borderStrong, tween(150), label = "yfBorder")
    val shape = RoundedCornerShape(MedAICorners.control)
    Column(modifier) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = c.textPrimary)
            Spacer(Modifier.height(6.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textPrimary),
            cursorBrush = SolidColor(c.brand),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                        .clip(shape)
                        .background(c.surface)
                        .border(if (focused) 2.dp else 1.dp, borderColor, shape)
                        .padding(start = 14.dp, end = if (trailingContent != null) 2.dp else 14.dp),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            leadingIcon, contentDescription = null, tint = c.textSecondary,
                            modifier = Modifier.padding(top = if (singleLine) 0.dp else 16.dp).size(22.dp).then(Modifier),
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Box(Modifier.weight(1f).padding(vertical = 14.dp)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
                        }
                        inner()
                    }
                    if (trailingContent != null) trailingContent()
                }
            },
        )
    }
}

/** Material Switch is only 32dp tall; wrapping it makes the real, labelled tap target 48dp. */
@Composable
internal fun YordamchiSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, description: String) {
    val c = MedAITheme.colors
    Box(
        Modifier
            .size(width = 56.dp, height = MinTouch)
            .semantics { contentDescription = description }
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        contentAlignment = Alignment.Center,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedThumbColor = c.onBrand, checkedTrackColor = c.brand),
        )
    }
}

/** Outlined danger action (reset statistics). 52dp tall, same shape as the kit buttons. */
@Composable
internal fun YordamchiDangerOutlineButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = MedAITheme.colors
    val shape = RoundedCornerShape(MedAICorners.control)
    Box(
        modifier
            .heightIn(min = 52.dp)
            .clip(shape)
            .border(1.5.dp, c.danger, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = c.danger, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = c.danger, textAlign = TextAlign.Center)
        }
    }
}
