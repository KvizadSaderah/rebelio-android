package com.kaidendev.rebelioclientandroid.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.kaidendev.rebelioclientandroid.ui.components.ContactAvatar
import com.kaidendev.rebelioclientandroid.ui.components.GlowButton
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTextField
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTopBar
import com.kaidendev.rebelioclientandroid.ui.theme.CardBlack
import com.kaidendev.rebelioclientandroid.ui.theme.DeepBlack
import com.kaidendev.rebelioclientandroid.ui.theme.MatrixGreen
import com.kaidendev.rebelioclientandroid.ui.theme.MatrixGreenTranslucent
import com.kaidendev.rebelioclientandroid.ui.theme.RebelioTypography
import com.kaidendev.rebelioclientandroid.ui.theme.Spacing
import com.kaidendev.rebelioclientandroid.ui.theme.TextMuted
import com.kaidendev.rebelioclientandroid.ui.theme.TextPrimary
import uniffi.rebelio_client.FfiGroup
import uniffi.rebelio_client.FfiContact

@Composable
fun GroupRow(
    group: FfiGroup,
    onClick: (FfiGroup) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(group) }
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Use ContactAvatar with hash for now, or new GroupAvatar
        ContactAvatar(
            routingToken = group.id, // Use ID for color generation
            nickname = group.name
        )
        
        Spacer(Modifier.width(Spacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.name,
                style = RebelioTypography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = "${group.members.size} members",
                style = RebelioTypography.labelSmall,
                color = TextMuted
            )
        }
        
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ChatListScreen(
    contacts: List<FfiContact>,
    groups: List<FfiGroup>,
    myRoutingToken: String?,
    unreadCounts: Map<String, Int> = emptyMap(),
    onAddContact: () -> Unit,
    onCreateGroup: () -> Unit,
    onContactSelected: (FfiContact) -> Unit,
    onRemoveContact: (FfiContact) -> Unit,
    onSettingsClicked: () -> Unit,
    onGroupSelected: (FfiGroup) -> Unit,
    onShowMyQr: () -> Unit,
    onScanQr: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            Column {
                RebelioTopBar(
                    title = "Messages",
                    actions = {
                        IconButton(onClick = onCreateGroup) {
                            Icon(
                                imageVector = Icons.Filled.Add, 
                                contentDescription = "Create Group",
                                tint = MatrixGreen
                            )
                        }
                        IconButton(onClick = onSettingsClicked) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = TextPrimary
                            )
                        }
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBlack)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onShowMyQr) {
                        Text("📱 MY QR", color = MatrixGreen)
                    }
                    TextButton(onClick = onScanQr) {
                        Text("📷 SCAN", color = MatrixGreen)
                    }
                }
                Divider(color = DeepBlack)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContact,
                containerColor = MatrixGreen,
                contentColor = DeepBlack
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
        ) {
            if (contacts.isEmpty() && groups.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No chats yet",
                        style = RebelioTypography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "Tap + to add contact or create group",
                        style = RebelioTypography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (groups.isNotEmpty()) {
                        item {
                            Text(
                                "Groups",
                                modifier = Modifier.padding(Spacing.md),
                                style = RebelioTypography.labelSmall,
                                color = MatrixGreen
                            )
                        }
                        items(groups) { group ->
                            GroupRow(group = group, onClick = onGroupSelected)
                            Divider(color = CardBlack)
                        }
                    }
                    
                    if (contacts.isNotEmpty()) {
                         item {
                            Text(
                                "Contacts",
                                modifier = Modifier.padding(Spacing.md),
                                style = RebelioTypography.labelSmall,
                                color = MatrixGreen
                            )
                        }
                        items(contacts) { contact ->
                            ContactRow(
                                contact = contact, 
                                unreadCount = unreadCounts[contact.routingToken] ?: 0,
                                onClick = onContactSelected,
                                onRemove = { onRemoveContact(contact) }
                            )
                            Divider(color = CardBlack)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactRow(
    contact: FfiContact,
    unreadCount: Int = 0,
    onClick: (FfiContact) -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick(contact) },
                    onLongClick = { showMenu = true }
                )
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                routingToken = contact.routingToken,
                nickname = contact.nickname
            )
            
            Spacer(Modifier.width(Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.nickname,
                    style = RebelioTypography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = contact.routingToken.take(12) + "...",
                    style = RebelioTypography.labelSmall,
                    color = TextMuted
                )
            }
            
            // Unread Badge
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MatrixGreen, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = DeepBlack,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
            }
            
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(DeepBlack) // Use modifier for background
        ) {
            DropdownMenuItem(
                text = { Text("Delete Contact", color = androidx.compose.ui.graphics.Color.Red) },
                onClick = {
                    onRemove()
                    showMenu = false
                }
            )
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var routingToken by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Contact",
                style = RebelioTypography.titleMedium,
                color = TextPrimary
            )
        },
        text = {
            Column {
                RebelioTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = "Nickname (Alia)",
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                RebelioTextField(
                    value = routingToken,
                    onValueChange = { routingToken = it },
                    label = "Routing Token",
                    singleLine = false // Allow multiline for long tokens if paste happens in weird way, but usually tokens are single line string
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nickname, routingToken) },
                enabled = nickname.isNotBlank() && routingToken.isNotBlank()
            ) {
                Text("ADD", color = MatrixGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextMuted)
            }
        },
        containerColor = CardBlack,
        textContentColor = TextPrimary
    )
}
