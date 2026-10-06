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

// --- SCREEN: MEDICAL SERVICES & CLINICS FILTER ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(viewModel: AppViewModel, onBack: () -> Unit, onNavigateToUpgrade: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isPremium = user?.hasPremiumAccess ?: false

    val context = LocalContext.current

    var selectedCity by remember { mutableStateOf("Toshkent") }
    var selectedSpecialty by remember { mutableStateOf("Terapevt") }

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("feat_services", lang), onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(medai.canvas)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // 1. City Chips
            item {
                Text(
                    text = "Shahar tanlang:".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = medai.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Toshkent", "Samarqand", "Buxoro", "Namangan").forEach { city ->
                        MedAIChip(
                            text = city,
                            selected = selectedCity == city,
                            onClick = { selectedCity = city }
                        )
                    }
                }
            }

            // 3. Specialty Chips
            item {
                Text(
                    text = "Yo'nalish tanlang:".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = medai.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Terapevt", "Kardiolog", "Pediatr", "Stomatolog", "Klinika").forEach { spec ->
                        MedAIChip(
                            text = spec,
                            selected = selectedSpecialty == spec,
                            onClick = { selectedSpecialty = spec }
                        )
                    }
                }
            }

            // 4. Emergency Call Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = medai.brand, spotColor = medai.brand)
                        .clickable {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:103"))
                            context.startActivity(dialIntent)
                        },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFD32F2F), Color(0xFFC2185B))
                                )
                            )
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneInTalk,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "103 - Tezkor Tibbiy Yordam",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Shoshilinch hollarda 103 xizmati bilan darhol bog'laning",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // 5. Featured clinics header
            item {
                Text(
                    text = "Tavsiya etiladigan shifoxonalar".uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = medai.brand,
                    letterSpacing = 1.2.sp
                )
            }

            // 6. Featured clinic cards
            val clinicsList = listOf(
                Triple("Akfa Medline", "Toshkent, Olmazor tumani, Kichik halqa yo'li", "+998 71 203 30 03"),
                Triple("Shox Med Center", "Toshkent, Yakkasaroy tumani, Shota Rustaveli", "+998 71 202 02 03"),
                Triple("Sog'lom Avlod", "Samarqand, Dahbed ko'chasi, 14", "+998 66 233 00 55"),
                Triple("Buxoro Shifo", "Buxoro, Ibn Sino ko'chasi, 23", "+998 65 224 11 22"),
                Triple("Namangan Shifo Nuri", "Namangan, Nodira ko'chasi, 45", "+998 69 227 88 99")
            ).filter { it.second.contains(selectedCity) }

            if (clinicsList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, medai.border)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Ushbu shahar bo'yicha shifoxona ma'lumotlari topilmadi.",
                                fontSize = 13.sp,
                                color = medai.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(clinicsList) { (name, address, phone) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, medai.border)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(medai.brandSoft, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = medai.brand,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = medai.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = address,
                                    fontSize = 12.sp,
                                    color = medai.textSecondary
                                )
                            }
                            IconButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.replace(" ", "")}"))
                                    context.startActivity(dialIntent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Qo'ng'iroq qilish",
                                    tint = medai.brand,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
