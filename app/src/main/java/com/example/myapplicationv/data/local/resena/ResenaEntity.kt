package com.example.myapplicationv.data.local.resena

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "resenas",
    foreignKeys = [
        ForeignKey(
            entity = com.example.myapplicationv.data.local.user.ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = com.example.myapplicationv.data.local.pet.PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["mascotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ResenaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val usuarioId: Long,
    val mascotaId: Long,
    val mascotaNombre: String,
    val calificacion: Int, // 1-5 estrellas
    val comentario: String,
    val fecha: String, // Formato: "2024-01-15"
    val sincronizado: Boolean = false, // Para sincronización con backend
    val createdAt: Long = System.currentTimeMillis()
)