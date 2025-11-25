package com.example.myapplicationv.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = false) // Los IDs vienen del microservicio
    val id: Long = 0L,
    val rut: String = "", // Nuevo campo para RUT
    val nombre: String = "", // Cambiado de name
    val apellido: String = "", // Nuevo campo
    val correo: String = "", // Cambiado de email
    val telefono: String = "", // Cambiado de phone
    val contrasena: String = "", // Cambiado de password
    val rolNombre: String = "CLIENTE", // Cambiado de role, valores: CLIENTE, VETERINARIO, ADMINISTRATIVO
    // Campos adicionales para compatibilidad local (no están en el microservicio)
    val address: String? = null,
    val emergencyContact: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Propiedades de compatibilidad para mantener funcionalidad existente
    val name: String get() = nombre
    val email: String get() = correo
    val phone: String get() = telefono
    val password: String get() = contrasena
    val role: String get() = rolNombre.lowercase()
}