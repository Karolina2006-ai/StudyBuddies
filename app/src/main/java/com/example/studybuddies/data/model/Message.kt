package com.example.studybuddies.data.model

import com.google.firebase.firestore.DocumentId

// Message model class
// We use 'var' and default values so Firebase can easily read/deserialize the data
data class Message(
    // @DocumentId automatically populates this field with the document's unique ID from Firestore.
    // This is crucial for LazyColumn (lists) to uniquely identify items.
    @DocumentId
    var id: String = "",

    var senderId: String = "",
    var text: String = "",

    // Using Long instead of Timestamp because ChatViewModel sends System.currentTimeMillis()
    // It is lighter and easier to sort.
    var timestamp: Long = System.currentTimeMillis(),

    var isRead: Boolean = false
)