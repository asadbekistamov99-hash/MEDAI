package com.example.ui

import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.i18n.Translations
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MinTouch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- SCREEN: LAB REPORT VISION ANALYZER ---

/** A "name: value (flag)" line found in the AI's answer, shown as a table row. */
private data class LabRow(val name: String, val value: String, val flag: LabFlag)

private enum class LabFlag { Normal, Borderline, High, Low }

private val highWords = Regex("high|yuqori|повышен|высок|elevated|↑")
private val lowWords = Regex("low|past\\b|пониж|низк|↓")
private val borderlineWords = Regex("borderline|chegara|погранич|o'rtacha xavf")
private val normalWords = Regex("normal|norma\\b|в норме|норма")
private val bulletPrefix = Regex("^([-*•]|\\d+[.)])\\s+")

private fun detectFlag(text: String): LabFlag? {
    val t = text.lowercase()
    return when {
        highWords.containsMatchIn(t) -> LabFlag.High
        lowWords.containsMatchIn(t) -> LabFlag.Low
        borderlineWords.containsMatchIn(t) -> LabFlag.Borderline
        normalWords.containsMatchIn(t) -> LabFlag.Normal
        else -> null
    }
}

/** Splits the answer into table rows and the remaining prose. Pure, so it can be remembered per text. */
private fun splitLabText(text: String): Pair<List<LabRow>, String> {
    val rows = mutableListOf<LabRow>()
    val rest = StringBuilder()
    text.split("\n").forEach { line ->
        val clean = line.trim().replace(bulletPrefix, "").replace("**", "").replace("`", "")
        val colon = clean.indexOf(':')
        var row: LabRow? = null
        if (colon in 1..40) {
            val name = clean.substring(0, colon).trim()
            val value = clean.substring(colon + 1).trim()
            if (value.length in 1..90 && value.any { it.isDigit() }) {
                val flag = detectFlag(value)
                if (flag != null) row = LabRow(name, value, flag)
            }
        }
        if (row != null) rows += row else rest.append(line).append('\n')
    }
    return rows to rest.toString().trim()
}

@Composable
fun LabAnalysisScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

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
                titleUz = "Umumiy qon tahlili",
                titleRu = "Общий анализ крови",
                titleEn = "Complete Blood Count",
                descUz = "Anemiya va kamqonlik belgilari (past Gemoglobin)",
                descRu = "Признаки анемии (низкий гемоглобин)",
                descEn = "Signs of anemia (low Hemoglobin)",
                prompt = "Analyze this Complete Blood Count (CBC) report. Hemoglobin is 10.5 g/dL (Low, norm: 12.0-16.0), RBC is 3.5 x10^12/L (Low, norm: 4.0-5.2), WBC is 6.2 x10^9/L (Normal), and Platelets are 220 x10^9/L (Normal). Detail the clinical significance of these low values in simple, practical $lang terms. Suggest dietary additions (iron-rich foods) and general medical lifestyle suggestions."
            ),
            PresetReport(
                titleUz = "Lipid paneli (Xolesterin)",
                titleRu = "Липидный профиль",
                titleEn = "Lipid Panel (Cholesterol)",
                descUz = "Yuqori xolesterin va yurak-qon tomir xavfi",
                descRu = "Повышенный холестерин и сердечный риск",
                descEn = "Elevated LDL & cardiovascular check",
                prompt = "Analyze this Lipid Profile report. Total Cholesterol is 245 mg/dL (High, norm: <200), LDL Cholesterol is 165 mg/dL (High, norm: <100), HDL Cholesterol is 38 mg/dL (Low, norm: >40), and Triglycerides are 190 mg/dL (High, norm: <150). Explain what LDL and HDL mean in plain language, and recommend cardio-friendly foods, exercises, and habits in simple $lang language."
            ),
            PresetReport(
                titleUz = "Qon shakari (Glukoza)",
                titleRu = "Сахар в крови (Глюкоза)",
                titleEn = "Blood Glucose & HbA1c",
                descUz = "Qandli diabet va prediabet holati tahlili",
                descRu = "Анализ на преддиабет и диабет",
                descEn = "Evaluation of glucose & HbA1c levels",
                prompt = "Analyze this Blood Sugar test. Fasting Blood Glucose is 138 mg/dL (High, norm: 70-100), and HbA1c is 7.2% (High, norm: <5.7%). Explain what these prediabetic/diabetic values mean in easy-to-understand $lang language, what are the primary nutritional limits, the importance of fiber and activity, and key advice for consulting an endocrinologist."
            )
        )
    }
    val presetIcons = listOf(Icons.Default.Bloodtype, Icons.Default.MonitorHeart, Icons.Default.Colorize)

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

    val parsed = remember(labResultText) { splitLabText(labResultText) }
    val canAnalyze = selectedImageUri != null || selectedPresetIndex != null

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("feat_lab", lang), onBack = onBack) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            item(key = "intro") {
                MedAIInfoBanner(
                    text = medText(
                        lang,
                        "Qon tahlillari va tibbiy hisobotlar rasmini yuklab, ularni oddiy va tushunarli tilda tahlil qilib oling.",
                        "Загрузите снимок анализов крови или медицинских отчётов, чтобы получить простое и понятное резюме.",
                        "Upload an image of your blood tests or medical reports to get a clear, plain-language breakdown.",
                    ),
                    tone = MedAITone.Info,
                )
            }

            item(key = "pick-title") {
                MedSectionTitle(medText(lang, "Tahlil rasmini tanlang", "Выберите снимок для анализа", "Choose analysis image"))
            }

            // Preview / drop zone
            item(key = "preview") {
                val shape = RoundedCornerShape(MedAICorners.card)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp)
                        .clip(shape)
                        .background(c.surface)
                        .border(if (selectedImageUri != null || selectedPresetIndex != null) 2.dp else 1.dp,
                            if (selectedImageUri != null || selectedPresetIndex != null) c.brand else c.borderStrong, shape)
                        .clickable(role = Role.Button) { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    val preset = selectedPresetIndex?.let { presets[it] }
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = medText(lang, "Tanlangan rasm", "Выбранное изображение", "Selected image preview"),
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(4.dp).size(MinTouch).clip(CircleShape)
                                .clickable(role = Role.Button) { selectedImageUri = null },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(Modifier.size(32.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = medText(lang, "Tozalash", "Очистить", "Clear"),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    } else if (preset != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MedAIBadge(medText(lang, "Tanlangan namuna", "Выбранный образец", "Selected sample"), MedAIBadgeTone.Brand)
                            Text(
                                text = medText(lang, preset.titleUz, preset.titleRu, preset.titleEn),
                                style = MaterialTheme.typography.titleMedium,
                                color = c.textPrimary,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = medText(lang, preset.descUz, preset.descRu, preset.descEn),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = c.textSecondary,
                            )
                            MedAITextButton(
                                text = medText(lang, "Tozalash", "Очистить", "Clear"),
                                onClick = { selectedPresetIndex = null },
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MedIconDisc(Icons.Default.AddPhotoAlternate, c.brandSoft, c.onBrandSoft, size = 56.dp)
                            Text(
                                text = Translations.getString("lab_title", lang),
                                style = MaterialTheme.typography.titleSmall,
                                color = c.textPrimary,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = medText(lang, "Galereyadan rasm tanlang", "Выберите изображение из галереи", "Pick an image from your gallery"),
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textSecondary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            // The two actions: pick (secondary) and analyze (primary, with loading)
            item(key = "actions") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MedAISecondaryButton(
                        text = medText(lang, "Galereyadan tanlash", "Выбрать из галереи", "Choose from gallery"),
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.PhotoLibrary,
                    )
                    MedAIPrimaryButton(
                        text = Translations.getString("lab_analyze", lang),
                        onClick = {
                            if (selectedPresetIndex != null) {
                                val preset = presets[selectedPresetIndex!!]
                                viewModel.analyzeLabReportImage(mockImageBase64, preset.prompt)
                            } else if (selectedImageUri != null) {
                                val base64 = convertUriToBase64(selectedImageUri!!)
                                if (base64 != null) {
                                    viewModel.analyzeLabReportImage(base64)
                                } else {
                                    Toast.makeText(
                                        context,
                                        medText(lang, "Rasmni yuklashda xatolik yuz berdi", "Не удалось загрузить изображение", "Could not load the image"),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Science,
                        enabled = canAnalyze && !isLoading,
                        loading = isLoading,
                    )
                }
            }

            // Sample reports
            item(key = "presets") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedSectionTitle(medText(lang, "Yoki tayyor namuna bilan sinab ko'ring", "Или проверьте на готовом образце", "Or try a sample report"))
                    MedAICard(contentPadding = 0.dp, modifier = Modifier.fillMaxWidth()) {
                        presets.forEachIndexed { index, preset ->
                            PresetRow(
                                icon = presetIcons[index],
                                title = medText(lang, preset.titleUz, preset.titleRu, preset.titleEn),
                                subtitle = medText(lang, preset.descUz, preset.descRu, preset.descEn),
                                selected = selectedPresetIndex == index,
                                showDivider = index < presets.lastIndex,
                                onClick = {
                                    selectedPresetIndex = index
                                    selectedImageUri = null // clear custom image
                                },
                            )
                        }
                    }
                }
            }

            // Current analysis result
            if (labResultText.isNotEmpty()) {
                item(key = "result") {
                    val (rows, prose) = parsed
                    MedAICard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MedIconDisc(Icons.Default.Science, c.tintTeal.bg, c.tintTeal.fg, size = 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = medText(lang, "Tahlil natijalari", "Результаты анализа", "Analysis results"),
                                style = MaterialTheme.typography.titleMedium,
                                color = c.textPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            MedIconButton(
                                Icons.Default.Close, medText(lang, "Natijani yopish", "Закрыть результат", "Dismiss result"),
                                tint = c.textSecondary, onClick = { viewModel.clearLabAnalysisResult() },
                            )
                        }
                        if (rows.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            MedHairline()
                            rows.forEachIndexed { i, row ->
                                LabValueRow(row, lang)
                                if (i < rows.lastIndex) MedHairline()
                            }
                        }
                        if (prose.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            if (rows.isNotEmpty()) {
                                MedHairline()
                                Spacer(Modifier.height(12.dp))
                            }
                            RichMarkdownText(text = prose, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            // History
            if (history.isNotEmpty()) {
                item(key = "history-title") {
                    MedSectionTitle(medText(lang, "O'tmishdagi tahlillar", "История анализов", "History of analyses"))
                }

                items(history, key = { it.id }) { record ->
                    val title = medText(lang, "Tahlil hisoboti", "Отчёт анализа", "Lab report analysis")
                    val date = record.timestamp.let {
                        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(it))
                    }
                    val preview = remember(record.analysisText) {
                        record.analysisText.replace("\n", " ").let { if (it.length > 120) it.take(120) + "..." else it }
                    }
                    MedAICard(contentPadding = 0.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                Modifier
                                    .weight(1f)
                                    .heightIn(min = 64.dp)
                                    .clickable(role = Role.Button) { showFullHistoryDialogText = record.analysisText }
                                    .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MedIconDisc(Icons.Default.History, c.tintSky.bg, c.tintSky.fg, size = 40.dp)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                    Text(date, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                    Text(
                                        preview,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            MedIconButton(
                                Icons.Default.Delete, medText(lang, "O'chirish", "Удалить", "Delete"),
                                tint = c.danger, onClick = { viewModel.deleteLabResult(record.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    // Detailed history dialog
    showFullHistoryDialogText?.let { fullText ->
        MedContentDialog(
            title = medText(lang, "Tahlil tafsilotlari", "Детали анализа", "Analysis details"),
            closeText = "OK",
            onDismiss = { showFullHistoryDialogText = null },
            icon = Icons.Default.Science,
        ) {
            RichMarkdownText(text = fullText)
        }
    }
}

@Composable
private fun PresetRow(
    icon: ImageVector, title: String, subtitle: String, selected: Boolean, showDivider: Boolean, onClick: () -> Unit,
) {
    val c = MedAITheme.colors
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .background(if (selected) c.brandSoft else Color.Transparent)
                .clickable(role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MedIconDisc(icon, c.tintTeal.bg, c.tintTeal.fg, size = 40.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = if (selected) c.onBrandSoft else c.textPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (selected) c.onBrandSoft else c.textSecondary)
            }
            if (selected) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = c.brand)
            }
        }
        if (showDivider) MedHairline(Modifier.padding(start = 70.dp))
    }
}

@Composable
private fun LabValueRow(row: LabRow, lang: String) {
    val c = MedAITheme.colors
    val (label, tone) = when (row.flag) {
        LabFlag.Normal -> medText(lang, "Normal", "В норме", "Normal") to MedAIBadgeTone.Success
        LabFlag.Borderline -> medText(lang, "Chegarada", "Погранично", "Borderline") to MedAIBadgeTone.Warning
        LabFlag.High -> medText(lang, "Yuqori", "Повышен", "High") to MedAIBadgeTone.Danger
        LabFlag.Low -> medText(lang, "Past", "Понижен", "Low") to MedAIBadgeTone.Danger
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(row.name, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
            Text(row.value, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        Spacer(Modifier.width(12.dp))
        MedAIBadge(label, tone)
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
