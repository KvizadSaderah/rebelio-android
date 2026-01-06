package com.kaidendev.rebelioclientandroid.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kaidendev.rebelioclientandroid.ui.theme.DeepBlack
import com.kaidendev.rebelioclientandroid.ui.theme.ErrorRed
import com.kaidendev.rebelioclientandroid.ui.theme.MatrixGreen
import com.kaidendev.rebelioclientandroid.ui.theme.RebelioTypography
import com.kaidendev.rebelioclientandroid.ui.theme.SurfaceBlack
import com.kaidendev.rebelioclientandroid.ui.theme.TextMuted
import com.kaidendev.rebelioclientandroid.ui.theme.TextPrimary

@Composable
fun RebelioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isValid: Boolean = true,
    validationMessage: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth(),
            isError = !isValid,
            textStyle = RebelioTypography.bodyLarge,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SurfaceBlack,
                unfocusedContainerColor = SurfaceBlack,
                cursorColor = MatrixGreen,
                focusedBorderColor = MatrixGreen,
                unfocusedBorderColor = TextMuted,
                errorBorderColor = ErrorRed,
                focusedLabelColor = MatrixGreen,
                unfocusedLabelColor = TextMuted,
                errorLabelColor = ErrorRed
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        
        if (validationMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = validationMessage,
                style = RebelioTypography.labelSmall,
                color = if (isValid) MatrixGreen else ErrorRed
            )
        }
    }
}
