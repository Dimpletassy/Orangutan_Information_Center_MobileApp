package com.oic.myapplication.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.myapplication.session.Session
import com.oic.myapplication.ui.screens.auth.LoginScreen
import com.oic.myapplication.ui.screens.auth.ResetScreen
import com.oic.myapplication.ui.screens.auth.SignUpScreen
import com.oic.myapplication.ui.screens.main.MainShell
import com.oic.myapplication.ui.screens.welcome.WelcomeScreen
import com.oic.myapplication.ui.sites.SiteDetailsScreen

@Composable
fun AppNav(navController: NavHostController) {
    // If already logged in on app launch, go straight to Main.
    // Fresh auth goes Login/SignUp -> SiteDetails -> Main.
    val start = if (Session.isLoggedIn) Routes.Main else Routes.Welcome

    NavHost(navController = navController, startDestination = start) {

        composable(Routes.Welcome) {
            WelcomeScreen(onSwipeUp = { navController.navigate(Routes.Login) })
        }

        composable(Routes.Login) {
            LoginScreen(
                validateCredentials = { email, pass -> email.contains("@") && pass.isNotBlank() },
                onLoginSuccess = {
                    // NOTE: do NOT call Session.login() here.
                    // Navigate to Site Details first.
                    navController.navigate(Routes.SiteDetails) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSignUp = { navController.navigate(Routes.SignUp) },
                onForgot = { navController.navigate(Routes.Reset) }
            )
        }

        composable(Routes.SignUp) {
            SignUpScreen(
                onSubmit = {
                    // Same idea: go to Site Details first.
                    navController.navigate(Routes.SiteDetails) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoLogin = {
                    navController.popBackStack()
                    navController.navigate(Routes.Login)
                }
            )
        }

        composable(Routes.Reset) {
            ResetScreen(onDone = { navController.popBackStack() })
        }

        // NEW: Site Details between auth and Main.
        composable(Routes.SiteDetails) {
            SiteDetailsScreen(
                onOpenBukitmas = {
                    // Mark session as logged-in *here* so any listeners
                    // won’t bypass SiteDetails prematurely.
                    Session.login()
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.SiteDetails) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Bottom-nav shell with inner NavHost for tabs
        composable(Routes.Main) {
            val inner = rememberNavController()
            MainShell(
                innerNav = inner,
                onLogout = {
                    Session.logout()
                    navController.navigate(Routes.Welcome) {
                        popUpTo(Routes.Main) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                openReset = { navController.navigate(Routes.Reset) },
                openSiteDetails = {  // 🆕 this fixes the error
                    navController.navigate(Routes.SiteDetails) {
                        popUpTo(Routes.Main) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }}