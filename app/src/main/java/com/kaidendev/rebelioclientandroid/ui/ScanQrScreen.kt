package com.kaidendev.rebelioclientandroid.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTopBar
import com.kaidendev.rebelioclientandroid.ui.theme.*

data class ScannedContact(
    val token: String,
    val name: String?
)

@Composable
fun ScanQrScreen(
    onBack: () -> Unit,
    onContactScanned: (token: String, name: String?) -> Unit,
    onManualEntry: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var scannedResult by remember { mutableStateOf<ScannedContact?>(null) }
    var scannerView by remember { mutableStateOf<DecoratedBarcodeView?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scannerView?.pause()
        }
    }

    Scaffold(
        containerColor = DeepBlack,
        topBar = {
            RebelioTopBar(
                title = "SCAN QR CODE",
                onBack = {
                    scannerView?.pause()
                    onBack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (hasCameraPermission) {
                // Camera Preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            DecoratedBarcodeView(ctx).apply {
                                scannerView = this
                                decodeContinuous(object : BarcodeCallback {
                                    override fun barcodeResult(result: BarcodeResult?) {
                                        result?.text?.let { qrContent ->
                                            val parsed = parseRebelioQr(qrContent)
                                            if (parsed != null) {
                                                scannedResult = parsed
                                                pause()
                                            }
                                        }
                                    }
                                })
                                resume()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Overlay Frame
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .border(2.dp, MatrixGreen, RoundedCornerShape(16.dp))
                        )
                    }
                }
                
                // Instructions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepBlack)
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Point camera at QR code",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.md))
                    
                    TextButton(onClick = { 
                        scannerView?.pause()
                        onManualEntry() 
                    }) {
                        Text("ENTER MANUALLY", color = MatrixGreen)
                    }
                }
            } else {
                // No Permission
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Camera access is required\nto scan QR codes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen)
                        ) {
                            Text("ALLOW ACCESS")
                        }
                        
                        Spacer(modifier = Modifier.height(Spacing.md))
                        
                        TextButton(onClick = onManualEntry) {
                            Text("ENTER MANUALLY", color = MatrixGreen)
                        }
                    }
                }
            }
        }
    }

    // Success Dialog
    if (scannedResult != null) {
        AlertDialog(
            onDismissRequest = { 
                scannedResult = null
                scannerView?.resume()
            },
            title = { Text("Contact Found!", color = TextPrimary) },
            text = { 
                Column {
                    if (scannedResult?.name != null) {
                        Text("Name: ${scannedResult?.name}", color = TextPrimary)
                    }
                    Text("Token: ${scannedResult?.token?.take(20)}...", color = TextMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scannedResult?.let { 
                            onContactScanned(it.token, it.name)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen)
                ) {
                    Text("ADD")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    scannedResult = null
                    scannerView?.resume()
                }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardBlack
        )
    }
}

private fun parseRebelioQr(content: String): ScannedContact? {
    return try {
        if (content.startsWith("rebelio://contact")) {
            val uri = Uri.parse(content)
            val token = uri.getQueryParameter("token")
            val name = uri.getQueryParameter("name")
            
            if (token != null) {
                ScannedContact(token, name?.takeIf { it.isNotBlank() })
            } else null
        } else {
            // Fallback: treat entire content as token
            ScannedContact(content, null)
        }
    } catch (e: Exception) {
        null
    }
}
