@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.LocationServices
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.*
import com.example.i18n.Translations
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
// --- SCREEN: ANALYTICS (weight & BMI, pressure & pulse, sleep & nutrition) ---

@Composable
fun AnalyticsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val todayMetricsState by viewModel.todayMetrics.collectAsState()
    val allMetricsList by viewModel.allDailyMetrics.collectAsState()
    val context = LocalContext.current

    fun tr(uz: String, ru: String, en: String) = when (lang) { "uz" -> uz; "ru" -> ru; else -> en }
    fun toast(uz: String, ru: String, en: String) =
        Toast.makeText(viewModel.getApplication(), tr(uz, ru, en), Toast.LENGTH_SHORT).show()

    var activeTab by remember { mutableStateOf(0) }
    val tabLabels = listOf(
        tr("Vazn va BMI", "Вес и ИМТ", "Weight & BMI"),
        tr("Bosim va puls", "Давление и пульс", "Pressure & pulse"),
        tr("Uyqu va ovqat", "Сон и питание", "Sleep & food"),
    )

    // Weight & BMI inputs
    var heightInput by remember { mutableStateOf("175") }
    var weightInput by remember { mutableStateOf(todayMetricsState?.weight?.let { if (it > 0) it.toString() else "70" } ?: "70") }

    // BP & HR inputs
    var systolicInput by remember { mutableStateOf("120") }
    var diastolicInput by remember { mutableStateOf("80") }
    var bpmInput by remember { mutableStateOf("72") }

    // Sleep & meal inputs
    var sleepHoursInput by remember { mutableStateOf("8.0") }
    var bedtimeInput by remember { mutableStateOf("23:00") }
    var waketimeInput by remember { mutableStateOf("07:00") }

    var mealTitleInput by remember { mutableStateOf("") }
    var mealCaloriesInput by remember { mutableStateOf("450") }
    var mealTypeSelected by remember { mutableStateOf("Breakfast") }

    val isAnalyzingNutrition by viewModel.isAnalyzingNutrition.collectAsState()
    val nutritionAnalysisResult by viewModel.nutritionAdvice.collectAsState()

    // Last seven days, parsed once per data change.
    val recentMetrics = remember(allMetricsList) { allMetricsList.takeLast(7) }

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("feat_analytics", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnalyticsTabs(tabLabels, activeTab) { activeTab = it }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeTab) {
                    0 -> {
                        // --- WEIGHT & BMI ---
                        MedAICard(Modifier.fillMaxWidth()) {
                            AnalyticsCardTitle(Icons.Default.MonitorWeight, tr("Tana vazni indeksi (BMI)", "Индекс массы тела (ИМТ)", "Body mass index (BMI)"), c.tintTeal)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MedAITextField(heightInput, { heightInput = it }, tr("Bo'y (sm)", "Рост (см)", "Height (cm)"), Modifier.weight(1f), keyboardType = KeyboardType.Number)
                                MedAITextField(weightInput, { weightInput = it }, tr("Vazn (kg)", "Вес (кг)", "Weight (kg)"), Modifier.weight(1f), keyboardType = KeyboardType.Number)
                            }

                            val heightM = heightInput.toDoubleOrNull()?.div(100.0) ?: 1.75
                            val weightKg = weightInput.toDoubleOrNull() ?: 70.0
                            val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 22.8

                            val (bmiCategory, bmiTone) = when {
                                bmi < 18.5 -> tr("Vazn yetarli emas", "Недостаточный вес", "Underweight") to MedAIBadgeTone.Info
                                bmi < 25.0 -> tr("Sog'lom vazn", "Нормальный вес", "Healthy weight") to MedAIBadgeTone.Success
                                bmi < 30.0 -> tr("Ortiqcha vazn", "Избыточный вес", "Overweight") to MedAIBadgeTone.Warning
                                else -> tr("Semizlik", "Ожирение", "Obesity") to MedAIBadgeTone.Danger
                            }

                            Spacer(Modifier.height(16.dp))
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(tr("Sizning BMI ko'rsatkichingiz", "Ваш индекс ИМТ", "Your BMI"), style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
                                Text(String.format("%.1f", bmi), style = MedAIText.MetricLarge, color = c.textPrimary)
                                MedAIBadge(bmiCategory, bmiTone)
                                Spacer(Modifier.height(12.dp))
                                BmiScale(bmi)
                            }

                            Spacer(Modifier.height(16.dp))
                            MedAIPrimaryButton(
                                text = tr("Kunlik vaznni saqlash", "Сохранить вес за день", "Save today's weight"),
                                onClick = {
                                    val w = weightInput.toDoubleOrNull()
                                    if (w != null) {
                                        viewModel.logWeightMetrics(w)
                                        toast("Vazn saqlandi", "Вес сохранён", "Weight saved")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        SeriesBarsCard(
                            title = tr("Oxirgi vazn o'zgarishlari", "Динамика веса", "Recent weight"),
                            emptyText = tr("Ma'lumotlar kam", "Недостаточно данных", "Not enough data"),
                            items = recentMetrics.filter { it.weight > 0 }.map { it.date.takeLast(2) to it.weight.toFloat() },
                            color = c.brand,
                            valueFormat = { it.toInt().toString() }
                        )

                        VitalsTrendsCard(viewModel)
                    }
                    1 -> {
                        // --- PRESSURE & PULSE ---
                        MedAICard(Modifier.fillMaxWidth()) {
                            AnalyticsCardTitle(Icons.Default.Favorite, tr("Qon bosimi va puls", "Давление и пульс", "Blood pressure and pulse"), c.tintPeach)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MedAITextField(systolicInput, { systolicInput = it }, tr("Sistolik (mmHg)", "Систолическое (мм рт. ст.)", "Systolic (mmHg)"), Modifier.weight(1f), keyboardType = KeyboardType.Number)
                                MedAITextField(diastolicInput, { diastolicInput = it }, tr("Diastolik (mmHg)", "Диастолическое (мм рт. ст.)", "Diastolic (mmHg)"), Modifier.weight(1f), keyboardType = KeyboardType.Number)
                            }
                            Spacer(Modifier.height(12.dp))
                            MedAITextField(bpmInput, { bpmInput = it }, tr("Puls (urish/daqiqa)", "Пульс (уд/мин)", "Pulse (bpm)"), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number)

                            val sys = systolicInput.toIntOrNull() ?: 120
                            val dia = diastolicInput.toIntOrNull() ?: 80
                            val (bpStatus, bpTone) = when {
                                sys < 120 && dia < 80 -> tr("Qon bosimi ideal darajada", "Давление в идеальной норме", "Blood pressure is ideal") to MedAITone.Success
                                sys in 120..129 && dia < 80 -> tr("Normadan biroz yuqori (pre-gipertoniya)", "Немного выше нормы (прегипертония)", "Slightly above normal (pre-hypertension)") to MedAITone.Warning
                                else -> tr("Yuqori qon bosimi (gipertoniya). Shifokor bilan maslahatlashing.", "Высокое давление (гипертония). Проконсультируйтесь с врачом.", "High blood pressure (hypertension). Consult a doctor.") to MedAITone.Error
                            }
                            Spacer(Modifier.height(16.dp))
                            MedAIInfoBanner(text = bpStatus, tone = bpTone)

                            Spacer(Modifier.height(16.dp))
                            MedAIPrimaryButton(
                                text = tr("Bosim va pulsni saqlash", "Сохранить давление и пульс", "Save pressure and pulse"),
                                onClick = {
                                    val sysVal = systolicInput.toIntOrNull()
                                    val diaVal = diastolicInput.toIntOrNull()
                                    val bpmVal = bpmInput.toIntOrNull()
                                    if (sysVal != null && diaVal != null && bpmVal != null) {
                                        viewModel.logBloodPressureMetrics(sysVal, diaVal)
                                        viewModel.logHeartRateMetrics(bpmVal)
                                        toast("Ko'rsatkichlar saqlandi", "Показатели сохранены", "Readings saved")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        SeriesBarsCard(
                            title = tr("Puls o'zgarishi (urish/daqiqa)", "Динамика пульса (уд/мин)", "Pulse trend (bpm)"),
                            emptyText = tr("Ma'lumotlar kam", "Недостаточно данных", "Not enough data"),
                            items = recentMetrics.filter { it.heartRate > 0 }.map { it.date.takeLast(2) to it.heartRate.toFloat() },
                            color = c.danger,
                            valueFormat = { it.toInt().toString() }
                        )

                        VitalsTrendsCard(viewModel)
                    }
                    2 -> {
                        // --- SLEEP & NUTRITION ---
                        MedAICard(Modifier.fillMaxWidth()) {
                            AnalyticsCardTitle(Icons.Default.Bedtime, tr("Uyqu jurnali", "Журнал сна", "Sleep log"), c.tintSky)
                            Spacer(Modifier.height(12.dp))
                            MedAITextField(sleepHoursInput, { sleepHoursInput = it }, tr("Uyqu (soat)", "Сон (часы)", "Sleep (hours)"), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MedAITextField(bedtimeInput, { bedtimeInput = it }, tr("Yotish vaqti", "Время сна", "Bedtime"), Modifier.weight(1f))
                                MedAITextField(waketimeInput, { waketimeInput = it }, tr("Uyg'onish", "Пробуждение", "Wake-up"), Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(16.dp))
                            MedAIPrimaryButton(
                                text = tr("Uyquni saqlash", "Сохранить сон", "Save sleep"),
                                onClick = {
                                    val hours = sleepHoursInput.toDoubleOrNull() ?: 8.0
                                    viewModel.logSleepMetrics(hours, bedtimeInput, waketimeInput)
                                    toast("Uyqu jurnali yangilandi", "Журнал сна обновлён", "Sleep log updated")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        MedAICard(Modifier.fillMaxWidth()) {
                            AnalyticsCardTitle(Icons.Default.Restaurant, tr("Taom qo'shish", "Добавить еду", "Add a meal"), c.tintPeach)
                            Spacer(Modifier.height(12.dp))
                            MedAITextField(
                                mealTitleInput, { mealTitleInput = it },
                                tr("Taom nomi", "Название блюда", "Meal name"), Modifier.fillMaxWidth(),
                                placeholder = tr("Masalan: lag'mon, olma", "Например: лагман, яблоко", "e.g. noodles, apple")
                            )
                            Spacer(Modifier.height(12.dp))
                            MedAITextField(mealCaloriesInput, { mealCaloriesInput = it }, tr("Kaloriya (kcal)", "Калории (ккал)", "Calories (kcal)"), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number)
                            Spacer(Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    "Breakfast" to tr("Nonushta", "Завтрак", "Breakfast"),
                                    "Lunch" to tr("Tushlik", "Обед", "Lunch"),
                                    "Dinner" to tr("Kechki ovqat", "Ужин", "Dinner"),
                                ).forEach { (type, label) ->
                                    MedAIFilterChip(label, mealTypeSelected == type, { mealTypeSelected = type })
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            MedAISecondaryButton(
                                text = tr("Taomni qo'shish", "Добавить еду", "Add meal"),
                                icon = Icons.Default.Add,
                                enabled = mealTitleInput.isNotBlank(),
                                onClick = {
                                    val cal = mealCaloriesInput.toIntOrNull() ?: 450
                                    if (mealTitleInput.isNotBlank()) {
                                        viewModel.logMealMetrics(mealTitleInput, cal, mealTypeSelected)
                                        mealTitleInput = ""
                                        toast("Taom qo'shildi", "Блюдо добавлено", "Meal added")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Daily meal summary
                        val mealList = remember(todayMetricsState?.mealsJson) {
                            val list = mutableListOf<Triple<String, String, Int>>()
                            todayMetricsState?.mealsJson?.let { json ->
                                try {
                                    val arr = org.json.JSONArray(json)
                                    for (i in 0 until arr.length()) {
                                        val obj = arr.getJSONObject(i)
                                        list.add(Triple(obj.optString("title", ""), obj.optString("type", ""), obj.optInt("calories", 0)))
                                    }
                                } catch (e: Exception) {}
                            }
                            list
                        }
                        val totalCalories = remember(todayMetricsState?.mealsJson) {
                            todayMetricsState?.mealsJson?.let {
                                try {
                                    val arr = org.json.JSONArray(it)
                                    var sum = 0
                                    for (i in 0 until arr.length()) sum += arr.getJSONObject(i).getInt("calories")
                                    sum
                                } catch (e: Exception) { 0 }
                            } ?: 0
                        }

                        MedAICard(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(tr("Bugungi ovqatlanish", "Питание сегодня", "Today's nutrition"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
                                Text("$totalCalories / 2000 kcal", style = MaterialTheme.typography.titleSmall, color = c.brand)
                            }
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (totalCalories.toFloat() / 2000f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = c.brand,
                                trackColor = c.brandSoft,
                                strokeCap = StrokeCap.Round,
                            )
                            if (mealList.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                mealList.forEach { (title, type, calories) ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("$title · $type", style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
                                        Text("$calories kcal", style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
                                    }
                                }
                            }
                        }

                        // AI nutrition coach
                        MedAIPremiumButton(
                            text = tr("AI parhez tavsiyasi", "Совет AI по питанию", "AI nutrition advice"),
                            icon = Icons.Default.AutoAwesome,
                            loading = isAnalyzingNutrition,
                            onClick = { viewModel.analyzeNutritionDaily() },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (nutritionAnalysisResult.isNotEmpty()) {
                            val resultShape = RoundedCornerShape(MedAICorners.card)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(resultShape)
                                    .background(c.premiumSoft)
                                    .border(1.dp, c.premium.copy(alpha = 0.3f), resultShape)
                                    .padding(16.dp)
                            ) {
                                Text(tr("AI parhezshunos maslahati", "Совет AI-диетолога", "AI dietitian's advice"), style = MaterialTheme.typography.titleSmall, color = c.onPremiumSoft)
                                Spacer(Modifier.height(8.dp))
                                Text(nutritionAnalysisResult, style = MaterialTheme.typography.bodyMedium, color = c.onPremiumSoft)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Three equal segments, 48dp tall, with the active one tinted. */
@Composable
private fun AnalyticsTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val c = MedAITheme.colors
    Column(Modifier.fillMaxWidth().background(c.surface)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEachIndexed { index, label ->
                val isSelected = index == selected
                val bg by animateColorAsState(if (isSelected) c.brandSoft else Color.Transparent, tween(180), label = "analyticsTabBg")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = MinTouch)
                        .clip(RoundedCornerShape(MedAICorners.control))
                        .background(bg)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) c.onBrandSoft else c.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
    }
}

@Composable
private fun AnalyticsCardTitle(icon: ImageVector, title: String, tint: MedAITint) {
    val c = MedAITheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(tint.bg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
    }
}

/** BMI ruler: four coloured ranges with a marker at the current value. */
@Composable
private fun BmiScale(bmi: Double) {
    val c = MedAITheme.colors
    val fraction = ((bmi.coerceIn(10.0, 40.0) - 10.0) / 30.0).toFloat()
    Box(Modifier.fillMaxWidth().height(28.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val barTop = size.height * 0.5f
            val barH = 8.dp.toPx()
            val gap = 2.dp.toPx()
            // ranges: <18.5, 18.5-25, 25-30, 30+  (scale 10..40)
            val bounds = listOf(10.0, 18.5, 25.0, 30.0, 40.0)
            val colors = listOf(c.info, c.success, c.warning, c.danger)
            for (i in 0 until 4) {
                val x0 = ((bounds[i] - 10.0) / 30.0).toFloat() * size.width
                val x1 = ((bounds[i + 1] - 10.0) / 30.0).toFloat() * size.width
                drawRoundRect(colors[i], Offset(x0 + if (i == 0) 0f else gap / 2, barTop), Size(x1 - x0 - if (i == 0 || i == 3) gap / 2 else gap, barH), androidx.compose.ui.geometry.CornerRadius(barH / 2))
            }
            val mx = fraction * size.width
            drawCircle(c.textPrimary, radius = 7.dp.toPx(), center = Offset(mx, barTop + barH / 2))
            drawCircle(c.surface, radius = 3.dp.toPx(), center = Offset(mx, barTop + barH / 2))
        }
    }
}

/** Compact bar chart for the last few days; bars are scaled between the series' own min and max. */
@Composable
private fun SeriesBarsCard(
    title: String,
    emptyText: String,
    items: List<Pair<String, Float>>,
    color: Color,
    valueFormat: (Float) -> String,
) {
    val c = MedAITheme.colors
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        Spacer(Modifier.height(12.dp))
        MedAICard(Modifier.fillMaxWidth()) {
            if (items.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                    Text(emptyText, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                }
            } else {
                val lo = items.minOf { it.second }
                val hi = items.maxOf { it.second }
                val span = (hi - lo).coerceAtLeast(1f)
                Row(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    items.forEach { (label, value) ->
                        // Min bar stays visible: 30% of the plot height at the lowest value.
                        val barFraction = 0.3f + 0.7f * ((value - lo) / span)
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                            Text(valueFormat(value), style = MaterialTheme.typography.labelMedium, color = c.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier
                                    .width(20.dp)
                                    .fillMaxHeight(barFraction * 0.62f)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(color)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(label, style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                        }
                    }
                }
            }
        }
    }
}

private data class VitalPoint(val date: String, val sys: Float?, val dia: Float?, val hr: Float?, val wt: Float?)

/**
 * Native replacement for the old WebView chart. The WebView downloaded Tailwind, React, Recharts
 * and Babel from public CDNs on every open (slow, broken offline, runs remote code); this draws
 * the same four views (all / pressure / pulse / weight) straight on a Canvas from the same
 * `firestoreVitals` list.
 */
@Composable
fun VitalsTrendsCard(viewModel: AppViewModel) {
    val vitals by viewModel.firestoreVitals.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    VitalsTrendsContent(vitals, lang)
}

/** Stateless chart so it can be previewed and tested with sample readings. */
@Composable
fun VitalsTrendsContent(vitals: List<com.example.data.FirestoreVitalReading>, lang: String) {
    val c = MedAITheme.colors
    fun tr(uz: String, ru: String, en: String) = when (lang) { "uz" -> uz; "ru" -> ru; else -> en }

    var view by remember { mutableStateOf("all") }

    val points = remember(vitals) {
        vitals.map {
            VitalPoint(
                date = it.date.split("-").drop(1).joinToString("/"),
                sys = it.bpSystolic.takeIf { v -> v > 0 }?.toFloat(),
                dia = it.bpDiastolic.takeIf { v -> v > 0 }?.toFloat(),
                hr = it.heartRate.takeIf { v -> v > 0 }?.toFloat(),
                wt = it.weight.takeIf { v -> v > 0 }?.toFloat(),
            )
        }.filter { it.sys != null || it.dia != null || it.hr != null || it.wt != null }
    }

    val views = listOf(
        "all" to tr("Hammasi", "Все", "All"),
        "bp" to tr("Bosim", "Давл.", "BP"),
        "hr" to tr("Puls", "Пульс", "HR"),
        "weight" to tr("Vazn", "Вес", "Weight"),
    )

    // Series shown for the selected view: values, colour, label, drawing style.
    class Series(val name: String, val color: Color, val values: List<Float?>, val style: String)
    val series: List<Series> = when (view) {
        "bp" -> listOf(Series(tr("Sistolik", "Систолическое", "Systolic"), c.brand, points.map { it.sys }, "line"), Series(tr("Diastolik", "Диастолическое", "Diastolic"), c.info, points.map { it.dia }, "line"))
        "hr" -> listOf(Series(tr("Puls", "Пульс", "Heart rate"), c.danger, points.map { it.hr }, "area"))
        "weight" -> listOf(Series(tr("Vazn (kg)", "Вес (кг)", "Weight (kg)"), c.brand, points.map { it.wt }, "bars"))
        else -> listOf(
            Series("Sys", c.brand, points.map { it.sys }, "line"),
            Series("Dia", c.info, points.map { it.dia }, "line"),
            Series("BPM", c.danger, points.map { it.hr }, "line"),
        )
    }

    val textMeasurer = rememberTextMeasurer()
    val axisStyle = MaterialTheme.typography.labelSmall.copy(color = c.textSecondary)
    val gridColor = c.border
    val latest = points.lastOrNull()
    val summary = latest?.let {
        listOfNotNull(
            if (it.sys != null && it.dia != null) "${it.sys.toInt()}/${it.dia.toInt()} mmHg" else null,
            it.hr?.let { v -> "${v.toInt()} bpm" },
            it.wt?.let { v -> "${v.toInt()} kg" },
        ).joinToString(", ")
    } ?: ""

    Column {
        Text(tr("Salomatlik ko'rsatkichlari dinamikasi", "Динамика показателей здоровья", "Health vitals trend"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        Spacer(Modifier.height(12.dp))
        MedAICard(Modifier.fillMaxWidth()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                views.forEach { (id, label) -> MedAIFilterChip(label, view == id, { view = id }) }
            }
            Spacer(Modifier.height(8.dp))
            if (points.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    Text(tr("Ma'lumotlar hali yo'q", "Данных пока нет", "No data yet"), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                }
            } else {
                val all = series.flatMap { it.values }.filterNotNull()
                val rawMin = all.minOrNull() ?: 0f
                val rawMax = all.maxOrNull() ?: 1f
                val pad = ((rawMax - rawMin) * 0.15f).coerceAtLeast(5f)
                val yMin = (rawMin - pad)
                val yMax = (rawMax + pad)
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .semantics { contentDescription = summary }
                ) {
                    val left = 36.dp.toPx(); val right = 8.dp.toPx(); val top = 8.dp.toPx(); val bottom = 22.dp.toPx()
                    val plotW = size.width - left - right
                    val plotH = size.height - top - bottom
                    fun yOf(v: Float) = top + (1f - (v - yMin) / (yMax - yMin)) * plotH
                    fun xOf(i: Int, n: Int) = if (n == 1) left + plotW / 2 else left + plotW * i / (n - 1)

                    // grid + y labels
                    for (g in 0..3) {
                        val v = yMin + (yMax - yMin) * g / 3f
                        val y = yOf(v)
                        drawLine(gridColor, Offset(left, y), Offset(left + plotW, y), strokeWidth = 1.dp.toPx())
                        val label = textMeasurer.measure(v.toInt().toString(), axisStyle)
                        drawText(label, topLeft = Offset(left - label.size.width - 6.dp.toPx(), y - label.size.height / 2f))
                    }
                    // x labels: first / middle / last
                    val n = points.size
                    listOf(0, n / 2, n - 1).distinct().forEach { i ->
                        val label = textMeasurer.measure(points[i].date, axisStyle)
                        val x = (xOf(i, n) - label.size.width / 2f).coerceIn(0f, size.width - label.size.width)
                        drawText(label, topLeft = Offset(x, size.height - label.size.height))
                    }

                    series.forEach { s ->
                        when (s.style) {
                            "bars" -> {
                                val bw = (plotW / n * 0.55f).coerceAtMost(28.dp.toPx())
                                s.values.forEachIndexed { i, v ->
                                    if (v != null) {
                                        val x = xOf(i, n) - bw / 2
                                        val y = yOf(v)
                                        drawRoundRect(s.color, Offset(x, y), Size(bw, top + plotH - y), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
                                    }
                                }
                            }
                            else -> {
                                val path = Path(); var started = false
                                val fillPath = Path()
                                var firstX = 0f; var lastX = 0f
                                s.values.forEachIndexed { i, v ->
                                    if (v != null) {
                                        val x = xOf(i, n); val y = yOf(v)
                                        if (!started) { path.moveTo(x, y); fillPath.moveTo(x, top + plotH); fillPath.lineTo(x, y); firstX = x; started = true }
                                        else { path.lineTo(x, y); fillPath.lineTo(x, y) }
                                        lastX = x
                                    }
                                }
                                if (started) {
                                    if (s.style == "area") {
                                        fillPath.lineTo(lastX, top + plotH); fillPath.close()
                                        drawPath(fillPath, s.color.copy(alpha = 0.18f))
                                    }
                                    drawPath(path, s.color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                    s.values.forEachIndexed { i, v -> if (v != null) drawCircle(s.color, 3.5.dp.toPx(), Offset(xOf(i, n), yOf(v))) }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                // legend
                FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    series.forEach { s ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(s.color))
                            Spacer(Modifier.width(6.dp))
                            Text(s.name, style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                        }
                    }
                }
            }
        }
    }
}
