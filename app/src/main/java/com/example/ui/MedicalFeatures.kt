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

@Composable
fun RichMarkdownText(text: String, modifier: Modifier = Modifier) {
    val medai = MedAITheme.colors

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val lines = text.split("\n")
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
            } else if (trimmed.startsWith("###") || trimmed.startsWith("##") || trimmed.startsWith("#")) {
                val headerText = trimmed.replace(Regex("^#+\\s*"), "").replace("**", "").replace("`", "")
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = medai.brand,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            } else if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                val bulletText = trimmed.substring(1).trim().replace("**", "").replace("`", "")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "•", style = MaterialTheme.typography.bodyMedium, color = medai.brand, fontWeight = FontWeight.Bold)
                    Text(
                        text = bulletText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = medai.textPrimary,
                        lineHeight = 20.sp
                    )
                }
            } else {
                // If it contains inline bold markers e.g. **text**
                val annotatedString = remember(trimmed) {
                    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
                    val parts = trimmed.split("**")
                    parts.forEachIndexed { index, part ->
                        if (index % 2 == 1) {
                            builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = medai.brand))
                            builder.append(part)
                            builder.pop()
                        } else {
                            builder.append(part)
                        }
                    }
                    builder.toAnnotatedString()
                }
                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = medai.textPrimary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// --- SCREEN: AI DOCTOR CHAT ---

@Composable
fun AIDoctorScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val chatMessages by viewModel.doctorChatMessages().collectAsState(initial = emptyList())

    var messageText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppHeader(
                title = Translations.getString("feat_ai_doctor", lang),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.clearChatHistory("doctor") }) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Clear Chat", tint = medai.textSecondary)
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Dori, kasallik yoki tahlil haqida so'rang...", color = medai.textSecondary.copy(alpha = 0.6f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = medai.textPrimary, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FFFE),
                            unfocusedContainerColor = Color(0xFFF8FFFE),
                            focusedBorderColor = medai.premium,
                            unfocusedBorderColor = medai.border
                        )
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotEmpty()) {
                                if (viewModel.sendChatMessage(messageText, "doctor")) {
                                    messageText = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .background(Brush.horizontalGradient(listOf(medai.premium, Color(0xFF5E35B1))), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(medai.canvas)
        ) {
            // Quick Chat Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Translations.getString("chat_chip_med", lang),
                    Translations.getString("chat_chip_disease", lang),
                    Translations.getString("chat_chip_lab", lang),
                    Translations.getString("chat_chip_diet", lang)
                ).forEach { chip ->
                    FilterChip(
                        selected = false,
                        onClick = { messageText = chip },
                        label = { Text(chip) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = medai.surface,
                            labelColor = medai.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = medai.border
                        )
                    )
                }
            }

            if (chatMessages.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(72.dp).background(medai.premiumSoft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = medai.premium, modifier = Modifier.size(34.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (lang) {
                                "uz" -> "AI Shifokoringiz tinglashga tayyor"
                                "ru" -> "Ваш AI-врач готов вас выслушать"
                                else -> "Your AI Doctor is ready to help"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = medai.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (lang) {
                                "uz" -> "Savolingizni yozing yoki yuqoridagi tugmalardan tanlang"
                                "ru" -> "Напишите вопрос или выберите один из чипов выше"
                                else -> "Type your question or pick a chip above"
                            },
                            fontSize = 12.sp,
                            color = medai.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isUser = msg.role == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) medai.premium else Color.White
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                border = if (isUser) null else BorderStroke(1.dp, medai.border),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 0.dp else 1.dp),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = msg.content,
                                    color = if (isUser) Color.White else medai.textPrimary,
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 14.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN: AI PERSONALIZED DAILY HEALTH TIPS ---

@Composable
fun AITipsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val tips by viewModel.aiTipsText.collectAsState()
    val isLoading by viewModel.isLoadingTips.collectAsState()

    var selectedTab by remember { mutableStateOf("nutrition") }

    LaunchedEffect(key1 = true) {
        if (tips.isEmpty()) {
            viewModel.fetchPersonalizedTips()
        }
    }

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("ai_tips_title", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(medai.canvas)
        ) {
            // personalized user details card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = medai.surface),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, medai.border)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(medai.brand.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = medai.brand, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Shaxsiy tavsiya tahlili", fontWeight = FontWeight.Bold, color = medai.textPrimary, fontSize = 14.sp)
                        Text(text = "AI shaxsiy parametrlaringiz (Bo'y, vazn, jins) asosida maslahat beradi", fontSize = 11.sp, color = medai.textSecondary)
                    }
                }
            }

            // Advice Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "nutrition" to Translations.getString("tab_nutrition", lang),
                    "activity" to Translations.getString("tab_activity", lang),
                    "sleep" to Translations.getString("tab_sleep", lang),
                    "mental" to Translations.getString("tab_mental", lang)
                ).forEach { (code, label) ->
                    val isSelected = selectedTab == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = code },
                        label = { Text(label, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = medai.surface,
                            labelColor = medai.textSecondary,
                            selectedContainerColor = medai.brand,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = medai.border,
                            selectedBorderColor = medai.brand
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = medai.brand)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (lang) {
                                "uz" -> "Maslahat tayyorlanmoqda..."
                                "ru" -> "Готовим совет..."
                                else -> "Preparing your tip..."
                            },
                            fontSize = 12.sp,
                            color = medai.textSecondary
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = medai.brand.copy(alpha = 0.12f), spotColor = medai.brand.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = medai.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, medai.border)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(medai.brand.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (selectedTab) {
                                    "nutrition" -> Icons.Default.Restaurant
                                    "activity" -> Icons.Default.DirectionsRun
                                    "sleep" -> Icons.Default.Bedtime
                                    else -> Icons.Default.SelfImprovement
                                }
                                Icon(imageVector = icon, contentDescription = null, tint = medai.brand, modifier = Modifier.size(32.dp))
                            }

                            val tipContent = tips[selectedTab] ?: "Yuklanmoqda..."
                            Text(
                                text = tipContent,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = medai.textPrimary,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.fetchPersonalizedTips() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = medai.brand, contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = Translations.getString("ai_tips_refresh", lang), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- SCREEN: DRUG INFORMATION ---

@Composable
fun DrugInfoScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

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

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("feat_med_info", lang), onBack = onBack) }
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Translations.getString("drug_search_placeholder", lang), color = medai.textSecondary.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = medai.textPrimary, fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8FFFE),
                    unfocusedContainerColor = Color(0xFFF8FFFE),
                    focusedBorderColor = medai.info,
                    unfocusedBorderColor = medai.border,
                    focusedTextColor = medai.textPrimary,
                    unfocusedTextColor = medai.textPrimary
                ),
                trailingIcon = {
                    IconButton(onClick = { viewModel.searchDrugInfo(searchQuery) }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = medai.info)
                    }
                }
            )

            // Search History & Quick Tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Qidiruv tarixi".uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = medai.textSecondary,
                    letterSpacing = 1.sp
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Prescription Scanner Trigger
                    IconButton(
                        onClick = { showScannerDialog = true },
                        modifier = Modifier
                            .background(medai.brand.copy(alpha = 0.15f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Scan Prescription",
                            tint = medai.brand,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Interaction Checker Trigger
                    IconButton(
                        onClick = { showInteractionDialog = true },
                        modifier = Modifier
                            .background(medai.premium.copy(alpha = 0.15f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Check Interactions",
                            tint = medai.premium,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Paracetamol", "Ibuprofen", "Aspirin").forEach { item ->
                    FilterChip(
                        selected = searchQuery == item,
                        onClick = {
                            searchQuery = item
                            viewModel.searchDrugInfo(item)
                        },
                        label = { Text(item) }
                    )
                }
            }

            // Allergy Filter Card for Search Result
            if (drugResult != null) {
                val matchedAllergies = remember(drugResult) {
                    val allText = searchQuery + " " + (drugResult?.values?.joinToString(" ") ?: "")
                    viewModel.checkMedicinesForAllergies(allText)
                }

                if (matchedAllergies.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = medai.danger.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, medai.danger.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = medai.danger)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "uz") "❌ Bu dori sizga mos emas — ${matchedAllergies.joinToString(", ")} allergiyangiz bor"
                                       else if (lang == "ru") "❌ Это лекарство вам не подходит — у вас аллергия на ${matchedAllergies.joinToString(", ")}"
                                       else "❌ This medicine is not suitable for you — you have allergy to ${matchedAllergies.joinToString(", ")}",
                                fontSize = 12.sp,
                                color = medai.danger,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = medai.success.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, medai.success.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = medai.success)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "uz") "✅ Bu dori allergiyangiz bilan mos keladi"
                                       else if (lang == "ru") "✅ Это лекарство совместимо с вашей аллергией"
                                       else "✅ This medicine is compatible with your allergies",
                                fontSize = 12.sp,
                                color = medai.success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = medai.info)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = when (lang) {
                                "uz" -> "Ma'lumot izlanmoqda..."
                                "ru" -> "Идёт поиск..."
                                else -> "Searching..."
                            },
                            fontSize = 12.sp,
                            color = medai.textSecondary
                        )
                    }
                }
            } else if (drugResult != null) {
                drugResult?.forEach { (key, value) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = medai.surface),
                        border = BorderStroke(1.dp, medai.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(medai.info, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = key, fontWeight = FontWeight.Bold, color = medai.info, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = value, fontSize = 13.sp, color = medai.textPrimary, lineHeight = 19.sp)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 1: PRESCRIPTION SCANNER ---
    if (showScannerDialog) {
        AlertDialog(
            onDismissRequest = { showScannerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = medai.brand)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (lang == "uz") "Retseptni skanerlash" else if (lang == "ru") "Сканирование рецепта" else "Scan Prescription", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (lang == "uz") "Skanerlashni simulyatsiya qilish uchun retsept turlardan birini tanlang va yuboring."
                               else "Выберите тип рецепта для симуляции сканирования.",
                        fontSize = 13.sp,
                        color = medai.textSecondary
                    )

                    val presets = listOf(
                        "Kardiolog retsepti (Aspirin, Lisinopril)" to "Cardiologist Prescription: Aspirin 75mg daily, Lisinopril 10mg daily morning. Please analyze usage and dosage.",
                        "Terapevt retsepti (Amoxicillin, Paracetamol)" to "General Physician: Amoxicillin 500mg three times daily, Paracetamol 500mg as needed for pain. Analyze.",
                        "Nevrolog retsepti (Magniy B6, Glycine)" to "Neurologist: Magnesium B6 two tablets evening, Glycine three times daily under tongue. Analyze."
                    )

                    presets.forEach { (title, prompt) ->
                        Button(
                            onClick = {
                                viewModel.scanPrescriptionImage(mockPrescriptionBase64)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = medai.brandSoft, contentColor = medai.brand),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = title, color = medai.brand, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (isScanningPrescription) {
                        Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = medai.brand)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Skanerlanmoqda...", fontSize = 12.sp, color = medai.textSecondary)
                            }
                        }
                    } else if (prescriptionScanResult.isNotEmpty()) {
                        Divider(color = medai.border)
                        Text(
                            text = "Natija:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = medai.brand
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(text = prescriptionScanResult, fontSize = 12.sp, color = medai.textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showScannerDialog = false }) {
                    Text(text = "Yopish", color = medai.brand)
                }
            }
        )
    }

    // --- DIALOG 2: DRUG INTERACTION CHECKER ---
    if (showInteractionDialog) {
        AlertDialog(
            onDismissRequest = { showInteractionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CompareArrows, contentDescription = null, tint = medai.premium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (lang == "uz") "Dori o'zaro ta'siri" else if (lang == "ru") "Взаимодействие лекарств" else "Drug Interactions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (lang == "uz") "Tekshirish uchun 2 dan 5 tagacha dori nomini yozing:"
                               else "Введите от 2 до 5 лекарств для проверки:",
                        fontSize = 13.sp,
                        color = medai.textSecondary
                    )

                    drugInputs.forEachIndexed { idx, value ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = { newVal ->
                                val list = drugInputs.toMutableList()
                                list[idx] = newVal
                                drugInputs = list
                            },
                            placeholder = { Text("Dori ${idx + 1}") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    if (drugInputs.size < 5) {
                        TextButton(
                            onClick = { drugInputs = drugInputs + "" },
                            colors = ButtonDefaults.textButtonColors(contentColor = medai.premium)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dori qo'shish")
                        }
                    }

                    Button(
                        onClick = { viewModel.checkDrugInteractions(drugInputs) },
                        enabled = drugInputs.count { it.isNotBlank() } >= 2 && !isCheckingInteractions,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = medai.premium),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isCheckingInteractions) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("O'zaro ta'sirni tekshirish")
                        }
                    }

                    if (interactionResult.isNotEmpty()) {
                        Divider(color = medai.border)

                        // Parse status marker
                        val isSafe = interactionResult.contains("STATUS: SAFE")
                        val isCaution = interactionResult.contains("STATUS: CAUTION")
                        val isDangerous = interactionResult.contains("STATUS: DANGEROUS")

                        val (statusText, statusColor) = when {
                            isSafe -> "✅ Xavfsiz (Safe)" to medai.brand
                            isCaution -> "⚠️ Ehtiyot bo'ling (Caution)" to medai.warning
                            isDangerous -> "❌ Birga ichmang! (Dangerous)" to medai.danger
                            else -> "Natija" to medai.textPrimary
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, statusColor)
                        ) {
                            Text(
                                text = statusText,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = interactionResult
                                    .replace("STATUS: SAFE", "")
                                    .replace("STATUS: CAUTION", "")
                                    .replace("STATUS: DANGEROUS", "")
                                    .trim(),
                                fontSize = 12.sp,
                                color = medai.textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInteractionDialog = false }) {
                    Text(text = "Yopish", color = medai.premium)
                }
            }
        )
    }
}

// --- SCREEN: LAB REPORT VISION ANALYZER ---

@Composable
fun LabAnalysisScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val labResultText by viewModel.labAnalysisResult.collectAsState()
    val isLoading by viewModel.isLoadingLab.collectAsState()
    val history by viewModel.labResults.collectAsState(initial = emptyList())

    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPresetIndex by remember { mutableStateOf<Int?>(null) }
    var showFullHistoryDialogText by remember { mutableStateOf<String?>(null) }

    val presets = remember(lang) {
        listOf(
            PresetReport(
                titleUz = "🩸 Umumiy qon tahlili",
                titleRu = "🩸 Общий анализ крови",
                titleEn = "🩸 Complete Blood Count",
                descUz = "Anemiya va kamqonlik belgilari (past Gemoglobin)",
                descRu = "Признаки анемии (низкий гемоглобин)",
                descEn = "Signs of anemia (low Hemoglobin)",
                prompt = "Analyze this Complete Blood Count (CBC) report. Hemoglobin is 10.5 g/dL (Low, norm: 12.0-16.0), RBC is 3.5 x10^12/L (Low, norm: 4.0-5.2), WBC is 6.2 x10^9/L (Normal), and Platelets are 220 x10^9/L (Normal). Detail the clinical significance of these low values in simple, practical $lang terms. Suggest dietary additions (iron-rich foods) and general medical lifestyle suggestions."
            ),
            PresetReport(
                titleUz = "🍔 Lipid paneli (Xolesterin)",
                titleRu = "🍔 Липидный профиль",
                titleEn = "🍔 Lipid Panel (Cholesterol)",
                descUz = "Yuqori xolesterin va yurak-qon tomir xavfi",
                descRu = "Повышенный холестерин и сердечный риск",
                descEn = "Elevated LDL & cardiovascular check",
                prompt = "Analyze this Lipid Profile report. Total Cholesterol is 245 mg/dL (High, norm: <200), LDL Cholesterol is 165 mg/dL (High, norm: <100), HDL Cholesterol is 38 mg/dL (Low, norm: >40), and Triglycerides are 190 mg/dL (High, norm: <150). Explain what LDL and HDL mean in plain language, and recommend cardio-friendly foods, exercises, and habits in simple $lang language."
            ),
            PresetReport(
                titleUz = "🍬 Qon shakari (Glukoza)",
                titleRu = "🍬 Сахар в крови (Глюкоза)",
                titleEn = "🍬 Blood Glucose & HbA1c",
                descUz = "Qandli diabet va prediabet holati tahlili",
                descRu = "Анализ на преддиабет и диабет",
                descEn = "Evaluation of glucose & HbA1c levels",
                prompt = "Analyze this Blood Sugar test. Fasting Blood Glucose is 138 mg/dL (High, norm: 70-100), and HbA1c is 7.2% (High, norm: <5.7%). Explain what these prediabetic/diabetic values mean in easy-to-understand $lang language, what are the primary nutritional limits, the importance of fiber and activity, and key advice for consulting an endocrinologist."
            )
        )
    }

    // Standard valid small base64 pixel as a fallback payload
    val mockImageBase64 = "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA="

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedPresetIndex = null // clear preset when selecting custom image
        }
    }

    // Helper to convert Uri to Base64
    fun convertUriToBase64(uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("feat_lab", lang), onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(medai.canvas)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header / Description
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = medai.brandSoft.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, medai.brand.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🔬", fontSize = 36.sp)
                        Column {
                            Text(
                                text = "Lab Vision Analyzer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            )
                            Text(
                                text = when (lang) {
                                    "uz" -> "Qon tahlillari va tibbiy hisobotlar rasmini yuklab, ularni oddiy va tushunarli tilda tahlil qilib oling."
                                    "ru" -> "Загрузите снимок анализов крови или медицинских отчетов, чтобы получить простое и понятное резюме."
                                    else -> "Upload an image of your blood tests or medical reports to get a clear, plain-language breakdown."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }

            // Image Source Picker Box
            item {
                Text(
                    text = when (lang) {
                        "uz" -> "Tahlil rasmini tanlang"
                        "ru" -> "Выберите снимок для анализа"
                        else -> "Choose Analysis Image"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(medai.surface)
                        .border(
                            width = 2.dp,
                            color = if (selectedImageUri != null) medai.brand else medai.border,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected image preview",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .clickable { selectedImageUri = null }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else if (selectedPresetIndex != null) {
                        // Display graphic of chosen preset
                        val preset = presets[selectedPresetIndex!!]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when (lang) {
                                    "uz" -> "Tanlangan namuna:"
                                    "ru" -> "Выбранный образец:"
                                    else -> "Selected Sample:"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = medai.brand)
                            )
                            Text(
                                text = when (lang) {
                                    "uz" -> preset.titleUz
                                    "ru" -> preset.titleRu
                                    else -> preset.titleEn
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = when (lang) {
                                    "uz" -> preset.descUz
                                    "ru" -> preset.descRu
                                    else -> preset.descEn
                                },
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = medai.textSecondary
                            )
                            Button(
                                onClick = { selectedPresetIndex = null },
                                colors = ButtonDefaults.buttonColors(containerColor = medai.brandSoft, contentColor = medai.brand),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = when (lang) {
                                        "uz" -> "Tozalash"
                                        "ru" -> "Очистить"
                                        else -> "Clear"
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = medai.brand,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = Translations.getString("lab_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = Translations.getString("lab_upload_hint", lang),
                                fontSize = 11.sp,
                                color = medai.textSecondary
                            )
                        }
                    }
                }
            }

            // Quick Presets Options
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (lang) {
                            "uz" -> "Yoki tayyor namuna bilan sinab ko'ring:"
                            "ru" -> "Или проверьте на готовом образце:"
                            else -> "Or test with a sample report preset:"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEachIndexed { index, preset ->
                            val title = when (lang) {
                                "uz" -> preset.titleUz
                                "ru" -> preset.titleRu
                                else -> preset.titleEn
                            }
                            val isSelected = selectedPresetIndex == index
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPresetIndex = index
                                    selectedImageUri = null // clear custom image
                                },
                                label = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = medai.brand.copy(alpha = 0.15f),
                                    selectedLabelColor = medai.brand
                                )
                            )
                        }
                    }
                }
            }

            // Action Button
            item {
                val canAnalyze = selectedImageUri != null || selectedPresetIndex != null
                Button(
                    onClick = {
                        if (selectedPresetIndex != null) {
                            val preset = presets[selectedPresetIndex!!]
                            viewModel.analyzeLabReportImage(mockImageBase64, preset.prompt)
                        } else if (selectedImageUri != null) {
                            val base64 = convertUriToBase64(selectedImageUri!!)
                            if (base64 != null) {
                                viewModel.analyzeLabReportImage(base64)
                            } else {
                                Toast.makeText(context, "Rasmni yuklashda xatolik yuz berdi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = medai.brand),
                    enabled = canAnalyze && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Science, contentDescription = null)
                            Text(text = Translations.getString("lab_analyze", lang), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Current analysis result
            if (labResultText.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = medai.brand.copy(alpha = 0.12f), spotColor = medai.brand.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = medai.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, medai.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(32.dp).background(medai.brand.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = medai.brand, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = when (lang) {
                                            "uz" -> "Tahlil Natijalari"
                                            "ru" -> "Результаты анализа"
                                            else -> "Analysis Results"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = medai.textPrimary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.clearLabAnalysisResult() }
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear result", tint = medai.textSecondary)
                                }
                            }
                            Divider(color = medai.border)

                            // Normal/Borderline/Abnormal Indicators
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = medai.success, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Translations.getString("lab_normal", lang), fontSize = 11.sp, color = medai.textSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = medai.warning, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Translations.getString("lab_borderline", lang), fontSize = 11.sp, color = medai.textSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = medai.danger, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Translations.getString("lab_abnormal", lang), fontSize = 11.sp, color = medai.textSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Use pre-coded Markdown Text
                            RichMarkdownText(
                                text = labResultText,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // History Section Header
            if (history.isNotEmpty()) {
                item {
                    Text(
                        text = when (lang) {
                            "uz" -> "O'tmishdagi tahlillar"
                            "ru" -> "История анализов"
                            else -> "History of Analyses"
                        }.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = medai.brand,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(history) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFullHistoryDialogText = record.analysisText },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = medai.surface),
                        border = BorderStroke(1.dp, medai.border)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(medai.brand.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = medai.brand, modifier = Modifier.size(18.dp))
                                    }
                                    Column {
                                        Text(
                                            text = when (lang) {
                                                "uz" -> "Tahlil hisoboti"
                                                "ru" -> "Отчет анализа"
                                                else -> "Lab Report Analysis"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = medai.textPrimary
                                        )
                                        Text(
                                            text = record.timestamp?.let {
                                                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(it))
                                            } ?: "Yaqinda",
                                            fontSize = 11.sp,
                                            color = medai.textSecondary
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteLabResult(record.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = medai.danger, modifier = Modifier.size(16.dp))
                                }
                            }
                            
                            val previewText = if (record.analysisText.length > 120) {
                                record.analysisText.take(120) + "..."
                            } else {
                                record.analysisText
                            }
                            Text(
                                text = previewText,
                                fontSize = 12.sp,
                                color = medai.textSecondary,
                                maxLines = 2,
                                lineHeight = 16.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = when (lang) {
                                        "uz" -> "Batafsil o'qish ➡️"
                                        "ru" -> "Подробнее ➡️"
                                        else -> "Read Full ➡️"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = medai.brand
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detailed History Dialog
    if (showFullHistoryDialogText != null) {
        AlertDialog(
            onDismissRequest = { showFullHistoryDialogText = null },
            confirmButton = {
                TextButton(onClick = { showFullHistoryDialogText = null }) {
                    Text(text = "OK", fontWeight = FontWeight.Bold, color = medai.brand)
                }
            },
            title = {
                Text(
                    text = when (lang) {
                        "uz" -> "Tahlil Tafsilotlari"
                        "ru" -> "Детали анализа"
                        else -> "Analysis Details"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    RichMarkdownText(text = showFullHistoryDialogText!!)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = medai.surface
        )
    }
}

data class PresetReport(
    val titleUz: String,
    val titleRu: String,
    val titleEn: String,
    val descUz: String,
    val descRu: String,
    val descEn: String,
    val prompt: String
)
