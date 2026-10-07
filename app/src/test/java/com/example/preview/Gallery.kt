package com.example.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.*
import com.example.ui.theme.MedAIText
import com.example.ui.theme.MedAITheme

data class GS(
  val title: String, val colors: String, val type: String, val buttons: String, val chips: String, val inputs: String,
  val cards: String, val nav: String, val states: String,
  val primary: String, val primaryLong: String, val secondary: String, val sos: String, val forgot: String, val loading: String, val disabled: String,
  val chipAll: String, val chipFamily: String, val chipMeds: String, val good: String, val premium: String, val warn: String, val late: String, val info: String,
  val email: String, val emailPh: String, val password: String, val passwordVal: String, val errorLabel: String, val errorMsg: String,
  val cardTitle: String, val cardBody: String,
  val topTitle: String, val topSub: String,
  val emptyTitle: String, val emptyMsg: String, val emptyAction: String,
  val dlgTitle: String, val dlgMsg: String, val dlgOk: String, val dlgCancel: String,
  val h1: String, val body: String, val label: String,
)

val GUZ = GS("Komponentlar", "Ranglar", "Tipografiya", "Tugmalar", "Chip va nishonlar", "Kiritish maydonlari", "Kartalar va ro'yxat", "Navigatsiya", "Holatlar",
  "Saqlash", "Premium bilan davom etish", "Bekor qilish", "SOS chaqirish", "Parolni unutdingizmi?", "Yuklanmoqda", "Faol emas",
  "Hammasi", "Oila", "Dorilar", "Yaxshi", "Premium", "Diqqat", "Kechikkan", "Ma'lumot",
  "Elektron pochta", "doctor@gmail.com", "Parol", "maxfiyparol", "Telefon raqami", "Raqam noto'g'ri formatda",
  "Dori qabul qilish eslatmasi", "Har kuni soat 08:00 da, ovqatdan keyin.",
  "Oila a'zolari", "3 ta a'zo ulangan",
  "Tarix bo'sh", "Qidiruv natijalari bu yerda saqlanadi.", "Simptomni tekshirish",
  "Akkauntdan chiqasizmi?", "Qayta kirish uchun email va parol kerak bo'ladi.", "Chiqish", "Bekor qilish",
  "Salomatlik", "Bugun o'zingizni qanday his qilyapsiz?", "SOG'LIQ DARAJASI")
val GRU = GS("Компоненты", "Цвета", "Типографика", "Кнопки", "Чипы и значки", "Поля ввода", "Карточки и списки", "Навигация", "Состояния",
  "Сохранить", "Продолжить с Premium", "Отмена", "Вызвать SOS", "Забыли пароль?", "Загрузка", "Недоступно",
  "Все", "Семья", "Лекарства", "Хорошо", "Premium", "Внимание", "Просрочено", "Инфо",
  "Электронная почта", "doctor@gmail.com", "Пароль", "секретныйпароль", "Номер телефона", "Неверный формат номера",
  "Напоминание о приёме лекарств", "Каждый день в 08:00, после еды.",
  "Члены семьи", "Подключено 3 участника",
  "История пуста", "Результаты проверок сохраняются здесь.", "Проверить симптом",
  "Выйти из аккаунта?", "Для повторного входа понадобятся почта и пароль.", "Выйти", "Отмена",
  "Здоровье", "Как вы себя сегодня чувствуете?", "ПОКАЗАТЕЛЬ ЗДОРОВЬЯ")
val GEN = GS("Components", "Colours", "Typography", "Buttons", "Chips and badges", "Text fields", "Cards and lists", "Navigation", "States",
  "Save", "Continue with Premium", "Cancel", "Call SOS", "Forgot password?", "Loading", "Unavailable",
  "All", "Family", "Medicines", "Good", "Premium", "Attention", "Overdue", "Info",
  "Email", "doctor@gmail.com", "Password", "secretpassword", "Phone number", "Number format is invalid",
  "Medication reminder", "Every day at 08:00, after meals.",
  "Family members", "3 members linked",
  "History is empty", "Check results are saved here.", "Check a symptom",
  "Sign out?", "You will need your email and password to sign back in.", "Sign out", "Cancel",
  "Health", "How are you feeling today?", "HEALTH SCORE")

@Composable private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
  val c = MedAITheme.colors
  Column(Modifier.fillMaxWidth().padding(top = 24.dp)) {
    Text(title.uppercase(), style = MedAIText.Eyebrow, color = c.brand)
    Spacer(Modifier.height(10.dp))
    content()
  }
}

@Composable private fun Swatch(name: String, color: Color, on: Color, modifier: Modifier = Modifier) {
  Box(modifier.height(44.dp).clip(RoundedCornerShape(10.dp)).background(color).padding(8.dp), contentAlignment = Alignment.CenterStart) {
    Text(name, style = MaterialTheme.typography.labelSmall, color = on, maxLines = 1)
  }
}

/** Page 1: tokens + basic controls. */
@Composable
fun GalleryPage1(g: GS) {
  val c = MedAITheme.colors
  Column(Modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp)) {
    Text(g.title, style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
    Section(g.colors) {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Swatch("brand", c.brand, c.onBrand, Modifier.weight(1f)); Swatch("canvas", c.canvas, c.textPrimary, Modifier.weight(1f)); Swatch("surface", c.surface, c.textPrimary, Modifier.weight(1f))
      }
      Spacer(Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Swatch("soft", c.brandSoft, c.onBrandSoft, Modifier.weight(1f)); Swatch("premium", c.premium, c.onPremium, Modifier.weight(1f)); Swatch("danger", c.danger, c.onDanger, Modifier.weight(1f))
      }
      Spacer(Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Swatch("success", c.success, c.surface, Modifier.weight(1f)); Swatch("warning", c.warning, c.surface, Modifier.weight(1f)); Swatch("info", c.info, c.surface, Modifier.weight(1f))
      }
    }
    Section(g.type) {
      Text(g.h1, style = MedAIText.MetricMedium, color = c.textPrimary)
      Text(g.title, style = MaterialTheme.typography.headlineSmall, color = c.textPrimary)
      Text(g.body, style = MaterialTheme.typography.bodyLarge, color = c.textPrimary)
      Text(g.body, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
      Spacer(Modifier.height(4.dp))
      Text(g.label, style = MedAIText.Eyebrow, color = c.brand)
    }
    Section(g.buttons) {
      MedAIPrimaryButton(g.primary, {}, Modifier.fillMaxWidth().testTag("btn_primary"), icon = Icons.Rounded.Check)
      Spacer(Modifier.height(10.dp))
      MedAIPrimaryButton(g.primaryLong, {}, Modifier.fillMaxWidth().testTag("btn_primary_long"), icon = Icons.Rounded.WorkspacePremium)
      Spacer(Modifier.height(10.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MedAIPrimaryButton(g.loading, {}, Modifier.weight(1f).testTag("btn_loading"), loading = true)
        MedAIPrimaryButton(g.disabled, {}, Modifier.weight(1f).testTag("btn_disabled"), enabled = false)
      }
      Spacer(Modifier.height(10.dp))
      MedAISecondaryButton(g.secondary, {}, Modifier.fillMaxWidth().testTag("btn_secondary"))
      Spacer(Modifier.height(10.dp))
      MedAIDangerButton(g.sos, {}, Modifier.fillMaxWidth().testTag("btn_danger"), icon = Icons.Rounded.Emergency)
      Spacer(Modifier.height(4.dp))
      MedAITextButton(g.forgot, {}, Modifier.testTag("btn_text"))
    }
    Section(g.chips) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MedAIFilterChip(g.chipAll, true, {}, Modifier.testTag("chip_selected"))
        MedAIFilterChip(g.chipFamily, false, {}, Modifier.testTag("chip_1"), icon = Icons.Rounded.FamilyRestroom)
        MedAIFilterChip(g.chipMeds, false, {}, Modifier.testTag("chip_2"))
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
        MedAIBadge(g.good, MedAIBadgeTone.Success)
        MedAIBadge(g.premium, MedAIBadgeTone.Premium)
        MedAIBadge(g.warn, MedAIBadgeTone.Warning)
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
        MedAIBadge(g.late, MedAIBadgeTone.Danger)
        MedAIBadge(g.info, MedAIBadgeTone.Info)
        MedAIBadge(g.chipFamily, MedAIBadgeTone.Brand)
      }
    }
    Section(g.inputs) {
      MedAITextField("doc@mail.uz", {}, g.email, Modifier.fillMaxWidth(), leadingIcon = Icons.Rounded.Email)
      Spacer(Modifier.height(12.dp))
      MedAITextField("", {}, g.email, Modifier.fillMaxWidth(), placeholder = g.emailPh, leadingIcon = Icons.Rounded.Email)
      Spacer(Modifier.height(12.dp))
      MedAITextField(g.passwordVal, {}, g.password, Modifier.fillMaxWidth(), leadingIcon = Icons.Rounded.Lock, isPassword = true)
      Spacer(Modifier.height(12.dp))
      MedAITextField("+998 90 12", {}, g.errorLabel, Modifier.fillMaxWidth(), leadingIcon = Icons.Rounded.Phone, error = g.errorMsg, keyboardType = KeyboardType.Phone)
    }
  }
}

/** Page 2: surfaces, navigation, states. */
@Composable
fun GalleryPage2(g: GS, s: S) {
  val c = MedAITheme.colors
  Column(Modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp)) {
    Section(g.cards) {
      MedAIHeroCard {
        Text(g.label, style = MaterialTheme.typography.labelMedium, color = c.onHeroMuted)
        Text(g.body, style = MaterialTheme.typography.titleMedium, color = c.onHero)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          MedAIMetricPill(Icons.Rounded.DirectionsWalk, "4 230", s.steps, Modifier.weight(1f))
          MedAIMetricPill(Icons.Rounded.WaterDrop, "5 / 8", s.water, Modifier.weight(1f))
          MedAIMetricPill(Icons.Rounded.Bedtime, "7h 20m", s.sleep, Modifier.weight(1f))
        }
      }
      Spacer(Modifier.height(12.dp))
      MedAICard(Modifier.fillMaxWidth(), onClick = {}) {
        Text(g.cardTitle, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text(g.cardBody, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
      }
      Spacer(Modifier.height(12.dp))
      MedAIQuickTileGrid(listOf(
        MedAIQuickItem(Icons.Rounded.MonitorHeart, s.symptoms, c.tintTeal, {}, "tile_0"),
        MedAIQuickItem(Icons.Rounded.Medication, s.meds, c.tintPeach, {}, "tile_1"),
        MedAIQuickItem(Icons.Rounded.Alarm, s.reminders, c.tintSky, {}, "tile_2"),
        MedAIQuickItem(Icons.Rounded.Emergency, s.sos, MedAITint2(c), {}, "tile_3"),
      ))
      Spacer(Modifier.height(12.dp))
      MedAICard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        MedAIListRow(Icons.Rounded.FamilyRestroom, s.family, {}, Modifier.testTag("row_0"), subtitle = s.familySub)
        MedAIListRow(Icons.Rounded.Science, s.lab, {}, Modifier.testTag("row_1"), subtitle = s.labSub, tint = c.tintViolet)
        MedAIListRow(Icons.Rounded.BarChart, s.stats, {}, Modifier.testTag("row_2"), subtitle = s.statsSub, tint = c.tintSky, showDivider = false)
      }
    }
    Section(g.nav) {
      Box(Modifier.clip(RoundedCornerShape(16.dp))) { MedAITopBar(g.topTitle, subtitle = g.topSub, onBack = {}, backDescription = "Back", actions = {
        androidx.compose.material3.Icon(Icons.Rounded.Notifications, null, tint = c.textPrimary, modifier = Modifier.padding(12.dp))
      }) }
      Spacer(Modifier.height(12.dp))
      Box(Modifier.clip(RoundedCornerShape(16.dp))) {
        MedAIBottomBar(
          listOf(MedAIBottomItem(Icons.Rounded.Home, s.nHome), MedAIBottomItem(Icons.Rounded.History, s.nHist), MedAIBottomItem(Icons.Rounded.Chat, s.nChat), MedAIBottomItem(Icons.Rounded.Person, s.nProf)),
          selectedIndex = 0, onSelect = {},
        )
      }
    }
    Section(g.states) {
      MedAICard(Modifier.fillMaxWidth(), contentPadding = 0.dp) { MedAIEmptyState(g.emptyTitle, message = g.emptyMsg, icon = Icons.Rounded.History, actionLabel = g.emptyAction, onAction = {}) }
      Spacer(Modifier.height(16.dp))
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        MedAISpinner()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          MedAISkeleton(Modifier.fillMaxWidth().height(14.dp), animated = false)
          MedAISkeleton(Modifier.fillMaxWidth(0.6f).height(14.dp), animated = false)
        }
      }
      Spacer(Modifier.height(16.dp))
      Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(c.scrim).padding(20.dp), contentAlignment = Alignment.Center) {
        MedAIDialogContent(g.dlgTitle, g.dlgMsg, g.dlgOk, {}, dismissText = g.dlgCancel, onDismiss = {}, destructive = true, icon = Icons.Rounded.Logout)
      }
    }
  }
}

private fun MedAITint2(c: com.example.ui.theme.MedAIColors) = com.example.ui.theme.MedAITint(c.dangerSoft, c.onDangerSoft)
