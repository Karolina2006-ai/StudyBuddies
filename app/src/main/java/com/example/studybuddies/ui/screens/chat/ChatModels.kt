package com.example.studybuddies.ui.screens.chat

/**
 * Model representing a chat preview on the conversation list (Chats Screen).
 * Contains the ID which is essential for opening a specific conversation.
 */
data class ChatData(
    val id: String = "", // <--- This is the Chat Document ID (needed for navigation)
    val name: String,    // Full Name (e.g., "Karolina Kowalska")
    val message: String, // Content of the last message (preview)
    val time: String,    // Formatted date/time string
    val unread: Int = 0  // Number of unread messages
)