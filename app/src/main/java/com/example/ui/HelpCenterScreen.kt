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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector

// --- SCREEN: HELP & SUPPORT CENTER ---

@Composable
fun HelpCenterScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val config by viewModel.appConfig.collectAsState()
    val context = LocalContext.current
    fun tr(uz: String, ru: String, en: String) = when (lang) { "uz" -> uz; "ru" -> ru; else -> en }

    val faq = listOf(
        Triple(
            tr("MedAI nima?", "Что такое MedAI?", "What is MedAI?"),
            tr(
                "MedAI - bu sun'iy intellektga asoslangan shaxsiy tibbiy maslahatchi va salomatlik tahlilchisidir.",
                "MedAI — персональный медицинский помощник и анализатор здоровья на основе ИИ.",
                "MedAI is a personal AI health assistant and analyser."
            ),
            c.tintTeal
        ),
        Triple(
            tr("Premium reja nima beradi?", "Что даёт тариф Premium?", "What does Premium include?"),
            tr(
                "Premium reja barcha shifokor chatlari, laboratoriya tahlili va oilaviy kuzatuvni faollashtiradi.",
                "Premium открывает все чаты с врачом, анализ лабораторных данных и семейный мониторинг.",
                "Premium unlocks every doctor chat, lab analysis and family monitoring."
            ),
            c.tintViolet
        ),
    )

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("feat_help", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MedAICard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(c.brandSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.HelpCenter, contentDescription = null, tint = c.onBrandSoft, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(tr("Tez-tez so'raladigan savollar", "Часто задаваемые вопросы", "Frequently asked questions"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                        Text(
                            tr(
                                "Javob toping yoki qo'llab-quvvatlash xizmatiga yozing",
                                "Найдите ответ или напишите в поддержку",
                                "Find an answer or message support"
                            ),
                            style = MaterialTheme.typography.bodySmall, color = c.textSecondary
                        )
                    }
                }
            }

            Text("FAQ", style = MedAIText.Eyebrow, color = c.brand)

            MedAICard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                faq.forEachIndexed { index, (q, a, tint) ->
                    Row(Modifier.fillMaxWidth().padding(16.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(tint.bg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = tint.fg, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(q, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(a, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                        }
                    }
                    if (index != faq.lastIndex) Box(Modifier.padding(start = 66.dp).fillMaxWidth().height(1.dp).background(c.divider))
                }
            }

            // Opens the support Telegram account from app config (it was a no-op placeholder before).
            MedAIPrimaryButton(
                text = tr("Telegram orqali bog'lanish", "Связаться через Telegram", "Contact us on Telegram"),
                icon = Icons.Default.Chat,
                onClick = {
                    val handle = (config?.supportTelegram ?: "Medai_support").trim().trimStart('@')
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$handle")))
                    } catch (e: Exception) {
                        Toast.makeText(context, "@$handle", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
