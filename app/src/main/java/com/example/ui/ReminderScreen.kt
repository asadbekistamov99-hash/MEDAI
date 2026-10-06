@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.*
import com.example.i18n.Translations
import com.example.ui.theme.*
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- SCREEN: REMINDERS & ALARMS ---

@Composable
fun ReminderScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val reminderItems by viewModel.reminders.collectAsState()
    val todayMetricsState by viewModel.todayMetrics.collectAsState()
    val allMetricsList by viewModel.allDailyMetrics.collectAsState()

    var name by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("08:00") }
    var frequency by remember { mutableStateOf("Daily") }
    var type by remember { mutableStateOf("Dori") }
    var targetFamily by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var isAdding by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Dorilar 💊", "Suv ichish 💧", "Taqvim 📅")

    val completedRemindersSet = remember(todayMetricsState) {
        val set = mutableSetOf<Int>()
        todayMetricsState?.let { m ->
            try {
                val arr = org.json.JSONArray(m.completedRemindersJson)
                for (i in 0 until arr.length()) {
                    set.add(arr.getInt(i))
                }
            } catch(e: Exception) {}
        }
        set
    }

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("reminder_title", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(medai.canvas)
        ) {
            // Tab Header
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = medai.surface,
                contentColor = medai.brand
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // --- TAB 0: MEDICINE REMINDERS (FIRESTORE) ---
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val firestoreReminders by viewModel.firestoreReminders.collectAsState()
                    val familyMembers by viewModel.familyMembers.collectAsState()
                    
                    var dosage by remember { mutableStateOf("1 ta tabletka") }
                    var notificationsEnabled by remember { mutableStateOf(true) }
                    var notificationFrequency by remember { mutableStateOf("Exact time") }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Button(
                                onClick = { isAdding = !isAdding },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
                            ) {
                                Icon(
                                    imageVector = if (isAdding) Icons.Default.Close else Icons.Default.Add,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAdding) "Bekor qilish" else "Yangi dori eslatmasi qo'shish",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isAdding) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, medai.brand.copy(alpha = 0.2f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Yangi eslatma tafsilotlari",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = medai.textPrimary
                                            )
                                        )

                                        // Medicine Name
                                        OutlinedTextField(
                                            value = name,
                                            onValueChange = { name = it },
                                            label = { Text("Dori nomi") },
                                            placeholder = { Text("Masalan: Paratsetamol, Kardiomagnil") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = medai.brand,
                                                unfocusedBorderColor = medai.border
                                            )
                                        )

                                        // Dosage Manual & Quick options
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(
                                                value = dosage,
                                                onValueChange = { dosage = it },
                                                label = { Text("Dozasi (Dosage)") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = medai.brand,
                                                    unfocusedBorderColor = medai.border
                                                )
                                            )
                                            
                                            // Quick dosages
                                            Row(
                                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                listOf("1 ta tabletka", "2 ta tabletka", "1/2 tabletka", "1 kapsula", "5 ml", "10 ml").forEach { qd ->
                                                    FilterChip(
                                                        selected = dosage == qd,
                                                        onClick = { dosage = qd },
                                                        label = { Text(qd, fontSize = 11.sp) }
                                                    )
                                                }
                                            }
                                        }

                                        // Time
                                        OutlinedTextField(
                                            value = time,
                                            onValueChange = { time = it },
                                            label = { Text("Vaqti") },
                                            placeholder = { Text("Masalan: 08:00, 14:00, 21:00") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = medai.brand,
                                                unfocusedBorderColor = medai.border
                                            )
                                        )

                                        // Frequency settings
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Takroriylik (Frequency Settings)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = medai.textSecondary)
                                            Row(
                                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                listOf("Daily" to "Kunlik", "Weekly" to "Haftalik", "Every 12 Hours" to "Har 12 soatda", "Every 8 Hours" to "Har 8 soatda", "Custom" to "Maxsus").forEach { (code, label) ->
                                                    FilterChip(
                                                        selected = frequency == code,
                                                        onClick = { frequency = code },
                                                        label = { Text(label, fontSize = 12.sp) }
                                                    )
                                                }
                                            }
                                        }

                                        // Notifications Configuration Card
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = medai.brandSoft.copy(alpha = 0.3f)),
                                            border = BorderStroke(1.dp, medai.brand.copy(alpha = 0.15f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Icon(
                                                            imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                                            contentDescription = null,
                                                            tint = if (notificationsEnabled) medai.brand else medai.textSecondary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Text("Bildirishnomalar (Notifications)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = medai.textPrimary)
                                                    }
                                                    Switch(
                                                        checked = notificationsEnabled,
                                                        onCheckedChange = { notificationsEnabled = it },
                                                        colors = SwitchDefaults.colors(checkedThumbColor = medai.brand)
                                                    )
                                                }

                                                if (notificationsEnabled) {
                                                    Text("Eslatish vaqti sozlamasi:", fontSize = 11.sp, color = medai.textSecondary, fontWeight = FontWeight.Medium)
                                                    Row(
                                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        listOf(
                                                            "Exact time" to "Aynan vaqtida",
                                                            "5m before" to "5 daqiqa oldin",
                                                            "15m before" to "15 daqiqa oldin",
                                                            "30m before" to "30 daqiqa oldin"
                                                        ).forEach { (code, label) ->
                                                            FilterChip(
                                                                selected = notificationFrequency == code,
                                                                onClick = { notificationFrequency = code },
                                                                label = { Text(label, fontSize = 11.sp) }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Target Family Member
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Kim uchun (Kim qabul qiladi):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = medai.textSecondary)
                                            
                                            Row(
                                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Myself Option
                                                FilterChip(
                                                    selected = targetFamily.isEmpty(),
                                                    onClick = { targetFamily = "" },
                                                    label = { Text("O'zimga", fontSize = 12.sp) }
                                                )
                                                
                                                // Loaded Family Members
                                                familyMembers.forEach { member ->
                                                    FilterChip(
                                                        selected = targetFamily == member.name,
                                                        onClick = { targetFamily = member.name },
                                                        label = { Text(member.name, fontSize = 12.sp) }
                                                    )
                                                }
                                            }
                                            
                                            if (targetFamily.isNotEmpty()) {
                                                Text(
                                                    text = "Eslatma ${targetFamily} uchun belgilanmoqda",
                                                    fontSize = 11.sp,
                                                    color = medai.premium,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(start = 2.dp)
                                                )
                                            }
                                        }

                                        // Notes
                                        OutlinedTextField(
                                            value = notes,
                                            onValueChange = { notes = it },
                                            label = { Text("Eslatma / Izoh") },
                                            placeholder = { Text("Masalan: Ovqatdan keyin, ko'p suv bilan") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = medai.brand,
                                                unfocusedBorderColor = medai.border
                                            )
                                        )

                                        Button(
                                            onClick = {
                                                if (name.isNotEmpty() && time.isNotEmpty()) {
                                                    viewModel.addFirestoreReminder(
                                                        medicineName = name,
                                                        dosage = dosage,
                                                        time = time,
                                                        frequency = frequency,
                                                        notificationsEnabled = notificationsEnabled,
                                                        notificationFrequency = notificationFrequency,
                                                        targetFamily = targetFamily.ifEmpty { null },
                                                        notes = notes.ifEmpty { null }
                                                    )
                                                    name = ""
                                                    notes = ""
                                                    targetFamily = ""
                                                    isAdding = false
                                                } else {
                                                    Toast.makeText(viewModel.getApplication(), "Iltimos, dori nomi va vaqtini kiriting!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = medai.brand),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Eslatmani Saqlash (Firestore)", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Firestore list header
                        item {
                            Text(
                                text = "Mening dori jadvallarim 🗓️",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = medai.textPrimary
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        if (firestoreReminders.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MedAIEmptyState(
                                        title = "Hozircha hech qanday dori eslatmasi yo'q.",
                                        message = "Dori ichish jadvallarini saqlash va nazorat qilish uchun yuqoridagi tugmani bosing.",
                                        icon = Icons.Default.Medication
                                    )
                                }
                            }
                        } else {
                            items(firestoreReminders) { item ->
                                val isCompletedToday = item.completedDates.contains(todayStr)
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, if (isCompletedToday) medai.success.copy(alpha = 0.25f) else medai.border.copy(alpha = 0.4f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .background(if (isCompletedToday) medai.brandSoft else medai.brand.copy(alpha = 0.1f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = "💊", fontSize = 20.sp)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text(
                                                            text = item.medicineName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 16.sp,
                                                            color = medai.textPrimary
                                                        )
                                                        
                                                        // Dosage badge
                                                        Box(
                                                            modifier = Modifier
                                                                .background(medai.brand.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = item.dosage,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = medai.brand
                                                            )
                                                        }
                                                    }
                                                    
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = medai.textSecondary, modifier = Modifier.size(12.dp))
                                                        Text(
                                                            text = "${item.time} - ${item.frequency}",
                                                            fontSize = 12.sp,
                                                            color = medai.textSecondary,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }

                                            Switch(
                                                checked = item.isActive,
                                                onCheckedChange = { viewModel.toggleFirestoreReminderActive(item.id, item.isActive) },
                                                colors = SwitchDefaults.colors(checkedThumbColor = medai.brand)
                                            )
                                        }

                                        // Notification & Target Info Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Notification chip
                                            Box(
                                                modifier = Modifier
                                                    .background(if (item.notificationsEnabled) Color(0xFFFFF3E0) else Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Icon(
                                                        imageVector = if (item.notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                                        contentDescription = null,
                                                        tint = if (item.notificationsEnabled) medai.warning else medai.textSecondary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = if (item.notificationsEnabled) "Eslatma: ${item.notificationFrequency}" else "Eslatma yo'q",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (item.notificationsEnabled) medai.warning else medai.textSecondary
                                                    )
                                                }
                                            }

                                            if (item.targetFamilyMember != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(medai.premium.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = medai.premium, modifier = Modifier.size(12.dp))
                                                        Text(text = item.targetFamilyMember, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = medai.premium)
                                                    }
                                                }
                                            }
                                        }

                                        if (item.notes != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = medai.textSecondary, modifier = Modifier.size(14.dp))
                                                    Text(text = item.notes, fontSize = 11.sp, color = medai.textSecondary, lineHeight = 14.sp)
                                                }
                                            }
                                        }

                                        Divider(color = medai.border.copy(alpha = 0.2f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (item.isActive) {
                                                if (isCompletedToday) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier
                                                            .background(medai.brandSoft, RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Done, contentDescription = null, tint = medai.success, modifier = Modifier.size(16.dp))
                                                        Text(text = "Bugun ichildi ✅", fontSize = 12.sp, color = medai.success, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = { viewModel.completeFirestoreReminder(item.id, item.completedDates) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = medai.brand),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                        modifier = Modifier.height(36.dp)
                                                    ) {
                                                        Text("Ichdim 💊", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                Text(text = "Eslatma faol emas", fontSize = 11.sp, color = medai.textSecondary, fontWeight = FontWeight.Medium)
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteFirestoreReminder(item.id) },
                                                modifier = Modifier
                                                    .background(Color(0xFFFFEBEE), CircleShape)
                                                    .size(36.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "O'chirish", tint = medai.danger, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // --- TAB 1: WATER REMINDERS ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val currentGlasses = todayMetricsState?.waterGlasses ?: 0
                        val waterGoal = todayMetricsState?.waterGoal ?: 8
                        val progressFraction = if (waterGoal > 0) currentGlasses.toFloat() / waterGoal else 0f

                        // Hero Water Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = medai.info.copy(alpha = 0.15f), spotColor = medai.info.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = medai.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                                    CircularProgressIndicator(
                                        progress = progressFraction,
                                        modifier = Modifier.fillMaxSize(),
                                        color = medai.info,
                                        strokeWidth = 8.dp,
                                        trackColor = medai.border
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "💧", fontSize = 32.sp)
                                        Text(text = "$currentGlasses / $waterGoal", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = medai.textPrimary)
                                    }
                                }

                                Text(
                                    text = if (currentGlasses >= waterGoal) "Ajoyib! Bugungi suv ichish normasi bajarildi! 🏆" else "Suv ichish salomatlik uchun juda muhim!",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = medai.textSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateWaterProgress(-1) },
                                        modifier = Modifier
                                            .background(medai.info.copy(alpha = 0.1f), CircleShape)
                                            .size(48.dp)
                                    ) {
                                        Text("-", fontSize = 24.sp, color = medai.info, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.updateWaterProgress(1) },
                                        colors = ButtonDefaults.buttonColors(containerColor = medai.info, contentColor = Color.White),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Stakan suv ichish 💧", fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = { viewModel.updateWaterProgress(1) },
                                        modifier = Modifier
                                            .background(medai.info.copy(alpha = 0.1f), CircleShape)
                                            .size(48.dp)
                                    ) {
                                        Text("+", fontSize = 20.sp, color = medai.info, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Target Goal Setup Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = medai.surface),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Suv ichish maqsadini sozlash", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = medai.textPrimary)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(6, 8, 10, 12).forEach { goal ->
                                        FilterChip(
                                            selected = waterGoal == goal,
                                            onClick = { viewModel.updateWaterGoal(goal) },
                                            label = { Text("$goal stakan") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = medai.info,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Reminder Frequency Card
                        var selectedFreq by remember { mutableStateOf(1) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = medai.surface),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Eslatma chastotasi (Suv)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = medai.textPrimary)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(1 to "Har soat", 2 to "Har 2 soat", 3 to "Har 3 soat").forEach { (hours, label) ->
                                        FilterChip(
                                            selected = selectedFreq == hours,
                                            onClick = { selectedFreq = hours },
                                            label = { Text(label) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = medai.info,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // --- TAB 2: HEALTH CALENDAR ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val calendar = Calendar.getInstance()
                        val currentYear = calendar.get(Calendar.YEAR)
                        val currentMonth = calendar.get(Calendar.MONTH)
                        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                        val firstDayCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, currentYear)
                            set(Calendar.MONTH, currentMonth)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                        val startOffset = (firstDayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday-indexed offset

                        var selectedDayInspect by remember { mutableStateOf<Int?>(calendar.get(Calendar.DAY_OF_MONTH)) }

                        Text(
                            text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = medai.brand,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        // Calendar Grid Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = medai.surface),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Weekdays Header
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    listOf("D", "S", "C", "P", "J", "S", "Y").forEach { dayLabel ->
                                        Text(
                                            text = dayLabel,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            color = medai.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Grid rows
                                val totalCells = startOffset + daysInMonth
                                var currentCell = 0

                                while (currentCell < totalCells) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        for (col in 0 until 7) {
                                            if (currentCell < startOffset || currentCell >= totalCells) {
                                                Box(modifier = Modifier.weight(1f))
                                            } else {
                                                val dayNum = currentCell - startOffset + 1
                                                val dateStr = String.format("%d-%02d-%02d", currentYear, currentMonth + 1, dayNum)
                                                
                                                val dayMetrics = allMetricsList.find { it.date == dateStr }
                                                val activeCount = reminderItems.count { it.isActive }
                                                
                                                val dotColor = when {
                                                    dayMetrics == null -> Color.Transparent
                                                    else -> {
                                                        val completedCount = try { org.json.JSONArray(dayMetrics.completedRemindersJson).length() } catch(e: Exception) { 0 }
                                                        when {
                                                            activeCount == 0 -> medai.textSecondary
                                                            completedCount >= activeCount -> medai.brand
                                                            completedCount > 0 -> medai.warning
                                                            else -> medai.danger
                                                        }
                                                    }
                                                }

                                                val isInspected = selectedDayInspect == dayNum

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .padding(2.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isInspected) medai.brand.copy(alpha = 0.15f) else Color.Transparent)
                                                        .border(
                                                            width = if (isInspected) 1.dp else 0.dp,
                                                            color = if (isInspected) medai.brand else Color.Transparent,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable { selectedDayInspect = dayNum },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = dayNum.toString(),
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isInspected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isInspected) medai.brand else medai.textPrimary
                                                        )
                                                        if (dotColor != Color.Transparent) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(6.dp)
                                                                    .background(dotColor, CircleShape)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            currentCell++
                                        }
                                    }
                                }
                            }
                        }

                        // Inspect selected day logs
                        selectedDayInspect?.let { inspectedDay ->
                            val inspectDateStr = String.format("%d-%02d-%02d", currentYear, currentMonth + 1, inspectedDay)
                            val dayMetrics = allMetricsList.find { it.date == inspectDateStr }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = medai.surface),
                                border = BorderStroke(1.dp, medai.border)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "$inspectDateStr - Kunlik hisobot",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = medai.brand
                                    )

                                    if (dayMetrics == null) {
                                        Text(text = "Ushbu kunda hech qanday ma'lumot kiritilmagan.", fontSize = 12.sp, color = medai.textSecondary)
                                    } else {
                                        val completedCount = try { org.json.JSONArray(dayMetrics.completedRemindersJson).length() } catch(e: Exception) { 0 }
                                        Text(text = "🥤 Suv ichilgan: ${dayMetrics.waterGlasses} stakan (Maqsad: ${dayMetrics.waterGoal})", fontSize = 12.sp, color = medai.textPrimary)
                                        Text(text = "💊 Qabul qilingan dorilar: $completedCount", fontSize = 12.sp, color = medai.textPrimary)
                                        if (dayMetrics.weight > 0) {
                                            Text(text = "⚖️ Vazn: ${dayMetrics.weight} kg", fontSize = 12.sp, color = medai.textPrimary)
                                        }
                                        if (dayMetrics.bpSystolic > 0) {
                                            Text(text = "🩸 Qon bosimi: ${dayMetrics.bpSystolic}/${dayMetrics.bpDiastolic} mmHg", fontSize = 12.sp, color = medai.textPrimary)
                                        }
                                        if (dayMetrics.heartRate > 0) {
                                            Text(text = "❤️ Puls: ${dayMetrics.heartRate} BPM", fontSize = 12.sp, color = medai.textPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
