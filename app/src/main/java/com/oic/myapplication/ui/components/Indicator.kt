package com.oic.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oic.myapplication.ui.palette.*


@Composable
fun DotsIndicator(modifier: Modifier = Modifier, total: Int, selected: Int) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        repeat(total) { i ->
            val isSelected = (i + 1) == selected
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (isSelected) 20.dp else 10.dp, height = 10.dp)
                    .background(
                        color = if (isSelected) LeafGreen else Color(0xFFE6E6E6),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}