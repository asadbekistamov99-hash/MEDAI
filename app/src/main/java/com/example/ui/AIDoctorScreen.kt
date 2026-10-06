package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.data.ChatMessageLocal
import com.example.i18n.Translations
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MinTouch

// --- SCREEN: AI DOCTOR CHAT ---

@Composable
fun AIDoctorScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val chatMessages by viewModel.doctorChatMessages().collectAsState(initial = emptyList())

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // The doctor replies after the user's message lands; while the newest message is the user's
    // (and fresh) show a "typing" row. Derived from the existing message flow, no new state source.
    val last = chatMessages.lastOrNull()
    val waitingForReply = remember(last?.id, last?.role) {
        last != null && last.role == "user" && System.currentTimeMillis() - last.timestamp < 90_000L
    }

    LaunchedEffect(chatMessages.size, waitingForReply) {
        val count = chatMessages.size + if (waitingForReply) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    Scaffold(
        containerColor = c.canvas,
        topBar = {
            AppHeader(
                title = Translations.getString("feat_ai_doctor", lang),
                onBack = onBack,
                actions = {
                    MedIconButton(
                        Icons.Default.DeleteOutline, medText(lang, "Suhbatni tozalash", "Очистить чат", "Clear chat"),
                        tint = c.textSecondary, onClick = { viewModel.clearChatHistory("doctor") },
                    )
                },
            )
        },
        bottomBar = {
            Column(Modifier.background(c.surface)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DoctorInput(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = medText(
                            lang,
                            "Dori, kasallik yoki tahlil haqida so'rang...",
                            "Спросите о лекарстве, болезни или анализах...",
                            "Ask about a medicine, illness or test...",
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    val canSend = messageText.isNotEmpty()
                    Box(
                        modifier = Modifier
                            .size(MinTouch)
                            .clip(CircleShape)
                            .background(if (canSend) c.brand else c.surfaceSunken)
                            .clickable(role = Role.Button) {
                                if (messageText.isNotEmpty()) {
                                    if (viewModel.sendChatMessage(messageText, "doctor")) {
                                        messageText = ""
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = medText(lang, "Yuborish", "Отправить", "Send"),
                            tint = if (canSend) c.onBrand else c.textSecondary,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            // Slim disclaimer
            MedAIInfoBanner(
                text = Translations.getString("disclaimer_desc", lang),
                tone = MedAITone.Warning,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
            )

            // Quick prompts: fill the input, do not send.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    Translations.getString("chat_chip_med", lang),
                    Translations.getString("chat_chip_disease", lang),
                    Translations.getString("chat_chip_lab", lang),
                    Translations.getString("chat_chip_diet", lang),
                ).forEach { chip ->
                    MedAIFilterChip(text = chip, selected = false, onClick = { messageText = chip })
                }
            }

            if (chatMessages.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    MedAIEmptyState(
                        title = medText(lang, "AI Shifokoringiz tinglashga tayyor", "Ваш AI-врач готов вас выслушать", "Your AI doctor is ready to help"),
                        message = medText(
                            lang,
                            "Savolingizni yozing yoki yuqoridagi tugmalardan tanlang",
                            "Напишите вопрос или выберите одну из подсказок выше",
                            "Type your question or pick a suggestion above",
                        ),
                        icon = Icons.Default.SmartToy,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    items(chatMessages, key = { it.id }) { msg -> DoctorBubble(msg) }
                    if (waitingForReply) {
                        item(key = "typing") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MedAiAvatar()
                                Spacer(Modifier.width(8.dp))
                                MedAISpinner(size = 20.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    medText(lang, "Javob yozilmoqda...", "Печатает ответ...", "Writing a reply..."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorBubble(msg: ChatMessageLocal) {
    val c = MedAITheme.colors
    val isUser = msg.role == "user"
    val shape = RoundedCornerShape(
        topStart = 18.dp, topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!isUser) {
            MedAiAvatar()
            Spacer(Modifier.width(8.dp))
        }
        Box(
            Modifier
                .widthIn(max = if (isUser) 300.dp else 276.dp)
                .clip(shape)
                .background(if (isUser) c.brand else c.surface)
                .then(if (isUser) Modifier else Modifier.border(1.dp, c.border, shape))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = msg.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) c.onBrand else c.textPrimary,
            )
        }
    }
}

/** Rounded multi-line input (up to 4 lines) for the chat bar. */
@Composable
private fun DoctorInput(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    val c = MedAITheme.colors
    val shape = RoundedCornerShape(24.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        maxLines = 4,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textPrimary),
        cursorBrush = SolidColor(c.brand),
        modifier = modifier,
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = MinTouch)
                    .clip(shape)
                    .background(c.surfaceSunken)
                    .border(1.dp, c.borderStrong, shape)
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = c.textSecondary, maxLines = 2)
                }
                inner()
            }
        },
    )
}
