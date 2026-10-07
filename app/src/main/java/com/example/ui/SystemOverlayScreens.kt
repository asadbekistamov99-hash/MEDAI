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

// --- SCREEN: BANNED OVERLAY SCREEN ---

@Composable
fun BannedScreen(viewModel: AppViewModel) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.canvas)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(c.dangerSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Block, contentDescription = null, tint = c.onDangerSoft, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(Translations.getString("banned_title", lang), style = MaterialTheme.typography.headlineMedium, color = c.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(Translations.getString("banned_desc", lang), style = MaterialTheme.typography.bodyLarge, color = c.textSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        MedAIPrimaryButton(
            text = Translations.getString("sign_out", lang),
            onClick = { viewModel.logout() },
            modifier = Modifier.widthIn(min = 220.dp)
        )
    }
}

// --- SCREEN: MAINTENANCE OVERLAY SCREEN ---

@Composable
fun MaintenanceScreen() {
    val c = MedAITheme.colors
    // This overlay has no language source of its own; follow the device language.
    val lang = Locale.getDefault().language
    fun tr(uz: String, ru: String, en: String) = when (lang) { "uz" -> uz; "ru" -> ru; else -> en }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.canvas)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(c.warningSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Construction, contentDescription = null, tint = c.onWarningSoft, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            tr("Texnik ishlar olib borilmoqda", "Ведутся технические работы", "Scheduled maintenance"),
            style = MaterialTheme.typography.headlineMedium, color = c.textPrimary, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            tr(
                "MedAI tizimi yangilanmoqda. Iltimos, birozdan so'ng qayta urinib ko'ring.",
                "MedAI обновляется. Пожалуйста, попробуйте позже.",
                "MedAI is being updated. Please try again shortly."
            ),
            style = MaterialTheme.typography.bodyLarge, color = c.textSecondary, textAlign = TextAlign.Center
        )
    }
}
