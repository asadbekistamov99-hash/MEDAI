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
    val lang by viewModel.currentLanguage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val medicalDocs by viewModel.medicalDocuments.collectAsState()

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
            // Page header: left-aligned icon badge + title/subtitle, matching the app's header pattern
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Profil",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Shaxsiy ma'lumotlar va sozlamalar",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Premium or Free Avatar
            val isPremium = user?.isPremium ?: false
            val borderBrush = if (isPremium) {
                Brush.sweepGradient(listOf(PremiumPurple, AccentCyan, PremiumPurple))
            } else {
                Brush.sweepGradient(listOf(PrimaryGreen, AccentCyan, PrimaryGreen))
            }
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(8.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .border(3.dp, borderBrush, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (isPremium) PremiumPurple.copy(alpha = 0.1f) else PrimaryGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    val initialChar = user?.name?.firstOrNull()?.toString()?.uppercase() ?: "U"
                    Text(
                        text = initialChar,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPremium) PremiumPurple else PrimaryGreen,
                            fontSize = 40.sp
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Name and Email with perfect contrast
            Text(
                text = user?.name ?: "Foydalanuvchi",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = user?.email ?: "email@example.com",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Plan Badge with custom gradients
            val badgeBrush = if (isPremium) {
                Brush.linearGradient(listOf(PremiumPurple, Color(0xFF8E24AA)))
            } else {
                Brush.linearGradient(listOf(PrimaryGreen, DarkGreen))
            }
            
            Row(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .background(badgeBrush, RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isPremium) Icons.Default.WorkspacePremium else Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isPremium) "PREMIUM FOYDALANUVCHI" else "BEPUL REJIM",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            // Super Admin Special Badge and Access Card (strictly and exclusively for SUPER_ADMIN_EMAIL)
            if (viewModel.isSuperAdmin) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .clickable { navController.navigate("admin") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.5.dp, WarningOrange)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(WarningOrange.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = WarningOrange,
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
                                        .background(WarningOrange, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "SUPER",
                                        color = Color.Black,
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
                            tint = WarningOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Streak Board
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFFFF3E0), CircleShape)
                            .border(1.dp, Color(0xFFFFB74D), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔥", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${user?.healthScore?.let { (it / 15).coerceAtLeast(1) } ?: 5} Kunlik Salomatlik Seriyasi!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Har kuni ilovaga kirib salomatligingizni nazorat qiling va seriyani davom ettiring!",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Stats summary row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MedicalBorder.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(LightGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Height, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Bo'y", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text(text = "${user?.height ?: 175.0} sm", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MedicalBorder.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(LightGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.MonitorWeight, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Vazn", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text(text = "${user?.weight ?: 70.0} kg", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
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
                border = BorderStroke(1.dp, MedicalBorder.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = PrimaryGreen)
                        Text(text = "Mening Yutuqlarim (Achievements)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }
                    
                    Divider(color = MedicalBorder.copy(alpha = 0.3f))
                    
                    achievementsList.forEach { ach ->
                        val isUnlocked = unlockedAchievementsSet.contains(ach.first)
                        val rowBg = if (isUnlocked) LightGreen.copy(alpha = 0.3f) else Color.Transparent
                        val rowBorder = if (isUnlocked) BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f)) else null
                        
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
                                    .background(if (isUnlocked) PrimaryGreen.copy(alpha = 0.2f) else TextSecondary.copy(alpha = 0.1f), CircleShape),
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
                                    color = if (isUnlocked) TextPrimary else TextSecondary
                                )
                                Text(
                                    text = ach.third,
                                    fontSize = 11.sp,
                                    color = if (isUnlocked) TextSecondary else TextSecondary.copy(alpha = 0.7f),
                                    lineHeight = 14.sp
                                )
                            }
                            if (isUnlocked) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            } else {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                        Text(text = "Mening Allergiyalarim ⚠️", fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 15.sp)
                    }

                    // Allergy List
                    if (allergiesList.isEmpty()) {
                        Text(
                            text = "Allergiyalar kiritilmagan. Quyida yangi allergiya qo'shishingiz mumkin.",
                            fontSize = 12.sp,
                            color = TextSecondary,
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
                                    label = { Text("$name ($type)", color = TextPrimary, fontWeight = FontWeight.Medium) },
                                    leadingIcon = {
                                        Box(modifier = Modifier.size(8.dp).background(ErrorRed, CircleShape))
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { viewModel.deleteAllergy(name) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete",
                                                tint = ErrorRed,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Divider(color = MedicalBorder.copy(alpha = 0.2f))

                    // Add Allergy Form
                    Text(text = "Yangi Allergiya Qo'shish", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    
                    OutlinedTextField(
                        value = allergyName,
                        onValueChange = { allergyName = it },
                        placeholder = { Text("Allergen nomi (masalan: Penitsillin, Yong'oq)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ErrorRed,
                            unfocusedBorderColor = MedicalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
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
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PremiumPurple.copy(alpha = 0.2f))
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
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = PremiumPurple)
                            Text(text = "Tibbiy Hujjatlar G'aladoni (Premium)", fontWeight = FontWeight.Bold, color = PremiumPurple, fontSize = 15.sp)
                        }
                        
                        IconButton(
                            onClick = { showDocUploadDialog = true },
                            modifier = Modifier
                                .background(PremiumPurple.copy(alpha = 0.15f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Upload", tint = PremiumPurple, modifier = Modifier.size(18.dp))
                        }
                    }

                    Divider(color = MedicalBorder.copy(alpha = 0.2f))

                    if (medicalDocs.isEmpty()) {
                        Text(
                            text = "Hech qanday tibbiy hujjat yuklanmagan. Premium foydalanuvchilar o'z analizlari, rentgen va retseptlarini saqlashlari mumkin.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            medicalDocs.forEach { doc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PremiumLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .border(1.dp, PremiumPurple.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
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
                                                .background(PremiumPurple.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null, tint = PremiumPurple, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = doc.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                            Text(text = "Turi: ${doc.docType}", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteMedicalDocument(doc.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MedicalBorder.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Translations.getString("select_language", lang),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
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
                                    .background(if (isSelected) PrimaryGreen.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PrimaryGreen else MedicalBorder.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setLanguage(code) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PrimaryGreen else TextSecondary,
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MedicalBorder.copy(alpha = 0.4f))
            ) {
                Column {
                    if (viewModel.isSuperAdmin) {
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
                                    .background(WarningOrange.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Admin Boshqaruv Paneli",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Foydalanuvchilar, to'lovlar va sozlamalar",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                        }
                        Divider(color = MedicalBorder.copy(alpha = 0.4f))
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
                                .background(LightGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.HelpCenter, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = Translations.getString("feat_help", lang),
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                    
                    Divider(color = MedicalBorder.copy(alpha = 0.2f))
                    
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
                                .background(ErrorRed.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = Translations.getString("sign_out", lang),
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
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
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
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
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = PremiumPurple)
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
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple)
                ) {
                    Text("Yuklash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDocUploadDialog = false }) {
                    Text("Bekor qilish", color = TextSecondary)
                }
            }
        )
    }
}

// --- SCREEN: PREMIUM SUBSCRIPTION MANAGEMENT ---

@Composable
fun PremiumUpgradeScreen(viewModel: AppViewModel, onBack: () -> Unit) {
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
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(PremiumPurple.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, PremiumPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = PremiumPurple,
                    modifier = Modifier.size(42.dp)
                )
            }
            
            Text(
                text = Translations.getString("premium_upgrade_title", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PremiumPurple
            )
            
            Text(
                text = Translations.getString("premium_upgrade_desc", lang),
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            // Price tag card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = PremiumPurple.copy(alpha = 0.3f), spotColor = PremiumPurple.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PremiumPurple)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(PremiumPurple, Color(0xFF9333EA))))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Translations.getString("premium_price", lang),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 30.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getLangText(
                            "Barcha professional imkoniyatlar to'liq ochiladi",
                            "Все профессиональные функции разблокируются",
                            "Unlock all professional features completely"
                        ),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Feature Checklist (Premium Advantages)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MedicalBorder),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = getLangText("Premium Imkoniyatlari:", "Возможности Премиум:", "Premium Features:"),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        fontSize = 16.sp
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
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = feat,
                                fontSize = 13.sp,
                                color = TextPrimary
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
                border = BorderStroke(1.dp, MedicalBorder),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        Text(
                            text = getLangText("To'lov Tafsilotlari", "Детали платежа", "Payment Details"),
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "Click / Payme karta raqami: 8600 1234 5678 9012",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = Translations.getString("premium_pay_details", lang),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!showCheckInput) {
                Button(
                    onClick = { showCheckInput = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple, contentColor = Color.White)
                ) {
                    Text(text = Translations.getString("premium_upload_check", lang), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else {
                Button(
                    onClick = {
                        // Simulate check upload
                        viewModel.submitPaymentCheck("simulated_payment_check_base64_receipt")
                        showCheckInput = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White)
                ) {
                    Text(text = Translations.getString("premium_submit", lang), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// --- SCREEN: REAL-TIME FAMILY MEMBERS MONITORING ---

@Composable
fun FamilyScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val lang by viewModel.currentLanguage.collectAsState()
    val family by viewModel.familyMembers.collectAsState()
    val immunizations by viewModel.immunizationRecords.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var inviteInput by remember { mutableStateOf("") }
    var selectedMemberForReminder by remember { mutableStateOf<FamilyMemberLocal?>(null) }
    var selectedMemberForDetail by remember { mutableStateOf<FamilyMemberLocal?>(null) }

    // Add sub-account state
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var newMemberName by remember { mutableStateOf("") }
    var newMemberRelation by remember { mutableStateOf("Child") }
    var newMemberEmail by remember { mutableStateOf("") }
    var newMemberPhone by remember { mutableStateOf("") }

    // Reminder inputs
    var medName by remember { mutableStateOf("") }
    var medTime by remember { mutableStateOf("09:00") }
    var medFreq by remember { mutableStateOf("Daily") }

    val relationsList = listOf("Child", "Spouse", "Father", "Mother", "Brother", "Sister")

    fun getLangText(uz: String, ru: String, en: String): String {
        return when (lang) {
            "uz" -> uz
            "ru" -> ru
            else -> en
        }
    }

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("family_title", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(LightGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👨‍👩‍👧‍👦", fontSize = 26.sp)
                    }
                    Column {
                        Text(
                            text = getLangText("Oilaviy sog'liq markazi", "Семейный центр здоровья", "Family Health Center"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Text(
                            text = getLangText(
                                "Oila a'zolarini qo'shing, ularning salomatlik ko'rsatkichlarini kuzating va emlash taqvimlarini boshqaring.",
                                "Добавляйте членов семьи, следите за их здоровьем и управляйте календарем прививок.",
                                "Add family members, monitor shared health records, and track detailed immunization schedules."
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }

            // Controls: Invite & Direct Creation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showAddMemberDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = getLangText("Yangi a'zo qo'shish", "Добавить члена", "Add Member"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = {
                        if (inviteInput.isNotEmpty()) {
                            Toast.makeText(viewModel.getApplication(), getLangText("Taklifnoma muvaffaqiyatli yuborildi!", "Приглашение успешно отправлено!", "Invitation sent successfully!"), Toast.LENGTH_SHORT).show()
                            inviteInput = ""
                        } else {
                            Toast.makeText(viewModel.getApplication(), getLangText("Iltimos, telefon yoki email kiriting", "Пожалуйста, введите телефон или email", "Please enter phone or email"), Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.12f), contentColor = AccentCyan),
                    modifier = Modifier.width(110.dp)
                ) {
                    Text(text = getLangText("Taklif etish", "Пригласить", "Invite"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Invitation text-field
            OutlinedTextField(
                value = inviteInput,
                onValueChange = { inviteInput = it },
                placeholder = { Text(getLangText("Email yoki Telefon raqami orqali tezda taklif yuborish...", "Быстрое приглашение по email или телефону...", "Quick invite by email or phone...")) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                singleLine = true
            )

            // Alerts Section
            Text(text = getLangText("Salomatlik Ogohlantirishlari ⚠️", "Предупреждения о здоровье ⚠️", "Health Alerts ⚠️"), fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 14.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, ErrorRed)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = getLangText(
                            "Diqqat! Jasur Karimovning salomatlik ko'rsatkichi 72% gacha tushib ketdi. Suv ichish eslatmasi yuboring!",
                            "Внимание! Показатель здоровья Джасура Каримова снизился до 72%. Отправьте напоминание о воде!",
                            "Warning! Jasur Karimov's health score fell to 72%. Send a water reminder!"
                        ),
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }

            // Family Weekly Summary
            Text(text = getLangText("Haftalik Salomatlik Hisoboti 📊", "Еженедельный отчет здоровья 📊", "Weekly Health Summary 📊"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 14.sp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = getLangText("Ushbu hafta oilaviy salomatlik juda yaxshi!", "На этой неделе здоровье семьи отличное!", "Family health is superb this week!"), fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 13.sp)
                    Text(text = getLangText("• Suv ichish normasi bajarilishi: 94%", "• Норма воды выполнена на: 94%", "• Water goal completed: 94%"), fontSize = 12.sp, color = TextSecondary)
                    Text(text = getLangText("• Dorilarni o'z vaqtida ichish: 88%", "• Прием лекарств вовремя: 88%", "• Medication adherence: 88%"), fontSize = 12.sp, color = TextSecondary)
                    Text(text = getLangText("• O'rtacha oilaviy ball: 86%", "• Средний балл семьи: 86%", "• Average family score: 86%"), fontSize = 12.sp, color = TextSecondary)
                }
            }

            // Live monitoring list
            Text(
                text = Translations.getString("family_member_live", lang).uppercase(),
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                family.forEach { member ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clickable { selectedMemberForDetail = member },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MedicalBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(LightGreen, CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.fillMaxSize().padding(8.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = member.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(
                                            text = getLangText(
                                                when(member.relation) {
                                                    "Child" -> "Farzand"
                                                    "Spouse" -> "Turmush o'rtog'i"
                                                    "Father" -> "Ota"
                                                    "Mother" -> "Ona"
                                                    "Brother" -> "Aka/Uka"
                                                    "Sister" -> "Opa/Singil"
                                                    else -> member.relation
                                                },
                                                when(member.relation) {
                                                    "Child" -> "Ребенок"
                                                    "Spouse" -> "Супруг(а)"
                                                    "Father" -> "Отец"
                                                    "Mother" -> "Мать"
                                                    "Brother" -> "Брат"
                                                    "Sister" -> "Сестра"
                                                    else -> member.relation
                                                },
                                                member.relation
                                            ),
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        Text(text = "${Translations.getString("family_steps", lang)}: ${member.stepsToday}", fontSize = 11.sp, color = PrimaryGreen)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(PrimaryGreen.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "${member.healthScore}%", fontWeight = FontWeight.Black, color = PrimaryGreen, fontSize = 13.sp)
                                    }
                                }
                            }

                            Divider(color = MedicalBorder.copy(alpha = 0.08f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add reminder button
                                Button(
                                    onClick = { selectedMemberForReminder = member },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple.copy(alpha = 0.12f), contentColor = PremiumPurple),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(getLangText("Eslatma", "Напомнить", "Reminder"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Open Detail/Schedule button
                                Button(
                                    onClick = { selectedMemberForDetail = member },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen.copy(alpha = 0.12f), contentColor = PrimaryGreen),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(getLangText("Emlash & Sog'liq", "Прививки и Здоровье", "Vaccines & Health"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Delete Button for custom ones
                                if (member.uid.startsWith("family_uid_") && member.uid != "family_uid_1" && member.uid != "family_uid_2" && member.uid != "family_uid_3") {
                                    IconButton(
                                        onClick = {
                                            viewModel.removeFamilyMember(member.uid)
                                            Toast.makeText(viewModel.getApplication(), getLangText("Sub-akkaunt o'chirildi", "Суб-аккаунт удален", "Sub-account removed"), Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: LINK NEW FAMILY SUB-ACCOUNT ---
    if (showAddMemberDialog) {
        var expandedRelMenu by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("➕", fontSize = 20.sp)
                    Text(
                        text = getLangText("Oila a'zosi profilini biriktirish", "Связать профиль члена семьи", "Link Family Member Profile"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = { Text(getLangText("Ism sharifi", "Имя и фамилия", "Full Name")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Relationship Selection dropdown button
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedRelMenu = true },
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
                                        when(newMemberRelation) {
                                            "Child" -> "Farzand"
                                            "Spouse" -> "Turmush o'rtog'i"
                                            "Father" -> "Ota"
                                            "Mother" -> "Ona"
                                            "Brother" -> "Aka/Uka"
                                            "Sister" -> "Opa/Singil"
                                            else -> newMemberRelation
                                        },
                                        when(newMemberRelation) {
                                            "Child" -> "Ребенок"
                                            "Spouse" -> "Супруг(а)"
                                            "Father" -> "Отец"
                                            "Mother" -> "Мать"
                                            "Brother" -> "Брат"
                                            "Sister" -> "Сестра"
                                            else -> newMemberRelation
                                        },
                                        newMemberRelation
                                    )
                                )
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expandedRelMenu,
                            onDismissRequest = { expandedRelMenu = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            relationsList.forEach { rel ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            getLangText(
                                                when(rel) {
                                                    "Child" -> "Farzand (Child)"
                                                    "Spouse" -> "Turmush o'rtog'i (Spouse)"
                                                    "Father" -> "Ota (Father)"
                                                    "Mother" -> "Ona (Mother)"
                                                    "Brother" -> "Aka/Uka (Brother)"
                                                    "Sister" -> "Opa/Singil (Sister)"
                                                    else -> rel
                                                },
                                                when(rel) {
                                                    "Child" -> "Ребенок (Child)"
                                                    "Spouse" -> "Супруг(а) (Spouse)"
                                                    "Father" -> "Отец (Father)"
                                                    "Mother" -> "Мать (Mother)"
                                                    "Brother" -> "Брат (Brother)"
                                                    "Sister" -> "Сестра (Sister)"
                                                    else -> rel
                                                },
                                                rel
                                            )
                                        )
                                    },
                                    onClick = {
                                        newMemberRelation = rel
                                        expandedRelMenu = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newMemberEmail,
                        onValueChange = { newMemberEmail = it },
                        label = { Text(getLangText("Email (ixtiyoriy)", "Email (необязательно)", "Email (optional)")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = newMemberPhone,
                        onValueChange = { newMemberPhone = it },
                        label = { Text(getLangText("Telefon (ixtiyoriy)", "Телефон (необязательно)", "Phone (optional)")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newMemberName.isNotBlank()) {
                            viewModel.linkFamilyMemberSubAccount(
                                name = newMemberName,
                                relation = newMemberRelation,
                                email = newMemberEmail,
                                phone = newMemberPhone
                            )
                            Toast.makeText(viewModel.getApplication(), getLangText("Sub-akkaunt muvaffaqiyatli bog'landi va emlash taqvimi yaratildi!", "Суб-аккаунт успешно привязан и календарь прививок создан!", "Sub-account linked and immunization schedule seeded!"), Toast.LENGTH_LONG).show()
                            newMemberName = ""
                            newMemberEmail = ""
                            newMemberPhone = ""
                            showAddMemberDialog = false
                        } else {
                            Toast.makeText(viewModel.getApplication(), getLangText("Iltimos, ism kiriting", "Пожалуйста, введите имя", "Please enter name"), Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text(getLangText("Bog'lash", "Связать", "Link"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text(getLangText("Bekor qilish", "Отмена", "Cancel"), color = TextSecondary)
                }
            }
        )
    }

    // --- DIALOG: DETAILED HEALTH DASHBOARD & IMMUNIZATION SCHEDULES ---
    selectedMemberForDetail?.let { member ->
        var activeTab by remember { mutableStateOf(0) } // 0: Vaccines, 1: Vitals & Logs
        var showAddVaccineDialog by remember { mutableStateOf(false) }

        // Vital Logger State
        var vitalSys by remember { mutableStateOf("120") }
        var vitalDia by remember { mutableStateOf("80") }
        var vitalHr by remember { mutableStateOf("72") }

        // Vaccine state inside detailed dialog
        val memberVaccines = remember(immunizations, member.uid) {
            immunizations.filter { it.familyMemberUid == member.uid }
        }

        // Active Vaccine Mark Completed State
        var completedVaccineRecord by remember { mutableStateOf<ImmunizationRecordLocal?>(null) }
        var adminByInput by remember { mutableStateOf("") }
        var compDateInput by remember { mutableStateOf("") }
        var notesInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedMemberForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = getLangText("Oila a'zosi salomatlik profili", "Профиль здоровья члена семьи", "Family Health Profile"),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = { selectedMemberForDetail = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Simple Tab layout
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color.Transparent,
                        contentColor = PrimaryGreen
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text(getLangText("💉 Emlash taqvimi", "💉 Календарь прививок", "💉 Immunizations"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text(getLangText("💓 Ko'rsatkichlar", "💓 Показатели", "💓 Vitals & Logs"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    if (activeTab == 0) {
                        // IMMUNIZATION SCHEDULES TAB
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = getLangText("Tavsiya etilgan vaksinalar:", "Рекомендованные вакцины:", "Recommended Vaccines:"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                TextButton(
                                    onClick = { showAddVaccineDialog = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryGreen)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(getLangText("Qo'shish", "Добавить", "Add New"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                }
                            }

                            if (memberVaccines.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🧬", fontSize = 32.sp)
                                        Text(
                                            text = getLangText("Hozircha emlashlar yo'q", "Пока прививок нет", "No immunizations scheduled yet"),
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                memberVaccines.forEach { vac ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (vac.status == "Completed") SuccessGreen.copy(alpha = 0.05f) else WarningOrange.copy(alpha = 0.06f)
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (vac.status == "Completed") SuccessGreen.copy(alpha = 0.2f) else MedicalBorder.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = vac.vaccineName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                                    Text(text = getLangText("Kasallik: ", "Болезнь: ", "Disease: ") + vac.targetDisease, fontSize = 11.sp, color = TextSecondary)
                                                }
                                                // Status Badge
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (vac.status == "Completed") SuccessGreen.copy(alpha = 0.15f) else WarningOrange.copy(alpha = 0.15f)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (vac.status == "Completed") getLangText("Emlangan", "Привит", "Completed") else getLangText("Kutilmoqda", "Ожидает", "Pending"),
                                                        color = if (vac.status == "Completed") SuccessGreen else WarningOrange,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(text = getLangText("Muddat: ", "Срок: ", "Age: ") + vac.scheduledAge, fontSize = 11.sp, color = TextSecondary)
                                                    Text(text = getLangText("Sana: ", "Дата: ", "Date: ") + vac.dueDate, fontSize = 11.sp, color = TextSecondary)
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    if (vac.status != "Completed") {
                                                        Button(
                                                            onClick = {
                                                                completedVaccineRecord = vac
                                                                compDateInput = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                                                                adminByInput = ""
                                                                notesInput = ""
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(28.dp),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(getLangText("Emlash", "Сделать", "Administer"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.deleteImmunizationRecord(vac.id) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete vaccine", tint = ErrorRed.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }

                                            if (vac.status == "Completed") {
                                                Divider(color = SuccessGreen.copy(alpha = 0.1f))
                                                Text(
                                                    text = "✅ " + getLangText("Emlandi: ", "Введено: ", "Administered: ") + "${vac.completedDate ?: "Yaqinda"}" +
                                                            (vac.administeredBy?.let { " | $it" } ?: ""),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SuccessGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // SHARED HEALTH RECORDS & VITALS TAB
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = getLangText("Joriy jismoniy ko'rsatkichlar", "Текущие показатели организма", "Current Vital Signs"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )

                            // Blood Pressure Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("🩸", fontSize = 24.sp)
                                        Column {
                                            Text(getLangText("Arterial Bosim", "Артериальное давление", "Blood Pressure"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(getLangText("Normal ko'rsatkich: 120/80 mm sim.ust", "Норма: 120/80 мм рт.ст.", "Normal: 120/80 mmHg"), fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                    Text(
                                        text = "$vitalSys/$vitalDia",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (vitalSys.toIntOrNull() ?: 120 > 135) ErrorRed else SuccessGreen
                                    )
                                }
                            }

                            // Heart Rate Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("💓", fontSize = 24.sp)
                                        Column {
                                            Text(getLangText("Yurak urishi (Puls)", "Пульс", "Heart Rate"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(getLangText("Normal: daqiqasiga 60-90 marta", "Норма: 60-90 уд/мин", "Normal: 60-90 bpm"), fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                    Text(
                                        text = "$vitalHr bpm",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = SuccessGreen
                                    )
                                }
                            }

                            // Activity steps
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("🏃", fontSize = 24.sp)
                                        Column {
                                            Text(getLangText("Kunlik qadamlar faolligi", "Шаги за сегодня", "Daily Step Activity"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(getLangText("Kunlik maqsad: 10,000 qadam", "Цель: 10,000 шагов", "Target: 10,000 steps"), fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                    Text(
                                        text = "${member.stepsToday} / 10,000",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = PrimaryGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Vitals Logger Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AccentCyan.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = getLangText("Yangi ko'rsatkichlarni kiritish ✍️", "Записать новые показатели ✍️", "Log New Vital Readings ✍️"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AccentCyan
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = vitalSys,
                                            onValueChange = { vitalSys = it },
                                            label = { Text("SYS") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                        )
                                        OutlinedTextField(
                                            value = vitalDia,
                                            onValueChange = { vitalDia = it },
                                            label = { Text("DIA") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                        )
                                        OutlinedTextField(
                                            value = vitalHr,
                                            onValueChange = { vitalHr = it },
                                            label = { Text("Puls") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (vitalSys.isNotBlank() && vitalDia.isNotBlank() && vitalHr.isNotBlank()) {
                                                // Calculate custom healthScore based on logged vitals
                                                val sys = vitalSys.toIntOrNull() ?: 120
                                                val score = if (sys in 115..129) 100 else if (sys in 100..139) 85 else 60
                                                
                                                // Save the values in local scope to see immediate update, and insert updated member!
                                                val updatedMember = member.copy(
                                                    healthScore = score,
                                                    lastActive = System.currentTimeMillis()
                                                )
                                                coroutineScope.launch {
                                                    viewModel.dao.insertFamilyMember(updatedMember)
                                                }
                                                Toast.makeText(viewModel.getApplication(), getLangText("Ko'rsatkichlar muvaffaqiyatli saqlandi!", "Показатели успешно сохранены!", "Vital readings saved successfully!"), Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(getLangText("Ko'rsatkichlarni saqlash", "Сохранить показатели", "Save Vitals"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMemberForDetail = null }) {
                    Text(getLangText("Yopish", "Закрыть", "Close"), fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )

        // --- DIALOG: MARK VACCINE AS COMPLETED ---
        completedVaccineRecord?.let { vac ->
            AlertDialog(
                onDismissRequest = { completedVaccineRecord = null },
                title = {
                    Text(
                        text = getLangText("Emlashni tasdiqlash", "Подтверждение прививки", "Confirm Vaccination"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = vac.vaccineName, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        OutlinedTextField(
                            value = compDateInput,
                            onValueChange = { compDateInput = it },
                            label = { Text(getLangText("Emlangan sana (kk.oo.yyyy)", "Дата прививки (дд.мм.гггг)", "Date Completed (dd.mm.yyyy)")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = adminByInput,
                            onValueChange = { adminByInput = it },
                            label = { Text(getLangText("Shifokor / Klinika nomi", "Врач / Клиника", "Doctor / Clinic Name")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text(getLangText("Shaxsiy eslatma / nojo'ya ta'sirlari", "Заметки / побочные эффекты", "Notes / Side Effects")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (compDateInput.isNotBlank()) {
                                val updated = vac.copy(
                                    status = "Completed",
                                    completedDate = compDateInput,
                                    administeredBy = adminByInput,
                                    notes = notesInput
                                )
                                viewModel.updateImmunizationRecord(updated)
                                completedVaccineRecord = null
                                Toast.makeText(viewModel.getApplication(), getLangText("Emlash muvaffaqiyatli saqlandi!", "Прививка успешно зафиксирована!", "Vaccine marked as completed!"), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(viewModel.getApplication(), getLangText("Sanani kiriting", "Введите дату", "Please enter date"), Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text(getLangText("Tasdiqlash", "Подтвердить", "Confirm"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { completedVaccineRecord = null }) {
                        Text(getLangText("Bekor qilish", "Отмена", "Cancel"), color = TextSecondary)
                    }
                }
            )
        }

        // --- DIALOG: ADD NEW CUSTOM IMMUNIZATION RECORD ---
        if (showAddVaccineDialog) {
            var customVacName by remember { mutableStateOf("") }
            var customDisease by remember { mutableStateOf("") }
            var customAge by remember { mutableStateOf("Custom") }
            var customDueDate by remember { mutableStateOf("") }
            var customNotes by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddVaccineDialog = false },
                title = {
                    Text(
                        text = getLangText("Yangi emlash qo'shish", "Добавить новую прививку", "Add New Custom Immunization"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = customVacName,
                            onValueChange = { customVacName = it },
                            label = { Text(getLangText("Vaksina nomi", "Название вакцины", "Vaccine Name")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customDisease,
                            onValueChange = { customDisease = it },
                            label = { Text(getLangText("Qarshi kasallik", "Целевая болезнь", "Target Disease")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customAge,
                            onValueChange = { customAge = it },
                            label = { Text(getLangText("Tavsiya etilgan muddat (masalan: 18 oylik)", "Срок (например, 18 месяцев)", "Recommended Age (e.g., 18 months)")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customDueDate,
                            onValueChange = { customDueDate = it },
                            label = { Text(getLangText("Emlash muddati (kk.oo.yyyy)", "Срок вакцинации (дд.мм.гггг)", "Due Date (dd.mm.yyyy)")) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. 15.09.2026") }
                        )
                        OutlinedTextField(
                            value = customNotes,
                            onValueChange = { customNotes = it },
                            label = { Text(getLangText("Izohlar / Qo'shimcha ma'lumot", "Заметки / Дополнительно", "Notes / Details")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customVacName.isNotBlank() && customDueDate.isNotBlank()) {
                                viewModel.addCustomImmunizationRecord(
                                    familyMemberUid = member.uid,
                                    vaccineName = customVacName,
                                    targetDisease = customDisease,
                                    scheduledAge = customAge,
                                    dueDate = customDueDate,
                                    notes = customNotes
                                )
                                showAddVaccineDialog = false
                                Toast.makeText(viewModel.getApplication(), getLangText("Yangi emlash muvaffaqiyatli jadvalga qo'shildi!", "Новая прививка успешно добавлена в расписание!", "New immunization scheduled successfully!"), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(viewModel.getApplication(), getLangText("Vaksina nomi va sanasini kiriting", "Введите имя вакцины и дату", "Please fill in vaccine name and due date"), Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text(getLangText("Qo'shish", "Добавить", "Add"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddVaccineDialog = false }) {
                        Text(getLangText("Bekor qilish", "Отмена", "Cancel"), color = TextSecondary)
                    }
                }
            )
        }
    }

    // Add reminder dialog
    selectedMemberForReminder?.let { member ->
        AlertDialog(
            onDismissRequest = { selectedMemberForReminder = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = PremiumPurple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = member.name + " " + getLangText("uchun eslatma", "напоминание для", "reminder for"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text(getLangText("Dori yoki tavsiya nomi", "Название лекарства / рекомендация", "Medicine name / Advice")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medTime,
                        onValueChange = { medTime = it },
                        label = { Text(getLangText("Vaqti (masalan: 08:00)", "Время (например: 08:00)", "Time (e.g. 08:00)")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medFreq,
                        onValueChange = { medFreq = it },
                        label = { Text(getLangText("Chastotasi (masalan: Kuniga 1 marta)", "Частота (например: 1 раз в день)", "Frequency (e.g. Daily)")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isNotBlank() && medTime.isNotBlank()) {
                            viewModel.addReminder(
                                medicineName = medName,
                                time = medTime,
                                frequency = medFreq,
                                type = "Oila",
                                targetFamily = member.name,
                                notes = getLangText("Oila a'zosi tomonidan yuborildi", "Отправлено членом семьи", "Added by family member")
                            )
                            medName = ""
                            selectedMemberForReminder = null
                            Toast.makeText(viewModel.getApplication(), getLangText("Eslatma muvaffaqiyatli oila a'zosiga biriktirildi!", "Напоминание успешно привязано к члену семьи!", "Reminder successfully assigned to family member!"), Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple)
                ) {
                    Text(getLangText("Biriktirish", "Привязать", "Assign"))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMemberForReminder = null }) {
                    Text(getLangText("Bekor qilish", "Отмена", "Cancel"), color = TextSecondary)
                }
            }
        )
    }
}

// --- SCREEN: ANALYTICS & BMI ---

@Composable
fun AnalyticsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val lang by viewModel.currentLanguage.collectAsState()
    val todayMetricsState by viewModel.todayMetrics.collectAsState()
    val allMetricsList by viewModel.allDailyMetrics.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    val tabLabels = listOf("Vazn & BMI ⚖️", "Bosim & Puls 🩸", "Uyqu & Taom 😴")

    // Weight & BMI Inputs
    var heightInput by remember { mutableStateOf("175") }
    var weightInput by remember { mutableStateOf(todayMetricsState?.weight?.let { if (it > 0) it.toString() else "70" } ?: "70") }

    // BP & HR Inputs
    var systolicInput by remember { mutableStateOf("120") }
    var diastolicInput by remember { mutableStateOf("80") }
    var bpmInput by remember { mutableStateOf("72") }

    // Sleep & Meal Inputs
    var sleepHoursInput by remember { mutableStateOf("8.0") }
    var bedtimeInput by remember { mutableStateOf("23:00") }
    var waketimeInput by remember { mutableStateOf("07:00") }

    var mealTitleInput by remember { mutableStateOf("") }
    var mealCaloriesInput by remember { mutableStateOf("450") }
    var mealTypeSelected by remember { mutableStateOf("Breakfast") }

    val isAnalyzingNutrition by viewModel.isAnalyzingNutrition.collectAsState()
    val nutritionAnalysisResult by viewModel.nutritionAdvice.collectAsState()

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("feat_analytics", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Header
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryGreen
            ) {
                tabLabels.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeTab) {
                    0 -> {
                        // --- WEIGHT & BMI TAB ---
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "Tana Vazni Indeksi (BMI) Kalkulyatori", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 15.sp)

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = heightInput,
                                        onValueChange = { heightInput = it },
                                        label = { Text("Bo'y (cm)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = { weightInput = it },
                                        label = { Text("Vazn (kg)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                val heightM = heightInput.toDoubleOrNull()?.div(100.0) ?: 1.75
                                val weightKg = weightInput.toDoubleOrNull() ?: 70.0
                                val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 22.8

                                val (bmiCategory, bmiColor) = when {
                                    bmi < 18.5 -> "Vazn yetarli emas (Underweight)" to Color(0xFF2196F3)
                                    bmi < 25.0 -> "Sog'lom vazn (Normal Weight) ✅" to PrimaryGreen
                                    bmi < 30.0 -> "Ortiqcha vazn (Overweight)" to Color(0xFFFF9800)
                                    else -> "Semizlik (Obese) ⚠️" to ErrorRed
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = bmiColor.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, bmiColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Sizning BMI ko'rsatkichingiz:", fontSize = 12.sp, color = TextSecondary)
                                        Text(text = String.format("%.1f", bmi), fontWeight = FontWeight.Bold, fontSize = 28.sp, color = bmiColor)
                                        Text(text = bmiCategory, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = bmiColor)
                                    }
                                }

                                Button(
                                    onClick = {
                                        val w = weightInput.toDoubleOrNull()
                                        if (w != null) {
                                            viewModel.logWeightMetrics(w)
                                            Toast.makeText(viewModel.getApplication(), "Vazn muvaffaqiyatli saqlandi!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                ) {
                                    Text("Kunlik vaznni saqlash")
                                }
                            }
                        }

                        // Last 7 days Weight chart
                        Text(text = "Oxirgi vazn o'zgarishlari", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 13.sp)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val recentMetrics = allMetricsList.takeLast(7)
                                if (recentMetrics.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = "Ma'lumotlar kam", color = TextSecondary, fontSize = 12.sp)
                                    }
                                } else {
                                    recentMetrics.forEachIndexed { index, metric ->
                                        if (metric.weight > 0) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "${metric.weight.toInt()}", fontSize = 10.sp, color = TextPrimary)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(16.dp)
                                                        .height((metric.weight * 1.2).dp.coerceAtMost(100.dp))
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(PrimaryGreen)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = metric.date.takeLast(2), fontSize = 10.sp, color = TextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val trendsLabel0 = when (lang) {
                            "uz" -> "Salomatlik dinamikasi (Recharts & Firestore)"
                            "ru" -> "Динамика здоровья (Recharts & Firestore)"
                            else -> "Health Dynamics (Recharts & Firestore)"
                        }
                        Text(text = trendsLabel0, fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 13.sp)
                        RechartsHealthTrends(viewModel = viewModel)
                    }
                    1 -> {
                        // --- PRESSURE & PULSE TAB ---
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "Qon bosimi va Puls (Yurak urishi)", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 15.sp)

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = systolicInput,
                                        onValueChange = { systolicInput = it },
                                        label = { Text("Sistolik (mmHg)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = diastolicInput,
                                        onValueChange = { diastolicInput = it },
                                        label = { Text("Diastolik (mmHg)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                OutlinedTextField(
                                    value = bpmInput,
                                    onValueChange = { bpmInput = it },
                                    label = { Text("Puls (BPM - bir daqiqada)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                val sys = systolicInput.toIntOrNull() ?: 120
                                val dia = diastolicInput.toIntOrNull() ?: 80

                                val (bpStatus, bpColor) = when {
                                    sys < 120 && dia < 80 -> "Qon bosimi ideal darajada! ✅" to PrimaryGreen
                                    sys in 120..129 && dia < 80 -> "Normal darajadan biroz yuqori (Pre-gipertoniya)" to Color(0xFFFF9800)
                                    else -> "Yuqori qon bosimi (Gipertoniya) ⚠️ Shifokor bilan maslahatlashing." to ErrorRed
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = bpColor.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, bpColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Tahlil:", fontSize = 11.sp, color = TextSecondary)
                                        Text(text = bpStatus, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = bpColor, textAlign = TextAlign.Center)
                                    }
                                }

                                Button(
                                    onClick = {
                                        val sysVal = systolicInput.toIntOrNull()
                                        val diaVal = diastolicInput.toIntOrNull()
                                        val bpmVal = bpmInput.toIntOrNull()
                                        if (sysVal != null && diaVal != null && bpmVal != null) {
                                            viewModel.logBloodPressureMetrics(sysVal, diaVal)
                                            viewModel.logHeartRateMetrics(bpmVal)
                                            Toast.makeText(viewModel.getApplication(), "Ko'rsatkichlar saqlandi!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                ) {
                                    Text("Bosim va pulsni saqlash")
                                }
                            }
                        }

                        // Last 7 days Pulse Rate Chart
                        Text(text = "Puls (Yurak urishi) o'zgarishi (BPM)", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 13.sp)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val recentMetrics = allMetricsList.takeLast(7)
                                if (recentMetrics.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = "Ma'lumotlar kam", color = TextSecondary, fontSize = 12.sp)
                                    }
                                } else {
                                    recentMetrics.forEachIndexed { index, metric ->
                                        if (metric.heartRate > 0) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "${metric.heartRate}", fontSize = 10.sp, color = TextPrimary)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(14.dp)
                                                        .height((metric.heartRate * 1.1).dp.coerceAtMost(100.dp))
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(Color(0xFFE91E63))
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = metric.date.takeLast(2), fontSize = 10.sp, color = TextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val trendsLabel1 = when (lang) {
                            "uz" -> "Salomatlik dinamikasi (Recharts & Firestore)"
                            "ru" -> "Динамика здоровья (Recharts & Firestore)"
                            else -> "Health Dynamics (Recharts & Firestore)"
                        }
                        Text(text = trendsLabel1, fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 13.sp)
                        RechartsHealthTrends(viewModel = viewModel)
                    }
                    2 -> {
                        // --- SLEEP & NUTRITION TAB ---
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "Kunlik uyqu va ovqatlanish jurnali", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 15.sp)

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = sleepHoursInput,
                                        onValueChange = { sleepHoursInput = it },
                                        label = { Text("Uyqu (soat)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = bedtimeInput,
                                        onValueChange = { bedtimeInput = it },
                                        label = { Text("Yotish vaqti") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = waketimeInput,
                                        onValueChange = { waketimeInput = it },
                                        label = { Text("Uyg'onish") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Button(
                                    onClick = {
                                        val hours = sleepHoursInput.toDoubleOrNull() ?: 8.0
                                        viewModel.logSleepMetrics(hours, bedtimeInput, waketimeInput)
                                        Toast.makeText(viewModel.getApplication(), "Uyqu jurnali yangilandi!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                ) {
                                    Text("Uyquni saqlash")
                                }

                                Divider()

                                // Food Input Section
                                Text(text = "Taom qo'shish", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 13.sp)

                                OutlinedTextField(
                                    value = mealTitleInput,
                                    onValueChange = { mealTitleInput = it },
                                    label = { Text("Taom nomi (masalan: Lag'mon, Olma)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = mealCaloriesInput,
                                        onValueChange = { mealCaloriesInput = it },
                                        label = { Text("Kalloriya (kcal)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf("Breakfast", "Lunch", "Dinner").forEach { type ->
                                                FilterChip(
                                                    selected = mealTypeSelected == type,
                                                    onClick = { mealTypeSelected = type },
                                                    label = { Text(type, fontSize = 10.sp) }
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        val cal = mealCaloriesInput.toIntOrNull() ?: 450
                                        if (mealTitleInput.isNotBlank()) {
                                            viewModel.logMealMetrics(mealTitleInput, cal, mealTypeSelected)
                                            mealTitleInput = ""
                                            Toast.makeText(viewModel.getApplication(), "Taom jurnali qo'shildi!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                ) {
                                    Text("Taomni qo'shish")
                                }
                            }
                        }

                        // Daily Meal summaries card
                        val totalCalories = todayMetricsState?.mealsJson?.let {
                            try {
                                val arr = org.json.JSONArray(it)
                                var sum = 0
                                for (i in 0 until arr.length()) {
                                    sum += arr.getJSONObject(i).getInt("calories")
                                }
                                sum
                            } catch(e: Exception) { 0 }
                        } ?: 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Bugungi ovqatlanish", fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = "$totalCalories / 2000 kcal", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                }

                                LinearProgressIndicator(
                                    progress = (totalCalories.toFloat() / 2000f).coerceAtMost(1f),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = PrimaryGreen,
                                    trackColor = MedicalBorder
                                )

                                val mealList = remember(todayMetricsState?.mealsJson) {
                                    val list = mutableListOf<Triple<String, String, Int>>()
                                    todayMetricsState?.mealsJson?.let { json ->
                                        try {
                                            val arr = org.json.JSONArray(json)
                                            for (i in 0 until arr.length()) {
                                                val obj = arr.getJSONObject(i)
                                                list.add(Triple(
                                                    obj.optString("title", ""),
                                                    obj.optString("type", ""),
                                                    obj.optInt("calories", 0)
                                                ))
                                            }
                                        } catch(e: Exception) {}
                                    }
                                    list
                                }

                                mealList.forEach { (title, type, calories) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "• $title ($type)", fontSize = 12.sp, color = TextSecondary)
                                        Text(text = "$calories kcal", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        // AI Nutrition Coach Advice Trigger
                        Button(
                            onClick = { viewModel.analyzeNutritionDaily() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple)
                        ) {
                            if (isAnalyzingNutrition) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI Parhez tavsiyasi (Gemini)")
                                }
                            }
                        }

                        if (nutritionAnalysisResult.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = PremiumPurple.copy(alpha = 0.1f)),
                                border = BorderStroke(1.dp, PremiumPurple)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "🥗 AI Parhezshunos maslahati:", fontWeight = FontWeight.Bold, color = PremiumPurple, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = nutritionAnalysisResult, fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN: MEDICAL SERVICES & CLINICS FILTER ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(viewModel: AppViewModel, onBack: () -> Unit, onNavigateToUpgrade: () -> Unit) {
    val lang by viewModel.currentLanguage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isPremium = user?.isPremium ?: false

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
                .background(MedicalBackground)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // 1. City Chips
            item {
                Text(
                    text = "Shahar tanlang:".uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Toshkent", "Samarqand", "Buxoro", "Namangan").forEach { city ->
                        val isSelected = selectedCity == city
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) PrimaryGreen else Color.White)
                                .border(1.dp, if (isSelected) PrimaryGreen else MedicalBorder, RoundedCornerShape(50.dp))
                                .clickable { selectedCity = city }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = city,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }

            // 3. Specialty Chips
            item {
                Text(
                    text = "Yo'nalish tanlang:".uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Terapevt", "Kardiolog", "Pediatr", "Stomatolog", "Klinika").forEach { spec ->
                        val isSelected = selectedSpecialty == spec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSelected) PrimaryGreen else MedicalBorder, RoundedCornerShape(50.dp))
                                .clickable { selectedSpecialty = spec }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = spec,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }

            // 4. Emergency Call Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = PrimaryGreen, spotColor = PrimaryGreen)
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
                    color = PrimaryGreen,
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
                        border = BorderStroke(1.dp, MedicalBorder)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Ushbu shahar bo'yicha shifoxona ma'lumotlari topilmadi.",
                                fontSize = 13.sp,
                                color = TextSecondary,
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
                        border = BorderStroke(1.dp, MedicalBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(LightGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = address,
                                    fontSize = 12.sp,
                                    color = TextSecondary
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
                                    tint = PrimaryGreen,
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

// --- SCREEN: HELP & SUPPORT CENTER ---

@Composable
fun HelpCenterScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val lang by viewModel.currentLanguage.collectAsState()

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("feat_help", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.HelpCenter, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text(text = "Tez-tez so'raladigan savollar", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = TextPrimary)
                        Text(text = "Savolingizga javob toping yoki qo'llab-quvvatlash xizmatiga yozing", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Text(
                text = "FAQ".uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = PrimaryGreen,
                letterSpacing = 1.2.sp
            )

            listOf(
                "MedAI nima?" to "MedAI - bu sun'iy intellektga asoslangan shaxsiy tibbiy maslahatchi va salomatlik tahlilchisidir.",
                "Premium plan nima bera oladi?" to "Premium plan barcha shifokor chatlari, vision laborator tahlil va oilaviy kuzatuvni faollashtiradi."
            ).forEach { (q, a) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MedicalBorder)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AccentCyan.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = q, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = a, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* Simulated Telegram Support */ },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(imageVector = Icons.Default.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Telegram Support orqali bog'lanish", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


// --- SCREEN: BANNED OVERLAY SCREEN ---

@Composable
fun BannedScreen(viewModel: AppViewModel) {
    val lang by viewModel.currentLanguage.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = Translations.getString("banned_title", lang), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = Translations.getString("banned_desc", lang),
            textAlign = TextAlign.Center,
            color = Color.LightGray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text(text = Translations.getString("sign_out", lang), color = Color.White)
        }
    }
}

// --- SCREEN: MAINTENANCE OVERLAY SCREEN ---

@Composable
fun MaintenanceScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Construction, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Texnik ishlar olib borilmoqda", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WarningOrange)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "MedAI tizimi yangilanmoqda. Iltimos birozdan so'ng qayta urinib ko'ring.",
            textAlign = TextAlign.Center,
            color = Color.LightGray,
            fontSize = 14.sp
        )
    }
}

// --- SCREEN: NOTIFICATIONS TIMELINE ---

@Composable
fun NotificationsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val lang by viewModel.currentLanguage.collectAsState()
    val list by viewModel.notifications.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.markAllNotificationsAsRead()
    }

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("feat_notifications", lang), onBack = onBack) }
    ) { innerPadding ->
        if (list.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .background(PrimaryGreen.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = null, tint = PrimaryGreen.copy(alpha = 0.6f), modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Hozircha hech qanday bildirishnoma yo'q.",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Yangi bildirishnomalar shu yerda paydo bo'ladi",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MedicalBorder)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        (if (item.type == "sos") ErrorRed else PrimaryGreen).copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.type == "sos") Icons.Default.Warning else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (item.type == "sos") ErrorRed else PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = item.message, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RechartsHealthTrends(viewModel: AppViewModel) {
    val firestoreVitals by viewModel.firestoreVitals.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()

    val titleText = when (lang) {
        "uz" -> "Salomatlik ko'rsatkichlari dinamikasi"
        "ru" -> "Динамика показателей здоровья"
        else -> "Health Vitals Dynamics"
    }

    val tabAllText = when (lang) {
        "uz" -> "Hammasi"
        "ru" -> "Все"
        else -> "All"
    }

    val jsonArray = org.json.JSONArray()
    firestoreVitals.forEach { r ->
        val obj = org.json.JSONObject()
        obj.put("date", r.date)
        obj.put("heartRate", r.heartRate)
        obj.put("bpSystolic", r.bpSystolic)
        obj.put("bpDiastolic", r.bpDiastolic)
        obj.put("weight", r.weight)
        obj.put("note", r.note)
        jsonArray.put(obj)
    }
    val jsonString = jsonArray.toString()

    val htmlTemplate = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <!-- Load Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary: '#2E7D32',
                        accent: '#E91E63',
                        slateDark: '#121212'
                    }
                }
            }
        }
    </script>
    <!-- Load React, ReactDOM, Recharts, and Babel for JSX -->
    <script src="https://unpkg.com/react@18/umd/react.production.min.js" crossorigin></script>
    <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js" crossorigin></script>
    <script src="https://unpkg.com/prop-types@15.8.1/prop-types.min.js"></script>
    <script src="https://unpkg.com/recharts@2.12.7/umd/Recharts.js"></script>
    <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
    <style>
        body {
            background-color: #121212;
            color: #ffffff;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            margin: 0;
            padding: 0;
            overflow-x: hidden;
        }
    </style>
</head>
<body>
    <div id="chart-root"></div>

    <script type="text/babel">
        const { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, AreaChart, Area } = Recharts;

        const rawData = $jsonString;

        function HealthTrendsApp() {
            const [view, setView] = React.useState('all');

            const chartData = rawData.map(d => ({
                date: d.date.split('-').slice(1).join('/'),
                systolic: d.bpSystolic > 0 ? d.bpSystolic : null,
                diastolic: d.bpDiastolic > 0 ? d.bpDiastolic : null,
                heartRate: d.heartRate > 0 ? d.heartRate : null,
                weight: d.weight > 0 ? d.weight : null
            })).filter(d => d.systolic !== null || d.diastolic !== null || d.heartRate !== null || d.weight !== null);

            return (
                <div className="p-4 bg-slateDark rounded-2xl">
                    <div className="flex justify-between items-center mb-6">
                        <h2 className="text-sm font-bold text-white flex items-center gap-2">
                            <span>📈</span>
                            <span>$titleText</span>
                        </h2>
                        
                        <div className="flex gap-1 bg-[#1e1e1e] p-1 rounded-lg text-xs">
                            <button 
                                onClick={() => setView('all')}
                                className={view === 'all' ? "px-2 py-1 rounded-md transition bg-primary text-white font-bold" : "px-2 py-1 rounded-md transition text-gray-400"}
                            >
                                $tabAllText
                            </button>
                            <button 
                                onClick={() => setView('bp')}
                                className={view === 'bp' ? "px-2 py-1 rounded-md transition bg-primary text-white font-bold" : "px-2 py-1 rounded-md transition text-gray-400"}
                            >
                                BP
                            </button>
                            <button 
                                onClick={() => setView('hr')}
                                className={view === 'hr' ? "px-2 py-1 rounded-md transition bg-primary text-white font-bold" : "px-2 py-1 rounded-md transition text-gray-400"}
                            >
                                HR
                            </button>
                            <button 
                                onClick={() => setView('weight')}
                                className={view === 'weight' ? "px-2 py-1 rounded-md transition bg-primary text-white font-bold" : "px-2 py-1 rounded-md transition text-gray-400"}
                            >
                                WT
                            </button>
                        </div>
                    </div>

                    <div className="h-64 w-full">
                        {chartData.length === 0 ? (
                            <div className="h-full flex items-center justify-center text-gray-500 text-xs text-center p-4">
                                Sog'lomlashtirish ma'lumotlari yuklanmoqda... / Loading historical vital data...
                            </div>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                {view === 'all' && (
                                    <LineChart data={chartData} margin={{ top: 5, right: 10, left: -25, bottom: 5 }}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="#2a2a2a" />
                                        <XAxis dataKey="date" stroke="#888888" fontSize={10} />
                                        <YAxis stroke="#888888" fontSize={10} />
                                        <Tooltip contentStyle={{ backgroundColor: '#222', borderColor: '#444' }} />
                                        <Legend wrapperStyle={{ fontSize: 10 }} />
                                        <Line type="monotone" dataKey="systolic" name="Sys" stroke="#4CAF50" strokeWidth={2.5} dot={{ r: 3 }} />
                                        <Line type="monotone" dataKey="diastolic" name="Dia" stroke="#81C784" strokeWidth={2} dot={{ r: 2 }} />
                                        <Line type="monotone" dataKey="heartRate" name="BPM" stroke="#E91E63" strokeWidth={2} dot={{ r: 2 }} />
                                    </LineChart>
                                )}

                                {view === 'bp' && (
                                    <LineChart data={chartData} margin={{ top: 5, right: 10, left: -25, bottom: 5 }}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="#2a2a2a" />
                                        <XAxis dataKey="date" stroke="#888888" fontSize={10} />
                                        <YAxis domain={[40, 200]} stroke="#888888" fontSize={10} />
                                        <Tooltip contentStyle={{ backgroundColor: '#222', borderColor: '#444' }} />
                                        <Legend wrapperStyle={{ fontSize: 10 }} />
                                        <Line type="monotone" dataKey="systolic" name="Systolic" stroke="#4CAF50" strokeWidth={3} activeDot={{ r: 5 }} />
                                        <Line type="monotone" dataKey="diastolic" name="Diastolic" stroke="#2196F3" strokeWidth={2.5} activeDot={{ r: 4 }} />
                                    </LineChart>
                                )}

                                {view === 'hr' && (
                                    <AreaChart data={chartData} margin={{ top: 5, right: 10, left: -25, bottom: 5 }}>
                                        <defs>
                                            <linearGradient id="colorHr" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#E91E63" stopOpacity={0.4}/>
                                                <stop offset="95%" stopColor="#E91E63" stopOpacity={0}/>
                                            </linearGradient>
                                        </defs>
                                        <CartesianGrid strokeDasharray="3 3" stroke="#2a2a2a" />
                                        <XAxis dataKey="date" stroke="#888888" fontSize={10} />
                                        <YAxis domain={[40, 150]} stroke="#888888" fontSize={10} />
                                        <Tooltip contentStyle={{ backgroundColor: '#222', borderColor: '#444' }} />
                                        <Legend wrapperStyle={{ fontSize: 10 }} />
                                        <Area type="monotone" dataKey="heartRate" name="Heart Rate" stroke="#E91E63" fillOpacity={1} fill="url(#colorHr)" strokeWidth={3} />
                                    </AreaChart>
                                )}

                                {view === 'weight' && (
                                    <BarChart data={chartData} margin={{ top: 5, right: 10, left: -25, bottom: 5 }}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="#2a2a2a" />
                                        <XAxis dataKey="date" stroke="#888888" fontSize={10} />
                                        <YAxis domain={['dataMin - 5', 'dataMax + 5']} stroke="#888888" fontSize={10} />
                                        <Tooltip contentStyle={{ backgroundColor: '#222', borderColor: '#444' }} />
                                        <Legend wrapperStyle={{ fontSize: 10 }} />
                                        <Bar dataKey="weight" name="Weight (kg)" fill="#4CAF50" radius={[4, 4, 0, 0]} />
                                    </BarChart>
                                )}
                            </ResponsiveContainer>
                        )}
                    </div>
                </div>
            );
        }

        const root = ReactDOM.createRoot(document.getElementById('chart-root'));
        root.render(<HealthTrendsApp />);
    </script>
</body>
</html>
    """.trimIndent()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    webViewClient = WebViewClient()
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://localhost", htmlTemplate, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
