package com.example.studybuddies.ui.screens.chat

import android.widget.Toast
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.* import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studybuddies.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.studybuddies.data.model.Message

// Custom Indication to disable ripple (removes the default Material wave effect on clicks)
private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return object : Modifier.Node(), DrawModifierNode {
            override fun ContentDrawScope.draw() {
                drawContent() // Simply draws the content without adding visual feedback layers
            }
        }
    }
    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = System.identityHashCode(this)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String, // ID of the specific conversation
    chatName: String, // Name of the person you are chatting with
    viewModel: ChatViewModel, // Shared chat logic
    onBack: () -> Unit // Navigation back to the list
) {
    val context = LocalContext.current
    val logoBlue = Color(0xFF1A73E8) // Standard app theme color
    val lightBlueBg = Color(0xFFF0F5FF)

    // Observe messages for this specific chat ID from the global state
    val allMessages by viewModel.messages.collectAsStateWithLifecycle()
    val messages = allMessages[chatId] ?: emptyList()

    // Identify the current user to distinguish between "Sent" and "Received" bubbles
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    var messageText by remember { mutableStateOf("") } // Local state for the text input
    val listState = rememberLazyListState() // Controls the scroll position of the message list

    // EFFECT: Automatically scroll to the bottom whenever a new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Surface(color = Color.White, shadowElevation = 0.5.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // Ensures the TopBar doesn't hide under the system clock/icons
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalIndication provides NoIndication) {
                        IconButton(
                            onClick = onBack,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Icon(Icons.Default.ArrowBack, null, tint = logoBlue)
                        }
                    }

                    // --- AVATAR / INITIALS ---
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(lightBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        // Logic to create a 2-letter uppercase initials placeholder
                        val initials = chatName.trim().split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                            .uppercase()
                            .ifEmpty { "?" }

                        Text(
                            text = initials,
                            color = logoBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = chatName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        },
        bottomBar = {
            // --- INPUT AREA ---
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // Adjusts for the system home gesture bar
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { Toast.makeText(context, "Attachments coming soon!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, null, tint = logoBlue)
                    }

                    Spacer(Modifier.width(12.dp))

                    // Text Input Field
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f) // Takes up remaining horizontal space
                            .heightIn(min = 48.dp, max = 120.dp), // Expands as user types multiple lines
                        placeholder = { Text("Message...", color = Color.Gray, fontSize = 14.sp) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = logoBlue,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedContainerColor = Color(0xFFF8F9FA),
                            cursorColor = logoBlue
                        ),
                        singleLine = false // Allows the message box to expand vertically
                    )

                    Spacer(Modifier.width(8.dp))

                    // SEND BUTTON
                    CompositionLocalProvider(LocalIndication provides NoIndication) {
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(chatId, messageText.trim())
                                    messageText = "" // Clear input after sending
                                }
                            },
                            enabled = messageText.isNotBlank(), // Button is grayed out if empty
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = if (messageText.isNotBlank()) logoBlue else Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        // --- MESSAGE LIST ---
        LazyColumn(
            state = listState, // Attach scroll state
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == currentUserId
                ChatBubble(text = msg.text, isMe = isMe, logoBlue = logoBlue)
            }
        }
    }
}

/**
 * Visual component for a single message bubble.
 */
@Composable
fun ChatBubble(text: String, isMe: Boolean, logoBlue: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start // Sent = Right, Received = Left
    ) {
        Surface(
            color = if (isMe) logoBlue else Color(0xFFF1F3F4), // Sent = Blue, Received = Gray
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                // Tail of the bubble points to the sender
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            )
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (isMe) Color.White else Color.Black,
                fontSize = 15.sp
            )
        }
    }
}