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

/**
 * Object that manages all chat-related data
 * Using an 'object' ensures we don't have multiple instances
 */
object ChatRepository {

    private val firestore = FirebaseFirestore.getInstance() // Direct access to the Firestore database
    private val auth = FirebaseAuth.getInstance() // Access to current user authentication state

    // StateFlow acts like a 'live stream' of data
    // This map stores messages indexed by their ChatId, so switching chats is instant
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Holds the list of all conversations the user is currently involved in
    private val _chats = MutableStateFlow<List<ChatData>>(emptyList())
    val chats: StateFlow<List<ChatData>> = _chats.asStateFlow()

    // Entry point to start fetching data once we know who the user is
    fun initialize(userId: String) {
        loadChats(userId) // Start the real-time listener for the inbox
    }

    /**
     * Sets up a real-time listener for the user's active chats
     * If someone sends a message, the list updates automatically without refreshing
     */
    fun loadChats(userId: String) {
        Log.d("ChatRepo", "Loading chats for user: $userId")

        firestore.collection("chats")
            .whereArrayContains("participants", userId) // Finds any chat where I am a member
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ChatRepo", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Sort by the newest message so the most active chat is at the top of the list
                    val sortedDocs = snapshot.documents.sortedByDescending {
                        it.getLong("lastMessageTimestamp") ?: 0L
                    }

                    val chatList = sortedDocs.map { doc ->
                        val lastMsg = doc.getString("lastMessage") ?: "Start chatting"
                        val timestamp = doc.getLong("lastMessageTimestamp") ?: System.currentTimeMillis()

                        // Logic to figure out the name of the 'other' person in the chat
                        val participants = doc.get("participants") as? List<String> ?: emptyList()
                        val otherUserId = participants.find { it != userId } ?: "Unknown"

                        val namesMap = doc.get("participantNames") as? Map<String, String> ?: emptyMap()
                        val otherUserName = namesMap[otherUserId] ?: "User"

                        ChatData(
                            id = doc.id,
                            name = otherUserName,
                            message = lastMsg,
                            time = formatTime(timestamp), // Convert the Long timestamp to a readable String
                            unread = 0 // Defaulting to 0 for UI initialization
                        )
                    }

                    _chats.value = chatList // Push the new list into the 'stream' for the UI to see
                }
            }
    }

    /**
     * Converts raw milliseconds into human-friendly text like "5m ago".
     */
    private fun formatTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60000 -> "Just now" // Less than a minute
            diff < 3600000 -> "${diff / 60000}m ago" // Minutes
            diff < 86400000 -> "${diff / 3600000}h ago" // Hours
            else -> "Older" // More than a day
        }
    }

    /**
     * Sets up a listener for a specific conversation's messages.
     */
    fun observeMessages(chatId: String) {
        if (chatId.isEmpty()) return

        firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Oldest at top, newest at bottom.
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    // Convert Firestore documents back into our 'Message' data class objects automatically.
                    val messageList = snapshot.toObjects(Message::class.java)
                    _messages.update { currentMap ->
                        currentMap + (chatId to messageList) // Update the map with the latest conversation data
                    }
                }
            }
    }

    /**
     * Logic for sending a message.
     * It writes to the 'messages' sub-collection AND updates the parent 'chat' document.
     */
    suspend fun sendMessage(chatId: String, text: String) {
        val currentUid = auth.currentUser?.uid ?: return

        val newMessage = Message(
            id = UUID.randomUUID().toString(), // Generate a unique ID locally
            senderId = currentUid,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        // Add the message to the specific conversation's sub-collection.
        firestore.collection("chats").document(chatId)
            .collection("messages")
            .add(newMessage)
            .await() // Suspend until the database write is complete

        // Update the 'preview' info in the main chat document for the inbox view.
        val chatUpdate = mapOf(
            "lastMessage" to text,
            "lastMessageTimestamp" to System.currentTimeMillis()
        )
        firestore.collection("chats").document(chatId).update(chatUpdate).await()
    }

    /**
     * Checks if a chat already exists between two users.
     * If not, it creates a new document to hold their messages.
     */
    suspend fun createChatIfNotExists(currentUserId: String, otherUserId: String, otherUserName: String): String {
        val query = firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .get()
            .await()

        // Look through my existing chats to see if one already contains the other specific user.
        val existingChat = query.documents.find { doc ->
            val participants = doc.get("participants") as? List<String> ?: emptyList()
            participants.contains(otherUserId)
        }

        if (existingChat != null) {
            return existingChat.id // Found it, return the existing ID.
        }

        // None found, so let's create a brand new conversation document.
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

        newChatRef.set(chatData).await() // Save the new chat metadata
        return newChatRef.id // Return the new document ID for immediate navigation
    }

    /**
     * Missing function from ViewModel: Updates the unread status for a chat.
     */
    suspend fun markAsRead(chatId: String) {
        val currentUid = auth.currentUser?.uid ?: return
        // Future Logic: Update unread counts in Firestore
        Log.d("ChatRepo", "Marking chat $chatId as read for $currentUid")
    }
}