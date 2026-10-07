package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.data.UserLocal
import com.example.ui.AppViewModel
import com.example.ui.LoginScreen
import com.example.ui.MainContainer
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/** Design audit captures: full-length screens and a Russian locale pass. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h914dp-xxhdpi")
class AuditScreenshotTest {
  @get:Rule val rule = createComposeRule()
  private lateinit var vm: AppViewModel
  private val uid = "demo-uid"

  @Before fun setUp() {
    vm = AppViewModel(ApplicationProvider.getApplicationContext())
    runBlocking {
      val now = System.currentTimeMillis()
      vm.daoForTest.insertUser(UserLocal(uid = uid, name = "Aziz Karimov", email = "aziz@example.com",
        phone = "+998901234567", dateOfBirth = "1995-04-12", gender = "male", bloodType = "O+", height = 178.0,
        weight = 74.0, isPremium = false, premiumExpiry = null, trialStartedAt = now,
        trialEndsAt = now + UserLocal.TRIAL_DURATION_MS, language = "uz", fcmToken = "", createdAt = now,
        lastActive = now, healthScore = 82, isAdmin = false, isBanned = false, avatarUrl = ""))
    }
  }

  private fun waitUser() {
    repeat(100) { if (vm.currentUser.value != null) return; ShadowLooper.idleMainLooper(); Thread.sleep(50) }
  }

  private fun main(name: String, tab: String?, lang: String = "uz") {
    waitUser()
    vm.setLanguage(lang)
    rule.setContent { MyApplicationTheme { MainContainer(rememberNavController(), vm) {} } }
    rule.waitForIdle(); Thread.sleep(800)
    if (tab != null) { rule.onNodeWithTextCompat(tab); rule.waitForIdle(); Thread.sleep(500) }
    rule.onRoot().captureRoboImage("src/test/screenshots/audit/$name.png")
  }

  private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onNodeWithTextCompat(t: String) {
    onNodeWithText(t).performClick()
  }

  @Config(qualifiers = "w411dp-h2300dp-xxhdpi") @Test fun home_tall() = main("home_tall", null)
  @Config(qualifiers = "w411dp-h2300dp-xxhdpi") @Test fun profile_tall() = main("profile_tall", "Profil")
  @Test fun home_ru() = main("home_ru", null, "ru")
  @Test fun login_ru() {
    waitUser(); vm.setLanguage("ru")
    rule.setContent { MyApplicationTheme { LoginScreen({}, {}, vm) } }
    rule.waitForIdle(); Thread.sleep(500)
    rule.onRoot().captureRoboImage("src/test/screenshots/audit/login_ru.png")
  }
}
