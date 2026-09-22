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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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

// --- MAIN CONTAINER WITH CUSTOM PREMIUM BOTTOM BAR ---

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
    var showFamilyQrDialog by remember { mutableStateOf(false) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MedicalBackground,
            bottomBar = {
                MedicalBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onQrClicked = { showFamilyQrDialog = true },
                    lang = lang
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> HomeScreen(viewModel, onNavigateToFeature)
                    1 -> HistoryScreen(viewModel)
                    2 -> GeneralChatScreen(viewModel)
                    3 -> ProfileScreen(viewModel, navController)
                }
            }
        }
    }

    if (showFamilyQrDialog) {
        FamilyQrDialog(viewModel = viewModel, onDismiss = { showFamilyQrDialog = false })
    }
}

@Composable
fun MedicalBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onQrClicked: () -> Unit,
    lang: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .border(1.dp, MedicalBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MedicalBottomTabItem(
                selected = selectedTab == 0,
                icon = Icons.Default.Home,
                label = Translations.getString("tab_home", lang),
                onClick = { onTabSelected(0) }
            )
            MedicalBottomTabItem(
                selected = selectedTab == 1,
                icon = Icons.Default.History,
                label = Translations.getString("tab_history", lang),
                onClick = { onTabSelected(1) }
            )

            // Center QR Code Button (Neon Cyan)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape, ambientColor = PrimaryGreen, spotColor = PrimaryGreen)
                    .background(
                        Brush.verticalGradient(listOf(PrimaryGreen, DarkGreen)),
                        CircleShape
                    )
                    .clickable(onClick = onQrClicked),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "QR Scanner",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            MedicalBottomTabItem(
                selected = selectedTab == 2,
                icon = Icons.Default.Chat,
                label = Translations.getString("tab_chat", lang),
                onClick = { onTabSelected(2) }
            )
            MedicalBottomTabItem(
                selected = selectedTab == 3,
                icon = Icons.Default.Person,
                label = Translations.getString("tab_profile", lang),
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
fun RowScope.MedicalBottomTabItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val animScale by animateFloatAsState(targetValue = if (selected) 1.12f else 1.0f, label = "tabScale")
        
        Box(
            modifier = Modifier
                .size(46.dp, 28.dp)
                .scale(animScale)
                .background(
                    if (selected) PrimaryGreen.copy(alpha = 0.15f) else Color.Transparent,
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) PrimaryGreen else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) PrimaryGreen else TextSecondary
        )
    }
}

// --- HOME SCREEN (FREE & PREMIUM) ---

@Composable
fun HomeScreen(viewModel: AppViewModel, onNavigate: (String) -> Unit) {
    val user by viewModel.currentUser.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    val steps by viewModel.dailySteps.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()

    val isPremium = user?.isPremium ?: false

    // Staggered enter animation for home screen rows
    var animateRows by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateRows = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. App Header: Hamburger, left-aligned brand block with slogan, Bell
        item {
            AnimatedVisibility(
                visible = animateRows,
                enter = slideInVertically(initialOffsetY = { -50 }) + fadeIn(animationSpec = tween(500))
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MedicalBorder, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Medical Hamburger Icon inside styled light button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape)
                                .clickable { onNavigate("services") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Center: MedAI title (left-aligned) with slogan underneath, PREMIUM pill inline
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MedAI",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = PrimaryGreen,
                                    letterSpacing = -0.5.sp
                                )

                                if (isPremium) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                    val shimmerTranslate by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 100f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(2000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "shimmer"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(PremiumPurple, Color(0xFF9333EA), PremiumPurple),
                                                    start = Offset(shimmerTranslate, 0f),
                                                    end = Offset(shimmerTranslate + 40f, 0f)
                                                ),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PREMIUM",
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = Translations.getString("app_slogan", lang),
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isPremium) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Premium",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Right: Bouncing notification bell with red badge inside light circular button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape)
                        ) {
                            val bellTransition = rememberInfiniteTransition(label = "bell")
                            val bellRotation by bellTransition.animateFloat(
                                initialValue = -10f,
                                targetValue = 10f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(400, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "bell"
                            )
                            val rotationModifier = if (unreadNotifications > 0) {
                                Modifier.rotate(bellRotation)
                            } else Modifier

                            IconButton(
                                onClick = { onNavigate("notifications") },
                                modifier = rotationModifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (unreadNotifications > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-2).dp, y = 2.dp)
                                        .size(16.dp)
                                        .background(ErrorRed, CircleShape)
                                        .border(1.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unreadNotifications.toString(),
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Hero Banner: Nature photo background with greeting overlay
        item {
            AnimatedVisibility(
                visible = animateRows,
                enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(animationSpec = tween(600))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = PrimaryGreen.copy(alpha = 0.2f), spotColor = PrimaryGreen.copy(alpha = 0.2f))
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    // Soft nature background photo — kept bright and visible, like the reference design
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.img_nature_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    // Faint white wash only, so dark text stays legible without hiding the photo
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White.copy(alpha = 0.12f))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = String.format(Translations.getString("home_greeting", lang), user?.name ?: ""),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = -0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bugun o'zingizni qanday his qilyapsiz?",
                                fontSize = 13.sp,
                                color = TextPrimary.copy(alpha = 0.75f)
                            )
                        }

                        // Right side: Doctor / Medical icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White.copy(alpha = 0.75f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPremium) Icons.Default.MilitaryTech else Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = if (isPremium) PremiumPurple else PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Health Score / Premium Dashboard Card
        item {
            AnimatedVisibility(
                visible = animateRows,
                enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(animationSpec = tween(700))
            ) {
                if (isPremium) {
                    PremiumHealthCard(steps = steps, user = user, lang = lang)
                } else {
                    FreeHealthCard(user = user, lang = lang)
                }
            }
        }

        // 4. Feature Grid Title
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clickable { onNavigate("yordamchi") },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(PrimaryGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MedAI Yordamchi (4-in-1)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Dori aniqlash, Simptom, Statistika & Reminder",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = PrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "MEDAI xizmatlari".uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = PrimaryGreen,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
        }

        // 5. Feature Grid Content
        item {
            // Features configuration
            val freeFeatures = listOf(
                FeatureItem("yordamchi", "MedAI Yordamchi", "Tezkor 4-in-1 yordam", Icons.Default.SmartToy, Brush.horizontalGradient(colors = listOf(PrimaryGreen, DarkGreen))),
                FeatureItem("family", Translations.getString("feat_family", lang), "Oila a'zolari", Icons.Default.Group, Brush.horizontalGradient(colors = listOf(Color(0xFFE65100), Color(0xFFFF6D00)))),
                FeatureItem("symptoms", Translations.getString("feat_symptoms", lang), "Tahlil qilish", Icons.Default.Favorite, Brush.horizontalGradient(colors = listOf(Color(0xFF00897B), Color(0xFF00ACC1)))),
                FeatureItem("drugs", Translations.getString("feat_med_info", lang), "Tarkibi va foydasi", Icons.Default.LocalPharmacy, Brush.horizontalGradient(colors = listOf(Color(0xFF1565C0), Color(0xFF1976D2)))),
                FeatureItem("reminder", Translations.getString("feat_reminder", lang), "O'z vaqtida ichish", Icons.Default.Alarm, Brush.horizontalGradient(colors = listOf(Color(0xFFE65100), Color(0xFFF57C00)))),
                FeatureItem("notifications", Translations.getString("feat_notifications", lang), "Ogohlantirishlar", Icons.Default.NotificationsActive, Brush.horizontalGradient(colors = listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA)))),
                FeatureItem("analytics", Translations.getString("feat_analytics", lang), "Sog'liq ko'rsatkichlari", Icons.Default.BarChart, Brush.horizontalGradient(colors = listOf(Color(0xFF00838F), Color(0xFF00ACC1)))),
                FeatureItem("sos", "Tez yordam SOS", "Favqulodda yordam", Icons.Default.Emergency, Brush.horizontalGradient(colors = listOf(Color(0xFFB71C1C), Color(0xFFE53935))))
            )

            val premiumFeatures = listOf(
                FeatureItem("yordamchi", "MedAI Yordamchi", "Smart 4-in-1 yordam", Icons.Default.SmartToy, Brush.horizontalGradient(colors = listOf(PrimaryGreen, DarkGreen))),
                FeatureItem("family", Translations.getString("feat_family", lang), "Oila a'zolari", Icons.Default.Group, Brush.horizontalGradient(colors = listOf(Color(0xFFE65100), Color(0xFFFF6D00))))
            ) + freeFeatures.drop(2).take(1) + listOf(
                FeatureItem("ai_doctor", Translations.getString("feat_ai_doctor", lang), "AI Robot-Shifokor", Icons.Default.SmartToy, Brush.horizontalGradient(colors = listOf(Color(0xFF4527A0), Color(0xFF5E35B1)))),
                FeatureItem("ai_tips", Translations.getString("feat_ai_tips", lang), "Aqlli maslahatlar", Icons.Default.TipsAndUpdates, Brush.horizontalGradient(colors = listOf(Color(0xFF0277BD), Color(0xFF0288D1)))),
                FeatureItem("drugs", Translations.getString("feat_med_info", lang), "Dori vositalari", Icons.Default.LocalPharmacy, Brush.horizontalGradient(colors = listOf(Color(0xFFE65100), Color(0xFFF4511E))))
            ) + listOf(
                FeatureItem("lab", Translations.getString("feat_lab", lang), "Retsept tahlil qilish", Icons.Default.Science, Brush.horizontalGradient(colors = listOf(Color(0xFF00695C), Color(0xFF00897B)))),
                FeatureItem("reminder", Translations.getString("feat_reminder", lang), "Dori eslatmalari", Icons.Default.Alarm, Brush.horizontalGradient(colors = listOf(Color(0xFFAD1457), Color(0xFFD81B60)))),
                FeatureItem("analytics", Translations.getString("feat_analytics", lang), "Grafik ko'rsatkichlar", Icons.Default.BarChart, Brush.horizontalGradient(colors = listOf(Color(0xFF283593), Color(0xFF3949AB)))),
                FeatureItem("notifications", Translations.getString("feat_notifications", lang), "Ogohlantirishlar", Icons.Default.NotificationsActive, Brush.horizontalGradient(colors = listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA))))
            ) + listOf(
                FeatureItem("sos", "Favqulodda vaziyat", "SOS tezkor yordam", Icons.Default.Emergency, Brush.horizontalGradient(colors = listOf(Color(0xFFC62828), Color(0xFFD32F2F)))),
                FeatureItem("services", Translations.getString("feat_services", lang), "Klinika xizmatlari", Icons.Default.LocalHospital, Brush.horizontalGradient(colors = listOf(Color(0xFF006064), Color(0xFF00838F))))
            )

            val activeFeatures = if (isPremium) premiumFeatures else freeFeatures
            val columns = 2

            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                activeFeatures.chunked(columns).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { item ->
                            HomeFeatureGridCard(
                                title = item.title,
                                subtitle = item.subtitle,
                                icon = item.icon,
                                brush = item.brush,
                                isEmergency = item.id == "sos",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(item.id) }
                            )
                        }
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 6. Quick access: Help Center row
        item {
            AnimatedVisibility(
                visible = animateRows,
                enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(animationSpec = tween(900))
            ) {
                MedicalCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    onClick = { onNavigate("help") }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(LightGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = "Yordam Markazi",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Yordam Markazi",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Savollar va qo'llab-quvvatlash",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        // 7. Non-premium Upsell Banner
        if (!isPremium) {
            item {
                AnimatedVisibility(
                    visible = animateRows,
                    enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(animationSpec = tween(1000))
                ) {
                    MedicalCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        onClick = { onNavigate("upgrade") }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(PremiumLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = "Upgrade",
                                    tint = PremiumPurple,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Premium versiyaga o'ting!",
                                    fontWeight = FontWeight.Bold,
                                    color = PremiumPurple,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "AI Shifokor, Analizlarni skanerlash, Oila monitoringi va barcha xizmatlarni oching.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS FOR HOME SCREEN ---

data class FeatureItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val brush: Brush
)

@Composable
fun FreeHealthCard(user: UserLocal?, lang: String) {
    val score = user?.healthScore ?: 0

    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "freeHealthProgress"
    )

    MedicalCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        borderStroke = BorderStroke(1.5.dp, Color(0xFFE8F5F3))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Subtle background ECG wave line decoration
                    val width = size.width
                    val height = size.height
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(0f, height * 0.7f)
                    path.lineTo(width * 0.4f, height * 0.7f)
                    path.lineTo(width * 0.45f, height * 0.3f)
                    path.lineTo(width * 0.5f, height * 0.9f)
                    path.lineTo(width * 0.55f, height * 0.5f)
                    path.lineTo(width * 0.6f, height * 0.7f)
                    path.lineTo(width, height * 0.7f)

                    drawPath(
                        path = path,
                        color = PrimaryGreen.copy(alpha = 0.04f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Animated beating heart icon
                val infiniteTransition = rememberInfiniteTransition(label = "heart")
                val heartScale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "heartScale"
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(LightGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Sog'liq Balli",
                        tint = PrimaryGreen,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                scaleX = heartScale
                                scaleY = heartScale
                            }
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "SOG'LIQ BALLI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$score / 100",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val (statusText, statusColor) = when {
                        score == 0 -> "Boshlang'ich (0 ball)" to TextSecondary
                        score <= 40 -> Translations.getString("status_poor", lang) to ErrorRed
                        score <= 70 -> Translations.getString("status_average", lang) to WarningOrange
                        else -> Translations.getString("status_good", lang) to SuccessGreen
                    }
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            // Right: thick progress ring
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = PrimaryGreen,
                    strokeWidth = 7.dp,
                    trackColor = Color.LightGray.copy(alpha = 0.2f)
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun PremiumHealthCard(steps: Int, user: UserLocal?, lang: String) {
    val score = user?.healthScore ?: 0

    MedicalCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: VIP Pill and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(PremiumPurple.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .border(1.dp, PremiumPurple.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = PremiumPurple,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PREMIUM VIP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PremiumPurple,
                        letterSpacing = 0.8.sp
                    )
                }

                val (statusText, statusColor) = when {
                    score == 0 -> "Boshlang'ich (0 ball)" to TextSecondary
                    score <= 40 -> Translations.getString("status_poor", lang) to ErrorRed
                    score <= 70 -> Translations.getString("status_average", lang) to WarningOrange
                    else -> Translations.getString("status_good", lang) to SuccessGreen
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Metrics
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(PrimaryGreen.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SOG'LIQ DARAJASI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$score / 100",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(WarningOrange.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KUNDALIK FAOLLIK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$steps qadam",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Steps Progress ring
                val stepsProgress = (steps / 10000f).coerceIn(0f, 1f)
                val animatedStepsProgress by animateFloatAsState(
                    targetValue = stepsProgress,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing),
                    label = "premiumStepsRing"
                )
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { animatedStepsProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = PrimaryGreen,
                        strokeWidth = 7.dp,
                        trackColor = MedicalBorder.copy(alpha = 0.6f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${(animatedStepsProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeFeatureGridCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    brush: Brush,
    isEmergency: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1.0f, label = "cardScale")

    // Pulsing outline for SOS card
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_outline")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val cardBorder = if (isEmergency) {
        BorderStroke(2.dp, Color.Red.copy(alpha = pulseAlpha))
    } else null

    Card(
        modifier = modifier
            .padding(6.dp)
            .height(115.dp)
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = cardBorder ?: BorderStroke(1.dp, MedicalBorder)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Icon Container on Left, Arrow on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(brush, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Top Right: Neon Cyan Arrow
                    Icon(
                        imageVector = Icons.Default.ArrowOutward,
                        contentDescription = "Batafsil",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Text details
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        lineHeight = 18.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        lineHeight = 15.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Full-color gradient tile used for the Premium feature grid
@Composable
fun HomeFeatureGridCardColored(
    title: String,
    subtitle: String,
    icon: ImageVector,
    brush: Brush,
    isEmergency: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1.0f, label = "coloredCardScale")

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_outline_colored")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha_colored"
    )
    val cardBorder = if (isEmergency) BorderStroke(2.dp, Color.White.copy(alpha = pulseAlpha)) else null

    Box(
        modifier = modifier
            .padding(6.dp)
            .height(118.dp)
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
            .then(if (cardBorder != null) Modifier.border(cardBorder.width, cardBorder.brush, RoundedCornerShape(20.dp)) else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowOutward,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis
                )
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
                            tint = PrimaryGreen,
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
                            containerColor = if (selectedTab == 0) PrimaryGreen else Color.Transparent,
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
                            containerColor = if (selectedTab == 1) PrimaryGreen else Color.Transparent,
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
                                    .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Text(
                                text = getLangText("Muvaffaqiyatli bog'landi!", "Успешно подключено!", "Successfully Linked!"),
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
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
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
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

                                        drawLine(SuccessGreen, Offset(0f, 0f), Offset(bracketLength, 0f), strokeWidth)
                                        drawLine(SuccessGreen, Offset(0f, 0f), Offset(0f, bracketLength), strokeWidth)

                                        drawLine(SuccessGreen, Offset(w, 0f), Offset(w - bracketLength, 0f), strokeWidth)
                                        drawLine(SuccessGreen, Offset(w, 0f), Offset(w, bracketLength), strokeWidth)

                                        drawLine(SuccessGreen, Offset(0f, h), Offset(bracketLength, h), strokeWidth)
                                        drawLine(SuccessGreen, Offset(0f, h), Offset(0f, h - bracketLength), strokeWidth)

                                        drawLine(SuccessGreen, Offset(w, h), Offset(w - bracketLength, h), strokeWidth)
                                        drawLine(SuccessGreen, Offset(w, h), Offset(w, h - bracketLength), strokeWidth)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .offset(y = (laserOffset * 190).dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color.Transparent, SuccessGreen, Color.Transparent)
                                                )
                                            )
                                    )
                                }

                                if (isScanning) {
                                    CircularProgressIndicator(color = SuccessGreen)
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
                                        color = PrimaryGreen
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
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
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
                                                Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = PrimaryGreen)
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
                            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text(text = getLangText("Ulashing / Saqlash", "Поделиться / Сохранить", "Share / Save"))
                        }
                    }
                }
            }
        }
    }
}
