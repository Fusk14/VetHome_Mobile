package com.example.myapplicationv.data.remote.dto

//  class que representa la forma del JSON de mascotas
data class MascotaDto(
    val id: Long? = null,      // Línea 3: id de la mascota
    val idCliente: Long,       // Línea 4: id del cliente propietario
    val nombre: String,        // Línea 5: nombre de la mascota
    val especie: String? = null, // Línea 6: especie (Perro, Gato, etc.)
    val raza: String? = null,   // Línea 7: raza de la mascota
    val genero: String? = null, // genero de la mascota
    val edad: Int = 0          // Línea 8: edad de la mascota
)
