package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MedAITheme

/** One parsed line of the markdown subset the AI answers use. */
private sealed interface MdBlock {
    data object Gap : MdBlock
    data class Heading(val text: String) : MdBlock
    data class Bullet(val marker: String, val text: AnnotatedString) : MdBlock
    data class Paragraph(val text: AnnotatedString) : MdBlock
}

private val headingPrefix = Regex("^#+\\s*")
private val numberedPrefix = Regex("^(\\d+)[.)]\\s+(.*)$")

/** Bold (**x**) becomes a semi-bold span; other markers are dropped. Pure, so it can be remembered per text. */
private fun inline(raw: String): AnnotatedString {
    val clean = raw.replace("`", "")
    val builder = AnnotatedString.Builder()
    clean.split("**").forEachIndexed { index, part ->
        if (index % 2 == 1) {
            builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            builder.append(part)
            builder.pop()
        } else {
            builder.append(part)
        }
    }
    return builder.toAnnotatedString()
}

private fun parse(text: String): List<MdBlock> = text.split("\n").map { line ->
    val trimmed = line.trim()
    when {
        trimmed.isEmpty() -> MdBlock.Gap
        trimmed.startsWith("#") -> MdBlock.Heading(trimmed.replace(headingPrefix, "").replace("**", "").replace("`", ""))
        trimmed.startsWith("- ") || trimmed.startsWith("* ") && !trimmed.startsWith("**") ->
            MdBlock.Bullet("•", inline(trimmed.substring(1).trim()))
        trimmed.startsWith("-") && trimmed.length > 1 && trimmed[1] != '-' -> MdBlock.Bullet("•", inline(trimmed.substring(1).trim()))
        numberedPrefix.matches(trimmed) -> {
            val m = numberedPrefix.matchEntire(trimmed)!!
            MdBlock.Bullet(m.groupValues[1] + ".", inline(m.groupValues[2]))
        }
        else -> MdBlock.Paragraph(inline(trimmed))
    }
}

/**
 * Renders the small markdown subset the AI returns (headings, bullets, numbered lists, **bold**).
 * Colours come from the theme, so it reads correctly on a light or dark surface.
 */
@Composable
fun RichMarkdownText(text: String, modifier: Modifier = Modifier) {
    val c = MedAITheme.colors
    val blocks = remember(text) { parse(text) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                MdBlock.Gap -> Spacer(Modifier.height(2.dp))
                is MdBlock.Heading -> Text(
                    text = block.text,
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                    modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                )
                is MdBlock.Bullet -> Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = block.marker,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.brand,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 14.dp),
                    )
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                }
                is MdBlock.Paragraph -> Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textPrimary,
                )
            }
        }
    }
}
