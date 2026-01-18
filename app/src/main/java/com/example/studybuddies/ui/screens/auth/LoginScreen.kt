package com.example.studybuddies.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studybuddies.R
import com.example.studybuddies.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel, // Accessing auth logic (login, reset, state)
    onNavigateToRegister: () -> Unit, // Callback to switch to the registration screen
    onLoginSuccess: () -> Unit // Callback to move into the main app after successful login
) {
    // Collect the UI state (loading, error, auth status) from the ViewModel
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // UI Theme Colors
    val logoBlue = Color(0xFF1A73E8)
    val bgInput = Color(0xFFF3F6FC)

    // Utilities for context, coroutines, and snackbars
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // local UI state for inputs (not saved in ViewModel yet)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Automatic transition: if auth state changes to true, trigger success callback
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    // Listens for the password reset flag to show a feedback message
    LaunchedEffect(authState.isPasswordResetSent) {
        if (authState.isPasswordResetSent) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "A password reset link has been sent to your email address.",
                    duration = SnackbarDuration.Short
                )
                authViewModel.clearResetState() // Resets the flag so the message doesn't repeat
            }
        }
    }

    Scaffold(
        containerColor = Color.White // Set background to clean white
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Content Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState()) // Ensures small screens can see the whole form
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // App Branding: Logo and Text
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Study Buddies",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = logoBlue
                )

                Text(
                    text = "Students helping students",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
                )

                // Reusable input fields for credentials
                LoginField("Email", email, { email = it }, "Enter your email", logoBlue, bgInput)

                Spacer(modifier = Modifier.height(16.dp))

                LoginField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    hint = "Enter your password",
                    color = logoBlue,
                    bg = bgInput,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )

                // Forgot Password link: triggers reset email if email field isn't empty
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        text = "Forgot Password?",
                        color = logoBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // Removes the gray box effect on click
                            ) {
                                if (email.isNotBlank()) {
                                    authViewModel.resetPassword(email.trim())
                                } else {
                                    Toast.makeText(context, "Please enter your email address first", Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Main Action Button: Logic changes based on loading state
                Button(
                    onClick = { authViewModel.loginUser(email.trim(), password.trim()) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = logoBlue,
                        disabledContainerColor = logoBlue.copy(alpha = 0.6f)
                    ),
                    enabled = !authState.isLoading // Prevent multiple clicks while logging in
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator( // Show spinner during network request
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            text = "Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                // Error reporting for invalid credentials or connection issues
                if (authState.error != null) {
                    Text(
                        text = authState.error ?: "Login failed. Check your data.",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer: Navigate to Register screen using an AnnotatedString for mixed styling
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Black)) {
                            append("Don't have an account? ")
                        }
                        withStyle(style = SpanStyle(color = logoBlue, fontWeight = FontWeight.Bold)) {
                            append("Register")
                        }
                    },
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigateToRegister() }
                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Snackbar Layer: Custom notification for password reset success
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(BiasAlignment(0f, -0.2f)) // Positioned slightly above the middle
                    .padding(horizontal = 24.dp)
            ) { data ->
                Surface(
                    color = logoBlue,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = data.visuals.message,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    color: Color,
    bg: Color,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(hint, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            // Toggles between dots and visible text for passwords
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                // Eye icon to show/hide password
                if (isPassword && onToggleVisibility != null) {
                    IconButton(
                        onClick = onToggleVisibility,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = color
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color,
                unfocusedBorderColor = color,
                focusedContainerColor = bg,
                unfocusedContainerColor = bg,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true
        )
    }
}