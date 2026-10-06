package com.example.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Stage 1: two Home design directions rendered with sample data (preview only, not app code). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h960dp-xxhdpi")
class HomeDirectionsScreenshotTest {
  @get:Rule val rule = createComposeRule()

  private fun shot(name: String, content: @Composable () -> Unit) {
    rule.setContent { content() }
    rule.waitForIdle()
    rule.onRoot().captureRoboImage("src/test/screenshots/directions/$name.png")
  }

  @Test fun a_uz() = shot("A_uz") { HomeDirectionA(UZ) }
  @Test fun b_uz() = shot("B_uz") { HomeDirectionB(UZ) }
  @Test fun c_uz() = shot("C_uz") { HomeDirectionC(UZ) }

  @Config(qualifiers = "w1245dp-h960dp-xxhdpi")
  @Test fun a_langs() = shot("A_uz_ru_en") { Sheet { HomeDirectionA(it) } }
  @Config(qualifiers = "w1245dp-h960dp-xxhdpi")
  @Test fun b_langs() = shot("B_uz_ru_en") { Sheet { HomeDirectionB(it) } }

  @Config(qualifiers = "w1245dp-h960dp-xxhdpi")
  @Test fun c_langs() = shot("C_uz_ru_en") { Sheet { HomeDirectionC(it) } }

  @Composable private fun Sheet(page: @Composable (S) -> Unit) {
    Row(Modifier.fillMaxSize().background(Color(0xFF94A3B8)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
      listOf(UZ, RU, EN).forEach { Box(Modifier.width(411.dp).fillMaxHeight()) { page(it) } }
    }
  }
}
