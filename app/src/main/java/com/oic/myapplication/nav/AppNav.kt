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

@Composable
fun AppNav(navController: NavHostController) {
    val start = if (Session.isLoggedIn) Routes.Main else Routes.Welcome

    NavHost(navController = navController, startDestination = start) {

        composable(Routes.Welcome) {
            WelcomeScreen(onSwipeUp = { navController.navigate(Routes.Login) })
        }

        composable(Routes.Login) {
            LoginScreen(
                validateCredentials = { email, pass -> email.contains("@") && pass.isNotBlank() },
                onLoginSuccess = {
                    Session.login()
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSignUp = { navController.navigate(Routes.SignUp) },
                onForgot = { navController.navigate(Routes.Reset) }
            )
        }

        composable(Routes.SignUp) {
            // You keep your current UI; on submit go to Main
            SignUpScreen(
                onSubmit = {
                    Session.login()
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoLogin = { navController.popBackStack(); navController.navigate(Routes.Login) }
            )
        }

        composable(Routes.Reset) {
            ResetScreen(onDone = { navController.popBackStack() })
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
                openReset = { navController.navigate(Routes.Reset) } // for Account -> Change password
            )
        }
    }
}

