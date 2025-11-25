package com.example.myapplicationv.data.remote.dto

// Línea 1: data class que representa la forma del JSON de reseñas
data class ResenaDto(
    val id: Long? = null,      // Línea 3: id de la reseña
    val idCliente: Long,       // Línea 4: id del cliente que hace la reseña
    val idVeterinario: Long,   // Línea 5: id del veterinario calificado
    val calificacion: Int,     // Línea 6: calificación (1-5)
    val comentario: String    // Línea 7: comentario de la reseña
)

