package com.example.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.example.data.FirestoreVitalReading
import com.example.ui.*
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

/** Extra states for the Profile group: Analytics tabs, native vitals chart with data, dialogs. Needs SHOTS_DIR. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h1700dp-xxhdpi")
class ProfileGroupShotsTest {
  @get:Rule val rule = createComposeRule()
  private val out = System.getenv("SHOTS_DIR") ?: "screens/current"
  @Before fun gate() { Assume.assumeTrue(System.getenv("SHOTS_DIR") != null) }

  private fun shot(name: String, dark: Boolean, lang: String = "uz", steps: (() -> Unit)? = null, content: @Composable (AppViewModel) -> Unit) {
    val vm = SampleData.seededVm(lang)
    rule.setContent {
      MyApplicationTheme(darkTheme = dark) { Box(Modifier.fillMaxSize().background(MedAITheme.colors.canvas)) { content(vm) } }
    }
    rule.waitForIdle(); Thread.sleep(800); rule.waitForIdle()
    steps?.invoke()
    rule.waitForIdle(); Thread.sleep(400); rule.waitForIdle()
    rule.onRoot().captureRoboImage("src/test/screenshots/$out/${name}_${if (dark) "dark" else "light"}.png")
  }

  private fun click(text: String) { rule.onNodeWithText(text).performClick() }

  @Test fun analytics_tab1_light() = shot("analytics_tab1", false, steps = { click("Bosim va puls") }) { AnalyticsScreen(it) {} }
  @Test fun analytics_tab1_dark() = shot("analytics_tab1", true, steps = { click("Bosim va puls") }) { AnalyticsScreen(it) {} }
  @Test fun analytics_tab2_light() = shot("analytics_tab2", false, steps = { click("Uyqu va ovqat") }) { AnalyticsScreen(it) {} }
  @Test fun analytics_tab2_dark() = shot("analytics_tab2", true, steps = { click("Uyqu va ovqat") }) { AnalyticsScreen(it) {} }
  @Test fun analytics_ru_light() = shot("analytics_ru", false, lang = "ru") { AnalyticsScreen(it) {} }

  private val vitals = listOf(
    FirestoreVitalReading(date = "2026-09-24", heartRate = 72, bpSystolic = 122, bpDiastolic = 79, weight = 75.2),
    FirestoreVitalReading(date = "2026-09-26", heartRate = 70, bpSystolic = 119, bpDiastolic = 77, weight = 75.0),
    FirestoreVitalReading(date = "2026-09-28", heartRate = 76, bpSystolic = 126, bpDiastolic = 82, weight = 74.8),
    FirestoreVitalReading(date = "2026-09-30", heartRate = 68, bpSystolic = 118, bpDiastolic = 76, weight = 74.5),
    FirestoreVitalReading(date = "2026-10-02", heartRate = 74, bpSystolic = 121, bpDiastolic = 78, weight = 74.6),
    FirestoreVitalReading(date = "2026-10-04", heartRate = 69, bpSystolic = 117, bpDiastolic = 75, weight = 74.2),
  )
  @Test fun vitals_all_light() = shot("vitals_all", false) { VitalsTrendsContent(vitals, "uz") }
  @Test fun vitals_all_dark() = shot("vitals_all", true) { VitalsTrendsContent(vitals, "uz") }
  @Test fun vitals_hr_light() = shot("vitals_hr", false, steps = { click("Puls") }) { VitalsTrendsContent(vitals, "uz") }
  @Test fun vitals_weight_light() = shot("vitals_weight", false, steps = { click("Vazn") }) { VitalsTrendsContent(vitals, "uz") }

  @Test fun profile_logout_dialog_light() = shot("profile_logout", false, steps = { click("Chiqish") }) { ProfileScreen(it, rememberNavController()) }
  @Test fun profile_logout_dialog_dark() = shot("profile_logout", true, steps = { click("Chiqish") }) { ProfileScreen(it, rememberNavController()) }
  @Test fun profile_ru_light() = shot("profile_ru", false, lang = "ru") { ProfileScreen(it, rememberNavController()) }
  @Test fun upgrade_ru_dark() = shot("upgrade_ru", true, lang = "ru") { PremiumUpgradeScreen(it) {} }
}
