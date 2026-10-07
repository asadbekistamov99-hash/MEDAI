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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow


// --- SCREEN: REAL-TIME FAMILY MEMBERS MONITORING ---

private fun relationLabel(rel: String, lang: String): String {
    val (uz, ru) = when (rel) {
        "Child" -> "Farzand" to "Ребенок"
        "Spouse" -> "Turmush o'rtog'i" to "Супруг(а)"
        "Father" -> "Ota" to "Отец"
        "Mother" -> "Ona" to "Мать"
        "Brother" -> "Aka/Uka" to "Брат"
        "Sister" -> "Opa/Singil" to "Сестра"
        else -> rel to rel
    }
    return when (lang) { "uz" -> uz; "ru" -> ru; else -> rel }
}

/** Shared shell for every form/sheet dialog on this screen: surface, title row, scrollable body, actions. */
@Composable
private fun FamilyDialogShell(
    title: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    closeDescription: String,
    actions: @Composable RowScope.() -> Unit,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = MedAITheme.colors
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(MedAIElevation.floating, RoundedCornerShape(MedAICorners.sheet))
                .clip(RoundedCornerShape(MedAICorners.sheet))
                .background(c.surfaceRaised)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(c.brandSoft), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = c.onBrandSoft, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                    if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(MinTouch)) {
                    Icon(Icons.Default.Close, closeDescription, tint = c.textSecondary)
                }
            }
            Column(
                Modifier.weight(1f, fill = false).heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), content = actions)
        }
    }
}

@Composable
private fun VitalRow(emoji: ImageVector, tint: MedAITint, title: String, hint: String, value: String, valueColor: Color) {
    val c = MedAITheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(MedAICorners.control)).background(c.surfaceSunken).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tint.bg), contentAlignment = Alignment.Center) {
            Icon(emoji, null, tint = tint.fg, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        Spacer(Modifier.width(8.dp))
        Text(value, style = MedAIText.MetricSmall, color = valueColor)
    }
}

@Composable
fun FamilyScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val family by viewModel.familyMembers.collectAsState()
    val immunizations by viewModel.immunizationRecords.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var inviteInput by remember { mutableStateOf("") }
    var selectedMemberForReminder by remember { mutableStateOf<FamilyMemberLocal?>(null) }
    var selectedMemberForDetail by remember { mutableStateOf<FamilyMemberLocal?>(null) }
    // The QR scanner used to be the centre button of the bottom bar; it lives here now, where
    // it is actually about the family.
    var showFamilyQr by remember { mutableStateOf(false) }

    // Add sub-account state
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var newMemberName by remember { mutableStateOf("") }
    var newMemberRelation by remember { mutableStateOf("Child") }
    var newMemberEmail by remember { mutableStateOf("") }
    var newMemberPhone by remember { mutableStateOf("") }

    // Reminder inputs
    var medName by remember { mutableStateOf("") }
    var medTime by remember { mutableStateOf("09:00") }
    var medFreq by remember { mutableStateOf("Daily") }

    val relationsList = listOf("Child", "Spouse", "Father", "Mother", "Brother", "Sister")

    fun getLangText(uz: String, ru: String, en: String): String = when (lang) { "uz" -> uz; "ru" -> ru; else -> en }
    val cancelText = getLangText("Bekor qilish", "Отмена", "Cancel")
    val closeText = getLangText("Yopish", "Закрыть", "Close")
    fun toast(uz: String, ru: String, en: String, long: Boolean = false) =
        Toast.makeText(viewModel.getApplication(), getLangText(uz, ru, en), if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

    if (showFamilyQr) {
        FamilyQrDialog(viewModel = viewModel, onDismiss = { showFamilyQr = false })
    }

    // Alerts and the weekly summary are derived from the real family list (they used to be fixed demo text).
    val needsAttention = remember(family) { family.filter { it.healthScore < 80 }.minByOrNull { it.healthScore } }
    val avgScore = remember(family) { if (family.isEmpty()) 0 else family.map { it.healthScore }.average().toInt() }
    val avgSteps = remember(family) { if (family.isEmpty()) 0 else family.map { it.stepsToday }.average().toInt() }

    Scaffold(
        containerColor = c.canvas,
        topBar = {
            AppHeader(
                title = Translations.getString("family_title", lang),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showFamilyQr = true }, modifier = Modifier.size(MinTouch)) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = Translations.getString("family_qr", lang),
                            tint = c.brand
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro
            MedAICard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(c.brandSoft), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FamilyRestroom, null, tint = c.onBrandSoft, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            getLangText("Oilaviy sog'liq markazi", "Семейный центр здоровья", "Family Health Center"),
                            style = MaterialTheme.typography.titleMedium, color = c.textPrimary
                        )
                        Text(
                            getLangText(
                                "A'zolarni qo'shing, ko'rsatkichlarni kuzating, emlash taqvimini boshqaring.",
                                "Добавляйте близких, следите за показателями и календарём прививок.",
                                "Add members, follow their vitals and manage vaccination schedules."
                            ),
                            style = MaterialTheme.typography.bodySmall, color = c.textSecondary
                        )
                    }
                }
            }

            MedAIPrimaryButton(
                text = getLangText("Yangi a'zo qo'shish", "Добавить члена семьи", "Add family member"),
                icon = Icons.Default.PersonAdd,
                onClick = { showAddMemberDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            // Quick invite
            MedAICard(Modifier.fillMaxWidth()) {
                Text(
                    getLangText("Tezkor taklif", "Быстрое приглашение", "Quick invite"),
                    style = MaterialTheme.typography.titleSmall, color = c.textPrimary
                )
                Spacer(Modifier.height(10.dp))
                MedAITextField(
                    value = inviteInput,
                    onValueChange = { inviteInput = it },
                    label = getLangText("Email yoki telefon", "Email или телефон", "Email or phone"),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Default.Send
                )
                Spacer(Modifier.height(10.dp))
                MedAISecondaryButton(
                    text = getLangText("Taklif etish", "Пригласить", "Invite"),
                    onClick = {
                        if (inviteInput.isNotEmpty()) {
                            toast("Taklifnoma muvaffaqiyatli yuborildi!", "Приглашение успешно отправлено!", "Invitation sent successfully!")
                            inviteInput = ""
                        } else {
                            toast("Iltimos, telefon yoki email kiriting", "Пожалуйста, введите телефон или email", "Please enter phone or email")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Alert: only when somebody's score is actually low
            needsAttention?.let { m ->
                MedAIInfoBanner(
                    text = getLangText(
                        "${m.name}ning salomatlik ko'rsatkichi ${m.healthScore}% ga tushdi. Eslatma yuborishingiz mumkin.",
                        "Показатель здоровья (${m.name}) снизился до ${m.healthScore}%. Отправьте напоминание.",
                        "${m.name}'s health score is down to ${m.healthScore}%. Consider sending a reminder."
                    ),
                    tone = MedAITone.Warning,
                    modifier = Modifier.fillMaxWidth(),
                    actionLabel = getLangText("Eslatma", "Напомнить", "Remind"),
                    onAction = { selectedMemberForReminder = m }
                )
            }

            // Weekly summary
            if (family.isNotEmpty()) {
                Text(getLangText("Oila ko'rsatkichlari", "Показатели семьи", "Family overview"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MedAICard(Modifier.weight(1f)) {
                        Text("$avgScore%", style = MedAIText.MetricMedium, color = c.brand)
                        Text(getLangText("O'rtacha ball", "Средний балл", "Average score"), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    }
                    MedAICard(Modifier.weight(1f)) {
                        Text("%,d".format(avgSteps), style = MedAIText.MetricMedium, color = c.brand)
                        Text(getLangText("O'rtacha qadam", "Средние шаги", "Average steps"), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    }
                }
            }

            // Live monitoring list
            Text(Translations.getString("family_member_live", lang), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)

            if (family.isEmpty()) {
                MedAIEmptyState(
                    title = getLangText("Hali a'zolar yo'q", "Пока нет членов семьи", "No family members yet"),
                    icon = Icons.Default.FamilyRestroom,
                    actionLabel = getLangText("Yangi a'zo qo'shish", "Добавить члена семьи", "Add family member"),
                    onAction = { showAddMemberDialog = true }
                )
            }

            family.forEach { member ->
                val isCustom = member.uid.startsWith("family_uid_") && member.uid != "family_uid_1" && member.uid != "family_uid_2" && member.uid != "family_uid_3"
                val scoreTone = when {
                    member.healthScore >= 90 -> MedAIBadgeTone.Success
                    member.healthScore >= 80 -> MedAIBadgeTone.Brand
                    else -> MedAIBadgeTone.Warning
                }
                MedAICard(Modifier.fillMaxWidth(), onClick = { selectedMemberForDetail = member }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).clip(CircleShape).background(c.brandSoft), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = c.onBrandSoft, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.titleSmall, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(relationLabel(member.relation, lang), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            Text("${Translations.getString("family_steps", lang)}: ${member.stepsToday}", style = MaterialTheme.typography.labelMedium, color = c.brandStrong)
                        }
                        Spacer(Modifier.width(8.dp))
                        MedAIBadge("${member.healthScore}%", scoreTone)
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        MedAITextButton(
                            text = getLangText("Eslatma", "Напомнить", "Reminder"),
                            onClick = { selectedMemberForReminder = member },
                            modifier = Modifier.weight(1f),
                            color = c.onPremiumSoft
                        )
                        MedAITextButton(
                            text = getLangText("Emlash va sog'liq", "Прививки и здоровье", "Vaccines & health"),
                            onClick = { selectedMemberForDetail = member },
                            modifier = Modifier.weight(1.4f),
                            color = c.brandStrong
                        )
                        if (isCustom) {
                            IconButton(
                                onClick = {
                                    viewModel.removeFamilyMember(member.uid)
                                    toast("Sub-akkaunt o'chirildi", "Суб-аккаунт удален", "Sub-account removed")
                                },
                                modifier = Modifier.size(MinTouch)
                            ) {
                                Icon(Icons.Default.Delete, getLangText("O'chirish", "Удалить", "Delete"), tint = c.danger)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    // --- DIALOG: LINK NEW FAMILY SUB-ACCOUNT ---
    if (showAddMemberDialog) {
        FamilyDialogShell(
            title = getLangText("Oila a'zosini qo'shish", "Добавить члена семьи", "Link family member"),
            icon = Icons.Default.PersonAdd,
            onDismiss = { showAddMemberDialog = false },
            closeDescription = closeText,
            actions = {
                MedAISecondaryButton(cancelText, { showAddMemberDialog = false }, Modifier.weight(1f))
                MedAIPrimaryButton(
                    text = getLangText("Bog'lash", "Связать", "Link"),
                    onClick = {
                        if (newMemberName.isNotBlank()) {
                            viewModel.linkFamilyMemberSubAccount(
                                name = newMemberName,
                                relation = newMemberRelation,
                                email = newMemberEmail,
                                phone = newMemberPhone
                            )
                            toast(
                                "Sub-akkaunt muvaffaqiyatli bog'landi va emlash taqvimi yaratildi!",
                                "Суб-аккаунт успешно привязан и календарь прививок создан!",
                                "Sub-account linked and immunization schedule seeded!", long = true
                            )
                            newMemberName = ""
                            newMemberEmail = ""
                            newMemberPhone = ""
                            showAddMemberDialog = false
                        } else {
                            toast("Iltimos, ism kiriting", "Пожалуйста, введите имя", "Please enter name")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        ) {
            MedAITextField(newMemberName, { newMemberName = it }, getLangText("Ism sharifi", "Имя и фамилия", "Full name"), Modifier.fillMaxWidth())
            Text(getLangText("Qarindoshligi", "Родство", "Relation"), style = MaterialTheme.typography.labelLarge, color = c.textPrimary)
            // Chips wrap instead of a dropdown: all options visible, 48dp targets.
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                relationsList.forEach { rel ->
                    MedAIFilterChip(relationLabel(rel, lang), newMemberRelation == rel, { newMemberRelation = rel })
                }
            }
            MedAITextField(newMemberEmail, { newMemberEmail = it }, getLangText("Email (ixtiyoriy)", "Email (необязательно)", "Email (optional)"), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Email)
            MedAITextField(newMemberPhone, { newMemberPhone = it }, getLangText("Telefon (ixtiyoriy)", "Телефон (необязательно)", "Phone (optional)"), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Phone)
        }
    }

    // --- DIALOG: DETAILED HEALTH DASHBOARD & IMMUNIZATION SCHEDULES ---
    selectedMemberForDetail?.let { member ->
        var activeTab by remember { mutableStateOf(0) } // 0: Vaccines, 1: Vitals & Logs
        var showAddVaccineDialog by remember { mutableStateOf(false) }

        var vitalSys by remember { mutableStateOf("120") }
        var vitalDia by remember { mutableStateOf("80") }
        var vitalHr by remember { mutableStateOf("72") }

        val memberVaccines = remember(immunizations, member.uid) {
            immunizations.filter { it.familyMemberUid == member.uid }
        }

        var completedVaccineRecord by remember { mutableStateOf<ImmunizationRecordLocal?>(null) }
        var adminByInput by remember { mutableStateOf("") }
        var compDateInput by remember { mutableStateOf("") }
        var notesInput by remember { mutableStateOf("") }

        FamilyDialogShell(
            title = member.name,
            subtitle = getLangText("Oila a'zosi salomatlik profili", "Профиль здоровья члена семьи", "Family health profile"),
            icon = Icons.Default.Favorite,
            onDismiss = { selectedMemberForDetail = null },
            closeDescription = closeText,
            actions = { MedAISecondaryButton(closeText, { selectedMemberForDetail = null }, Modifier.fillMaxWidth()) }
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MedAIFilterChip(getLangText("Emlash taqvimi", "Календарь прививок", "Immunizations"), activeTab == 0, { activeTab = 0 }, icon = Icons.Default.Vaccines)
                MedAIFilterChip(getLangText("Ko'rsatkichlar", "Показатели", "Vitals"), activeTab == 1, { activeTab = 1 }, icon = Icons.Default.MonitorHeart)
            }

            if (activeTab == 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        getLangText("Tavsiya etilgan vaksinalar", "Рекомендованные вакцины", "Recommended vaccines"),
                        style = MaterialTheme.typography.titleSmall, color = c.textPrimary, modifier = Modifier.weight(1f)
                    )
                    MedAITextButton(getLangText("Qo'shish", "Добавить", "Add"), { showAddVaccineDialog = true }, color = c.brandStrong)
                }
                if (memberVaccines.isEmpty()) {
                    MedAIEmptyState(
                        title = getLangText("Hozircha emlashlar yo'q", "Пока прививок нет", "No immunizations scheduled yet"),
                        icon = Icons.Default.Vaccines
                    )
                } else {
                    memberVaccines.forEach { vac ->
                        val done = vac.status == "Completed"
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(MedAICorners.control)).background(c.surfaceSunken).padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(vac.vaccineName, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                    Text(getLangText("Kasallik: ", "Болезнь: ", "Disease: ") + vac.targetDisease, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                }
                                Spacer(Modifier.width(8.dp))
                                MedAIBadge(
                                    if (done) getLangText("Emlangan", "Привит", "Completed") else getLangText("Kutilmoqda", "Ожидает", "Pending"),
                                    if (done) MedAIBadgeTone.Success else MedAIBadgeTone.Warning
                                )
                            }
                            Text(getLangText("Muddat: ", "Срок: ", "Age: ") + vac.scheduledAge, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            Text(getLangText("Sana: ", "Дата: ", "Date: ") + vac.dueDate, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            if (done) {
                                Text(
                                    getLangText("Emlandi: ", "Введено: ", "Administered: ") + "${vac.completedDate ?: "—"}" + (vac.administeredBy?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""),
                                    style = MaterialTheme.typography.labelMedium, color = c.success
                                )
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                if (!done) {
                                    MedAIPrimaryButton(
                                        text = getLangText("Emlash", "Сделать", "Administer"),
                                        onClick = {
                                            completedVaccineRecord = vac
                                            compDateInput = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                                            adminByInput = ""
                                            notesInput = ""
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else Spacer(Modifier.weight(1f))
                                IconButton(onClick = { viewModel.deleteImmunizationRecord(vac.id) }, modifier = Modifier.size(MinTouch)) {
                                    Icon(Icons.Default.Delete, getLangText("O'chirish", "Удалить", "Delete"), tint = c.danger)
                                }
                            }
                        }
                    }
                }
            } else {
                val sysBad = (vitalSys.toIntOrNull() ?: 120) > 135
                VitalRow(Icons.Default.Bloodtype, c.tintPeach, getLangText("Arterial bosim", "Артериальное давление", "Blood pressure"),
                    getLangText("Normal: 120/80 mm sim.ust", "Норма: 120/80 мм рт.ст.", "Normal: 120/80 mmHg"),
                    "$vitalSys/$vitalDia", if (sysBad) c.danger else c.success)
                VitalRow(Icons.Default.Favorite, c.tintTeal, getLangText("Yurak urishi", "Пульс", "Heart rate"),
                    getLangText("Normal: 60-90 marta/daq", "Норма: 60-90 уд/мин", "Normal: 60-90 bpm"),
                    "$vitalHr bpm", c.success)
                VitalRow(Icons.Default.DirectionsWalk, c.tintSky, getLangText("Kunlik qadamlar", "Шаги за сегодня", "Daily steps"),
                    getLangText("Maqsad: 10,000 qadam", "Цель: 10,000 шагов", "Target: 10,000 steps"),
                    "${member.stepsToday}", c.brandStrong)

                Text(getLangText("Yangi ko'rsatkichlarni kiritish", "Записать новые показатели", "Log new readings"), style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedAITextField(vitalSys, { vitalSys = it }, "SYS", Modifier.weight(1f), keyboardType = KeyboardType.Number)
                    MedAITextField(vitalDia, { vitalDia = it }, "DIA", Modifier.weight(1f), keyboardType = KeyboardType.Number)
                    MedAITextField(vitalHr, { vitalHr = it }, getLangText("Puls", "Пульс", "Pulse"), Modifier.weight(1f), keyboardType = KeyboardType.Number)
                }
                MedAIPrimaryButton(
                    text = getLangText("Ko'rsatkichlarni saqlash", "Сохранить показатели", "Save vitals"),
                    onClick = {
                        if (vitalSys.isNotBlank() && vitalDia.isNotBlank() && vitalHr.isNotBlank()) {
                            val sys = vitalSys.toIntOrNull() ?: 120
                            val score = if (sys in 115..129) 100 else if (sys in 100..139) 85 else 60
                            val updatedMember = member.copy(healthScore = score, lastActive = System.currentTimeMillis())
                            coroutineScope.launch { viewModel.updateFamilyMember(updatedMember) }
                            toast("Ko'rsatkichlar muvaffaqiyatli saqlandi!", "Показатели успешно сохранены!", "Vital readings saved successfully!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- DIALOG: MARK VACCINE AS COMPLETED ---
        completedVaccineRecord?.let { vac ->
            FamilyDialogShell(
                title = getLangText("Emlashni tasdiqlash", "Подтверждение прививки", "Confirm vaccination"),
                subtitle = vac.vaccineName,
                icon = Icons.Default.Vaccines,
                onDismiss = { completedVaccineRecord = null },
                closeDescription = closeText,
                actions = {
                    MedAISecondaryButton(cancelText, { completedVaccineRecord = null }, Modifier.weight(1f))
                    MedAIPrimaryButton(
                        text = getLangText("Tasdiqlash", "Подтвердить", "Confirm"),
                        onClick = {
                            if (compDateInput.isNotBlank()) {
                                viewModel.updateImmunizationRecord(
                                    vac.copy(status = "Completed", completedDate = compDateInput, administeredBy = adminByInput, notes = notesInput)
                                )
                                completedVaccineRecord = null
                                toast("Emlash muvaffaqiyatli saqlandi!", "Прививка успешно зафиксирована!", "Vaccine marked as completed!")
                            } else {
                                toast("Sanani kiriting", "Введите дату", "Please enter date")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            ) {
                MedAITextField(compDateInput, { compDateInput = it }, getLangText("Emlangan sana (kk.oo.yyyy)", "Дата прививки (дд.мм.гггг)", "Date completed (dd.mm.yyyy)"), Modifier.fillMaxWidth())
                MedAITextField(adminByInput, { adminByInput = it }, getLangText("Shifokor / Klinika", "Врач / Клиника", "Doctor / Clinic"), Modifier.fillMaxWidth())
                MedAITextField(notesInput, { notesInput = it }, getLangText("Izoh / nojo'ya ta'sirlar", "Заметки / побочные эффекты", "Notes / side effects"), Modifier.fillMaxWidth())
            }
        }

        // --- DIALOG: ADD NEW CUSTOM IMMUNIZATION RECORD ---
        if (showAddVaccineDialog) {
            var customVacName by remember { mutableStateOf("") }
            var customDisease by remember { mutableStateOf("") }
            var customAge by remember { mutableStateOf("Custom") }
            var customDueDate by remember { mutableStateOf("") }
            var customNotes by remember { mutableStateOf("") }

            FamilyDialogShell(
                title = getLangText("Yangi emlash qo'shish", "Добавить новую прививку", "Add custom immunization"),
                icon = Icons.Default.Add,
                onDismiss = { showAddVaccineDialog = false },
                closeDescription = closeText,
                actions = {
                    MedAISecondaryButton(cancelText, { showAddVaccineDialog = false }, Modifier.weight(1f))
                    MedAIPrimaryButton(
                        text = getLangText("Qo'shish", "Добавить", "Add"),
                        onClick = {
                            if (customVacName.isNotBlank() && customDueDate.isNotBlank()) {
                                viewModel.addCustomImmunizationRecord(
                                    familyMemberUid = member.uid,
                                    vaccineName = customVacName,
                                    targetDisease = customDisease,
                                    scheduledAge = customAge,
                                    dueDate = customDueDate,
                                    notes = customNotes
                                )
                                showAddVaccineDialog = false
                                toast("Yangi emlash muvaffaqiyatli jadvalga qo'shildi!", "Новая прививка успешно добавлена в расписание!", "New immunization scheduled successfully!")
                            } else {
                                toast("Vaksina nomi va sanasini kiriting", "Введите имя вакцины и дату", "Please fill in vaccine name and due date")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            ) {
                MedAITextField(customVacName, { customVacName = it }, getLangText("Vaksina nomi", "Название вакцины", "Vaccine name"), Modifier.fillMaxWidth())
                MedAITextField(customDisease, { customDisease = it }, getLangText("Qarshi kasallik", "Целевая болезнь", "Target disease"), Modifier.fillMaxWidth())
                MedAITextField(customAge, { customAge = it }, getLangText("Tavsiya etilgan muddat (masalan: 18 oylik)", "Срок (например, 18 месяцев)", "Recommended age (e.g., 18 months)"), Modifier.fillMaxWidth())
                MedAITextField(customDueDate, { customDueDate = it }, getLangText("Emlash muddati (kk.oo.yyyy)", "Срок вакцинации (дд.мм.гггг)", "Due date (dd.mm.yyyy)"), Modifier.fillMaxWidth(), placeholder = "15.09.2026")
                MedAITextField(customNotes, { customNotes = it }, getLangText("Izohlar", "Заметки", "Notes"), Modifier.fillMaxWidth())
            }
        }
    }

    // Add reminder dialog
    selectedMemberForReminder?.let { member ->
        FamilyDialogShell(
            title = member.name + " " + getLangText("uchun eslatma", "— напоминание", "— reminder"),
            icon = Icons.Default.Alarm,
            onDismiss = { selectedMemberForReminder = null },
            closeDescription = closeText,
            actions = {
                MedAISecondaryButton(cancelText, { selectedMemberForReminder = null }, Modifier.weight(1f))
                MedAIPremiumButton(
                    text = getLangText("Biriktirish", "Привязать", "Assign"),
                    onClick = {
                        if (medName.isNotBlank() && medTime.isNotBlank()) {
                            viewModel.addReminder(
                                medicineName = medName,
                                time = medTime,
                                frequency = medFreq,
                                type = "Oila",
                                targetFamily = member.name,
                                notes = getLangText("Oila a'zosi tomonidan yuborildi", "Отправлено членом семьи", "Added by family member")
                            )
                            medName = ""
                            selectedMemberForReminder = null
                            toast("Eslatma muvaffaqiyatli oila a'zosiga biriktirildi!", "Напоминание успешно привязано к члену семьи!", "Reminder successfully assigned to family member!")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        ) {
            MedAITextField(medName, { medName = it }, getLangText("Dori yoki tavsiya nomi", "Название лекарства / рекомендация", "Medicine name / advice"), Modifier.fillMaxWidth())
            MedAITextField(medTime, { medTime = it }, getLangText("Vaqti (masalan: 08:00)", "Время (например: 08:00)", "Time (e.g. 08:00)"), Modifier.fillMaxWidth())
            MedAITextField(medFreq, { medFreq = it }, getLangText("Chastotasi (masalan: Kuniga 1 marta)", "Частота (например: 1 раз в день)", "Frequency (e.g. Daily)"), Modifier.fillMaxWidth())
        }
    }
}
