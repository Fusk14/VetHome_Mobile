package com.example.myapplicationv.data.remote.dto

// Línea 1: data class que representa la forma del JSON de consultas
data class ConsultaDto(
    val id: Long? = null,         // Línea 3: id de la consulta
    val idMascota: Long,          // Línea 4: id de la mascota
    val idVeterinario: Long,     // Línea 5: id del veterinario
    val idCliente: Long,          // Línea 6: id del cliente
    val fecha: String,            // Línea 7: fecha de la consulta (YYYY-MM-DD)
    val motivo: String? = null,   // Línea 8: motivo de la consulta
    val diagnostico: String? = null,    // Línea 9: diagnóstico
    val tratamiento: String? = null    // Línea 10: tratamiento
)

