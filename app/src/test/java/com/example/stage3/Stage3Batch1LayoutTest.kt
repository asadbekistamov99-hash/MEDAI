package com.example.stage3

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.data.UserLocal
import com.example.ui.AppViewModel
import com.example.ui.LoginScreen
import com.example.ui.MainContainer
import com.example.ui.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/** Stage 3 / batch 1: no clipped text and 48dp targets for Home, Login and Onboarding in UZ/RU/EN at 411dp and 360dp. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class Stage3Batch1LayoutTest {
  @get:Rule val rule = createComposeRule()

  private fun check(where: String, lang: String, content: @androidx.compose.runtime.Composable (AppViewModel) -> Unit) {
    val vm = AppViewModel(ApplicationProvider.getApplicationContext())
    val now = System.currentTimeMillis()
    runBlocking {
      vm.daoForTest.insertUser(UserLocal(uid = "demo-uid", name = "Aziz Karimov", email = "aziz@example.com", phone = "+998901234567",
        dateOfBirth = "1995-04-12", gender = "male", bloodType = "O+", height = 178.0, weight = 74.0, isPremium = false, premiumExpiry = null,
        trialStartedAt = now, trialEndsAt = now + UserLocal.TRIAL_DURATION_MS, language = lang, fcmToken = "", createdAt = now,
        lastActive = now, healthScore = 82, isAdmin = false, isBanned = false, avatarUrl = ""))
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

  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun home_uz_411() = check("home/uz/411", "uz") { MainContainer(rememberNavController(), it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun home_ru_411() = check("home/ru/411", "ru") { MainContainer(rememberNavController(), it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun home_en_411() = check("home/en/411", "en") { MainContainer(rememberNavController(), it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun home_uz_360() = check("home/uz/360", "uz") { MainContainer(rememberNavController(), it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun home_ru_360() = check("home/ru/360", "ru") { MainContainer(rememberNavController(), it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun home_en_360() = check("home/en/360", "en") { MainContainer(rememberNavController(), it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun login_uz_411() = check("login/uz/411", "uz") { LoginScreen({}, {}, it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun login_ru_411() = check("login/ru/411", "ru") { LoginScreen({}, {}, it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun login_en_411() = check("login/en/411", "en") { LoginScreen({}, {}, it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun login_uz_360() = check("login/uz/360", "uz") { LoginScreen({}, {}, it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun login_ru_360() = check("login/ru/360", "ru") { LoginScreen({}, {}, it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun login_en_360() = check("login/en/360", "en") { LoginScreen({}, {}, it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun onboarding_uz_411() = check("onboarding/uz/411", "uz") { OnboardingScreen({}, it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun onboarding_ru_411() = check("onboarding/ru/411", "ru") { OnboardingScreen({}, it) }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun onboarding_en_411() = check("onboarding/en/411", "en") { OnboardingScreen({}, it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun onboarding_uz_360() = check("onboarding/uz/360", "uz") { OnboardingScreen({}, it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun onboarding_ru_360() = check("onboarding/ru/360", "ru") { OnboardingScreen({}, it) }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun onboarding_en_360() = check("onboarding/en/360", "en") { OnboardingScreen({}, it) }
}
