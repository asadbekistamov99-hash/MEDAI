@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.*
import com.example.i18n.Translations
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// --- MAIN CONTAINER ---

@Composable
fun MainContainer(
    navController: NavController,
    viewModel: AppViewModel,
    onNavigateToFeature: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val lang by viewModel.currentLanguage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    LaunchedEffect(user, onboardingCompleted) {
        if (user == null || !onboardingCompleted) {
            navController.navigate("onboarding") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Banned check overlay
    if (user?.isBanned == true) {
        BannedScreen(viewModel)
        return
    }

    // Maintenance check overlay
    val config by viewModel.appConfig.collectAsState()
    if (config?.maintenanceMode == true && user?.isAdmin == false) {
        MaintenanceScreen()
        return
    }

    val colors = MedAITheme.colors
    Scaffold(
        containerColor = colors.canvas,
        bottomBar = {
            // The surface colour extends behind the system navigation bar.
            Box(Modifier.background(colors.surface).navigationBarsPadding()) {
                MedAIBottomBar(
                    items = listOf(
                        MedAIBottomItem(Icons.Default.Home, Translations.getString("tab_home", lang)),
                        MedAIBottomItem(Icons.Default.History, Translations.getString("tab_history", lang)),
                        MedAIBottomItem(Icons.Default.Chat, Translations.getString("tab_chat", lang)),
                        MedAIBottomItem(Icons.Default.Person, Translations.getString("tab_profile", lang)),
                    ),
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it },
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen(viewModel, onNavigateToFeature)
                1 -> HistoryScreen(viewModel)
                2 -> GeneralChatScreen(
                    viewModel = viewModel,
                    onNavigateToUpgrade = { onNavigateToFeature("upgrade") }
                )
                3 -> ProfileScreen(viewModel, navController)
            }
        }
    }
}

// --- HOME SCREEN (FREE & PREMIUM) ---

private data class HomeRowItem(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: MedAITint,
)

@Composable
fun HomeScreen(viewModel: AppViewModel, onNavigate: (String) -> Unit) {
    val user by viewModel.currentUser.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    val steps by viewModel.dailySteps.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()
    val trialDays by viewModel.trialDaysRemaining.collectAsState()

    // hasPremiumAccess (paid subscription OR unexpired free trial) rather than the raw
    // isPremium column, so a trial account sees the same features a paying one does.
    val isPremium = user?.hasPremiumAccess ?: false
    val isPaid = user?.isPremium == true
    val score = user?.healthScore ?: 0

    var animateRows by remember { mutableStateOf(false) }
    var trialBannerDismissed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateRows = true }

    val c = MedAITheme.colors
    fun t(key: String) = Translations.getString(key, lang)

    // What each tier can reach is unchanged: the four quick actions plus this list cover exactly
    // the routes the old two-column grid offered (free: 8, premium: 12) and the help centre.
    val serviceRows = if (isPremium) {
        listOf(
            HomeRowItem("yordamchi", "MedAI Yordamchi", t("feat_sub_yordamchi"), Icons.Default.SmartToy, c.tintTeal),
            HomeRowItem("family", t("feat_family"), t("feat_sub_family"), Icons.Default.Group, c.tintPeach),
            HomeRowItem("ai_doctor", t("feat_ai_doctor"), t("feat_sub_ai_doctor"), Icons.Default.SmartToy, c.tintViolet),
            HomeRowItem("ai_tips", t("feat_ai_tips"), t("feat_sub_ai_tips"), Icons.Default.TipsAndUpdates, c.tintSky),
            HomeRowItem("lab", t("feat_lab"), t("feat_sub_lab"), Icons.Default.Science, c.tintTeal),
            HomeRowItem("analytics", t("feat_analytics"), t("feat_sub_analytics"), Icons.Default.BarChart, c.tintSky),
            HomeRowItem("notifications", t("feat_notifications"), t("feat_sub_notifications"), Icons.Default.NotificationsActive, c.tintViolet),
            HomeRowItem("services", t("feat_services"), t("feat_sub_services"), Icons.Default.LocalHospital, c.tintTeal),
        )
    } else {
        listOf(
            HomeRowItem("yordamchi", "MedAI Yordamchi", t("feat_sub_yordamchi"), Icons.Default.SmartToy, c.tintTeal),
            HomeRowItem("family", t("feat_family"), t("feat_sub_family"), Icons.Default.Group, c.tintPeach),
            HomeRowItem("notifications", t("feat_notifications"), t("feat_sub_notifications"), Icons.Default.NotificationsActive, c.tintViolet),
            HomeRowItem("analytics", t("feat_analytics"), t("feat_sub_analytics"), Icons.Default.BarChart, c.tintSky),
        )
    } + HomeRowItem("help", t("feat_help"), t("feat_sub_help"), Icons.Default.SupportAgent, c.tintTeal)

    @Composable
    fun Reveal(durationMs: Int, content: @Composable () -> Unit) {
        AnimatedVisibility(
            visible = animateRows,
            enter = fadeIn(tween(durationMs)) + slideInVertically(tween(durationMs)) { 40 },
        ) { content() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top bar: menu (all services), greeting, notifications.
        item {
            Reveal(300) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(MinTouch)
                            .clip(CircleShape)
                            .background(c.surface)
                            .border(1.dp, c.border, CircleShape)
                            .clickable(role = Role.Button) { onNavigate("services") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = t("feat_services"), tint = c.textPrimary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = String.format(t("home_greeting"), user?.name ?: "").removeSuffix("👋").trimEnd(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = c.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isPremium) {
                            Spacer(Modifier.height(2.dp))
                            MedAIBadge(
                                text = if (isPaid) t("badge_premium") else t("badge_trial"),
                                tone = MedAIBadgeTone.Premium,
                                icon = Icons.Default.WorkspacePremium
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(MinTouch)
                            .clip(CircleShape)
                            .background(c.surface)
                            .border(1.dp, c.border, CircleShape)
                            .clickable(role = Role.Button) { onNavigate("notifications") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = t("feat_notifications"), tint = c.textPrimary)
                        if (unreadNotifications > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 2.dp, end = 0.dp)
                                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                                    .background(c.danger, CircleShape)
                                    .border(2.dp, c.surface, CircleShape)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotifications > 9) "9+" else unreadNotifications.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.onDanger,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Hero: health score + (premium) activity + the one primary action.
        item {
            Reveal(400) {
                HomeHealthHero(
                    score = score,
                    steps = steps,
                    showActivity = isPremium,
                    lang = lang,
                    ctaTitle = if (isPremium) t("home_cta_ai_title") else t("home_cta_sym_title"),
                    ctaSubtitle = if (isPremium) t("home_cta_ai_sub") else t("home_cta_sym_sub"),
                    onCta = { onNavigate(if (isPremium) "ai_doctor" else "symptoms") }
                )
            }
        }

        // 3. Trial countdown. Dismissable for the session, but it comes back on next launch — a
        // user should not be able to bury the one warning that their free access is finite.
        if (trialDays > 0 && !trialBannerDismissed) {
            item {
                TrialBanner(
                    daysRemaining = trialDays,
                    lang = lang,
                    onUpgradeClick = { onNavigate("upgrade") },
                    onDismiss = { trialBannerDismissed = true },
                )
            }
        }

        // 4. Quick actions.
        item {
            Reveal(500) {
                Column {
                    Text(t("home_quick_actions"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                    Spacer(Modifier.height(12.dp))
                    MedAIQuickTileGrid(
                        listOf(
                            MedAIQuickItem(Icons.Default.MonitorHeart, t("quick_symptoms"), c.tintTeal, { onNavigate("symptoms") }),
                            MedAIQuickItem(Icons.Default.Medication, t("quick_meds"), c.tintPeach, { onNavigate("drugs") }),
                            MedAIQuickItem(Icons.Default.Alarm, t("quick_reminders"), c.tintSky, { onNavigate("reminder") }),
                            MedAIQuickItem(Icons.Default.Emergency, t("quick_sos"), MedAITint(c.dangerSoft, c.onDangerSoft), { onNavigate("sos") }, danger = true),
                        )
                    )
                }
            }
        }

        // 5. All other services.
        item {
            Reveal(600) {
                Column {
                    Text(t("home_all_services"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                    Spacer(Modifier.height(12.dp))
                    MedAICard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                        serviceRows.forEachIndexed { index, row ->
                            MedAIListRow(
                                icon = row.icon,
                                title = row.title,
                                subtitle = row.subtitle,
                                tint = row.tint,
                                showDivider = index != serviceRows.lastIndex,
                                onClick = { onNavigate(row.route) },
                            )
                        }
                    }
                }
            }
        }

        // 6. Non-premium upsell.
        if (!isPremium) {
            item {
                Reveal(700) {
                    MedAICard(Modifier.fillMaxWidth(), onClick = { onNavigate("upgrade") }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).background(c.premiumSoft, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = c.premium, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t("upsell_title"), style = MaterialTheme.typography.titleSmall, color = c.premium)
                                Text(t("upsell_desc"), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Score ring, status, optional activity pills and the primary action, on the brand gradient. */
@Composable
private fun HomeHealthHero(
    score: Int,
    steps: Int,
    showActivity: Boolean,
    lang: String,
    ctaTitle: String,
    ctaSubtitle: String,
    onCta: () -> Unit,
) {
    val c = MedAITheme.colors
    fun t(key: String) = Translations.getString(key, lang)
    val progress by animateFloatAsState(
        targetValue = (score / 100f).coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "heroScoreRing"
    )
    val (statusKey, statusTone) = when {
        score == 0 -> "status_start" to MedAIBadgeTone.Info
        score <= 40 -> "status_poor" to MedAIBadgeTone.Danger
        score <= 70 -> "status_average" to MedAIBadgeTone.Warning
        else -> "status_good" to MedAIBadgeTone.Success
    }
    val stepsGoalPercent = ((steps / 10000f).coerceIn(0f, 1f) * 100).toInt()

    MedAIHeroCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(t("home_health_score"), style = MaterialTheme.typography.labelLarge, color = c.onHeroMuted)
                Spacer(Modifier.height(4.dp))
                Text(t("home_subtitle"), style = MaterialTheme.typography.titleMedium, color = c.onHero)
                Spacer(Modifier.height(12.dp))
                MedAIBadge(text = t(statusKey), tone = statusTone)
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "${t("home_health_score")}: $score / 100"
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 10.dp.toPx()
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val topLeft = Offset(stroke / 2, stroke / 2)
                    drawArc(Color.White.copy(alpha = 0.22f), -90f, 360f, false, topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                    if (progress > 0f) {
                        drawArc(Color.White, -90f, 360f * progress, false, topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$score", style = MedAIText.MetricMedium, color = c.onHero)
                    Text("/100", style = MaterialTheme.typography.labelMedium, color = c.onHeroMuted, modifier = Modifier.padding(bottom = 5.dp))
                }
            }
        }
        if (showActivity) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MedAIMetricPill(Icons.Default.DirectionsRun, "$steps", t("home_steps"), Modifier.weight(1f))
                MedAIMetricPill(Icons.Default.Flag, "$stepsGoalPercent%", t("home_goal"), Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(16.dp))
        MedAICard(Modifier.fillMaxWidth(), onClick = onCta, contentPadding = 0.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(c.tintViolet.bg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = c.tintViolet.fg, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(ctaTitle, style = MaterialTheme.typography.titleSmall, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(ctaSubtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = c.brand)
            }
        }
    }
}


@Composable
fun QrCodeCanvas(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val blockSize = sizePx / 17f
        
        val qrMatrix = arrayOf(
            intArrayOf(1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,1),
            intArrayOf(1,0,0,0,0,0,1,0,0,1,1,0,0,0,0,0,1),
            intArrayOf(1,0,1,1,1,0,1,0,1,0,1,0,1,1,1,0,1),
            intArrayOf(1,0,1,1,1,0,1,0,0,0,1,0,1,1,1,0,1),
            intArrayOf(1,0,1,1,1,0,1,0,1,1,1,0,1,1,1,0,1),
            intArrayOf(1,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,1),
            intArrayOf(1,1,1,1,1,1,1,0,1,1,1,0,1,1,1,1,1),
            intArrayOf(0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0),
            intArrayOf(1,1,0,1,0,0,1,1,0,1,1,1,0,0,1,1,1),
            intArrayOf(0,0,1,1,1,0,0,1,1,0,0,1,1,1,0,1,0),
            intArrayOf(1,0,1,0,1,1,1,0,1,1,0,0,0,1,1,0,1),
            intArrayOf(0,0,0,0,0,0,0,0,1,1,1,1,0,1,0,1,1),
            intArrayOf(1,1,1,1,1,1,1,0,0,0,1,0,0,1,0,0,1),
            intArrayOf(1,0,0,0,0,0,1,0,1,0,1,1,1,0,1,1,0),
            intArrayOf(1,0,1,1,1,0,1,0,0,1,1,0,1,0,1,0,1),
            intArrayOf(1,0,0,0,0,0,1,0,1,1,1,0,0,1,1,0,1),
            intArrayOf(1,1,1,1,1,1,1,0,0,1,0,1,1,0,1,1,1)
        )
        
        for (r in qrMatrix.indices) {
            for (c in qrMatrix[r].indices) {
                if (qrMatrix[r][c] == 1) {
                    drawRect(
                        color = color,
                        topLeft = Offset(c * blockSize, r * blockSize),
                        size = androidx.compose.ui.geometry.Size(blockSize + 0.5f, blockSize + 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyQrDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Scan, 1: My QR
    
    var scanSuccess by remember { mutableStateOf(false) }
    var scannedMemberName by remember { mutableStateOf("") }
    var scannedMemberRelation by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    
    var customName by remember { mutableStateOf("") }
    var customRelation by remember { mutableStateOf("Child") }
    var showCustomInputs by remember { mutableStateOf(false) }
    
    val relationsList = listOf("Child", "Spouse", "Father", "Mother", "Brother", "Sister")
    val coroutineScope = rememberCoroutineScope()

    fun getLangText(uz: String, ru: String, en: String): String {
        return when (lang) {
            "uz" -> uz
            "ru" -> ru
            else -> en
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = medai.brand,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = getLangText("QR-Kod orqali ulanish", "Подключение по QR-коду", "QR-Code Connection"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabScanText = getLangText("QR Skanerlash", "Сканировать", "Scan QR")
                    val tabMyQrText = getLangText("Mening QR kodim", "Мой QR-код", "My QR Code")
                    
                    Button(
                        onClick = { selectedTab = 0; scanSuccess = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) medai.brand else Color.Transparent,
                            contentColor = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = if (selectedTab == 0) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(text = tabScanText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) medai.brand else Color.Transparent,
                            contentColor = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = if (selectedTab == 1) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(text = tabMyQrText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (selectedTab == 0) {
                    if (scanSuccess) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(medai.brand.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = medai.brand,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Text(
                                text = getLangText("Muvaffaqiyatli bog'landi!", "Успешно подключено!", "Successfully Linked!"),
                                fontWeight = FontWeight.Bold,
                                color = medai.brand,
                                fontSize = 18.sp
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = scannedMemberName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = getLangText("Qarindoshligi: ", "Родство: ", "Relation: ") + getLangText(
                                            when (scannedMemberRelation) {
                                                "Child" -> "Farzand"
                                                "Spouse" -> "Turmush o'rtog'i"
                                                "Father" -> "Ota"
                                                "Mother" -> "Ona"
                                                "Brother" -> "Aka/Uka"
                                                "Sister" -> "Opa/Singil"
                                                else -> scannedMemberRelation
                                            },
                                            when (scannedMemberRelation) {
                                                "Child" -> "Ребенок"
                                                "Spouse" -> "Супруг(а)"
                                                "Father" -> "Отец"
                                                "Mother" -> "Мать"
                                                "Brother" -> "Брат"
                                                "Sister" -> "Сестра"
                                                else -> scannedMemberRelation
                                            },
                                            scannedMemberRelation
                                        ),
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
                            ) {
                                Text(text = getLangText("Yopish", "Закрыть", "Close"))
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .shadow(6.dp, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF0F2620), Color(0xFF163832))))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val infiniteTransition = rememberInfiniteTransition()
                                val laserOffset by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val bracketLength = 24.dp.toPx()
                                        val strokeWidth = 3.dp.toPx()
                                        val w = size.width
                                        val h = size.height

                                        drawLine(medai.success, Offset(0f, 0f), Offset(bracketLength, 0f), strokeWidth)
                                        drawLine(medai.success, Offset(0f, 0f), Offset(0f, bracketLength), strokeWidth)

                                        drawLine(medai.success, Offset(w, 0f), Offset(w - bracketLength, 0f), strokeWidth)
                                        drawLine(medai.success, Offset(w, 0f), Offset(w, bracketLength), strokeWidth)

                                        drawLine(medai.success, Offset(0f, h), Offset(bracketLength, h), strokeWidth)
                                        drawLine(medai.success, Offset(0f, h), Offset(0f, h - bracketLength), strokeWidth)

                                        drawLine(medai.success, Offset(w, h), Offset(w - bracketLength, h), strokeWidth)
                                        drawLine(medai.success, Offset(w, h), Offset(w, h - bracketLength), strokeWidth)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .offset(y = (laserOffset * 190).dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color.Transparent, medai.success, Color.Transparent)
                                                )
                                            )
                                    )
                                }

                                if (isScanning) {
                                    CircularProgressIndicator(color = medai.success)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.25f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }

                            Text(
                                text = getLangText("Kamera doirangizga QR kodni yo'naltiring", "Направьте камеру на QR-код", "Point camera at the QR code"),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = getLangText("⚠️ Simulyatsiya Rejimi", "⚠️ Режим симуляции", "⚠️ Simulation Mode"),
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { showCustomInputs = !showCustomInputs }) {
                                    Text(
                                        text = if (showCustomInputs) getLangText("Shablonlar", "Шаблоны", "Templates") else getLangText("Boshqa ism", "Другое имя", "Custom Name"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = medai.brand
                                    )
                                }
                            }

                            if (showCustomInputs) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customName,
                                        onValueChange = { customName = it },
                                        label = { Text(getLangText("Ism sharifi", "Имя и фамилия", "Full Name")) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                        singleLine = true
                                    )

                                    var relExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(
                                            onClick = { relExpanded = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = getLangText("Qarindoshligi: ", "Родство: ", "Relation: ") + getLangText(
                                                        when (customRelation) {
                                                            "Child" -> "Farzand"
                                                            "Spouse" -> "Turmush o'rtog'i"
                                                            "Father" -> "Ota"
                                                            "Mother" -> "Ona"
                                                            "Brother" -> "Aka/Uka"
                                                            "Sister" -> "Opa/Singil"
                                                            else -> customRelation
                                                        },
                                                        when (customRelation) {
                                                            "Child" -> "Ребенок"
                                                            "Spouse" -> "Супруг(а)"
                                                            "Father" -> "Отец"
                                                            "Mother" -> "Мать"
                                                            "Brother" -> "Брат"
                                                            "Sister" -> "Сестра"
                                                            else -> customRelation
                                                        },
                                                        customRelation
                                                    ),
                                                    fontSize = 12.sp
                                                )
                                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = relExpanded,
                                            onDismissRequest = { relExpanded = false }
                                        ) {
                                            relationsList.forEach { rel ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(getLangText(
                                                            when (rel) {
                                                                "Child" -> "Farzand (Child)"
                                                                "Spouse" -> "Turmush o'rtog'i (Spouse)"
                                                                "Father" -> "Ota (Father)"
                                                                "Mother" -> "Ona (Mother)"
                                                                "Brother" -> "Aka/Uka (Brother)"
                                                                "Sister" -> "Opa/Singil (Sister)"
                                                                else -> rel
                                                            },
                                                            when (rel) {
                                                                "Child" -> "Ребенок (Child)"
                                                                "Spouse" -> "Супруг(а) (Spouse)"
                                                                "Father" -> "Отец (Father)"
                                                                "Mother" -> "Мать (Mother)"
                                                                "Brother" -> "Брат (Brother)"
                                                                "Sister" -> "Сестра (Sister)"
                                                                else -> rel
                                                            },
                                                            rel
                                                        ))
                                                    },
                                                    onClick = {
                                                        customRelation = rel
                                                        relExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (customName.isNotBlank()) {
                                                isScanning = true
                                                coroutineScope.launch {
                                                    kotlinx.coroutines.delay(1200)
                                                    viewModel.linkFamilyMemberSubAccount(
                                                        name = customName,
                                                        relation = customRelation,
                                                        email = "${customName.lowercase().replace(" ", "")}@medai.uz",
                                                        phone = "+99890" + (1000000..9999999).random()
                                                    )
                                                    scannedMemberName = customName
                                                    scannedMemberRelation = customRelation
                                                    isScanning = false
                                                    scanSuccess = true
                                                }
                                            } else {
                                                Toast.makeText(viewModel.getApplication(), getLangText("Iltimos, ism kiriting", "Пожалуйста, введите имя", "Please enter name"), Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
                                    ) {
                                        Text(text = getLangText("QR skanerlashni simulyatsiya qilish", "Имитировать сканирование QR", "Simulate QR Scan"))
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val presets = listOf(
                                        Triple("Kamola Karimova", "Sister", "kamola@medai.uz"),
                                        Triple("Jasur Karimov", "Child", "jasur@medai.uz"),
                                        Triple("Nilufar Karimova", "Spouse", "nilufar@medai.uz")
                                    )
                                    
                                    presets.forEach { preset ->
                                        OutlinedButton(
                                            onClick = {
                                                if (!isScanning) {
                                                    isScanning = true
                                                    coroutineScope.launch {
                                                        kotlinx.coroutines.delay(1000)
                                                        viewModel.linkFamilyMemberSubAccount(
                                                            name = preset.first,
                                                            relation = preset.second,
                                                            email = preset.third,
                                                            phone = "+99890" + (1000000..9999999).random()
                                                        )
                                                        scannedMemberName = preset.first
                                                        scannedMemberRelation = preset.second
                                                        isScanning = false
                                                        scanSuccess = true
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text("👤", fontSize = 18.sp)
                                                    Column {
                                                        Text(text = preset.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                                        Text(
                                                            text = getLangText(
                                                                when(preset.second) {
                                                                    "Sister" -> "Opa/Singil"
                                                                    "Child" -> "Farzand"
                                                                    "Spouse" -> "Turmush o'rtog'i"
                                                                    else -> preset.second
                                                                },
                                                                when(preset.second) {
                                                                    "Sister" -> "Сестра"
                                                                    "Child" -> "Ребенок"
                                                                    "Spouse" -> "Супруг(а)"
                                                                    else -> preset.second
                                                                },
                                                                preset.second
                                                            ), 
                                                            fontSize = 11.sp, 
                                                            color = Color.Gray
                                                        )
                                                    }
                                                }
                                                Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = medai.brand)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = getLangText("Ushbu QR kodni boshqa oila a'zolaringizga ko'rsating", "Покажите этот QR-код другим членам семьи", "Show this QR code to other family members"),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Card(
                            modifier = Modifier
                                .size(220.dp)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = medai.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                QrCodeCanvas(
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color(0xFF1E1E1E)
                                )
                            }
                        }

                        val localUser by viewModel.currentUser.collectAsState()
                        Card(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("👤", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = localUser?.name ?: "MedAI Foydalanuvchisi",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = localUser?.email ?: "user@medai.uz",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                Toast.makeText(viewModel.getApplication(), getLangText("QR kod rasmi saqlandi!", "Изображение QR-кода сохранено!", "QR Code image saved!"), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(0.9f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
                        ) {
                            Text(text = getLangText("Ulashing / Saqlash", "Поделиться / Сохранить", "Share / Save"))
                        }
                    }
                }
            }
        }
    }
}
