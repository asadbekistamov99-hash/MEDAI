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

// --- SCREEN: AI DOCTOR CHAT ---

@Composable
fun AIDoctorScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val chatMessages by viewModel.doctorChatMessages().collectAsState(initial = emptyList())

    var messageText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppHeader(
                title = Translations.getString("feat_ai_doctor", lang),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.clearChatHistory("doctor") }) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Clear Chat", tint = medai.textSecondary)
                    }
                }
            )
        },
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
                        placeholder = { Text("Dori, kasallik yoki tahlil haqida so'rang...", color = medai.textSecondary.copy(alpha = 0.6f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = medai.textPrimary, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FFFE),
                            unfocusedContainerColor = Color(0xFFF8FFFE),
                            focusedBorderColor = medai.premium,
                            unfocusedBorderColor = medai.border
                        )
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotEmpty()) {
                                if (viewModel.sendChatMessage(messageText, "doctor")) {
                                    messageText = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .background(Brush.horizontalGradient(listOf(medai.premium, Color(0xFF5E35B1))), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(medai.canvas)
        ) {
            // Quick Chat Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Translations.getString("chat_chip_med", lang),
                    Translations.getString("chat_chip_disease", lang),
                    Translations.getString("chat_chip_lab", lang),
                    Translations.getString("chat_chip_diet", lang)
                ).forEach { chip ->
                    FilterChip(
                        selected = false,
                        onClick = { messageText = chip },
                        label = { Text(chip) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = medai.surface,
                            labelColor = medai.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = medai.border
                        )
                    )
                }
            }

            if (chatMessages.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(72.dp).background(medai.premiumSoft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = medai.premium, modifier = Modifier.size(34.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (lang) {
                                "uz" -> "AI Shifokoringiz tinglashga tayyor"
                                "ru" -> "Ваш AI-врач готов вас выслушать"
                                else -> "Your AI Doctor is ready to help"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = medai.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (lang) {
                                "uz" -> "Savolingizni yozing yoki yuqoridagi tugmalardan tanlang"
                                "ru" -> "Напишите вопрос или выберите один из чипов выше"
                                else -> "Type your question or pick a chip above"
                            },
                            fontSize = 12.sp,
                            color = medai.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
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
                                    containerColor = if (isUser) medai.premium else Color.White
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                border = if (isUser) null else BorderStroke(1.dp, medai.border),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 0.dp else 1.dp),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = msg.content,
                                    color = if (isUser) Color.White else medai.textPrimary,
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
}
