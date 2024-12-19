package com.example.antitheft.ui

sealed class NavScreens (val screen:String){

    data object HomePage: NavScreens("home")
    data object Profile: NavScreens("profile")
    data object DataBackup: NavScreens("data backup")
    data object AppSetup: NavScreens("app setup")
    data object Help: NavScreens("help")
    data object Settings: NavScreens("settings")
}