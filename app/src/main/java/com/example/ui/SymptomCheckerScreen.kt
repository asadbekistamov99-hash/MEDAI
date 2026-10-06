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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.i18n.Translations
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.MedAITheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// --- SCREEN: SYMPTOM CHECKER ---

@Composable
fun SymptomCheckerScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val isChecking by viewModel.isCheckingSymptoms.collectAsState()
    val resultText by viewModel.symptomResultText.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val checks by viewModel.symptomChecks.collectAsState()

    var symptomsText by remember { mutableStateOf("") }
    var selectedBodyPart by remember { mutableStateOf("General") }
    var selectedDurationCode by remember { mutableStateOf("Just today") }
    var selectedSeverityCode by remember { mutableStateOf("Mild") }

    // (label shown to the user, code sent to the ViewModel)
    val durationOptions = remember(lang) {
        when (lang) {
            "uz" -> listOf("Bugun" to "Just today", "1-3 kun" to "1-3 days", "Bir hafta" to "About a week", "Ko'p vaqt" to "More than a week")
            "ru" -> listOf("Сегодня" to "Just today", "1-3 дня" to "1-3 days", "Около недели" to "About a week", "Более недели" to "More than a week")
            else -> listOf("Just today" to "Just today", "1-3 days" to "1-3 days", "About a week" to "About a week", "More than a week" to "More than a week")
        }
    }
    val severityOptions = remember(lang) {
        when (lang) {
            "uz" -> listOf("Yengil" to "Mild", "O'rtacha" to "Moderate", "Og'ir" to "Severe")
            "ru" -> listOf("Лёгкая" to "Mild", "Умеренная" to "Moderate", "Тяжёлая" to "Severe")
            else -> listOf("Mild" to "Mild", "Moderate" to "Moderate", "Severe" to "Severe")
        }
    }
    val bodyAreas = remember(lang) {
        listOf(
            "Head" to medText(lang, "Bosh", "Голова", "Head"),
            "Chest" to medText(lang, "Ko'krak", "Грудь", "Chest"),
            "Abdomen" to medText(lang, "Qorin", "Живот", "Abdomen"),
            "Limbs" to medText(lang, "Qo'l/Oyoq", "Руки/ноги", "Limbs"),
            "General" to medText(lang, "Umumiy", "Общее", "General"),
        )
    }
    // (label, body-area code)
    val quickSymptoms = remember(lang) {
        listOf(
            medText(lang, "Bosh og'riq", "Головная боль", "Headache") to "Head",
            medText(lang, "Harorat", "Температура", "Fever") to "Head",
            medText(lang, "Yo'tal", "Кашель", "Cough") to "Chest",
            medText(lang, "Qorin og'riq", "Боль в животе", "Stomach ache") to "Abdomen",
            medText(lang, "Charchoq", "Усталость", "Fatigue") to "General",
            medText(lang, "Ko'ngil aynish", "Тошнота", "Nausea") to "Abdomen",
        )
    }

    val lastFive = remember(checks) { checks.take(5) }
    val insightText = remember(checks, lang) {
        if (checks.isEmpty()) ""
        else {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            val thisMonthChecks = checks.filter { check ->
                val checkCal = Calendar.getInstance().apply { timeInMillis = check.timestamp }
                checkCal.get(Calendar.MONTH) == currentMonth && checkCal.get(Calendar.YEAR) == currentYear
            }
            if (thisMonthChecks.isNotEmpty()) {
                val topSymptom = thisMonthChecks.groupBy { it.symptomsInput.lowercase().trim() }
                    .maxByOrNull { it.value.size }
                if (topSymptom != null) {
                    val count = topSymptom.value.size
                    val symptomName = topSymptom.key
                    when (lang) {
                        "uz" -> "Bu oy $count marta \"$symptomName\" tekshirdingiz"
                        "ru" -> "В этом месяце вы проверили \"$symptomName\" $count раз"
                        else -> "You checked \"$symptomName\" $count times this month"
                    }
                } else ""
            } else ""
        }
    }

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("feat_symptoms", lang), onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Always-visible, soft medical disclaimer.
            MedAIInfoBanner(
                text = Translations.getString("disclaimer_title", lang) + " " + Translations.getString("disclaimer_desc", lang),
                tone = MedAITone.Warning,
            )

            // Step 1: symptoms
            MedSectionTitle(medText(lang, "1. Belgilarni kiriting", "1. Введите симптомы", "1. Enter symptoms"))
            MedAITextField(
                value = symptomsText,
                onValueChange = { symptomsText = it },
                label = medText(lang, "Belgilar", "Симптомы", "Symptoms"),
                placeholder = Translations.getString("symptom_input_placeholder", lang),
                singleLine = false,
                trailingContent = {
                    MedIconButton(
                        Icons.Default.Search, Translations.getString("check_now", lang), tint = c.brand,
                        onClick = { viewModel.checkSymptoms(symptomsText, selectedBodyPart, selectedDurationCode, selectedSeverityCode) },
                    )
                },
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                quickSymptoms.forEach { (label, part) ->
                    val isSelected = symptomsText.contains(label)
                    MedAIFilterChip(
                        text = label,
                        selected = isSelected,
                        onClick = {
                            symptomsText = if (isSelected) {
                                symptomsText.replace(label, "").trim().trim(',').trim()
                            } else {
                                if (symptomsText.isEmpty()) label else "$symptomsText, $label"
                            }
                            selectedBodyPart = part
                        },
                    )
                }
            }

            // Step 2: body area
            MedSectionTitle(medText(lang, "2. Tana sohasini tanlang", "2. Выберите область тела", "2. Select body area"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                bodyAreas.forEach { (code, label) ->
                    MedAIFilterChip(text = label, selected = selectedBodyPart == code, onClick = { selectedBodyPart = code })
                }
            }

            // Step 3: duration
            MedSectionTitle(medText(lang, "3. Belgilar davomiyligi", "3. Продолжительность симптомов", "3. How long"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                durationOptions.forEach { (display, code) ->
                    MedAIFilterChip(text = display, selected = selectedDurationCode == code, onClick = { selectedDurationCode = code })
                }
            }

            // Step 4: severity
            MedSectionTitle(medText(lang, "4. Belgilar og'irligi", "4. Тяжесть симптомов", "4. How severe"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                severityOptions.forEach { (display, code) ->
                    MedAIFilterChip(text = display, selected = selectedSeverityCode == code, onClick = { selectedSeverityCode = code })
                }
            }

            // Premium upsell (same condition as before)
            if (user?.isPremium == false) {
                val shape = RoundedCornerShape(MedAICorners.control)
                Row(
                    Modifier.fillMaxWidth().clip(shape).background(c.premiumSoft).border(1.dp, c.premium.copy(alpha = 0.3f), shape).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = c.onPremiumSoft)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        Translations.getString("premium_upsell_banner", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.onPremiumSoft,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Recent checks
            if (lastFive.isNotEmpty()) {
                MedSectionTitle(medText(lang, "Oxirgi tekshiruvlar", "Последние проверки", "Recent checks"))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    lastFive.forEach { check ->
                        val formattedDate = SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(check.timestamp))
                        val cleanInput = check.symptomsInput.substringBefore("[").trim()
                        MedAIFilterChip(
                            text = "$formattedDate: ${cleanInput.take(12)}",
                            selected = false,
                            onClick = {
                                symptomsText = cleanInput
                                selectedBodyPart = check.bodyPart
                                viewModel.checkSymptoms(cleanInput, check.bodyPart, selectedDurationCode, selectedSeverityCode)
                            },
                        )
                    }
                }
            }

            if (insightText.isNotEmpty()) {
                MedAIInfoBanner(text = insightText, tone = MedAITone.Info)
            }

            // The one primary action
            MedAIPrimaryButton(
                text = Translations.getString("check_now", lang),
                onClick = { viewModel.checkSymptoms(symptomsText, selectedBodyPart, selectedDurationCode, selectedSeverityCode) },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.MedicalServices,
                enabled = symptomsText.isNotEmpty() && !isChecking,
                loading = isChecking,
            )

            // Results
            if (resultText.isNotEmpty()) {
                val matchedAllergies = remember(resultText) { viewModel.checkMedicinesForAllergies(resultText) }
                if (matchedAllergies.isNotEmpty()) {
                    MedAIInfoBanner(
                        text = medText(
                            lang,
                            "Diqqat! Bu dori sizga allergiya qilishi mumkin: ",
                            "Внимание! Это лекарство может вызвать аллергию: ",
                            "Warning! This medicine may cause an allergy: ",
                        ) + matchedAllergies.joinToString(", "),
                        tone = MedAITone.Error,
                    )
                }

                val dispArea = bodyAreas.find { it.first == selectedBodyPart }?.second ?: selectedBodyPart
                val dispDuration = durationOptions.find { it.second == selectedDurationCode }?.first ?: selectedDurationCode
                val dispSeverity = severityOptions.find { it.second == selectedSeverityCode }?.first ?: selectedSeverityCode
                val severityTone = when (selectedSeverityCode) {
                    "Mild" -> MedAIBadgeTone.Success
                    "Moderate" -> MedAIBadgeTone.Warning
                    else -> MedAIBadgeTone.Danger
                }

                MedAICard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MedIconDisc(Icons.Default.Assessment, c.tintTeal.bg, c.tintTeal.fg, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            medText(lang, "Tahlil natijalari", "Результаты анализа", "Analysis results"),
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MedAIBadge(dispArea, MedAIBadgeTone.Info)
                        MedAIBadge(dispDuration, MedAIBadgeTone.Brand)
                        MedAIBadge(dispSeverity, severityTone)
                    }
                    Spacer(Modifier.height(12.dp))
                    MedHairline()
                    Spacer(Modifier.height(12.dp))
                    RichMarkdownText(text = resultText)
                }

                MedAIInfoBanner(
                    text = Translations.getString("disclaimer_title", lang) + " " + Translations.getString("disclaimer_desc", lang),
                    tone = MedAITone.Warning,
                )
            }
        }
    }
}
