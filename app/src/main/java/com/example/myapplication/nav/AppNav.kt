package com.example.myapplication.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.screens.auth.LoginScreen
import com.example.myapplication.ui.screens.auth.ResetScreen
import com.example.myapplication.ui.screens.auth.SignUpScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.welcome.WelcomeScreen

@Composable
fun AppNav(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.Welcome) {
        composable(Routes.Welcome) {
            WelcomeScreen(onSwipeUp = { navController.navigate(Routes.Login) })
        }
        composable(Routes.Login) {
            LoginScreen(
                onLogin = { u, _ -> if (u.isNotBlank()) navController.navigate(Routes.Home) },
                onSignUp = { navController.navigate(Routes.SignUp) },
                onForgot = { navController.navigate(Routes.Reset) }
            )
        }
        composable(Routes.SignUp) {
            SignUpScreen(
                onSubmit = { navController.navigate(Routes.Home) },
                onGoLogin = { navController.popBackStack(); navController.navigate(Routes.Login) }
            )
        }
        composable(Routes.Reset) {
            ResetScreen(onDone = { navController.popBackStack(Routes.Login, inclusive = false) })
        }
        composable(Routes.Home) {
            HomeScreen(onBackToLogin = { navController.popBackStack(Routes.Login, false) })
        }
    }
}
