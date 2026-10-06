package com.example.catalog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChatMessageLocal
import com.example.data.LabResultLocal
import com.example.data.SymptomCheck
import com.example.data.UserLocal
import com.example.stage3.LayoutChecks
import kotlinx.coroutines.runBlocking
import org.robolectric.shadows.ShadowLooper
import com.example.ui.AIDoctorScreen
import com.example.ui.AITipsScreen
import com.example.ui.AppViewModel
import com.example.ui.DrugInfoScreen
import com.example.ui.LabAnalysisScreen
import com.example.ui.SymptomCheckerScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Symptom checker, AI doctor, AI tips, Drug info, Lab analysis: no clipped text, no tap target under 48dp, UZ/RU/EN at 411dp and 360dp. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class MedicalFeaturesLayoutTest {
  @get:Rule val rule = createComposeRule()

  private fun check(where: String, lang: String, content: @androidx.compose.runtime.Composable (AppViewModel) -> Unit) {
    // Not SampleData.seededVm: it seeds reminders, whose WorkManager scheduling throws under Robolectric.
    val vm = AppViewModel(ApplicationProvider.getApplicationContext())
    val now = System.currentTimeMillis()
    runBlocking {
      val dao = vm.daoForTest
      dao.insertUser(UserLocal(uid = "demo-uid", name = "Aziz Karimov", email = "aziz@example.com", phone = "+998901234567",
        dateOfBirth = "1995-04-12", gender = "male", bloodType = "O+", height = 178.0, weight = 74.0, isPremium = false, premiumExpiry = null,
        trialStartedAt = now, trialEndsAt = now + UserLocal.TRIAL_DURATION_MS, language = lang, fcmToken = "", createdAt = now,
        lastActive = now, healthScore = 82, isAdmin = false, isBanned = false, avatarUrl = ""))
      dao.insertSymptomCheck(SymptomCheck(userId = "demo-uid", symptomsInput = "Headache and fatigue", bodyPart = "Head", resultJson = "x", timestamp = now - 86_400_000L))
      dao.insertChatMessage(ChatMessageLocal(userId = "demo-uid", chatType = "doctor", role = "user", content = "I have a headache since morning", timestamp = now - 400_000L))
      dao.insertChatMessage(ChatMessageLocal(userId = "demo-uid", chatType = "doctor", role = "model", content = "Drink water, rest and see a doctor if it persists for more than two days.", timestamp = now - 300_000L))
      dao.insertLabResult(LabResultLocal(userId = "demo-uid", imagePath = "", analysisText = "Hemoglobin: 138 g/L (normal). Cholesterol: 5.6 mmol/L (high).", valuesJson = "[]", timestamp = now - 86_400_000L))
    }
    repeat(100) { if (vm.currentUser.value != null) return@repeat; ShadowLooper.idleMainLooper(); Thread.sleep(50) }
    vm.setLanguage(lang)
    repeat(20) { ShadowLooper.idleMainLooper(); Thread.sleep(25) }
    rule.setContent { MyApplicationTheme { content(vm) } }
    rule.waitForIdle(); Thread.sleep(900); rule.waitForIdle()
    val clipped = LayoutChecks.clippedTexts(rule)
    val small = LayoutChecks.smallTargets(rule)
    assertTrue("$where: clipped text: $clipped | small targets: $small", clipped.isEmpty() && small.isEmpty())
  }

  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun symptoms_uz_411() = check("symptoms/uz/411", "uz") { SymptomCheckerScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun symptoms_ru_411() = check("symptoms/ru/411", "ru") { SymptomCheckerScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun symptoms_en_411() = check("symptoms/en/411", "en") { SymptomCheckerScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun symptoms_uz_360() = check("symptoms/uz/360", "uz") { SymptomCheckerScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun symptoms_ru_360() = check("symptoms/ru/360", "ru") { SymptomCheckerScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun symptoms_en_360() = check("symptoms/en/360", "en") { SymptomCheckerScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun aidoctor_uz_411() = check("aidoctor/uz/411", "uz") { AIDoctorScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun aidoctor_ru_411() = check("aidoctor/ru/411", "ru") { AIDoctorScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun aidoctor_en_411() = check("aidoctor/en/411", "en") { AIDoctorScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun aidoctor_uz_360() = check("aidoctor/uz/360", "uz") { AIDoctorScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun aidoctor_ru_360() = check("aidoctor/ru/360", "ru") { AIDoctorScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun aidoctor_en_360() = check("aidoctor/en/360", "en") { AIDoctorScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun aitips_uz_411() = check("aitips/uz/411", "uz") { AITipsScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun aitips_ru_411() = check("aitips/ru/411", "ru") { AITipsScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun aitips_en_411() = check("aitips/en/411", "en") { AITipsScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun aitips_uz_360() = check("aitips/uz/360", "uz") { AITipsScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun aitips_ru_360() = check("aitips/ru/360", "ru") { AITipsScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun aitips_en_360() = check("aitips/en/360", "en") { AITipsScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun drugs_uz_411() = check("drugs/uz/411", "uz") { DrugInfoScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun drugs_ru_411() = check("drugs/ru/411", "ru") { DrugInfoScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun drugs_en_411() = check("drugs/en/411", "en") { DrugInfoScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun drugs_uz_360() = check("drugs/uz/360", "uz") { DrugInfoScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun drugs_ru_360() = check("drugs/ru/360", "ru") { DrugInfoScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun drugs_en_360() = check("drugs/en/360", "en") { DrugInfoScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun lab_uz_411() = check("lab/uz/411", "uz") { LabAnalysisScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun lab_ru_411() = check("lab/ru/411", "ru") { LabAnalysisScreen(it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun lab_en_411() = check("lab/en/411", "en") { LabAnalysisScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun lab_uz_360() = check("lab/uz/360", "uz") { LabAnalysisScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun lab_ru_360() = check("lab/ru/360", "ru") { LabAnalysisScreen(it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun lab_en_360() = check("lab/en/360", "en") { LabAnalysisScreen(it) {} }
}
