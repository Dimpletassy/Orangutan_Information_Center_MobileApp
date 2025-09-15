package com.example.myapplication.ui.screens.home

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.FilledButton
import com.example.myapplication.ui.palette.Cocoa
import com.example.myapplication.ui.palette.CocoaDeep

@Composable
fun HomeScreen(onBackToLogin: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Home screen goes here", color = CocoaDeep, fontSize = 20.sp)
            Spacer(Modifier.height(12.dp))
            FilledButton("Back to Login", onClick = onBackToLogin, container = Cocoa)
        }
    }
}
