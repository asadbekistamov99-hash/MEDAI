@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReminderLocal
import com.example.i18n.Translations
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// Distinct accent used for the Statistika tab (indigo), sitting alongside the app's
// existing PrimaryGreen / AccentCyan / WarningOrange tokens without adding a new theme token.
private val TabStatsAccent = Color(0xFF6366F1)

/**
 * MedAI Yordamchi — All-in-One Smart Medical Assistant
 * Features:
 * 1. Dori Aniqlash (Pill Identification & Drug Leaflet)
 * 2. Simptom Aniqlash (2-Step Dynamic Clinical Questionnaire & Triage)
 * 3. Statistika (Medication Adherence 4-Box Stats: Qabul qilindi, Kechikib qabul, O'tkazib yuborildi, Muntazamlik)
 * 4. Reminder (Medication Reminders Scheduling & Timeline)
 */
@Composable
fun MedAIYordamchiScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val lang by viewModel.currentLanguage.collectAsState()

    val tabs = listOf(
        "Dori Aniqlash" to Icons.Default.Medication,
        "Simptom Aniqlash" to Icons.Default.HealthAndSafety,
        "Statistika" to Icons.Default.BarChart,
        "Reminder" to Icons.Default.Alarm
    )
    // Distinct brand-consistent accent per tab (PrimaryGreen stays the anchor for the core feature)
    val tabAccentColors = listOf(PrimaryGreen, AccentCyan, TabStatsAccent, WarningOrange)

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Orqaga",
                                tint = PrimaryGreen
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MedAI",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = DarkGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "YORDAMCHI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Top Tab Navigation Bar — polished segmented pill switcher, each tab
                    // carrying its own accent color (matching MedicalBottomNavigation's
                    // selected/unselected pattern).
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .background(MedicalBackground, RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tabs.forEachIndexed { index, (title, icon) ->
                            val isSelected = selectedTab == index
                            val accent = tabAccentColors[index]
                            Surface(
                                onClick = { selectedTab = index },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) accent else Color.Transparent,
                                shadowElevation = if (isSelected) 3.dp else 0.dp,
                                border = if (isSelected) null else BorderStroke(1.dp, MedicalBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MedicalBackground)
        ) {
            when (selectedTab) {
                0 -> DoriAniqlashTab(viewModel)
                1 -> SimptomAniqlashTab(viewModel)
                2 -> StatistikaTab(viewModel)
                3 -> ReminderTab(viewModel)
            }
        }
    }
}

/* ============================================================
   TAB 1 — Dori Aniqlash
   ============================================================ */
@Composable
fun DoriAniqlashTab(viewModel: AppViewModel) {
    val isIdentifying by viewModel.isIdentifyingPill.collectAsState()
    val pillResult by viewModel.pillIdentifyResult.collectAsState()
    var drugInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val quickPills = listOf("Paracetamol", "Tsitramon", "Ibuprofen", "Amoksitsillin", "No-shpa", "Lisinopril")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(PrimaryGreen.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Dori vositasini aniqlash",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Dori nomi bo'yicha to'liq tibbiy yo'riqnomani oling",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = drugInput,
                        onValueChange = { drugInput = it },
                        placeholder = { Text("Dori nomini kiriting (masalan: Paracetamol)...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (drugInput.isNotBlank()) {
                                viewModel.identifyPill(drugInput.trim())
                            } else {
                                Toast.makeText(context, "Iltimos, dori nomini kiriting", Toast.LENGTH_SHORT).show()
                            }
                        }),
                        trailingIcon = {
                            if (drugInput.isNotEmpty()) {
                                IconButton(onClick = { drugInput = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Tozalash")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick drug chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPills.forEach { pill ->
                            SuggestionChip(
                                onClick = {
                                    drugInput = pill
                                    viewModel.identifyPill(pill)
                                },
                                label = { Text(pill, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (drugInput.isNotBlank()) {
                                viewModel.identifyPill(drugInput.trim())
                            } else {
                                Toast.makeText(context, "Iltimos, dori nomini kiriting", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isIdentifying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        if (isIdentifying) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Dori ma'lumotlari izlanmoqda...",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ma'lumot olish",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Result display
        if (pillResult.isNotEmpty()) {
            item {
                ResultCard(
                    title = "Dori yo'rig'i",
                    text = pillResult
                )
            }
        }
    }
}

/* ============================================================
   TAB 2 — Simptom Aniqlash (2-Step Smart Triage)
   ============================================================ */
@Composable
fun SimptomAniqlashTab(viewModel: AppViewModel) {
    var complaintInput by remember { mutableStateOf("") }
    val isLoadingQuestions by viewModel.isLoadingSymptomQuestions.collectAsState()
    val questionsData by viewModel.symptomQuestionsData.collectAsState()
    val isAnalyzingAnswers by viewModel.isAnalyzingSymptomAnswers.collectAsState()
    val dynamicAnalysisResult by viewModel.symptomDynamicAnalysisResult.collectAsState()

    var userAnswers by remember { mutableStateOf(listOf<String>()) }
    val context = LocalContext.current

    LaunchedEffect(questionsData) {
        if (questionsData != null) {
            userAnswers = List(questionsData?.questions?.size ?: 0) { "" }
        }
    }

    val quickComplaints = listOf(
        "Boshim qattiq og'riyapti va ko'nglim aynyapti",
        "Tana haroratim 38.5, quruq yo'tal bezovta qilyapti",
        "Qornimning o'ng pastki qismida sanchuvchi og'riq bor",
        "Yuragim tez urib, nafas yetishmayapti",
        "Belim qattiq og'rib, oyog'imga beryapti"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: Input Complaint
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(AccentCyan.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "1-Qadam: Shikoyatingizni yozing",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "AI sizga mos shifokorlik savollarini tayyorlaydi",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = complaintInput,
                        onValueChange = { complaintInput = it },
                        placeholder = { Text("Qayeringiz og'riyapti yoki qanday alomatlar bor?...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick complaints chips
                    Text(text = "Tezkor misollar:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickComplaints.forEach { c ->
                            SuggestionChip(
                                onClick = { complaintInput = c },
                                label = { Text(c.take(28) + "...", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (complaintInput.isNotBlank()) {
                                viewModel.getSymptomQuestions(complaintInput.trim())
                            } else {
                                Toast.makeText(context, "Iltimos, shikoyatingizni kiriting", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isLoadingQuestions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        if (isLoadingQuestions) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Savollar yuklanmoqda...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(imageVector = Icons.Default.Quiz, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Savollarni olish", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Step 2: Dynamic Questions List
        if (questionsData != null) {
            val qData = questionsData!!
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MedicalBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "2-Qadam: Qo'shimcha savollar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            // Title / Medical Name badge
                            Surface(
                                color = AccentCyan.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (qData.medicalName.isNotBlank()) "${qData.title} • ${qData.medicalName}" else qData.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            qData.questions.forEachIndexed { index, question ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MedicalBackground,
                                    border = BorderStroke(1.dp, MedicalBorder)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(AccentCyan.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentCyan
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = question,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = userAnswers.getOrElse(index) { "" },
                                            onValueChange = { newVal ->
                                                val mutableList = userAnswers.toMutableList()
                                                if (index < mutableList.size) {
                                                    mutableList[index] = newVal
                                                } else {
                                                    while (mutableList.size < index) mutableList.add("")
                                                    mutableList.add(newVal)
                                                }
                                                userAnswers = mutableList
                                            },
                                            placeholder = { Text("Javob yozing...", fontSize = 13.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                                focusedContainerColor = MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.analyzeSymptomAnswers(
                                    complaint = complaintInput,
                                    questions = qData.questions,
                                    answers = userAnswers
                                )
                            },
                            enabled = !isAnalyzingAnswers,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                        ) {
                            if (isAnalyzingAnswers) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "Tahlil qilinmoqda...", fontWeight = FontWeight.Bold, color = Color.White)
                            } else {
                                Icon(imageVector = Icons.Default.FactCheck, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Tahlil qilish", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Final Diagnostic Result Card
        if (dynamicAnalysisResult.isNotEmpty()) {
            item {
                ResultCard(
                    title = "Tahlil natijasi",
                    text = dynamicAnalysisResult
                )
            }
        }
    }
}

/* ============================================================
   TAB 3 — Statistika
   ============================================================ */
@Composable
fun StatistikaTab(viewModel: AppViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    val dailyMetrics by viewModel.allDailyMetrics.collectAsState()
    val symptomChecks by viewModel.symptomChecks.collectAsState()
    val context = LocalContext.current
    var showResetSuccess by remember { mutableStateOf(false) }

    // Calculated adherence metrics based on real records (starts strictly at 0)
    val totalRemindersCount = reminders.size
    val tookCount = dailyMetrics.sumOf { m ->
        try { org.json.JSONArray(m.completedRemindersJson).length() } catch(e: Exception) { 0 }
    }
    val lateTookCount = 0
    val missedCount = if (totalRemindersCount > tookCount) totalRemindersCount - tookCount else 0
    val totalScheduled = tookCount + missedCount
    val adherencePercent = if (totalScheduled > 0) {
        ((tookCount.toDouble() / totalScheduled) * 100.0).coerceIn(0.0, 100.0)
    } else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(TabStatsAccent.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = TabStatsAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dori va salomatlik statistikasi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Foydalanish va intizomga qarab oshib boruvchi statistika",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4-Box Stats Grid (Starts at 0, grows with adherence)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBox(
                            value = "$tookCount",
                            label = "Qabul qilindi",
                            color = PrimaryGreen,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            value = "$lateTookCount",
                            label = "Kechikib qabul",
                            color = WarningOrange,
                            icon = Icons.Default.Schedule,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBox(
                            value = "$missedCount",
                            label = "O'tkazib yuborildi",
                            color = ErrorRed,
                            icon = Icons.Default.Cancel,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            value = String.format(java.util.Locale.US, "%.1f%%", adherencePercent),
                            label = "Muntazamlik",
                            color = TabStatsAccent,
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Action card: Reset Statistics button requested by user
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Statistikani boshqarish",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Agar barcha hisoblagichlarni 0 ga tushirmoqchi bo'lsangiz, quyidagi tugmani bosing.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.resetAllStatistics {
                                showResetSuccess = true
                                Toast.makeText(context, "Hamma statistika 0 ga tushirildi ✅", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ErrorRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Hamma statistikani 0 ga tushirish", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Summary insights card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Umumiy ko'rsatkichlar".uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TabStatsAccent,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Rejalashtirilgan eslatmalar soni:", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "$totalRemindersCount ta", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Tekshirilgan simptomlar:", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "${symptomChecks.size} ta", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Davolanish intizomi bahosi:", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            text = when {
                                adherencePercent >= 80.0 -> "A'lo darajada"
                                adherencePercent > 0.0 -> "Yaxshi"
                                else -> "Boshlang'ich (0%)"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adherencePercent > 0) PrimaryGreen else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(
    value: String,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/* ============================================================
   TAB 4 — Reminder (Dori eslatmalari)
   ============================================================ */
@Composable
fun ReminderTab(viewModel: AppViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    val todayMetrics by viewModel.todayMetrics.collectAsState(initial = null)
    val completedReminderIds = remember(todayMetrics) {
        try {
            val arr = org.json.JSONArray(todayMetrics?.completedRemindersJson ?: "[]")
            (0 until arr.length()).map { arr.getInt(it) }.toSet()
        } catch (e: Exception) {
            emptySet<Int>()
        }
    }
    var medNameInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("08:00") }
    var isSaving by remember { mutableStateOf(false) }
    var savedSuccessMsg by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val quickTimes = listOf("08:00", "13:00", "18:00", "21:00")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(WarningOrange.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Yangi eslatma qo'shish",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Dorilarni o'z vaqtida ichish uchun eslatma o'rnating",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Dori nomi", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = medNameInput,
                        onValueChange = { medNameInput = it },
                        placeholder = { Text("Dori nomini kiriting (masalan: Lisinopril 10mg)...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Qabul qilish vaqti (HH:mm)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        placeholder = { Text("08:00") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = WarningOrange)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick time picker chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickTimes.forEach { t ->
                            FilterChip(
                                selected = timeInput == t,
                                onClick = { timeInput = t },
                                label = { Text(t, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarningOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (medNameInput.isBlank()) {
                                Toast.makeText(context, "Iltimos, dori nomini kiriting", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (timeInput.isBlank()) {
                                Toast.makeText(context, "Iltimos, vaqtni tanlang", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSaving = true
                            coroutineScope.launch {
                                viewModel.addSimpleReminder(medNameInput.trim(), timeInput.trim())
                                isSaving = false
                                savedSuccessMsg = "Eslatma muvaffaqiyatli saqlandi!"
                                medNameInput = ""
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Eslatma saqlanmoqda...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(imageVector = Icons.Default.AddAlarm, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Eslatmani saqlash", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Success banner
        if (savedSuccessMsg.isNotEmpty()) {
            item {
                Surface(
                    color = SuccessGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(22.dp).background(SuccessGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = savedSuccessMsg, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }
            }
        }

        // Reminders List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rejalashtirilgan eslatmalar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    color = WarningOrange.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${reminders.size} ta",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (reminders.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MedicalBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(WarningOrange.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = WarningOrange.copy(alpha = 0.7f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hali eslatmalar yo'q",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                ReminderItemCard(
                    reminder = reminder,
                    isCompleted = completedReminderIds.contains(reminder.id),
                    onComplete = { viewModel.completeReminder(reminder.id) },
                    onToggle = { viewModel.toggleReminderActive(reminder) },
                    onDelete = { viewModel.deleteReminder(reminder.id) }
                )
            }
        }
    }
}

@Composable
fun ReminderItemCard(
    reminder: ReminderLocal,
    isCompleted: Boolean,
    onComplete: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val contentAlpha = if (reminder.isActive) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isCompleted) PrimaryGreen.copy(alpha = 0.5f) else MedicalBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f).alpha(contentAlpha)
                ) {
                    // Time badge with alarm icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(WarningOrange.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = reminder.time,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarningOrange
                            )
                        }
                    }

                    Column {
                        Text(
                            text = reminder.medicineName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = reminder.frequency,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = reminder.isActive,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen
                        )
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "O'chirish",
                            tint = ErrorRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Mark as taken or taken badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCompleted) {
                    Surface(
                        color = PrimaryGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bugun qabul qilindi (+10 ball)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Qabul qildim",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/* ============================================================
   Helper: Result Card matching the web success card
   ============================================================ */
@Composable
fun ResultCard(
    title: String,
    text: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(PrimaryGreen, CircleShape)
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MedicalBorder)
            Spacer(modifier = Modifier.height(14.dp))

            RichMarkdownText(text = text)
        }
    }
}
