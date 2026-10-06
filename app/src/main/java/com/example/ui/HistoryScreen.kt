package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.Translations
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MinTouch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- SCREEN: HISTORY (past symptom checks) ---

@Composable
fun HistoryScreen(viewModel: AppViewModel) {
    val c = MedAITheme.colors
    val lang by viewModel.currentLanguage.collectAsState()
    val checks by viewModel.symptomChecks.collectAsState()
    val dateFormat = remember(lang) { SimpleDateFormat("d MMM yyyy, HH:mm", Locale(lang)) }
    val deleteLabel = supportText(lang, "O'chirish", "Удалить", "Delete")

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("tab_history", lang)) },
    ) { innerPadding ->
        if (checks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                MedAIEmptyState(
                    title = supportText(lang, "Tarix bo'sh", "История пуста", "No history yet"),
                    message = supportText(
                        lang,
                        "Simptom tekshiruvi natijalari shu yerda saqlanadi.",
                        "Результаты проверки симптомов будут сохраняться здесь.",
                        "Your symptom check results will be saved here.",
                    ),
                    icon = Icons.Default.History,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(checks, key = { it.id }) { check ->
                    // Tapping a card toggles between the 3-line preview and the full AI answer.
                    var expanded by rememberSaveable(check.id) { mutableStateOf(false) }
                    MedAICard(onClick = { expanded = !expanded }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp).clip(CircleShape).background(c.tintTeal.bg),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = c.tintTeal.fg, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    check.bodyPart,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = c.textPrimary,
                                    maxLines = 2,
                                )
                                Text(
                                    dateFormat.format(Date(check.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textSecondary,
                                )
                            }
                            Box(
                                Modifier
                                    .size(MinTouch)
                                    .clip(CircleShape)
                                    .clickable(role = Role.Button) { viewModel.deleteSymptomCheck(check.id) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = deleteLabel, tint = c.danger)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            check.symptomsInput,
                            style = MaterialTheme.typography.titleSmall,
                            color = c.textPrimary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            check.resultJson,
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary,
                            maxLines = if (expanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
