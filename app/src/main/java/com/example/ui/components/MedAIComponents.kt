package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MedAICard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = MedicalBorder,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = elevation
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun Pill(
    label: String,
    color: Color = PrimaryGreen,
    backgroundColor: Color = LightGreen,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun IconBubble(
    icon: ImageVector,
    color: Color = PrimaryGreen,
    backgroundColor: Color = LightGreen,
    size: Dp = 42.dp,
    iconSize: Dp = 22.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun GradientButton(
    label: String,
    icon: ImageVector? = null,
    variant: String = "primary", // primary, violet, destructive
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brush = when (variant) {
        "violet" -> Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF6366F1)))
        "destructive" -> Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF00A884), Color(0xFF00897B)))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = if (enabled) Color.Transparent else Color(0xFFE2E8F0),
        shadowElevation = if (enabled) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (enabled) brush else Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun HealthScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 104.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "scoreProgress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 9.dp.toPx()
            // Background track
            drawCircle(
                color = Color(0xFFE2E8F0),
                style = Stroke(width = strokeWidth)
            )
            // Foreground arc
            val gradientBrush = Brush.sweepGradient(
                listOf(
                    Color(0xFF00A884),
                    Color(0xFF10B981),
                    Color(0xFF0284C7),
                    Color(0xFF00A884)
                )
            )
            drawArc(
                brush = gradientBrush,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = if (score >= 80) "A’LO" else if (score >= 60) "YAXSHI" else "O‘RTA",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (score >= 80) SuccessGreen else WarningOrange,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun HealthPlanCard(
    heightCm: String,
    weightKg: String,
    waterTaken: Int,
    medsTaken: Int,
    medsTotal: Int,
    steps: Int,
    pulse: String,
    onManagePlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val h = heightCm.toDoubleOrNull() ?: 170.0
    val w = weightKg.toDoubleOrNull() ?: 70.0
    val bmi = (w / ((h / 100.0) * (h / 100.0))).let { if (it.isNaN()) 22.4 else it }
    val bmiStatus = when {
        bmi < 18.5 -> "Kam vazn"
        bmi < 25.0 -> "Normal vazn"
        bmi < 30.0 -> "Ortiqcha vazn"
        else -> "Semizlik"
    }

    val targetWaterLiters = String.format("%.1f", (w * 0.033))
    val waterGoal = 8
    val waterRemaining = (waterGoal - waterTaken).coerceAtLeast(0)

    MedAICard(
        modifier = modifier,
        backgroundColor = Color.White,
        borderColor = MedicalBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(
                    icon = Icons.Default.Favorite,
                    color = PrimaryGreen,
                    backgroundColor = LightGreen,
                    size = 36.dp,
                    iconSize = 20.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "INDIVIDUAL REJA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Bugungi sog‘liq monitoringi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
            Pill(label = "VIP", color = PremiumPurple, backgroundColor = PremiumLight)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // BMI Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tana vazni indeksi (BMI)", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(String.format("%.1f", bmi), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(bmiStatus, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryGreen)
                    }
                }
                Pill(
                    label = "Normada",
                    color = SuccessGreen,
                    backgroundColor = Color(0xFFD1FAE5)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Grid metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Water
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F9FF))
                    .padding(10.dp)
            ) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Suv balansi", fontSize = 10.sp, color = TextSecondary)
                Text("$waterTaken/$waterGoal stakan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("$targetWaterLiters L maqsad", fontSize = 9.sp, color = Color(0xFF0284C7))
            }

            // Steps
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFFBEB))
                    .padding(10.dp)
            ) {
                Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Faollik", fontSize = 10.sp, color = TextSecondary)
                Text("$steps qadam", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Maqsad: 8000", fontSize = 9.sp, color = Color(0xFFF59E0B))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Medication
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF2F2))
                    .padding(10.dp)
            ) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Dori qabuli", fontSize = 10.sp, color = TextSecondary)
                Text("$medsTaken/$medsTotal ichildi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Intizom: ${if (medsTotal > 0) (medsTaken * 100 / medsTotal) else 100}%", fontSize = 9.sp, color = Color(0xFFEF4444))
            }

            // Pulse
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F3FF))
                    .padding(10.dp)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = PremiumPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Puls monitoring", fontSize = 10.sp, color = TextSecondary)
                Text("${pulse.ifBlank { "72" }} BPM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Barqaror ritm", fontSize = 9.sp, color = PremiumPurple)
            }
        }
    }
}

@Composable
fun AppHeader(
    title: String,
    kicker: String? = null,
    onBack: (() -> Unit)? = null,
    trailingAction: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, MedicalBorder, RoundedCornerShape(14.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Ortga",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            if (kicker != null) {
                Text(
                    text = kicker,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    letterSpacing = 1.2.sp
                )
            }
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (trailingAction != null) {
            trailingAction()
        }
    }
}
