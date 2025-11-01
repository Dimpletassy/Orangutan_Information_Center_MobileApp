package com.oic.myapplication.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.oic.myapplication.ui.components.HeaderWithImage
import com.oic.myapplication.ui.components.PillField
import com.oic.myapplication.ui.palette.*

@Composable
fun AccountScreen(
    onLogout: () -> Unit,
    onChangePassword: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    // Split Firebase displayName into first and last parts if possible
    val displayParts = user?.displayName?.split(" ") ?: listOf("", "")
    var firstName by remember { mutableStateOf(displayParts.getOrNull(0) ?: "") }
    var lastName by remember { mutableStateOf(displayParts.getOrNull(1) ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var message by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        HeaderWithImage(selectedDot = 4, titleOverride = "Account")

        Surface(
            shape = CardXL,
            color = SurfaceWhite,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "My Account",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CocoaDeep
                )
                Spacer(Modifier.height(16.dp))

                // First Name
                PillField(
                    label = "First Name",
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = "Enter First Name",
                    leadingIcon = Icons.Outlined.Person
                )
                Spacer(Modifier.height(10.dp))

                // Last Name
                PillField(
                    label = "Last Name",
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = "Enter Last Name",
                    leadingIcon = Icons.Outlined.Person
                )
                Spacer(Modifier.height(10.dp))

                // Email (readonly)
                PillField(
                    label = "Email",
                    value = email,
                    onValueChange = {},
                    placeholder = "Email",
                    leadingIcon = Icons.Outlined.Email
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        val fullName = "$firstName $lastName".trim()
                        val profileUpdate = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()

                        isSaving = true
                        user?.updateProfile(profileUpdate)
                            ?.addOnCompleteListener { task ->
                                isSaving = false
                                if (task.isSuccessful) {
                                    user.reload() // refresh Firebase user data
                                    message = "✅ Profile updated successfully!"
                                } else {
                                    message = "❌ Failed to update profile."
                                }
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = SurfaceWhite,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Save Changes")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { onChangePassword() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Password")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        auth.signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cocoa),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }

                Spacer(Modifier.height(16.dp))
                if (message.isNotEmpty()) Text(text = message, color = Cocoa)
            }
        }
    }
}