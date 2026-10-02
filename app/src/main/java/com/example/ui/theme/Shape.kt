package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape scale.
 *
 * Cards are generously rounded (20dp) and controls less so (12–14dp). A single radius for
 * everything is the most common giveaway of an amateur UI: pills for chips, soft rectangles
 * for cards, and a tighter curve on buttons so the label never looks like it is floating.
 */
val MedAIShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// Spacing scale. Compose has no spacing token system, so this keeps ad-hoc 4/8/12/16 values
// from drifting across the 16 screens.
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val section = 32.dp
}

object MedAICorners {
    val card = 20.dp
    val cardLarge = 24.dp
    val control = 14.dp
    val pill = 999.dp
}
