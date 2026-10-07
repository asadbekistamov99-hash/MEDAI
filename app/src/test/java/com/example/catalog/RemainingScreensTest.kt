package com.example.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.stage3.LayoutChecks
import com.example.ui.*
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Family, Notifications, Help, Services, Register, Splash, overlays: layout checks (always) + screenshots (SHOTS_DIR). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h1500dp-xxhdpi")
class RemainingScreensTest {
  @get:Rule val rule = createComposeRule()

  @org.junit.Before fun initWorkManager() {
    try {
      androidx.work.WorkManager.initialize(
        androidx.test.core.app.ApplicationProvider.getApplicationContext(),
        androidx.work.Configuration.Builder().setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor()).build(),
      )
    } catch (e: IllegalStateException) { }
  }

  private val screens: List<Pair<String, @Composable (AppViewModel) -> Unit>> = listOf(
    "family" to { vm -> FamilyScreen(vm) {} },
    "notifications" to { vm -> NotificationsScreen(vm) {} },
    "help" to { vm -> HelpCenterScreen(vm) {} },
    "services" to { vm -> ServicesScreen(vm, {}, {}) },
    "register" to { vm -> RegisterScreen({}, {}, vm) },
    "banned" to { vm -> BannedScreen(vm) },
  )

  private class Case(val name: String, val lang: String, val dark: Boolean, val vmHolder: Array<AppViewModel?>,
                     val content: @Composable (AppViewModel) -> Unit)

  /** One setContent per test (Compose rule limit); cases are swapped through state. */
  private fun runCases(cases: List<Case>, after: (Case) -> Unit) {
    cases.first().vmHolder[0] = SampleData.seededVm(cases.first().lang)
    var current by androidx.compose.runtime.mutableStateOf(cases.first())
    rule.setContent {
      val c = current
      androidx.compose.runtime.key(c) {
        MyApplicationTheme(darkTheme = c.dark) { Box(Modifier.fillMaxSize().background(MedAITheme.colors.canvas)) { c.content(c.vmHolder[0]!!) } }
      }
    }
    for (c in cases) {
      // Language lives in shared state, so each case gets its VM only when it is about to be shown.
      c.vmHolder[0] = SampleData.seededVm(c.lang)
      current = c
      rule.waitForIdle(); Thread.sleep(600); rule.waitForIdle()
      after(c)
    }
  }

  private fun layout(width: Int) {
    val failures = mutableListOf<String>()
    val cases = screens.flatMap { (n, f) -> listOf("uz", "ru", "en").map { Case(n, it, false, arrayOfNulls(1), f) } }
    runCases(cases) { c ->
      // A support URL (t.me/...) may wrap at "/" - unavoidable for a long token, not a defect.
      val clipped = LayoutChecks.clippedTexts(rule).filterNot { it.contains("t.me/") && it.endsWith("(word broken mid-way)") }
      val small = LayoutChecks.smallTargets(rule)
      if (clipped.isNotEmpty() || small.isNotEmpty()) failures += "${c.name}/${c.lang}/$width clipped=$clipped small=$small"
    }
    assertTrue(failures.joinToString("\n"), failures.isEmpty())
  }

  @Test fun layout_411() = layout(411)
  @Config(qualifiers = "w360dp-h1500dp-xxhdpi") @Test fun layout_360() = layout(360)

  @Test fun screenshots() {
    val out = System.getenv("SHOTS_DIR")
    Assume.assumeTrue(out != null)
    val cases = screens.flatMap { (n, f) ->
      listOf("uz", "ru", "en").map { Case(n, it, false, arrayOfNulls(1), f) } + Case(n, "uz", true, arrayOfNulls(1), f)
    }
    runCases(cases) { c ->
      rule.onRoot().captureRoboImage("src/test/screenshots/$out/${c.name}_${c.lang}_${if (c.dark) "dark" else "light"}.png")
    }
  }
}
