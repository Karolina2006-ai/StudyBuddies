package com.example.studybuddies.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddies.data.model.Message
import com.example.studybuddies.data.repository.ChatRepository
import com.example.studybuddies.data.repository.UserRepository
import com.example.studybuddies.ui.screens.chat.ChatData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val userRepository: UserRepository // Access to user-specific profile data
) : ViewModel() {

    // Internal state flow for the list of active chat summaries (who you are talking to)
    private val _chats = MutableStateFlow<List<ChatData>>(emptyList())
    val chats: StateFlow<List<ChatData>> = _chats.asStateFlow()

    // Internal state flow mapping Chat IDs to their respective list of message bubbles
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    init {
        // 1. Listen for chat list updates - Updates the inbox screen in real-time
        viewModelScope.launch {
            ChatRepository.chats.collect { chatList ->
                _chats.value = chatList
            }
        }

        // 2. Listen for messages (bubbles) updates - Updates the active conversation screen
        viewModelScope.launch {
            ChatRepository.messages.collect { messageMap ->
                _messages.value = messageMap
            }
        }

        // 3. Force refresh of chat list on startup - Ensures the inbox is current
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            ChatRepository.initialize(currentUserId)
        }
    }

    // Called when the student taps a conversation to open the chat details
    fun startObservingChat(chatId: String) {
        viewModelScope.launch {
            try {
                // Tells the Repository to start a specific Firebase listener for this ID
                ChatRepository.observeMessages(chatId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error observing chat: ${e.message}")
            }
        }
    }

    // Clears unread counts when the student views the messages
    fun markAsRead(chatId: String) {
        viewModelScope.launch {
            ChatRepository.markAsRead(chatId)
        }
    }

    // Validates and pushes a new message to the cloud database
    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return // Prevention of empty message bubbles

        viewModelScope.launch {
            try {
                ChatRepository.sendMessage(chatId, text.trim())
                // The Repository updates the Flow automatically upon successful send
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
            }
        }
    }
}