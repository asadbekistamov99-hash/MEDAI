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

// --- SCREEN: PREMIUM SUBSCRIPTION MANAGEMENT ---

@Composable
fun PremiumUpgradeScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()

    var showCheckInput by remember { mutableStateOf(false) }

    fun getLangText(uz: String, ru: String, en: String): String {
        return when (lang) {
            "uz" -> uz
            "ru" -> ru
            else -> en
        }
    }

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("premium_upgrade_title", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // One premium hero instead of three stacked rows. This is the funnel moment, so
            // the badge, the promise and the pitch belong in a single block of brand colour.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(PremiumGradient)
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = Translations.getString("premium_upgrade_title", lang),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = Translations.getString("premium_upgrade_desc", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center
                )
            }

            // Price tag card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = medai.premium.copy(alpha = 0.3f), spotColor = medai.premium.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = medai.premium)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Translations.getString("premium_price", lang),
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getLangText(
                            "Barcha professional imkoniyatlar to'liq ochiladi",
                            "Все профессиональные функции разблокируются",
                            "Unlock all professional features completely"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Feature Checklist (Premium Advantages)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, medai.border),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = getLangText("Premium Imkoniyatlari:", "Возможности Премиум:", "Premium Features:"),
                        style = MaterialTheme.typography.titleSmall,
                        color = medai.brand
                    )

                    val features = listOf(
                        getLangText("🤖 Cheksiz AI Shifokor bilan suhbat", "🤖 Безлимитный чат с ИИ-Врачом", "🤖 Unlimited AI Doctor Chat"),
                        getLangText("🩺 Cheksiz va chuqur Simptom Tekshiruvi", "🩺 Полная и глубокая диагностика симптомов", "🩺 Complete & Deep Symptom Analysis"),
                        getLangText("📊 Batafsil tahlillar va grafiklar", "📊 Детальная аналитика и графики", "📊 Advanced Health Analytics & Charts"),
                        getLangText("👨‍👩‍👧‍👦 Oilaviy guruh va monitoring", "👨‍👩‍👧‍👦 Семейные группы и мониторинг", "👨‍👩‍👧‍👦 Family Groups & Remote Monitoring"),
                        getLangText("🔔 Cheksiz dori eslatmalari", "🔔 Безлимитные напоминания о приеме лекарств", "🔔 Unlimited Pill Reminders")
                    )

                    features.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = medai.success,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = feat,
                                style = MaterialTheme.typography.bodyMedium,
                                color = medai.textPrimary
                            )
                        }
                    }
                }
            }

            // Pay details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, medai.border),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = medai.brand, modifier = Modifier.size(20.dp))
                        Text(
                            text = getLangText("To'lov Tafsilotlari", "Детали платежа", "Payment Details"),
                            fontWeight = FontWeight.Bold,
                            color = medai.brand,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "Click / Payme karta raqami: 8600 1234 5678 9012",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = medai.textPrimary
                    )
                    Text(
                        text = Translations.getString("premium_pay_details", lang),
                        fontSize = 12.sp,
                        color = medai.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!showCheckInput) {
                MedAIButton(
                    text = Translations.getString("premium_upload_check", lang),
                    onClick = { showCheckInput = true },
                    icon = Icons.Default.ReceiptLong,
                    brush = PremiumGradient
                )
            } else {
                MedAIButton(
                    text = Translations.getString("premium_submit", lang),
                    onClick = {
                        // Simulate check upload
                        viewModel.submitPaymentCheck("simulated_payment_check_base64_receipt")
                        showCheckInput = false
                    },
                    icon = Icons.Default.CheckCircle
                )
            }
        }
    }
}
