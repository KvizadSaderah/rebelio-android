package com.kaidendev.rebelioclientandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaidendev.rebelioclientandroid.ui.theme.RebelioTypography

@Composable
fun ContactAvatar(
    routingToken: String,
    nickname: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val color = remember(routingToken) {
        generateColorFromToken(routingToken)
    }
    // Show nickname initials if available, else first 2 chars of token
    val initials = remember(routingToken, nickname) {
        if (!nickname.isNullOrBlank()) {
            nickname.take(2).uppercase()
        } else {
            routingToken.take(2).uppercase()
        }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, color.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = RebelioTypography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun generateColorFromToken(token: String): Color {
    val hash = token.hashCode()
    // Golden ratio conjugate for generating distinct colors
    val hue = (hash and 0xFF) / 255f * 360f
    // Saturation 0.7, Lightness 0.5 for nice pastel/vibrant colors
    return Color.hsl(hue, 0.7f, 0.4f)
}
