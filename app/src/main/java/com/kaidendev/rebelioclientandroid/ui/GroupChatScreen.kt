package com.kaidendev.rebelioclientandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaidendev.rebelioclientandroid.ui.components.ContactAvatar
import com.kaidendev.rebelioclientandroid.ui.components.EncryptedBadge
import com.kaidendev.rebelioclientandroid.ui.components.MessageBubble
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTextField
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTopBar
import com.kaidendev.rebelioclientandroid.ui.theme.DeepBlack
import com.kaidendev.rebelioclientandroid.ui.theme.MatrixGreen
import com.kaidendev.rebelioclientandroid.ui.theme.RebelioTypography
import com.kaidendev.rebelioclientandroid.ui.theme.Spacing
import uniffi.rebelio_client.FfiMessage
import com.kaidendev.rebelioclientandroid.model.FfiGroup

@Composable
fun GroupChatScreen(
    group: FfiGroup,
    messages: List<FfiMessage>,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Filter messages for this group (Assuming sender or content logic matches group)
    // For MVP, we assume messages from group have sender = group.id? 
    // Or we show ALL messages since filtering is hard without metadata?
    // Let's assume sender == group.id OR we just show messages directed to it?
    // Actually, client doesn't know "recipient" of INCOMING message (only sender).
    // If server sets sender=GroupID, this works.
    val chatMessages = remember(messages, group) {
        messages.filter { msg ->
            // Received messages from group
            msg.sender == group.id || 
            // Sent messages to this group
            msg.sender == "me:${group.id}"
        }
        .distinctBy { it.id } // Prevent duplicates
        .sortedBy { it.timestamp }
    }

    // Scroll to bottom on new message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            RebelioTopBar(
                title = group.name,
                onBack = onBack,
                actions = {
                    // Small avatar in top bar
                    ContactAvatar(
                        routingToken = group.id,
                        nickname = group.name,
                        size = 32.dp
                    )
                    Spacer(Modifier.width(Spacing.md))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .imePadding() // Handle keyboard properly
        ) {
            // E2E Banner (Group)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Should differentiate group encryption
                EncryptedBadge() 
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = Spacing.sm)
            ) {
                items(chatMessages) { message ->
                    MessageBubble(
                        message = message,
                        isMe = message.sender.startsWith("me:"),
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = 2.dp)
                    )
                }
            }

            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBlack)
                    .padding(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RebelioTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = "Message Group",
                    modifier = Modifier.weight(1f),
                    singleLine = false
                )
                
                Spacer(Modifier.width(Spacing.sm))
                
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MatrixGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
