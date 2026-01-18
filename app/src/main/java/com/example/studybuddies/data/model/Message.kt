package com.example.studybuddies.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Message model class
 * We use 'var' and default values so Firebase can easily read/deserialize the data.
 * Basically, Firebase needs a "no-argument constructor" to create the object before filling it with data.
 */
data class Message(
    // @DocumentId automatically populates this field with the document's unique ID from Firestore.
    // This is crucial for things like 'LazyColumn' (the chat list) to uniquely identify items.
    @DocumentId
    var id: String = "",

    // Who sent the message? This will be the User UID from Firebase Auth.
    var senderId: String = "",

    // The actual text content of the message.
    var text: String = "",

    /**
     * Using Long instead of Timestamp because ChatViewModel sends System.currentTimeMillis().
     * It's super easy to compare two numbers (Longs) to see which message came first.
     * Also, it avoids some of the timezone headaches you get with other date types.
     */
    var timestamp: Long = System.currentTimeMillis(),

    // A flag to check if the other person has opened the chat.
    // Useful if we want to show "Seen" or "Unread" badges later.
    var isRead: Boolean = false
)