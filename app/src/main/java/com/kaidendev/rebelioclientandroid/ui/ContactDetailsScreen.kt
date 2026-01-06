package com.kaidendev.rebelioclientandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaidendev.rebelioclientandroid.ui.components.ContactAvatar
import com.kaidendev.rebelioclientandroid.ui.components.GlowButton
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTopBar
import com.kaidendev.rebelioclientandroid.ui.theme.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.kaidendev.rebelioclientandroid.ui.components.EditContactDialog
import uniffi.rebelio_client.FfiContact

@Composable
fun ContactDetailsScreen(
    contact: FfiContact,
    onBack: () -> Unit,
    onDeleteContact: () -> Unit,
    onStartChat: () -> Unit,
    onRenameContact: (String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditContactDialog(
            initialName = contact.nickname,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName ->
                onRenameContact(newName)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            RebelioTopBar(
                title = "Contact Info",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Large Avatar
            Box(modifier = Modifier.size(120.dp)) {
                ContactAvatar(
                    routingToken = contact.routingToken,
                    nickname = contact.nickname
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            Text(
                text = contact.nickname,
                style = RebelioTypography.displayLarge,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(Spacing.xs))
            
            // Edit Button
            TextButton(onClick = { showEditDialog = true }) {
                Text("EDIT NAME", color = MatrixGreen)
            }
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Surface(
                color = CardBlack,
                shape = RebelioShapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Text(
                        text = "Routing Token",
                        style = RebelioTypography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = contact.routingToken,
                        style = RebelioTypography.bodyMedium,
                        color = MatrixGreen
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            GlowButton(
                text = "Open Chat",
                onClick = onStartChat,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            OutlinedButton(
                onClick = onDeleteContact,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = androidx.compose.ui.graphics.Color.Red
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.Red)
            ) {
                Text("Delete Contact")
            }
        }
    }
}
