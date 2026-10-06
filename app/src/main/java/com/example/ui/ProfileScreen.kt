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

// --- SCREEN: PROFILE AND SETTINGS ---

@Composable
fun ProfileScreen(viewModel: AppViewModel, navController: NavController) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isSuperAdmin = user?.email?.trim()?.equals(SUPER_ADMIN_EMAIL, ignoreCase = true) == true
    val medicalDocs by viewModel.medicalDocuments.collectAsState()
    val context = LocalContext.current

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Allergy Manager state
    var allergyName by remember { mutableStateOf("") }
    var allergyTypeSelected by remember { mutableStateOf("Dori") }
    val allergyTypesList = listOf("Dori", "Taom", "Boshqa")

    // Document Uploader state
    var showDocUploadDialog by remember { mutableStateOf(false) }
    var docTitle by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("Analiz") }
    val docTypesList = listOf("Analiz", "Rentgen", "Retsept")

    val mockDocBase64 = "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA="

    val unlockedAchievementsSet = remember(user) {
        val set = mutableSetOf<String>()
        user?.let { u ->
            try {
                val arr = org.json.JSONArray(u.unlockedAchievementsJson)
                for (i in 0 until arr.length()) {
                    set.add(arr.getString(i))
                }
            } catch(e: Exception) {}
        }
        set
    }

    val allergiesList = remember(user) {
        val list = mutableListOf<Pair<String, String>>()
        user?.let { u ->
            try {
                val arr = org.json.JSONArray(u.allergiesJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Pair(obj.getString("name"), obj.getString("type")))
                }
            } catch(e: Exception) {}
        }
        list
    }

    val achievementsList = listOf(
        Triple("first_reminder", "Birinchi Qadam 💊", "Birinchi bor dorini o'z vaqtida qabul qildingiz"),
        Triple("water_champ", "Suv Qiroli 🥤", "Bugun suv ichish maqsadiga to'liq erishdingiz"),
        Triple("ai_pioneer", "AI Katta Shifokor 🧠", "Semptomlarni sun'iy intellekt orqali muvaffaqiyatli tahlil qildingiz")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            val isPremium = user?.hasPremiumAccess ?: false
            val heroBrush = if (isPremium) {
                Brush.linearGradient(listOf(medai.premium, Color(0xFF4C1D95)))
            } else {
                Brush.linearGradient(listOf(medai.brand, medai.brandStrong))
            }

            // Hero card: gradient background + soft decorative circles, matching the richer
            // treatment used on the home screen banner instead of a bare white header.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(heroBrush)
            ) {
                // Decorative background circles
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .offset(x = 220.dp, y = (-70).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .offset(x = (-40).dp, y = 130.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.07f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROFIL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { navController.navigate("notifications") },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Sozlamalar",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .shadow(6.dp, CircleShape)
                            .background(medai.surface, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (isPremium) medai.premium.copy(alpha = 0.12f) else medai.brand.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val initialChar = user?.name?.firstOrNull()?.toString()?.uppercase() ?: "U"
                            Text(
                                text = initialChar,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) medai.premium else medai.brand,
                                    fontSize = 38.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = user?.name ?: "Foydalanuvchi",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 22.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = user?.email ?: "email@example.com",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isPremium) Icons.Default.WorkspacePremium else Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isPremium) "PREMIUM FOYDALANUVCHI" else "BEPUL REJIM",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Super Admin Special Badge and Access Card (strictly and exclusively for SUPER_ADMIN_EMAIL)
            // Uses a deep-teal brand gradient with a gold accent instead of pure black/orange,
            // so it reads as "elevated" rather than clashing with the rest of the palette.
            if (isSuperAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF012A24), medai.brandStrong)))
                        .border(1.dp, Color(0xFFFFC978).copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .clickable { navController.navigate("admin") }
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .offset(x = 260.dp, y = (-40).dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFC978).copy(alpha = 0.08f))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFFFFC978).copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = Color(0xFFFFC978),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Admin Boshqaruv Paneli",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFC978), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "SUPER",
                                        color = Color(0xFF012A24),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Text(
                                text = "Faqat $SUPER_ADMIN_EMAIL uchun ruxsat berilgan",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFFFC978),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Streak Board — warm gradient card with a decorative watermark flame, not flat white
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFF57C00), Color(0xFFFFA726))))
            ) {
                Text(
                    text = "🔥",
                    fontSize = 90.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 26.dp, y = 6.dp)
                        .alpha(0.16f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color.White.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔥", fontSize = 26.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${user?.healthScore?.let { (it / 15).coerceAtLeast(1) } ?: 5} Kunlik Salomatlik Seriyasi!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Har kuni ilovaga kirib salomatligingizni nazorat qiling va seriyani davom ettiring!",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Stats summary row — soft gradient-tinted cards instead of flat white
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(3.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(medai.brandSoft, Color.White)))
                        .border(1.dp, medai.brand.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(medai.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Height, contentDescription = null, tint = medai.brand, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Bo'y", fontSize = 12.sp, color = medai.textSecondary, fontWeight = FontWeight.Medium)
                            Text(text = "${user?.height ?: 175.0} sm", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = medai.textPrimary)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(3.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(medai.brandSoft, Color.White)))
                        .border(1.dp, medai.brand.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(medai.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.MonitorWeight, contentDescription = null, tint = medai.brand, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Vazn", fontSize = 12.sp, color = medai.textSecondary, fontWeight = FontWeight.Medium)
                            Text(text = "${user?.weight ?: 70.0} kg", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = medai.textPrimary)
                        }
                    }
                }
            }
        }

        // Achievements Board
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, medai.border.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = medai.brand)
                        Text(text = "Mening Yutuqlarim (Achievements)", fontWeight = FontWeight.Bold, color = medai.textPrimary, fontSize = 15.sp)
                    }
                    
                    Divider(color = medai.border.copy(alpha = 0.3f))
                    
                    achievementsList.forEach { ach ->
                        val isUnlocked = unlockedAchievementsSet.contains(ach.first)
                        val rowBg = if (isUnlocked) medai.brandSoft.copy(alpha = 0.3f) else Color.Transparent
                        val rowBorder = if (isUnlocked) BorderStroke(1.dp, medai.brand.copy(alpha = 0.2f)) else null
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(rowBg)
                                .then(if (rowBorder != null) Modifier.border(rowBorder, RoundedCornerShape(12.dp)) else Modifier)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(if (isUnlocked) medai.brand.copy(alpha = 0.2f) else medai.textSecondary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = ach.second.takeLast(2), fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ach.second.dropLast(2).trim(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isUnlocked) medai.textPrimary else medai.textSecondary
                                )
                                Text(
                                    text = ach.third,
                                    fontSize = 11.sp,
                                    color = if (isUnlocked) medai.textSecondary else medai.textSecondary.copy(alpha = 0.7f),
                                    lineHeight = 14.sp
                                )
                            }
                            if (isUnlocked) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = medai.success)
                            } else {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = medai.textSecondary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Allergy CRUD Management
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = medai.surface),
                border = BorderStroke(1.dp, medai.danger.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = medai.danger)
                        Text(text = "Mening Allergiyalarim ⚠️", fontWeight = FontWeight.Bold, color = medai.danger, fontSize = 15.sp)
                    }

                    // Allergy List
                    if (allergiesList.isEmpty()) {
                        Text(
                            text = "Allergiyalar kiritilmagan. Quyida yangi allergiya qo'shishingiz mumkin.",
                            fontSize = 12.sp,
                            color = medai.textSecondary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allergiesList.forEach { (name, type) ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text("$name ($type)", color = medai.textPrimary, fontWeight = FontWeight.Medium) },
                                    leadingIcon = {
                                        Box(modifier = Modifier.size(8.dp).background(medai.danger, CircleShape))
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { viewModel.deleteAllergy(name) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete",
                                                tint = medai.danger,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Divider(color = medai.border.copy(alpha = 0.2f))

                    // Add Allergy Form
                    Text(text = "Yangi Allergiya Qo'shish", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = medai.textPrimary)
                    
                    OutlinedTextField(
                        value = allergyName,
                        onValueChange = { allergyName = it },
                        placeholder = { Text("Allergen nomi (masalan: Penitsillin, Yong'oq)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = medai.danger,
                            unfocusedBorderColor = medai.border,
                            focusedTextColor = medai.textPrimary,
                            unfocusedTextColor = medai.textPrimary,
                            focusedContainerColor = medai.surface,
                            unfocusedContainerColor = medai.surface
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allergyTypesList.forEach { type ->
                                FilterChip(
                                    selected = allergyTypeSelected == type,
                                    onClick = { allergyTypeSelected = type },
                                    label = { Text(type, fontSize = 12.sp) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (allergyName.isNotBlank()) {
                                    viewModel.addAllergy(allergyName, allergyTypeSelected)
                                    allergyName = ""
                                    Toast.makeText(viewModel.getApplication(), "Allergiya muvaffaqiyatli qo'shildi!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = medai.danger),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Qo'shish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Medical Documents Upload Section (Premium)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = medai.surface),
                border = BorderStroke(1.dp, medai.premium.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = medai.premium)
                            Text(text = "Tibbiy Hujjatlar G'aladoni (Premium)", fontWeight = FontWeight.Bold, color = medai.premium, fontSize = 15.sp)
                        }
                        
                        IconButton(
                            onClick = { showDocUploadDialog = true },
                            modifier = Modifier
                                .background(medai.premium.copy(alpha = 0.15f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Upload", tint = medai.premium, modifier = Modifier.size(18.dp))
                        }
                    }

                    Divider(color = medai.border.copy(alpha = 0.2f))

                    if (medicalDocs.isEmpty()) {
                        Text(
                            text = "Hech qanday tibbiy hujjat yuklanmagan. Premium foydalanuvchilar o'z analizlari, rentgen va retseptlarini saqlashlari mumkin.",
                            fontSize = 12.sp,
                            color = medai.textSecondary,
                            lineHeight = 16.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            medicalDocs.forEach { doc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(medai.premiumSoft.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .border(1.dp, medai.premium.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(medai.premium.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null, tint = medai.premium, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = doc.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = medai.textPrimary)
                                            Text(text = "Turi: ${doc.docType}", fontSize = 11.sp, color = medai.textSecondary)
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteMedicalDocument(doc.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = medai.danger, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Language settings selection
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = medai.surface),
                border = BorderStroke(1.dp, medai.border.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Translations.getString("select_language", lang),
                        fontWeight = FontWeight.Bold,
                        color = medai.textPrimary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("uz" to "🇺🇿 UZ", "ru" to "🇷🇺 RU", "en" to "🇬🇧 EN").forEach { (code, label) ->
                            val isSelected = lang == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) medai.brand.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) medai.brand else medai.border.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setLanguage(code) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) medai.brand else medai.textSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Standard links list
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = medai.surface),
                border = BorderStroke(1.dp, medai.border.copy(alpha = 0.4f))
            ) {
                Column {
                    if (isSuperAdmin) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("admin") }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(medai.warning.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = medai.warning, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Admin Boshqaruv Paneli",
                                    color = medai.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Foydalanuvchilar, to'lovlar va sozlamalar",
                                    color = medai.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = medai.warning, modifier = Modifier.size(20.dp))
                        }
                        Divider(color = medai.border.copy(alpha = 0.4f))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("help") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(medai.brandSoft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.HelpCenter, contentDescription = null, tint = medai.brand, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = Translations.getString("feat_help", lang),
                            color = medai.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = medai.textSecondary, modifier = Modifier.size(20.dp))
                    }

                    Divider(color = medai.border.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://claude.ai/code/artifact/d55f2334-c8df-41ef-b060-65046cfa7965")
                                )
                                context.startActivity(intent)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(medai.info.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.PrivacyTip, contentDescription = null, tint = medai.info, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Maxfiylik siyosati",
                            color = medai.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = medai.textSecondary, modifier = Modifier.size(20.dp))
                    }

                    Divider(color = medai.border.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(medai.danger.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = medai.danger, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = Translations.getString("sign_out", lang),
                            color = medai.danger,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = medai.danger.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(Translations.getString("sign_out", lang)) },
            text = { Text(Translations.getString("sign_out_confirm", lang)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = medai.danger)
                ) {
                    Text(Translations.getString("confirm", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = Translations.getString("cancel", lang))
                }
            }
        )
    }

    // Document upload dialog (Premium)
    if (showDocUploadDialog) {
        AlertDialog(
            onDismissRequest = { showDocUploadDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = medai.premium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Hujjat yuklash", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Hujjat nomi (masalan: Rentgen tahlili)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        docTypesList.forEach { type ->
                            FilterChip(
                                selected = docType == type,
                                onClick = { docType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (docTitle.isNotBlank()) {
                            viewModel.addMedicalDocument(docTitle, docType, mockDocBase64)
                            docTitle = ""
                            showDocUploadDialog = false
                            Toast.makeText(viewModel.getApplication(), "Hujjat muvaffaqiyatli saqlandi!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = medai.premium)
                ) {
                    Text("Yuklash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDocUploadDialog = false }) {
                    Text("Bekor qilish", color = medai.textSecondary)
                }
            }
        )
    }
}
