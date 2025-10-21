package com.example.myapplicationv.navigation

sealed class Route(val path: String) {
    object Home : Route("home")
    object Login : Route("login")
    object Register : Route("register")
    object Mascotas : Route("mascotas")
    object Citas : Route("citas")
}