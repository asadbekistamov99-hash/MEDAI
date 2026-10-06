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

// --- SCREEN: GENERAL CHAT ---

@Composable
fun GeneralChatScreen(
    viewModel: AppViewModel,
    onNavigateToUpgrade: () -> Unit = {}
) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val chatMessages by viewModel.generalChatMessages().collectAsState(initial = emptyList())
    val hasPremium by viewModel.hasPremiumAccess.collectAsState()
    var messageText by remember { mutableStateOf("") }

    // The free daily quota. Shown up front rather than only as a refusal on send, so the user
    // knows the limit exists and has the upgrade path in view before they hit it.
    val freeQuota by viewModel.freeChatQuota.collectAsState()

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
                        placeholder = { Text("AI Sog'liq maslahatchisidan so'rang...", color = medai.textSecondary.copy(alpha = 0.6f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = medai.textPrimary, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FFFE),
                            unfocusedContainerColor = Color(0xFFF8FFFE),
                            focusedBorderColor = medai.brand,
                            unfocusedBorderColor = medai.border
                        )
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotEmpty()) {
                                // Only clear the field once the message is actually accepted;
                                // when the quota is spent sendChatMessage routes to the paywall
                                // and the typed text should survive so nothing is lost.
                                val accepted = viewModel.sendChatMessage(
                                    message = messageText,
                                    chatType = "general",
                                    onUpgradeRequired = onNavigateToUpgrade
                                )
                                if (accepted) messageText = ""
                            }
                        },
                        modifier = Modifier
                            .background(Brush.horizontalGradient(listOf(medai.brand, medai.brandStrong)), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (chatMessages.isEmpty()) {
            val quickQuestions = listOf(
                "💊" to "Qaysi dorilarni birga ichish xavfli?",
                "💧" to "Kuniga qancha suv ichish tavsiya qilinadi?",
                "🩸" to "Qon bosimini tabiiy tushirish yo'llari",
                "🤕" to "Bosh og'rig'i va charchoq sabablari nima?",
                "🍅" to "Immunitetni oshirish uchun qanday taomlar kerak?"
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(medai.canvas)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Brush.linearGradient(listOf(medai.brandSoft, Color(0xFFE6F6F4))))
                            .border(1.dp, medai.brand.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
                            .padding(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .shadow(4.dp, CircleShape)
                                    .background(Brush.linearGradient(listOf(medai.brand, medai.brandStrong)), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(medai.success, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (lang) {
                                        "uz" -> "AI Tibbiy Maslahatchi • 24/7 Onlayn"
                                        "ru" -> "AI консультант • 24/7 онлайн"
                                        else -> "AI Health Advisor • 24/7 Online"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = medai.brandStrong
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (lang) {
                                    "uz" -> "Salom! Sizga qanday yordam bera olaman?"
                                    "ru" -> "Привет! Чем я могу вам помочь?"
                                    else -> "Hi! How can I help you today?"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = medai.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (lang) {
                                    "uz" -> "Sog'liq, alomatlar, to'g'ri ovqatlanish yoki tahlil natijalari bo'yicha savollaringizni bering."
                                    "ru" -> "Задайте вопрос о здоровье, симптомах, питании или результатах анализов."
                                    else -> "Ask about symptoms, nutrition, or your lab results."
                                },
                                fontSize = 12.5.sp,
                                color = medai.textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                item {
                    // horizontalPadding = 0: this list already pads its own content by 16dp, and
                    // the header's default 20dp would leave this row hanging out of alignment.
                    MedAISectionHeader(
                        title = "Tezkor savollar",
                        subtitle = "Bir bosishda so'rang",
                        horizontalPadding = 0.dp,
                    )
                }

                items(quickQuestions) { (emoji, question) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(medai.surface)
                            .border(1.dp, medai.divider, RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.sendChatMessage(
                                    message = question,
                                    chatType = "general",
                                    onUpgradeRequired = onNavigateToUpgrade
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = emoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = question,
                            fontSize = 13.5.sp,
                            color = medai.textPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = medai.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(medai.canvas)
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
                                containerColor = if (isUser) medai.brand else Color.White
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
