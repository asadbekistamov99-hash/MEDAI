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

// --- SCREEN: REAL-TIME FAMILY MEMBERS MONITORING ---

@Composable
fun FamilyScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val family by viewModel.familyMembers.collectAsState()
    val immunizations by viewModel.immunizationRecords.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var inviteInput by remember { mutableStateOf("") }
    var selectedMemberForReminder by remember { mutableStateOf<FamilyMemberLocal?>(null) }
    var selectedMemberForDetail by remember { mutableStateOf<FamilyMemberLocal?>(null) }
    // The QR scanner used to be the centre button of the bottom bar; it lives here now, where
    // it is actually about the family.
    var showFamilyQr by remember { mutableStateOf(false) }

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

    if (showFamilyQr) {
        FamilyQrDialog(viewModel = viewModel, onDismiss = { showFamilyQr = false })
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = Translations.getString("family_title", lang),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showFamilyQr = true }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = Translations.getString("family_qr", lang),
                            tint = medai.brand
                        )
                    }
                }
            )
        }
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
                border = BorderStroke(1.dp, medai.border)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(medai.brandSoft, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👨‍👩‍👧‍👦", fontSize = 26.sp)
                    }
                    Column {
                        Text(
                            text = getLangText("Oilaviy sog'liq markazi", "Семейный центр здоровья", "Family Health Center"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = medai.textPrimary)
                        )
                        Text(
                            text = getLangText(
                                "Oila a'zolarini qo'shing, ularning salomatlik ko'rsatkichlarini kuzating va emlash taqvimlarini boshqaring.",
                                "Добавляйте членов семьи, следите за их здоровьем и управляйте календарем прививок.",
                                "Add family members, monitor shared health records, and track detailed immunization schedules."
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(color = medai.textSecondary)
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
                    colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
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
                    colors = ButtonDefaults.buttonColors(containerColor = medai.info.copy(alpha = 0.12f), contentColor = medai.info),
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
            Text(text = getLangText("Salomatlik Ogohlantirishlari ⚠️", "Предупреждения о здоровье ⚠️", "Health Alerts ⚠️"), fontWeight = FontWeight.Bold, color = medai.danger, fontSize = 14.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = medai.danger.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, medai.danger)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = medai.danger)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = getLangText(
                            "Diqqat! Jasur Karimovning salomatlik ko'rsatkichi 72% gacha tushib ketdi. Suv ichish eslatmasi yuboring!",
                            "Внимание! Показатель здоровья Джасура Каримова снизился до 72%. Отправьте напоминание о воде!",
                            "Warning! Jasur Karimov's health score fell to 72%. Send a water reminder!"
                        ),
                        fontSize = 12.sp,
                        color = medai.textPrimary
                    )
                }
            }

            // Family Weekly Summary
            Text(text = getLangText("Haftalik Salomatlik Hisoboti 📊", "Еженедельный отчет здоровья 📊", "Weekly Health Summary 📊"), fontWeight = FontWeight.Bold, color = medai.brand, fontSize = 14.sp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, medai.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = getLangText("Ushbu hafta oilaviy salomatlik juda yaxshi!", "На этой неделе здоровье семьи отличное!", "Family health is superb this week!"), fontWeight = FontWeight.Bold, color = medai.brand, fontSize = 13.sp)
                    Text(text = getLangText("• Suv ichish normasi bajarilishi: 94%", "• Норма воды выполнена на: 94%", "• Water goal completed: 94%"), fontSize = 12.sp, color = medai.textSecondary)
                    Text(text = getLangText("• Dorilarni o'z vaqtida ichish: 88%", "• Прием лекарств вовремя: 88%", "• Medication adherence: 88%"), fontSize = 12.sp, color = medai.textSecondary)
                    Text(text = getLangText("• O'rtacha oilaviy ball: 86%", "• Средний балл семьи: 86%", "• Average family score: 86%"), fontSize = 12.sp, color = medai.textSecondary)
                }
            }

            // Live monitoring list
            Text(
                text = Translations.getString("family_member_live", lang).uppercase(),
                fontWeight = FontWeight.Bold,
                color = medai.brand,
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
                        border = BorderStroke(1.dp, medai.border)
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
                                            .background(medai.brandSoft, CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = medai.brand, modifier = Modifier.fillMaxSize().padding(8.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = member.name, fontWeight = FontWeight.Bold, color = medai.textPrimary)
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
                                            color = medai.textSecondary
                                        )
                                        Text(text = "${Translations.getString("family_steps", lang)}: ${member.stepsToday}", fontSize = 11.sp, color = medai.brand)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(medai.brand.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "${member.healthScore}%", fontWeight = FontWeight.Black, color = medai.brand, fontSize = 13.sp)
                                    }
                                }
                            }

                            Divider(color = medai.border.copy(alpha = 0.08f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add reminder button
                                Button(
                                    onClick = { selectedMemberForReminder = member },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = medai.premium.copy(alpha = 0.12f), contentColor = medai.premium),
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
                                    colors = ButtonDefaults.buttonColors(containerColor = medai.brand.copy(alpha = 0.12f), contentColor = medai.brand),
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
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = medai.danger.copy(alpha = 0.7f))
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
                    colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
                ) {
                    Text(getLangText("Bog'lash", "Связать", "Link"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text(getLangText("Bekor qilish", "Отмена", "Cancel"), color = medai.textSecondary)
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
                                .background(medai.brand.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = medai.brand, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = getLangText("Oila a'zosi salomatlik profili", "Профиль здоровья члена семьи", "Family Health Profile"),
                                fontSize = 11.sp,
                                color = medai.textSecondary
                            )
                        }
                    }
                    IconButton(onClick = { selectedMemberForDetail = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = medai.textSecondary)
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
                        contentColor = medai.brand
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
                                    color = medai.textPrimary
                                )
                                TextButton(
                                    onClick = { showAddVaccineDialog = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = medai.brand)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(getLangText("Qo'shish", "Добавить", "Add New"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = medai.brand)
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
                                            color = medai.textSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                memberVaccines.forEach { vac ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (vac.status == "Completed") medai.success.copy(alpha = 0.05f) else medai.warning.copy(alpha = 0.06f)
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (vac.status == "Completed") medai.success.copy(alpha = 0.2f) else medai.border.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = vac.vaccineName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = medai.textPrimary)
                                                    Text(text = getLangText("Kasallik: ", "Болезнь: ", "Disease: ") + vac.targetDisease, fontSize = 11.sp, color = medai.textSecondary)
                                                }
                                                // Status Badge
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (vac.status == "Completed") medai.success.copy(alpha = 0.15f) else medai.warning.copy(alpha = 0.15f)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (vac.status == "Completed") getLangText("Emlangan", "Привит", "Completed") else getLangText("Kutilmoqda", "Ожидает", "Pending"),
                                                        color = if (vac.status == "Completed") medai.success else medai.warning,
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
                                                    Text(text = getLangText("Muddat: ", "Срок: ", "Age: ") + vac.scheduledAge, fontSize = 11.sp, color = medai.textSecondary)
                                                    Text(text = getLangText("Sana: ", "Дата: ", "Date: ") + vac.dueDate, fontSize = 11.sp, color = medai.textSecondary)
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
                                                            colors = ButtonDefaults.buttonColors(containerColor = medai.success),
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
                                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete vaccine", tint = medai.danger.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }

                                            if (vac.status == "Completed") {
                                                Divider(color = medai.success.copy(alpha = 0.1f))
                                                Text(
                                                    text = "✅ " + getLangText("Emlandi: ", "Введено: ", "Administered: ") + "${vac.completedDate ?: "Yaqinda"}" +
                                                            (vac.administeredBy?.let { " | $it" } ?: ""),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = medai.success
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
                                color = medai.textPrimary
                            )

                            // Blood Pressure Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = medai.brandSoft.copy(alpha = 0.5f))
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
                                            Text(getLangText("Normal ko'rsatkich: 120/80 mm sim.ust", "Норма: 120/80 мм рт.ст.", "Normal: 120/80 mmHg"), fontSize = 11.sp, color = medai.textSecondary)
                                        }
                                    }
                                    Text(
                                        text = "$vitalSys/$vitalDia",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (vitalSys.toIntOrNull() ?: 120 > 135) medai.danger else medai.success
                                    )
                                }
                            }

                            // Heart Rate Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = medai.brandSoft.copy(alpha = 0.5f))
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
                                            Text(getLangText("Normal: daqiqasiga 60-90 marta", "Норма: 60-90 уд/мин", "Normal: 60-90 bpm"), fontSize = 11.sp, color = medai.textSecondary)
                                        }
                                    }
                                    Text(
                                        text = "$vitalHr bpm",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = medai.success
                                    )
                                }
                            }

                            // Activity steps
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = medai.brandSoft.copy(alpha = 0.5f))
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
                                            Text(getLangText("Kunlik maqsad: 10,000 qadam", "Цель: 10,000 шагов", "Target: 10,000 steps"), fontSize = 11.sp, color = medai.textSecondary)
                                        }
                                    }
                                    Text(
                                        text = "${member.stepsToday} / 10,000",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = medai.brand
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Vitals Logger Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = medai.info.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, medai.info.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = getLangText("Yangi ko'rsatkichlarni kiritish ✍️", "Записать новые показатели ✍️", "Log New Vital Readings ✍️"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = medai.info
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
                                                    viewModel.updateFamilyMember(updatedMember)
                                                }
                                                Toast.makeText(viewModel.getApplication(), getLangText("Ko'rsatkichlar muvaffaqiyatli saqlandi!", "Показатели успешно сохранены!", "Vital readings saved successfully!"), Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = medai.info),
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
                    Text(getLangText("Yopish", "Закрыть", "Close"), fontWeight = FontWeight.Bold, color = medai.brand)
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
                        Text(text = vac.vaccineName, fontWeight = FontWeight.Bold, color = medai.brand)
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
                        colors = ButtonDefaults.buttonColors(containerColor = medai.success)
                    ) {
                        Text(getLangText("Tasdiqlash", "Подтвердить", "Confirm"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { completedVaccineRecord = null }) {
                        Text(getLangText("Bekor qilish", "Отмена", "Cancel"), color = medai.textSecondary)
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
                        colors = ButtonDefaults.buttonColors(containerColor = medai.brand)
                    ) {
                        Text(getLangText("Qo'shish", "Добавить", "Add"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddVaccineDialog = false }) {
                        Text(getLangText("Bekor qilish", "Отмена", "Cancel"), color = medai.textSecondary)
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
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = medai.premium)
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
                    colors = ButtonDefaults.buttonColors(containerColor = medai.premium)
                ) {
                    Text(getLangText("Biriktirish", "Привязать", "Assign"))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMemberForReminder = null }) {
                    Text(getLangText("Bekor qilish", "Отмена", "Cancel"), color = medai.textSecondary)
                }
            }
        )
    }
}
