package com.example.studybuddies.data.repository

import android.util.Log
import com.example.studybuddies.data.model.Message
import com.example.studybuddies.ui.screens.chat.ChatData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.UUID

object ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatData>>(emptyList())
    val chats: StateFlow<List<ChatData>> = _chats.asStateFlow()

    // --- 1. INITIALIZE FUNCTION (Fixes issue in ChatViewModel) ---
    fun initialize(userId: String) {
        loadChats(userId)
    }

    // --- 2. LOADING CHATS ---
    fun loadChats(userId: String) {
        Log.d("ChatRepo", "Loading chats for user: $userId")

        firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ChatRepo", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // First, sort documents by time while we have access to the timestamp
                    val sortedDocs = snapshot.documents.sortedByDescending {
                        it.getLong("lastMessageTimestamp") ?: 0L
                    }

                    val chatList = sortedDocs.map { doc ->
                        val lastMsg = doc.getString("lastMessage") ?: "Start chatting"
                        val timestamp = doc.getLong("lastMessageTimestamp") ?: System.currentTimeMillis()

                        // Logic to find the other participant's name
                        val participants = doc.get("participants") as? List<String> ?: emptyList()
                        val otherUserId = participants.find { it != userId } ?: "Unknown"

                        val namesMap = doc.get("participantNames") as? Map<String, String> ?: emptyMap()
                        val otherUserName = namesMap[otherUserId] ?: "User"

                        // FIX: Using field names consistent with your ChatData model (message, time, unread)
                        ChatData(
                            id = doc.id,
                            name = otherUserName,
                            message = lastMsg,           // Was 'lastMessage', fixed to 'message'
                            time = formatTime(timestamp), // Was 'timestamp', fixed to 'time' (String)
                            unread = 0                   // Was 'unreadCount', fixed to 'unread'
                        )
                    }

                    _chats.value = chatList
                }
            }
    }

    // --- Helper function for time formatting (added because ChatData requires a String) ---
    private fun formatTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60000 -> "Just now" // Less than a minute
            diff < 3600000 -> "${diff / 60000}m ago" // Minutes ago
            diff < 86400000 -> "${diff / 3600000}h ago" // Hours ago
            else -> "Older" // Older
        }
    }

    fun observeMessages(chatId: String) {
        if (chatId.isEmpty()) return

        firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    val messageList = snapshot.toObjects(Message::class.java)
                    _messages.update { currentMap ->
                        currentMap + (chatId to messageList)
                    }
                }
            }
    }

    suspend fun sendMessage(chatId: String, text: String) {
        val currentUid = auth.currentUser?.uid ?: return

        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            senderId = currentUid,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("chats").document(chatId)
            .collection("messages")
            .add(newMessage)
            .await()

        val chatUpdate = mapOf(
            "lastMessage" to text,
            "lastMessageTimestamp" to System.currentTimeMillis()
        )
        firestore.collection("chats").document(chatId).update(chatUpdate).await()
    }

    fun markAsRead(chatId: String) {
        // Optional implementation
    }

    suspend fun createChatIfNotExists(currentUserId: String, otherUserId: String, otherUserName: String): String {
        val query = firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .get()
            .await()

        val existingChat = query.documents.find { doc ->
            val participants = doc.get("participants") as? List<String> ?: emptyList()
            participants.contains(otherUserId)
        }

        if (existingChat != null) {
            return existingChat.id
        }

        val currentUserName = auth.currentUser?.displayName ?: "Me"

        val newChatRef = firestore.collection("chats").document()
        val chatData = hashMapOf(
            "participants" to listOf(currentUserId, otherUserId),
            "participantNames" to mapOf(
                currentUserId to currentUserName,
                otherUserId to otherUserName
            ),
            "lastMessage" to "",
            "lastMessageTimestamp" to System.currentTimeMillis()
        )

        newChatRef.set(chatData).await()
        return newChatRef.id
    }
}