@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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

// --- SCREEN: MEDICAL SERVICES & CLINICS ---

@Composable
fun ServicesScreen(viewModel: AppViewModel, onBack: () -> Unit, onNavigateToUpgrade: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isPremium = user?.hasPremiumAccess ?: false

    val context = LocalContext.current
    fun tr(uz: String, ru: String, en: String) = when (lang) { "uz" -> uz; "ru" -> ru; else -> en }

    // The stored values stay Uzbek (the clinic list below is matched against them); only labels change.
    var selectedCity by remember { mutableStateOf("Toshkent") }
    var selectedSpecialty by remember { mutableStateOf("Terapevt") }

    val cities = listOf(
        "Toshkent" to tr("Toshkent", "Ташкент", "Tashkent"),
        "Samarqand" to tr("Samarqand", "Самарканд", "Samarkand"),
        "Buxoro" to tr("Buxoro", "Бухара", "Bukhara"),
        "Namangan" to tr("Namangan", "Наманган", "Namangan"),
    )
    val specialties = listOf(
        "Terapevt" to tr("Terapevt", "Терапевт", "General practitioner"),
        "Kardiolog" to tr("Kardiolog", "Кардиолог", "Cardiologist"),
        "Pediatr" to tr("Pediatr", "Педиатр", "Paediatrician"),
        "Stomatolog" to tr("Stomatolog", "Стоматолог", "Dentist"),
        "Klinika" to tr("Klinika", "Клиника", "Clinic"),
    )

    val clinicsList = remember(selectedCity) {
        listOf(
            Triple("Akfa Medline", "Toshkent, Olmazor tumani, Kichik halqa yo'li", "+998 71 203 30 03"),
            Triple("Shox Med Center", "Toshkent, Yakkasaroy tumani, Shota Rustaveli", "+998 71 202 02 03"),
            Triple("Sog'lom Avlod", "Samarqand, Dahbed ko'chasi, 14", "+998 66 233 00 55"),
            Triple("Buxoro Shifo", "Buxoro, Ibn Sino ko'chasi, 23", "+998 65 224 11 22"),
            Triple("Namangan Shifo Nuri", "Namangan, Nodira ko'chasi, 45", "+998 69 227 88 99")
        ).filter { it.second.contains(selectedCity) }
    }

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("feat_services", lang), onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
        ) {
            // Emergency call first: it is the one thing that must never be hard to find.
            item(key = "emergency") {
                val shape = RoundedCornerShape(MedAICorners.card)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .clip(shape)
                        .background(c.danger)
                        .clickable(role = Role.Button) {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:103")))
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = c.onDanger, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            tr("103 - Tez tibbiy yordam", "103 — скорая помощь", "103 - Emergency medical help"),
                            style = MaterialTheme.typography.titleSmall, color = c.onDanger
                        )
                        Text(
                            tr(
                                "Shoshilinch holatda darhol qo'ng'iroq qiling",
                                "В экстренной ситуации звоните сразу",
                                "In an emergency, call right away"
                            ),
                            style = MaterialTheme.typography.bodySmall, color = c.onDanger
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = c.onDanger)
                }
            }

            item(key = "city") {
                Column {
                    Text(tr("Shahar", "Город", "City"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        cities.forEach { (value, label) ->
                            MedAIFilterChip(label, selectedCity == value, { selectedCity = value })
                        }
                    }
                }
            }

            item(key = "specialty") {
                Column {
                    Text(tr("Yo'nalish", "Специальность", "Specialty"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        specialties.forEach { (value, label) ->
                            MedAIFilterChip(label, selectedSpecialty == value, { selectedSpecialty = value })
                        }
                    }
                }
            }

            item(key = "clinics-title") {
                Text(tr("Tavsiya etiladigan shifoxonalar", "Рекомендуемые клиники", "Recommended clinics"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
            }

            if (clinicsList.isEmpty()) {
                item(key = "empty") {
                    MedAICard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                        MedAIEmptyState(
                            title = tr(
                                "Bu shahar bo'yicha ma'lumot topilmadi",
                                "По этому городу данных нет",
                                "No clinics found for this city"
                            ),
                            icon = Icons.Default.LocalHospital
                        )
                    }
                }
            } else {
                items(clinicsList, key = { it.first }) { (name, address, phone) ->
                    MedAICard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(46.dp).clip(CircleShape).background(c.tintTeal.bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = c.tintTeal.fg, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                Text(address, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                Text(phone, style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .size(MinTouch)
                                    .clip(CircleShape)
                                    .background(c.brandSoft)
                                    .clickable(role = Role.Button) {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.replace(" ", "")}")))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "${tr("Qo'ng'iroq qilish", "Позвонить", "Call")}: $name",
                                    tint = c.onBrandSoft
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
