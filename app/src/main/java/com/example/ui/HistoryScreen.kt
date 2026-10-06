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

// --- SCREEN: HISTORIC TIMELINE LOG ---

@Composable
fun HistoryScreen(viewModel: AppViewModel) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val checks by viewModel.symptomChecks.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppHeader(title = Translations.getString("tab_history", lang)) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(medai.canvas)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(medai.brand, medai.brandStrong)))
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .offset(x = 260.dp, y = (-50).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.White.copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tibbiy Qidiruv va Tahlillar Tarixi",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bu yerda siz o'tkazgan barcha simptom tekshiruvlari, AI tahlillari va salomatlik xulosalaringiz xronologik tartibda saqlanadi.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (checks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(72.dp).background(medai.brand.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(32.dp), tint = medai.brand)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Tarix bo'sh", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = medai.textPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Qidiruv natijalari bu yerda saqlanadi.",
                                fontSize = 12.sp,
                                color = medai.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            } else {
                items(checks) { check ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = medai.surface),
                        border = BorderStroke(1.dp, medai.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier.size(40.dp).background(medai.brand.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = medai.brand, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = "Simptom tekshiruvi: ${check.bodyPart}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = medai.textPrimary)
                                        Text(
                                            text = "Kiritilgan simptomlar: ${check.symptomsInput}",
                                            fontSize = 12.sp,
                                            color = medai.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.deleteSymptomCheck(check.id)
                                    },
                                    modifier = Modifier
                                        .background(medai.danger.copy(alpha = 0.08f), CircleShape)
                                        .size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "O'chirish", tint = medai.danger, modifier = Modifier.size(16.dp))
                                }
                            }
                            Divider(color = medai.border)
                            Text(text = check.resultJson, fontSize = 12.sp, color = medai.textSecondary, lineHeight = 16.sp, maxLines = 4)
                        }
                    }
                }
            }
        }
    }
}
