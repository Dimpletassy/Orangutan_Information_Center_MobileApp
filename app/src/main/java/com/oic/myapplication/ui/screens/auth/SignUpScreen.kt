package com.oic.myapplication.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.oic.myapplication.ui.components.*
import com.oic.myapplication.ui.palette.*

@Composable
fun SignUpScreen(onSubmit: () -> Unit, onGoLogin: () -> Unit) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var stay by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    Column(Modifier.fillMaxSize()) {
        HeaderWithImage(selectedDot = 2)
        Surface(
            shape = CardXL,
            color = SurfaceWhite,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Sign up",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CocoaDeep
                )
                Spacer(Modifier.height(12.dp))

                // --- Input fields ---
                PillField(
                    "First Name",
                    first,
                    { first = it },
                    placeholder = "Enter First Name",
                    leadingIcon = Icons.Outlined.Person
                )
                Spacer(Modifier.height(10.dp))

                PillField(
                    "Last Name",
                    last,
                    { last = it },
                    placeholder = "Enter Last Name",
                    leadingIcon = Icons.Outlined.Person
                )
                Spacer(Modifier.height(10.dp))

                PillField(
                    "Email",
                    contact,
                    { contact = it },
                    placeholder = "Enter Email Address",
                    leadingIcon = Icons.Outlined.Email
                )
                Spacer(Modifier.height(10.dp))

                PillField(
                    "Password",
                    password,
                    { password = it },
                    isPassword = true,
                    placeholder = "Enter Password",
                    leadingIcon = Icons.Outlined.Lock
                )
                Spacer(Modifier.height(10.dp))

                PillField(
                    "Re-Enter Password",
                    confirm,
                    { confirm = it },
                    isPassword = true,
                    placeholder = "Re-enter Password",
                    leadingIcon = Icons.Outlined.Lock
                )

                // Stay signed in
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Checkbox(checked = stay, onCheckedChange = { stay = it })
                    Text("Stay signed in", color = Cocoa)
                }

                Spacer(Modifier.height(14.dp))

                // Loading spinner
                if (isLoading) {
                    CircularProgressIndicator(color = GoldDark)
                    Spacer(Modifier.height(12.dp))
                }

                // --- Action buttons ---
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledButton(
                        "Sign Up",
                        onClick = {
                            if (contact.isBlank() || password.isBlank() || confirm.isBlank() || first.isBlank() || last.isBlank()) {
                                message = "Please fill in all fields."
                                return@FilledButton
                            }
                            if (password != confirm) {
                                message = "Passwords do not match."
                                return@FilledButton
                            }

                            isLoading = true
                            message = ""

                            // Create user in Firebase
                            auth.createUserWithEmailAndPassword(contact, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        val fullName = "$first $last".trim()

                                        // Update Firebase display name (used on Home + Account)
                                        val profileUpdates = UserProfileChangeRequest.Builder()
                                            .setDisplayName(fullName)
                                            .build()

                                        user?.updateProfile(profileUpdates)
                                            ?.addOnCompleteListener {
                                                user.reload() // refresh immediately
                                                Log.d("SignUpScreen", "Display name updated: $fullName")
                                            }

                                        // Send verification email
                                        user?.sendEmailVerification()
                                            ?.addOnCompleteListener { emailTask ->
                                                if (emailTask.isSuccessful) {
                                                    message =
                                                        "✅ Account created! Verification email sent to $contact."
                                                    Log.d("SignUpScreen", "Verification email sent.")
                                                } else {
                                                    message =
                                                        "⚠️ Account created, but failed to send verification email."
                                                    Log.w(
                                                        "SignUpScreen",
                                                        "Email send failed",
                                                        emailTask.exception
                                                    )
                                                }
                                            }

                                        // Handle stay signed in
                                        if (!stay) {
                                            auth.signOut()
                                            Log.d("SignUpScreen", "User signed out after signup.")
                                        } else {
                                            Log.d("SignUpScreen", "User chose to stay signed in.")
                                        }

                                        onSubmit()
                                    } else {
                                        message =
                                            "❌ Signup failed: ${task.exception?.localizedMessage ?: "Unknown error"}"
                                        Log.w("SignUpScreen", "Signup failed", task.exception)
                                    }
                                }
                        },
                        container = GoldDark,
                        modifier = Modifier.weight(1f)
                    )

                    FilledButton(
                        "Login",
                        onClick = onGoLogin,
                        container = Cocoa,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))
                if (message.isNotEmpty()) Text(text = message, color = Cocoa)
            }
        }
    }
}