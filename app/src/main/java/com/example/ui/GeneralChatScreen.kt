package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.ChatMessageLocal
import com.example.i18n.Translations
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MinTouch
import kotlinx.coroutines.delay

// --- SCREEN: GENERAL CHAT ---

@Composable
fun GeneralChatScreen(
    viewModel: AppViewModel,
    onNavigateToUpgrade: () -> Unit = {}
) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val chatMessages by viewModel.generalChatMessages().collectAsState(initial = emptyList())
    val hasPremium by viewModel.hasPremiumAccess.collectAsState()
    var messageText by remember { mutableStateOf("") }

    // The free daily quota. Shown up front rather than only as a refusal on send, so the user
    // knows the limit exists and has the upgrade path in view before they hit it.
    val freeQuota by viewModel.freeChatQuota.collectAsState()

    // Suggestions only FILL the input; the user reviews and sends.
    val suggestions = remember(lang) {
        listOf(
            supportText(lang, "Qaysi dorilarni birga ichish xavfli?", "Какие лекарства опасно принимать вместе?", "Which medicines are unsafe to take together?"),
            supportText(lang, "Kuniga qancha suv ichish tavsiya qilinadi?", "Сколько воды рекомендуется пить в день?", "How much water should I drink per day?"),
            supportText(lang, "Bosh og'rig'i va charchoq sabablari nima?", "Каковы причины головной боли и усталости?", "What causes headaches and fatigue?"),
        )
    }

    // The reply arrives a moment after the user message, so "last message is mine" means the model
    // is still answering. The indicator gives up after a minute so an offline device never spins forever.
    val lastMsg = chatMessages.lastOrNull()
    val waitingReply by produceState(false, lastMsg?.id, lastMsg?.role) {
        if (lastMsg != null && lastMsg.role == "user") {
            val left = 60_000L - (System.currentTimeMillis() - lastMsg.timestamp)
            if (left > 0) {
                value = true
                delay(left)
            }
        }
        value = false
    }

    val listState = rememberLazyListState()
    val itemCount = chatMessages.size + if (waitingReply) 1 else 0
    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    val sendDescription = supportText(lang, "Yuborish", "Отправить", "Send")
    val canSend = messageText.isNotEmpty()

    Scaffold(
        containerColor = c.canvas,
        topBar = { AppHeader(title = Translations.getString("tab_chat", lang)) },
        bottomBar = {
            Column(Modifier.background(c.surface)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChatInputField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = supportText(
                            lang,
                            "AI sog'liq maslahatchisidan so'rang...",
                            "Спросите AI-консультанта...",
                            "Ask the AI health advisor...",
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(MinTouch)
                            .clip(CircleShape)
                            .background(if (canSend) c.brand else c.surfaceSunken)
                            .clickable(enabled = canSend, role = Role.Button) {
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
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = sendDescription,
                            tint = if (canSend) c.onBrand else c.textSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MedAIInfoBanner(
                    text = supportText(
                        lang,
                        "AI maslahati shifokor o'rnini bosmaydi. Jiddiy alomatlarda shifokorga murojaat qiling.",
                        "Советы ИИ не заменяют врача. При серьёзных симптомах обратитесь к врачу.",
                        "AI advice does not replace a doctor. For serious symptoms, see a doctor.",
                    ),
                )
                if (!hasPremium) {
                    if (freeQuota.remaining > 0) {
                        QuotaStrip(
                            text = supportText(
                                lang,
                                "Bugungi bepul xabarlar: ${freeQuota.remaining} / ${freeQuota.limit}",
                                "Бесплатных сообщений сегодня: ${freeQuota.remaining} из ${freeQuota.limit}",
                                "Free messages today: ${freeQuota.remaining} of ${freeQuota.limit}",
                            ),
                        )
                    } else {
                        MedAIInfoBanner(
                            text = supportText(
                                lang,
                                "Bugungi bepul xabarlar tugadi.",
                                "Бесплатные сообщения на сегодня закончились.",
                                "You have used all free messages for today.",
                            ),
                            tone = MedAITone.Warning,
                            actionLabel = supportText(lang, "Premium", "Premium", "Upgrade"),
                            onAction = onNavigateToUpgrade,
                        )
                    }
                }
            }

            if (chatMessages.isEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(key = "intro") {
                        Column(
                            Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AiAvatar(size = 64.dp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                supportText(lang, "Salom! Sizga qanday yordam bera olaman?", "Здравствуйте! Чем могу помочь?", "Hi! How can I help you today?"),
                                style = MaterialTheme.typography.titleMedium,
                                color = c.textPrimary,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                supportText(
                                    lang,
                                    "Sog'liq, alomatlar, ovqatlanish yoki tahlil natijalari haqida so'rang.",
                                    "Задайте вопрос о здоровье, симптомах, питании или результатах анализов.",
                                    "Ask about symptoms, nutrition, or your lab results.",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textSecondary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    items(suggestions, key = { it }) { question ->
                        SuggestionChip(text = question, onClick = { messageText = question })
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    items(chatMessages, key = { it.id }) { msg -> ChatBubble(msg) }
                    if (waitingReply) {
                        item(key = "typing") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AiAvatar(size = 28.dp)
                                Spacer(Modifier.width(8.dp))
                                MedAISpinner(size = 20.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    supportText(lang, "AI javob yozmoqda...", "ИИ печатает...", "AI is typing..."),
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
private fun AiAvatar(size: androidx.compose.ui.unit.Dp) {
    val c = MedAITheme.colors
    Box(Modifier.size(size).clip(CircleShape).background(c.brandSoft), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.SmartToy, contentDescription = null, tint = c.onBrandSoft, modifier = Modifier.size(size * 0.55f))
    }
}

@Composable
private fun ChatBubble(msg: ChatMessageLocal) {
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
            AiAvatar(size = 28.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = msg.content,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUser) c.onBrand else c.textPrimary,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(if (isUser) c.brand else c.surface)
                .then(if (isUser) Modifier else Modifier.border(BorderStroke(1.dp, c.border), shape))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun QuotaStrip(text: String) {
    val c = MedAITheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surfaceSunken)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    val c = MedAITheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouch)
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.borderStrong, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary)
    }
}

/** Rounded multi-line input in the style of MedAITextField, without the label above it. */
@Composable
private fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val c = MedAITheme.colors
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(24.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textPrimary),
        cursorBrush = SolidColor(c.brand),
        maxLines = 4,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = modifier.onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = MinTouch)
                    .clip(shape)
                    .background(c.surface)
                    .border(if (focused) 2.dp else 1.dp, if (focused) c.brand else c.borderStrong, shape)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
                }
                inner()
            }
        },
    )
}
