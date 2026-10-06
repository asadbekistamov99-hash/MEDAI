package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChatMessageLocal
import com.example.data.SymptomCheck
import com.example.data.UserLocal
import com.example.ui.AppViewModel
import com.example.ui.LoginScreen
import com.example.ui.MainContainer
import com.example.ui.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Captures the app's main screens as PNGs under src/test/screenshots/app. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AppScreensScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var vm: AppViewModel
  private val uid = "demo-uid"

  @Before
  fun setUp() {
    vm = AppViewModel(ApplicationProvider.getApplicationContext())
    runBlocking {
      vm.daoForTest.insertUser(
        UserLocal(
          uid = uid, name = "Aziz Karimov", email = "aziz@example.com", phone = "+998901234567",
          dateOfBirth = "1995-04-12", gender = "male", bloodType = "O+", height = 178.0,
          weight = 74.0, isPremium = false, premiumExpiry = null,
          trialStartedAt = System.currentTimeMillis(),
          trialEndsAt = System.currentTimeMillis() + UserLocal.TRIAL_DURATION_MS,
          language = "uz", fcmToken = "", createdAt = System.currentTimeMillis(),
          lastActive = System.currentTimeMillis(), healthScore = 82, isAdmin = false,
          isBanned = false, avatarUrl = ""
        )
      )
      vm.daoForTest.insertChatMessage(
        ChatMessageLocal(userId = uid, chatType = "general", role = "user", content = "Boshim og'riyapti, nima qilay?")
      )
      vm.daoForTest.insertChatMessage(
        ChatMessageLocal(userId = uid, chatType = "general", role = "model", content = "Ko'proq suv iching va dam oling. Og'riq davom etsa, shifokorga murojaat qiling.")
      )
    }
  }

  private fun shot(name: String, content: @androidx.compose.runtime.Composable () -> Unit) {
    composeTestRule.setContent { MyApplicationTheme { content() } }
    composeTestRule.waitForIdle()
    Thread.sleep(500)
    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/app/$name.png")
  }

  @Test fun onboarding() = shot("0_onboarding") { OnboardingScreen(onNavigateToLogin = {}, viewModel = vm) }

  @Test fun login() = shot("1_login") { LoginScreen(onNavigateToRegister = {}, onLoginSuccess = {}, viewModel = vm) }

  private fun main(name: String, tab: String?) {
    composeTestRule.setContent {
      MyApplicationTheme {
        val nav = rememberNavController()
        MainContainer(navController = nav, viewModel = vm, onNavigateToFeature = {})
      }
    }
    composeTestRule.waitForIdle()
    Thread.sleep(800)
    if (tab != null) {
      composeTestRule.onNodeWithText(tab).performClick()
      composeTestRule.waitForIdle()
      Thread.sleep(500)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/app/$name.png")
  }

  @Test fun home() = main("2_home", null)
  @Test fun history() = main("3_history", "Tarix")
  @Test fun aiChat() = main("4_ai_chat", "AI Chat")
  @Test fun profile() = main("5_profile", "Profil")
}
