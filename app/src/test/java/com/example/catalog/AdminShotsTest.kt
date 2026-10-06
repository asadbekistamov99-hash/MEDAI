package com.example.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import com.example.ui.AdminScreen
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Every Admin tab (plus a dialog), light and dark, with populated data. Needs SHOTS_DIR. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h2000dp-xxhdpi")
class AdminShotsTest {
  @get:Rule val rule = createComposeRule()
  private val out = System.getenv("SHOTS_DIR") ?: "screens/current"
  private val lang = System.getenv("SHOTS_LANG") ?: "uz"
  @Before fun gate() { Assume.assumeTrue(System.getenv("SHOTS_DIR") != null) }

  /** [steps] are texts clicked in order (first match of each). */
  private fun shot(name: String, dark: Boolean, vararg steps: String) {
    val vm = AdminTestData.adminVm(lang)
    rule.setContent {
      MyApplicationTheme(darkTheme = dark) { Box(Modifier.fillMaxSize().background(MedAITheme.colors.canvas)) { AdminScreen(vm) {} } }
    }
    rule.waitForIdle(); Thread.sleep(800); rule.waitForIdle()
    steps.forEachIndexed { i, text ->
      val last = i == steps.lastIndex
      // A focused text field blinks its cursor forever, so a dialog never reaches "idle": step the clock by hand.
      if (last && name.contains("dialog")) rule.mainClock.autoAdvance = false
      rule.onAllNodesWithText(text, substring = text.endsWith("("))[0].performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick)
      if (last && name.contains("dialog")) rule.mainClock.advanceTimeBy(800) else { rule.waitForIdle(); Thread.sleep(300); rule.waitForIdle() }
    }
    rule.onRoot().captureRoboImage("src/test/screenshots/$out/${name}_${if (dark) "dark" else "light"}.png")
  }

  @Test fun users_light() = shot("admin_users", false, "Foydalanuvchilar")
  @Test fun users_dark() = shot("admin_users", true, "Foydalanuvchilar")
  @Test fun payments_light() = shot("admin_payments", false, "To'lovlar")
  @Test fun payments_dark() = shot("admin_payments", true, "To'lovlar")
  @Test fun system_light() = shot("admin_system", false, "Tizim")
  @Test fun system_dark() = shot("admin_system", true, "Tizim")
  @Test fun cms_light() = shot("admin_cms", false, "Tibbiy CMS")
  @Test fun cms_dark() = shot("admin_cms", true, "Tibbiy CMS")
  @Test fun logs_light() = shot("admin_logs", false, "Jurnallar")
  @Test fun logs_dark() = shot("admin_logs", true, "Jurnallar")
  @Test fun logs_errors_light() = shot("admin_logs_errors", false, "Jurnallar", "Xatolar (")
  @Test fun logs_errors_dark() = shot("admin_logs_errors", true, "Jurnallar", "Xatolar (")
}
