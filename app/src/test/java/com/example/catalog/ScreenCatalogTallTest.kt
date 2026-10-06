package com.example.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
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
 * Full-length (2400dp) variant of ScreenCatalogTest for long scrolling screens.
 * One screenshot of every secondary screen, light and dark, with realistic sample data.
 * Output folder is picked with the SHOTS_DIR env var (default "screens/current"), language with SHOTS_LANG.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h2400dp-xxhdpi")
class ScreenCatalogTallTest {
  @get:Rule val rule = createComposeRule()
  private val outDir = System.getenv("SHOTS_DIR") ?: "screens/current"
  private val lang = System.getenv("SHOTS_LANG") ?: "uz"

  @Before fun onlyWhenRequested() {
    // Screenshot generator, not a regression test: runs only when SHOTS_DIR is set.
    Assume.assumeTrue(System.getenv("SHOTS_DIR") != null)
  }

  private fun shot(name: String, dark: Boolean, content: @Composable (AppViewModel) -> Unit) {
    val vm = SampleData.seededVm(lang, admin = name == "admin")
    rule.setContent {
      MyApplicationTheme(darkTheme = dark) {
        // In the app the canvas comes from MainContainer / Scaffold; give standalone tab screens the same one.
        androidx.compose.foundation.layout.Box(
          androidx.compose.ui.Modifier.fillMaxSize().background(com.example.ui.theme.MedAITheme.colors.canvas)
        ) { content(vm) }
      }
    }
    rule.waitForIdle(); Thread.sleep(1200); rule.waitForIdle()
    rule.onRoot().captureRoboImage("src/test/screenshots/$outDir/${name}_${if (dark) "dark" else "light"}.png")
  }

  @Test fun profile_light() = shot("profile", false) { vm -> ProfileScreen(vm, rememberNavController()) }
  @Test fun profile_dark() = shot("profile", true) { vm -> ProfileScreen(vm, rememberNavController()) }
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
  @Test fun reminder_light() = shot("reminder", false) { vm -> ReminderScreen(vm) {} }
  @Test fun reminder_dark() = shot("reminder", true) { vm -> ReminderScreen(vm) {} }
  @Test fun admin_light() = shot("admin", false) { vm -> AdminScreen(vm) {} }
  @Test fun admin_dark() = shot("admin", true) { vm -> AdminScreen(vm) {} }
}
