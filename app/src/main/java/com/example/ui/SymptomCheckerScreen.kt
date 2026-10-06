@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Translations
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.ui.theme.*
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

// --- SCREEN: SYMPTOM CHECKER ---

@Composable
fun SymptomCheckerScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val isChecking by viewModel.isCheckingSymptoms.collectAsState()
    val resultText by viewModel.symptomResultText.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    var symptomsText by remember { mutableStateOf("") }
    var selectedBodyPart by remember { mutableStateOf("General") }

    val durationOptions = when (lang) {
        "uz" -> listOf("Bugun" to "Just today", "1-3 kun" to "1-3 days", "Bir hafta" to "About a week", "Ko'p vaqt" to "More than a week")
        "ru" -> listOf("Сегодня" to "Just today", "1-3 дня" to "1-3 days", "Около недели" to "About a week", "Более недели" to "More than a week")
        else -> listOf("Just today" to "Just today", "1-3 days" to "1-3 days", "About a week" to "About a week", "More than a week" to "More than a week")
    }

    val severityOptions = when (lang) {
        "uz" -> listOf("Yengil" to "Mild", "O'rtacha" to "Moderate", "Og'ir" to "Severe")
        "ru" -> listOf("Легкая" to "Mild", "Умеренная" to "Moderate", "Тяжелая" to "Severe")
        else -> listOf("Mild" to "Mild", "Moderate" to "Moderate", "Severe" to "Severe")
    }

    var selectedDurationCode by remember { mutableStateOf("Just today") }
    var selectedSeverityCode by remember { mutableStateOf("Mild") }

    val quickSymptoms = listOf(
        "Bosh og'riq" to "Head", "Harorat" to "Head", "Yo'tal" to "Chest",
        "Qorin og'riq" to "Abdomen", "Charchoq" to "General", "Ko'ngil aynish" to "Abdomen"
    )

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("feat_symptoms", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(medai.canvas)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Symptoms Search/Input
            Text(
                text = (if (lang == "uz") "1. Belgilarni kiriting" else if (lang == "ru") "1. Введите симптомы" else "1. Enter Symptoms").uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = medai.brand,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = symptomsText,
                onValueChange = { symptomsText = it },
                placeholder = { Text(Translations.getString("symptom_input_placeholder", lang), color = medai.textSecondary.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = medai.textPrimary, fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8FFFE),
                    unfocusedContainerColor = Color(0xFFF8FFFE),
                    focusedBorderColor = medai.brand,
                    unfocusedBorderColor = medai.border,
                    focusedTextColor = medai.textPrimary,
                    unfocusedTextColor = medai.textPrimary
                ),
                trailingIcon = {
                    IconButton(onClick = { viewModel.checkSymptoms(symptomsText, selectedBodyPart, selectedDurationCode, selectedSeverityCode) }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = medai.brand)
                    }
                }
            )

            // Symptom Quick Select Chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickSymptoms.forEach { (label, part) ->
                    val isSelected = symptomsText.contains(label)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            symptomsText = if (isSelected) {
                                symptomsText.replace(label, "").trim().trim(',').trim()
                            } else {
                                if (symptomsText.isEmpty()) label else "$symptomsText, $label"
                            }
                            selectedBodyPart = part
                        },
                        label = { Text(label) }
                    )
                }
            }

            // Section 2: Body Area Map Picker Layout
            Text(
                text = (if (lang == "uz") "2. Tana sohasini tanlang" else if (lang == "ru") "2. Выберите область тела" else "2. Select Body Area").uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = medai.brand,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Head" to "Bosh", "Chest" to "Ko'krak", "Abdomen" to "Qorin", "Limbs" to "Qo'l/Oyoq", "General" to "Umumiy").forEach { (code, label) ->
                    val selected = selectedBodyPart == code
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) medai.brand else Color.White)
                            .border(1.dp, if (selected) medai.brand else medai.border, RoundedCornerShape(14.dp))
                            .clickable { selectedBodyPart = code }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) Color.White else medai.textSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Section 3: Duration
            Text(
                text = (if (lang == "uz") "3. Belgilar davomiyligi" else if (lang == "ru") "3. Продолжительность симптомов" else "3. Duration of Symptoms").uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = medai.brand,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durationOptions.forEach { (display, code) ->
                    val isSelected = selectedDurationCode == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDurationCode = code },
                        label = { Text(display) }
                    )
                }
            }

            // Section 4: Severity
            Text(
                text = (if (lang == "uz") "4. Belgilar og'irligi" else if (lang == "ru") "4. Тяжесть симптомов" else "4. Symptom Severity").uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = medai.brand,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                severityOptions.forEach { (display, code) ->
                    val isSelected = selectedSeverityCode == code
                    val dotColor = when (code) {
                        "Mild" -> medai.success
                        "Moderate" -> medai.warning
                        else -> medai.danger
                    }
                    val selectedBgColor = when (code) {
                        "Mild" -> medai.success.copy(alpha = 0.12f)
                        "Moderate" -> medai.warning.copy(alpha = 0.12f)
                        else -> medai.danger.copy(alpha = 0.12f)
                    }
                    val borderColor = if (isSelected) dotColor else medai.border
                    val bgColor = if (isSelected) selectedBgColor else Color.White

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable { selectedSeverityCode = code }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                            Text(
                                text = display,
                                color = if (isSelected) dotColor else medai.textSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Premium upsell warning
            if (user?.isPremium == false) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = medai.premium.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = medai.premium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = Translations.getString("premium_upsell_banner", lang), fontSize = 11.sp, color = medai.premium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Disease History & Insights Section
            val checks by viewModel.symptomChecks.collectAsState()
            val lastFive = remember(checks) { checks.take(5) }
            val insightText = remember(checks) {
                if (checks.isEmpty()) ""
                else {
                    val calendar = Calendar.getInstance()
                    val currentMonth = calendar.get(Calendar.MONTH)
                    val currentYear = calendar.get(Calendar.YEAR)
                    
                    val thisMonthChecks = checks.filter { c ->
                        val checkCal = Calendar.getInstance().apply { timeInMillis = c.timestamp }
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

            if (lastFive.isNotEmpty()) {
                Text(
                    text = if (lang == "uz") "Oxirgi tekshiruvlar" else if (lang == "ru") "Последние проверки" else "Recent Checks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = medai.textPrimary
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    lastFive.forEach { check ->
                        val formattedDate = SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(check.timestamp))
                        val cleanInput = check.symptomsInput.substringBefore("[").trim()
                        SuggestionChip(
                            onClick = {
                                symptomsText = cleanInput
                                selectedBodyPart = check.bodyPart
                                viewModel.checkSymptoms(cleanInput, check.bodyPart, selectedDurationCode, selectedSeverityCode)
                            },
                            label = { Text("$formattedDate: ${cleanInput.take(12)}") }
                        )
                    }
                }
            }

            if (insightText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = medai.brandSoft.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = medai.brand)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = insightText, fontSize = 13.sp, color = medai.textPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Check button
            Button(
                onClick = { viewModel.checkSymptoms(symptomsText, selectedBodyPart, selectedDurationCode, selectedSeverityCode) },
                enabled = symptomsText.isNotEmpty() && !isChecking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
            ) {
                if (isChecking) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null)
                        Text(text = Translations.getString("check_now", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Results Screen
            if (resultText.isNotEmpty()) {
                val matchedAllergies = remember(resultText) { viewModel.checkMedicinesForAllergies(resultText) }
                if (matchedAllergies.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = medai.danger.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, medai.danger.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = medai.danger)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (lang == "uz") "⚠️ Diqqat! Bu dori sizga allergiya qilishi mumkin:"
                                           else if (lang == "ru") "⚠️ Внимание! Это лекарство может вызвать аллергию:"
                                           else "⚠️ Warning! This medicine may cause an allergy:",
                                    color = medai.danger,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = matchedAllergies.joinToString(", "),
                                    color = medai.danger,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = medai.brand.copy(alpha = 0.15f), spotColor = medai.brand.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = medai.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, medai.border)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).background(medai.brand.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = medai.brand, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = if (lang == "uz") "Tahlil natijalari" else if (lang == "ru") "Результаты анализа" else "Analysis Results",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = medai.textPrimary
                            )
                        }

                        Divider(color = medai.border)

                        // Selected Metadata summary for context
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val dispDuration = durationOptions.find { it.second == selectedDurationCode }?.first ?: selectedDurationCode
                            val dispSeverity = severityOptions.find { it.second == selectedSeverityCode }?.first ?: selectedSeverityCode
                            
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Area: $selectedBodyPart") }
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(dispDuration) }
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(dispSeverity) }
                            )
                        }

                        RichMarkdownText(text = resultText)

                        // Disclaimer
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardColors(
                                containerColor = medai.danger.copy(alpha = 0.05f),
                                contentColor = medai.textSecondary,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, medai.danger.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = Translations.getString("disclaimer_title", lang), color = medai.danger, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = Translations.getString("disclaimer_desc", lang), color = medai.textSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
