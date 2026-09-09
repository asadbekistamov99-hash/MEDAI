@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Extra per-section accent colors for the admin panel, in the same spirit as the
// home screen's colored feature-grid tiles (PrimaryGreen stays the overall brand anchor).
private val AdminIndigo = Color(0xFF4F46E5)
private val AdminSlate = Color(0xFF475569)

@Composable
fun AdminScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
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
            topBar = {
                AppHeader(title = "Kirish cheklangan", onBack = onBack)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MedicalBackground)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = ErrorRed.copy(alpha = 0.18f),
                            spotColor = ErrorRed.copy(alpha = 0.18f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(ErrorRed.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GppBad,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Ruxsat Berilmagan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Admin panel faqat $SUPER_ADMIN_EMAIL hisobi orqali kirgan foydalanuvchi uchun ochiq. Joriy hisobingiz: ${currentUser?.email ?: "Noma'lum"}",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(22.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Bosh sahifaga qaytish", fontWeight = FontWeight.Bold)
                        }
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
    val tabTitles = listOf("Foydalanuvchilar", "To'lovlar", "Tizim", "Tibbiy CMS", "Jurnallar")
    val tabIcons = listOf(Icons.Default.Group, Icons.Default.ReceiptLong, Icons.Default.AdminPanelSettings, Icons.Default.MedicalServices, Icons.Default.History)
    val tabColors = listOf(PrimaryGreen, AccentCyan, AdminIndigo, SecondaryGreen, AdminSlate)

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.border(1.dp, MedicalBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Admin Boshqaruv Paneli",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(WarningOrange, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SUPER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            Text(
                                text = "${currentUser?.email ?: SUPER_ADMIN_EMAIL} • Online",
                                fontSize = 11.sp,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Quick Summary Metrics Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(label = "Foydalanuvchilar", count = systemUsers.size.toString(), color = PrimaryGreen)
                        MetricBadge(label = "Premium VIP", count = systemUsers.count { it.isPremium }.toString(), color = PremiumPurple)
                        val pendingCount = paymentRequests.count { it.status == "pending" }
                        MetricBadge(label = "Kutilayotgan Cheklar", count = pendingCount.toString(), color = if (pendingCount > 0) WarningOrange else Color.Gray)
                        val unresolvedErrors = errorLogs.count { !it.isResolved }
                        MetricBadge(label = "Xatolar", count = unresolvedErrors.toString(), color = if (unresolvedErrors > 0) ErrorRed else SuccessGreen)
                    }

                    // Navigation Tabs — polished segmented pill switcher
                    Divider(color = MedicalBorder.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            AdminTabPill(
                                title = title,
                                icon = tabIcons[index],
                                selected = selectedTab == index,
                                accent = tabColors[index],
                                onClick = { selectedTab = index }
                            )
                        }
                    }
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

@Composable
private fun MetricBadge(label: String, count: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Text(text = count, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

// Polished segmented-pill tab item used by the admin panel's main and sub tab switchers.
@Composable
private fun AdminTabPill(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) accent.copy(alpha = 0.12f) else Color.Transparent)
            .then(
                if (selected) Modifier.border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) accent else TextSecondary,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) accent else TextSecondary,
            maxLines = 1
        )
    }
}

// Centered icon + text empty-state, used across the admin panel's list tabs.
@Composable
private fun AdminEmptyState(
    icon: ImageVector,
    text: String,
    accent: Color = TextSecondary
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(accent.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = text, fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

// ==========================================
// TAB 0: USERS MANAGEMENT
// ==========================================

@Composable
fun UsersManagementTab(viewModel: AppViewModel, users: List<UserSystem>) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        item {
            Text(
                text = "FOYDALANUVCHILARNI BOSHQARISH".uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = PrimaryGreen,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Search & Cold Users broadcast action
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = PrimaryGreen.copy(alpha = 0.1f),
                        spotColor = PrimaryGreen.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Ism, email yoki telefon orqali qidirish...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = MedicalBorder
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Barchasi", "Premium", "Bloklangan", "Sovuq (30+ kun)").forEach { f ->
                            FilterChip(
                                selected = selectedFilter == f,
                                onClick = { selectedFilter = f },
                                label = { Text(f, fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen.copy(alpha = 0.15f),
                                    selectedLabelColor = PrimaryGreen
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.sendNotificationToColdUsers() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("30 kundan beri kirmaganlarga eslatma yuborish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (filteredUsers.isEmpty()) {
            item {
                AdminEmptyState(
                    icon = Icons.Default.Search,
                    text = "Mos foydalanuvchilar topilmadi.",
                    accent = PrimaryGreen
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
                    }
                )
            }
        }
    }

    if (blockDialogUser != null) {
        AlertDialog(
            onDismissRequest = { blockDialogUser = null },
            title = { Text("Foydalanuvchini bloklash: ${blockDialogUser?.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Iltimos, bloklash sababini kiriting:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = blockReason,
                        onValueChange = { blockReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        blockDialogUser?.let { u ->
                            viewModel.blockUser(u.uid, u.name, blockReason)
                        }
                        blockDialogUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Bloklash")
                }
            },
            dismissButton = {
                TextButton(onClick = { blockDialogUser = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}

@Composable
fun UserSystemCard(
    user: UserSystem,
    onExtendPremium: () -> Unit,
    onSendReminder: () -> Unit,
    onToggleBlock: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val lastActiveStr = remember(user.lastActive) { sdf.format(Date(user.lastActive)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = PrimaryGreen.copy(alpha = 0.08f),
                spotColor = PrimaryGreen.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (user.isBanned) ErrorRed.copy(alpha = 0.4f)
            else if (user.isPremium) PremiumPurple.copy(alpha = 0.3f)
            else MedicalBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (user.isBanned) ErrorRed.copy(alpha = 0.2f)
                            else if (user.isPremium) PremiumPurple.copy(alpha = 0.15f)
                            else PrimaryGreen.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (user.isBanned) ErrorRed else if (user.isPremium) PremiumPurple else PrimaryGreen
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user.isPremium) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(PremiumPurple, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("VIP", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (user.isBanned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(ErrorRed, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("BLOK", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "${user.email} • ${user.phone}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // User statistics and screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MedicalBackground, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Oxirgi faollik: $lastActiveStr",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Ekran: ${user.currentScreen}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryGreen
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExtendPremium,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, PremiumPurple)
                ) {
                    Text("+30 kun VIP", fontSize = 11.sp, color = PremiumPurple, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSendReminder,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, AccentCyan)
                ) {
                    Text("Eslatma", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onToggleBlock,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBanned) SuccessGreen else ErrorRed
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (user.isBanned) "Ochish" else "Blok",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 1: PAYMENTS & CHECKS MANAGEMENT
// ==========================================

@Composable
fun PaymentsManagementTab(viewModel: AppViewModel, payments: List<PaymentRequestLocal>) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "TO'LOV SO'ROVLARI".uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = AccentCyan,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            // There is no real multi-device backend yet: "approve" only grants premium on
            // THIS device if its locally signed-in user happens to be the requester. Say so,
            // rather than letting the admin believe every approval reaches the real user.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarningOrange.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .border(1.dp, WarningOrange.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = WarningOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hozircha real backend yo'q: tasdiqlash faqat shu qurilmadagi joriy foydalanuvchi so'rov egasi bo'lsa premium beradi.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Barchasi", "Kutilmoqda", "Tasdiqlangan", "Rad etilgan").forEach { st ->
                    FilterChip(
                        selected = filterStatus == st,
                        onClick = { filterStatus = st },
                        label = { Text(st, fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan.copy(alpha = 0.15f),
                            selectedLabelColor = AccentCyan
                        )
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                AdminEmptyState(
                    icon = Icons.Default.ReceiptLong,
                    text = "To'lov so'rovlari mavjud emas.",
                    accent = AccentCyan
                )
            }
        } else {
            items(filtered, key = { it.id }) { req ->
                PaymentItemCard(
                    req = req,
                    onApprove = { viewModel.approvePayment(req) },
                    onReject = { rejectDialogReq = req }
                )
            }
        }
    }

    if (rejectDialogReq != null) {
        AlertDialog(
            onDismissRequest = { rejectDialogReq = null },
            title = { Text("To'lov chekini rad etish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Rad etish sababi:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        rejectDialogReq?.let { r ->
                            viewModel.rejectPayment(r, rejectReason)
                        }
                        rejectDialogReq = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Rad etish")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectDialogReq = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}

@Composable
fun PaymentItemCard(
    req: PaymentRequestLocal,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val dateStr = remember(req.submittedAt) { sdf.format(Date(req.submittedAt)) }

    val statusColor = when (req.status) {
        "approved" -> SuccessGreen
        "rejected" -> ErrorRed
        else -> WarningOrange
    }

    val statusText = when (req.status) {
        "approved" -> "✅ Tasdiqlangan"
        "rejected" -> "❌ Rad etilgan"
        else -> "⏳ Kutilmoqda"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = statusColor.copy(alpha = 0.1f),
                spotColor = statusColor.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "To'lov so'rovi (VIP 49,000 UZS)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${req.userName} • ${req.userPhone} • $dateStr",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            // Receipt preview icon box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MedicalBackground, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AccentCyan.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = AccentCyan)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Chek nusxasi qabul qilingan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (req.status == "rejected") "Sabab: ${req.rejectionReason ?: "Noma'lum"}"
                        else "Admin tekshiruvi: ${req.reviewedBy ?: "Kutilmoqda"}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            if (req.status == "pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tasdiqlash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onReject,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rad etish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
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
    val context = LocalContext.current

    var symptomChecked by remember(flags) { mutableStateOf(flags?.symptomChecker ?: true) }
    var doctorChecked by remember(flags) { mutableStateOf(flags?.aiDoctor ?: true) }
    var labChecked by remember(flags) { mutableStateOf(flags?.labAnalysis ?: true) }
    var familyChecked by remember(flags) { mutableStateOf(flags?.family ?: true) }
    var analyticsChecked by remember(flags) { mutableStateOf(flags?.analytics ?: true) }

    var minVersionText by remember(version) { mutableStateOf(version?.minVersion ?: "1.0.0") }
    var forceUpdateChecked by remember(version) { mutableStateOf(version?.forceUpdate ?: false) }

    var bannerActive by remember(banner) { mutableStateOf(banner?.active ?: false) }
    var bannerTitleUz by remember(banner) { mutableStateOf(banner?.titleUz ?: "Maxsus Taklif!") }
    var bannerSubUz by remember(banner) { mutableStateOf(banner?.subtitleUz ?: "Bugun VIP a'zolikni faollashtiring!") }

    var announceActive by remember(announcement) { mutableStateOf(announcement?.active ?: false) }
    var announceTextUz by remember(announcement) { mutableStateOf(announcement?.textUz ?: "MedAI tizimi yangilandi.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "TIZIM BOSHQARUVI".uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = AdminIndigo,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // 1. Maintenance Mode Switch Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = WarningOrange.copy(alpha = 0.1f),
                        spotColor = WarningOrange.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (config?.maintenanceMode == true) ErrorRed.copy(alpha = 0.5f) else MedicalBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(WarningOrange.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Construction, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Texnik xizmat rejimi (Maintenance)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text(
                                text = "Yoqilganda oddiy foydalanuvchilar kirishi to'xtatiladi, faqat admin ishlay oladi.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Switch(
                        checked = config?.maintenanceMode ?: false,
                        onCheckedChange = { viewModel.toggleMaintenanceMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ErrorRed, checkedTrackColor = ErrorRed.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // 2. Feature Flags Control
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = AdminIndigo.copy(alpha = 0.08f),
                        spotColor = AdminIndigo.copy(alpha = 0.08f)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AdminIndigo.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = AdminIndigo, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Funksiyalar Boshqaruvi (Feature Flags)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }

                    FeatureSwitchRow("Symptom Checker (Alomatlar)", symptomChecked) { symptomChecked = it }
                    FeatureSwitchRow("AI Shifokor (Doctor)", doctorChecked) { doctorChecked = it }
                    FeatureSwitchRow("Laboratoriya Tahlili (Lab Vision)", labChecked) { labChecked = it }
                    FeatureSwitchRow("Oila Salomatligi (Family)", familyChecked) { familyChecked = it }
                    FeatureSwitchRow("Salomatlik Tahlili (Analytics)", analyticsChecked) { analyticsChecked = it }

                    Button(
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Funksiyalarni Saqlash", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. App Version & Force Update
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = AccentCyan.copy(alpha = 0.08f),
                        spotColor = AccentCyan.copy(alpha = 0.08f)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AccentCyan.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Ilova Versiyasi & Majburiy Yangilanish", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }

                    OutlinedTextField(
                        value = minVersionText,
                        onValueChange = { minVersionText = it },
                        label = { Text("Minimal versiya (masalan 1.0.1)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = MedicalBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Majburiy yangilash (Force Update)", fontSize = 12.sp, color = TextPrimary)
                        Switch(
                            checked = forceUpdateChecked,
                            onCheckedChange = { forceUpdateChecked = it }
                        )
                    }

                    Button(
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text("Versiyani Saqlash", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Pop-up Banner & Announcement
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = PremiumPurple.copy(alpha = 0.08f),
                        spotColor = PremiumPurple.copy(alpha = 0.08f)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PremiumPurple.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = PremiumPurple, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Global Banner & E'lon", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        }
                        Switch(
                            checked = announceActive,
                            onCheckedChange = { announceActive = it }
                        )
                    }

                    OutlinedTextField(
                        value = announceTextUz,
                        onValueChange = { announceTextUz = it },
                        label = { Text("E'lon matni (O'zbekcha)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PremiumPurple,
                            unfocusedBorderColor = MedicalBorder
                        )
                    )

                    Button(
                        onClick = {
                            viewModel.updateAnnouncement(
                                active = announceActive,
                                textUz = announceTextUz,
                                textRu = announceTextUz,
                                textEn = announceTextUz,
                                color = "#2E7D32"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple)
                    ) {
                        Text("E'lonni Saqlash", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen, checkedTrackColor = PrimaryGreen.copy(alpha = 0.5f))
        )
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
    var cmsSubTab by remember { mutableStateOf(0) }
    var showAddDiseaseDialog by remember { mutableStateOf(false) }
    var showAddMedicineDialog by remember { mutableStateOf(false) }
    var showAddTipDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AdminTabPill(
                        title = "Kasalliklar (${diseases.size})",
                        icon = Icons.Default.HealthAndSafety,
                        selected = cmsSubTab == 0,
                        accent = SecondaryGreen,
                        onClick = { cmsSubTab = 0 }
                    )
                    AdminTabPill(
                        title = "Dorilar (${medicines.size})",
                        icon = Icons.Default.Medication,
                        selected = cmsSubTab == 1,
                        accent = SecondaryGreen,
                        onClick = { cmsSubTab = 1 }
                    )
                    AdminTabPill(
                        title = "Maslahatlar (${tips.size})",
                        icon = Icons.Default.TipsAndUpdates,
                        selected = cmsSubTab == 2,
                        accent = SecondaryGreen,
                        onClick = { cmsSubTab = 2 }
                    )
                }
                Divider(color = MedicalBorder.copy(alpha = 0.5f))
            }
        }

        when (cmsSubTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddDiseaseDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yangi Kasallik Qo'shish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (diseases.isEmpty()) {
                        item {
                            AdminEmptyState(
                                icon = Icons.Default.HealthAndSafety,
                                text = "Kasalliklar ro'yxati bo'sh.",
                                accent = SecondaryGreen
                            )
                        }
                    }

                    items(diseases) { d ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = SecondaryGreen.copy(alpha = 0.06f),
                                    spotColor = SecondaryGreen.copy(alpha = 0.06f)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(SecondaryGreen.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = SecondaryGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = d.nameUz, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Alomatlar: ${d.symptomsUz}", fontSize = 11.sp, color = TextSecondary)
                                    Text(text = "Mutaxassis: ${d.specialistType} • Daraja: ${d.severity}", fontSize = 10.sp, color = SecondaryGreen)
                                }
                                IconButton(onClick = { viewModel.deleteDisease(d.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddMedicineDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yangi Dori Qo'shish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (medicines.isEmpty()) {
                        item {
                            AdminEmptyState(
                                icon = Icons.Default.Medication,
                                text = "Dorilar ro'yxati bo'sh.",
                                accent = SecondaryGreen
                            )
                        }
                    }

                    items(medicines) { m ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = SecondaryGreen.copy(alpha = 0.06f),
                                    spotColor = SecondaryGreen.copy(alpha = 0.06f)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(SecondaryGreen.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Medication, contentDescription = null, tint = SecondaryGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = m.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Dozasi: ${m.dosage} • Toifasi: ${m.category}", fontSize = 11.sp, color = TextSecondary)
                                    Text(text = m.description, fontSize = 10.sp, color = TextSecondary, maxLines = 2)
                                }
                                IconButton(onClick = { viewModel.deleteMedicine(m.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddTipDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yangi Maslahat Qo'shish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (tips.isEmpty()) {
                        item {
                            AdminEmptyState(
                                icon = Icons.Default.TipsAndUpdates,
                                text = "Maslahatlar ro'yxati bo'sh.",
                                accent = SecondaryGreen
                            )
                        }
                    }

                    items(tips) { t ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = SecondaryGreen.copy(alpha = 0.06f),
                                    spotColor = SecondaryGreen.copy(alpha = 0.06f)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(SecondaryGreen.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = SecondaryGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = t.uz, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Text(text = t.ru, fontSize = 11.sp, color = TextSecondary)
                                }
                                IconButton(onClick = { viewModel.deleteHealthTip(t.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Disease Dialog
    if (showAddDiseaseDialog) {
        var dName by remember { mutableStateOf("") }
        var dSymptoms by remember { mutableStateOf("") }
        var dDesc by remember { mutableStateOf("") }
        var dSpec by remember { mutableStateOf("Terapevt") }

        AlertDialog(
            onDismissRequest = { showAddDiseaseDialog = false },
            title = { Text("Yangi Kasallik Kiritish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dName, onValueChange = { dName = it }, label = { Text("Kasallik nomi") })
                    OutlinedTextField(value = dSymptoms, onValueChange = { dSymptoms = it }, label = { Text("Alomatlari") })
                    OutlinedTextField(value = dDesc, onValueChange = { dDesc = it }, label = { Text("Tavsifi") })
                    OutlinedTextField(value = dSpec, onValueChange = { dSpec = it }, label = { Text("Shifokor mutaxassisligi") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dName.isNotBlank()) {
                            viewModel.addDisease(
                                nameUz = dName, nameRu = dName, nameEn = dName,
                                symptomsUz = dSymptoms, descUz = dDesc, descRu = dDesc, descEn = dDesc,
                                specType = dSpec, severity = "medium"
                            )
                            showAddDiseaseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDiseaseDialog = false }) { Text("Bekor qilish") }
            }
        )
    }

    // Add Medicine Dialog
    if (showAddMedicineDialog) {
        var mName by remember { mutableStateOf("") }
        var mDosage by remember { mutableStateOf("1 tabletka 2 mahal") }
        var mDesc by remember { mutableStateOf("") }
        var mCat by remember { mutableStateOf("Og'riq qoldiruvchi") }

        AlertDialog(
            onDismissRequest = { showAddMedicineDialog = false },
            title = { Text("Yangi Dori Kiritish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = mName, onValueChange = { mName = it }, label = { Text("Dori nomi") })
                    OutlinedTextField(value = mDosage, onValueChange = { mDosage = it }, label = { Text("Dozasi") })
                    OutlinedTextField(value = mDesc, onValueChange = { mDesc = it }, label = { Text("Tavsif va ko'rsatmalar") })
                    OutlinedTextField(value = mCat, onValueChange = { mCat = it }, label = { Text("Toifasi") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mName.isNotBlank()) {
                            viewModel.addMedicine(
                                name = mName, description = mDesc, dosage = mDosage,
                                sideEffects = "Kamdan-kam hollarda allergik reaksiya", category = mCat
                            )
                            showAddMedicineDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMedicineDialog = false }) { Text("Bekor qilish") }
            }
        )
    }

    // Add Tip Dialog
    if (showAddTipDialog) {
        var tUz by remember { mutableStateOf("") }
        var tRu by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTipDialog = false },
            title = { Text("Yangi Salomatlik Maslahati") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = tUz, onValueChange = { tUz = it }, label = { Text("O'zbekcha matn") })
                    OutlinedTextField(value = tRu, onValueChange = { tRu = it }, label = { Text("Ruscha matn (ixtiyoriy)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tUz.isNotBlank()) {
                            viewModel.addHealthTip(uz = tUz, ru = if (tRu.isNotBlank()) tRu else tUz, en = tUz, orderIndex = tips.size)
                            showAddTipDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTipDialog = false }) { Text("Bekor qilish") }
            }
        )
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
    val sdf = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }
    var logsSubTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AdminTabPill(
                        title = "Admin Jurnali (${adminLogs.size})",
                        icon = Icons.Default.History,
                        selected = logsSubTab == 0,
                        accent = AdminSlate,
                        onClick = { logsSubTab = 0 }
                    )
                    AdminTabPill(
                        title = "Xatolar (${errorLogs.size})",
                        icon = Icons.Default.Warning,
                        selected = logsSubTab == 1,
                        accent = ErrorRed,
                        onClick = { logsSubTab = 1 }
                    )
                }
                Divider(color = MedicalBorder.copy(alpha = 0.5f))
            }
        }

        when (logsSubTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (adminLogs.isEmpty()) {
                        item {
                            AdminEmptyState(
                                icon = Icons.Default.History,
                                text = "Jurnal yozuvlari mavjud emas.",
                                accent = AdminSlate
                            )
                        }
                    } else {
                        items(adminLogs) { log ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 3.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = AdminSlate.copy(alpha = 0.06f),
                                        spotColor = AdminSlate.copy(alpha = 0.06f)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MedicalBorder)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(AdminSlate.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = null, tint = AdminSlate, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
                                            Text(text = sdf.format(Date(log.timestamp)), fontSize = 10.sp, color = TextSecondary)
                                        }
                                        Text(text = "Kimga: ${log.targetUser} • Admin: ${log.adminEmail}", fontSize = 11.sp, color = TextSecondary)
                                        Text(text = log.details, fontSize = 12.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (errorLogs.isEmpty()) {
                        item {
                            AdminEmptyState(
                                icon = Icons.Default.CheckCircle,
                                text = "Xatolar aniqlanmagan. Tizim barqaror!",
                                accent = SuccessGreen
                            )
                        }
                    } else {
                        items(errorLogs) { err ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 3.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = ErrorRed.copy(alpha = 0.06f),
                                        spotColor = ErrorRed.copy(alpha = 0.06f)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (err.isResolved) SuccessGreen.copy(alpha = 0.3f) else ErrorRed.copy(alpha = 0.4f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                (if (err.isResolved) SuccessGreen else ErrorRed).copy(alpha = 0.12f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (err.isResolved) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (err.isResolved) SuccessGreen else ErrorRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "Ekran: ${err.screen}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ErrorRed)
                                            Text(text = sdf.format(Date(err.timestamp)), fontSize = 10.sp, color = TextSecondary)
                                        }
                                        Text(text = err.errorMessage, fontSize = 12.sp, color = TextPrimary)
                                        Text(text = "Qurilma: ${err.deviceInfo} • Versiya: ${err.appVersion}", fontSize = 10.sp, color = TextSecondary)

                                        if (!err.isResolved) {
                                            Button(
                                                onClick = { viewModel.resolveErrorLog(err.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("Hal qilindi", fontSize = 11.sp)
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
    }
}
