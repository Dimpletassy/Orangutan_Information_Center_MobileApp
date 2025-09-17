package com.oic.myapplication.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oic.myapplication.services.irrigationControl.IrrigationControl
import com.oic.myapplication.ui.components.FilledButton
import com.oic.myapplication.ui.palette.Cocoa
import com.oic.myapplication.ui.palette.CocoaDeep

@Composable
fun HomeScreen(onBackToLogin: () -> Unit) {
    val context = LocalContext.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Home screen goes here", color = CocoaDeep, fontSize = 20.sp)
            Spacer(Modifier.height(12.dp))
            FilledButton("Back to Login", onClick = onBackToLogin, container = Cocoa)
            FilledButton("testAPI", onClick = {
                val irrigation = IrrigationControl(context)
                irrigation.startAllZones()
            }, container = Cocoa)
        }
    }
}