package com.kaidendev.rebelioclientandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaidendev.rebelioclientandroid.repository.RebelioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import uniffi.rebelio_client.FfiContact
import com.kaidendev.rebelioclientandroid.model.FfiGroup
import uniffi.rebelio_client.FfiMessage
import uniffi.rebelio_client.FfiStatus

data class AppState(
    val isRegistered: Boolean = false,
    val username: String? = null,
    val serverUrl: String = "",
    val myRoutingToken: String? = null,
    val contacts: List<FfiContact> = emptyList(),
    val groups: List<FfiGroup> = emptyList(),
    val messages: List<FfiMessage> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false,
    val unreadCounts: Map<String, Int> = emptyMap(),
    val identityChangeAlert: IdentityChangeAlert? = null
)

/**
 * Alert shown when a contact's identity key has changed
 */
data class IdentityChangeAlert(
    val contactId: String,
    val contactName: String
)

class RebelioViewModel(private val repository: RebelioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AppState())
    val uiState: StateFlow<AppState> = _uiState.asStateFlow()
    
    private val _notificationFlow = kotlinx.coroutines.flow.MutableSharedFlow<FfiMessage>()
    val notificationFlow = _notificationFlow.asSharedFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getStatus()
            result.onSuccess { status ->
                _uiState.value = _uiState.value.copy(
                    isRegistered = status.isRegistered,
                    username = status.username,
                    serverUrl = status.serverUrl,
                    myRoutingToken = status.routingToken,
                    isLoading = false
                )
                if (status.isRegistered) {
                    loadData()
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = "Failed to get status: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    // Local storage for sent and received messages (session only)
    private val sentMessages = mutableListOf<FfiMessage>()
    private val receivedMessages = mutableListOf<FfiMessage>()

    private fun loadData() {
        viewModelScope.launch {
            val contactsResult = repository.loadContacts()
            val groupsResult = repository.loadGroups()
            
            // Workaround: Load persistent history
            val historyMessages = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                repository.loadLocalHistory()
            }
            
            // Add history first
            historyMessages.forEach { msg ->
                if (receivedMessages.none { it.id == msg.id }) {
                    receivedMessages.add(msg)
                }
            }

            val messagesResult = repository.getInboxMessages()
            
            // Accumulate new messages
            val newInboxMessages = messagesResult.getOrDefault(emptyList())
            
            newInboxMessages.forEach { newMsg ->
                if (receivedMessages.none { it.id == newMsg.id }) {
                    receivedMessages.add(newMsg)
                }
            }

            val allMessages = receivedMessages + sentMessages
            
            _uiState.value = _uiState.value.copy(
                contacts = contactsResult.getOrDefault(emptyList()),
                groups = groupsResult.getOrDefault(emptyList()),
                messages = allMessages.sortedBy { it.timestamp }
            )
        }
    }

    fun register(username: String, serverUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.registerUser(username, serverUrl)
            result.onSuccess {
                refreshStatus()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Registration failed: ${e.message}"
                )
            }
        }
    }

    fun addContact(nickname: String, routingToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.addContact(nickname, routingToken)
                .onSuccess { loadData(); _uiState.value = _uiState.value.copy(isLoading = false) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
        }
    }

    fun removeContact(nickname: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.removeContact(nickname)
                .onSuccess { loadData(); _uiState.value = _uiState.value.copy(isLoading = false) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
        }
    }

    fun renameContact(oldNickname: String, newNickname: String, routingToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.updateContact(oldNickname, newNickname, routingToken)
                .onSuccess { loadData(); _uiState.value = _uiState.value.copy(isLoading = false) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
        }
    }

    fun createGroup(name: String, members: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.createGroup(name, members)
                .onSuccess { loadData(); _uiState.value = _uiState.value.copy(isLoading = false) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
        }
    }

    fun sendGroupMessage(groupId: String, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.sendGroupMessage(groupId, message)
                .onSuccess { 
                     loadData()
                     _uiState.value = _uiState.value.copy(isLoading = false) 
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
        }
    }

    // Polling mechanism
    private var isPolling = false
    
    // Track which conversation is currently open (for notification suppression)
    private var currentlyViewingContact: String? = null
    
    fun setViewingContact(contactId: String?) {
        currentlyViewingContact = contactId
    }
    
    fun startAutoRefresh() {
        if (isPolling) return
        isPolling = true
        println("Rebelio: Polling started")
        viewModelScope.launch {
            while (isPolling) {
                println("Rebelio: Polling tick, isRegistered=${_uiState.value.isRegistered}")
                if (_uiState.value.isRegistered) {
                    println("Rebelio: Fetching inbox messages...")
                    println("Rebelio: Fetching inbox messages...")
                    val messagesResult = repository.getInboxMessages()
                    
                    messagesResult.onFailure { e ->
                        println("Rebelio: Fetch message failed: ${e.message}")
                        val msg = e.message ?: ""
                        if (msg.contains("untrusted identity", ignoreCase = true)) {
                            // Try to find which contact caused this
                            val contact = _uiState.value.contacts.find { msg.contains(it.routingToken) }
                            if (contact != null) {
                                showIdentityChangeAlert(contact.routingToken, contact.nickname)
                            }
                        }
                    }

                    println("Rebelio: Got ${messagesResult.getOrNull()?.size ?: "error"} messages")
                    val newInboxMessages = messagesResult.getOrDefault(emptyList())
                    
                    if (newInboxMessages.isNotEmpty()) {
                        var changed = false
                        val currentContacts = _uiState.value.contacts.toMutableList()
                        var newUnreadCounts = _uiState.value.unreadCounts.toMutableMap()
                        
                        newInboxMessages.forEach { newMsg ->
                            println("Rebelio: New message from '${newMsg.sender}': ${newMsg.content}")
                            
                            // Auto-create contact if sender is unknown
                            if (newMsg.sender != "me" && 
                                currentContacts.none { it.routingToken == newMsg.sender || it.nickname == newMsg.sender }) {
                                // Create temporary contact from sender routing token
                                val shortName = "User-${newMsg.sender.take(6)}"
                                val newContact = uniffi.rebelio_client.FfiContact(
                                    nickname = shortName,
                                    routingToken = newMsg.sender
                                )
                                currentContacts.add(newContact)
                                println("Rebelio: Auto-created contact '$shortName' for unknown sender")
                                
                                // Persist contact
                                viewModelScope.launch {
                                    repository.addContact(shortName, newMsg.sender)
                                        .onFailure { e -> println("Rebelio: Failed to persist contact: ${e.message}") }
                                }
                            }
                            
                            if (receivedMessages.none { it.id == newMsg.id }) {
                                receivedMessages.add(newMsg)
                                changed = true
                                
                                // Increment unread count for sender
                                if (newMsg.sender != "me") {
                                    val count = newUnreadCounts[newMsg.sender] ?: 0
                                    newUnreadCounts[newMsg.sender] = count + 1
                                    
                                    // Only trigger notification if NOT viewing this conversation
                                    if (currentlyViewingContact != newMsg.sender) {
                                        viewModelScope.launch {
                                            _notificationFlow.emit(newMsg)
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (changed) {
                            val allMessages = receivedMessages + sentMessages
                            _uiState.value = _uiState.value.copy(
                                messages = allMessages.sortedBy { it.timestamp },
                                contacts = currentContacts,
                                unreadCounts = newUnreadCounts
                            )
                        }
                    }
                    
                    // Sync status updates for sent messages
                    syncSentMessageStatuses()
                }
                kotlinx.coroutines.delay(2000) // Poll every 2 seconds
            }
        }
    }
    
    /**
     * Sync status updates for sent messages from local history
     * This updates the checkmarks: sent → delivered → read
     */
    private fun syncSentMessageStatuses() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val updates = uniffi.rebelio_client.getSentMessageStatuses()
                var changed = false
                
                updates.forEach { update ->
                    val idx = sentMessages.indexOfFirst { it.id == update.messageId }
                    if (idx >= 0 && sentMessages[idx].status != update.status) {
                        val msg = sentMessages[idx]
                        sentMessages[idx] = uniffi.rebelio_client.FfiMessage(
                            id = msg.id,
                            sender = msg.sender,
                            content = msg.content,
                            timestamp = msg.timestamp,
                            isEncrypted = msg.isEncrypted,
                            status = update.status
                        )
                        changed = true
                        println("Rebelio: Message ${msg.id} status: ${msg.status} → ${update.status}")
                    }
                }
                
                if (changed) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        val allMessages = receivedMessages + sentMessages
                        _uiState.value = _uiState.value.copy(
                            messages = allMessages.sortedBy { it.timestamp }
                        )
                    }
                }
            } catch (e: Exception) {
                // Silent fail - status sync is non-critical
                println("Rebelio: Status sync failed: ${e.message}")
            }
        }
    }
    
    fun stopPolling() {
        isPolling = false
    }

    fun sendMessage(recipientToken: String, message: String) {
        viewModelScope.launch {
            // Optimistic update first? No, let's wait for ack to confirm sent.
            // Actually user wants to see it. Let's add it to sentMessages on success.
            _uiState.value = _uiState.value.copy(isLoading = true)
            println("Rebelio: Sending message to $recipientToken: $message")
            
            repository.sendMessage(recipientToken, message)
                .onSuccess { 
                    println("Rebelio: Message sent successfully")
                    
                    // Reload history to get correct message ID from persistent storage
                    val historyMessages = repository.loadLocalHistory()
                    
                    // Find the message we just sent (most recent outgoing to this recipient)
                    val sentMsg = historyMessages.filter { 
                        it.sender == "You" || it.sender.startsWith("me:")
                    }.filter {
                        it.content == message // Match content
                    }.maxByOrNull { it.timestamp }
                    
                    val messageToAdd = sentMsg ?: FfiMessage(
                        id = System.currentTimeMillis().toString(), // Fallback: use timestamp as ID
                        sender = "me:$recipientToken",
                        content = message,
                        timestamp = System.currentTimeMillis() / 1000,
                        isEncrypted = true,
                        status = "sent"
                    )
                    
                    // Avoid duplicates
                    if (sentMessages.none { it.id == messageToAdd.id }) {
                        sentMessages.add(messageToAdd)
                    }
                    
                    val allMessages = receivedMessages + sentMessages
                    _uiState.value = _uiState.value.copy(
                        messages = allMessages.sortedBy { it.timestamp },
                        isLoading = false
                    )
                }
                .onFailure { e -> 
                    println("Rebelio: Failed to send: ${e.message}")
                    val msg = e.message ?: ""
                    if (msg.contains("untrusted identity", ignoreCase = true)) {
                        val contact = _uiState.value.contacts.find { it.routingToken == recipientToken }
                        showIdentityChangeAlert(recipientToken, contact?.nickname ?: "Unknown")
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) 
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Mark messages as read - clears unread count and sends read receipts to server
     */
    fun markAsRead(contactId: String) {
        // Clear local unread count
        val currentCounts = _uiState.value.unreadCounts.toMutableMap()
        if (currentCounts.containsKey(contactId)) {
            currentCounts.remove(contactId)
            _uiState.value = _uiState.value.copy(unreadCounts = currentCounts)
        }
        
        // Send read receipts to server
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val messageIds = receivedMessages.filter { it.sender == contactId }.map { it.id }
            val myToken = _uiState.value.myRoutingToken
            if (messageIds.isNotEmpty() && myToken != null) {
                try {
                    uniffi.rebelio_client.markMessagesRead(messageIds, myToken)
                    println("Rebelio: Sent read receipts for ${messageIds.size} messages")
                } catch (e: Exception) {
                    println("Rebelio: Failed to send read receipts: ${e.message}")
                }
            }
        }
    }

    /**
     * Trust a contact's new identity key and reset session
     * Call this when user confirms they trust the new identity
     */
    fun trustNewIdentity(contactId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                uniffi.rebelio_client.trustIdentity(contactId)
                println("Rebelio: Trusted new identity for $contactId")
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(identityChangeAlert = null)
                }
            } catch (e: Exception) {
                println("Rebelio: Failed to trust identity: ${e.message}")
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to trust identity: ${e.message}",
                        identityChangeAlert = null
                    )
                }
            }
        }
    }

    /**
     * Show identity change alert for a contact
     */
    fun showIdentityChangeAlert(contactId: String, contactName: String) {
        _uiState.value = _uiState.value.copy(
            identityChangeAlert = IdentityChangeAlert(contactId, contactName)
        )
    }

    /**
     * Dismiss identity change alert without trusting
     */
    fun dismissIdentityChangeAlert() {
        _uiState.value = _uiState.value.copy(identityChangeAlert = null)
    }

    fun clearHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.clearHistory()
            
            sentMessages.clear()
            receivedMessages.clear()
            
            _uiState.value = _uiState.value.copy(
                messages = emptyList(),
                isLoading = false
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
             _uiState.value = _uiState.value.copy(isLoading = true)
             repository.resetIdentity()
             
             // Clear in-memory state
             sentMessages.clear()
             receivedMessages.clear()
             
             // Reset UI state
             _uiState.value = AppState() // Reset to default (isRegistered=false)
        }
    }

    fun exportIdentity(): String? {
        return try {
            uniffi.rebelio_client.exportIdentity()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = "Failed to export identity: ${e.message}")
            null
        }
    }

    fun importIdentity(json: String) {
        try {
            uniffi.rebelio_client.importIdentity(json)
            // After import, user should register to re-auth with server
            // The identity is now in place, registration will use it
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = "Failed to import identity: ${e.message}")
        }
    }
}

class RebelioViewModelFactory(private val repository: RebelioRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RebelioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RebelioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
