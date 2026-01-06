package com.kaidendev.rebelioclientandroid.ui

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.kaidendev.rebelioclientandroid.ui.components.RebelioTopBar
import com.kaidendev.rebelioclientandroid.ui.theme.*

@Composable
fun ImportIdentityScreen(
    onBack: () -> Unit,
    onIdentityImported: (json: String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var scannedJson by remember { mutableStateOf<String?>(null) }
    var scannerView by remember { mutableStateOf<DecoratedBarcodeView?>(null) }
    var importError by remember { mutableStateOf<String?>(null) }

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
                title = "IMPORT IDENTITY",
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
                                            // Try to parse as identity JSON
                                            if (isValidIdentityJson(qrContent)) {
                                                scannedJson = qrContent
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
                                .border(2.dp, NeonCyan, RoundedCornerShape(16.dp))
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
                        text = "Scan the QR code from your other device's Export Identity screen",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
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
                            text = "Camera access is required\nto scan identity QR code",
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
                    }
                }
            }
        }
    }

    // Confirm Dialog
    if (scannedJson != null) {
        AlertDialog(
            onDismissRequest = { 
                scannedJson = null
                scannerView?.resume()
            },
            title = { Text("Identity Found!", color = TextPrimary) },
            text = { 
                Column {
                    Text("Ready to import your identity from another device.", color = TextMuted)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text("⚠️ This will replace any existing identity on this device.", color = WarningYellow)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scannedJson?.let { onIdentityImported(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen)
                ) {
                    Text("IMPORT")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    scannedJson = null
                    scannerView?.resume()
                }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardBlack
        )
    }

    // Error Dialog
    if (importError != null) {
        AlertDialog(
            onDismissRequest = { importError = null },
            title = { Text("Import Failed", color = ErrorRed) },
            text = { Text(importError!!, color = TextMuted) },
            confirmButton = {
                Button(onClick = { 
                    importError = null
                    scannerView?.resume()
                }) {
                    Text("OK")
                }
            },
            containerColor = CardBlack
        )
    }
}

private fun isValidIdentityJson(content: String): Boolean {
    return try {
        // Check if it looks like our identity JSON structure
        content.contains("\"version\"") && 
        content.contains("\"private_key\"") && 
        content.contains("\"public_key\"")
    } catch (e: Exception) {
        false
    }
}
