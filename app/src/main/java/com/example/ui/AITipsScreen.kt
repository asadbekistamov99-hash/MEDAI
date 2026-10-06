@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.Translations
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MedAITint

// --- SCREEN: AI PERSONALIZED DAILY HEALTH TIPS ---

private data class TipKind(val code: String, val icon: ImageVector, val tint: MedAITint, val title: String)

@Composable
fun AITipsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val tips by viewModel.aiTipsText.collectAsState()
    val isLoading by viewModel.isLoadingTips.collectAsState()

    var selectedTab by remember { mutableStateOf("nutrition") }

    LaunchedEffect(key1 = true) {
        if (tips.isEmpty()) {
            viewModel.fetchPersonalizedTips()
        }
    }

    val kinds = listOf(
        TipKind("nutrition", Icons.Default.Restaurant, c.tintPeach, Translations.getString("tab_nutrition", lang)),
        TipKind("activity", Icons.Default.DirectionsRun, c.tintTeal, Translations.getString("tab_activity", lang)),
        TipKind("sleep", Icons.Default.Bedtime, c.tintViolet, Translations.getString("tab_sleep", lang)),
        TipKind("mental", Icons.Default.SelfImprovement, c.tintSky, Translations.getString("tab_mental", lang)),
    )
    val loadingText = medText(lang, "Maslahat tayyorlanmoqda...", "Готовим совет...", "Preparing your tip...")

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("ai_tips_title", lang), onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // What the tips are based on
            MedAIInfoBanner(
                text = medText(
                    lang,
                    "AI shaxsiy parametrlaringiz (bo'y, vazn, jins) asosida maslahat beradi.",
                    "AI даёт советы с учётом ваших параметров (рост, вес, пол).",
                    "AI tailors advice to your profile (height, weight, gender).",
                ),
                tone = MedAITone.Info,
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                kinds.forEach { k ->
                    MedAIFilterChip(text = k.title, selected = selectedTab == k.code, onClick = { selectedTab = k.code })
                }
            }

            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MedAISpinner(size = 22.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(loadingText, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                }
                repeat(3) {
                    MedAISkeleton(Modifier.fillMaxWidth().height(96.dp), animated = false)
                }
            } else {
                val selected = kinds.first { it.code == selectedTab }
                // Selected tip, in full.
                TipCard(selected, tips[selected.code] ?: loadingText, full = true, onClick = null)

                val others = kinds.filter { it.code != selectedTab && tips[it.code] != null }
                if (others.isNotEmpty()) {
                    MedSectionTitle(medText(lang, "Boshqa maslahatlar", "Другие советы", "More tips"))
                    others.forEach { k ->
                        TipCard(k, tips[k.code].orEmpty(), full = false, onClick = { selectedTab = k.code })
                    }
                }
            }

            MedAISecondaryButton(
                text = Translations.getString("ai_tips_refresh", lang),
                onClick = { viewModel.fetchPersonalizedTips() },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Refresh,
            )
        }
    }
}

@Composable
private fun TipCard(kind: TipKind, body: String, full: Boolean, onClick: (() -> Unit)?) {
    val c = MedAITheme.colors
    MedAICard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MedIconDisc(kind.icon, kind.tint.bg, kind.tint.fg, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Text(kind.title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = body,
            style = if (full) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            color = if (full) c.textPrimary else c.textSecondary,
            maxLines = if (full) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
