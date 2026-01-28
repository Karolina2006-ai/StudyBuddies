package com.example.studybuddies.ui.screens.chat

/**
 * Model representing a chat preview on the conversation list (Chats Screen)
 * Contains the ID which is essential for opening a specific conversation
 */
data class ChatData(
    val id: String = "", // Unique Firestore Document ID; used to route the user to the correct ChatDetailScreen
    val name: String,    // Display name of the participant (Tutor or Student)
    val message: String, // The snippet of the most recent message shown in the list
    val time: String,    // Timestamp of the last interaction, usually formatted as "HH:mm" or "dd/MM"
    val unread: Int = 0  // Counter for unread messages, used to display notification badges on the UI
)