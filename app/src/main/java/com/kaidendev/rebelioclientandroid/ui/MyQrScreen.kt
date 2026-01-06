package com.kaidendev.rebelioclientandroid.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTopBar
import com.kaidendev.rebelioclientandroid.ui.theme.*

@Composable
fun MyQrScreen(
    username: String?,
    routingToken: String?,
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    
    // Generate QR code bitmap
    val qrBitmap = remember(routingToken) {
        routingToken?.let { token ->
            generateQrCode("rebelio://contact?token=$token&name=${username ?: ""}")
        }
    }

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            RebelioTopBar(
                title = "MY QR CODE",
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
            
            // Username
            Text(
                text = username ?: "Unknown",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // QR Code
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .border(2.dp, MatrixGreen, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator(color = MatrixGreen)
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Instructions
            Text(
                text = "Show this QR code to your contact\nso they can add you",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Copy Token Button
            OutlinedButton(
                onClick = {
                    routingToken?.let {
                        clipboardManager.setText(AnnotatedString(it))
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MatrixGreen),
                border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen)
            ) {
                Text("COPY TOKEN")
            }
        }
    }
}

private fun generateQrCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
