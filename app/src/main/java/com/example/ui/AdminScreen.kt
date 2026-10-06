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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val c = MedAITheme.colors
    val lang = adminLang(viewModel)
    fun t(uz: String, ru: String, en: String) = adminT(lang, uz, ru, en)

    val currentUser by viewModel.currentUser.collectAsState()
    val isSuperAdmin = viewModel.isSuperAdmin

    // Self-heals the server-side custom claim for accounts that signed in before the
    // Cloud Function existed, or after functions are (re)deployed — see functions/index.js.
    LaunchedEffect(isSuperAdmin) {
        if (isSuperAdmin) {
            viewModel.ensureAdminClaimIfEligible()
        }
    }

    if (!isSuperAdmin) {
        // Access Denied Screen
        Scaffold(
            containerColor = c.canvas,
            topBar = {
                AppHeader(title = t("Kirish cheklangan", "Доступ ограничен", "Access restricted"), onBack = onBack)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                MedAICard(modifier = Modifier.fillMaxWidth(), contentPadding = 24.dp) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(c.dangerSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GppBad, contentDescription = null, tint = c.onDangerSoft, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            t("Ruxsat berilmagan", "Нет разрешения", "Permission denied"),
                            style = MaterialTheme.typography.titleLarge, color = c.textPrimary, textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        val unknown = t("Noma'lum", "Неизвестно", "Unknown")
                        Text(
                            text = t(
                                "Admin panel faqat $SUPER_ADMIN_EMAIL hisobi orqali kirgan foydalanuvchi uchun ochiq. Joriy hisobingiz: ${currentUser?.email ?: unknown}",
                                "Админ-панель доступна только пользователю, вошедшему под аккаунтом $SUPER_ADMIN_EMAIL. Ваш текущий аккаунт: ${currentUser?.email ?: unknown}",
                                "The admin panel is only open to the user signed in as $SUPER_ADMIN_EMAIL. Your current account: ${currentUser?.email ?: unknown}"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        MedAIPrimaryButton(
                            text = t("Bosh sahifaga qaytish", "Вернуться на главную", "Back to home"),
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        return
    }

    // Authorized Admin Dashboard
    val systemUsers by viewModel.systemUsers.collectAsState()
    val paymentRequests by viewModel.paymentRequests.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()
    val featureFlags by viewModel.featureFlagsConfig.collectAsState()
    val versionConfig by viewModel.appVersionConfig.collectAsState()
    val bannerConfig by viewModel.popupBannerConfig.collectAsState()
    val announceConfig by viewModel.announcementConfig.collectAsState()
    val diseases by viewModel.diseases.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val healthTips by viewModel.healthTips.collectAsState()
    val adminLogs by viewModel.adminLogs.collectAsState()
    val errorLogs by viewModel.errorLogs.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val pendingCount = remember(paymentRequests) { paymentRequests.count { it.status == "pending" } }
    val unresolvedErrors = remember(errorLogs) { errorLogs.count { !it.isResolved } }
    val cmsCount = diseases.size + medicines.size + healthTips.size
    val tabs = listOf(
        AdminTabItem(Icons.Default.Group, t("Foydalanuvchilar", "Пользователи", "Users"), systemUsers.size, MedAIBadgeTone.Info),
        AdminTabItem(Icons.Default.ReceiptLong, t("To'lovlar", "Платежи", "Payments"), pendingCount, MedAIBadgeTone.Warning),
        AdminTabItem(Icons.Default.AdminPanelSettings, t("Tizim", "Система", "System")),
        AdminTabItem(Icons.Default.MedicalServices, t("Tibbiy CMS", "Мед. CMS", "Medical CMS"), cmsCount, MedAIBadgeTone.Info),
        AdminTabItem(Icons.Default.History, t("Jurnallar", "Журналы", "Logs"), unresolvedErrors, MedAIBadgeTone.Danger),
    )

    Scaffold(
        containerColor = c.canvas,
        topBar = {
            AppHeader(title = t("Admin paneli", "Админ-панель", "Admin panel"), onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MedAIBadge("SUPER", MedAIBadgeTone.Warning)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${currentUser?.email ?: SUPER_ADMIN_EMAIL} • Online",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
            AdminTabBar(tabs, selectedTab, { selectedTab = it }, Modifier.padding(vertical = 8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> UsersManagementTab(viewModel = viewModel, users = systemUsers)
                    1 -> PaymentsManagementTab(viewModel = viewModel, payments = paymentRequests)
                    2 -> SystemControlTab(
                        viewModel = viewModel,
                        config = appConfig,
                        flags = featureFlags,
                        version = versionConfig,
                        banner = bannerConfig,
                        announcement = announceConfig
                    )
                    3 -> MedicalCmsTab(
                        viewModel = viewModel,
                        diseases = diseases,
                        medicines = medicines,
                        tips = healthTips
                    )
                    4 -> LogsAuditTab(
                        viewModel = viewModel,
                        adminLogs = adminLogs,
                        errorLogs = errorLogs
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 0: USERS MANAGEMENT
// ==========================================

@Composable
fun UsersManagementTab(viewModel: AppViewModel, users: List<UserSystem>) {
    val c = MedAITheme.colors
    val lang = adminLang(viewModel)
    fun t(uz: String, ru: String, en: String) = adminT(lang, uz, ru, en)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Barchasi") }
    var blockDialogUser by remember { mutableStateOf<UserSystem?>(null) }
    var blockReason by remember { mutableStateOf("") }

    val filteredUsers = remember(users, searchQuery, selectedFilter) {
        users.filter { u ->
            val matchesQuery = searchQuery.isBlank() ||
                    u.name.contains(searchQuery, ignoreCase = true) ||
                    u.email.contains(searchQuery, ignoreCase = true) ||
                    u.phone.contains(searchQuery)
            val matchesFilter = when (selectedFilter) {
                "Premium" -> u.isPremium
                "Bloklangan" -> u.isBanned
                "Sovuq (30+ kun)" -> {
                    val days = (System.currentTimeMillis() - u.lastActive) / (24 * 3600 * 1000L)
                    days >= 30
                }
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }
    val premiumCount = remember(users) { users.count { it.isPremium } }
    val bannedCount = remember(users) { users.count { it.isBanned } }

    // Filter keys stay Uzbek (they are only internal identifiers); the label shown is translated.
    val filterKeys = listOf("Barchasi", "Premium", "Bloklangan", "Sovuq (30+ kun)")
    fun filterLabel(key: String) = when (key) {
        "Barchasi" -> t("Barchasi", "Все", "All")
        "Premium" -> "Premium"
        "Bloklangan" -> t("Bloklangan", "Заблокированные", "Banned")
        else -> t("Sovuq (30+ kun)", "Неактивные (30+ дн.)", "Inactive (30+ days)")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "tiles") {
            AdminTileRow {
                AdminMetricTile(Icons.Default.Group, c.tintTeal, users.size.toString(), t("Jami", "Всего", "Total"))
                AdminMetricTile(Icons.Default.WorkspacePremium, c.tintViolet, premiumCount.toString(), "Premium")
                AdminMetricTile(Icons.Default.Block, c.tintPeach, bannedCount.toString(), t("Bloklangan", "Заблок.", "Banned"))
            }
        }

        item(key = "search") {
            MedAICard(modifier = Modifier.fillMaxWidth()) {
                MedAITextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = t("Qidirish", "Поиск", "Search"),
                    placeholder = t("Ism, email yoki telefon orqali qidirish...", "Поиск по имени, email или телефону...", "Search by name, email or phone..."),
                    leadingIcon = Icons.Default.Search,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    filterKeys.forEach { f ->
                        MedAIFilterChip(text = filterLabel(f), selected = selectedFilter == f, onClick = { selectedFilter = f })
                    }
                }
                Spacer(Modifier.height(8.dp))
                MedAISecondaryButton(
                    text = t(
                        "30 kundan beri kirmaganlarga eslatma yuborish",
                        "Напомнить неактивным 30+ дней",
                        "Remind users inactive 30+ days"
                    ),
                    onClick = { viewModel.sendNotificationToColdUsers() },
                    icon = Icons.Default.Campaign,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (filteredUsers.isEmpty()) {
            item(key = "empty") {
                MedAIEmptyState(
                    title = t("Mos foydalanuvchilar topilmadi.", "Подходящие пользователи не найдены.", "No matching users found."),
                    icon = Icons.Default.Search
                )
            }
        } else {
            items(filteredUsers, key = { it.uid }) { user ->
                UserSystemCard(
                    user = user,
                    onExtendPremium = { viewModel.extendPremium(user.uid, user.name, 30) },
                    onSendReminder = { viewModel.sendPremiumExpiryReminder(user.uid, user.name, 7) },
                    onToggleBlock = {
                        if (user.isBanned) {
                            viewModel.unblockUser(user.uid, user.name)
                        } else {
                            blockDialogUser = user
                            blockReason = "Qoidalarni buzganligi sababli"
                        }
                    },
                    lang = lang
                )
            }
        }
    }

    if (blockDialogUser != null) {
        AdminFormDialog(
            title = t("Foydalanuvchini bloklash: ", "Блокировка пользователя: ", "Block user: ") + (blockDialogUser?.name ?: ""),
            confirmText = t("Bloklash", "Заблокировать", "Block"),
            dismissText = t("Bekor qilish", "Отмена", "Cancel"),
            destructive = true,
            onDismiss = { blockDialogUser = null },
            onConfirm = {
                blockDialogUser?.let { u ->
                    viewModel.blockUser(u.uid, u.name, blockReason)
                }
                blockDialogUser = null
            }
        ) {
            MedAITextField(
                value = blockReason,
                onValueChange = { blockReason = it },
                label = t("Iltimos, bloklash sababini kiriting:", "Укажите причину блокировки:", "Please enter the reason for blocking:"),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun UserSystemCard(
    user: UserSystem,
    onExtendPremium: () -> Unit,
    onSendReminder: () -> Unit,
    onToggleBlock: () -> Unit,
    lang: String = "uz"
) {
    val c = MedAITheme.colors
    fun t(uz: String, ru: String, en: String) = adminT(lang, uz, ru, en)

    val sdf = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val lastActiveStr = remember(user.lastActive) { sdf.format(Date(user.lastActive)) }

    val avatar = when {
        user.isBanned -> c.dangerSoft to c.onDangerSoft
        user.isPremium -> c.premiumSoft to c.onPremiumSoft
        else -> c.brandSoft to c.onBrandSoft
    }

    MedAICard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(avatar.first), contentAlignment = Alignment.Center) {
                Text(user.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, color = avatar.second)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(user.name, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                if (user.isPremium || user.isBanned) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (user.isPremium) MedAIBadge("VIP", MedAIBadgeTone.Premium)
                        if (user.isBanned) MedAIBadge(t("BLOK", "БЛОК", "BANNED"), MedAIBadgeTone.Danger)
                    }
                }
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                Text(user.phone, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }

        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(c.surfaceSunken)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "${t("Oxirgi faollik", "Последняя активность", "Last active")}: $lastActiveStr",
                style = MaterialTheme.typography.bodySmall, color = c.textSecondary
            )
            Text(
                "${t("Ekran", "Экран", "Screen")}: ${user.currentScreen}",
                style = MaterialTheme.typography.bodySmall, color = c.textPrimary
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AdminActionButton(
                text = t("+30 kun VIP", "+30 дней VIP", "+30 days VIP"),
                onClick = onExtendPremium,
                bg = c.premiumSoft, fg = c.onPremiumSoft,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            AdminActionButton(
                text = t("Eslatma", "Напомнить", "Remind"),
                onClick = onSendReminder,
                bg = c.infoSoft, fg = c.onInfoSoft,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            if (user.isBanned) {
                AdminActionButton(
                    text = t("Ochish", "Разблок.", "Unblock"),
                    onClick = onToggleBlock,
                    bg = c.successSoft, fg = c.onSuccessSoft,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            } else {
                AdminActionButton(
                    text = t("Blok", "Блок", "Block"),
                    onClick = onToggleBlock,
                    bg = c.danger, fg = c.onDanger,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}
