@file:OptIn(ExperimentalMaterial3Api::class)

package com.oic.myapplication.ui.sites

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.oic.myapplication.R

private val Cream = Color(0xFFFEF7EC) // light cream background

@Composable
fun SiteDetailsScreen(
    onOpenBukitmas: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.oic),
                            contentDescription = "OIC",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                        )
                        Text("Site Details", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Cream)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SITE 1: Bukitmas (active) ---
            SiteCard(
                title = "Bukitmas Permaculture Centre",
                subtitle = "Site 1",
                imageIds = listOf(
                    R.drawable.bukitmas_1,
                    R.drawable.bukitmas_2,
                    R.drawable.bukitmas_3
                ),
                onOpen = onOpenBukitmas
            )

            // --- TEMPLATE (future site) ---
            TemplateCard(
                title = "Next OIC Site",
                subtitle = "Coming soon"
            )
        }
    }
}

@Composable
private fun SiteCard(
    title: String,
    subtitle: String,
    imageIds: List<Int>,
    onOpen: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F4FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // “bubble”
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFEDE7FF))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6650A4)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            AutoImageSwitcher(
                imageIds = imageIds,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(Modifier.height(12.dp))

            FilledTonalButton(onClick = onOpen) {
                Text("Open Scheduling")
            }
        }
    }
}

@Composable
private fun TemplateCard(
    title: String,
    subtitle: String
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F2E9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE0E0E0))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6E6E6E)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6E6E6E)
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFEAEAEA)),
                contentAlignment = Alignment.Center
            ) {
                Text("Placeholder for future site", color = Color(0xFF9E9E9E))
            }
            Spacer(Modifier.height(8.dp))
            Text("Not yet available", color = Color(0xFF9E9E9E), fontSize = 12.sp)
        }
    }
}

@Composable
private fun AutoImageSwitcher(
    imageIds: List<Int>,
    modifier: Modifier = Modifier,
    intervalMillis: Long = 3_000
) {
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(imageIds) {
        while (imageIds.isNotEmpty()) {
            delay(intervalMillis)
            index = (index + 1) % imageIds.size
        }
    }

    Crossfade(targetState = index, label = "site-image-fade") { i ->
        Image(
            painter = painterResource(id = imageIds[i]),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}
