package com.example.myapplicationv.navigation

sealed class Route(val path: String) {
    // Rutas simples sin argumentos
    object Home : Route("home")
    object Login : Route("login")
    object Register : Route("register")
    object Mascotas : Route("mascotas")
    object AddMascota : Route("add_mascota")
    object Citas : Route("citas")
    object AddCita : Route("add_cita")
    object AdminDashboard : Route("admin_dashboard")

    // ✅ Nueva ruta: Perfil del usuario
    object Perfil : Route("perfil")

    // Rutas con argumentos
    object PetDetail : Route("pet_detail/{petId}") {
        fun createRoute(petId: Long) = "pet_detail/$petId"
    }

    object EditUser : Route("edit_user/{userId}") {
        fun createRoute(userId: Long) = "edit_user/$userId"
    }
    // ✅ NUEVAS RUTAS PARA RESEÑAS
    object Resenas : Route("resenas")
    object AddResena : Route("add_resena")

    // Rutas con argumentos para reseñas
    object ResenaDetail : Route("resena_detail/{resenaId}") {
        fun createRoute(resenaId: Long) = "resena_detail/$resenaId"
    }
}
