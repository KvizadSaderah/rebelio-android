package com.kaidendev.rebelioclientandroid.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kaidendev.rebelioclientandroid.ui.components.GlowButton
import com.kaidendev.rebelioclientandroid.ui.theme.MatrixGreen
import com.kaidendev.rebelioclientandroid.ui.theme.NeonCyan
import com.kaidendev.rebelioclientandroid.ui.theme.RebelioTypography
import com.kaidendev.rebelioclientandroid.ui.theme.Spacing
import com.kaidendev.rebelioclientandroid.ui.theme.TextPrimary
import com.kaidendev.rebelioclientandroid.ui.theme.TextMuted

@Composable
fun WelcomeScreen(
    onStartClicked: () -> Unit,
    onImportIdentity: () -> Unit = {}
) {
    val logoScale = remember { Animatable(0.8f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, animationSpec = tween(500))
        contentAlpha.animateTo(1f, animationSpec = tween(500))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            
            // Animated logo
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Rebelio Logo",
                tint = MatrixGreen,
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale.value)
            )
            
            Spacer(Modifier.height(Spacing.xl))
            
            Column(modifier = Modifier.alpha(contentAlpha.value)) {
                // Tagline
                Text(
                    text = "REBELIO",
                    style = RebelioTypography.displayLarge,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "The Private Messenger",
                    style = RebelioTypography.titleMedium,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(Modifier.height(Spacing.xxl))
                
                // Feature list
                FeatureList(
                    features = listOf(
                        "No phone number required",
                        "No email required", 
                        "Post-quantum encryption"
                    )
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Create New Account
                GlowButton(
                    text = "CREATE NEW ACCOUNT",
                    onClick = onStartClicked
                )
                
                Spacer(Modifier.height(Spacing.md))
                
                // Import Existing Identity
                OutlinedButton(
                    onClick = onImportIdentity,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeonCyan,
                        containerColor = Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Text("IMPORT EXISTING IDENTITY")
                }
            }
            
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun FeatureList(features: List<String>) {
    Column {
        features.forEach { feature ->
            Row(
                modifier = Modifier.padding(vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("●", color = MatrixGreen, style = RebelioTypography.bodyMedium)
                Spacer(Modifier.width(Spacing.md))
                Text(feature, style = RebelioTypography.bodyLarge, color = TextPrimary)
            }
        }
    }
}
