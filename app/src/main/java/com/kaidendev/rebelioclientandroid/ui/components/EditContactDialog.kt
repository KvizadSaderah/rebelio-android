package com.kaidendev.rebelioclientandroid.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kaidendev.rebelioclientandroid.ui.theme.*

@Composable
fun EditContactDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nickname by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Contact",
                style = RebelioTypography.titleMedium,
                color = TextPrimary
            )
        },
        text = {
            Column {
                RebelioTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = "Nickname",
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nickname) },
                enabled = nickname.isNotBlank() && nickname != initialName
            ) {
                Text("SAVE", color = MatrixGreen)
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
