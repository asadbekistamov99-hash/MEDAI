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
import androidx.compose.ui.graphics.Color
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

@Composable
fun AdminScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isSuperAdmin = viewModel.isSuperAdmin

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
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(ErrorRed.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GppBad,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ruxsat Berilmagan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Admin panel faqat $SUPER_ADMIN_EMAIL hisobi orqali kirgan foydalanuvchi uchun ochiq. Joriy hisobingiz: ${currentUser?.email ?: "Noma'lum"}",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
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
                                        .background(Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SUPER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
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

                    // Navigation Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = PrimaryGreen,
                        divider = { Divider(color = MedicalBorder.copy(alpha = 0.5f)) }
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
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
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Text(text = count, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
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
        // Search & Cold Users broadcast action
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Foydalanuvchini ism, email yoki telefon orqali qidirish...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
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
                                label = { Text(f, fontSize = 11.sp) }
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.sendNotificationToColdUsers() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Mos foydalanuvchilar topilmadi.", color = TextSecondary)
                }
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                        .size(44.dp)
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
                                    .background(PremiumPurple, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text("VIP", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (user.isBanned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(ErrorRed, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
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
                    .background(MedicalBackground, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
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
                        label = { Text(st, fontSize = 11.sp) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("To'lov so'rovlari mavjud emas.", color = TextSecondary)
                }
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
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
                    .background(MedicalBackground, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = PrimaryGreen)
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
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tasdiqlash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onReject,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        modifier = Modifier.weight(1f)
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
        // 1. Maintenance Mode Switch Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (config?.maintenanceMode == true) ErrorRed else MedicalBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Construction, contentDescription = null, tint = WarningOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Texnik xizmat rejimi (Maintenance)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text = "Yoqilganda oddiy foydalanuvchilar kirishi to'xtatiladi, faqat admin ishlay oladi.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Funksiyalar Boshqaruvi (Feature Flags)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ilova Versiyasi & Majburiy Yangilanish", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = minVersionText,
                        onValueChange = { minVersionText = it },
                        label = { Text("Minimal versiya (masalan 1.0.1)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Majburiy yangilash (Force Update)", fontSize = 12.sp)
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Versiyani Saqlash", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Pop-up Banner & Announcement
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Global Banner & E'lon", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        shape = RoundedCornerShape(10.dp)
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
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
            .padding(vertical = 2.dp),
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
        TabRow(
            selectedTabIndex = cmsSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryGreen
        ) {
            Tab(selected = cmsSubTab == 0, onClick = { cmsSubTab = 0 }, text = { Text("Kasalliklar (${diseases.size})", fontSize = 11.sp) })
            Tab(selected = cmsSubTab == 1, onClick = { cmsSubTab = 1 }, text = { Text("Dorilar (${medicines.size})", fontSize = 11.sp) })
            Tab(selected = cmsSubTab == 2, onClick = { cmsSubTab = 2 }, text = { Text("Maslahatlar (${tips.size})", fontSize = 11.sp) })
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yangi Kasallik Qo'shish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    items(diseases) { d ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = d.nameUz, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Alomatlar: ${d.symptomsUz}", fontSize = 11.sp, color = TextSecondary)
                                    Text(text = "Mutaxassis: ${d.specialistType} • Daraja: ${d.severity}", fontSize = 10.sp, color = PrimaryGreen)
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yangi Dori Qo'shish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    items(medicines) { m ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = m.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Dozasi: ${m.dosage} • Toifasi: ${m.category}", fontSize = 11.sp, color = TextSecondary)
                                    Text(text = m.description, fontSize = 10.sp, color = Color.Gray, maxLines = 2)
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yangi Maslahat Qo'shish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    items(tips) { t ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = t.uz, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
        TabRow(
            selectedTabIndex = logsSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryGreen
        ) {
            Tab(selected = logsSubTab == 0, onClick = { logsSubTab = 0 }, text = { Text("Admin Jurnali (${adminLogs.size})", fontSize = 11.sp) })
            Tab(selected = logsSubTab == 1, onClick = { logsSubTab = 1 }, text = { Text("Xatolar (${errorLogs.size})", fontSize = 11.sp) })
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
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Jurnal yozuvlari mavjud emas.", color = TextSecondary)
                            }
                        }
                    } else {
                        items(adminLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MedicalBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (errorLogs.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Xatolar aniqlanmagan. Tizim barqaror!", color = SuccessGreen)
                            }
                        }
                    } else {
                        items(errorLogs) { err ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (err.isResolved) SuccessGreen.copy(alpha = 0.3f) else ErrorRed.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                            shape = RoundedCornerShape(8.dp),
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
