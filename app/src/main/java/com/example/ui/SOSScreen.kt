package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MedAIText
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MinTouch

// --- SCREEN: SOS EMERGENCY ---

@Composable
fun SOSScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val c = MedAITheme.colors

    val context = LocalContext.current
    val lang by viewModel.currentLanguage.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val gpsLocation by viewModel.gpsLocation.collectAsState()
    val acceptedFamily = remember(familyMembers) { familyMembers.filter { it.inviteStatus == "accepted" } }
    var sosSent by remember { mutableStateOf(false) }

    val callFailed = supportText(lang, "Qo'ng'iroq qilib bo'lmadi", "Не удалось совершить звонок", "Could not place the call")
    fun dial(phone: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        } catch (e: Exception) {
            Toast.makeText(context, callFailed, Toast.LENGTH_SHORT).show()
        }
    }

    val callLabel = supportText(lang, "Qo'ng'iroq qilish", "Позвонить", "Call")

    Scaffold(
        topBar = {
            AppHeader(
                title = supportText(lang, "SOS Favqulodda yordam", "SOS Экстренная помощь", "SOS Emergency"),
                onBack = onBack,
            )
        },
        containerColor = c.canvas
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero: the one action that matters most.
            item(key = "hero") {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    MedAIDangerButton(
                        text = supportText(lang, "Tez yordam: 103", "Скорая помощь: 103", "Ambulance: 103"),
                        onClick = { dial("103") },
                        icon = Icons.Default.Emergency,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        supportText(
                            lang,
                            "Tez tibbiy yordam chaqirish uchun bosing",
                            "Нажмите, чтобы вызвать скорую помощь",
                            "Tap to call emergency medical help",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            item(key = "location") {
                MedAICard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(c.dangerSoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = c.onDangerSoft, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                supportText(lang, "Joriy joylashuv", "Текущее местоположение", "Current location"),
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textSecondary,
                            )
                            Text(gpsLocation, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    MedAISecondaryButton(
                        text = if (sosSent) {
                            supportText(lang, "SOS signali yuborildi", "SOS-сигнал отправлен", "SOS signal sent")
                        } else {
                            supportText(lang, "Oila a'zolariga SOS signal yuborish", "Отправить SOS-сигнал семье", "Send SOS signal to family")
                        },
                        onClick = {
                            viewModel.triggerSOS()
                            sosSent = true
                        },
                        icon = Icons.Default.NotificationsActive,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (sosSent) {
                        Spacer(Modifier.height(12.dp))
                        MedAIInfoBanner(
                            text = supportText(
                                lang,
                                "Oila a'zolaringizga joylashuvingiz bilan xabar yuborildi.",
                                "Семье отправлено уведомление с вашим местоположением.",
                                "Your family has been notified with your location.",
                            ),
                            tone = MedAITone.Success,
                        )
                    }
                }
            }

            item(key = "numbers_header") {
                Text(
                    supportText(lang, "Tezkor raqamlar", "Экстренные номера", "Emergency numbers"),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                )
            }

            item(key = "numbers") {
                val numbers = listOf(
                    "103" to supportText(lang, "Tez yordam", "Скорая", "Ambulance"),
                    "102" to supportText(lang, "Politsiya", "Полиция", "Police"),
                    "101" to supportText(lang, "Yong'in", "Пожарные", "Fire"),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    numbers.forEach { (number, label) ->
                        MedAICard(
                            modifier = Modifier.weight(1f),
                            onClick = { dial(number) },
                            contentPadding = 12.dp,
                        ) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(number, style = MedAIText.MetricSmall, color = c.danger)
                                Text(label, style = MaterialTheme.typography.labelMedium, color = c.textSecondary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            item(key = "family_header") {
                Text(
                    supportText(lang, "Oila a'zolari", "Члены семьи", "Family members"),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                )
            }

            if (acceptedFamily.isEmpty()) {
                item(key = "family_empty") {
                    MedAIInfoBanner(
                        text = supportText(
                            lang,
                            "Tasdiqlangan oila a'zolari yo'q. Ularni Oila bo'limida qo'shing.",
                            "Нет подтверждённых членов семьи. Добавьте их в разделе «Семья».",
                            "No confirmed family contacts yet. Add them in the Family section.",
                        ),
                    )
                }
            } else {
                items(acceptedFamily, key = { it.uid }) { member ->
                    MedAICard(contentPadding = 0.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
                                .clickable(onClickLabel = callLabel, role = Role.Button) { dial(member.phone) }
                                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.size(40.dp).clip(CircleShape).background(c.tintTeal.bg),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = c.tintTeal.fg, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(member.name, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                Text(member.relation, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                            Box(Modifier.size(MinTouch), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = c.brand)
                            }
                        }
                    }
                }
            }
        }
    }
}
