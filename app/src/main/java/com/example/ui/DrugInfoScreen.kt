@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import com.example.i18n.Translations
import com.example.ui.theme.MedAITheme

// --- SCREEN: DRUG INFORMATION ---

/** Which result sections are cautions, so they get a badge. Keys come from the ViewModel. */
private fun sectionBadge(key: String): MedAIBadgeTone? = when (key) {
    "SideEffects", "Interactions", "PregnancySafety" -> MedAIBadgeTone.Warning
    "Contraindications" -> MedAIBadgeTone.Danger
    else -> null
}

@Composable
fun DrugInfoScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val drugResult by viewModel.drugInfoResult.collectAsState()
    val isLoading by viewModel.isLoadingDrug.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Prescription Scanner state
    var showScannerDialog by remember { mutableStateOf(false) }
    val isScanningPrescription by viewModel.isScanningPrescription.collectAsState()
    val prescriptionScanResult by viewModel.prescriptionScanResult.collectAsState()

    // Drug Interaction state
    var showInteractionDialog by remember { mutableStateOf(false) }
    val isCheckingInteractions by viewModel.isCheckingInteractions.collectAsState()
    val interactionResult by viewModel.interactionResult.collectAsState()
    var drugInputs by remember { mutableStateOf(listOf("", "")) }

    // Tiny valid 1x1 base64 JPEG image to make actual multi-modal API calls
    val mockPrescriptionBase64 = "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA="

    val sectionTitles = remember(lang) {
        mapOf(
            "Dosage" to medText(lang, "Dozalash", "Дозировка", "Dosage"),
            "SideEffects" to medText(lang, "Yon ta'sirlari", "Побочные эффекты", "Side effects"),
            "Interactions" to medText(lang, "O'zaro ta'sir", "Взаимодействия", "Interactions"),
            "Contraindications" to medText(lang, "Qarshi ko'rsatmalar", "Противопоказания", "Contraindications"),
            "Alternatives" to medText(lang, "Muqobillar", "Аналоги", "Alternatives"),
            "PregnancySafety" to medText(lang, "Homiladorlikda xavfsizligi", "Безопасность при беременности", "Pregnancy safety"),
            "Storage" to medText(lang, "Saqlash", "Хранение", "Storage"),
            "Description" to medText(lang, "Tavsif", "Описание", "Description"),
        )
    }

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("feat_med_info", lang), onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Search
            MedAITextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = medText(lang, "Dori nomi", "Название лекарства", "Medicine name"),
                placeholder = Translations.getString("drug_search_placeholder", lang),
                leadingIcon = Icons.Default.Search,
                trailingContent = {
                    MedAITextButton(
                        text = medText(lang, "Qidirish", "Найти", "Search"),
                        onClick = { viewModel.searchDrugInfo(searchQuery) },
                    )
                },
            )

            // Recent / common searches
            MedSectionTitle(medText(lang, "Qidiruv tarixi", "История поиска", "Recent searches"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Paracetamol", "Ibuprofen", "Aspirin").forEach { item ->
                    MedAIFilterChip(
                        text = item,
                        selected = searchQuery == item,
                        onClick = {
                            searchQuery = item
                            viewModel.searchDrugInfo(item)
                        },
                    )
                }
            }

            // Tools: prescription scanner, interaction checker
            MedAICard(contentPadding = 0.dp, modifier = Modifier.fillMaxWidth()) {
                MedAIListRow(
                    icon = Icons.Default.PhotoCamera,
                    title = medText(lang, "Retseptni skanerlash", "Сканирование рецепта", "Scan prescription"),
                    subtitle = medText(lang, "Retseptdagi dorilar tahlili", "Разбор лекарств из рецепта", "Understand the medicines on a prescription"),
                    tint = c.tintTeal,
                    onClick = { showScannerDialog = true },
                )
                MedAIListRow(
                    icon = Icons.Default.CompareArrows,
                    title = medText(lang, "Dori o'zaro ta'siri", "Взаимодействие лекарств", "Drug interactions"),
                    subtitle = medText(lang, "2-5 ta dorini birga tekshirish", "Проверка 2-5 лекарств вместе", "Check 2-5 medicines together"),
                    tint = c.tintSky,
                    showDivider = false,
                    onClick = { showInteractionDialog = true },
                )
            }

            // Allergy check for the search result
            if (drugResult != null) {
                val matchedAllergies = remember(drugResult) {
                    val allText = searchQuery + " " + (drugResult?.values?.joinToString(" ") ?: "")
                    viewModel.checkMedicinesForAllergies(allText)
                }
                if (matchedAllergies.isNotEmpty()) {
                    val names = matchedAllergies.joinToString(", ")
                    MedAIInfoBanner(
                        text = medText(
                            lang,
                            "Bu dori sizga mos emas — $names allergiyangiz bor",
                            "Это лекарство вам не подходит — у вас аллергия на $names",
                            "This medicine is not suitable for you — you have an allergy to $names",
                        ),
                        tone = MedAITone.Error,
                    )
                } else {
                    MedAIInfoBanner(
                        text = medText(
                            lang,
                            "Bu dori allergiyangiz bilan mos keladi",
                            "Это лекарство совместимо с вашей аллергией",
                            "This medicine is compatible with your allergies",
                        ),
                        tone = MedAITone.Success,
                    )
                }
            }

            if (isLoading) {
                Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    MedAISpinner(size = 24.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        medText(lang, "Ma'lumot izlanmoqda...", "Идёт поиск...", "Searching..."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary,
                    )
                }
            } else if (drugResult != null) {
                drugResult?.forEach { (key, value) ->
                    MedAICard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = sectionTitles[key] ?: key,
                                style = MaterialTheme.typography.titleSmall,
                                color = c.textPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            val tone = sectionBadge(key)
                            if (tone != null) {
                                Spacer(Modifier.width(8.dp))
                                MedAIBadge(
                                    text = medText(lang, "Ehtiyot bo'ling", "Осторожно", "Caution"),
                                    tone = tone,
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                    }
                }
            }
        }
    }

    // --- DIALOG 1: PRESCRIPTION SCANNER ---
    if (showScannerDialog) {
        MedContentDialog(
            title = medText(lang, "Retseptni skanerlash", "Сканирование рецепта", "Scan prescription"),
            closeText = medText(lang, "Yopish", "Закрыть", "Close"),
            onDismiss = { showScannerDialog = false },
            icon = Icons.Default.PhotoCamera,
        ) {
            Text(
                text = medText(
                    lang,
                    "Skanerlashni simulyatsiya qilish uchun retsept turlardan birini tanlang va yuboring.",
                    "Выберите тип рецепта для симуляции сканирования.",
                    "Pick a prescription type to simulate a scan.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )

            val presets = listOf(
                medText(lang, "Kardiolog retsepti (Aspirin, Lisinopril)", "Рецепт кардиолога (аспирин, лизиноприл)", "Cardiologist prescription (Aspirin, Lisinopril)"),
                medText(lang, "Terapevt retsepti (Amoxicillin, Paracetamol)", "Рецепт терапевта (амоксициллин, парацетамол)", "Physician prescription (Amoxicillin, Paracetamol)"),
                medText(lang, "Nevrolog retsepti (Magniy B6, Glycine)", "Рецепт невролога (магний B6, глицин)", "Neurologist prescription (Magnesium B6, Glycine)"),
            )
            presets.forEach { title ->
                MedAISecondaryButton(
                    text = title,
                    onClick = { viewModel.scanPrescriptionImage(mockPrescriptionBase64) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (isScanningPrescription) {
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    MedAISpinner(size = 22.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(medText(lang, "Skanerlanmoqda...", "Сканирование...", "Scanning..."), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                }
            } else if (prescriptionScanResult.isNotEmpty()) {
                MedHairline()
                MedSectionTitle(medText(lang, "Natija", "Результат", "Result"))
                RichMarkdownText(text = prescriptionScanResult)
            }
        }
    }

    // --- DIALOG 2: DRUG INTERACTION CHECKER ---
    if (showInteractionDialog) {
        MedContentDialog(
            title = medText(lang, "Dori o'zaro ta'siri", "Взаимодействие лекарств", "Drug interactions"),
            closeText = medText(lang, "Yopish", "Закрыть", "Close"),
            onDismiss = { showInteractionDialog = false },
            icon = Icons.Default.CompareArrows,
        ) {
            Text(
                text = medText(
                    lang,
                    "Tekshirish uchun 2 dan 5 tagacha dori nomini yozing:",
                    "Введите от 2 до 5 лекарств для проверки:",
                    "Enter 2 to 5 medicines to check:",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )

            drugInputs.forEachIndexed { idx, value ->
                MedAITextField(
                    value = value,
                    onValueChange = { newVal ->
                        val list = drugInputs.toMutableList()
                        list[idx] = newVal
                        drugInputs = list
                    },
                    label = medText(lang, "Dori ${idx + 1}", "Лекарство ${idx + 1}", "Medicine ${idx + 1}"),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (drugInputs.size < 5) {
                MedAISecondaryButton(
                    text = medText(lang, "Dori qo'shish", "Добавить лекарство", "Add medicine"),
                    onClick = { drugInputs = drugInputs + "" },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Add,
                )
            }

            MedAIPrimaryButton(
                text = medText(lang, "O'zaro ta'sirni tekshirish", "Проверить взаимодействие", "Check interactions"),
                onClick = { viewModel.checkDrugInteractions(drugInputs) },
                modifier = Modifier.fillMaxWidth(),
                enabled = drugInputs.count { it.isNotBlank() } >= 2 && !isCheckingInteractions,
                loading = isCheckingInteractions,
            )

            if (interactionResult.isNotEmpty()) {
                MedHairline()

                val isSafe = interactionResult.contains("STATUS: SAFE")
                val isCaution = interactionResult.contains("STATUS: CAUTION")
                val isDangerous = interactionResult.contains("STATUS: DANGEROUS")

                when {
                    isSafe -> MedAIInfoBanner(medText(lang, "Xavfsiz", "Безопасно", "Safe"), tone = MedAITone.Success)
                    isCaution -> MedAIInfoBanner(medText(lang, "Ehtiyot bo'ling", "Будьте осторожны", "Use caution"), tone = MedAITone.Warning)
                    isDangerous -> MedAIInfoBanner(medText(lang, "Birga ichmang!", "Не принимайте вместе!", "Do not take together!"), tone = MedAITone.Error)
                    else -> MedSectionTitle(medText(lang, "Natija", "Результат", "Result"))
                }

                RichMarkdownText(
                    text = interactionResult
                        .replace("STATUS: SAFE", "")
                        .replace("STATUS: CAUTION", "")
                        .replace("STATUS: DANGEROUS", "")
                        .trim(),
                )
            }
        }
    }
}
