@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DailyHealthMetricsLocal
import com.example.data.FirestoreMedicationReminder
import com.example.i18n.Translations
import com.example.ui.theme.MedAICorners
import com.example.ui.theme.MedAIElevation
import com.example.ui.theme.MedAIText
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MinTouch
import androidx.compose.ui.draw.shadow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// --- SCREEN: REMINDERS & ALARMS ---

/** Form values of the "new reminder" dialog. The defaults are the ones the old inline form started with. */
internal class ReminderFormState(lang: String) {
    var name by mutableStateOf("")
    var dosage by mutableStateOf(supportText(lang, "1 ta tabletka", "1 таблетка", "1 tablet"))
    var time by mutableStateOf("08:00")
    var frequency by mutableStateOf("Daily")
    var notificationsEnabled by mutableStateOf(true)
    var notificationFrequency by mutableStateOf("Exact time")
    var targetFamily by mutableStateOf("")
    var notes by mutableStateOf("")
}

/** Day status for the calendar dots. */
private enum class DayStatus { None, NoReminders, AllDone, Partial, Missed }

private fun completedCountOf(json: String): Int =
    try { org.json.JSONArray(json).length() } catch (e: Exception) { 0 }

@Composable
fun ReminderScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val reminderItems by viewModel.reminders.collectAsState()
    val todayMetricsState by viewModel.todayMetrics.collectAsState()
    val allMetricsList by viewModel.allDailyMetrics.collectAsState()
    val firestoreReminders by viewModel.firestoreReminders.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()

    val form = remember { ReminderFormState(lang) }
    var isAdding by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf(
        supportText(lang, "Dorilar", "Лекарства", "Medicines"),
        supportText(lang, "Suv", "Вода", "Water"),
        supportText(lang, "Taqvim", "Календарь", "Calendar"),
    )
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val familyNames = remember(familyMembers) { familyMembers.map { it.name } }

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("reminder_title", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = c.surface,
                contentColor = c.brand,
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        selectedContentColor = c.brand,
                        unselectedContentColor = c.textSecondary,
                        text = { Text(title, style = MaterialTheme.typography.labelLarge, maxLines = 2, textAlign = TextAlign.Center) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // --- TAB 0: MEDICINE REMINDERS (FIRESTORE) ---
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item(key = "add") {
                            MedAIPrimaryButton(
                                text = supportText(lang, "Yangi dori eslatmasi qo'shish", "Добавить напоминание о лекарстве", "Add medicine reminder"),
                                onClick = { isAdding = !isAdding },
                                icon = Icons.Default.Add,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        item(key = "list_header") {
                            Text(
                                supportText(lang, "Mening dori jadvallarim", "Мой график приёма", "My medicine schedule"),
                                style = MaterialTheme.typography.titleMedium,
                                color = c.textPrimary,
                            )
                        }

                        if (firestoreReminders.isEmpty()) {
                            item(key = "empty") {
                                MedAIEmptyState(
                                    title = supportText(lang, "Hozircha dori eslatmasi yo'q", "Напоминаний о лекарствах пока нет", "No medicine reminders yet"),
                                    message = supportText(
                                        lang,
                                        "Dori ichish jadvalini saqlash uchun yuqoridagi tugmani bosing.",
                                        "Нажмите кнопку выше, чтобы сохранить график приёма лекарств.",
                                        "Tap the button above to save a medicine schedule.",
                                    ),
                                    icon = Icons.Default.Medication,
                                )
                            }
                        } else {
                            items(firestoreReminders, key = { it.id }) { item ->
                                ReminderCard(
                                    lang = lang,
                                    item = item,
                                    isCompletedToday = item.completedDates.contains(todayStr),
                                    onToggleActive = { viewModel.toggleFirestoreReminderActive(item.id, item.isActive) },
                                    onComplete = { viewModel.completeFirestoreReminder(item.id, item.completedDates) },
                                    onDelete = { viewModel.deleteFirestoreReminder(item.id) },
                                )
                            }
                        }
                    }
                }
                1 -> WaterTab(
                    lang = lang,
                    metrics = todayMetricsState,
                    onWaterDelta = { viewModel.updateWaterProgress(it) },
                    onGoal = { viewModel.updateWaterGoal(it) },
                )
                2 -> CalendarTab(
                    lang = lang,
                    allMetrics = allMetricsList,
                    activeCount = remember(reminderItems) { reminderItems.count { it.isActive } },
                )
            }
        }
    }

    if (isAdding) {
        Dialog(
            onDismissRequest = { isAdding = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            ReminderFormContent(
                lang = lang,
                form = form,
                familyNames = familyNames,
                modifier = Modifier.padding(16.dp),
                onCancel = { isAdding = false },
                onSave = {
                    if (form.name.isNotEmpty() && form.time.isNotEmpty()) {
                        viewModel.addFirestoreReminder(
                            medicineName = form.name,
                            dosage = form.dosage,
                            time = form.time,
                            frequency = form.frequency,
                            notificationsEnabled = form.notificationsEnabled,
                            notificationFrequency = form.notificationFrequency,
                            targetFamily = form.targetFamily.ifEmpty { null },
                            notes = form.notes.ifEmpty { null }
                        )
                        form.name = ""
                        form.notes = ""
                        form.targetFamily = ""
                        isAdding = false
                    } else {
                        Toast.makeText(
                            viewModel.getApplication(),
                            supportText(lang, "Iltimos, dori nomi va vaqtini kiriting!", "Пожалуйста, укажите название лекарства и время!", "Please enter the medicine name and time!"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------------------
// Add-reminder form (shown inside a Dialog)
// ---------------------------------------------------------------------------------------
@Composable
internal fun ReminderFormContent(
    lang: String,
    form: ReminderFormState,
    familyNames: List<String>,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MedAITheme.colors
    val shape = RoundedCornerShape(MedAICorners.sheet)
    val dosages = remember(lang) {
        listOf(
            supportText(lang, "1 ta tabletka", "1 таблетка", "1 tablet"),
            supportText(lang, "2 ta tabletka", "2 таблетки", "2 tablets"),
            supportText(lang, "1/2 tabletka", "1/2 таблетки", "1/2 tablet"),
            supportText(lang, "1 kapsula", "1 капсула", "1 capsule"),
            "5 ml", "10 ml",
        )
    }
    val frequencies = remember(lang) { frequencyOptions(lang) }
    val notifyOptions = remember(lang) { notificationOptions(lang) }

    Column(
        modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .shadow(MedAIElevation.floating, shape)
            .clip(shape)
            .background(c.surfaceRaised)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            supportText(lang, "Yangi eslatma", "Новое напоминание", "New reminder"),
            style = MaterialTheme.typography.headlineSmall,
            color = c.textPrimary,
        )

        MedAITextField(
            value = form.name,
            onValueChange = { form.name = it },
            label = supportText(lang, "Dori nomi", "Название лекарства", "Medicine name"),
            placeholder = supportText(lang, "Masalan: Paratsetamol", "Например: Парацетамол", "e.g. Paracetamol"),
            modifier = Modifier.fillMaxWidth(),
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MedAITextField(
                value = form.dosage,
                onValueChange = { form.dosage = it },
                label = supportText(lang, "Dozasi", "Дозировка", "Dosage"),
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dosages.forEach { qd ->
                    MedAIFilterChip(text = qd, selected = form.dosage == qd, onClick = { form.dosage = qd })
                }
            }
        }

        MedAITextField(
            value = form.time,
            onValueChange = { form.time = it },
            label = supportText(lang, "Vaqti", "Время", "Time"),
            placeholder = supportText(lang, "Masalan: 08:00", "Например: 08:00", "e.g. 08:00"),
            modifier = Modifier.fillMaxWidth(),
        )

        Column {
            Text(
                supportText(lang, "Takroriylik", "Периодичность", "Frequency"),
                style = MaterialTheme.typography.labelLarge,
                color = c.textPrimary,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                frequencies.forEach { (code, label) ->
                    MedAIFilterChip(text = label, selected = form.frequency == code, onClick = { form.frequency = code })
                }
            }
        }

        // Notifications
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(MedAICorners.control))
                .background(c.surfaceSunken)
                .padding(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (form.notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = if (form.notificationsEnabled) c.brand else c.textSecondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    supportText(lang, "Bildirishnomalar", "Уведомления", "Notifications"),
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                LabelledSwitch(
                    checked = form.notificationsEnabled,
                    onCheckedChange = { form.notificationsEnabled = it },
                    description = supportText(lang, "Bildirishnomalar", "Уведомления", "Notifications"),
                )
            }
            if (form.notificationsEnabled) {
                Text(
                    supportText(lang, "Eslatish vaqti", "Когда напомнить", "Remind me"),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    notifyOptions.forEach { (code, label) ->
                        MedAIFilterChip(text = label, selected = form.notificationFrequency == code, onClick = { form.notificationFrequency = code })
                    }
                }
            }
        }

        // Who takes it
        Column {
            Text(
                supportText(lang, "Kim uchun", "Для кого", "Who is it for"),
                style = MaterialTheme.typography.labelLarge,
                color = c.textPrimary,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MedAIFilterChip(
                    text = supportText(lang, "O'zimga", "Для себя", "Myself"),
                    selected = form.targetFamily.isEmpty(),
                    onClick = { form.targetFamily = "" },
                )
                familyNames.forEach { n ->
                    MedAIFilterChip(text = n, selected = form.targetFamily == n, onClick = { form.targetFamily = n })
                }
            }
            if (form.targetFamily.isNotEmpty()) {
                Text(
                    supportText(
                        lang,
                        "Eslatma ${form.targetFamily} uchun belgilanmoqda",
                        "Напоминание назначается: ${form.targetFamily}",
                        "Reminder is set for ${form.targetFamily}",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onPremiumSoft,
                )
            }
        }

        MedAITextField(
            value = form.notes,
            onValueChange = { form.notes = it },
            label = supportText(lang, "Izoh", "Заметка", "Note"),
            placeholder = supportText(lang, "Masalan: Ovqatdan keyin", "Например: после еды", "e.g. after meals"),
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
        )

        Column {
            MedAIPrimaryButton(
                text = supportText(lang, "Eslatmani saqlash", "Сохранить напоминание", "Save reminder"),
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            MedAITextButton(
                text = supportText(lang, "Bekor qilish", "Отмена", "Cancel"),
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun frequencyOptions(lang: String) = listOf(
    "Daily" to supportText(lang, "Kunlik", "Ежедневно", "Daily"),
    "Weekly" to supportText(lang, "Haftalik", "Еженедельно", "Weekly"),
    "Every 12 Hours" to supportText(lang, "Har 12 soatda", "Каждые 12 часов", "Every 12 hours"),
    "Every 8 Hours" to supportText(lang, "Har 8 soatda", "Каждые 8 часов", "Every 8 hours"),
    "Custom" to supportText(lang, "Maxsus", "Другое", "Custom"),
)

private fun notificationOptions(lang: String) = listOf(
    "Exact time" to supportText(lang, "Aynan vaqtida", "Точно во время", "At the exact time"),
    "5m before" to supportText(lang, "5 daqiqa oldin", "За 5 минут", "5 min before"),
    "15m before" to supportText(lang, "15 daqiqa oldin", "За 15 минут", "15 min before"),
    "30m before" to supportText(lang, "30 daqiqa oldin", "За 30 минут", "30 min before"),
)

// ---------------------------------------------------------------------------------------
// One medicine reminder
// ---------------------------------------------------------------------------------------
@Composable
internal fun ReminderCard(
    lang: String,
    item: FirestoreMedicationReminder,
    isCompletedToday: Boolean,
    onToggleActive: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = MedAITheme.colors
    val frequencyLabel = remember(lang, item.frequency) {
        frequencyOptions(lang).firstOrNull { it.first == item.frequency }?.second ?: item.frequency
    }
    val notifyLabel = remember(lang, item.notificationFrequency) {
        notificationOptions(lang).firstOrNull { it.first == item.notificationFrequency }?.second ?: item.notificationFrequency
    }

    MedAICard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.time, style = MedAIText.MetricSmall, color = c.brand)
                Text(item.medicineName, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                Text(frequencyLabel, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
            Spacer(Modifier.width(8.dp))
            LabelledSwitch(
                checked = item.isActive,
                onCheckedChange = { onToggleActive() },
                description = supportText(lang, "Faol: ${item.medicineName}", "Активно: ${item.medicineName}", "Active: ${item.medicineName}"),
            )
        }

        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (item.dosage.isNotEmpty()) MedAIBadge(item.dosage, MedAIBadgeTone.Brand, icon = Icons.Default.Medication)
            MedAIBadge(
                text = if (item.notificationsEnabled) notifyLabel else supportText(lang, "Eslatma yo'q", "Без уведомлений", "No notifications"),
                tone = if (item.notificationsEnabled) MedAIBadgeTone.Warning else MedAIBadgeTone.Info,
                icon = if (item.notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
            )
            if (item.targetFamilyMember != null) {
                MedAIBadge(item.targetFamilyMember, MedAIBadgeTone.Premium, icon = Icons.Default.Person)
            }
        }

        if (item.notes != null) {
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.surfaceSunken)
                    .padding(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(item.notes, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }

        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (item.isActive) {
                    if (isCompletedToday) {
                        MedAIBadge(
                            supportText(lang, "Bugun ichildi", "Сегодня принято", "Taken today"),
                            MedAIBadgeTone.Success,
                            icon = Icons.Default.Done,
                        )
                    } else {
                        MedAIPrimaryButton(
                            text = supportText(lang, "Ichdim", "Принял(а)", "Mark as taken"),
                            onClick = onComplete,
                        )
                    }
                } else {
                    Text(
                        supportText(lang, "Eslatma faol emas", "Напоминание отключено", "Reminder is off"),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
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

// ---------------------------------------------------------------------------------------
// Water tab
// ---------------------------------------------------------------------------------------
@Composable
private fun WaterTab(
    lang: String,
    metrics: DailyHealthMetricsLocal?,
    onWaterDelta: (Int) -> Unit,
    onGoal: (Int) -> Unit,
) {
    val c = MedAITheme.colors
    val currentGlasses = metrics?.waterGlasses ?: 0
    val waterGoal = metrics?.waterGoal ?: 8
    val sleepHours = metrics?.sleepHours ?: 0.0
    val progressFraction = if (waterGoal > 0) currentGlasses.toFloat() / waterGoal else 0f
    var selectedFreq by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MedAICard {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    CircularProgressIndicator(
                        progress = { progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = c.brand,
                        strokeWidth = 10.dp,
                        trackColor = c.brandSoft,
                    )
                    Text("$currentGlasses / $waterGoal", style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (currentGlasses >= waterGoal) {
                        supportText(lang, "Ajoyib! Bugungi suv ichish normasi bajarildi.", "Отлично! Дневная норма воды выполнена.", "Great! Today's water goal is complete.")
                    } else {
                        supportText(lang, "Suv ichish salomatlik uchun juda muhim.", "Питьевой режим очень важен для здоровья.", "Staying hydrated is important for your health.")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoundIconButton(
                        icon = Icons.Default.Remove,
                        description = supportText(lang, "Bir stakan kamaytirish", "Убрать один стакан", "Remove one glass"),
                        onClick = { onWaterDelta(-1) },
                    )
                    MedAIPrimaryButton(
                        text = supportText(lang, "Stakan suv ichdim", "Выпил(а) стакан воды", "I drank a glass"),
                        onClick = { onWaterDelta(1) },
                        modifier = Modifier.weight(1f),
                    )
                    RoundIconButton(
                        icon = Icons.Default.Add,
                        description = supportText(lang, "Bir stakan qo'shish", "Добавить один стакан", "Add one glass"),
                        onClick = { onWaterDelta(1) },
                    )
                }
            }
        }

        // Compact stat tiles
        MedAICard {
            Text(
                supportText(lang, "Bugungi ko'rsatkichlar", "Показатели за сегодня", "Today's metrics"),
                style = MaterialTheme.typography.titleSmall,
                color = c.textPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(
                    value = "$currentGlasses",
                    label = supportText(lang, "Ichildi", "Выпито", "Drunk"),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = "$waterGoal",
                    label = supportText(lang, "Maqsad", "Цель", "Goal"),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = if (sleepHours > 0) String.format(Locale.US, "%.1f", sleepHours) else "-",
                    label = supportText(lang, "Uyqu, soat", "Сон, ч", "Sleep, h"),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        MedAICard {
            Text(
                supportText(lang, "Suv ichish maqsadi", "Цель по воде", "Water goal"),
                style = MaterialTheme.typography.titleSmall,
                color = c.textPrimary,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(6, 8, 10, 12).forEach { goal ->
                    MedAIFilterChip(
                        text = supportText(lang, "$goal stakan", "$goal стак.", "$goal glasses"),
                        selected = waterGoal == goal,
                        onClick = { onGoal(goal) },
                    )
                }
            }
        }

        MedAICard {
            Text(
                supportText(lang, "Eslatma chastotasi (suv)", "Частота напоминаний (вода)", "Reminder frequency (water)"),
                style = MaterialTheme.typography.titleSmall,
                color = c.textPrimary,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    1 to supportText(lang, "Har soat", "Каждый час", "Every hour"),
                    2 to supportText(lang, "Har 2 soat", "Каждые 2 часа", "Every 2 hours"),
                    3 to supportText(lang, "Har 3 soat", "Каждые 3 часа", "Every 3 hours"),
                ).forEach { (hours, label) ->
                    MedAIFilterChip(text = label, selected = selectedFreq == hours, onClick = { selectedFreq = hours })
                }
            }
        }
    }
}

@Composable
private fun RoundIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    val c = MedAITheme.colors
    Box(
        Modifier
            .size(MinTouch)
            .clip(CircleShape)
            .background(c.brandSoft)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = c.onBrandSoft)
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    val c = MedAITheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(MedAICorners.control))
            .background(c.surfaceSunken)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textSecondary, textAlign = TextAlign.Center)
    }
}

// ---------------------------------------------------------------------------------------
// Calendar tab
// ---------------------------------------------------------------------------------------
@Composable
private fun CalendarTab(
    lang: String,
    allMetrics: List<DailyHealthMetricsLocal>,
    activeCount: Int,
) {
    val c = MedAITheme.colors
    val now = remember { Calendar.getInstance() }
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH)
    val today = now.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = remember { now.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val startOffset = remember {
        val first = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear); set(Calendar.MONTH, currentMonth); set(Calendar.DAY_OF_MONTH, 1)
        }
        (first.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday-indexed offset
    }
    val monthTitle = remember(lang) { SimpleDateFormat("LLLL yyyy", Locale(lang)).format(Date()) }
    val weekdays = remember(lang) {
        when (lang) {
            "uz" -> listOf("D", "S", "C", "P", "J", "S", "Y")
            "ru" -> listOf("П", "В", "С", "Ч", "П", "С", "В")
            else -> listOf("M", "T", "W", "T", "F", "S", "S")
        }
    }
    val metricsByDate = remember(allMetrics) { allMetrics.associateBy { it.date } }
    val statuses = remember(metricsByDate, activeCount) {
        (1..daysInMonth).associateWith { day ->
            val m = metricsByDate[String.format(Locale.US, "%d-%02d-%02d", currentYear, currentMonth + 1, day)]
            if (m == null) DayStatus.None else {
                val done = completedCountOf(m.completedRemindersJson)
                when {
                    activeCount == 0 -> DayStatus.NoReminders
                    done >= activeCount -> DayStatus.AllDone
                    done > 0 -> DayStatus.Partial
                    else -> DayStatus.Missed
                }
            }
        }
    }
    val rows = remember(startOffset, daysInMonth) {
        (List<Int?>(startOffset) { null } + (1..daysInMonth).toList()).chunked(7)
    }
    var selectedDay by remember { mutableStateOf<Int?>(today) }

    fun dotColor(s: DayStatus): Color = when (s) {
        DayStatus.None -> Color.Transparent
        DayStatus.NoReminders -> c.textSecondary
        DayStatus.AllDone -> c.brand
        DayStatus.Partial -> c.warning
        DayStatus.Missed -> c.danger
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            monthTitle.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            color = c.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        // Full-width on purpose: seven 48dp-wide day cells must fit a 360dp phone.
        Column(
            Modifier
                .fillMaxWidth()
                .background(c.surface)
                .padding(vertical = 8.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                weekdays.forEach { d ->
                    Text(
                        d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textSecondary,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            rows.forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val day = week.getOrNull(col)
                        if (day == null) {
                            Spacer(Modifier.weight(1f).height(MinTouch))
                        } else {
                            val isSelected = selectedDay == day
                            val isToday = day == today
                            val status = statuses[day] ?: DayStatus.None
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(MinTouch)
                                    .clickable(role = Role.Button) { selectedDay = day },
                                contentAlignment = Alignment.Center,
                            ) {
                                val cellShape = RoundedCornerShape(12.dp)
                                Column(
                                    Modifier
                                        .size(width = 40.dp, height = 44.dp)
                                        .clip(cellShape)
                                        .background(if (isSelected) c.brandSoft else Color.Transparent)
                                        .then(
                                            if (isSelected) Modifier.border(1.5.dp, c.brand, cellShape)
                                            else if (isToday) Modifier.border(1.dp, c.borderStrong, cellShape)
                                            else Modifier
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        day.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) c.onBrandSoft else c.textPrimary,
                                    )
                                    Box(Modifier.padding(top = 2.dp).size(6.dp).clip(CircleShape).background(dotColor(status)))
                                }
                            }
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.border).padding(0.dp))

        // Legend
        FlowRow(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf(
                DayStatus.AllDone to supportText(lang, "Hammasi ichildi", "Всё принято", "All taken"),
                DayStatus.Partial to supportText(lang, "Qisman", "Частично", "Partly"),
                DayStatus.Missed to supportText(lang, "O'tkazib yuborildi", "Пропущено", "Missed"),
            ).forEach { (s, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor(s)))
                    Spacer(Modifier.width(6.dp))
                    Text(label, style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                }
            }
        }

        // Selected day report
        selectedDay?.let { inspectedDay ->
            val inspectDateStr = String.format(Locale.US, "%d-%02d-%02d", currentYear, currentMonth + 1, inspectedDay)
            val dayMetrics = metricsByDate[inspectDateStr]
            MedAICard(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "$inspectDateStr — " + supportText(lang, "Kunlik hisobot", "Отчёт за день", "Daily report"),
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textPrimary,
                )
                Spacer(Modifier.height(8.dp))
                if (dayMetrics == null) {
                    Text(
                        supportText(lang, "Ushbu kunda ma'lumot kiritilmagan.", "За этот день данных нет.", "No data was recorded for this day."),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReportRow(
                            supportText(lang, "Suv", "Вода", "Water"),
                            supportText(
                                lang,
                                "${dayMetrics.waterGlasses} stakan (maqsad: ${dayMetrics.waterGoal})",
                                "${dayMetrics.waterGlasses} стак. (цель: ${dayMetrics.waterGoal})",
                                "${dayMetrics.waterGlasses} glasses (goal: ${dayMetrics.waterGoal})",
                            ),
                        )
                        ReportRow(
                            supportText(lang, "Qabul qilingan dorilar", "Принято лекарств", "Medicines taken"),
                            completedCountOf(dayMetrics.completedRemindersJson).toString(),
                        )
                        if (dayMetrics.weight > 0) {
                            ReportRow(supportText(lang, "Vazn", "Вес", "Weight"), "${dayMetrics.weight} " + supportText(lang, "kg", "кг", "kg"))
                        }
                        if (dayMetrics.bpSystolic > 0) {
                            ReportRow(
                                supportText(lang, "Qon bosimi", "Давление", "Blood pressure"),
                                "${dayMetrics.bpSystolic}/${dayMetrics.bpDiastolic} " + supportText(lang, "mm sim. ust.", "мм рт. ст.", "mmHg"),
                            )
                        }
                        if (dayMetrics.heartRate > 0) {
                            ReportRow(supportText(lang, "Puls", "Пульс", "Heart rate"), "${dayMetrics.heartRate} " + supportText(lang, "zarba/daq", "уд/мин", "BPM"))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    val c = MedAITheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

/** Material Switch is only 32dp tall; wrapping it makes the real, labelled tap target 48dp. */
@Composable
private fun LabelledSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, description: String) {
    val c = MedAITheme.colors
    Box(
        Modifier
            .size(width = 56.dp, height = MinTouch)
            .semantics { contentDescription = description }
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        contentAlignment = Alignment.Center,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedThumbColor = c.onBrand, checkedTrackColor = c.brand),
        )
    }
}
