package com.oic.myapplication.ui.screens.main


import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.oic.myapplication.R
import com.oic.myapplication.nav.Routes
import com.oic.myapplication.ui.components.*
import com.oic.myapplication.ui.palette.*
import com.oic.myapplication.ui.screens.account.AccountScreen
import com.oic.myapplication.ui.screens.notifications.NotificationsScreen
import com.oic.myapplication.ui.screens.reporting.ReportingScreen
import com.oic.myapplication.ui.screens.scheduling.SchedulingScreen

@Composable
fun MainShell(
    innerNav: NavHostController,
    onLogout: () -> Unit,
    openReset: () -> Unit
) {
    val tabs = listOf(
        Triple(Routes.Home,      Icons.Outlined.Home,          "Home"),
        Triple(Routes.Reporting, Icons.Outlined.CalendarMonth, "Reporting"),
        Triple(Routes.Account,   Icons.Outlined.AccountCircle, "Account"),
    )

    Scaffold(
        // light background behind screens; your Scheduling page draws its own image/header
        containerColor = SurfaceWhite,
        bottomBar = {
            NavigationBar(
                containerColor = Latte.copy(alpha = 0.95f),  // lighter bar per your request
                contentColor   = CocoaDeep
            ) {
                val backStack by innerNav.currentBackStackEntryAsState()
                val current = backStack?.destination?.route
                tabs.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        selected = current == route,
                        onClick = {
                            if (current != route) {
                                innerNav.navigate(route) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = CocoaDeep,
                            selectedTextColor   = CocoaDeep,
                            unselectedIconColor = Cocoa,
                            unselectedTextColor = Cocoa,
                            indicatorColor      = GoldDark.copy(alpha = .35f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // IMPORTANT: pass the Scaffold's padding into the NavHost so content sits above the bar
        NavHost(
            navController = innerNav,
            startDestination = Routes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Home) {
                SchedulingScreen(
                    onOpenReporting     = { innerNav.navigate(Routes.Reporting) },
                    onOpenAccount       = { innerNav.navigate(Routes.Account) },
                    onOpenNotifications = { innerNav.navigate(Routes.Notifications) }
                )
            }
            composable(Routes.Reporting) { ReportingScreen() }
            composable(Routes.Account)   { AccountScreen(onLogout = onLogout, onChangePassword = openReset) }
            composable(Routes.Notifications) { NotificationsScreen() }
        }
    }
}
