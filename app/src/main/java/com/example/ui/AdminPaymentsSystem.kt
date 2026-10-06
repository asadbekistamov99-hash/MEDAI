package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// TAB 1: PAYMENTS & CHECKS MANAGEMENT
// ==========================================

@Composable
fun PaymentsManagementTab(viewModel: AppViewModel, payments: List<PaymentRequestLocal>) {
    val c = MedAITheme.colors
    val lang = adminLang(viewModel)
    fun t(uz: String, ru: String, en: String) = adminT(lang, uz, ru, en)

    var filterStatus by remember { mutableStateOf("Barchasi") }
    var rejectDialogReq by remember { mutableStateOf<PaymentRequestLocal?>(null) }
    var rejectReason by remember { mutableStateOf("Chek tasdiqlanmadi / Rasm tushunarsiz") }

    val filtered = remember(payments, filterStatus) {
        when (filterStatus) {
            "Kutilmoqda" -> payments.filter { it.status == "pending" }
            "Tasdiqlangan" -> payments.filter { it.status == "approved" }
            "Rad etilgan" -> payments.filter { it.status == "rejected" }
            else -> payments
        }
    }
    val pending = remember(payments) { payments.count { it.status == "pending" } }
    val approved = remember(payments) { payments.count { it.status == "approved" } }
    val rejected = remember(payments) { payments.count { it.status == "rejected" } }

    val filterKeys = listOf("Barchasi", "Kutilmoqda", "Tasdiqlangan", "Rad etilgan")
    fun filterLabel(key: String) = when (key) {
        "Barchasi" -> t("Barchasi", "Все", "All")
        "Kutilmoqda" -> t("Kutilmoqda", "Ожидают", "Pending")
        "Tasdiqlangan" -> t("Tasdiqlangan", "Подтверждённые", "Approved")
        else -> t("Rad etilgan", "Отклонённые", "Rejected")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "tiles") {
            AdminTileRow {
                AdminMetricTile(Icons.Default.Schedule, c.tintPeach, pending.toString(), t("Kutilmoqda", "Ожидают", "Pending"))
                AdminMetricTile(Icons.Default.CheckCircle, c.tintTeal, approved.toString(), t("Tasdiqlangan", "Одобрено", "Approved"))
                AdminMetricTile(Icons.Default.Cancel, c.tintViolet, rejected.toString(), t("Rad etilgan", "Отклонено", "Rejected"))
            }
        }

        item(key = "note") {
            // Approval now reaches the real requester's own device via the per-user
            // paymentRequests Firestore listener (see AppViewModel.startPaymentRequestsFirestoreListener),
            // but that requires Firestore rules + Cloud Functions to actually be deployed to a
            // real Firebase project — say so, rather than implying it's guaranteed.
            MedAIInfoBanner(
                text = t(
                    "Tasdiqlash so'rov egasining qurilmasiga Firestore orqali yetadi — buning uchun loyiha haqiqiy Firebase'ga deploy qilingan bo'lishi kerak.",
                    "Подтверждение доходит до устройства автора запроса через Firestore — для этого проект должен быть развёрнут в реальном Firebase.",
                    "Approval reaches the requester's device through Firestore — the project must be deployed to a real Firebase for this to work."
                ),
                tone = MedAITone.Warning
            )
        }

        item(key = "filters") {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                filterKeys.forEach { st ->
                    MedAIFilterChip(text = filterLabel(st), selected = filterStatus == st, onClick = { filterStatus = st })
                }
            }
        }

        if (filtered.isEmpty()) {
            item(key = "empty") {
                MedAIEmptyState(
                    title = t("To'lov so'rovlari mavjud emas.", "Платёжных запросов нет.", "No payment requests."),
                    icon = Icons.Default.ReceiptLong
                )
            }
        } else {
            items(filtered, key = { it.id }) { req ->
                PaymentItemCard(
                    req = req,
                    onApprove = { viewModel.approvePayment(req) },
                    onReject = { rejectDialogReq = req },
                    lang = lang
                )
            }
        }
    }

    if (rejectDialogReq != null) {
        AdminFormDialog(
            title = t("To'lov chekini rad etish", "Отклонить чек оплаты", "Reject payment receipt"),
            confirmText = t("Rad etish", "Отклонить", "Reject"),
            dismissText = t("Bekor qilish", "Отмена", "Cancel"),
            destructive = true,
            onDismiss = { rejectDialogReq = null },
            onConfirm = {
                rejectDialogReq?.let { r ->
                    viewModel.rejectPayment(r, rejectReason)
                }
                rejectDialogReq = null
            }
        ) {
            MedAITextField(
                value = rejectReason,
                onValueChange = { rejectReason = it },
                label = t("Rad etish sababi:", "Причина отклонения:", "Reason for rejection:"),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PaymentItemCard(
    req: PaymentRequestLocal,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    lang: String = "uz"
) {
    val c = MedAITheme.colors
    fun t(uz: String, ru: String, en: String) = adminT(lang, uz, ru, en)

    val sdf = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val dateStr = remember(req.submittedAt) { sdf.format(Date(req.submittedAt)) }

    val tone = when (req.status) {
        "approved" -> MedAIBadgeTone.Success
        "rejected" -> MedAIBadgeTone.Danger
        else -> MedAIBadgeTone.Warning
    }
    val statusText = when (req.status) {
        "approved" -> t("Tasdiqlangan", "Подтверждён", "Approved")
        "rejected" -> t("Rad etilgan", "Отклонён", "Rejected")
        else -> t("Kutilmoqda", "Ожидает", "Pending")
    }
    val statusIcon = when (req.status) {
        "approved" -> Icons.Default.Check
        "rejected" -> Icons.Default.Close
        else -> Icons.Default.Schedule
    }

    MedAICard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    t("To'lov so'rovi (VIP 49,000 UZS)", "Запрос на оплату (VIP 49,000 UZS)", "Payment request (VIP 49,000 UZS)"),
                    style = MaterialTheme.typography.titleSmall, color = c.textPrimary
                )
                Text(
                    "${req.userName} • ${req.userPhone} • $dateStr",
                    style = MaterialTheme.typography.bodySmall, color = c.textSecondary
                )
            }
            Spacer(Modifier.width(8.dp))
            MedAIBadge(statusText, tone, icon = statusIcon)
        }

        Spacer(Modifier.height(10.dp))
        // Receipt preview box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(c.surfaceSunken)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(c.tintSky.bg), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = c.tintSky.fg, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    t("Chek nusxasi qabul qilingan", "Копия чека получена", "Receipt copy received"),
                    style = MaterialTheme.typography.titleSmall, color = c.textPrimary
                )
                Text(
                    text = if (req.status == "rejected") "${t("Sabab", "Причина", "Reason")}: ${req.rejectionReason ?: t("Noma'lum", "Неизвестно", "Unknown")}"
                    else "${t("Admin tekshiruvi", "Проверка админа", "Admin review")}: ${req.reviewedBy ?: t("Kutilmoqda", "Ожидает", "Pending")}",
                    style = MaterialTheme.typography.bodySmall, color = c.textSecondary
                )
            }
        }

        if (req.status == "pending") {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminActionButton(
                    text = t("Tasdiqlash", "Подтвердить", "Approve"),
                    onClick = onApprove,
                    bg = c.brand, fg = c.onBrand,
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                AdminActionButton(
                    text = t("Rad etish", "Отклонить", "Reject"),
                    onClick = onReject,
                    bg = c.danger, fg = c.onDanger,
                    icon = Icons.Default.Close,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

// ==========================================
// TAB 2: SYSTEM CONTROL & FEATURE FLAGS
// ==========================================

@Composable
fun SystemControlTab(
    viewModel: AppViewModel,
    config: AppConfigLocal?,
    flags: FeatureFlagsConfig?,
    version: AppVersionConfig?,
    banner: PopupBannerConfig?,
    announcement: AnnouncementConfig?
) {
    val c = MedAITheme.colors
    val lang = adminLang(viewModel)
    fun t(uz: String, ru: String, en: String) = adminT(lang, uz, ru, en)

    var symptomChecked by remember(flags) { mutableStateOf(flags?.symptomChecker ?: true) }
    var doctorChecked by remember(flags) { mutableStateOf(flags?.aiDoctor ?: true) }
    var labChecked by remember(flags) { mutableStateOf(flags?.labAnalysis ?: true) }
    var familyChecked by remember(flags) { mutableStateOf(flags?.family ?: true) }
    var analyticsChecked by remember(flags) { mutableStateOf(flags?.analytics ?: true) }

    var minVersionText by remember(version) { mutableStateOf(version?.minVersion ?: "1.0.0") }
    var forceUpdateChecked by remember(version) { mutableStateOf(version?.forceUpdate ?: false) }

    var announceActive by remember(announcement) { mutableStateOf(announcement?.active ?: false) }
    var announceTextUz by remember(announcement) { mutableStateOf(announcement?.textUz ?: "MedAI tizimi yangilandi.") }

    val maintenanceOn = config?.maintenanceMode ?: false
    val enabledFlags = listOf(symptomChecked, doctorChecked, labChecked, familyChecked, analyticsChecked).count { it }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "tiles") {
            AdminTileRow {
                AdminMetricTile(
                    Icons.Default.Construction, c.tintPeach,
                    if (maintenanceOn) t("Yoqiq", "Вкл", "On") else t("O'chiq", "Выкл", "Off"),
                    t("Texnik rejim", "Техработы", "Maintenance")
                )
                AdminMetricTile(Icons.Default.Tune, c.tintTeal, "$enabledFlags/5", t("Funksiyalar", "Функции", "Features"))
                AdminMetricTile(Icons.Default.Info, c.tintSky, minVersionText, t("Min. versiya", "Мин. версия", "Min. version"))
            }
        }

        // 1. Maintenance Mode Switch Card
        item(key = "maintenance") {
            MedAICard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                AdminSwitchRow(
                    title = t("Texnik xizmat rejimi (Maintenance)", "Режим техобслуживания (Maintenance)", "Maintenance mode"),
                    subtitle = t(
                        "Yoqilganda oddiy foydalanuvchilar kirishi to'xtatiladi, faqat admin ishlay oladi.",
                        "При включении обычные пользователи не смогут войти, работает только админ.",
                        "When on, regular users are locked out and only the admin can work."
                    ),
                    checked = maintenanceOn,
                    onChange = { viewModel.toggleMaintenanceMode(it) },
                    icon = Icons.Default.Construction,
                    tint = c.tintPeach,
                    danger = true
                )
            }
        }

        // 2. Feature Flags Control
        item(key = "flags") {
            MedAICard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                AdminCardHeader(Icons.Default.Tune, c.tintTeal, t("Funksiyalar boshqaruvi (Feature Flags)", "Управление функциями (Feature Flags)", "Feature flags"))
                AdminSwitchRow(t("Symptom Checker (Alomatlar)", "Проверка симптомов", "Symptom Checker"), symptomChecked, { symptomChecked = it })
                AdminDivider()
                AdminSwitchRow(t("AI Shifokor (Doctor)", "ИИ-врач (Doctor)", "AI Doctor"), doctorChecked, { doctorChecked = it })
                AdminDivider()
                AdminSwitchRow(t("Laboratoriya tahlili (Lab Vision)", "Анализ лаборатории (Lab Vision)", "Lab analysis (Lab Vision)"), labChecked, { labChecked = it })
                AdminDivider()
                AdminSwitchRow(t("Oila salomatligi (Family)", "Здоровье семьи (Family)", "Family health"), familyChecked, { familyChecked = it })
                AdminDivider()
                AdminSwitchRow(t("Salomatlik tahlili (Analytics)", "Аналитика здоровья (Analytics)", "Health analytics"), analyticsChecked, { analyticsChecked = it })
                Spacer(Modifier.height(8.dp))
                MedAIPrimaryButton(
                    text = t("Funksiyalarni saqlash", "Сохранить функции", "Save features"),
                    onClick = {
                        viewModel.updateFeatureFlags(
                            symptom = symptomChecked,
                            doctor = doctorChecked,
                            lab = labChecked,
                            family = familyChecked,
                            sos = false,
                            analytics = analyticsChecked
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 3. App Version & Force Update
        item(key = "version") {
            MedAICard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                AdminCardHeader(Icons.Default.Info, c.tintSky, t("Ilova versiyasi va majburiy yangilanish", "Версия приложения и принудительное обновление", "App version and forced update"))
                Spacer(Modifier.height(12.dp))
                MedAITextField(
                    value = minVersionText,
                    onValueChange = { minVersionText = it },
                    label = t("Minimal versiya (masalan 1.0.1)", "Минимальная версия (например 1.0.1)", "Minimum version (e.g. 1.0.1)"),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                AdminSwitchRow(
                    t("Majburiy yangilash (Force Update)", "Принудительное обновление (Force Update)", "Force update"),
                    forceUpdateChecked, { forceUpdateChecked = it }
                )
                Spacer(Modifier.height(8.dp))
                MedAIPrimaryButton(
                    text = t("Versiyani saqlash", "Сохранить версию", "Save version"),
                    onClick = {
                        viewModel.updateAppVersion(
                            force = forceUpdateChecked,
                            minVer = minVersionText,
                            msgUz = "Iltimos ilovani yangilang",
                            msgRu = "Пожалуйста обновите приложение",
                            msgEn = "Please update the app",
                            storeUrl = "https://play.google.com"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 4. Pop-up Banner & Announcement
        item(key = "announcement") {
            MedAICard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                AdminSwitchRow(
                    title = t("Global banner va e'lon", "Глобальный баннер и объявление", "Global banner and announcement"),
                    checked = announceActive,
                    onChange = { announceActive = it },
                    icon = Icons.Default.Campaign,
                    tint = c.tintViolet
                )
                Spacer(Modifier.height(8.dp))
                MedAITextField(
                    value = announceTextUz,
                    onValueChange = { announceTextUz = it },
                    label = t("E'lon matni (o'zbekcha)", "Текст объявления (на узбекском)", "Announcement text (Uzbek)"),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                MedAIPrimaryButton(
                    text = t("E'lonni saqlash", "Сохранить объявление", "Save announcement"),
                    onClick = {
                        viewModel.updateAnnouncement(
                            active = announceActive,
                            textUz = announceTextUz,
                            textRu = announceTextUz,
                            textEn = announceTextUz,
                            color = "#2E7D32"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
