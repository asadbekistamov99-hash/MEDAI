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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.*
import com.example.i18n.Translations
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

// --- SCREEN: PREMIUM SUBSCRIPTION ---

@Composable
fun PremiumUpgradeScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val context = LocalContext.current

    var showCheckInput by remember { mutableStateOf(false) }

    fun getLangText(uz: String, ru: String, en: String): String {
        return when (lang) {
            "uz" -> uz
            "ru" -> ru
            else -> en
        }
    }

    // Paid state is the only place violet is used; the whole screen is built on it.
    val featureRows = listOf(
        Triple(Icons.Default.SmartToy, getLangText("Cheksiz AI shifokor suhbati", "Безлимитный чат с AI-врачом", "Unlimited AI doctor chat"), c.tintViolet),
        Triple(Icons.Default.MonitorHeart, getLangText("Chuqur simptom tekshiruvi", "Глубокий анализ симптомов", "Deep symptom analysis"), c.tintTeal),
        Triple(Icons.Default.BarChart, getLangText("Batafsil tahlillar va grafiklar", "Подробная аналитика и графики", "Advanced analytics and charts"), c.tintSky),
        Triple(Icons.Default.FamilyRestroom, getLangText("Oilaviy guruh va monitoring", "Семейные группы и мониторинг", "Family groups and monitoring"), c.tintPeach),
        Triple(Icons.Default.NotificationsActive, getLangText("Cheksiz dori eslatmalari", "Безлимитные напоминания о лекарствах", "Unlimited medicine reminders"), c.tintViolet),
    )
    val cardNumber = "8600 1234 5678 9012"

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("premium_upgrade_title", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Promise + price in one soft violet block.
            val heroShape = RoundedCornerShape(MedAICorners.hero)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(heroShape)
                    .background(c.premiumSoft)
                    .border(1.dp, c.premium.copy(alpha = 0.3f), heroShape)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(c.premium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = c.onPremium, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = Translations.getString("premium_upgrade_title", lang),
                    style = MaterialTheme.typography.headlineMedium,
                    color = c.onPremiumSoft,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = Translations.getString("premium_upgrade_desc", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onPremiumSoft,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = Translations.getString("premium_price", lang),
                    style = MaterialTheme.typography.headlineLarge,
                    color = c.onPremiumSoft,
                    textAlign = TextAlign.Center
                )
            }

            // Advantages
            Column {
                Text(
                    getLangText("Premium imkoniyatlari", "Возможности Premium", "Premium features"),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary
                )
                Spacer(Modifier.height(12.dp))
                MedAICard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                    featureRows.forEachIndexed { index, (icon, text, tint) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tint.bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(text, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = c.success, modifier = Modifier.size(20.dp))
                        }
                        if (index != featureRows.lastIndex) {
                            Box(Modifier.padding(start = 70.dp).fillMaxWidth().height(1.dp).background(c.divider))
                        }
                    }
                }
            }

            // Payment details
            MedAICard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(c.tintTeal.bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = c.tintTeal.fg, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        getLangText("To'lov tafsilotlari", "Детали платежа", "Payment details"),
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    getLangText("Click / Payme karta raqami", "Номер карты Click / Payme", "Click / Payme card number"),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(cardNumber, style = MaterialTheme.typography.titleLarge, color = c.textPrimary, modifier = Modifier.weight(1f))
                    // Copy the number (48dp target).
                    Box(
                        modifier = Modifier
                            .size(MinTouch)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) {
                                val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("card", cardNumber.replace(" ", "")))
                                Toast.makeText(context, getLangText("Nusxalandi", "Скопировано", "Copied"), Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = getLangText("Karta raqamini nusxalash", "Скопировать номер карты", "Copy card number"),
                            tint = c.brand
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = Translations.getString("premium_pay_details", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary
                )
            }

            if (!showCheckInput) {
                MedAIPremiumButton(
                    text = Translations.getString("premium_upload_check", lang),
                    icon = Icons.Default.ReceiptLong,
                    onClick = { showCheckInput = true },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                MedAIPrimaryButton(
                    text = Translations.getString("premium_submit", lang),
                    onClick = {
                        // Simulate check upload
                        viewModel.submitPaymentCheck("simulated_payment_check_base64_receipt")
                        showCheckInput = false
                    },
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
