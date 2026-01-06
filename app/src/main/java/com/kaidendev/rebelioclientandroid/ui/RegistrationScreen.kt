package com.kaidendev.rebelioclientandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kaidendev.rebelioclientandroid.ui.components.GlowButton
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTextField
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTopBar
import com.kaidendev.rebelioclientandroid.ui.theme.ErrorRed
import com.kaidendev.rebelioclientandroid.ui.theme.MatrixGreen
import com.kaidendev.rebelioclientandroid.ui.theme.RebelioTypography
import com.kaidendev.rebelioclientandroid.ui.theme.Spacing
import com.kaidendev.rebelioclientandroid.ui.theme.TextMuted

@Composable
fun RegistrationScreen(
    onRegister: (String, String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onClearError: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    // Production API hardcoded
    val serverUrl = "https://api.rebelio.org"
    // TODO: Add ExpandableServerConfig logic

    Scaffold(
        topBar = {
            RebelioTopBar(
                title = "Create Identity",
                onBack = null // Root screen of flow basically, or could add back to Welcome
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(Spacing.lg)
                .fillMaxSize()
        ) {
            Text(
                text = "Choose a username",
                style = RebelioTypography.headlineLarge
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = "This will be your public identity.",
                style = RebelioTypography.bodyMedium,
                color = TextMuted
            )
            
            Spacer(Modifier.height(Spacing.xl))
            
            RebelioTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                isValid = error == null, // Simple validation for now
                validationMessage = if (username.length in 1..2) "Too short" else null 
            )
            
            Spacer(Modifier.height(Spacing.md))
            
            // Privacy notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MatrixGreen.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                    .background(MatrixGreen.copy(alpha=0.1f), RoundedCornerShape(8.dp))
                    .padding(Spacing.md)
            ) {
                Column {
                    Text(
                        text = "🔒 ENCRYPTION NOTICE",
                        style = RebelioTypography.labelSmall,
                        color = MatrixGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your identity keys are stored ONLY on this device. If you delete the app or use 'Delete Identity', your account is lost forever. There is no cloud recovery.",
                        style = RebelioTypography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            if (error != null) {
                Text(
                    text = error,
                    style = RebelioTypography.bodyMedium,
                    color = ErrorRed,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
            }
            
            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MatrixGreen)
                    Spacer(Modifier.height(Spacing.sm))
                    Text("Generating keys...", style = RebelioTypography.labelSmall)
                }
            } else {
                GlowButton(
                    text = "CREATE ACCOUNT",
                    onClick = { onRegister(username, serverUrl) },
                    enabled = username.length >= 3
                )
            }
        }
    }
}
