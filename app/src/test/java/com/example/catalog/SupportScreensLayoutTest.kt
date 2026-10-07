package com.example.catalog

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.FirestoreMedicationReminder
import com.example.stage3.LayoutChecks
import com.example.ui.AppViewModel
import com.example.ui.GeneralChatScreen
import com.example.ui.HistoryScreen
import com.example.ui.ReminderCard
import com.example.ui.ReminderFormContent
import com.example.ui.ReminderFormState
import com.example.ui.ReminderScreen
import com.example.ui.SOSScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * History, AI Chat, Reminders and SOS in UZ/RU/EN at 411dp and 360dp: no clipped text, no tap target under 48dp.
 *
 * Documented exception: the AI result preview on a History card is deliberately limited to 3 lines
 * (tap to expand), so "ellipsized" on exactly that text (it starts with the SampleData answer) is expected.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class SupportScreensLayoutTest {
  @get:Rule val rule = createComposeRule()

  // SampleData seeds reminders; the view model then schedules WorkManager jobs, which throws on the main
  // dispatcher (and poisons the next test) unless WorkManager is initialised.
  @org.junit.Before fun initWorkManager() {
    try {
      androidx.work.WorkManager.initialize(
        androidx.test.core.app.ApplicationProvider.getApplicationContext(),
        androidx.work.Configuration.Builder().setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor()).build(),
      )
    } catch (e: IllegalStateException) { /* already initialised by an earlier test */ }
  }

  private val sampleReminder = FirestoreMedicationReminder(
    id = "r1", userId = "demo-uid", medicineName = "Paratsetamol", dosage = "1 tablet", time = "08:00", frequency = "Every 12 Hours",
    notificationsEnabled = true, notificationFrequency = "15m before", isActive = true, targetFamilyMember = "Malika Karimova",
    notes = "After meals with plenty of water", completedDates = emptyList(),
  )

  private fun check(where: String, lang: String, allowTruncatedHistoryPreview: Boolean = false, content: @Composable (AppViewModel) -> Unit) {
    val vm = SampleData.seededVm(lang)
    rule.setContent { MyApplicationTheme { content(vm) } }
    rule.waitForIdle(); Thread.sleep(900); rule.waitForIdle()
    var clipped = LayoutChecks.clippedTexts(rule)
    if (allowTruncatedHistoryPreview) clipped = clipped.filterNot { it.startsWith("'Ehtimoliy sabablar") && it.endsWith("(ellipsized)") }
    val small = LayoutChecks.smallTargets(rule)
    assertTrue("$where: clipped text: $clipped | small targets: $small", clipped.isEmpty() && small.isEmpty())
  }

  // ---- 411dp ----
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun history_uz_411() = check("history/uz/411", "uz", true) { HistoryScreen(it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun history_ru_411() = check("history/ru/411", "ru", true) { HistoryScreen(it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun history_en_411() = check("history/en/411", "en", true) { HistoryScreen(it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun chat_uz_411() = check("chat/uz/411", "uz") { GeneralChatScreen(it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun chat_ru_411() = check("chat/ru/411", "ru") { GeneralChatScreen(it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun chat_en_411() = check("chat/en/411", "en") { GeneralChatScreen(it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun reminder_uz_411() = check("reminder/uz/411", "uz") { ReminderScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun reminder_ru_411() = check("reminder/ru/411", "ru") { ReminderScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun reminder_en_411() = check("reminder/en/411", "en") { ReminderScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun reminderParts_uz_411() = check("reminderParts/uz/411", "uz") { Parts("uz") }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun reminderParts_ru_411() = check("reminderParts/ru/411", "ru") { Parts("ru") }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun reminderParts_en_411() = check("reminderParts/en/411", "en") { Parts("en") }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun sos_uz_411() = check("sos/uz/411", "uz") { SOSScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun sos_ru_411() = check("sos/ru/411", "ru") { SOSScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun sos_en_411() = check("sos/en/411", "en") { SOSScreen(it) {} }

  // ---- 360dp ----
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun history_uz_360() = check("history/uz/360", "uz", true) { HistoryScreen(it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun history_ru_360() = check("history/ru/360", "ru", true) { HistoryScreen(it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun history_en_360() = check("history/en/360", "en", true) { HistoryScreen(it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun chat_uz_360() = check("chat/uz/360", "uz") { GeneralChatScreen(it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun chat_ru_360() = check("chat/ru/360", "ru") { GeneralChatScreen(it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun chat_en_360() = check("chat/en/360", "en") { GeneralChatScreen(it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun reminder_uz_360() = check("reminder/uz/360", "uz") { ReminderScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun reminder_ru_360() = check("reminder/ru/360", "ru") { ReminderScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun reminder_en_360() = check("reminder/en/360", "en") { ReminderScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun reminderParts_uz_360() = check("reminderParts/uz/360", "uz") { Parts("uz") }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun reminderParts_ru_360() = check("reminderParts/ru/360", "ru") { Parts("ru") }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun reminderParts_en_360() = check("reminderParts/en/360", "en") { Parts("en") }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun sos_uz_360() = check("sos/uz/360", "uz") { SOSScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun sos_ru_360() = check("sos/ru/360", "ru") { SOSScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun sos_en_360() = check("sos/en/360", "en") { SOSScreen(it) {} }

  /**
   * The pieces of the Reminders screen that the seeded view model cannot reach: a medicine card (the reminders list
   * comes from Firestore, which is empty in tests) and the add-reminder form (it lives in a Dialog window).
   * They are composed inline here.
   */
  @Composable
  private fun Parts(lang: String) {
    androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.padding(16.dp)) {
      ReminderCard(lang, sampleReminder, isCompletedToday = false, onToggleActive = {}, onComplete = {}, onDelete = {})
      ReminderFormContent(lang, ReminderFormState(lang), listOf("Malika Karimova", "Jasur Karimov"), onSave = {}, onCancel = {})
    }
  }
}
