package com.example.studybuddies.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.studybuddies.data.model.User
import com.example.studybuddies.viewmodel.AuthViewModel

/**
 * Updated Edit Profile Screen:
 * 1. Smooth transitions and no ripple effect (Rule 2).
 * 2. All labels and texts are BLACK.
 * 3. Text field borders (Focus) in color 0xFF1A73E8.
 * FIX: Converted lists to ArrayList to match User model requirements.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    user: User,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val logoBlue = Color(0xFF1A73E8)
    val lightBlueBg = Color(0xFFF0F5FF)

    // Editing States - Point 7: Efficient data handling
    var firstName by remember { mutableStateOf(user.firstName ?: "") }
    var surname by remember { mutableStateOf(user.surname ?: "") }
    var city by remember { mutableStateOf(user.city) }
    var university by remember { mutableStateOf(user.university) }
    var telephone by remember { mutableStateOf(user.telephone) }
    var bio by remember { mutableStateOf(user.bio) }

    // Editable lists (Hobbies and Subjects)
    val hobbiesList = remember { user.hobbies.toMutableStateList() }
    var newHobbyTemp by remember { mutableStateOf("") }

    val subjectsList = remember { user.subjects.toMutableStateList() }
    var newSubjectTemp by remember { mutableStateOf("") }

    // Image Selection
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedImageUri = uri
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        interactionSource = remember { MutableInteractionSource() } // No ripple (Rule 2)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = logoBlue)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 0.dp, color = Color.White) {
                Button(
                    onClick = {
                        val updatedUser = user.copy(
                            firstName = firstName.trim(),
                            surname = surname.trim(),
                            city = city.trim(),
                            university = university.trim(),
                            telephone = telephone.trim(),
                            bio = bio.trim(),
                            // FIX: Explicitly convert to ArrayList to match User model
                            hobbies = ArrayList(hobbiesList),
                            subjects = ArrayList(subjectsList),
                            // If a new photo was selected, use its URI, otherwise keep the old one
                            profileImageUri = selectedImageUri?.toString() ?: user.profileImageUri
                        )
                        authViewModel.updateUserProfile(updatedUser) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = logoBlue),
                    interactionSource = remember { MutableInteractionSource() } // No ripple
                ) {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Photo Section - Point 6: Initials fallback
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(lightBlueBg)
                    .border(2.dp, logoBlue, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { photoPickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (!user.profileImageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = user.profileImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = user.initials,
                        color = logoBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }
            }
            Text(
                "Change Photo",
                color = logoBlue,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Text Fields - Point 1: Black text
            ProfileTextField("First Name", firstName) { firstName = it }
            Spacer(Modifier.height(16.dp))
            ProfileTextField("Surname", surname) { surname = it }
            Spacer(Modifier.height(16.dp))
            ProfileTextField("City", city) { city = it }
            Spacer(Modifier.height(16.dp))
            ProfileTextField("University", university) { university = it }
            Spacer(Modifier.height(16.dp))
            ProfileTextField("Phone", telephone, KeyboardType.Phone) { telephone = it }
            Spacer(Modifier.height(16.dp))

            // Subjects Section - Point 7: Dynamic adding
            ChipInputSection(
                title = "Subjects",
                tempValue = newSubjectTemp,
                onValueChange = { newSubjectTemp = it },
                items = subjectsList,
                onAddItem = { if (newSubjectTemp.isNotBlank()) { subjectsList.add(newSubjectTemp.trim()); newSubjectTemp = "" } },
                onRemoveItem = { subjectsList.remove(it) },
                logoBlue = logoBlue,
                lightBlueBg = lightBlueBg
            )

            Spacer(Modifier.height(24.dp))

            ProfileTextField("Bio", bio, singleLine = false) { bio = it }

            Spacer(Modifier.height(24.dp))

            // Hobbies Section
            ChipInputSection(
                title = "Hobbies & Interests",
                tempValue = newHobbyTemp,
                onValueChange = { newHobbyTemp = it },
                items = hobbiesList,
                onAddItem = { if (newHobbyTemp.isNotBlank()) { hobbiesList.add(newHobbyTemp.trim()); newHobbyTemp = "" } },
                onRemoveItem = { hobbiesList.remove(it) },
                logoBlue = logoBlue,
                lightBlueBg = lightBlueBg
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black, // Black label (Rule 1)
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A73E8), // Blue border (Rule 6)
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipInputSection(
    title: String,
    tempValue: String,
    onValueChange: (String) -> Unit,
    items: List<String>,
    onAddItem: () -> Unit,
    onRemoveItem: (String) -> Unit,
    logoBlue: Color,
    lightBlueBg: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black // Black header (Rule 1)
        )

        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = tempValue,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type to add...", fontSize = 14.sp, color = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = logoBlue,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                singleLine = true
            )
            IconButton(
                onClick = onAddItem,
                modifier = Modifier.padding(start = 8.dp),
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Icon(Icons.Default.AddCircle, null, tint = logoBlue, modifier = Modifier.size(36.dp))
            }
        }

        if (items.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    InputChip(
                        selected = true,
                        onClick = { onRemoveItem(item) },
                        label = { Text(item, color = logoBlue, fontWeight = FontWeight.Bold) },
                        trailingIcon = { Icon(Icons.Default.Close, null, tint = logoBlue, modifier = Modifier.size(16.dp)) },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = lightBlueBg,
                            selectedLabelColor = logoBlue
                        ),
                        border = BorderStroke(1.dp, logoBlue.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        interactionSource = remember { MutableInteractionSource() } // No ripple
                    )
                }
            }
        }
    }
}