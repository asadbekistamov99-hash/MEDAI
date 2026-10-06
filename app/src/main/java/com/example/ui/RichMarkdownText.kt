@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Translations
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.ui.theme.*
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

@Composable
fun RichMarkdownText(text: String, modifier: Modifier = Modifier) {
    val medai = MedAITheme.colors

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val lines = text.split("\n")
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
            } else if (trimmed.startsWith("###") || trimmed.startsWith("##") || trimmed.startsWith("#")) {
                val headerText = trimmed.replace(Regex("^#+\\s*"), "").replace("**", "").replace("`", "")
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = medai.brand,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            } else if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                val bulletText = trimmed.substring(1).trim().replace("**", "").replace("`", "")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "•", style = MaterialTheme.typography.bodyMedium, color = medai.brand, fontWeight = FontWeight.Bold)
                    Text(
                        text = bulletText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = medai.textPrimary,
                        lineHeight = 20.sp
                    )
                }
            } else {
                // If it contains inline bold markers e.g. **text**
                val annotatedString = remember(trimmed) {
                    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
                    val parts = trimmed.split("**")
                    parts.forEachIndexed { index, part ->
                        if (index % 2 == 1) {
                            builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = medai.brand))
                            builder.append(part)
                            builder.pop()
                        } else {
                            builder.append(part)
                        }
                    }
                    builder.toAnnotatedString()
                }
                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = medai.textPrimary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
