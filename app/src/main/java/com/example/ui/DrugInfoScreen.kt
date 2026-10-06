@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Translations
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.ui.theme.*
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

// --- SCREEN: DRUG INFORMATION ---

@Composable
fun DrugInfoScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val drugResult by viewModel.drugInfoResult.collectAsState()
    val isLoading by viewModel.isLoadingDrug.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Prescription Scanner state
    var showScannerDialog by remember { mutableStateOf(false) }
    val isScanningPrescription by viewModel.isScanningPrescription.collectAsState()
    val prescriptionScanResult by viewModel.prescriptionScanResult.collectAsState()

    // Drug Interaction state
    var showInteractionDialog by remember { mutableStateOf(false) }
    val isCheckingInteractions by viewModel.isCheckingInteractions.collectAsState()
    val interactionResult by viewModel.interactionResult.collectAsState()
    var drugInputs by remember { mutableStateOf(listOf("", "")) }

    // Tiny valid 1x1 base64 JPEG image to make actual multi-modal API calls
    val mockPrescriptionBase64 = "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA="

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("feat_med_info", lang), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(medai.canvas)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Translations.getString("drug_search_placeholder", lang), color = medai.textSecondary.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = medai.textPrimary, fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8FFFE),
                    unfocusedContainerColor = Color(0xFFF8FFFE),
                    focusedBorderColor = medai.info,
                    unfocusedBorderColor = medai.border,
                    focusedTextColor = medai.textPrimary,
                    unfocusedTextColor = medai.textPrimary
                ),
                trailingIcon = {
                    IconButton(onClick = { viewModel.searchDrugInfo(searchQuery) }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = medai.info)
                    }
                }
            )

            // Search History & Quick Tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Qidiruv tarixi".uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = medai.textSecondary,
                    letterSpacing = 1.sp
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Prescription Scanner Trigger
                    IconButton(
                        onClick = { showScannerDialog = true },
                        modifier = Modifier
                            .background(medai.brand.copy(alpha = 0.15f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Scan Prescription",
                            tint = medai.brand,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Interaction Checker Trigger
                    IconButton(
                        onClick = { showInteractionDialog = true },
                        modifier = Modifier
                            .background(medai.premium.copy(alpha = 0.15f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Check Interactions",
                            tint = medai.premium,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Paracetamol", "Ibuprofen", "Aspirin").forEach { item ->
                    FilterChip(
                        selected = searchQuery == item,
                        onClick = {
                            searchQuery = item
                            viewModel.searchDrugInfo(item)
                        },
                        label = { Text(item) }
                    )
                }
            }

            // Allergy Filter Card for Search Result
            if (drugResult != null) {
                val matchedAllergies = remember(drugResult) {
                    val allText = searchQuery + " " + (drugResult?.values?.joinToString(" ") ?: "")
                    viewModel.checkMedicinesForAllergies(allText)
                }

                if (matchedAllergies.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = medai.danger.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, medai.danger.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = medai.danger)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "uz") "❌ Bu dori sizga mos emas — ${matchedAllergies.joinToString(", ")} allergiyangiz bor"
                                       else if (lang == "ru") "❌ Это лекарство вам не подходит — у вас аллергия на ${matchedAllergies.joinToString(", ")}"
                                       else "❌ This medicine is not suitable for you — you have allergy to ${matchedAllergies.joinToString(", ")}",
                                fontSize = 12.sp,
                                color = medai.danger,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = medai.success.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, medai.success.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = medai.success)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "uz") "✅ Bu dori allergiyangiz bilan mos keladi"
                                       else if (lang == "ru") "✅ Это лекарство совместимо с вашей аллергией"
                                       else "✅ This medicine is compatible with your allergies",
                                fontSize = 12.sp,
                                color = medai.success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = medai.info)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = when (lang) {
                                "uz" -> "Ma'lumot izlanmoqda..."
                                "ru" -> "Идёт поиск..."
                                else -> "Searching..."
                            },
                            fontSize = 12.sp,
                            color = medai.textSecondary
                        )
                    }
                }
            } else if (drugResult != null) {
                drugResult?.forEach { (key, value) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = medai.surface),
                        border = BorderStroke(1.dp, medai.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(medai.info, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = key, fontWeight = FontWeight.Bold, color = medai.info, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = value, fontSize = 13.sp, color = medai.textPrimary, lineHeight = 19.sp)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 1: PRESCRIPTION SCANNER ---
    if (showScannerDialog) {
        AlertDialog(
            onDismissRequest = { showScannerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = medai.brand)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (lang == "uz") "Retseptni skanerlash" else if (lang == "ru") "Сканирование рецепта" else "Scan Prescription", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (lang == "uz") "Skanerlashni simulyatsiya qilish uchun retsept turlardan birini tanlang va yuboring."
                               else "Выберите тип рецепта для симуляции сканирования.",
                        fontSize = 13.sp,
                        color = medai.textSecondary
                    )

                    val presets = listOf(
                        "Kardiolog retsepti (Aspirin, Lisinopril)" to "Cardiologist Prescription: Aspirin 75mg daily, Lisinopril 10mg daily morning. Please analyze usage and dosage.",
                        "Terapevt retsepti (Amoxicillin, Paracetamol)" to "General Physician: Amoxicillin 500mg three times daily, Paracetamol 500mg as needed for pain. Analyze.",
                        "Nevrolog retsepti (Magniy B6, Glycine)" to "Neurologist: Magnesium B6 two tablets evening, Glycine three times daily under tongue. Analyze."
                    )

                    presets.forEach { (title, prompt) ->
                        Button(
                            onClick = {
                                viewModel.scanPrescriptionImage(mockPrescriptionBase64)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = medai.brandSoft, contentColor = medai.brand),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = title, color = medai.brand, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (isScanningPrescription) {
                        Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = medai.brand)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Skanerlanmoqda...", fontSize = 12.sp, color = medai.textSecondary)
                            }
                        }
                    } else if (prescriptionScanResult.isNotEmpty()) {
                        Divider(color = medai.border)
                        Text(
                            text = "Natija:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = medai.brand
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(text = prescriptionScanResult, fontSize = 12.sp, color = medai.textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showScannerDialog = false }) {
                    Text(text = "Yopish", color = medai.brand)
                }
            }
        )
    }

    // --- DIALOG 2: DRUG INTERACTION CHECKER ---
    if (showInteractionDialog) {
        AlertDialog(
            onDismissRequest = { showInteractionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CompareArrows, contentDescription = null, tint = medai.premium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (lang == "uz") "Dori o'zaro ta'siri" else if (lang == "ru") "Взаимодействие лекарств" else "Drug Interactions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (lang == "uz") "Tekshirish uchun 2 dan 5 tagacha dori nomini yozing:"
                               else "Введите от 2 до 5 лекарств для проверки:",
                        fontSize = 13.sp,
                        color = medai.textSecondary
                    )

                    drugInputs.forEachIndexed { idx, value ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = { newVal ->
                                val list = drugInputs.toMutableList()
                                list[idx] = newVal
                                drugInputs = list
                            },
                            placeholder = { Text("Dori ${idx + 1}") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    if (drugInputs.size < 5) {
                        TextButton(
                            onClick = { drugInputs = drugInputs + "" },
                            colors = ButtonDefaults.textButtonColors(contentColor = medai.premium)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dori qo'shish")
                        }
                    }

                    Button(
                        onClick = { viewModel.checkDrugInteractions(drugInputs) },
                        enabled = drugInputs.count { it.isNotBlank() } >= 2 && !isCheckingInteractions,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = medai.premium),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isCheckingInteractions) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("O'zaro ta'sirni tekshirish")
                        }
                    }

                    if (interactionResult.isNotEmpty()) {
                        Divider(color = medai.border)

                        // Parse status marker
                        val isSafe = interactionResult.contains("STATUS: SAFE")
                        val isCaution = interactionResult.contains("STATUS: CAUTION")
                        val isDangerous = interactionResult.contains("STATUS: DANGEROUS")

                        val (statusText, statusColor) = when {
                            isSafe -> "✅ Xavfsiz (Safe)" to medai.brand
                            isCaution -> "⚠️ Ehtiyot bo'ling (Caution)" to medai.warning
                            isDangerous -> "❌ Birga ichmang! (Dangerous)" to medai.danger
                            else -> "Natija" to medai.textPrimary
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, statusColor)
                        ) {
                            Text(
                                text = statusText,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = interactionResult
                                    .replace("STATUS: SAFE", "")
                                    .replace("STATUS: CAUTION", "")
                                    .replace("STATUS: DANGEROUS", "")
                                    .trim(),
                                fontSize = 12.sp,
                                color = medai.textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInteractionDialog = false }) {
                    Text(text = "Yopish", color = medai.premium)
                }
            }
        )
    }
}
