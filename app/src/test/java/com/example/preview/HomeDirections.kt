package com.example.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Sample copy for the direction mock-ups. Numbers are sample data, not the user's. */
data class S(
  val hello: String, val sub: String, val trial: String, val upgrade: String,
  val score: String, val good: String, val steps: String, val water: String, val sleep: String,
  val aiTitle: String, val aiSub: String, val quick: String,
  val symptoms: String, val meds: String, val reminders: String, val sos: String,
  val all: String, val family: String, val familySub: String, val lab: String, val labSub: String,
  val tips: String, val tipsSub: String, val stats: String, val statsSub: String,
  val nHome: String, val nHist: String, val nChat: String, val nProf: String,
)

val UZ = S("Salom, Aziz", "Bugun o'zingizni qanday his qilyapsiz?", "Premium sinov · 7 kun qoldi", "Davom ettirish",
  "Sog'liq darajasi", "Yaxshi", "Qadamlar", "Suv", "Uyqu",
  "AI shifokor bilan maslahat", "Simptomlarni yozing — javob soniyalarda", "Tezkor harakatlar",
  "Simptomlar", "Dorilar", "Eslatmalar", "SOS",
  "Barcha xizmatlar", "Oila a'zolari", "Yaqinlaringiz salomatligi", "Tahlillar", "Natijalarni AI bilan o'qing",
  "Maslahatlar", "Kunlik sog'liq tavsiyalari", "Statistika", "Ko'rsatkichlar dinamikasi",
  "Asosiy", "Tarix", "AI Chat", "Profil")
val RU = S("Привет, Азиз", "Как вы себя сегодня чувствуете?", "Пробный Premium · осталось 7 дней", "Продлить",
  "Показатель здоровья", "Хорошо", "Шаги", "Вода", "Сон",
  "Консультация с AI-врачом", "Опишите симптомы — ответ за секунды", "Быстрые действия",
  "Симптомы", "Лекарства", "Напоминания", "SOS",
  "Все сервисы", "Члены семьи", "Здоровье ваших близких", "Анализы", "Расшифровка результатов с AI",
  "Советы", "Ежедневные рекомендации", "Статистика", "Динамика показателей",
  "Главная", "История", "AI Чат", "Профиль")
val EN = S("Hello, Aziz", "How are you feeling today?", "Premium trial · 7 days left", "Upgrade",
  "Health score", "Good", "Steps", "Water", "Sleep",
  "Consult the AI doctor", "Describe symptoms — answers in seconds", "Quick actions",
  "Symptoms", "Medicines", "Reminders", "SOS",
  "All services", "Family members", "Your loved ones' health", "Lab results", "Understand results with AI",
  "Health tips", "Daily wellbeing advice", "Statistics", "Trends over time",
  "Home", "History", "AI Chat", "Profile")

private val Ink900 = Color(0xFF0B1220)
private val Ink500 = Color(0xFF526175)

// ======================================================================================
// A — "Toza tibbiy": clinical, trustworthy. White surfaces, hairlines, one teal, calm.
// ======================================================================================
private val ATeal = Color(0xFF0A7568)
private val ATealDeep = Color(0xFF075E55)
private val ATint = Color(0xFFE3F4F1)
private val ABorder = Color(0xFFE1EAE8)
private val ACanvas = Color(0xFFF6F9F9)

@Composable
fun HomeDirectionA(s: S) {
  Column(Modifier.fillMaxSize().background(ACanvas)) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
      Spacer(Modifier.height(20.dp))
      // top bar
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(ATint), contentAlignment = Alignment.Center) {
          Text("A", color = ATealDeep, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
          Text(s.hello, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, color = Ink900, maxLines = 1, overflow = TextOverflow.Ellipsis)
          Text(s.sub, fontSize = 13.sp, lineHeight = 18.sp, color = Ink500, maxLines = 2)
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White).border(1.dp, ABorder, CircleShape), contentAlignment = Alignment.Center) {
          Icon(Icons.Rounded.Notifications, null, tint = Ink900, modifier = Modifier.size(24.dp))
          Box(Modifier.align(Alignment.TopEnd).padding(12.dp).size(9.dp).clip(CircleShape).background(Color(0xFFDC2626)).border(1.5.dp, Color.White, CircleShape))
        }
      }
      Spacer(Modifier.height(16.dp))
      // trial strip: one line, not a banner
      Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF5F3FF))
          .border(1.dp, Color(0xFFE4DCFB), RoundedCornerShape(14.dp)).padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Rounded.WorkspacePremium, null, tint = Color(0xFF6D28D9), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(s.trial, Modifier.weight(1f), fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4C1D95), maxLines = 2)
        Box(Modifier.heightIn(min = 48.dp).clickable { }.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
          Text(s.upgrade, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
        }
      }
      Spacer(Modifier.height(16.dp))
      // health summary
      Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).border(1.dp, ABorder, RoundedCornerShape(20.dp)).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(s.score, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Ink500)
          Text(s.good, Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFD1FAE5)).padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF065F46))
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
          Text("82", fontSize = 48.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold, color = Ink900)
          Text(" /100", fontSize = 16.sp, color = Ink500, modifier = Modifier.padding(bottom = 8.dp))
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color(0xFFE6EEEC))) {
          Box(Modifier.fillMaxWidth(0.82f).fillMaxHeight().clip(CircleShape).background(ATeal))
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(ABorder))
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
          AMetric(Modifier.weight(1f), Icons.Rounded.DirectionsWalk, "4 230", s.steps)
          AMetric(Modifier.weight(1f), Icons.Rounded.WaterDrop, "5 / 8", s.water)
          AMetric(Modifier.weight(1f), Icons.Rounded.Bedtime, "7h 20m", s.sleep)
        }
      }
      Spacer(Modifier.height(16.dp))
      // primary AI action
      Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(ATealDeep, ATeal)))
          .clickable { }.padding(18.dp), verticalAlignment = Alignment.CenterVertically
      ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
          Icon(Icons.Rounded.Psychology, null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
          Text(s.aiTitle, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
          Text(s.aiSub, fontSize = 13.sp, lineHeight = 18.sp, color = Color.White.copy(alpha = 0.92f))
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = Color.White)
      }
      Spacer(Modifier.height(24.dp))
      Text(s.quick, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink900)
      Spacer(Modifier.height(12.dp))
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AQuick(Modifier.weight(1f), Icons.Rounded.MonitorHeart, s.symptoms, false)
        AQuick(Modifier.weight(1f), Icons.Rounded.Medication, s.meds, false)
        AQuick(Modifier.weight(1f), Icons.Rounded.Alarm, s.reminders, false)
        AQuick(Modifier.weight(1f), Icons.Rounded.Emergency, s.sos, true)
      }
      Spacer(Modifier.height(24.dp))
      Text(s.all, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink900)
      Spacer(Modifier.height(12.dp))
      Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).border(1.dp, ABorder, RoundedCornerShape(20.dp))) {
        ARow(Icons.Rounded.FamilyRestroom, s.family, s.familySub, true)
        ARow(Icons.Rounded.Science, s.lab, s.labSub, true)
        ARow(Icons.Rounded.Lightbulb, s.tips, s.tipsSub, true)
        ARow(Icons.Rounded.BarChart, s.stats, s.statsSub, false)
      }
      Spacer(Modifier.height(20.dp))
    }
    ANav(s)
  }
}

@Composable private fun AMetric(m: Modifier, icon: ImageVector, value: String, label: String) {
  Column(m, horizontalAlignment = Alignment.CenterHorizontally) {
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(ATint), contentAlignment = Alignment.Center) {
      Icon(icon, null, tint = ATealDeep, modifier = Modifier.size(20.dp))
    }
    Spacer(Modifier.height(8.dp))
    Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink900, maxLines = 1)
    Text(label, fontSize = 12.sp, color = Ink500, maxLines = 1, overflow = TextOverflow.Ellipsis)
  }
}

@Composable private fun AQuick(m: Modifier, icon: ImageVector, label: String, danger: Boolean) {
  val bg = if (danger) Color(0xFFFEE9E9) else Color.White
  val fg = if (danger) Color(0xFFB91C1C) else ATealDeep
  Column(
    m.heightIn(min = 96.dp).clip(RoundedCornerShape(16.dp)).background(bg)
      .border(1.dp, if (danger) Color(0xFFFAC9C9) else ABorder, RoundedCornerShape(16.dp)).clickable { }.padding(vertical = 14.dp, horizontal = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
  ) {
    Icon(icon, null, tint = fg, modifier = Modifier.size(28.dp))
    Spacer(Modifier.height(8.dp))
    Text(label, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium, color = if (danger) fg else Ink900,
      textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
  }
}

@Composable private fun ARow(icon: ImageVector, title: String, sub: String, divider: Boolean) {
  Column {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable { }.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(ATint), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = ATealDeep, modifier = Modifier.size(22.dp))
      }
      Spacer(Modifier.width(14.dp))
      Column(Modifier.weight(1f)) {
        Text(title, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink900, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(sub, fontSize = 12.sp, lineHeight = 16.sp, color = Ink500, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
      Icon(Icons.Rounded.ChevronRight, null, tint = Color(0xFF8795A8))
    }
    if (divider) Box(Modifier.padding(start = 70.dp).fillMaxWidth().height(1.dp).background(Color(0xFFEEF3F2)))
  }
}

@Composable private fun ANav(s: S) {
  Column(Modifier.background(Color.White)) {
    Box(Modifier.fillMaxWidth().height(1.dp).background(ABorder))
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
      listOf(Icons.Rounded.Home to s.nHome, Icons.Rounded.History to s.nHist, Icons.Rounded.Chat to s.nChat, Icons.Rounded.Person to s.nProf)
        .forEachIndexed { i, (ic, label) ->
          val sel = i == 0
          Column(Modifier.weight(1f).heightIn(min = 56.dp).clickable { }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.height(28.dp).width(56.dp).clip(CircleShape).background(if (sel) ATint else Color.Transparent), contentAlignment = Alignment.Center) {
              Icon(ic, null, tint = if (sel) ATealDeep else Ink500, modifier = Modifier.size(24.dp))
            }
            Text(label, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium,
              color = if (sel) ATealDeep else Ink500, maxLines = 1, overflow = TextOverflow.Ellipsis)
          }
        }
    }
  }
}

// ======================================================================================
// B — "Yumshoq / premium": big gradient hero, pastel bento, floating pill navigation.
// ======================================================================================
private val BTeal = Color(0xFF0F766E)
private val BIndigo = Color(0xFF3730A3)
private val BCanvas = Color(0xFFF3F6FB)

@Composable
fun HomeDirectionB(s: S) {
  Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF1F5FC), Color(0xFFEAF5F2))))) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
      Spacer(Modifier.height(20.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("MedAI", Modifier.weight(1f), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = BIndigo, letterSpacing = (-0.5).sp)
        Box(Modifier.size(48.dp).shadow(8.dp, CircleShape, ambientColor = Color(0x1A3730A3), spotColor = Color(0x1A3730A3)).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
          Icon(Icons.Rounded.Notifications, null, tint = BIndigo, modifier = Modifier.size(24.dp))
        }
      }
      Spacer(Modifier.height(16.dp))
      // hero
      Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Brush.linearGradient(listOf(BTeal, BIndigo))).padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.weight(1f)) {
            Text(s.hello + " 👋", fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = (-0.4).sp)
            Spacer(Modifier.height(4.dp))
            Text(s.sub, fontSize = 14.sp, lineHeight = 20.sp, color = Color.White.copy(alpha = 0.92f))
            Spacer(Modifier.height(12.dp))
            Text(s.good, Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 6.dp),
              fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
          }
          Spacer(Modifier.width(12.dp))
          Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
              val st = 10.dp.toPx()
              drawArc(Color.White.copy(alpha = 0.22f), -90f, 360f, false, topLeft = Offset(st / 2, st / 2), size = Size(size.width - st, size.height - st), style = Stroke(st, cap = StrokeCap.Round))
              drawArc(Color.White, -90f, 360f * 0.82f, false, topLeft = Offset(st / 2, st / 2), size = Size(size.width - st, size.height - st), style = Stroke(st, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("82", fontSize = 30.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
              Text(s.score, fontSize = 10.sp, lineHeight = 12.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 2, textAlign = TextAlign.Center,
                modifier = Modifier.width(64.dp))
            }
          }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          BPill(Modifier.weight(1f), Icons.Rounded.DirectionsWalk, "4 230", s.steps)
          BPill(Modifier.weight(1f), Icons.Rounded.WaterDrop, "5 / 8", s.water)
          BPill(Modifier.weight(1f), Icons.Rounded.Bedtime, "7h 20m", s.sleep)
        }
      }
      Spacer(Modifier.height(12.dp))
      // trial
      Row(
        Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x1A4C1D95), spotColor = Color(0x1A4C1D95))
          .clip(RoundedCornerShape(24.dp)).background(Color.White).padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))), contentAlignment = Alignment.Center) {
          Icon(Icons.Rounded.WorkspacePremium, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(s.trial, Modifier.weight(1f), fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B0764), maxLines = 2)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(50)).background(Color(0xFF6D28D9)).clickable { }.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
          Text(s.upgrade, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
        }
      }
      Spacer(Modifier.height(22.dp))
      Text(s.all, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink900)
      Spacer(Modifier.height(12.dp))
      // bento
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BTile(Modifier.weight(1f).height(196.dp), Icons.Rounded.Psychology, s.aiTitle, s.aiSub, Color(0xFFEDE9FE), Color(0xFF4C1D95), big = true)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          BTile(Modifier.fillMaxWidth().height(92.dp), Icons.Rounded.MonitorHeart, s.symptoms, null, Color(0xFFD7F3EC), Color(0xFF064E46))
          BTile(Modifier.fillMaxWidth().height(92.dp), Icons.Rounded.Medication, s.meds, null, Color(0xFFFFE9D6), Color(0xFF7C2D12))
        }
      }
      Spacer(Modifier.height(12.dp))
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        BRound(Icons.Rounded.Alarm, s.reminders, Color(0xFFDDEBFF), Color(0xFF1E3A8A))
        BRound(Icons.Rounded.FamilyRestroom, s.family, Color(0xFFD7F3EC), Color(0xFF064E46))
        BRound(Icons.Rounded.BarChart, s.stats, Color(0xFFEDE9FE), Color(0xFF4C1D95))
        BRound(Icons.Rounded.Emergency, s.sos, Color(0xFFFEE2E2), Color(0xFF991B1B))
      }
      Spacer(Modifier.height(110.dp))
    }
    // floating pill nav
    Row(
      Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth()
        .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = Color(0x333730A3), spotColor = Color(0x333730A3))
        .clip(RoundedCornerShape(32.dp)).background(Color.White).padding(6.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      listOf(Icons.Rounded.Home to s.nHome, Icons.Rounded.History to s.nHist, Icons.Rounded.Chat to s.nChat, Icons.Rounded.Person to s.nProf)
        .forEachIndexed { i, (ic, label) ->
          val sel = i == 0
          Row(
            Modifier.heightIn(min = 52.dp).clip(RoundedCornerShape(26.dp)).background(if (sel) BIndigo else Color.Transparent)
              .clickable { }.padding(horizontal = if (sel) 16.dp else 14.dp), verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(ic, null, tint = if (sel) Color.White else Ink500, modifier = Modifier.size(24.dp))
            if (sel) { Spacer(Modifier.width(6.dp)); Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1) }
          }
        }
    }
  }
}

@Composable private fun BPill(m: Modifier, icon: ImageVector, value: String, label: String) {
  Column(m.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.16f)).padding(vertical = 12.dp, horizontal = 10.dp)) {
    Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
    Spacer(Modifier.height(6.dp))
    Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
    Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.92f), maxLines = 1, overflow = TextOverflow.Ellipsis)
  }
}

@Composable private fun BTile(m: Modifier, icon: ImageVector, title: String, sub: String?, bg: Color, fg: Color, big: Boolean = false) {
  Column(m.clip(RoundedCornerShape(28.dp)).background(bg).clickable { }.padding(16.dp), verticalArrangement = if (big) Arrangement.SpaceBetween else Arrangement.Center) {
    Box(Modifier.size(if (big) 48.dp else 36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.75f)), contentAlignment = Alignment.Center) {
      Icon(icon, null, tint = fg, modifier = Modifier.size(if (big) 26.dp else 20.dp))
    }
    if (!big) Spacer(Modifier.height(8.dp))
    Column {
      Text(title, fontSize = if (big) 16.sp else 14.sp, lineHeight = if (big) 21.sp else 18.sp, fontWeight = FontWeight.Bold, color = fg, maxLines = if (big) 3 else 1, overflow = TextOverflow.Ellipsis)
      if (sub != null) Text(sub, fontSize = 12.sp, lineHeight = 16.sp, color = fg.copy(alpha = 0.85f), maxLines = 3, overflow = TextOverflow.Ellipsis)
    }
  }
}

@Composable private fun BRound(icon: ImageVector, label: String, bg: Color, fg: Color) {
  Column(Modifier.width(78.dp).clickable { }, horizontalAlignment = Alignment.CenterHorizontally) {
    Box(Modifier.size(60.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) { Icon(icon, null, tint = fg, modifier = Modifier.size(26.dp)) }
    Spacer(Modifier.height(6.dp))
    Text(label, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium, color = Ink900, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
  }
}
