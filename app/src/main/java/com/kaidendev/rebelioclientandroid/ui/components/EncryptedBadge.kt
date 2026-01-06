package com.kaidendev.rebelioclientandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaidendev.rebelioclientandroid.ui.theme.EncryptedGreen
import com.kaidendev.rebelioclientandroid.ui.theme.RebelioTypography

@Composable
fun EncryptedBadge(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = EncryptedGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = "Encrypted",
            tint = EncryptedGreen,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "E2E Encrypted",
            style = RebelioTypography.labelSmall,
            color = EncryptedGreen
        )
    }
}
