package com.kaidendev.rebelioclientandroid.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.rebelio_client.*





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
            uniffi.rebelio_client.updateContact(oldNickname, newNickname, routingToken)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGroup(name: String, members: List<String>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val groupId = uniffi.rebelio_client.createGroup(name, members)
            Result.success(groupId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addGroupMembers(groupId: String, memberRoutingTokens: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            uniffi.rebelio_client.addGroupMembers(groupId, memberRoutingTokens)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeGroupMembers(groupId: String, memberIds: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            uniffi.rebelio_client.removeGroupMembers(groupId, memberIds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveGroup(groupId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            uniffi.rebelio_client.leaveGroup(groupId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadGroups(): Result<List<FfiGroup>> = withContext(Dispatchers.IO) {
        try {
            val groups = uniffi.rebelio_client.loadGroups()
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendGroupMessage(groupId: String, message: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            uniffi.rebelio_client.sendGroupMessage(groupId, message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSentMessageStatuses(): Result<List<FfiStatusUpdate>> = withContext(Dispatchers.IO) {
        try {
            val updates = uniffi.rebelio_client.getSentMessageStatuses()
            Result.success(updates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listDevices(): Result<List<FfiDeviceInfo>> = withContext(Dispatchers.IO) {
        try {
            val devices = uniffi.rebelio_client.listDevices()
            Result.success(devices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun revokeDevice(deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            uniffi.rebelio_client.revokeDevice(deviceId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerPushToken(token: String, platform: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            uniffi.rebelio_client.registerPushToken(token, platform)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadBlob(filePath: String, mimeType: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val blobId = uniffi.rebelio_client.uploadBlob(filePath, mimeType)
            Result.success(blobId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadBlob(blobId: String, destinationPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            uniffi.rebelio_client.downloadBlob(blobId, destinationPath)
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
