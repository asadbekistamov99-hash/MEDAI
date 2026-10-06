package com.example.stage3

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.core.app.ApplicationProvider

/** Objective layout checks shared by the stage-3 screen tests. */
object LayoutChecks {
  /** Text that is ellipsized, vertically clipped or broken in the middle of a word. */
  fun clippedTexts(rule: ComposeContentTestRule): List<String> {
    val bad = mutableListOf<String>()
    rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult)).fetchSemanticsNodes().forEach { n ->
      val out = mutableListOf<TextLayoutResult>()
      n.config[SemanticsActions.GetTextLayoutResult].action?.invoke(out)
      val r = out.firstOrNull() ?: return@forEach
      val text = r.layoutInput.text.text
      val txt = n.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text } ?: "<?>"
      var problem: String? = null
      if (r.didOverflowHeight) problem = "height clipped"
      for (i in 0 until r.lineCount) if (r.isLineEllipsized(i)) problem = "ellipsized"
      for (i in 0 until r.lineCount - 1) {
        val end = r.getLineEnd(i)
        if (end in 1 until text.length && !text[end - 1].isWhitespace() && !text[end].isWhitespace()) problem = "word broken mid-way"
      }
      if (problem != null) bad += "'$txt' ($problem)"
    }
    return bad
  }

  /** Clickable nodes smaller than 48dp in either direction. */
  fun smallTargets(rule: ComposeContentTestRule): List<String> {
    val density = ApplicationProvider.getApplicationContext<android.content.Context>().resources.displayMetrics.density
    val bad = mutableListOf<String>()
    rule.onAllNodes(hasClickAction()).fetchSemanticsNodes().forEach { n ->
      val w = n.size.width / density; val h = n.size.height / density
      if (w < 47.5f || h < 47.5f) {
        val label = n.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }
          ?: n.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
          ?: "<unlabelled>"
        bad += "'$label' ${"%.1f".format(w)}x${"%.1f".format(h)}dp"
      }
    }
    return bad
  }
}
