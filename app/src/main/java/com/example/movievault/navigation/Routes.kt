package com.example.movievault.navigation

sealed class Routes(val route: String) {
    data object Login : Routes("login")
    data object Register : Routes("register")
    data object Main : Routes("main")
    data object AddMovie : Routes("add_movie")
    data object Profile : Routes("profile")
    data object About : Routes("about")
    data object Search : Routes("search")

}
