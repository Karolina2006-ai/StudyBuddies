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
import androidx.compose.material.icons.filled.Send // FIX: Changed to standard Send icon for compatibility
import androidx.compose.material3.* // Imports everything from Material3, including Text and OutlinedTextField
import androidx.compose.runtime.*
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

// Custom Indication to disable ripple (No wave effect on click)
private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return object : Modifier.Node(), DrawModifierNode {
            override fun ContentDrawScope.draw() {
                drawContent()
            }
        }
    }
    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = System.identityHashCode(this)
}

@OptIn(ExperimentalMaterial3Api::class) // Added in case your M3 version requires it
@Composable
fun ChatDetailScreen(
    chatId: String,
    chatName: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current // Needed for Toast (attachment button)
    val logoBlue = Color(0xFF1A73E8)
    val lightBlueBg = Color(0xFFF0F5FF)

    val allMessages by viewModel.messages.collectAsStateWithLifecycle()
    val messages = allMessages[chatId] ?: emptyList()

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
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
                        .statusBarsPadding()
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

                    // --- INITIALS CIRCLE ---
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(lightBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        // Splits name (e.g. "John D") and takes "JD"
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
            // --- TYPING BAR ---
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // Spacing from system bottom bar
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment Icon
                    IconButton(
                        onClick = { Toast.makeText(context, "Attachments coming soon!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, null, tint = logoBlue)
                    }

                    Spacer(Modifier.width(12.dp))

                    // IMPROVED OutlinedTextField
                    // Removed specific text colors that might cause errors in older M3 versions,
                    // kept key container and border colors.
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 120.dp),
                        placeholder = { Text("Message...", color = Color.Gray, fontSize = 14.sp) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = logoBlue,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedContainerColor = Color(0xFFF8F9FA),
                            cursorColor = logoBlue
                            // focusedTextColor and unfocusedTextColor removed for compatibility.
                            // Text will default to black thanks to app theme.
                        ),
                        singleLine = false // Changed to false so users can write multi-line messages
                    )

                    Spacer(Modifier.width(8.dp))

                    CompositionLocalProvider(LocalIndication provides NoIndication) {
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(chatId, messageText.trim())
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank(),
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Icon(
                                // Using standard Send icon (safer than AutoMirrored)
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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Respects TopBar
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

@Composable
fun ChatBubble(text: String, isMe: Boolean, logoBlue: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) logoBlue else Color(0xFFF1F3F4),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
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