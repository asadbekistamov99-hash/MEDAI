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

// --- SCREEN: NOTIFICATIONS ---

@Composable
fun NotificationsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val list by viewModel.notifications.collectAsState()
    fun tr(uz: String, ru: String, en: String) = when (lang) { "uz" -> uz; "ru" -> ru; else -> en }

    LaunchedEffect(key1 = true) {
        viewModel.markAllNotificationsAsRead()
    }

    val timeFormat = remember { SimpleDateFormat("dd.MM  HH:mm", Locale.getDefault()) }

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("feat_notifications", lang), onBack = onBack) }
    ) { innerPadding ->
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                MedAIEmptyState(
                    title = tr("Hozircha bildirishnomalar yo'q", "Пока нет уведомлений", "No notifications yet"),
                    message = tr("Yangi bildirishnomalar shu yerda paydo bo'ladi", "Новые уведомления появятся здесь", "New notifications will appear here"),
                    icon = Icons.Default.NotificationsNone
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list, key = { it.id }) { item ->
                    // Type decides the icon disc; SOS is the only red one.
                    val (icon, tint) = when (item.type) {
                        "sos" -> Icons.Default.Warning to MedAITint(c.dangerSoft, c.onDangerSoft)
                        "reminder" -> Icons.Default.Alarm to c.tintSky
                        "premium" -> Icons.Default.WorkspacePremium to c.tintViolet
                        "invite" -> Icons.Default.PersonAdd to c.tintPeach
                        else -> Icons.Default.Notifications to c.tintTeal
                    }
                    MedAICard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(CircleShape).background(tint.bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                Spacer(Modifier.height(2.dp))
                                Text(item.message, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                                Spacer(Modifier.height(6.dp))
                                Text(timeFormat.format(Date(item.timestamp)), style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
