package com.example.studybuddies.ui.screens.auth

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.studybuddies.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrationDetailsScreen(
    role: String, // Whether the user is a Tutor or Student
    viewModel: AuthViewModel, // Handles Firebase Auth logic
    onNavigateToLogin: () -> Unit, // Back to login screen
    onRegistrationSuccess: () -> Unit, // Callback for successful signup
    onBack: () -> Unit // Go back to Role Selection
) {
    // Observing state from ViewModel to handle loading/errors
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // UI Constants for branding
    val logoBlue = Color(0xFF1A73E8)
    val lightBlueBg = Color(0xFFF0F5FF)
    val fieldTextSize = 14.sp
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Local UI State for form inputs
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var city by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var hobbiesAndInterests by remember { mutableStateOf("") }
    var selectedSubjects by remember { mutableStateOf(setOf<String>()) }

    // Pre-defined list for subject chips
    val subjectsList = listOf("Mathematics", "Physics", "Chemistry", "Biology", "English", "History", "Computer Science")

    // Image Picker Setup: Standard Android contract for selecting content
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()) // Necessary for long forms
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        // Back button navigation logic
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart),
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = logoBlue)
            }
        }

        Text("Complete your profile", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = logoBlue)
        Text("Registering as a $role", color = Color.Black, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

        // Profile Picture Upload Area
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(lightBlueBg)
                .border(2.dp, logoBlue, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { photoPickerLauncher.launch("image/*") }, // Launch the gallery picker
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage( // Coil library used for high-performance image loading
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = logoBlue)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Personal Information Fields
        RegistrationInputBox("Name", firstName, { firstName = it }, "First name", logoBlue, lightBlueBg, fieldTextSize)
        Spacer(modifier = Modifier.height(12.dp))
        RegistrationInputBox("Surname", surname, { surname = it }, "Last name", logoBlue, lightBlueBg, fieldTextSize)
        Spacer(modifier = Modifier.height(12.dp))
        RegistrationInputBox("Email", email, { email = it }, "example@email.com", logoBlue, lightBlueBg, fieldTextSize, KeyboardType.Email)
        Spacer(modifier = Modifier.height(12.dp))

        // Secure Password Input with visibility toggle
        RegistrationInputBox(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            hint = "Min. 6 characters",
            color = logoBlue,
            bg = lightBlueBg,
            fontSize = fieldTextSize,
            isPassword = true,
            passwordVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            kType = KeyboardType.Password
        )

        Spacer(modifier = Modifier.height(12.dp))
        RegistrationInputBox("City", city, { city = it }, "Your city", logoBlue, lightBlueBg, fieldTextSize)
        Spacer(modifier = Modifier.height(12.dp))
        RegistrationInputBox("University", university, { university = it }, "School name", logoBlue, lightBlueBg, fieldTextSize)
        Spacer(modifier = Modifier.height(12.dp))
        RegistrationInputBox("Telephone", telephone, { telephone = it }, "Phone number", logoBlue, lightBlueBg, fieldTextSize, KeyboardType.Phone)
        Spacer(modifier = Modifier.height(12.dp))
        RegistrationInputBox("Bio (Optional)", bio, { bio = it }, "Write something about yourself", logoBlue, lightBlueBg, fieldTextSize)
        Spacer(modifier = Modifier.height(12.dp))
        RegistrationInputBox("Hobbies (e.g. Volleyball, Chess)", hobbiesAndInterests, { hobbiesAndInterests = it }, "Separate with commas", logoBlue, lightBlueBg, fieldTextSize)

        Spacer(modifier = Modifier.height(24.dp))

        // Subject Selection Area (Using FlowRow for automatic wrapping)
        Text(
            text = "Subjects you want to ${if(role == "Tutor") "teach" else "learn"}",
            fontWeight = FontWeight.Bold,
            fontSize = fieldTextSize,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjectsList.forEach { subject ->
                val isSelected = selectedSubjects.contains(subject)
                Surface(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Toggle selection logic using immutable Set operations
                        selectedSubjects = if (isSelected) selectedSubjects - subject else selectedSubjects + subject
                    },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, logoBlue),
                    color = if (isSelected) logoBlue else Color.White
                ) {
                    Text(
                        text = subject,
                        color = if (isSelected) Color.White else logoBlue,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Final Submit Button
        Button(
            onClick = {
                // Validation before network request
                if (email.isBlank() || password.isBlank() || firstName.isBlank()) {
                    Toast.makeText(context, "Fill in required fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                scope.launch {
                    val isSuccess = viewModel.register(
                        email = email.trim(),
                        pass = password.trim(),
                        firstName = firstName.trim(),
                        surname = surname.trim(),
                        role = role,
                        city = city.trim(),
                        university = university.trim(),
                        telephone = telephone.trim(),
                        bio = bio.trim(),
                        hobbies = hobbiesAndInterests.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        subjects = selectedSubjects.toList()
                    )
                    // If login/reg fails, state error is shown; if success, MainActivity re-navigates.
                    if (!isSuccess) {
                        Toast.makeText(context, authState.error ?: "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = logoBlue,
                contentColor = Color.White
            ),
            enabled = !authState.isLoading // Prevents double submission
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Create Account",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Navigate to Login Option
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Black)) {
                    append("Already registered? ")
                }
                withStyle(style = SpanStyle(color = logoBlue, fontWeight = FontWeight.Bold)) {
                    append("Login")
                }
            },
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onNavigateToLogin() }
                .padding(bottom = 75.dp) // Requested bottom padding
        )
    }
}

/**
 * Reusable text field component with specific styling for registration
 */
@Composable
fun RegistrationInputBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    color: Color,
    bg: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    kType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(hint, color = Color.Gray, fontSize = fontSize) },
            // Toggle for password dots vs plain text
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = kType),
            trailingIcon = {
                if (isPassword && onToggleVisibility != null) {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(
                        onClick = onToggleVisibility,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        Icon(icon, null, tint = color)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = bg,
                unfocusedContainerColor = bg,
                focusedBorderColor = color,
                unfocusedBorderColor = color,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true
        )
    }
}