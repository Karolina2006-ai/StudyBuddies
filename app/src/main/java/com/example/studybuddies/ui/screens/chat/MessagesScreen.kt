package com.example.studybuddies.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studybuddies.viewmodel.ChatViewModel

/**
 * MessagesScreen - Displays a list of active conversations
 * Handles the high-level list state and search/empty results
 */
@Composable
fun MessagesScreen(
    viewModel: ChatViewModel,
    // Callback passes both ID (for Firebase) and Name (for the Detail Header)
    onChatClick: (String, String) -> Unit
) {
    val logoBlue = Color(0xFF1A73E8)
    val lightBlueBg = Color(0xFFF0F5FF)

    // Observes the chats flow from the ViewModel as state
    val chatList by viewModel.chats.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding() // Ensures content is below the system clock/icons
    ) {
        // Main Screen Title
        Text(
            text = "Messages",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = logoBlue,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        // Show a prompt if there are no messages, otherwise, show the list
        if (chatList.isEmpty()) {
            EmptyMessagesPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for bottom navigation
            ) {
                // Persistent header inside the scrollable area
                item {
                    Text(
                        text = "Recent Conversations",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                // Generates items dynamically from the chat list
                items(chatList) { chat ->
                    MessageItem(
                        chat = chat,
                        logoBlue = logoBlue,
                        lightBlueBg = lightBlueBg,
                        onClick = {
                            onChatClick(chat.id, chat.name)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual chat row component.
 */
@Composable
fun MessageItem(
    chat: ChatData,
    logoBlue: Color,
    lightBlueBg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Removes the default gray background flash when clicked
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Avatar section
        Box(modifier = Modifier.size(60.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(lightBlueBg),
                contentAlignment = Alignment.Center
            ) {
                // Extracts the first letter of first and last name (e.g., "John Doe" -> "JD")
                val initials = if (chat.name.isNotBlank()) {
                    chat.name.trim().split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()
                } else "?"

                Text(initials, color = logoBlue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            // Green "Online" status badge positioned at the bottom-right of the avatar
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
            }
        }

        // Text content section (Name, Message Preview, and Time)
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
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
                Text(
                    text = chat.time,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.message,
                    // If unread messages exist, the preview turns black (instead of gray)
                    color = if (chat.unread > 0) Color.Black else Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                // Blue notification badge for unread message counts
                if (chat.unread > 0) {
                    Surface(
                        color = logoBlue,
                        shape = CircleShape,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${chat.unread}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Centered state for empty lists.
 */
@Composable
fun EmptyMessagesPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No messages yet", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 18.sp)
        Text("Your conversations will appear here", color = Color.Gray, fontSize = 14.sp)
    }
}