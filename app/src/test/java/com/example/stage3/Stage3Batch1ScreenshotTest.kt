package com.example.stage3

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.data.UserLocal
import com.example.ui.AppViewModel
import com.example.ui.FamilyScreen
import com.example.ui.LoginScreen
import com.example.ui.MainContainer
import com.example.ui.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/** Stage 3, batch 1: Home, Login, Onboarding, Family header (QR). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h914dp-xxhdpi")
class Stage3Batch1ScreenshotTest {
  @get:Rule val rule = createComposeRule()
  private val uid = "demo-uid"

  private fun vm(lang: String = "uz", paid: Boolean = false, trial: Boolean = true, score: Int = 82): AppViewModel {
    val vm = AppViewModel(ApplicationProvider.getApplicationContext())
    val now = System.currentTimeMillis()
    runBlocking {
      vm.daoForTest.insertUser(UserLocal(uid = uid, name = "Aziz Karimov", email = "aziz@example.com", phone = "+998901234567",
        dateOfBirth = "1995-04-12", gender = "male", bloodType = "O+", height = 178.0, weight = 74.0,
        isPremium = paid, premiumExpiry = if (paid) now + 20L * 86_400_000 else null,
        trialStartedAt = if (trial) now else now - 20L * 86_400_000,
        trialEndsAt = if (trial) now + UserLocal.TRIAL_DURATION_MS else now - 13L * 86_400_000,
        language = lang, fcmToken = "", createdAt = now, lastActive = now, healthScore = score,
        isAdmin = false, isBanned = false, avatarUrl = ""))
    }
    repeat(100) { if (vm.currentUser.value != null) return@repeat; ShadowLooper.idleMainLooper(); Thread.sleep(50) }
    vm.setLanguage(lang)
    repeat(20) { ShadowLooper.idleMainLooper(); Thread.sleep(25) }
    return vm
  }

  private fun shot(name: String, dark: Boolean, vm: AppViewModel, content: @androidx.compose.runtime.Composable (AppViewModel) -> Unit) {
    rule.setContent { MyApplicationTheme(darkTheme = dark) { content(vm) } }
    rule.waitForIdle(); Thread.sleep(900); rule.waitForIdle()
    rule.onRoot().captureRoboImage("src/test/screenshots/stage3/$name.png")
  }

  private val tall = "w411dp-h1500dp-xxhdpi"

  // ---- Home (trial = premium layout) ----
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun home_uz_light() = shot("home_uz_light", false, vm()) { MainContainer(rememberNavController(), it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun home_uz_dark() = shot("home_uz_dark", true, vm()) { MainContainer(rememberNavController(), it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun home_ru_light() = shot("home_ru_light", false, vm("ru")) { MainContainer(rememberNavController(), it) {} }
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun home_en_light() = shot("home_en_light", false, vm("en")) { MainContainer(rememberNavController(), it) {} }
  // free layout: trial over, not paid, low score
  @Config(qualifiers = "w411dp-h1500dp-xxhdpi") @Test fun home_free_uz_light() = shot("home_free_uz_light", false, vm(trial = false, score = 35)) { MainContainer(rememberNavController(), it) {} }
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun home_ru_360() = shot("home_ru_360", false, vm("ru")) { MainContainer(rememberNavController(), it) {} }

  // ---- Login ----
  @Test fun login_uz_light() = shot("login_uz_light", false, vm()) { LoginScreen({}, {}, it) }
  @Test fun login_uz_dark() = shot("login_uz_dark", true, vm()) { LoginScreen({}, {}, it) }
  @Test fun login_ru_light() = shot("login_ru_light", false, vm("ru")) { LoginScreen({}, {}, it) }
  @Test fun login_en_light() = shot("login_en_light", false, vm("en")) { LoginScreen({}, {}, it) }

  // ---- Onboarding ----
  @Test fun onboarding_uz_light() = shot("onboarding_uz_light", false, vm()) { OnboardingScreen({}, it) }
  @Test fun onboarding_uz_dark() = shot("onboarding_uz_dark", true, vm()) { OnboardingScreen({}, it) }
  @Test fun onboarding_ru_light() = shot("onboarding_ru_light", false, vm("ru")) { OnboardingScreen({}, it) }
  @Test fun onboarding_en_light() = shot("onboarding_en_light", false, vm("en")) { OnboardingScreen({}, it) }

  // ---- Family (QR button now lives in the header) ----
  @Test fun family_uz_light() = shot("family_uz_light", false, vm()) { FamilyScreen(it) {} }
}
