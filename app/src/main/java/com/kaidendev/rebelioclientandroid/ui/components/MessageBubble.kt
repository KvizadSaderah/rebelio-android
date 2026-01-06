package com.kaidendev.rebelioclientandroid.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaidendev.rebelioclientandroid.ui.theme.BubbleReceived
import com.kaidendev.rebelioclientandroid.ui.theme.BubbleSent
import com.kaidendev.rebelioclientandroid.ui.theme.DeepBlack
import com.kaidendev.rebelioclientandroid.ui.theme.EncryptedGreen
import com.kaidendev.rebelioclientandroid.ui.theme.MatrixGreen
import com.kaidendev.rebelioclientandroid.ui.theme.RebelioTypography
import com.kaidendev.rebelioclientandroid.ui.theme.TextPrimary
import com.kaidendev.rebelioclientandroid.ui.theme.TextSecondary
import uniffi.rebelio_client.FfiMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: FfiMessage,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) BubbleSent else BubbleReceived,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (isMe) DeepBlack else TextPrimary,
                style = RebelioTypography.bodyMedium
            )
        }
        
        Spacer(Modifier.height(2.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatTime(message.timestamp),
                style = RebelioTypography.labelSmall
            )
            if (isMe) {
                Spacer(Modifier.width(4.dp))
                // TODO: Use message.status after UniFFI bindings regeneration
                // For now, always show "sent" status
                MessageStatusIcon(status = "sent")
            }
        }
    }
}

/**
 * Status icon for sent messages:
 * - "sent" = single gray checkmark
 * - "delivered" = double gray checkmarks  
 * - "read" = double green checkmarks
 */
@Composable
fun MessageStatusIcon(status: String) {
    when (status.lowercase()) {
        "read" -> {
            // Double checkmark (green) - read  
            // Using Done icon twice since DoneAll may not be available
            DoubleCheckIcon(tint = MatrixGreen, contentDescription = "Read")
        }
        "delivered" -> {
            // Double checkmark (gray) - delivered
            DoubleCheckIcon(tint = TextSecondary, contentDescription = "Delivered")
        }
        else -> {
            // Single checkmark (gray) - sent
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = "Sent",
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun DoubleCheckIcon(tint: androidx.compose.ui.graphics.Color, contentDescription: String) {
    Row {
        Icon(
            imageVector = Icons.Filled.Done,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Icon(
            imageVector = Icons.Filled.Done,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp).offset(x = (-8).dp)
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp * 1000)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}
