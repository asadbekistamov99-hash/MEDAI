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

// --- SCREEN: SOS EMERGENCY ---

@Composable
fun SOSScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val medai = MedAITheme.colors

    val context = LocalContext.current
    val familyMembers by viewModel.familyMembers.collectAsState()
    val gpsLocation by viewModel.gpsLocation.collectAsState()
    val acceptedFamily = familyMembers.filter { it.inviteStatus == "accepted" }
    var sosSent by remember { mutableStateOf(false) }

    fun dial(phone: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        } catch (e: Exception) {
            Toast.makeText(context, "Qo'ng'iroq qilib bo'lmadi", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = { AppHeader(title = "SOS Favqulodda Yordam", onBack = onBack) },
        containerColor = medai.canvas
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    val pulseTransition = rememberInfiniteTransition(label = "sosPulse")
                    val pulseScale by pulseTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.06f,
                        animationSpec = infiniteRepeatable(animation = tween(700), repeatMode = RepeatMode.Reverse),
                        label = "sosPulseScale"
                    )
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale)
                            .shadow(16.dp, CircleShape, ambientColor = medai.danger, spotColor = medai.danger)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(colors = listOf(Color(0xFFEF5350), medai.danger)))
                            .clickable {
                                dial("103")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = "103 ga qo'ng'iroq",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("103", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tez tibbiy yordam chaqirish uchun bosing",
                        fontSize = 13.sp,
                        color = medai.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, medai.border)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(medai.danger.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = medai.danger, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Joriy joylashuv", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = medai.textSecondary)
                                Text(gpsLocation, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = medai.textPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.triggerSOS()
                                sosSent = true
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = medai.danger)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (sosSent) "SOS signali yuborildi ✓" else "Oila a'zolariga SOS signal yuborish",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "TEZKOR RAQAMLAR".uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = medai.brand,
                    letterSpacing = 1.2.sp
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("103" to "Tez yordam", "102" to "Politsiya", "101" to "Yong'in").forEach { (number, label) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dial(number) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, medai.border)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 14.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(number, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = medai.danger)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(label, fontSize = 11.sp, color = medai.textSecondary)
                            }
                        }
                    }
                }
            }

            if (acceptedFamily.isNotEmpty()) {
                item {
                    Text(
                        text = "OILA A'ZOLARI".uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = medai.brand,
                        letterSpacing = 1.2.sp
                    )
                }
                items(acceptedFamily, key = { it.uid }) { member ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, medai.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(medai.brandSoft, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = medai.brand, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = medai.textPrimary)
                                Text(member.relation, fontSize = 12.sp, color = medai.textSecondary)
                            }
                            IconButton(
                                onClick = { dial(member.phone) },
                                modifier = Modifier.size(40.dp).background(medai.brand.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Qo'ng'iroq", tint = medai.brand, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
