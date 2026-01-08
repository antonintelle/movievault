package com.example.movievault.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movievault.ui.about.AboutScreen
import com.example.movievault.ui.add.AddMovieScreen
import com.example.movievault.ui.auth.LoginScreen
import com.example.movievault.ui.auth.RegisterScreen
import com.example.movievault.ui.main.MainScreen
import com.example.movievault.ui.profile.ProfileScreen
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    val start = if (FirebaseAuth.getInstance().currentUser != null) {
        Routes.Main.route
    } else {
        Routes.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Login.route
    ) {

        composable(Routes.Login.route) {
            LoginScreen(
                onGoRegister = { navController.navigate(Routes.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Main.route) {
            MainScreen(
                onAddMovie = { navController.navigate(Routes.AddMovie.route) },
                onAbout = { navController.navigate(Routes.About.route) },
                onProfile = { navController.navigate(Routes.Profile.route) }
            )
        }

        composable(Routes.AddMovie.route) {
            AddMovieScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
