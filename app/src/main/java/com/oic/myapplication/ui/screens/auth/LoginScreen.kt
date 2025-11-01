package com.oic.myapplication.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.oic.myapplication.R
import com.oic.myapplication.services.auth.FirebaseAuthService
import com.oic.myapplication.ui.components.DotsIndicator
import com.oic.myapplication.ui.components.FilledButton
import com.oic.myapplication.ui.components.PillField
import com.oic.myapplication.ui.palette.*

@Composable
fun LoginScreen(
    validateCredentials: (String, String) -> Boolean,
    onLoginSuccess: () -> Unit,
    onSignUp: () -> Unit,
    onForgot: () -> Unit // kept for navigation if you want a separate forgot page
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var staySignedIn by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetMessage by remember { mutableStateOf("") }
    var isResetLoading by remember { mutableStateOf(false) }

    val firebaseAuth = FirebaseAuthService()
    val auth = FirebaseAuth.getInstance()

    val screenH = LocalConfiguration.current.screenHeightDp
    val headerH = (screenH * 0.48f).dp

    Column(modifier = Modifier.fillMaxSize()) {

        // Header image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerH)
        ) {
            Image(
                painter = painterResource(R.drawable.hands_header),
                contentDescription = "Header image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            DotsIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                total = 3, selected = 1
            )
        }

        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = SurfaceWhite,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CocoaDeep)
                Spacer(Modifier.height(14.dp))

                PillField(
                    label = "Email",
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "Enter email address",
                    leadingIcon = Icons.Outlined.Person
                )
                Spacer(Modifier.height(10.dp))
                PillField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Enter password",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = staySignedIn, onCheckedChange = { staySignedIn = it })
                        Text("Stay Signed in", color = Cocoa)
                    }
                    TextButton(onClick = { showForgotDialog = true }) { // ← open reset dialog
                        Text("Forgot Password?", color = Cocoa)
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(error!!, color = Color.Red)
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledButton(
                        text = "Sign Up",
                        onClick = onSignUp,
                        container = GoldDark,
                        modifier = Modifier.weight(1f)
                    )
                    FilledButton(
                        text = "Login",
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                error = "Please enter email and password"
                            } else {
                                firebaseAuth.login(username.trim(), password) { success ->
                                    if (success) {
                                        onLoginSuccess()
                                    } else {
                                        error = "Invalid email or password"
                                    }
                                }
                            }
                        },
                        container = Cocoa,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // ---------- Forgot Password Dialog ----------
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (resetEmail.isBlank()) {
                        resetMessage = "Please enter your email."
                    } else {
                        isResetLoading = true
                        resetMessage = ""
                        auth.sendPasswordResetEmail(resetEmail.trim())
                            .addOnCompleteListener { task ->
                                isResetLoading = false
                                resetMessage = if (task.isSuccessful) {
                                    "✅ Password reset email sent to $resetEmail"
                                } else {
                                    "❌ Failed to send reset email: ${task.exception?.localizedMessage}"
                                }
                            }
                    }
                }) {
                    Text("Send", color = GoldDark)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    resetEmail = ""
                    resetMessage = ""
                }) {
                    Text("Cancel", color = Cocoa)
                }
            },
            title = { Text("Forgot Password", fontWeight = FontWeight.Bold, color = CocoaDeep) },
            text = {
                Column {
                    Text("Enter your registered email to receive a password reset link:")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        placeholder = { Text("Email address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isResetLoading) {
                        Spacer(Modifier.height(10.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = GoldDark,
                            strokeWidth = 2.dp
                        )
                    }
                    if (resetMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(resetMessage, color = Cocoa, fontSize = 14.sp)
                    }
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}