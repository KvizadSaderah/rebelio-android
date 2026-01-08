package com.kaidendev.rebelioclientandroid.ui

import app.cash.turbine.test
import com.kaidendev.rebelioclientandroid.MainDispatcherRule
import com.kaidendev.rebelioclientandroid.repository.RebelioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import uniffi.rebelio_client.FfiContact
import uniffi.rebelio_client.FfiMessage
import uniffi.rebelio_client.FfiStatus

@OptIn(ExperimentalCoroutinesApi::class)
class RebelioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<RebelioRepository>() // Not relaxed, we want strict control or explicit relaxed
    
    private fun setupMocks() {
        // Mock init block calls
        coEvery { repository.getStatus() } returns Result.success(FfiStatus(false, "", "", ""))
        coEvery { repository.loadContacts() } returns Result.success(emptyList())
        coEvery { repository.loadGroups() } returns Result.success(emptyList())
        coEvery { repository.getInboxMessages() } returns Result.success(emptyList())
        coEvery { repository.loadLocalHistory() } returns emptyList() // Not suspend, direct call? Actually loadLocalHistory is used inside loadData
    }

    private fun createViewModel(): RebelioViewModel {
        setupMocks()
        return RebelioViewModel(repository)
    }

    @Test
    fun `initial state is correct`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        
        assertFalse(state.isRegistered)
        assertFalse(state.isLoading) // Should be false after init completes
        assertTrue(state.messages.isEmpty())
        assertTrue(state.contacts.isEmpty())
    }

    @Test
    fun `sendMessage updates UI on success`() = runTest {
        val viewModel = createViewModel()
        val recipientToken = "contact_token_123"
        val messageText = "Hello World"
        
        // Mock successful send
        coEvery { repository.sendMessage(recipientToken, messageText) } returns Result.success(Unit)
        // Mock loadLocalHistory (called after send to get correct message ID)
        coEvery { repository.loadLocalHistory() } returns emptyList()
        
        viewModel.sendMessage(recipientToken, messageText)
        
        // Wait for coroutine to complete
        advanceUntilIdle()
        
        // Verify UI state contains the sent message
        val state = viewModel.uiState.value
        assertEquals(1, state.messages.size)
        val msg = state.messages.first()
        assertEquals(messageText, msg.content)
        assertTrue(msg.sender.startsWith("me:"))
        
        // Verify repository call
        coVerify { repository.sendMessage(recipientToken, messageText) }
    }

    @Test
    fun `addContact calls repository and reloads data`() = runTest {
        val viewModel = createViewModel()
        val nickname = "Alice"
        val token = "alice_token"
        
        // Mock add success
        coEvery { repository.addContact(nickname, token) } returns Result.success(Unit)
        coEvery { repository.loadContacts() } returns Result.success(
            listOf(FfiContact(nickname, token))
        )
        coEvery { repository.getInboxMessages() } returns Result.success(emptyList())
        coEvery { repository.loadGroups() } returns Result.success(emptyList())

        viewModel.uiState.test {
            // StateFlow emits current value immediately. 
            // This is the stable state after init.
            val initial = awaitItem()
            assertFalse(initial.isLoading)
            
            viewModel.addContact(nickname, token)
            
            // We might see loading=true, or we might skip directly to final state due to conflation
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            
            // Now we should be in final success state
            assertFalse(state.isLoading)
            assertEquals(1, state.contacts.size)
            assertEquals(nickname, state.contacts[0].nickname)
            
            cancelAndIgnoreRemainingEvents()
        }
        
        coVerify { repository.addContact(nickname, token) }
    }
    
    @Test
    fun `renameContact calls updateContact`() = runTest {
        val viewModel = createViewModel()
        val oldName = "User-123"
        val newName = "Bob"
        val token = "bob_token"

        coEvery { repository.updateContact(oldName, newName, token) } returns Result.success(Unit)
        coEvery { repository.loadContacts() } returns Result.success(listOf(FfiContact(newName, token)))
        coEvery { repository.getInboxMessages() } returns Result.success(emptyList())
        coEvery { repository.loadGroups() } returns Result.success(emptyList())

        viewModel.uiState.test {
            awaitItem() // Initial
            
            viewModel.renameContact(oldName, newName, token)
            
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            
            assertFalse(state.isLoading)
            assertEquals(1, state.contacts.size)
            assertEquals(newName, state.contacts[0].nickname)
            
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { repository.updateContact(oldName, newName, token) }
    }
}
