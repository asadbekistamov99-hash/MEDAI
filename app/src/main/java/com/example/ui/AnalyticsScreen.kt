@file:OptIn(ExperimentalMaterial3Api::class)
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

// --- SCREEN: ANALYTICS & BMI ---

@Composable
fun AnalyticsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val todayMetricsState by viewModel.todayMetrics.collectAsState()
    val allMetricsList by viewModel.allDailyMetrics.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    val tabLabels = listOf("Vazn & BMI ⚖️", "Bosim & Puls 🩸", "Uyqu & Taom 😴")

    // Weight & BMI Inputs
    var heightInput by remember { mutableStateOf("175") }
    var weightInput by remember { mutableStateOf(todayMetricsState?.weight?.let { if (it > 0) it.toString() else "70" } ?: "70") }

    // BP & HR Inputs
    var systolicInput by remember { mutableStateOf("120") }
    var diastolicInput by remember { mutableStateOf("80") }
    var bpmInput by remember { mutableStateOf("72") }

    // Sleep & Meal Inputs
    var sleepHoursInput by remember { mutableStateOf("8.0") }
    var bedtimeInput by remember { mutableStateOf("23:00") }
    var waketimeInput by remember { mutableStateOf("07:00") }

    var mealTitleInput by remember { mutableStateOf("") }
    var mealCaloriesInput by remember { mutableStateOf("450") }
    var mealTypeSelected by remember { mutableStateOf("Breakfast") }

    val isAnalyzingNutrition by viewModel.isAnalyzingNutrition.collectAsState()
    val nutritionAnalysisResult by viewModel.nutritionAdvice.collectAsState()

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("feat_analytics", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Header
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = medai.brand
            ) {
                tabLabels.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeTab) {
                    0 -> {
                        // --- WEIGHT & BMI TAB ---
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "Tana Vazni Indeksi (BMI) Kalkulyatori", fontWeight = FontWeight.Bold, color = medai.brand, fontSize = 15.sp)

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = heightInput,
                                        onValueChange = { heightInput = it },
                                        label = { Text("Bo'y (cm)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = { weightInput = it },
                                        label = { Text("Vazn (kg)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                val heightM = heightInput.toDoubleOrNull()?.div(100.0) ?: 1.75
                                val weightKg = weightInput.toDoubleOrNull() ?: 70.0
                                val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 22.8

                                val (bmiCategory, bmiColor) = when {
                                    bmi < 18.5 -> "Vazn yetarli emas (Underweight)" to Color(0xFF2196F3)
                                    bmi < 25.0 -> "Sog'lom vazn (Normal Weight) ✅" to medai.brand
                                    bmi < 30.0 -> "Ortiqcha vazn (Overweight)" to Color(0xFFFF9800)
                                    else -> "Semizlik (Obese) ⚠️" to medai.danger
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = bmiColor.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, bmiColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Sizning BMI ko'rsatkichingiz:", fontSize = 12.sp, color = medai.textSecondary)
                                        Text(text = String.format("%.1f", bmi), fontWeight = FontWeight.Bold, fontSize = 28.sp, color = bmiColor)
                                        Text(text = bmiCategory, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = bmiColor)
                                    }
                                }

                                Button(
                                    onClick = {
                                        val w = weightInput.toDoubleOrNull()
                                        if (w != null) {
                                            viewModel.logWeightMetrics(w)
                                            Toast.makeText(viewModel.getApplication(), "Vazn muvaffaqiyatli saqlandi!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
                                ) {
                                    Text("Kunlik vaznni saqlash")
                                }
                            }
                        }

                        // Last 7 days Weight chart
                        Text(text = "Oxirgi vazn o'zgarishlari", fontWeight = FontWeight.Bold, color = medai.textSecondary, fontSize = 13.sp)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val recentMetrics = allMetricsList.takeLast(7)
                                if (recentMetrics.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = "Ma'lumotlar kam", color = medai.textSecondary, fontSize = 12.sp)
                                    }
                                } else {
                                    recentMetrics.forEachIndexed { index, metric ->
                                        if (metric.weight > 0) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "${metric.weight.toInt()}", fontSize = 10.sp, color = medai.textPrimary)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(16.dp)
                                                        .height((metric.weight * 1.2).dp.coerceAtMost(100.dp))
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(medai.brand)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = metric.date.takeLast(2), fontSize = 10.sp, color = medai.textSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val trendsLabel0 = when (lang) {
                            "uz" -> "Salomatlik dinamikasi (Recharts & Firestore)"
                            "ru" -> "Динамика здоровья (Recharts & Firestore)"
                            else -> "Health Dynamics (Recharts & Firestore)"
                        }
                        Text(text = trendsLabel0, fontWeight = FontWeight.Bold, color = medai.textSecondary, fontSize = 13.sp)
                        RechartsHealthTrends(viewModel = viewModel)
                    }
                    1 -> {
                        // --- PRESSURE & PULSE TAB ---
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "Qon bosimi va Puls (Yurak urishi)", fontWeight = FontWeight.Bold, color = medai.brand, fontSize = 15.sp)

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = systolicInput,
                                        onValueChange = { systolicInput = it },
                                        label = { Text("Sistolik (mmHg)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = diastolicInput,
                                        onValueChange = { diastolicInput = it },
                                        label = { Text("Diastolik (mmHg)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                OutlinedTextField(
                                    value = bpmInput,
                                    onValueChange = { bpmInput = it },
                                    label = { Text("Puls (BPM - bir daqiqada)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                val sys = systolicInput.toIntOrNull() ?: 120
                                val dia = diastolicInput.toIntOrNull() ?: 80

                                val (bpStatus, bpColor) = when {
                                    sys < 120 && dia < 80 -> "Qon bosimi ideal darajada! ✅" to medai.brand
                                    sys in 120..129 && dia < 80 -> "Normal darajadan biroz yuqori (Pre-gipertoniya)" to Color(0xFFFF9800)
                                    else -> "Yuqori qon bosimi (Gipertoniya) ⚠️ Shifokor bilan maslahatlashing." to medai.danger
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = bpColor.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, bpColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Tahlil:", fontSize = 11.sp, color = medai.textSecondary)
                                        Text(text = bpStatus, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = bpColor, textAlign = TextAlign.Center)
                                    }
                                }

                                Button(
                                    onClick = {
                                        val sysVal = systolicInput.toIntOrNull()
                                        val diaVal = diastolicInput.toIntOrNull()
                                        val bpmVal = bpmInput.toIntOrNull()
                                        if (sysVal != null && diaVal != null && bpmVal != null) {
                                            viewModel.logBloodPressureMetrics(sysVal, diaVal)
                                            viewModel.logHeartRateMetrics(bpmVal)
                                            Toast.makeText(viewModel.getApplication(), "Ko'rsatkichlar saqlandi!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
                                ) {
                                    Text("Bosim va pulsni saqlash")
                                }
                            }
                        }

                        // Last 7 days Pulse Rate Chart
                        Text(text = "Puls (Yurak urishi) o'zgarishi (BPM)", fontWeight = FontWeight.Bold, color = medai.textSecondary, fontSize = 13.sp)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val recentMetrics = allMetricsList.takeLast(7)
                                if (recentMetrics.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = "Ma'lumotlar kam", color = medai.textSecondary, fontSize = 12.sp)
                                    }
                                } else {
                                    recentMetrics.forEachIndexed { index, metric ->
                                        if (metric.heartRate > 0) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "${metric.heartRate}", fontSize = 10.sp, color = medai.textPrimary)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(14.dp)
                                                        .height((metric.heartRate * 1.1).dp.coerceAtMost(100.dp))
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(Color(0xFFE91E63))
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = metric.date.takeLast(2), fontSize = 10.sp, color = medai.textSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val trendsLabel1 = when (lang) {
                            "uz" -> "Salomatlik dinamikasi (Recharts & Firestore)"
                            "ru" -> "Динамика здоровья (Recharts & Firestore)"
                            else -> "Health Dynamics (Recharts & Firestore)"
                        }
                        Text(text = trendsLabel1, fontWeight = FontWeight.Bold, color = medai.textSecondary, fontSize = 13.sp)
                        RechartsHealthTrends(viewModel = viewModel)
                    }
                    2 -> {
                        // --- SLEEP & NUTRITION TAB ---
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "Kunlik uyqu va ovqatlanish jurnali", fontWeight = FontWeight.Bold, color = medai.brand, fontSize = 15.sp)

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = sleepHoursInput,
                                        onValueChange = { sleepHoursInput = it },
                                        label = { Text("Uyqu (soat)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = bedtimeInput,
                                        onValueChange = { bedtimeInput = it },
                                        label = { Text("Yotish vaqti") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = waketimeInput,
                                        onValueChange = { waketimeInput = it },
                                        label = { Text("Uyg'onish") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Button(
                                    onClick = {
                                        val hours = sleepHoursInput.toDoubleOrNull() ?: 8.0
                                        viewModel.logSleepMetrics(hours, bedtimeInput, waketimeInput)
                                        Toast.makeText(viewModel.getApplication(), "Uyqu jurnali yangilandi!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
                                ) {
                                    Text("Uyquni saqlash")
                                }

                                Divider()

                                // Food Input Section
                                Text(text = "Taom qo'shish", fontWeight = FontWeight.Bold, color = medai.brand, fontSize = 13.sp)

                                OutlinedTextField(
                                    value = mealTitleInput,
                                    onValueChange = { mealTitleInput = it },
                                    label = { Text("Taom nomi (masalan: Lag'mon, Olma)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = mealCaloriesInput,
                                        onValueChange = { mealCaloriesInput = it },
                                        label = { Text("Kalloriya (kcal)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf("Breakfast", "Lunch", "Dinner").forEach { type ->
                                                FilterChip(
                                                    selected = mealTypeSelected == type,
                                                    onClick = { mealTypeSelected = type },
                                                    label = { Text(type, fontSize = 10.sp) }
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        val cal = mealCaloriesInput.toIntOrNull() ?: 450
                                        if (mealTitleInput.isNotBlank()) {
                                            viewModel.logMealMetrics(mealTitleInput, cal, mealTypeSelected)
                                            mealTitleInput = ""
                                            Toast.makeText(viewModel.getApplication(), "Taom jurnali qo'shildi!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
                                ) {
                                    Text("Taomni qo'shish")
                                }
                            }
                        }

                        // Daily Meal summaries card
                        val totalCalories = todayMetricsState?.mealsJson?.let {
                            try {
                                val arr = org.json.JSONArray(it)
                                var sum = 0
                                for (i in 0 until arr.length()) {
                                    sum += arr.getJSONObject(i).getInt("calories")
                                }
                                sum
                            } catch(e: Exception) { 0 }
                        } ?: 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Bugungi ovqatlanish", fontWeight = FontWeight.Bold, color = medai.textPrimary)
                                    Text(text = "$totalCalories / 2000 kcal", fontWeight = FontWeight.Bold, color = medai.brand)
                                }

                                LinearProgressIndicator(
                                    progress = (totalCalories.toFloat() / 2000f).coerceAtMost(1f),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = medai.brand,
                                    trackColor = medai.border
                                )

                                val mealList = remember(todayMetricsState?.mealsJson) {
                                    val list = mutableListOf<Triple<String, String, Int>>()
                                    todayMetricsState?.mealsJson?.let { json ->
                                        try {
                                            val arr = org.json.JSONArray(json)
                                            for (i in 0 until arr.length()) {
                                                val obj = arr.getJSONObject(i)
                                                list.add(Triple(
                                                    obj.optString("title", ""),
                                                    obj.optString("type", ""),
                                                    obj.optInt("calories", 0)
                                                ))
                                            }
                                        } catch(e: Exception) {}
                                    }
                                    list
                                }

                                mealList.forEach { (title, type, calories) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "• $title ($type)", fontSize = 12.sp, color = medai.textSecondary)
                                        Text(text = "$calories kcal", fontSize = 12.sp, color = medai.textPrimary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        // AI Nutrition Coach Advice Trigger
                        Button(
                            onClick = { viewModel.analyzeNutritionDaily() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = medai.premium)
                        ) {
                            if (isAnalyzingNutrition) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI Parhez tavsiyasi (Gemini)")
                                }
                            }
                        }

                        if (nutritionAnalysisResult.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = medai.premium.copy(alpha = 0.1f)),
                                border = BorderStroke(1.dp, medai.premium)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "🥗 AI Parhezshunos maslahati:", fontWeight = FontWeight.Bold, color = medai.premium, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = nutritionAnalysisResult, fontSize = 13.sp, color = medai.textPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RechartsHealthTrends(viewModel: AppViewModel) {
    val firestoreVitals by viewModel.firestoreVitals.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()

    val titleText = when (lang) {
        "uz" -> "Salomatlik ko'rsatkichlari dinamikasi"
        "ru" -> "Динамика показателей здоровья"
        else -> "Health Vitals Dynamics"
    }

    val tabAllText = when (lang) {
        "uz" -> "Hammasi"
        "ru" -> "Все"
        else -> "All"
    }

    val jsonArray = org.json.JSONArray()
    firestoreVitals.forEach { r ->
        val obj = org.json.JSONObject()
        obj.put("date", r.date)
        obj.put("heartRate", r.heartRate)
        obj.put("bpSystolic", r.bpSystolic)
        obj.put("bpDiastolic", r.bpDiastolic)
        obj.put("weight", r.weight)
        obj.put("note", r.note)
        jsonArray.put(obj)
    }
    val jsonString = jsonArray.toString()

    val htmlTemplate = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <!-- Load Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary: '#2E7D32',
                        accent: '#E91E63',
                        slateDark: '#121212'
                    }
                }
            }
        }
    </script>
    <!-- Load React, ReactDOM, Recharts, and Babel for JSX -->
    <script src="https://unpkg.com/react@18/umd/react.production.min.js" crossorigin></script>
    <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js" crossorigin></script>
    <script src="https://unpkg.com/prop-types@15.8.1/prop-types.min.js"></script>
    <script src="https://unpkg.com/recharts@2.12.7/umd/Recharts.js"></script>
    <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
    <style>
        body {
            background-color: #121212;
            color: #ffffff;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            margin: 0;
            padding: 0;
            overflow-x: hidden;
        }
    </style>
</head>
<body>
    <div id="chart-root"></div>

    <script type="text/babel">
        const { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, AreaChart, Area } = Recharts;

        const rawData = $jsonString;

        function HealthTrendsApp() {
            const [view, setView] = React.useState('all');

            const chartData = rawData.map(d => ({
                date: d.date.split('-').slice(1).join('/'),
                systolic: d.bpSystolic > 0 ? d.bpSystolic : null,
                diastolic: d.bpDiastolic > 0 ? d.bpDiastolic : null,
                heartRate: d.heartRate > 0 ? d.heartRate : null,
                weight: d.weight > 0 ? d.weight : null
            })).filter(d => d.systolic !== null || d.diastolic !== null || d.heartRate !== null || d.weight !== null);

            return (
                <div className="p-4 bg-slateDark rounded-2xl">
                    <div className="flex justify-between items-center mb-6">
                        <h2 className="text-sm font-bold text-white flex items-center gap-2">
                            <span>📈</span>
                            <span>$titleText</span>
                        </h2>
                        
                        <div className="flex gap-1 bg-[#1e1e1e] p-1 rounded-lg text-xs">
                            <button 
                                onClick={() => setView('all')}
                                className={view === 'all' ? "px-2 py-1 rounded-md transition bg-primary text-white font-bold" : "px-2 py-1 rounded-md transition text-gray-400"}
                            >
                                $tabAllText
                            </button>
                            <button 
                                onClick={() => setView('bp')}
                                className={view === 'bp' ? "px-2 py-1 rounded-md transition bg-primary text-white font-bold" : "px-2 py-1 rounded-md transition text-gray-400"}
                            >
                                BP
                            </button>
                            <button 
                                onClick={() => setView('hr')}
                                className={view === 'hr' ? "px-2 py-1 rounded-md transition bg-primary text-white font-bold" : "px-2 py-1 rounded-md transition text-gray-400"}
                            >
                                HR
                            </button>
                            <button 
                                onClick={() => setView('weight')}
                                className={view === 'weight' ? "px-2 py-1 rounded-md transition bg-primary text-white font-bold" : "px-2 py-1 rounded-md transition text-gray-400"}
                            >
                                WT
                            </button>
                        </div>
                    </div>

                    <div className="h-64 w-full">
                        {chartData.length === 0 ? (
                            <div className="h-full flex items-center justify-center text-gray-500 text-xs text-center p-4">
                                Sog'lomlashtirish ma'lumotlari yuklanmoqda... / Loading historical vital data...
                            </div>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                {view === 'all' && (
                                    <LineChart data={chartData} margin={{ top: 5, right: 10, left: -25, bottom: 5 }}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="#2a2a2a" />
                                        <XAxis dataKey="date" stroke="#888888" fontSize={10} />
                                        <YAxis stroke="#888888" fontSize={10} />
                                        <Tooltip contentStyle={{ backgroundColor: '#222', borderColor: '#444' }} />
                                        <Legend wrapperStyle={{ fontSize: 10 }} />
                                        <Line type="monotone" dataKey="systolic" name="Sys" stroke="#4CAF50" strokeWidth={2.5} dot={{ r: 3 }} />
                                        <Line type="monotone" dataKey="diastolic" name="Dia" stroke="#81C784" strokeWidth={2} dot={{ r: 2 }} />
                                        <Line type="monotone" dataKey="heartRate" name="BPM" stroke="#E91E63" strokeWidth={2} dot={{ r: 2 }} />
                                    </LineChart>
                                )}

                                {view === 'bp' && (
                                    <LineChart data={chartData} margin={{ top: 5, right: 10, left: -25, bottom: 5 }}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="#2a2a2a" />
                                        <XAxis dataKey="date" stroke="#888888" fontSize={10} />
                                        <YAxis domain={[40, 200]} stroke="#888888" fontSize={10} />
                                        <Tooltip contentStyle={{ backgroundColor: '#222', borderColor: '#444' }} />
                                        <Legend wrapperStyle={{ fontSize: 10 }} />
                                        <Line type="monotone" dataKey="systolic" name="Systolic" stroke="#4CAF50" strokeWidth={3} activeDot={{ r: 5 }} />
                                        <Line type="monotone" dataKey="diastolic" name="Diastolic" stroke="#2196F3" strokeWidth={2.5} activeDot={{ r: 4 }} />
                                    </LineChart>
                                )}

                                {view === 'hr' && (
                                    <AreaChart data={chartData} margin={{ top: 5, right: 10, left: -25, bottom: 5 }}>
                                        <defs>
                                            <linearGradient id="colorHr" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#E91E63" stopOpacity={0.4}/>
                                                <stop offset="95%" stopColor="#E91E63" stopOpacity={0}/>
                                            </linearGradient>
                                        </defs>
                                        <CartesianGrid strokeDasharray="3 3" stroke="#2a2a2a" />
                                        <XAxis dataKey="date" stroke="#888888" fontSize={10} />
                                        <YAxis domain={[40, 150]} stroke="#888888" fontSize={10} />
                                        <Tooltip contentStyle={{ backgroundColor: '#222', borderColor: '#444' }} />
                                        <Legend wrapperStyle={{ fontSize: 10 }} />
                                        <Area type="monotone" dataKey="heartRate" name="Heart Rate" stroke="#E91E63" fillOpacity={1} fill="url(#colorHr)" strokeWidth={3} />
                                    </AreaChart>
                                )}

                                {view === 'weight' && (
                                    <BarChart data={chartData} margin={{ top: 5, right: 10, left: -25, bottom: 5 }}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="#2a2a2a" />
                                        <XAxis dataKey="date" stroke="#888888" fontSize={10} />
                                        <YAxis domain={['dataMin - 5', 'dataMax + 5']} stroke="#888888" fontSize={10} />
                                        <Tooltip contentStyle={{ backgroundColor: '#222', borderColor: '#444' }} />
                                        <Legend wrapperStyle={{ fontSize: 10 }} />
                                        <Bar dataKey="weight" name="Weight (kg)" fill="#4CAF50" radius={[4, 4, 0, 0]} />
                                    </BarChart>
                                )}
                            </ResponsiveContainer>
                        )}
                    </div>
                </div>
            );
        }

        const root = ReactDOM.createRoot(document.getElementById('chart-root'));
        root.render(<HealthTrendsApp />);
    </script>
</body>
</html>
    """.trimIndent()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    webViewClient = WebViewClient()
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://localhost", htmlTemplate, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
