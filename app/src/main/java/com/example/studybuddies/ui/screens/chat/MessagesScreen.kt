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
 * MessagesScreen - Displays a list of active conversations.
 * Updated: Navigation by ID and Name, consistent with ChatData model.
 */
@Composable
fun MessagesScreen(
    viewModel: ChatViewModel,
    // FIX: Changed to (String, String) to match NavHost requirements
    onChatClick: (String, String) -> Unit
) {
    val logoBlue = Color(0xFF1A73E8)
    val lightBlueBg = Color(0xFFF0F5FF)
    val chatList by viewModel.chats.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Screen Title (Rule 6)
        Text(
            text = "Messages",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = logoBlue,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        if (chatList.isEmpty()) {
            EmptyMessagesPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header item inside the list
                item {
                    Text(
                        text = "Recent Conversations",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black, // BLACK HEADER (Rule 1)
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                items(chatList) { chat ->
                    MessageItem(
                        chat = chat,
                        logoBlue = logoBlue,
                        lightBlueBg = lightBlueBg,
                        onClick = {
                            // FIX: Passing ID (for database) and Name (for header)
                            onChatClick(chat.id, chat.name)
                        }
                    )
                }
            }
        }
    }
}

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
                indication = null // REMOVING GRAY RIPPLE (Rule 2)
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with initials
        Box(modifier = Modifier.size(60.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(lightBlueBg),
                contentAlignment = Alignment.Center
            ) {
                // Initials Logic (e.g. "John Doe" -> "JD")
                val initials = if (chat.name.isNotBlank()) {
                    chat.name.trim().split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()
                } else "?"

                Text(initials, color = logoBlue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            // Green Online Status Indicator (Rule 7)
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
                    text = chat.name, // Full Name (Rule 6)
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.Black
                )
                Text(
                    text = chat.time,
                    color = Color.Black, // BLACK DATE (Rule 1)
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.message,
                    color = if (chat.unread > 0) Color.Black else Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                // Blue unread message counter
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