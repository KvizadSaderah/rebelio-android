package com.kaidendev.rebelioclientandroid.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.rebelio_client.*
import com.kaidendev.rebelioclientandroid.model.FfiGroup




class RebelioRepository(private val storagePath: String) {

    init {
        // Initialize SDK with Android storage path
        uniffi.rebelio_client.initSdk(storagePath)
    }

    // ... existing methods ...

    // Workaround for NDK persistence issue: Load history.json directly
    fun loadLocalHistory(): List<FfiMessage> {
        return try {
        val historyFile = java.io.File(storagePath, "history.json")
        if (!historyFile.exists()) return emptyList()

        val jsonString = historyFile.readText()
        val json = org.json.JSONObject(jsonString)
        val messagesMap = json.optJSONObject("messages") ?: return emptyList()
        
        val allMessages = mutableListOf<FfiMessage>()
        
        messagesMap.keys().forEach { routingToken ->
            val msgsArray = messagesMap.getJSONArray(routingToken)
            for (i in 0 until msgsArray.length()) {
                val msgObj = msgsArray.getJSONObject(i)
                val isOutgoing = msgObj.optBoolean("is_outgoing")
                val sender = if (isOutgoing) "me" else msgObj.optString("sender")
                
                allMessages.add(FfiMessage(
                    id = msgObj.optString("id"),
                    sender = sender,
                    content = msgObj.optString("content"),
                    timestamp = msgObj.optLong("timestamp"),
                    isEncrypted = msgObj.optBoolean("is_encrypted"),
                    status = msgObj.optString("status", "sent")
                ))
            }
        }
        allMessages.sortedBy { it.timestamp }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

    suspend fun registerUser(username: String, serverUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = uniffi.rebelio_client.registerUser(username, serverUrl)
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStatus(): Result<FfiStatus> = withContext(Dispatchers.IO) {
        try {
            val status = uniffi.rebelio_client.getStatus()
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(recipientToken: String, messageText: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            uniffi.rebelio_client.sendMessage(recipientToken, messageText)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInboxMessages(): Result<List<FfiMessage>> = withContext(Dispatchers.IO) {
        try {
            val messages = uniffi.rebelio_client.getInboxMessages()
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadContacts(): Result<List<FfiContact>> = withContext(Dispatchers.IO) {
        try {
            val contacts = uniffi.rebelio_client.loadContacts()
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addContact(nickname: String, routingToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            uniffi.rebelio_client.addContact(nickname, routingToken)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeContact(nickname: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            uniffi.rebelio_client.removeContact(nickname)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateContact(oldNickname: String, newNickname: String, routingToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // FFI doesn't support update, so we delete and re-add
            uniffi.rebelio_client.removeContact(oldNickname)
            uniffi.rebelio_client.addContact(newNickname, routingToken)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGroup(name: String, members: List<String>): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Mock implementation until NDK is fixed
            // val groupId = uniffi.rebelio_client.createGroup(name, members)
            val groupId = java.util.UUID.randomUUID().toString()
            Result.success(groupId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadGroups(): Result<List<FfiGroup>> = withContext(Dispatchers.IO) {
        try {
            // Mock implementation until NDK is fixed
            // val groups = uniffi.rebelio_client.loadGroups()
            val groups = listOf<FfiGroup>() // Return empty list or dummy
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendGroupMessage(groupId: String, message: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Mock implementation until NDK is fixed
            // uniffi.rebelio_client.sendGroupMessage(groupId, message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearHistory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val historyFile = java.io.File(storagePath, "history.json")
            if (historyFile.exists()) historyFile.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetIdentity(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val configFile = java.io.File(storagePath, "config.json")
            if (configFile.exists()) configFile.delete()
            
            val historyFile = java.io.File(storagePath, "history.json")
            if (historyFile.exists()) historyFile.delete()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
