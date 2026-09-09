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
                .background(MedicalBackground)
        ) {
            // Tab Header
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryGreen
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
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
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
                                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Yangi eslatma tafsilotlari",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
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
                                                focusedBorderColor = PrimaryGreen,
                                                unfocusedBorderColor = MedicalBorder
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
                                                    focusedBorderColor = PrimaryGreen,
                                                    unfocusedBorderColor = MedicalBorder
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
                                                focusedBorderColor = PrimaryGreen,
                                                unfocusedBorderColor = MedicalBorder
                                            )
                                        )

                                        // Frequency settings
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Takroriylik (Frequency Settings)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextSecondary)
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
                                            colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.3f)),
                                            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f))
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
                                                            tint = if (notificationsEnabled) PrimaryGreen else TextSecondary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Text("Bildirishnomalar (Notifications)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                                    }
                                                    Switch(
                                                        checked = notificationsEnabled,
                                                        onCheckedChange = { notificationsEnabled = it },
                                                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen)
                                                    )
                                                }

                                                if (notificationsEnabled) {
                                                    Text("Eslatish vaqti sozlamasi:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
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
                                            Text("Kim uchun (Kim qabul qiladi):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextSecondary)
                                            
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
                                                    color = PremiumPurple,
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
                                                focusedBorderColor = PrimaryGreen,
                                                unfocusedBorderColor = MedicalBorder
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
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
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
                                    color = TextPrimary
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
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(64.dp).background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("💊", fontSize = 28.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Hozircha hech qanday dori eslatmasi yo'q.",
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Dori ichish jadvallarini Firestore-da saqlash va nazorat qilish uchun yuqoridagi tugmani bosing.",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(firestoreReminders) { item ->
                                val isCompletedToday = item.completedDates.contains(todayStr)
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, if (isCompletedToday) SuccessGreen.copy(alpha = 0.25f) else MedicalBorder.copy(alpha = 0.4f)),
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
                                                        .background(if (isCompletedToday) LightGreen else PrimaryGreen.copy(alpha = 0.1f), CircleShape),
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
                                                            color = TextPrimary
                                                        )
                                                        
                                                        // Dosage badge
                                                        Box(
                                                            modifier = Modifier
                                                                .background(PrimaryGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = item.dosage,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = PrimaryGreen
                                                            )
                                                        }
                                                    }
                                                    
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                                                        Text(
                                                            text = "${item.time} - ${item.frequency}",
                                                            fontSize = 12.sp,
                                                            color = TextSecondary,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }

                                            Switch(
                                                checked = item.isActive,
                                                onCheckedChange = { viewModel.toggleFirestoreReminderActive(item.id, item.isActive) },
                                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen)
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
                                                        tint = if (item.notificationsEnabled) WarningOrange else TextSecondary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = if (item.notificationsEnabled) "Eslatma: ${item.notificationFrequency}" else "Eslatma yo'q",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (item.notificationsEnabled) WarningOrange else TextSecondary
                                                    )
                                                }
                                            }

                                            if (item.targetFamilyMember != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(PremiumPurple.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PremiumPurple, modifier = Modifier.size(12.dp))
                                                        Text(text = item.targetFamilyMember, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PremiumPurple)
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
                                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                                    Text(text = item.notes, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
                                                }
                                            }
                                        }

                                        Divider(color = MedicalBorder.copy(alpha = 0.2f))

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
                                                            .background(LightGreen, RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Done, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                                        Text(text = "Bugun ichildi ✅", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = { viewModel.completeFirestoreReminder(item.id, item.completedDates) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                        modifier = Modifier.height(36.dp)
                                                    ) {
                                                        Text("Ichdim 💊", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                Text(text = "Eslatma faol emas", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteFirestoreReminder(item.id) },
                                                modifier = Modifier
                                                    .background(Color(0xFFFFEBEE), CircleShape)
                                                    .size(36.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "O'chirish", tint = ErrorRed, modifier = Modifier.size(18.dp))
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
                                .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = AccentCyan.copy(alpha = 0.15f), spotColor = AccentCyan.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, MedicalBorder)
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
                                        color = AccentCyan,
                                        strokeWidth = 8.dp,
                                        trackColor = MedicalBorder
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "💧", fontSize = 32.sp)
                                        Text(text = "$currentGlasses / $waterGoal", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                                    }
                                }

                                Text(
                                    text = if (currentGlasses >= waterGoal) "Ajoyib! Bugungi suv ichish normasi bajarildi! 🏆" else "Suv ichish salomatlik uchun juda muhim!",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateWaterProgress(-1) },
                                        modifier = Modifier
                                            .background(AccentCyan.copy(alpha = 0.1f), CircleShape)
                                            .size(48.dp)
                                    ) {
                                        Text("-", fontSize = 24.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.updateWaterProgress(1) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.White),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Stakan suv ichish 💧", fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = { viewModel.updateWaterProgress(1) },
                                        modifier = Modifier
                                            .background(AccentCyan.copy(alpha = 0.1f), CircleShape)
                                            .size(48.dp)
                                    ) {
                                        Text("+", fontSize = 20.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Target Goal Setup Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Suv ichish maqsadini sozlash", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
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
                                                selectedContainerColor = AccentCyan,
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
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MedicalBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Eslatma chastotasi (Suv)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
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
                                                selectedContainerColor = AccentCyan,
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
                            color = PrimaryGreen,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        // Calendar Grid Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MedicalBorder)
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
                                            color = TextSecondary,
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
                                                            activeCount == 0 -> TextSecondary
                                                            completedCount >= activeCount -> PrimaryGreen
                                                            completedCount > 0 -> WarningOrange
                                                            else -> ErrorRed
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
                                                        .background(if (isInspected) PrimaryGreen.copy(alpha = 0.15f) else Color.Transparent)
                                                        .border(
                                                            width = if (isInspected) 1.dp else 0.dp,
                                                            color = if (isInspected) PrimaryGreen else Color.Transparent,
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
                                                            color = if (isInspected) PrimaryGreen else TextPrimary
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
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, MedicalBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "$inspectDateStr - Kunlik hisobot",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = PrimaryGreen
                                    )

                                    if (dayMetrics == null) {
                                        Text(text = "Ushbu kunda hech qanday ma'lumot kiritilmagan.", fontSize = 12.sp, color = TextSecondary)
                                    } else {
                                        val completedCount = try { org.json.JSONArray(dayMetrics.completedRemindersJson).length() } catch(e: Exception) { 0 }
                                        Text(text = "🥤 Suv ichilgan: ${dayMetrics.waterGlasses} stakan (Maqsad: ${dayMetrics.waterGoal})", fontSize = 12.sp, color = TextPrimary)
                                        Text(text = "💊 Qabul qilingan dorilar: $completedCount", fontSize = 12.sp, color = TextPrimary)
                                        if (dayMetrics.weight > 0) {
                                            Text(text = "⚖️ Vazn: ${dayMetrics.weight} kg", fontSize = 12.sp, color = TextPrimary)
                                        }
                                        if (dayMetrics.bpSystolic > 0) {
                                            Text(text = "🩸 Qon bosimi: ${dayMetrics.bpSystolic}/${dayMetrics.bpDiastolic} mmHg", fontSize = 12.sp, color = TextPrimary)
                                        }
                                        if (dayMetrics.heartRate > 0) {
                                            Text(text = "❤️ Puls: ${dayMetrics.heartRate} BPM", fontSize = 12.sp, color = TextPrimary)
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

// --- SCREEN: HISTORIC TIMELINE LOG ---

@Composable
fun HistoryScreen(viewModel: AppViewModel) {
    val lang by viewModel.currentLanguage.collectAsState()
    val checks by viewModel.symptomChecks.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("tab_history", lang)) }
    ) { innerPadding ->
        if (checks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MedicalBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(72.dp).background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(32.dp), tint = PrimaryGreen)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tarix bo'sh", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Qidiruv natijalari bu yerda saqlanadi.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MedicalBackground)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(checks) { check ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MedicalBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier.size(40.dp).background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = "Simptom tekshiruvi: ${check.bodyPart}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text(
                                            text = "Kiritilgan simptomlar: ${check.symptomsInput}",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch { viewModel.dao.deleteSymptomCheck(check.id) }
                                    },
                                    modifier = Modifier
                                        .background(ErrorRed.copy(alpha = 0.08f), CircleShape)
                                        .size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "O'chirish", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                }
                            }
                            Divider(color = MedicalBorder)
                            Text(text = check.resultJson, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp, maxLines = 4)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN: GENERAL CHAT ---

@Composable
fun GeneralChatScreen(viewModel: AppViewModel) {
    val lang by viewModel.currentLanguage.collectAsState()
    val chatMessages by viewModel.dao.getChatMessagesFlow("general").collectAsState(initial = emptyList())
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("tab_chat", lang)) },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("AI Sog'liq maslahatchisidan so'rang...", color = TextSecondary.copy(alpha = 0.6f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FFFE),
                            unfocusedContainerColor = Color(0xFFF8FFFE),
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = MedicalBorder
                        )
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotEmpty()) {
                                viewModel.sendChatMessage(messageText, "general")
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .background(Brush.horizontalGradient(listOf(PrimaryGreen, DarkGreen)), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (chatMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MedicalBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(72.dp).background(LightGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(34.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (lang) {
                            "uz" -> "AI Sog'liq maslahatchisi"
                            "ru" -> "AI консультант по здоровью"
                            else -> "AI Health Advisor"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (lang) {
                            "uz" -> "Sog'liq bilan bog'liq savolingizni yozing"
                            "ru" -> "Напишите свой вопрос о здоровье"
                            else -> "Ask any health-related question"
                        },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MedicalBackground)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(chatMessages) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) PrimaryGreen else Color.White
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            border = if (isUser) null else BorderStroke(1.dp, MedicalBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 0.dp else 1.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.content,
                                color = if (isUser) Color.White else TextPrimary,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
