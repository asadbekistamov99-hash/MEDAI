@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ReminderLocal
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.MedAIElevation
import com.example.ui.theme.MedAIText
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MedAITint
import com.example.ui.theme.MinTouch
import kotlinx.coroutines.launch

/**
 * MedAI Yordamchi: all-in-one medical assistant.
 * 1. Pill identification and drug leaflet
 * 2. Symptom detection (2-step questionnaire)
 * 3. Statistics (medication adherence)
 * 4. Reminders
 */
@Composable
fun MedAIYordamchiScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    initialTab: Int = 0
) {
    val c = MedAITheme.colors

    var selectedTab by remember { mutableStateOf(initialTab) }
    val lang by viewModel.currentLanguage.collectAsState()

    val tabs = remember(lang) {
        listOf(
            supportText(lang, "Dori aniqlash", "Лекарства", "Pill ID"),
            supportText(lang, "Simptomlar", "Симптомы", "Symptoms"),
            supportText(lang, "Statistika", "Статистика", "Statistics"),
            supportText(lang, "Eslatmalar", "График приёма", "Reminders"),
        )
    }

    Scaffold(
        containerColor = c.canvas,
        topBar = {
            Column {
                AppHeader(
                    title = supportText(lang, "MedAI Yordamchi", "Помощник MedAI", "MedAI Assistant"),
                    onBack = onBack,
                )
                YordamchiTabs(tabs, selectedTab) { selectedTab = it }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(c.canvas)
                .imePadding()
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

private val TabContentPadding = PaddingValues(16.dp)

/* ============================================================
   TAB 1: Dori Aniqlash
   ============================================================ */
@Composable
fun DoriAniqlashTab(viewModel: AppViewModel) {
    val c = MedAITheme.colors
    val lang by viewModel.currentLanguage.collectAsState()

    val isIdentifying by viewModel.isIdentifyingPill.collectAsState()
    val pillResult by viewModel.pillIdentifyResult.collectAsState()
    var drugInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val quickPills = remember { listOf("Paracetamol", "Tsitramon", "Ibuprofen", "Amoksitsillin", "No-shpa", "Lisinopril") }
    val emptyToast = supportText(lang, "Iltimos, dori nomini kiriting", "Пожалуйста, введите название лекарства", "Please enter the medicine name")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = TabContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "input") {
            MedAICard {
                YordamchiCardTitle(
                    icon = Icons.Default.Medication,
                    title = supportText(lang, "Dori vositasini aniqlash", "Определение лекарства", "Identify a medicine"),
                    subtitle = supportText(
                        lang,
                        "Dori nomi bo'yicha to'liq tibbiy yo'riqnomani oling",
                        "Получите полную инструкцию по названию лекарства",
                        "Get the full medical leaflet by medicine name",
                    ),
                    tint = c.tintTeal,
                )

                Spacer(Modifier.height(16.dp))

                YordamchiField(
                    value = drugInput,
                    onValueChange = { drugInput = it },
                    label = supportText(lang, "Dori nomi", "Название лекарства", "Medicine name"),
                    placeholder = supportText(lang, "Masalan: Paracetamol", "Например: Парацетамол", "e.g. Paracetamol"),
                    leadingIcon = Icons.Default.Search,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (drugInput.isNotBlank()) {
                            viewModel.identifyPill(drugInput.trim())
                        } else {
                            Toast.makeText(context, emptyToast, Toast.LENGTH_SHORT).show()
                        }
                    }),
                    trailingContent = if (drugInput.isNotEmpty()) {
                        {
                            Box(
                                Modifier.size(MinTouch).clip(CircleShape).clickable(role = Role.Button) { drugInput = "" },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = supportText(lang, "Tozalash", "Очистить", "Clear"),
                                    tint = c.textSecondary,
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    supportText(lang, "Tezkor tanlov", "Быстрый выбор", "Quick picks"),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textSecondary,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickPills.forEach { pill ->
                        MedAIFilterChip(
                            text = pill,
                            selected = false,
                            onClick = {
                                drugInput = pill
                                viewModel.identifyPill(pill)
                            },
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                MedAIPrimaryButton(
                    text = if (isIdentifying) {
                        supportText(lang, "Dori ma'lumotlari izlanmoqda...", "Ищем информацию о лекарстве...", "Looking up medicine info...")
                    } else {
                        supportText(lang, "Ma'lumot olish", "Получить информацию", "Get information")
                    },
                    onClick = {
                        if (drugInput.isNotBlank()) {
                            viewModel.identifyPill(drugInput.trim())
                        } else {
                            Toast.makeText(context, emptyToast, Toast.LENGTH_SHORT).show()
                        }
                    },
                    icon = Icons.Default.Search,
                    enabled = !isIdentifying,
                    loading = isIdentifying,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Result display
        if (pillResult.isNotEmpty()) {
            item(key = "result") {
                ResultCard(
                    title = supportText(lang, "Dori yo'riqnomasi", "Инструкция к лекарству", "Medicine leaflet"),
                    text = pillResult,
                )
            }
            item(key = "disclaimer") {
                MedAIInfoBanner(
                    text = supportText(
                        lang,
                        "Ma'lumot AI tomonidan tayyorlangan va umumiy xarakterga ega. Dori ichishdan oldin shifokor yoki farmatsevt bilan maslahatlashing.",
                        "Информация подготовлена ИИ и носит общий характер. Перед приёмом лекарства проконсультируйтесь с врачом или фармацевтом.",
                        "This information is AI-generated and general in nature. Consult a doctor or pharmacist before taking any medicine.",
                    ),
                    tone = MedAITone.Warning,
                )
            }
        }
    }
}

/* ============================================================
   TAB 2: Simptom Aniqlash (2-step triage)
   ============================================================ */
@Composable
fun SimptomAniqlashTab(viewModel: AppViewModel) {
    val c = MedAITheme.colors
    val lang by viewModel.currentLanguage.collectAsState()

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

    val quickComplaints = remember(lang) {
        when (lang) {
            "ru" -> listOf(
                "Сильно болит голова и тошнит",
                "Температура 38,5, беспокоит сухой кашель",
                "Колющая боль в правом нижнем отделе живота",
                "Сердце быстро бьётся, не хватает воздуха",
                "Сильно болит поясница, боль отдаёт в ногу",
            )
            "en" -> listOf(
                "I have a severe headache and feel nauseous",
                "My temperature is 38.5 and a dry cough bothers me",
                "Stabbing pain in the lower right of my abdomen",
                "My heart is racing and I am short of breath",
                "Severe lower back pain spreading to my leg",
            )
            else -> listOf(
                "Boshim qattiq og'riyapti va ko'nglim aynyapti",
                "Tana haroratim 38.5, quruq yo'tal bezovta qilyapti",
                "Qornimning o'ng pastki qismida sanchuvchi og'riq bor",
                "Yuragim tez urib, nafas yetishmayapti",
                "Belim qattiq og'rib, oyog'imga beryapti",
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = TabContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: input complaint
        item(key = "step1") {
            MedAICard {
                YordamchiCardTitle(
                    icon = Icons.Default.HealthAndSafety,
                    title = supportText(lang, "1-qadam: Shikoyatingizni yozing", "Шаг 1: опишите жалобы", "Step 1: describe your symptoms"),
                    subtitle = supportText(
                        lang,
                        "AI sizga mos shifokorlik savollarini tayyorlaydi",
                        "ИИ подготовит подходящие вопросы врача",
                        "AI will prepare relevant clinical questions",
                    ),
                    tint = c.tintSky,
                )

                Spacer(Modifier.height(16.dp))

                YordamchiField(
                    value = complaintInput,
                    onValueChange = { complaintInput = it },
                    label = supportText(lang, "Shikoyat", "Жалобы", "Symptoms"),
                    placeholder = supportText(
                        lang,
                        "Qayeringiz og'riyapti yoki qanday alomatlar bor?",
                        "Что болит или какие симптомы вас беспокоят?",
                        "Where does it hurt, or what symptoms do you have?",
                    ),
                    singleLine = false,
                    minHeight = 96.dp,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    supportText(lang, "Tezkor misollar", "Быстрые примеры", "Quick examples"),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textSecondary,
                )
                Column {
                    quickComplaints.forEachIndexed { i, sample ->
                        if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = MinTouch)
                                .clickable(role = Role.Button) { complaintInput = sample }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = c.brand, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(sample, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                MedAIPrimaryButton(
                    text = if (isLoadingQuestions) {
                        supportText(lang, "Savollar yuklanmoqda...", "Загрузка вопросов...", "Loading questions...")
                    } else {
                        supportText(lang, "Savollarni olish", "Получить вопросы", "Get questions")
                    },
                    onClick = {
                        if (complaintInput.isNotBlank()) {
                            viewModel.getSymptomQuestions(complaintInput.trim())
                        } else {
                            Toast.makeText(
                                context,
                                supportText(lang, "Iltimos, shikoyatingizni kiriting", "Пожалуйста, опишите жалобы", "Please describe your symptoms"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    icon = Icons.Default.Quiz,
                    enabled = !isLoadingQuestions,
                    loading = isLoadingQuestions,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Step 2: dynamic questions
        if (questionsData != null) {
            val qData = questionsData!!
            item(key = "step2") {
                MedAICard {
                    YordamchiCardTitle(
                        icon = Icons.Default.Quiz,
                        title = supportText(lang, "2-qadam: Qo'shimcha savollar", "Шаг 2: уточняющие вопросы", "Step 2: follow-up questions"),
                        subtitle = if (qData.medicalName.isNotBlank()) "${qData.title} • ${qData.medicalName}" else qData.title,
                        tint = c.tintSky,
                    )

                    Spacer(Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        qData.questions.forEachIndexed { index, question ->
                            Column {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        Modifier.size(24.dp).clip(CircleShape).background(c.tintSky.bg),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("${index + 1}", style = MaterialTheme.typography.labelMedium, color = c.tintSky.fg)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = question,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = c.textPrimary,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                YordamchiField(
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
                                    placeholder = supportText(lang, "Javob yozing...", "Напишите ответ...", "Type your answer..."),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    MedAIPrimaryButton(
                        text = if (isAnalyzingAnswers) {
                            supportText(lang, "Tahlil qilinmoqda...", "Анализируем...", "Analysing...")
                        } else {
                            supportText(lang, "Tahlil qilish", "Анализировать", "Analyse")
                        },
                        onClick = {
                            viewModel.analyzeSymptomAnswers(
                                complaint = complaintInput,
                                questions = qData.questions,
                                answers = userAnswers
                            )
                        },
                        icon = Icons.Default.FactCheck,
                        enabled = !isAnalyzingAnswers,
                        loading = isAnalyzingAnswers,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Final diagnostic result
        if (dynamicAnalysisResult.isNotEmpty()) {
            item(key = "result") {
                ResultCard(
                    title = supportText(lang, "Tahlil natijasi", "Результат анализа", "Analysis result"),
                    text = dynamicAnalysisResult,
                )
            }
            item(key = "disclaimer") {
                MedAIInfoBanner(
                    text = supportText(
                        lang,
                        "Bu tashxis emas, faqat AI taxlili. Holat og'irlashsa yoki xavotirli alomatlar bo'lsa, zudlik bilan shifokorga murojaat qiling yoki 103 ga qo'ng'iroq qiling.",
                        "Это не диагноз, а анализ ИИ. При ухудшении или тревожных симптомах немедленно обратитесь к врачу или позвоните по номеру 103.",
                        "This is not a diagnosis, only an AI analysis. If your condition worsens or you have alarming symptoms, see a doctor or call emergency services right away.",
                    ),
                    tone = MedAITone.Warning,
                )
            }
        }
    }
}

/* ============================================================
   TAB 3: Statistika
   ============================================================ */
@Composable
fun StatistikaTab(viewModel: AppViewModel) {
    val c = MedAITheme.colors
    val lang by viewModel.currentLanguage.collectAsState()

    val reminders by viewModel.reminders.collectAsState()
    val dailyMetrics by viewModel.allDailyMetrics.collectAsState()
    val symptomChecks by viewModel.symptomChecks.collectAsState()
    val context = LocalContext.current
    var showResetSuccess by remember { mutableStateOf(false) }

    // Calculated adherence metrics based on real records (starts strictly at 0)
    val totalRemindersCount = reminders.size
    val tookCount = remember(dailyMetrics) {
        dailyMetrics.sumOf { m ->
            try { org.json.JSONArray(m.completedRemindersJson).length() } catch (e: Exception) { 0 }
        }
    }
    val lateTookCount = 0
    val missedCount = if (totalRemindersCount > tookCount) totalRemindersCount - tookCount else 0
    val totalScheduled = tookCount + missedCount
    val adherencePercent = if (totalScheduled > 0) {
        ((tookCount.toDouble() / totalScheduled) * 100.0).coerceIn(0.0, 100.0)
    } else 0.0
    val adherenceText = String.format(java.util.Locale.US, "%.1f%%", adherencePercent)

    val resetToast = supportText(lang, "Hamma statistika 0 ga tushirildi", "Вся статистика обнулена", "All statistics reset to 0")
    val countSuffix = if (lang == "uz") " ta" else ""

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = TabContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "tiles") {
            MedAICard {
                YordamchiCardTitle(
                    icon = Icons.Default.BarChart,
                    title = supportText(lang, "Dori va salomatlik statistikasi", "Статистика лекарств и здоровья", "Medicine & health statistics"),
                    subtitle = supportText(
                        lang,
                        "Foydalanish va intizomga qarab oshib boruvchi statistika",
                        "Статистика растёт по мере использования и регулярности",
                        "Statistics grow with your use and consistency",
                    ),
                    tint = c.tintSky,
                )

                Spacer(Modifier.height(16.dp))

                // 2x2 stat tiles (starts at 0, grows with adherence)
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBox(
                        value = "$tookCount",
                        label = supportText(lang, "Qabul qilindi", "Принято", "Taken"),
                        tint = c.tintTeal,
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    StatBox(
                        value = "$lateTookCount",
                        label = supportText(lang, "Kechikib qabul", "Принято с опозданием", "Taken late"),
                        tint = c.tintPeach,
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBox(
                        value = "$missedCount",
                        label = supportText(lang, "O'tkazib yuborildi", "Пропущено", "Missed"),
                        tint = MedAITint(c.dangerSoft, c.onDangerSoft),
                        icon = Icons.Default.Cancel,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    StatBox(
                        value = adherenceText,
                        label = supportText(lang, "Muntazamlik", "Регулярность", "Adherence"),
                        tint = c.tintSky,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }

        // Adherence progress + overview
        item(key = "overview") {
            MedAICard {
                Text(
                    supportText(lang, "Umumiy ko'rsatkichlar", "Общие показатели", "Overview"),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        supportText(lang, "Muntazamlik", "Регулярность", "Adherence"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        adherenceText,
                        style = MedAIText.MetricSmall,
                        color = if (adherencePercent > 0) c.brand else c.textSecondary,
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (adherencePercent / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(MedAICorners.pill)),
                    color = c.brand,
                    trackColor = c.brandSoft,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
                Spacer(Modifier.height(16.dp))
                OverviewRow(
                    supportText(lang, "Rejalashtirilgan eslatmalar soni", "Запланированных напоминаний", "Scheduled reminders"),
                    "$totalRemindersCount$countSuffix",
                )
                Box(Modifier.padding(vertical = 10.dp).fillMaxWidth().height(1.dp).background(c.divider))
                OverviewRow(
                    supportText(lang, "Tekshirilgan simptomlar", "Проверок симптомов", "Symptom checks"),
                    "${symptomChecks.size}$countSuffix",
                )
                Box(Modifier.padding(vertical = 10.dp).fillMaxWidth().height(1.dp).background(c.divider))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        supportText(lang, "Davolanish intizomi bahosi", "Оценка регулярности лечения", "Treatment adherence rating"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    when {
                        adherencePercent >= 80.0 -> MedAIBadge(supportText(lang, "A'lo darajada", "Отлично", "Excellent"), MedAIBadgeTone.Success)
                        adherencePercent > 0.0 -> MedAIBadge(supportText(lang, "Yaxshi", "Хорошо", "Good"), MedAIBadgeTone.Brand)
                        else -> MedAIBadge(supportText(lang, "Boshlang'ich (0%)", "Начальный (0%)", "Starting (0%)"), MedAIBadgeTone.Info)
                    }
                }
            }
        }

        // Reset statistics
        item(key = "reset") {
            MedAICard {
                Text(
                    supportText(lang, "Statistikani boshqarish", "Управление статистикой", "Manage statistics"),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    supportText(
                        lang,
                        "Agar barcha hisoblagichlarni 0 ga tushirmoqchi bo'lsangiz, quyidagi tugmani bosing.",
                        "Чтобы обнулить все счётчики, нажмите кнопку ниже.",
                        "To reset all counters to 0, tap the button below.",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                )
                Spacer(Modifier.height(12.dp))
                YordamchiDangerOutlineButton(
                    text = supportText(lang, "Hamma statistikani 0 ga tushirish", "Обнулить всю статистику", "Reset all statistics"),
                    icon = Icons.Default.Refresh,
                    onClick = {
                        viewModel.resetAllStatistics {
                            showResetSuccess = true
                            Toast.makeText(context, resetToast, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showResetSuccess) {
                    Spacer(Modifier.height(12.dp))
                    MedAIInfoBanner(text = resetToast, tone = MedAITone.Success)
                }
            }
        }
    }
}

@Composable
private fun OverviewRow(label: String, value: String) {
    val c = MedAITheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
    }
}

/** One statistic: tinted icon disc, big number, wrapped label. */
@Composable
fun StatBox(
    value: String,
    label: String,
    tint: MedAITint,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val c = MedAITheme.colors
    val shape = RoundedCornerShape(MedAICorners.tile)
    Column(
        modifier = modifier
            .clip(shape)
            .background(c.surfaceSunken)
            .padding(14.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(tint.bg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(value, style = MedAIText.MetricMedium, color = c.textPrimary)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
}

/* ============================================================
   TAB 4: Reminder
   ============================================================ */
@Composable
fun ReminderTab(viewModel: AppViewModel) {
    val c = MedAITheme.colors
    val lang by viewModel.currentLanguage.collectAsState()

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
    var showForm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val quickTimes = remember { listOf("08:00", "13:00", "18:00", "21:00") }
    val countSuffix = if (lang == "uz") " ta" else ""

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = TabContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "add") {
            MedAIPrimaryButton(
                text = supportText(lang, "Yangi eslatma qo'shish", "Добавить напоминание", "Add reminder"),
                onClick = { showForm = true },
                icon = Icons.Default.AddAlarm,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Success banner
        if (savedSuccessMsg.isNotEmpty()) {
            item(key = "saved") {
                MedAIInfoBanner(text = savedSuccessMsg, tone = MedAITone.Success)
            }
        }

        // Reminders list header
        item(key = "list_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = supportText(lang, "Rejalashtirilgan eslatmalar", "Запланированные напоминания", "Scheduled reminders"),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                MedAIBadge("${reminders.size}$countSuffix", MedAIBadgeTone.Brand)
            }
        }

        if (reminders.isEmpty()) {
            item(key = "empty") {
                MedAIEmptyState(
                    title = supportText(lang, "Hali eslatmalar yo'q", "Напоминаний пока нет", "No reminders yet"),
                    message = supportText(
                        lang,
                        "Dori ichish vaqtini saqlash uchun yuqoridagi tugmani bosing.",
                        "Нажмите кнопку выше, чтобы сохранить время приёма лекарств.",
                        "Tap the button above to save a medicine time.",
                    ),
                    icon = Icons.Default.Alarm,
                )
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                ReminderItemCard(
                    reminder = reminder,
                    lang = lang,
                    isCompleted = completedReminderIds.contains(reminder.id),
                    onComplete = { viewModel.completeReminder(reminder.id) },
                    onToggle = { viewModel.toggleReminderActive(reminder) },
                    onDelete = { viewModel.deleteReminder(reminder.id) }
                )
            }
        }
    }

    if (showForm) {
        Dialog(
            onDismissRequest = { showForm = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            val shape = RoundedCornerShape(MedAICorners.sheet)
            Column(
                Modifier
                    .padding(16.dp)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .shadow(MedAIElevation.floating, shape)
                    .clip(shape)
                    .background(c.surfaceRaised)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                YordamchiCardTitle(
                    icon = Icons.Default.Alarm,
                    title = supportText(lang, "Yangi eslatma qo'shish", "Новое напоминание", "New reminder"),
                    subtitle = supportText(
                        lang,
                        "Dorilarni o'z vaqtida ichish uchun eslatma o'rnating",
                        "Установите напоминание, чтобы принимать лекарства вовремя",
                        "Set a reminder to take your medicine on time",
                    ),
                    tint = c.tintPeach,
                )

                MedAITextField(
                    value = medNameInput,
                    onValueChange = { medNameInput = it },
                    label = supportText(lang, "Dori nomi", "Название лекарства", "Medicine name"),
                    placeholder = supportText(lang, "Masalan: Lisinopril 10mg", "Например: Лизиноприл 10 мг", "e.g. Lisinopril 10mg"),
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MedAITextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = supportText(lang, "Qabul qilish vaqti (HH:mm)", "Время приёма (ЧЧ:мм)", "Time to take (HH:mm)"),
                        placeholder = "08:00",
                        leadingIcon = Icons.Default.Schedule,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // Quick time picker chips
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        quickTimes.forEach { t ->
                            MedAIFilterChip(text = t, selected = timeInput == t, onClick = { timeInput = t })
                        }
                    }
                }

                Column {
                    MedAIPrimaryButton(
                        text = if (isSaving) {
                            supportText(lang, "Eslatma saqlanmoqda...", "Сохраняем напоминание...", "Saving reminder...")
                        } else {
                            supportText(lang, "Eslatmani saqlash", "Сохранить напоминание", "Save reminder")
                        },
                        onClick = {
                            if (medNameInput.isBlank()) {
                                Toast.makeText(
                                    context,
                                    supportText(lang, "Iltimos, dori nomini kiriting", "Пожалуйста, введите название лекарства", "Please enter the medicine name"),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@MedAIPrimaryButton
                            }
                            if (timeInput.isBlank()) {
                                Toast.makeText(
                                    context,
                                    supportText(lang, "Iltimos, vaqtni tanlang", "Пожалуйста, выберите время", "Please choose a time"),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@MedAIPrimaryButton
                            }

                            isSaving = true
                            coroutineScope.launch {
                                viewModel.addSimpleReminder(medNameInput.trim(), timeInput.trim())
                                isSaving = false
                                savedSuccessMsg = supportText(
                                    lang,
                                    "Eslatma muvaffaqiyatli saqlandi!",
                                    "Напоминание успешно сохранено!",
                                    "Reminder saved successfully!",
                                )
                                medNameInput = ""
                                showForm = false
                            }
                        },
                        icon = Icons.Default.AddAlarm,
                        enabled = !isSaving,
                        loading = isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    MedAITextButton(
                        text = supportText(lang, "Bekor qilish", "Отмена", "Cancel"),
                        onClick = { showForm = false },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderItemCard(
    reminder: ReminderLocal,
    lang: String,
    isCompleted: Boolean,
    onComplete: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val c = MedAITheme.colors

    val frequencyLabel = remember(lang, reminder.frequency) {
        when (reminder.frequency.lowercase()) {
            "har kuni", "daily" -> supportText(lang, "Har kuni", "Ежедневно", "Daily")
            "weekly" -> supportText(lang, "Haftalik", "Еженедельно", "Weekly")
            else -> reminder.frequency
        }
    }

    MedAICard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(reminder.time, style = MedAIText.MetricSmall, color = if (reminder.isActive) c.brand else c.textSecondary)
                Text(reminder.medicineName, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                Text(frequencyLabel, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
            Spacer(Modifier.width(8.dp))
            YordamchiSwitch(
                checked = reminder.isActive,
                onCheckedChange = { onToggle() },
                description = supportText(lang, "Faol: ${reminder.medicineName}", "Активно: ${reminder.medicineName}", "Active: ${reminder.medicineName}"),
            )
        }

        if (!reminder.isActive) {
            Spacer(Modifier.height(8.dp))
            MedAIBadge(
                supportText(lang, "Eslatma o'chirilgan", "Напоминание отключено", "Reminder is off"),
                MedAIBadgeTone.Info,
                icon = Icons.Default.NotificationsOff,
            )
        }

        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (isCompleted) {
                    MedAIBadge(
                        supportText(lang, "Bugun qabul qilindi (+10 ball)", "Сегодня принято (+10 баллов)", "Taken today (+10 points)"),
                        MedAIBadgeTone.Success,
                        icon = Icons.Default.CheckCircle,
                    )
                } else {
                    MedAIPrimaryButton(
                        text = supportText(lang, "Qabul qildim", "Принял(а)", "Mark as taken"),
                        onClick = onComplete,
                        icon = Icons.Default.Check,
                    )
                }
            }
            Box(
                Modifier.size(MinTouch).clip(CircleShape).clickable(role = Role.Button, onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = supportText(lang, "O'chirish", "Удалить", "Delete"),
                    tint = c.danger,
                )
            }
        }
    }
}

/* ============================================================
   Helper: result card (AI answer rendered as markdown)
   ============================================================ */
@Composable
fun ResultCard(
    title: String,
    text: String,
) {
    val c = MedAITheme.colors

    MedAICard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(c.tintTeal.bg), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = c.tintTeal.fg, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            MedAIBadge("AI", MedAIBadgeTone.Brand, icon = Icons.Default.AutoAwesome)
        }

        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))
        Spacer(Modifier.height(12.dp))

        RichMarkdownText(text = text)
    }
}
