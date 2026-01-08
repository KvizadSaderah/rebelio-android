package com.kaidendev.rebelioclientandroid

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kaidendev.rebelioclientandroid.repository.RebelioRepository
import com.kaidendev.rebelioclientandroid.ui.AddContactDialog
import com.kaidendev.rebelioclientandroid.ui.ChatListScreen
import com.kaidendev.rebelioclientandroid.ui.ChatScreen
import com.kaidendev.rebelioclientandroid.ui.GroupChatScreen
import com.kaidendev.rebelioclientandroid.ui.CreateGroupScreen
import com.kaidendev.rebelioclientandroid.ui.ContactDetailsScreen
import com.kaidendev.rebelioclientandroid.ui.RebelioViewModel
import com.kaidendev.rebelioclientandroid.ui.RebelioViewModelFactory
import com.kaidendev.rebelioclientandroid.ui.RegistrationScreen
import com.kaidendev.rebelioclientandroid.ui.MyQrScreen
import com.kaidendev.rebelioclientandroid.ui.OnboardingScreen
import com.kaidendev.rebelioclientandroid.ui.ScanQrScreen
import com.kaidendev.rebelioclientandroid.ui.SettingsScreen
import com.kaidendev.rebelioclientandroid.ui.ImportIdentityScreen
import com.kaidendev.rebelioclientandroid.ui.WelcomeScreen
import com.kaidendev.rebelioclientandroid.ui.theme.RebelioTheme

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import uniffi.rebelio_client.FfiMessage

class MainActivity : ComponentActivity() {
    
    // Manual dependency injection for now
    private val repository by lazy { 
        RebelioRepository(filesDir.absolutePath) 
    }
    private val viewModel: RebelioViewModel by viewModels { RebelioViewModelFactory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Notification Channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "rebelio_messages",
                "Messages",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New message notifications"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Request Permission
        val requestPermissionLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted
            }
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Start polling when app starts
        viewModel.startAutoRefresh()
        
        // Listen for notifications
        lifecycleScope.launch {
             viewModel.notificationFlow.collect { message: FfiMessage ->
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                val notification = androidx.core.app.NotificationCompat.Builder(this@MainActivity, "rebelio_messages")
                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                    .setContentTitle("New Message")
                    .setContentText(message.content)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
                
                notificationManager.notify(message.id.hashCode(), notification)
            }
        }
        
        setContent {
            RebelioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    
                    // Onboarding State (SharedPreferences)
                    val prefs = remember { getSharedPreferences("rebelio_prefs", Context.MODE_PRIVATE) }
                    var onboardingCompleted by remember { mutableStateOf(prefs.getBoolean("onboarding_completed", false)) }
                    
                    // Navigation State
                    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
                    
                    // Onboarding Flow (First Launch)
                    if (!onboardingCompleted) {
                        OnboardingScreen(
                            onComplete = {
                                prefs.edit().putBoolean("onboarding_completed", true).apply()
                                onboardingCompleted = true
                            }
                        )
                    } else if (!uiState.isRegistered) {
                        // Registration Flow
                        var showRegistration by remember { mutableStateOf(false) }
                        var showImportIdentity by remember { mutableStateOf(false) }
                        
                        when {
                            showImportIdentity -> {
                                ImportIdentityScreen(
                                    onBack = { showImportIdentity = false },
                                    onIdentityImported = { json ->
                                        viewModel.importIdentity(json)
                                        showImportIdentity = false
                                        // After import, show registration to re-auth
                                        showRegistration = true
                                    }
                                )
                            }
                            showRegistration -> {
                                RegistrationScreen(
                                    onRegister = { user, url -> viewModel.register(user, url) },
                                    isLoading = uiState.isLoading,
                                    error = uiState.error,
                                    onClearError = { viewModel.clearError() }
                                )
                            }
                            else -> {
                                WelcomeScreen(
                                    onStartClicked = { showRegistration = true },
                                    onImportIdentity = { showImportIdentity = true }
                                )
                            }
                        }
                    } else {
                        // Main App Flow
                        when (val screen = currentScreen) {
                            is AppScreen.Home -> {
                                var showAddContactDialog by remember { mutableStateOf(false) }
                                
                                ChatListScreen(
                                    contacts = uiState.contacts,
                                    groups = uiState.groups,
                                    myRoutingToken = uiState.myRoutingToken,
                                    unreadCounts = uiState.unreadCounts,
                                    onAddContact = { showAddContactDialog = true },
                                    onCreateGroup = { currentScreen = AppScreen.CreateGroup },
                                    onContactSelected = { contact -> 
                                        viewModel.markAsRead(contact.routingToken)
                                        currentScreen = AppScreen.Chat(contact) 
                                    },
                                    onRemoveContact = { contact -> viewModel.removeContact(contact.nickname) },
                                    onSettingsClicked = { currentScreen = AppScreen.Settings },
                                    onGroupSelected = { group -> currentScreen = AppScreen.GroupChat(group) },

                                    onShowMyQr = { currentScreen = AppScreen.MyQr },
                                    onScanQr = { currentScreen = AppScreen.ScanQr }
                                 )

                                if (showAddContactDialog) {
                                    AddContactDialog(
                                        onDismiss = { showAddContactDialog = false },
                                        onConfirm = { nickname, token ->
                                            viewModel.addContact(nickname, token)
                                            showAddContactDialog = false
                                        }
                                    )
                                }
                            }
                            is AppScreen.Chat -> {
                                // Start viewing this contact (suppress notifications)
                                androidx.compose.runtime.DisposableEffect(screen.contact.routingToken) {
                                    viewModel.setViewingContact(screen.contact.routingToken)
                                    onDispose {
                                        viewModel.setViewingContact(null)
                                    }
                                }
                                
                                ChatScreen(
                                    contact = screen.contact,
                                    messages = uiState.messages,
                                    onSendMessage = { text -> viewModel.sendMessage(screen.contact.routingToken, text) },
                                    onBack = { currentScreen = AppScreen.Home },
                                    onContactInfoClicked = { currentScreen = AppScreen.ContactDetails(screen.contact) }
                                )
                            }
                            is AppScreen.Settings -> {
                                SettingsScreen(
                                    username = uiState.username,
                                    myRoutingToken = uiState.myRoutingToken,
                                    serverUrl = uiState.serverUrl,
                                    onBack = { currentScreen = AppScreen.Home },
                                    onLogout = { 
                                        viewModel.logout()
                                    },
                                    onClearHistory = {
                                        viewModel.clearHistory()
                                    },
                                    onExportIdentity = {
                                        viewModel.exportIdentity()
                                    }
                                )
                            }
                            is AppScreen.ContactDetails -> {
                                ContactDetailsScreen(
                                    contact = screen.contact,
                                    onBack = { currentScreen = AppScreen.Home },
                                    onDeleteContact = {
                                        viewModel.removeContact(screen.contact.nickname)
                                        currentScreen = AppScreen.Home
                                    },
                                    onStartChat = { currentScreen = AppScreen.Chat(screen.contact) },
                                    onRenameContact = { newName ->
                                        viewModel.renameContact(screen.contact.nickname, newName, screen.contact.routingToken)
                                        // Update the current screen state with the new name so UI reflects it immediately
                                        // Although viewModel update should trigger recomposition of Home, sticking here might keep old name
                                        // Ideally we navigate back or update the local 'screen.contact'
                                        currentScreen = AppScreen.Home // Simplest: go back to list to see change
                                    }
                                )
                            }
                            is AppScreen.CreateGroup -> {
                                CreateGroupScreen(
                                    contacts = uiState.contacts,
                                    onCreateGroup = { name, members ->
                                        viewModel.createGroup(name, members)
                                        currentScreen = AppScreen.Home
                                    },
                                    onBack = { currentScreen = AppScreen.Home }
                                )
                            }
                            is AppScreen.GroupChat -> {
                                GroupChatScreen(
                                    group = screen.group,
                                    messages = uiState.messages,
                                    onSendMessage = { text -> viewModel.sendGroupMessage(screen.group.id, text) },
                                    onBack = { currentScreen = AppScreen.Home }
                                )
                            }
                            is AppScreen.MyQr -> {
                                MyQrScreen(
                                    username = uiState.username,
                                    routingToken = uiState.myRoutingToken,
                                    onBack = { currentScreen = AppScreen.Home }
                                )
                            }
                            is AppScreen.ScanQr -> {
                                ScanQrScreen(
                                    onBack = { currentScreen = AppScreen.Home },
                                    onContactScanned = { token, name ->
                                        viewModel.addContact(name ?: "Contact", token)
                                        currentScreen = AppScreen.Home
                                    },
                                    onManualEntry = { currentScreen = AppScreen.Home }
                                )
                            }
                        }
                    }
                    
                    // Global Error
                    if (uiState.error != null) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { viewModel.clearError() },
                            title = { Text("Error") },
                            text = { Text(uiState.error!!) },
                            confirmButton = {
                                androidx.compose.material3.Button(onClick = { viewModel.clearError() }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// Sealed Class for Navigation
sealed class AppScreen {
    object Home : AppScreen()
    data class Chat(val contact: uniffi.rebelio_client.FfiContact) : AppScreen()
    object Settings : AppScreen()
    data class ContactDetails(val contact: uniffi.rebelio_client.FfiContact) : AppScreen()
    object CreateGroup : AppScreen()
    data class GroupChat(val group: com.kaidendev.rebelioclientandroid.model.FfiGroup) : AppScreen()
    object MyQr : AppScreen()
    object ScanQr : AppScreen()
}
