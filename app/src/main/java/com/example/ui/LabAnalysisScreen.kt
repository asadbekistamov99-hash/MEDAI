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
