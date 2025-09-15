package com.example.myapplication.ui.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.palette.CocoaDeep
import com.example.myapplication.ui.palette.SurfaceWhite

@Composable
fun HeaderWithImage(selectedDot: Int, titleOverride: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.hands_header),
            contentDescription = "Header",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DotsIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp),
                total = 3, selected = selectedDot
            )
            titleOverride?.let {
                Surface(color = SurfaceWhite.copy(alpha = 0.92f), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        it,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CocoaDeep
                    )
                }
            }
        }
    }
}
