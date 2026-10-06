package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Translations
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MinTouch
import com.example.ui.theme.PremiumGradient
import com.example.ui.theme.PremiumPurple
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.Spacing
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange

/**
 * Banner shown to an account with a running free trial.
 *
 * The trial is the product's onboarding-to-paid conversion path, so it has to be visible and
 * honest about two things at once: how long is left, and what happens when it runs out. A
 * silent 7-day window that expires into a locked screen is how an app loses the user it just
 * spent a week educating.
 *
 * The countdown re-reads [daysRemaining] on every recomposition and the ViewModel recomputes
 * it from the stored trial end timestamp, so the number is derived from persisted state rather
 * than from a timer started when the banner was composed — a banner rebuilt after process
 * death still shows the truth.
 */
@Composable
fun TrialBanner(
    daysRemaining: Int,
    lang: String,
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (daysRemaining <= 0) return

    val c = MedAITheme.colors
    // A 1-day warning reads differently from a 6-day one: colour shifts to amber so the
    // urgency is visible without opening anything. Violet stays reserved for "paid".
    val isUrgent = daysRemaining <= 1
    val accent = if (isUrgent) c.warning else c.premium
    val soft = if (isUrgent) c.warningSoft else c.premiumSoft
    val onSoft = if (isUrgent) c.onWarningSoft else c.onPremiumSoft
    val onAccent = when {
        !isUrgent -> c.onPremium
        c.isDark -> c.warningSoft
        else -> Color.White
    }
    val shape = RoundedCornerShape(MedAICorners.card)

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { -it / 2 },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 2 },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(soft)
                .border(1.dp, accent.copy(alpha = 0.35f), shape)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Translations.getString("trial_banner_title", lang),
                        style = MaterialTheme.typography.titleSmall,
                        color = onSoft,
                    )
                    Text(
                        text = Translations.getString("trial_banner_days", lang)
                            .replace("{days}", daysRemaining.toString()),
                        style = MaterialTheme.typography.bodySmall,
                        color = onSoft,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Seven dots, one per trial day. A number tells you how long is left; a
                // depleting row tells you how much of the value is already gone.
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(7) { index ->
                        val used = index >= daysRemaining
                        Box(
                            modifier = Modifier
                                .size(width = 12.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (used) accent.copy(alpha = 0.25f) else accent)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .heightIn(min = MinTouch)
                        .clip(RoundedCornerShape(MedAICorners.pill))
                        .background(accent)
                        .clickable(role = Role.Button, onClick = onUpgradeClick)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Translations.getString("trial_banner_upgrade", lang),
                        style = MaterialTheme.typography.labelLarge,
                        color = onAccent,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * Full-screen paywall shown when a free user hits the daily limit, and from the upgrade route.
 *
 * States what the trial bought and what continuing costs, rather than just refusing the action —
 * the user already liked the app, the point of this screen is to convert that, not to block.
 */
@Composable
fun TrialEndedUpsell(
    lang: String,
    onUpgradeClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PremiumGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = Translations.getString("trial_upgrade_title", lang),
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Text(
            text = Translations.getString("trial_upgrade_body", lang),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        MedAIButton(
            text = Translations.getString("trial_banner_upgrade", lang),
            onClick = onUpgradeClick,
            icon = Icons.Default.WorkspacePremium,
            brush = PremiumGradient
        )

        androidx.compose.material3.TextButton(onClick = onBack) {
            Text(Translations.getString("trial_ended_body", lang), color = TextSecondary)
        }
    }
}
