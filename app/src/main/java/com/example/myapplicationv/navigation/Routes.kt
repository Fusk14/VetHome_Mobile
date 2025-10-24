package com.example.myapplicationv.navigation

// En navigation/Route.kt
sealed class Route(val path: String) {
    object Home : Route("home")
    object Login : Route("login")
    object Register : Route("register")
    object Mascotas : Route("mascotas")
    object AddMascota : Route("add_mascota")
    object Citas : Route("citas")
    object AddCita : Route("add_cita")
    object PetDetail : Route("pet_detail/{petId}") {
        fun createRoute(petId: Long) = "pet_detail/$petId"
    }
}