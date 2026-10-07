package com.example.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Stage 2: component gallery, light/dark x UZ/RU/EN, two pages. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w1245dp-h2600dp-hdpi")
class GalleryScreenshotTest {
  @get:Rule val rule = createComposeRule()

  private fun sheet(name: String, dark: Boolean, page: @Composable (GS, S) -> Unit) {
    rule.mainClock.autoAdvance = false
    rule.setContent {
      Row(Modifier.fillMaxSize().background(Color(0xFF94A3B8)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(GUZ to UZ, GRU to RU, GEN to EN).forEach { (g, s) ->
          Box(Modifier.width(411.dp).fillMaxHeight()) { MyApplicationTheme(darkTheme = dark) { page(g, s) } }
        }
      }
    }
    rule.mainClock.advanceTimeBy(300)
    rule.waitForIdle()
    rule.onRoot().captureRoboImage("src/test/screenshots/gallery/$name.png")
  }

  @Config(qualifiers = "w1245dp-h1750dp-hdpi")
  @Test fun p1_light() = sheet("1_tokens_buttons_inputs_light", false) { g, _ -> GalleryPage1(g) }
  @Config(qualifiers = "w1245dp-h1750dp-hdpi")
  @Test fun p1_dark() = sheet("1_tokens_buttons_inputs_dark", true) { g, _ -> GalleryPage1(g) }
  @Config(qualifiers = "w1245dp-h1300dp-hdpi")
  @Test fun p2_light() = sheet("2_cards_nav_states_light", false) { g, s -> GalleryPage2(g, s) }
  @Config(qualifiers = "w1245dp-h1300dp-hdpi")
  @Test fun p2_dark() = sheet("2_cards_nav_states_dark", true) { g, s -> GalleryPage2(g, s) }
}
