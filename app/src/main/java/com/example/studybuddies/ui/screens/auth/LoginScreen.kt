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
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val logoBlue = Color(0xFF1A73E8)
    val bgInput = Color(0xFFF3F6FC)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Automatic login handling
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    // PROFESSIONAL PASSWORD RESET HANDLING
    LaunchedEffect(authState.isPasswordResetSent) {
        if (authState.isPasswordResetSent) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "A password reset link has been sent to your email address.",
                    duration = SnackbarDuration.Short
                )
                authViewModel.clearResetState()
            }
        }
    }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        // Use Box to position overlay elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // CONTENT LAYER (Form)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

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
                                indication = null
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

                Button(
                    onClick = { authViewModel.loginUser(email.trim(), password.trim()) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = logoBlue,
                        disabledContainerColor = logoBlue.copy(alpha = 0.6f)
                    ),
                    enabled = !authState.isLoading
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(
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

            // NOTIFICATION LAYER (SNACKBAR)
            // Using BiasAlignment(0f, -0.2f) to position it "slightly above" the center
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(BiasAlignment(0f, -0.2f))
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
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
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