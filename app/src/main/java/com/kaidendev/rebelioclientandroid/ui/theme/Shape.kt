package com.kaidendev.rebelioclientandroid.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val RebelioShapes = Shapes(
    small = RoundedCornerShape(8.dp),    // Buttons, badges
    medium = RoundedCornerShape(16.dp),  // Cards, dialogs
    large = RoundedCornerShape(24.dp)    // Bottom sheets
)

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
