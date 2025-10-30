package com.oic.myapplication.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oic.myapplication.ui.palette.*
import com.oic.myapplication.R
import com.oic.myapplication.services.auth.FirebaseAuthService
import com.oic.myapplication.ui.components.DotsIndicator
import com.oic.myapplication.ui.components.FilledButton
import com.oic.myapplication.ui.components.PillField


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

    val firebaseAuth = FirebaseAuthService()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            Image(
                painter = painterResource(R.drawable.hands_header),
                contentDescription = "Header image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            DotsIndicator(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                total = 3, selected = 1
            )
        }

        Surface(
            shape = CardXL, color = SurfaceWhite,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
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
                    //isPassword = true,
                    leadingIcon = Icons.Outlined.Lock
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledButton(text = "Sign Up", onClick = onSignUp, container = GoldDark, modifier = Modifier.weight(1f))
                    FilledButton(
                        text = "Login",
                        onClick = {
                            if (username.isBlank() || password.isBlank()){
                                error = "Please enter email or password"
                            } else{
                                firebaseAuth.login(username.trim(), password){success ->
                                    if (success){
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
}


