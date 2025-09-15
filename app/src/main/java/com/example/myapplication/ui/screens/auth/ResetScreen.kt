package com.example.myapplication.ui.screens.auth

import androidx.compose.ui.unit.dp
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.components.FilledButton
import com.example.myapplication.ui.components.HeaderWithImage
import com.example.myapplication.ui.components.PillField
import com.example.myapplication.ui.palette.*

@Composable
fun ResetScreen(onDone: () -> Unit) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var stay by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        HeaderWithImage(selectedDot = 3, titleOverride = "Re-set Password")
        Surface(
            shape = CardXL, color = SurfaceWhite,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Column(
                Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PillField("First Name/s", first, { first = it }, placeholder = "Enter First Name/s", leadingIcon = Icons.Outlined.Person)
                Spacer(Modifier.height(10.dp))
                PillField("Surname", last, { last = it }, placeholder = "Enter Surname", leadingIcon = Icons.Outlined.Person)
                Spacer(Modifier.height(10.dp))
                PillField("Phone Number / Email", contact, { contact = it }, placeholder = "Enter Phone Number / Email", leadingIcon = Icons.Outlined.Email)
                Spacer(Modifier.height(10.dp))
                PillField("Password", pass, { pass = it }, isPassword = true, placeholder = "Enter Password", leadingIcon = Icons.Outlined.Lock)
                Spacer(Modifier.height(10.dp))
                PillField("Re-Enter Password", confirm, { confirm = it }, isPassword = true, placeholder = "Enter Password", leadingIcon = Icons.Outlined.Lock)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    Checkbox(checked = stay, onCheckedChange = { stay = it })
                    Text("Stay signed in", color = Cocoa)
                }

                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                    FilledButton("Sign Up", onClick = onDone, container = GoldDark, modifier = Modifier.weight(1f))
                    FilledButton("Login", onClick = onDone, container = Cocoa, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
