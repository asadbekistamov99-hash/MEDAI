package com.example.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Objective checks for the kit: every tappable is >= 48dp in both axes, and no text is
 * clipped or ellipsized in UZ / RU / EN on 411dp and 360dp wide phones, light and dark.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h2600dp-xxhdpi")
class MedAIKitAccessibilityTest {
  @get:Rule val rule = createComposeRule()

  private val tappable = listOf(
    "btn_primary", "btn_primary_long", "btn_loading", "btn_secondary", "btn_danger", "btn_text",
    "chip_selected", "chip_1", "chip_2", "tile_0", "tile_1", "tile_2", "tile_3",
    "row_0", "row_1", "row_2",
  )

  private fun show(dark: Boolean, g: GS, s: S) {
    rule.mainClock.autoAdvance = false
    rule.setContent {
      MyApplicationTheme(darkTheme = dark) {
        Box(Modifier.fillMaxSize()) { GalleryPage1(g) }
      }
    }
    rule.mainClock.advanceTimeBy(200)
    rule.waitForIdle()
  }

  private fun showPage2(dark: Boolean, g: GS, s: S) {
    rule.mainClock.autoAdvance = false
    rule.setContent { MyApplicationTheme(darkTheme = dark) { Box(Modifier.fillMaxSize()) { GalleryPage2(g, s) } } }
    rule.mainClock.advanceTimeBy(200)
    rule.waitForIdle()
  }

  private fun assertNoClippedText(where: String) {
    val bad = mutableListOf<String>()
    rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult)).fetchSemanticsNodes().forEach { n ->
      val out = mutableListOf<TextLayoutResult>()
      n.config[SemanticsActions.GetTextLayoutResult].action?.invoke(out)
      val r = out.firstOrNull() ?: return@forEach
      val txt = n.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text } ?: "<?>"
      val text = r.layoutInput.text.text
      var problem: String? = null
      if (r.didOverflowHeight) problem = "height clipped"
      for (i in 0 until r.lineCount) if (r.isLineEllipsized(i)) problem = "ellipsized"
      for (i in 0 until r.lineCount - 1) {
        val end = r.getLineEnd(i)
        if (end in 1 until text.length && !text[end - 1].isWhitespace() && !text[end].isWhitespace()) problem = "word broken mid-way"
      }
      if (problem != null) bad += "$txt ($problem)"
    }
    assertTrue("$where: clipped/ellipsized text: $bad", bad.isEmpty())
  }

  private fun assertTouchTargets(where: String) {
    for (tag in tappable + (0..3).map { "bottom_item_$it" }) {
      val node = runCatching { rule.onNodeWithTag(tag, useUnmergedTree = true) }.getOrNull() ?: continue
      runCatching { node.assertExists() }.onFailure { continue }
      try {
        node.assertHeightIsAtLeast(48.dp)
        node.assertWidthIsAtLeast(48.dp)
      } catch (e: AssertionError) {
        throw AssertionError("$where: touch target '$tag' is below 48dp: ${e.message}")
      }
    }
  }

  private fun case(where: String, dark: Boolean, lang: String, page: Int) {
    val (g, st) = when (lang) { "uz" -> GUZ to UZ; "ru" -> GRU to RU; else -> GEN to EN }
    if (page == 1) show(dark, g, st) else showPage2(dark, g, st)
    assertNoClippedText("$where/$lang/p$page")
    assertTouchTargets("$where/$lang/p$page")
  }

  // 411dp, light
  @Test fun w411_light_uz_p1() = case("411/light", false, "uz", 1)
  @Test fun w411_light_ru_p1() = case("411/light", false, "ru", 1)
  @Test fun w411_light_en_p1() = case("411/light", false, "en", 1)
  @Test fun w411_light_uz_p2() = case("411/light", false, "uz", 2)
  @Test fun w411_light_ru_p2() = case("411/light", false, "ru", 2)
  @Test fun w411_light_en_p2() = case("411/light", false, "en", 2)

  // 360dp (small phone), dark
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun w360_dark_uz_p1() = case("360/dark", true, "uz", 1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun w360_dark_ru_p1() = case("360/dark", true, "ru", 1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun w360_dark_en_p1() = case("360/dark", true, "en", 1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun w360_dark_uz_p2() = case("360/dark", true, "uz", 2)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun w360_dark_ru_p2() = case("360/dark", true, "ru", 2)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun w360_dark_en_p2() = case("360/dark", true, "en", 2)
}
