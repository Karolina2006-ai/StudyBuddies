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
    private val userRepository: UserRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<ChatData>>(emptyList())
    val chats: StateFlow<List<ChatData>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    init {
        // 1. Listen for chat list updates
        viewModelScope.launch {
            ChatRepository.chats.collect { chatList ->
                _chats.value = chatList
            }
        }

        // 2. Listen for messages (bubbles) updates
        viewModelScope.launch {
            ChatRepository.messages.collect { messageMap ->
                _messages.value = messageMap
            }
        }

        // 3. Force refresh of chat list on startup
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            ChatRepository.initialize(currentUserId)
        }
    }

    // Called when entering a specific conversation
    fun startObservingChat(chatId: String) {
        viewModelScope.launch {
            try {
                ChatRepository.observeMessages(chatId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error observing chat: ${e.message}")
            }
        }
    }

    fun markAsRead(chatId: String) {
        viewModelScope.launch {
            ChatRepository.markAsRead(chatId)
        }
    }

    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                ChatRepository.sendMessage(chatId, text.trim())
                // After sending, Repository updates the Flow automatically, so the bubble appears instantly
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
            }
        }
    }
}