package com.example.myapplicationv.data.remote.dto

// Línea 1: data class que representa la forma del JSON de usuarios
data class UsuarioDto(
    val id: Long? = null,     // Línea 3: id del usuario
    val rut: String,           // Línea 4: RUT del usuario
    val nombre: String,        // Línea 5: nombre del usuario
    val apellido: String,     // Línea 6: apellido del usuario
    val correo: String,       // Línea 7: correo del usuario
    val telefono: String,    // Línea 8: teléfono del usuario
    val rol: RolDto? = null   // Línea 9: rol del usuario
)

data class RolDto(
    val id: Long? = null,
    val nombre: String
)

data class LoginRequestDto(
    val correo: String,
    val contrasena: String
)

data class RegisterRequestDto(
    val rut: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val telefono: String,
    val contrasena: String,
    val rolNombre: String
)

