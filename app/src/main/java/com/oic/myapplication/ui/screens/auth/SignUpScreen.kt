package com.oic.myapplication.ui.screens.auth

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oic.myapplication.services.auth.firebaseSignup
import com.oic.myapplication.services.auth.validateSignUpInput
import com.oic.myapplication.ui.components.FilledButton
import com.oic.myapplication.ui.components.HeaderWithImage
import com.oic.myapplication.ui.components.PillField
import com.oic.myapplication.ui.palette.CardXL
import com.oic.myapplication.ui.palette.Cocoa
import com.oic.myapplication.ui.palette.CocoaDeep
import com.oic.myapplication.ui.palette.GoldDark
import com.oic.myapplication.ui.palette.SurfaceWhite

@Composable
fun SignUpScreen(onSubmit: () -> Unit, onGoLogin: () -> Unit) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var stay by remember { mutableStateOf(false) }

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
                Text("Sign up", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = CocoaDeep)
                Spacer(Modifier.height(12.dp))

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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledButton("Login", onClick = onGoLogin, container = Cocoa, modifier = Modifier.weight(1f))
                    FilledButton("Sign Up", onClick = {
                        val error = validateSignUpInput(first, last, contact, pass)
                        if (error != null){
                            //TODO: add ui for error msgs
                            Log.d(TAG, error)
                        }else{
                            //TODO: add to firebase users
                            firebaseSignup(first, last, contact, pass){ success ->
                                if (success == true){
                                    onGoLogin
                                }
                            }
                        }

                    }, container = GoldDark, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}