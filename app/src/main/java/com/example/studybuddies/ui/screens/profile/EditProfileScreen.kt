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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.studybuddies.data.model.User
import com.example.studybuddies.viewmodel.AuthViewModel

/**
 * Screen for editing user profile information including photo, biography, and academic interests
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    user: User, // The current user data passed from the navigation
    authViewModel: AuthViewModel, // ViewModel handling the Firebase logic
    onNavigateBack: () -> Unit // Function to close this screen
) {
    val logoBlue = Color(0xFF1A73E8) // Standard app blue
    val lightBlueBg = Color(0xFFF0F5FF) // Accent background for avatars/chips

    // Observing the auth state to check for loading status during save operations
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // These variables store the modified values before the user hits "Save"
    var firstName by remember { mutableStateOf(user.firstName ?: "") }
    var surname by remember { mutableStateOf(user.surname ?: "") }
    var city by remember { mutableStateOf(user.city) }
    var university by remember { mutableStateOf(user.university) }
    var telephone by remember { mutableStateOf(user.telephone) }
    var bio by remember { mutableStateOf(user.bio) }

    // Dynamic lists that allow adding/removing items in real-time
    val hobbiesList = remember { user.hobbies.toMutableStateList() }
    var newHobbyTemp by remember { mutableStateOf("") }

    val subjectsList = remember { user.subjects.toMutableStateList() }
    var newSubjectTemp by remember { mutableStateOf("") }

    // Handles picking a photo from the device gallery
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
                        interactionSource = remember { MutableInteractionSource() }
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
                        // Creating a copy of the user object with the new data
                        val updatedUser = user.copy(
                            firstName = firstName.trim(),
                            surname = surname.trim(),
                            city = city.trim(),
                            university = university.trim(),
                            telephone = telephone.trim(),
                            bio = bio.trim(),
                            hobbies = ArrayList(hobbiesList),
                            subjects = ArrayList(subjectsList)
                        )

                        // Initiates the update process in the ViewModel
                        authViewModel.updateUserProfile(updatedUser, selectedImageUri) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = logoBlue),
                    enabled = !authState.isLoading, // Prevents multiple taps while uploading
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (authState.isLoading) {
                        // Shows progress while the image/data is being sent to Firebase
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()) // Makes the screen scrollable if the bio is long
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // The Avatar Editor
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(lightBlueBg)
                    .border(2.dp, logoBlue, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // UI Rule: No ripple flash on image click
                    ) { photoPickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    // Show the image that was just picked from the gallery
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (!user.profileImageUri.isNullOrEmpty()) {
                    // Show the existing image already on the server
                    AsyncImage(
                        model = user.profileImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Default to name initials if no image is present
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

            // Standard Information Inputs
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

            // Academic Interests Management
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

            // Biography Input
            ProfileTextField("Bio", bio, singleLine = false) { bio = it }

            Spacer(Modifier.height(24.dp))

            // Personal Interests Management
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

/**
 * Reusable text field styled specifically for the profile editing screen
 */
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
            color = Color.Black, // Ensures all labels remain Black
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A73E8), // Focus turns brand blue
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

/**
 * Section for adding and displaying interactive tags (Chips) for subjects or hobbies
 */
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
            color = Color.Black
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

        // Only render the FlowRow if there are actual items to show
        if (items.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    InputChip(
                        selected = true,
                        onClick = { onRemoveItem(item) }, // Tapping a chip removes it
                        label = { Text(item, color = logoBlue, fontWeight = FontWeight.Bold) },
                        trailingIcon = { Icon(Icons.Default.Close, null, tint = logoBlue, modifier = Modifier.size(16.dp)) },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = lightBlueBg,
                            selectedLabelColor = logoBlue
                        ),
                        border = BorderStroke(1.dp, logoBlue.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                }
            }
        }
    }
}