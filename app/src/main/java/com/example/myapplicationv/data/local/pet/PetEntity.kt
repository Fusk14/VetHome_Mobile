package com.example.myapplicationv.data.local.pet

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "pets",
    foreignKeys = [ForeignKey(
        entity = com.example.myapplicationv.data.local.user.ClientEntity::class,
        parentColumns = ["id"],
        childColumns = ["idCliente"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PetEntity(
    @PrimaryKey(autoGenerate = false) // Los IDs vienen del microservicio
    val id: Long = 0L,
    val idCliente: Long, // Cambiado de ownerId para coincidir con microservicio
    val nombre: String,
    val especie: String? = null, // Ahora nullable como en el microservicio
    val raza: String? = null, // Ahora nullable como en el microservicio
    val genero: String? = null,
    val edad: Int = 0, // Nuevo campo del microservicio
    // Campos adicionales para compatibilidad local (no están en el microservicio)
    val fechaNacimiento: String? = null,
    val peso: Double? = null,
    val color: String? = null,
    val notasMedicas: String? = null,
    val imagenUri: String? = null, // URI de la imagen de la mascota
    val createdAt: Long = System.currentTimeMillis(),
)
