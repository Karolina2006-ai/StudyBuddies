package com.example.studybuddies.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studybuddies.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onChatClick: (String, String) -> Unit // Navigates to details, passing the conversation ID and Name
) {
    val logoBlue = Color(0xFF1A73E8)
    var searchQuery by remember { mutableStateOf("") } // Stores the current search text locally (we can search other users that we want to write to)

    // Obtains a lifecycle-aware stream of chat data from the ViewModel/Firestore
    val chatList by viewModel.chats.collectAsStateWithLifecycle()

    // Filters the chat list in real-time based on the search query (name or message content)
    val filteredChats = chatList.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.message.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        Text(
            text = "Chats",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = logoBlue,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        // Search bar with rounded corners and a leading search icon
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            placeholder = { Text("Search conversations...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = logoBlue) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = logoBlue,
                unfocusedBorderColor = Color(0xFFEEEEEE),
                focusedContainerColor = Color(0xFFF8F9FA),
                unfocusedContainerColor = Color(0xFFF8F9FA),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Logic to display either a "No Chats" message or the scrollable list
        if (chatList.isEmpty()) {
            EmptyChatPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Prevents bottom nav from hiding the last item
            ) {
                items(filteredChats) { chat ->
                    ChatItem(chat, logoBlue) {
                        onChatClick(chat.id, chat.name) // Triggers navigation when an item is tapped
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No conversations yet",
            color = Color.Black,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start by finding a tutor and asking a question!",
            color = Color.Gray,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChatItem(chat: ChatData, logoBlue: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Removes the default Material gray ripple/flash on tap
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logic to generate a 2-letter uppercase avatar from the user's name
        val initials = if (chat.name.isNotBlank()) {
            chat.name.trim().split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .uppercase()
        } else "?"

        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFF0F5FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = initials, color = logoBlue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.Black
                )
                // Displays the time of the last message on the far right
                Text(text = chat.time, color = Color.Gray, fontSize = 12.sp)
            }
            Text(
                text = chat.message,
                // If there are unread messages, the text is SemiBold and Black for visibility
                color = if (chat.unread > 0) Color.Black else Color.Gray,
                fontSize = 14.sp,
                maxLines = 1, // Truncates long messages to keep the list uniform
                fontWeight = if (chat.unread > 0) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
    // Decorative line between each conversation item
    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 1.dp, color = Color(0xFFF1F1F1))
}