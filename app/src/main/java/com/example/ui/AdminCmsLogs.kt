package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/** Leading tinted disc used by CMS and log rows. */
@Composable
private fun AdminDisc(icon: ImageVector, tint: MedAITint, size: Int = 40) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(tint.bg), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size((size / 2).dp))
    }
}

/** 48x48dp delete target. */
@Composable
private fun AdminDeleteButton(description: String, onClick: () -> Unit) {
    val c = MedAITheme.colors
    Box(
        Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Delete, contentDescription = description, tint = c.danger, modifier = Modifier.size(22.dp))
    }
}

// ==========================================
// TAB 3: MEDICAL CMS (DISEASES, MEDICINES, TIPS)
// ==========================================

@Composable
fun MedicalCmsTab(
    viewModel: AppViewModel,
    diseases: List<DiseaseEntry>,
    medicines: List<MedicineEntry>,
    tips: List<HealthTipLocal>
) {
    val c = MedAITheme.colors
    val lang = adminLang(viewModel)
    fun t(uz: String, ru: String, en: String) = adminT(lang, uz, ru, en)

    var cmsSubTab by remember { mutableStateOf(0) }
    var showAddDiseaseDialog by remember { mutableStateOf(false) }
    var showAddMedicineDialog by remember { mutableStateOf(false) }
    var showAddTipDialog by remember { mutableStateOf(false) }

    val deleteLabel = t("O'chirish", "Удалить", "Delete")
    val addLabel = when (cmsSubTab) {
        0 -> t("Yangi kasallik qo'shish", "Добавить заболевание", "Add disease")
        1 -> t("Yangi dori qo'shish", "Добавить лекарство", "Add medicine")
        else -> t("Yangi maslahat qo'shish", "Добавить совет", "Add tip")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "tiles") {
            AdminTileRow {
                AdminMetricTile(Icons.Default.HealthAndSafety, c.tintTeal, diseases.size.toString(), t("Kasalliklar", "Болезни", "Diseases"))
                AdminMetricTile(Icons.Default.Medication, c.tintSky, medicines.size.toString(), t("Dorilar", "Лекарства", "Medicines"))
                AdminMetricTile(Icons.Default.TipsAndUpdates, c.tintPeach, tips.size.toString(), t("Maslahatlar", "Советы", "Tips"))
            }
        }

        item(key = "chips") {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MedAIFilterChip(t("Kasalliklar", "Болезни", "Diseases"), cmsSubTab == 0, { cmsSubTab = 0 }, icon = Icons.Default.HealthAndSafety)
                MedAIFilterChip(t("Dorilar", "Лекарства", "Medicines"), cmsSubTab == 1, { cmsSubTab = 1 }, icon = Icons.Default.Medication)
                MedAIFilterChip(t("Maslahatlar", "Советы", "Tips"), cmsSubTab == 2, { cmsSubTab = 2 }, icon = Icons.Default.TipsAndUpdates)
            }
        }

        item(key = "add") {
            MedAIPrimaryButton(
                text = addLabel,
                onClick = {
                    when (cmsSubTab) {
                        0 -> showAddDiseaseDialog = true
                        1 -> showAddMedicineDialog = true
                        else -> showAddTipDialog = true
                    }
                },
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }

        when (cmsSubTab) {
            0 -> {
                if (diseases.isEmpty()) {
                    item(key = "empty0") {
                        MedAIEmptyState(title = t("Kasalliklar ro'yxati bo'sh.", "Список заболеваний пуст.", "The disease list is empty."), icon = Icons.Default.HealthAndSafety)
                    }
                }
                items(diseases, key = { "d${it.id}" }) { d ->
                    val sev = when (d.severity.lowercase()) {
                        "mild", "low" -> MedAIBadgeTone.Success
                        "severe", "high" -> MedAIBadgeTone.Danger
                        "moderate", "medium" -> MedAIBadgeTone.Warning
                        else -> MedAIBadgeTone.Info
                    }
                    MedAICard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                        Row(verticalAlignment = Alignment.Top) {
                            AdminDisc(Icons.Default.HealthAndSafety, c.tintTeal)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(d.nameUz, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                Text("${t("Alomatlar", "Симптомы", "Symptoms")}: ${d.symptomsUz}", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                Text("${t("Mutaxassis", "Специалист", "Specialist")}: ${d.specialistType}", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                MedAIBadge("${t("Daraja", "Тяжесть", "Severity")}: ${d.severity}", sev)
                            }
                            AdminDeleteButton(deleteLabel) { viewModel.deleteDisease(d.id) }
                        }
                    }
                }
            }
            1 -> {
                if (medicines.isEmpty()) {
                    item(key = "empty1") {
                        MedAIEmptyState(title = t("Dorilar ro'yxati bo'sh.", "Список лекарств пуст.", "The medicine list is empty."), icon = Icons.Default.Medication)
                    }
                }
                items(medicines, key = { "m${it.id}" }) { m ->
                    MedAICard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                        Row(verticalAlignment = Alignment.Top) {
                            AdminDisc(Icons.Default.Medication, c.tintSky)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(m.name, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                Text("${t("Dozasi", "Дозировка", "Dosage")}: ${m.dosage}", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                MedAIBadge(m.category, MedAIBadgeTone.Brand)
                                Text(m.description, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 2)
                            }
                            AdminDeleteButton(deleteLabel) { viewModel.deleteMedicine(m.id) }
                        }
                    }
                }
            }
            else -> {
                if (tips.isEmpty()) {
                    item(key = "empty2") {
                        MedAIEmptyState(title = t("Maslahatlar ro'yxati bo'sh.", "Список советов пуст.", "The tips list is empty."), icon = Icons.Default.TipsAndUpdates)
                    }
                }
                items(tips, key = { "t${it.id}" }) { tip ->
                    MedAICard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                        Row(verticalAlignment = Alignment.Top) {
                            AdminDisc(Icons.Default.TipsAndUpdates, c.tintPeach)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(tip.uz, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                Text(tip.ru, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                            AdminDeleteButton(deleteLabel) { viewModel.deleteHealthTip(tip.id) }
                        }
                    }
                }
            }
        }
    }

    val cancel = t("Bekor qilish", "Отмена", "Cancel")
    val save = t("Saqlash", "Сохранить", "Save")

    // Add Disease Dialog
    if (showAddDiseaseDialog) {
        var dName by remember { mutableStateOf("") }
        var dSymptoms by remember { mutableStateOf("") }
        var dDesc by remember { mutableStateOf("") }
        var dSpec by remember { mutableStateOf("Terapevt") }

        AdminFormDialog(
            title = t("Yangi kasallik kiritish", "Новое заболевание", "New disease"),
            confirmText = save, dismissText = cancel,
            onDismiss = { showAddDiseaseDialog = false },
            onConfirm = {
                if (dName.isNotBlank()) {
                    viewModel.addDisease(
                        nameUz = dName, nameRu = dName, nameEn = dName,
                        symptomsUz = dSymptoms, descUz = dDesc, descRu = dDesc, descEn = dDesc,
                        specType = dSpec, severity = "medium"
                    )
                    showAddDiseaseDialog = false
                }
            }
        ) {
            MedAITextField(dName, { dName = it }, t("Kasallik nomi", "Название заболевания", "Disease name"), Modifier.fillMaxWidth())
            MedAITextField(dSymptoms, { dSymptoms = it }, t("Alomatlari", "Симптомы", "Symptoms"), Modifier.fillMaxWidth())
            MedAITextField(dDesc, { dDesc = it }, t("Tavsifi", "Описание", "Description"), Modifier.fillMaxWidth())
            MedAITextField(dSpec, { dSpec = it }, t("Shifokor mutaxassisligi", "Специальность врача", "Doctor specialty"), Modifier.fillMaxWidth())
        }
    }

    // Add Medicine Dialog
    if (showAddMedicineDialog) {
        var mName by remember { mutableStateOf("") }
        var mDosage by remember { mutableStateOf("1 tabletka 2 mahal") }
        var mDesc by remember { mutableStateOf("") }
        var mCat by remember { mutableStateOf("Og'riq qoldiruvchi") }

        AdminFormDialog(
            title = t("Yangi dori kiritish", "Новое лекарство", "New medicine"),
            confirmText = save, dismissText = cancel,
            onDismiss = { showAddMedicineDialog = false },
            onConfirm = {
                if (mName.isNotBlank()) {
                    viewModel.addMedicine(
                        name = mName, description = mDesc, dosage = mDosage,
                        sideEffects = "Kamdan-kam hollarda allergik reaksiya", category = mCat
                    )
                    showAddMedicineDialog = false
                }
            }
        ) {
            MedAITextField(mName, { mName = it }, t("Dori nomi", "Название лекарства", "Medicine name"), Modifier.fillMaxWidth())
            MedAITextField(mDosage, { mDosage = it }, t("Dozasi", "Дозировка", "Dosage"), Modifier.fillMaxWidth())
            MedAITextField(mDesc, { mDesc = it }, t("Tavsif va ko'rsatmalar", "Описание и указания", "Description and instructions"), Modifier.fillMaxWidth())
            MedAITextField(mCat, { mCat = it }, t("Toifasi", "Категория", "Category"), Modifier.fillMaxWidth())
        }
    }

    // Add Tip Dialog
    if (showAddTipDialog) {
        var tUz by remember { mutableStateOf("") }
        var tRu by remember { mutableStateOf("") }

        AdminFormDialog(
            title = t("Yangi salomatlik maslahati", "Новый совет по здоровью", "New health tip"),
            confirmText = save, dismissText = cancel,
            onDismiss = { showAddTipDialog = false },
            onConfirm = {
                if (tUz.isNotBlank()) {
                    viewModel.addHealthTip(uz = tUz, ru = if (tRu.isNotBlank()) tRu else tUz, en = tUz, orderIndex = tips.size)
                    showAddTipDialog = false
                }
            }
        ) {
            MedAITextField(tUz, { tUz = it }, t("O'zbekcha matn", "Текст на узбекском", "Uzbek text"), Modifier.fillMaxWidth())
            MedAITextField(tRu, { tRu = it }, t("Ruscha matn (ixtiyoriy)", "Текст на русском (необязательно)", "Russian text (optional)"), Modifier.fillMaxWidth())
        }
    }
}

// ==========================================
// TAB 4: AUDIT LOGS & ERROR REPORTS
// ==========================================

@Composable
fun LogsAuditTab(
    viewModel: AppViewModel,
    adminLogs: List<AdminLog>,
    errorLogs: List<ErrorLog>
) {
    val c = MedAITheme.colors
    val lang = adminLang(viewModel)
    fun t(uz: String, ru: String, en: String) = adminT(lang, uz, ru, en)

    val sdf = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }
    var logsSubTab by remember { mutableStateOf(0) }
    val unresolved = remember(errorLogs) { errorLogs.count { !it.isResolved } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "tiles") {
            AdminTileRow {
                AdminMetricTile(Icons.Default.History, c.tintSky, adminLogs.size.toString(), t("Yozuvlar", "Записи", "Entries"))
                AdminMetricTile(Icons.Default.Warning, c.tintPeach, unresolved.toString(), t("Ochiq xato", "Открытые", "Open"))
                AdminMetricTile(Icons.Default.CheckCircle, c.tintTeal, (errorLogs.size - unresolved).toString(), t("Hal qilingan", "Решённые", "Resolved"))
            }
        }

        item(key = "chips") {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MedAIFilterChip("${t("Admin jurnali", "Журнал админа", "Admin log")} (${adminLogs.size})", logsSubTab == 0, { logsSubTab = 0 }, icon = Icons.Default.History)
                MedAIFilterChip("${t("Xatolar", "Ошибки", "Errors")} (${errorLogs.size})", logsSubTab == 1, { logsSubTab = 1 }, icon = Icons.Default.Warning)
            }
        }

        when (logsSubTab) {
            0 -> {
                if (adminLogs.isEmpty()) {
                    item(key = "empty0") {
                        MedAIEmptyState(title = t("Jurnal yozuvlari mavjud emas.", "Записей в журнале нет.", "No log entries."), icon = Icons.Default.History)
                    }
                } else {
                    items(adminLogs, key = { "a${it.id}" }) { log ->
                        MedAICard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                            Row(verticalAlignment = Alignment.Top) {
                                AdminDisc(Icons.Default.History, c.tintSky, 36)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(log.action, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                    Text(sdf.format(Date(log.timestamp)), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                    Text(
                                        "${t("Kimga", "Кому", "Target")}: ${log.targetUser} • Admin: ${log.adminEmail}",
                                        style = MaterialTheme.typography.bodySmall, color = c.textSecondary
                                    )
                                    Text(log.details, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary)
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                if (errorLogs.isEmpty()) {
                    item(key = "empty1") {
                        MedAIEmptyState(
                            title = t("Xatolar aniqlanmagan. Tizim barqaror!", "Ошибок не обнаружено. Система стабильна!", "No errors detected. The system is stable!"),
                            icon = Icons.Default.CheckCircle
                        )
                    }
                } else {
                    items(errorLogs, key = { "e${it.id}" }) { err ->
                        MedAICard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape).background(if (err.isResolved) c.successSoft else c.dangerSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (err.isResolved) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (err.isResolved) c.onSuccessSoft else c.onDangerSoft,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${t("Ekran", "Экран", "Screen")}: ${err.screen}", style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MedAIBadge(
                                            if (err.isResolved) t("Hal qilingan", "Решено", "Resolved") else t("Ochiq", "Открыто", "Open"),
                                            if (err.isResolved) MedAIBadgeTone.Success else MedAIBadgeTone.Danger
                                        )
                                        Text(sdf.format(Date(err.timestamp)), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                    }
                                    Text(err.errorMessage, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary)
                                    Text(
                                        "${t("Qurilma", "Устройство", "Device")}: ${err.deviceInfo} • ${t("Versiya", "Версия", "Version")}: ${err.appVersion}",
                                        style = MaterialTheme.typography.bodySmall, color = c.textSecondary
                                    )
                                    if (!err.isResolved) {
                                        AdminActionButton(
                                            text = t("Hal qilindi", "Решено", "Mark resolved"),
                                            onClick = { viewModel.resolveErrorLog(err.id) },
                                            bg = c.brand, fg = c.onBrand,
                                            icon = Icons.Default.Check,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
