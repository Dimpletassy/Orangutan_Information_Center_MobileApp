package com.oic.myapplication.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oic.myapplication.R
import com.oic.myapplication.ui.components.DotsIndicator
import com.oic.myapplication.ui.components.FilledButton
import com.oic.myapplication.ui.components.PillField
import com.oic.myapplication.ui.palette.*

@Composable
fun LoginScreen(
    validateCredentials: (String, String) -> Boolean,
    onLoginSuccess: () -> Unit,
    onSignUp: () -> Unit,
    onForgot: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var staySignedIn by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Make the header image ~48% of screen height (nice balance on most phones)
    val screenH = LocalConfiguration.current.screenHeightDp
    val headerH = (screenH * 0.48f).dp

    Column(modifier = Modifier.fillMaxSize()) {

        // BIGGER header image
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

        // Pull the card up over the image so there's less white
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = SurfaceWhite,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)   // <- overlap
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
                    leadingIcon = Icons.Outlined.Lock
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
                    TextButton(onClick = onForgot) { Text("Forgot Password?", color = Cocoa) }
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
                                error = "Please enter email or password"
                            } else {
                                // your firebaseLogin() call from before
                                com.oic.myapplication.services.auth.firebaseLogin(
                                    username.trim(),
                                    password
                                ) { success ->
                                    if (success) onLoginSuccess() else {
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
}
