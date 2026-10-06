@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Translations
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.ui.theme.*
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

// --- SCREEN: AI PERSONALIZED DAILY HEALTH TIPS ---

@Composable
fun AITipsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val tips by viewModel.aiTipsText.collectAsState()
    val isLoading by viewModel.isLoadingTips.collectAsState()

    var selectedTab by remember { mutableStateOf("nutrition") }

    LaunchedEffect(key1 = true) {
        if (tips.isEmpty()) {
            viewModel.fetchPersonalizedTips()
        }
    }

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("ai_tips_title", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(medai.canvas)
        ) {
            // personalized user details card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = medai.surface),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, medai.border)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(medai.brand.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = medai.brand, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Shaxsiy tavsiya tahlili", fontWeight = FontWeight.Bold, color = medai.textPrimary, fontSize = 14.sp)
                        Text(text = "AI shaxsiy parametrlaringiz (Bo'y, vazn, jins) asosida maslahat beradi", fontSize = 11.sp, color = medai.textSecondary)
                    }
                }
            }

            // Advice Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "nutrition" to Translations.getString("tab_nutrition", lang),
                    "activity" to Translations.getString("tab_activity", lang),
                    "sleep" to Translations.getString("tab_sleep", lang),
                    "mental" to Translations.getString("tab_mental", lang)
                ).forEach { (code, label) ->
                    val isSelected = selectedTab == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = code },
                        label = { Text(label, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = medai.surface,
                            labelColor = medai.textSecondary,
                            selectedContainerColor = medai.brand,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = medai.border,
                            selectedBorderColor = medai.brand
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = medai.brand)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (lang) {
                                "uz" -> "Maslahat tayyorlanmoqda..."
                                "ru" -> "Готовим совет..."
                                else -> "Preparing your tip..."
                            },
                            fontSize = 12.sp,
                            color = medai.textSecondary
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = medai.brand.copy(alpha = 0.12f), spotColor = medai.brand.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = medai.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, medai.border)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(medai.brand.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (selectedTab) {
                                    "nutrition" -> Icons.Default.Restaurant
                                    "activity" -> Icons.Default.DirectionsRun
                                    "sleep" -> Icons.Default.Bedtime
                                    else -> Icons.Default.SelfImprovement
                                }
                                Icon(imageVector = icon, contentDescription = null, tint = medai.brand, modifier = Modifier.size(32.dp))
                            }

                            val tipContent = tips[selectedTab] ?: "Yuklanmoqda..."
                            Text(
                                text = tipContent,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = medai.textPrimary,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.fetchPersonalizedTips() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = medai.brand, contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = Translations.getString("ai_tips_refresh", lang), fontWeight = FontWeight.Bold)
            }
        }
    }
}
