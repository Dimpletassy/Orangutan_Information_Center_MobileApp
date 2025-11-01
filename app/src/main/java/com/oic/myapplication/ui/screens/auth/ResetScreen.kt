package com.oic.myapplication.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.oic.myapplication.ui.components.FilledButton
import com.oic.myapplication.ui.components.HeaderWithImage
import com.oic.myapplication.ui.components.PillField
import com.oic.myapplication.ui.palette.*

@Composable
fun ResetScreen(onDone: () -> Unit) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Column(Modifier.fillMaxSize()) {
        HeaderWithImage(selectedDot = 3, titleOverride = "Reset Password")

        Surface(
            shape = CardXL,
            color = SurfaceWhite,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Column(
                Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PillField(
                    label = "Old Password",
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    isPassword = true,
                    placeholder = "Enter old password",
                    leadingIcon = Icons.Outlined.Lock
                )
                Spacer(Modifier.height(10.dp))

                PillField(
                    label = "New Password",
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    isPassword = true,
                    placeholder = "Enter new password",
                    leadingIcon = Icons.Outlined.Lock
                )
                Spacer(Modifier.height(10.dp))

                PillField(
                    label = "Re-type New Password",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    isPassword = true,
                    placeholder = "Re-enter new password",
                    leadingIcon = Icons.Outlined.Lock
                )

                Spacer(Modifier.height(20.dp))

                // Show progress indicator while updating
                if (isLoading) {
                    CircularProgressIndicator(color = GoldDark)
                    Spacer(Modifier.height(20.dp))
                }

                FilledButton(
                    text = "Update Password",
                    onClick = {
                        if (user == null) {
                            message = "No user logged in."
                            return@FilledButton
                        }
                        if (newPassword != confirmPassword) {
                            message = "New passwords do not match."
                            return@FilledButton
                        }

                        val email = user.email
                        if (email.isNullOrEmpty()) {
                            message = "User email not found."
                            return@FilledButton
                        }

                        isLoading = true
                        message = ""

                        val credential = EmailAuthProvider.getCredential(email, oldPassword)
                        user.reauthenticate(credential)
                            .addOnCompleteListener { reauth ->
                                if (reauth.isSuccessful) {
                                    user.updatePassword(newPassword)
                                        .addOnCompleteListener { update ->
                                            isLoading = false
                                            message = if (update.isSuccessful) {
                                                "Password updated successfully."
                                            } else {
                                                "Error: ${update.exception?.message}"
                                            }
                                        }
                                } else {
                                    isLoading = false
                                    message = "Old password incorrect."
                                }
                            }
                    },
                    container = GoldDark,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                if (message.isNotEmpty()) {
                    Text(text = message, color = Cocoa)
                }

                Spacer(Modifier.height(10.dp))

                FilledButton(
                    text = "Back",
                    onClick = onDone,
                    container = Cocoa,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
