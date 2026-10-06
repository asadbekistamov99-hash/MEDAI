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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.selection.selectable
import com.example.data.*
import com.example.i18n.Translations
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

// --- SCREEN: PROFILE ---

/** Achievement id, emoji, title key, description key. The ids are what the ViewModel stores. */
private val ProfileAchievements = listOf(
    listOf("first_reminder", "💊", "ach_first_title", "ach_first_desc"),
    listOf("water_champ", "🥤", "ach_water_title", "ach_water_desc"),
    listOf("ai_pioneer", "🧠", "ach_ai_title", "ach_ai_desc"),
)

// Allergy and document types are persisted as these Uzbek tokens; only their labels are localised.
private val AllergyTypes = listOf("Dori", "Taom", "Boshqa")
private val DocumentTypes = listOf("Analiz", "Rentgen", "Retsept")

private const val MOCK_DOC_BASE64 =
    "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA="

@Composable
fun ProfileScreen(viewModel: AppViewModel, navController: NavController) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isSuperAdmin = user?.email?.trim()?.equals(SUPER_ADMIN_EMAIL, ignoreCase = true) == true
    val medicalDocs by viewModel.medicalDocuments.collectAsState()
    val context = LocalContext.current
    fun t(key: String) = Translations.getString(key, lang)

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Allergy form state
    var allergyName by remember { mutableStateOf("") }
    var allergyTypeSelected by remember { mutableStateOf("Dori") }

    // Document upload state
    var showDocUploadDialog by remember { mutableStateOf(false) }
    var docTitle by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("Analiz") }

    val unlockedAchievementsSet = remember(user) {
        val set = mutableSetOf<String>()
        user?.let { u ->
            try {
                val arr = org.json.JSONArray(u.unlockedAchievementsJson)
                for (i in 0 until arr.length()) {
                    set.add(arr.getString(i))
                }
            } catch (e: Exception) {}
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
            } catch (e: Exception) {}
        }
        list
    }

    fun allergyTypeLabel(stored: String) = when (stored) {
        "Dori" -> t("type_med")
        "Taom" -> t("type_food")
        "Boshqa" -> t("type_other")
        else -> stored
    }
    fun docTypeLabel(stored: String) = when (stored) {
        "Analiz" -> t("doc_type_analysis")
        "Rentgen" -> t("doc_type_xray")
        "Retsept" -> t("doc_type_rx")
        else -> stored
    }

    val isPremium = user?.hasPremiumAccess ?: false
    val isPaid = user?.isPremium == true
    val streakDays = user?.healthScore?.let { (it / 15).coerceAtLeast(1) } ?: 5

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title + notifications
        item(key = "title") {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = t("tab_profile"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(MinTouch)
                        .clip(CircleShape)
                        .background(c.surface)
                        .border(1.dp, c.border, CircleShape)
                        .clickable(role = Role.Button) { navController.navigate("notifications") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = t("feat_notifications"), tint = c.textPrimary)
                }
            }
        }

        // Identity
        item(key = "identity") {
            MedAICard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val tint = if (isPremium) c.tintViolet else c.tintTeal
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(tint.bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.name?.firstOrNull()?.toString()?.uppercase() ?: "U",
                            style = MedAIText.MetricMedium,
                            color = tint.fg
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = user?.name ?: "—",
                            style = MaterialTheme.typography.titleLarge,
                            color = c.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = user?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        when {
                            isPaid -> MedAIBadge(t("badge_premium"), MedAIBadgeTone.Premium, icon = Icons.Default.WorkspacePremium)
                            isPremium -> MedAIBadge(t("badge_trial"), MedAIBadgeTone.Premium, icon = Icons.Default.WorkspacePremium)
                            else -> MedAIBadge(t("plan_free"), MedAIBadgeTone.Brand, icon = Icons.Default.Shield)
                        }
                    }
                }
            }
        }

        // Super admin entry (only for SUPER_ADMIN_EMAIL)
        if (isSuperAdmin) {
            item(key = "admin") {
                MedAICard(Modifier.fillMaxWidth(), onClick = { navController.navigate("admin") }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(c.tintPeach.bg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = c.tintPeach.fg, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(t("profile_admin_title"), style = MaterialTheme.typography.titleSmall, color = c.textPrimary, modifier = Modifier.weight(1f, fill = false))
                                Spacer(Modifier.width(8.dp))
                                MedAIBadge("SUPER", MedAIBadgeTone.Warning)
                            }
                            Text(t("profile_admin_sub"), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = c.textSecondary)
                    }
                }
            }
        }

        // Body measurements
        item(key = "body") {
            MedAICard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    ProfileStat(Modifier.weight(1f), Icons.Default.Height, "${user?.height ?: 175.0} ${t("unit_cm")}", t("profile_height"), c.tintTeal)
                    ProfileStat(Modifier.weight(1f), Icons.Default.MonitorWeight, "${user?.weight ?: 70.0} ${t("unit_kg")}", t("profile_weight"), c.tintSky)
                    ProfileStat(Modifier.weight(1f), Icons.Default.Bloodtype, user?.bloodType?.ifBlank { "—" } ?: "—", t("blood_type_label"), c.tintPeach)
                }
            }
        }

        // Streak
        item(key = "streak") {
            val shape = RoundedCornerShape(MedAICorners.card)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(c.warningSoft)
                    .border(1.dp, c.warning.copy(alpha = 0.3f), shape)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", fontSize = 28.sp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(t("streak_title").replace("{n}", streakDays.toString()), style = MaterialTheme.typography.titleSmall, color = c.onWarningSoft)
                    Text(t("streak_desc"), style = MaterialTheme.typography.bodySmall, color = c.onWarningSoft)
                }
            }
        }

        // Allergies
        item(key = "allergies") {
            MedAICard(Modifier.fillMaxWidth()) {
                ProfileSectionTitle(Icons.Default.Warning, t("allergy_title"), c.tintPeach)
                Spacer(Modifier.height(12.dp))
                if (allergiesList.isEmpty()) {
                    Text(t("allergy_empty"), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allergiesList.forEach { (name, type) ->
                            val chipShape = RoundedCornerShape(MedAICorners.pill)
                            Row(
                                modifier = Modifier
                                    .heightIn(min = MinTouch)
                                    .clip(chipShape)
                                    .background(c.dangerSoft)
                                    .border(1.dp, c.danger.copy(alpha = 0.3f), chipShape)
                                    .padding(start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$name · ${allergyTypeLabel(type)}", style = MaterialTheme.typography.labelLarge, color = c.onDangerSoft)
                                Box(
                                    modifier = Modifier
                                        .size(MinTouch)
                                        .clip(CircleShape)
                                        .clickable(role = Role.Button) { viewModel.deleteAllergy(name) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "${t("allergy_remove")}: $name", tint = c.onDangerSoft, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                MedAITextField(
                    value = allergyName,
                    onValueChange = { allergyName = it },
                    label = t("allergy_name_label"),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = t("allergy_name_ph"),
                )
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AllergyTypes.forEach { type ->
                        MedAIFilterChip(allergyTypeLabel(type), allergyTypeSelected == type, { allergyTypeSelected = type })
                    }
                }
                Spacer(Modifier.height(8.dp))
                MedAISecondaryButton(
                    text = t("allergy_add"),
                    icon = Icons.Default.Add,
                    enabled = allergyName.isNotBlank(),
                    onClick = {
                        if (allergyName.isNotBlank()) {
                            viewModel.addAllergy(allergyName, allergyTypeSelected)
                            allergyName = ""
                            Toast.makeText(viewModel.getApplication(), t("allergy_added"), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Medical documents
        item(key = "docs") {
            MedAICard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileSectionTitle(Icons.Default.Folder, t("docs_title"), c.tintViolet, Modifier.weight(1f))
                    MedAIBadge(t("badge_premium"), MedAIBadgeTone.Premium)
                }
                Spacer(Modifier.height(12.dp))
                if (medicalDocs.isEmpty()) {
                    Text(t("docs_empty"), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                } else {
                    Column {
                        medicalDocs.forEach { doc ->
                            Row(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(c.tintViolet.bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = c.tintViolet.fg, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(doc.title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(docTypeLabel(doc.docType), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(MinTouch)
                                        .clip(CircleShape)
                                        .clickable(role = Role.Button) { viewModel.deleteMedicalDocument(doc.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "${t("docs_delete")}: ${doc.title}", tint = c.danger)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                MedAISecondaryButton(
                    text = t("docs_upload"),
                    icon = Icons.Default.CloudUpload,
                    onClick = { showDocUploadDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Achievements
        item(key = "achievements") {
            Column {
                Text(t("ach_title"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                Spacer(Modifier.height(12.dp))
                MedAICard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                    ProfileAchievements.forEachIndexed { index, ach ->
                        val unlocked = unlockedAchievementsSet.contains(ach[0])
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (unlocked) c.brandSoft else c.surfaceSunken),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(ach[1], fontSize = 20.sp, modifier = Modifier.alpha(if (unlocked) 1f else 0.5f))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t(ach[2]), style = MaterialTheme.typography.titleSmall, color = if (unlocked) c.textPrimary else c.textSecondary)
                                Text(t(ach[3]), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                            Spacer(Modifier.width(8.dp))
                            if (unlocked) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = c.success)
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (index != ProfileAchievements.lastIndex) {
                            Box(Modifier.padding(start = 74.dp).fillMaxWidth().height(1.dp).background(c.divider))
                        }
                    }
                }
            }
        }

        // Language
        item(key = "language") {
            MedAICard(Modifier.fillMaxWidth()) {
                Text(t("select_language"), style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("uz" to "🇺🇿 UZ", "ru" to "🇷🇺 RU", "en" to "🇬🇧 EN").forEach { (code, label) ->
                        val selected = lang == code
                        val optionShape = RoundedCornerShape(MedAICorners.control)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = MinTouch)
                                .clip(optionShape)
                                .background(if (selected) c.brandSoft else c.surface)
                                .border(if (selected) 2.dp else 1.dp, if (selected) c.brand else c.borderStrong, optionShape)
                                .selectable(selected = selected, role = Role.RadioButton, onClick = { viewModel.setLanguage(code) }),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) c.onBrandSoft else c.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Links
        item(key = "links") {
            MedAICard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                MedAIListRow(
                    icon = Icons.Default.HelpCenter,
                    title = t("feat_help"),
                    onClick = { navController.navigate("help") },
                )
                MedAIListRow(
                    icon = Icons.Default.PrivacyTip,
                    title = t("profile_privacy"),
                    tint = c.tintSky,
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://claude.ai/code/artifact/d55f2334-c8df-41ef-b060-65046cfa7965")
                        )
                        context.startActivity(intent)
                    },
                )
                MedAIListRow(
                    icon = Icons.Default.ExitToApp,
                    title = t("sign_out"),
                    tint = MedAITint(c.dangerSoft, c.onDangerSoft),
                    showDivider = false,
                    onClick = { showLogoutDialog = true },
                )
            }
        }
    }

    if (showLogoutDialog) {
        MedAIDialog(
            title = t("sign_out"),
            message = t("sign_out_confirm"),
            confirmText = t("confirm"),
            onConfirm = {
                viewModel.logout()
                showLogoutDialog = false
            },
            onDismissRequest = { showLogoutDialog = false },
            dismissText = t("cancel"),
            destructive = true,
            icon = Icons.Default.ExitToApp,
        )
    }

    // Document upload dialog
    if (showDocUploadDialog) {
        val dialogShape = RoundedCornerShape(MedAICorners.sheet)
        Dialog(onDismissRequest = { showDocUploadDialog = false }) {
            Column(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .shadow(MedAIElevation.floating, dialogShape)
                    .clip(dialogShape)
                    .background(c.surfaceRaised)
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(c.tintViolet.bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = c.tintViolet.fg)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(t("docs_upload_title"), style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
                }
                Spacer(Modifier.height(16.dp))
                MedAITextField(
                    value = docTitle,
                    onValueChange = { docTitle = it },
                    label = t("docs_name_label"),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = t("docs_name_ph"),
                )
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DocumentTypes.forEach { type ->
                        MedAIFilterChip(docTypeLabel(type), docType == type, { docType = type })
                    }
                }
                Spacer(Modifier.height(16.dp))
                MedAIPrimaryButton(
                    text = t("docs_upload"),
                    enabled = docTitle.isNotBlank(),
                    onClick = {
                        if (docTitle.isNotBlank()) {
                            viewModel.addMedicalDocument(docTitle, docType, MOCK_DOC_BASE64)
                            docTitle = ""
                            showDocUploadDialog = false
                            Toast.makeText(viewModel.getApplication(), t("docs_saved"), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                MedAITextButton(
                    text = t("cancel"),
                    onClick = { showDocUploadDialog = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionTitle(icon: ImageVector, title: String, tint: MedAITint, modifier: Modifier = Modifier) {
    val c = MedAITheme.colors
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(tint.bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
    }
}

@Composable
private fun ProfileStat(modifier: Modifier, icon: ImageVector, value: String, label: String, tint: MedAITint) {
    val c = MedAITheme.colors
    Column(modifier.padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(tint.bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint.fg, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textSecondary, maxLines = 2, textAlign = TextAlign.Center)
    }
}
