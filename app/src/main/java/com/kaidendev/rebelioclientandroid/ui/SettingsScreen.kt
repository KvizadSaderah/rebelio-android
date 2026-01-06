package com.kaidendev.rebelioclientandroid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTopBar
import com.kaidendev.rebelioclientandroid.ui.theme.*
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun SettingsScreen(
    username: String?,
    myRoutingToken: String?,
    serverUrl: String, 
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onClearHistory: () -> Unit,
    onExportIdentity: () -> String? = { null }
) {
    val clipboardManager = LocalClipboardManager.current
    var showToken by remember { mutableStateOf(false) }
    var showExportQR by remember { mutableStateOf(false) }
    var exportedData by remember { mutableStateOf<String?>(null) }

    // QR Export Dialog
    if (showExportQR && exportedData != null) {
        ExportQRDialog(
            data = exportedData!!,
            onDismiss = { showExportQR = false }
        )
    }

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            RebelioTopBar(
                title = "SYSTEM SETTINGS",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Spacing.lg)
                .background(DeepBlack)
        ) {
            // Identity Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(MatrixGreen, NeonCyan)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(CardBlack, RoundedCornerShape(12.dp))
                    .padding(Spacing.lg)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(2.dp, MatrixGreen, CircleShape)
                            .background(SurfaceBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MatrixGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.md))
                    
                    Text(
                        text = username ?: "Unknown Agent",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "OPERATIVE STATUS: ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MatrixGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                "ENCRYPTION & IDENTITY",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            // Routing Token
            SettingsCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = "Token", tint = NeonCyan)
                            Spacer(modifier = Modifier.width(Spacing.md))
                            Column {
                                Text("Routing Token", color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    if (showToken) myRoutingToken ?: "Loading..." else "••••••••••••••••",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        TextButton(onClick = { showToken = !showToken }) {
                            Text(if (showToken) "HIDE" else "SHOW", color = MatrixGreen)
                        }
                    }
                    
                    if (showToken) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        TextButton(
                            onClick = {
                                myRoutingToken?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("COPY ADDRESS", color = MatrixGreen)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                "SYSTEM",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            SettingsCard {
                 SettingsItem(icon = Icons.Default.Info, title = "Notifications", value = "Enabled")
                 Divider(color = DeepBlack, thickness = 1.dp, modifier = Modifier.padding(vertical = Spacing.sm))
                 SettingsItem(icon = Icons.Default.Lock, title = "Privacy Mode", value = "Standard")
                 Divider(color = DeepBlack, thickness = 1.dp, modifier = Modifier.padding(vertical = Spacing.sm))
                 
                 // Clear History Action
                 SettingsItem(
                    icon = Icons.Default.Delete, 
                    title = "Clear Local History", 
                    value = "Action",
                    onClick = onClearHistory,
                    itemColor = WarningYellow
                 )
                 
                 Divider(color = DeepBlack, thickness = 1.dp, modifier = Modifier.padding(vertical = Spacing.sm))
                 SettingsItem(icon = Icons.Default.Info, title = "Rebelio Client", value = "v0.1.0-alpha")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Export Identity
            OutlinedButton(
                onClick = {
                    exportedData = onExportIdentity()
                    if (exportedData != null) {
                        showExportQR = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonCyan,
                    containerColor = Color.Transparent
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text("EXPORT IDENTITY (BACKUP)")
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Logout (Danger)
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorRed,
                    containerColor = Color.Transparent
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text("DELETE IDENTITY (DANGER)")
            }
        }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = CardBlack,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.md), content = content)
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    title: String, 
    value: String,
    onClick: (() -> Unit)? = null,
    itemColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (onClick != null) itemColor else TextTopBar, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(Spacing.md))
            Text(title, color = itemColor, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

val TextTopBar = Color(0xFFB3B3B3)

@Composable
fun ExportQRDialog(
    data: String,
    onDismiss: () -> Unit
) {
    val qrBitmap = remember(data) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBlack
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "EXPORT IDENTITY",
                    style = MaterialTheme.typography.titleLarge,
                    color = MatrixGreen,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(Spacing.md))
                
                Text(
                    "Scan this QR code on your new device to import your identity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = Spacing.md)
                )
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Identity QR Code",
                        modifier = Modifier
                            .size(256.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier.size(256.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Failed to generate QR", color = ErrorRed)
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                Text(
                    "⚠️ Keep this code secret!",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarningYellow
                )
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen)
                ) {
                    Text("DONE", color = DeepBlack)
                }
            }
        }
    }
}

