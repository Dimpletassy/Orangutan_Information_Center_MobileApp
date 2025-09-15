package com.example.myapplication.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.components.DotsIndicator
import com.example.myapplication.ui.components.FilledButton
import com.example.myapplication.ui.components.PillField
import com.example.myapplication.ui.palette.*

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onSignUp: () -> Unit,
    onForgot: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var staySignedIn by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header image
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

        // Card-like surface
        Surface(
            shape = CardXL,
            color = SurfaceWhite,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CocoaDeep)
                Spacer(Modifier.height(14.dp))

                PillField(
                    label = "Username",
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "Enter Phone Number / Email",
                    leadingIcon = Icons.Outlined.Person
                )
                Spacer(Modifier.height(10.dp))
                PillField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Enter Password",
                    isPassword = true,
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
                    TextButton(onClick = onForgot) {
                        Text("Forgot Password?", color = Cocoa)
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(error!!, color = androidx.compose.ui.graphics.Color.Red)
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledButton(text = "Sign Up", onClick = onSignUp, container = GoldDark, modifier = Modifier.weight(1f))
                    FilledButton(text = "Login", onClick = {
                        if (username.isBlank() || password.isBlank()) error = "Please enter username and password"
                        else onLogin(username, password)
                    }, container = Cocoa, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


