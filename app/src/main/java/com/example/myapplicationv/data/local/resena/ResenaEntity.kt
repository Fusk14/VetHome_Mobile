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
            childColumns = ["idCliente"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ResenaEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 0L,
    val idCliente: Long,
    val idVeterinario: Long,
    val calificacion: Int,
    val comentario: String,
    val mascotaId: Long? = null,
    val mascotaNombre: String? = null,
    val fecha: String? = null,
    val sincronizado: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
