package com.kaidendev.rebelioclientandroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaidendev.rebelioclientandroid.ui.components.GlowButton
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTopBar
import com.kaidendev.rebelioclientandroid.ui.theme.DeepBlack
import com.kaidendev.rebelioclientandroid.ui.theme.MatrixGreen
import com.kaidendev.rebelioclientandroid.ui.theme.RebelioTypography
import com.kaidendev.rebelioclientandroid.ui.theme.Spacing
import com.kaidendev.rebelioclientandroid.ui.theme.TextMuted
import uniffi.rebelio_client.FfiContact

@Composable
fun CreateGroupScreen(
    contacts: List<FfiContact>,
    onCreateGroup: (String, List<String>) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            RebelioTopBar(
                title = "Create Group",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning, // Or Construction/Build icon if available
                contentDescription = null,
                tint = MatrixGreen,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(Modifier.height(Spacing.md))
            
            Text(
                text = "Coming Soon",
                style = RebelioTypography.headlineMedium,
                color = MatrixGreen
            )
            
            Spacer(Modifier.height(Spacing.sm))
            
            Text(
                text = "Group Chats are currently being implemented. Check back in the next update!",
                style = RebelioTypography.bodyLarge,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(Modifier.height(Spacing.xl))
            
            GlowButton(
                text = "Go Back",
                onClick = onBack
            )
        }
    }
}
