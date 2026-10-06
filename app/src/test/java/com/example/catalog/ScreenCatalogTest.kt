package com.example.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * One screenshot of every secondary screen, light and dark, with realistic sample data.
 * Output folder is picked with the SHOTS_DIR env var (default "screens/current"), language with SHOTS_LANG.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h914dp-xxhdpi")
class ScreenCatalogTest {
  @get:Rule val rule = createComposeRule()
  private val uid = "demo-uid"
  private val outDir = System.getenv("SHOTS_DIR") ?: "screens/current"
  private val lang = System.getenv("SHOTS_LANG") ?: "uz"

  @Before fun onlyWhenRequested() {
    // Screenshot generator, not a regression test: runs only when SHOTS_DIR is set.
    Assume.assumeTrue(System.getenv("SHOTS_DIR") != null)
  }

  private fun seededVm(admin: Boolean = false): AppViewModel {
    val vm = AppViewModel(ApplicationProvider.getApplicationContext())
    val now = System.currentTimeMillis()
    val day = 86_400_000L
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    runBlocking {
      val dao = vm.daoForTest
      dao.insertUser(UserLocal(uid = uid, name = "Aziz Karimov", email = if (admin) "admin@example.com" else "aziz@example.com", phone = "+998901234567",
        dateOfBirth = "1995-04-12", gender = "male", bloodType = "O+", height = 178.0, weight = 74.0, isPremium = false, premiumExpiry = null,
        trialStartedAt = now, trialEndsAt = now + 5 * day, language = lang, fcmToken = "", createdAt = now, lastActive = now, healthScore = 74,
        isAdmin = admin, isBanned = false, avatarUrl = "",
        allergiesJson = "[{\"name\":\"Penitsillin\",\"type\":\"Dori\",\"addedAt\":0},{\"name\":\"Yeryong'oq\",\"type\":\"Taom\",\"addedAt\":0}]",
        unlockedAchievementsJson = "[\"beginner\",\"water\"]"))
      dao.insertDailyMetrics(DailyHealthMetricsLocal(userId = uid, date = today, waterGlasses = 5, waterGoal = 8, weight = 74.0,
        bpSystolic = 118, bpDiastolic = 76, heartRate = 68, sleepHours = 7.3, steps = 4230))
      listOf("Boshim og'riyapti va holsizlik his qilyapman" to "Bosh", "Tomoqda og'riq, yo'tal 2 kundan beri" to "Tomoq", "Qorin og'rig'i, ko'ngil aynish" to "Qorin").forEachIndexed { i, (t, p) ->
        dao.insertSymptomCheck(SymptomCheck(userId = uid, symptomsInput = t, bodyPart = p,
          resultJson = "Ehtimoliy sabablar: charchoq, suvsizlanish, shamollash alomatlari. Ko'proq suyuqlik iching, dam oling. Holat 48 soatda yaxshilanmasa, shifokorga murojaat qiling.",
          timestamp = now - (i + 1) * day))
      }
      dao.insertChatMessage(ChatMessageLocal(userId = uid, chatType = "general", role = "user", content = "Boshim og'riyapti, nima qilay?", timestamp = now - 4000))
      dao.insertChatMessage(ChatMessageLocal(userId = uid, chatType = "general", role = "model", content = "Ko'proq suv iching va dam oling. Og'riq davom etsa, shifokorga murojaat qiling.", timestamp = now - 3000))
      dao.insertChatMessage(ChatMessageLocal(userId = uid, chatType = "general", role = "user", content = "Qancha suv ichishim kerak?", timestamp = now - 2000))
      dao.insertChatMessage(ChatMessageLocal(userId = uid, chatType = "general", role = "model", content = "Kuniga o'rtacha 1,5–2 litr, ya'ni taxminan 8 stakan.", timestamp = now - 1000))
      dao.insertChatMessage(ChatMessageLocal(userId = uid, chatType = "doctor", role = "model", content = "Salom! Men MedAI shifokoriman. Sizni nima bezovta qilyapti?", timestamp = now - 500))
      dao.insertReminder(ReminderLocal(userId = uid, medicineName = "Vitamin D", time = "08:00", frequency = "daily", type = "Dori", notes = "Ovqatdan keyin"))
      dao.insertReminder(ReminderLocal(userId = uid, medicineName = "Suv ichish", time = "12:30", frequency = "daily", type = "Suv"))
      dao.insertReminder(ReminderLocal(userId = uid, medicineName = "Terapevt qabuli", time = "17:00", frequency = "weekly", type = "Doctor"))
      listOf(Triple("m1", "Malika Karimova", "Spouse"), Triple("m2", "Jasur Karimov", "Child"), Triple("m3", "Soliha Karimova", "Mother")).forEachIndexed { i, (id, name, rel) ->
        dao.insertFamilyMember(FamilyMemberLocal(uid = id, ownerUserId = uid, name = name, email = "$id@example.com", phone = "+99890000000$i", relation = rel,
          avatarUrl = "", healthScore = 70 + i * 8, stepsToday = 3000 + i * 1500, lastActive = now - i * 3_600_000L, activeRemindersCount = i,
          sosStatus = false, inviteStatus = if (i == 2) "pending" else "accepted", isInvitedByMe = true))
      }
      dao.insertNotification(NotificationLocal(userId = uid, title = "Dori vaqti", message = "Vitamin D qabul qilish vaqti keldi.", type = "reminder", timestamp = now - 600_000))
      dao.insertNotification(NotificationLocal(userId = uid, title = "7 kunlik bepul Premium sinov", message = "Barcha imkoniyatlar 5 kun davomida ochiq.", type = "premium", isRead = true, timestamp = now - day))
      dao.insertNotification(NotificationLocal(userId = uid, title = "Oila taklifi", message = "Soliha Karimova sizni oilaga qo'shdi.", type = "invite", isRead = true, timestamp = now - 2 * day))
      dao.insertLabResult(LabResultLocal(userId = uid, imagePath = "", analysisText = "Gemoglobin: 138 g/L (norma). Leykotsitlar: 6,2 (norma). Xolesterin: 5,6 mmol/L (biroz yuqori).", valuesJson = "[]", timestamp = now - 3 * day))
    }
    repeat(100) { if (vm.currentUser.value != null) return@repeat; ShadowLooper.idleMainLooper(); Thread.sleep(50) }
    vm.setLanguage(lang)
    repeat(30) { ShadowLooper.idleMainLooper(); Thread.sleep(25) }
    return vm
  }

  private fun shot(name: String, dark: Boolean, content: @Composable (AppViewModel) -> Unit) {
    val vm = seededVm(admin = name == "admin")
    rule.setContent { MyApplicationTheme(darkTheme = dark) { content(vm) } }
    rule.waitForIdle(); Thread.sleep(1200); rule.waitForIdle()
    rule.onRoot().captureRoboImage("src/test/screenshots/$outDir/${name}_${if (dark) "dark" else "light"}.png")
  }

  @Test fun profile_light() = shot("profile", false) { vm -> ProfileScreen(vm, rememberNavController()) }
  @Test fun profile_dark() = shot("profile", true) { vm -> ProfileScreen(vm, rememberNavController()) }
  @Test fun history_light() = shot("history", false) { vm -> HistoryScreen(vm) }
  @Test fun history_dark() = shot("history", true) { vm -> HistoryScreen(vm) }
  @Test fun chat_light() = shot("chat", false) { vm -> GeneralChatScreen(vm) }
  @Test fun chat_dark() = shot("chat", true) { vm -> GeneralChatScreen(vm) }
  @Test fun family_light() = shot("family", false) { vm -> FamilyScreen(vm) {} }
  @Test fun family_dark() = shot("family", true) { vm -> FamilyScreen(vm) {} }
  @Test fun upgrade_light() = shot("upgrade", false) { vm -> PremiumUpgradeScreen(vm) {} }
  @Test fun upgrade_dark() = shot("upgrade", true) { vm -> PremiumUpgradeScreen(vm) {} }
  @Test fun analytics_light() = shot("analytics", false) { vm -> AnalyticsScreen(vm) {} }
  @Test fun analytics_dark() = shot("analytics", true) { vm -> AnalyticsScreen(vm) {} }
  @Test fun services_light() = shot("services", false) { vm -> ServicesScreen(vm, {}, {}) }
  @Test fun services_dark() = shot("services", true) { vm -> ServicesScreen(vm, {}, {}) }
  @Test fun help_light() = shot("help", false) { vm -> HelpCenterScreen(vm) {} }
  @Test fun help_dark() = shot("help", true) { vm -> HelpCenterScreen(vm) {} }
  @Test fun notifications_light() = shot("notifications", false) { vm -> NotificationsScreen(vm) {} }
  @Test fun notifications_dark() = shot("notifications", true) { vm -> NotificationsScreen(vm) {} }
  @Test fun yordamchi_light() = shot("yordamchi", false) { vm -> MedAIYordamchiScreen(vm, onBack = {}) }
  @Test fun yordamchi_dark() = shot("yordamchi", true) { vm -> MedAIYordamchiScreen(vm, onBack = {}) }
  @Test fun symptoms_light() = shot("symptoms", false) { vm -> SymptomCheckerScreen(vm) {} }
  @Test fun symptoms_dark() = shot("symptoms", true) { vm -> SymptomCheckerScreen(vm) {} }
  @Test fun ai_doctor_light() = shot("ai_doctor", false) { vm -> AIDoctorScreen(vm) {} }
  @Test fun ai_doctor_dark() = shot("ai_doctor", true) { vm -> AIDoctorScreen(vm) {} }
  @Test fun ai_tips_light() = shot("ai_tips", false) { vm -> AITipsScreen(vm) {} }
  @Test fun ai_tips_dark() = shot("ai_tips", true) { vm -> AITipsScreen(vm) {} }
  @Test fun drugs_light() = shot("drugs", false) { vm -> DrugInfoScreen(vm) {} }
  @Test fun drugs_dark() = shot("drugs", true) { vm -> DrugInfoScreen(vm) {} }
  @Test fun lab_light() = shot("lab", false) { vm -> LabAnalysisScreen(vm) {} }
  @Test fun lab_dark() = shot("lab", true) { vm -> LabAnalysisScreen(vm) {} }
  @Test fun reminder_light() = shot("reminder", false) { vm -> ReminderScreen(vm) {} }
  @Test fun reminder_dark() = shot("reminder", true) { vm -> ReminderScreen(vm) {} }
  @Test fun sos_light() = shot("sos", false) { vm -> SOSScreen(vm) {} }
  @Test fun sos_dark() = shot("sos", true) { vm -> SOSScreen(vm) {} }
  @Test fun register_light() = shot("register", false) { vm -> RegisterScreen({}, {}, vm) }
  @Test fun register_dark() = shot("register", true) { vm -> RegisterScreen({}, {}, vm) }
  @Test fun admin_light() = shot("admin", false) { vm -> AdminScreen(vm) {} }
  @Test fun admin_dark() = shot("admin", true) { vm -> AdminScreen(vm) {} }
}
