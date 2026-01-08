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

    // Load all messages from encrypted database
    // NOTE: getAllMessages will be available after running update-libs workflow
    // to get the latest bindings from client-lib
    fun loadLocalHistory(): List<FfiMessage> {
        return try {
            uniffi.rebelio_client.getAllMessages()
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
