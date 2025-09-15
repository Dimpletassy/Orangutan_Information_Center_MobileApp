package com.example.myapplication.ui.screens.welcome


import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.palette.CocoaDeep
import com.example.myapplication.ui.palette.GoldDark
import com.example.myapplication.ui.palette.GoldLight

@Composable
fun WelcomeScreen(onSwipeUp: () -> Unit) {
    val gradient = Brush.verticalGradient(listOf(GoldDark, GoldLight))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.oic),
                contentDescription = "OIC logo",
                modifier = Modifier.size(220.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(24.dp))
            Text("Automated", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = CocoaDeep)
            Text("Plant-Watering", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = CocoaDeep)
            Text("System", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = CocoaDeep)
        }

        SwipeUpHint(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            onSwipeUp = onSwipeUp
        )
    }
}

@Composable
private fun SwipeUpHint(modifier: Modifier = Modifier, onSwipeUp: () -> Unit) {
    var dragY by remember { mutableStateOf(0f) }
    val threshold = 140f
    val infinite = rememberInfiniteTransition(label = "bounce")
    val offset = infinite.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "bounceAnim"
    )
    Column(
        modifier = modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onVerticalDrag = { _, dy -> dragY += dy },
                onDragEnd = {
                    if (dragY < -threshold) onSwipeUp()
                    dragY = 0f
                },
                onDragCancel = { dragY = 0f }
            )
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.ExpandLess, contentDescription = null, modifier = Modifier.offset(y = offset.value.dp))
        Icon(Icons.Rounded.ExpandLess, contentDescription = null, modifier = Modifier.offset(y = (offset.value - 6).dp))
        Spacer(Modifier.height(2.dp))
        Text("swipe up to login", style = MaterialTheme.typography.bodyMedium, color = CocoaDeep)
    }
}
