package com.oic.myapplication.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.oic.myapplication.ui.palette.* // Latte, Cocoa, CocoaDeep, GoldLight

@Composable
fun BottomNavBar(
    navController: NavHostController,
    items: List<BottomItem>,
    containerColor: Color = Latte,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    NavigationBar(containerColor = containerColor, contentColor = CocoaDeep) {
        items.forEach { item ->
            val selected = destination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CocoaDeep,
                    selectedTextColor = CocoaDeep,
                    indicatorColor = GoldLight.copy(alpha = .6f),
                    unselectedIconColor = Cocoa,
                    unselectedTextColor = Cocoa
                )
            )
        }
    }
}

data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
