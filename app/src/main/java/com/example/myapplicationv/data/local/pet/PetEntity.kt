package com.example.myapplicationv.data.local.pet

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "pets",
    foreignKeys = [ForeignKey(
        entity = com.example.myapplicationv.data.local.user.ClientEntity::class,
        parentColumns = ["id"],
        childColumns = ["ownerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val ownerId: Long,
    val nombre: String,
    val especie: String,
    val raza: String,
    val fechaNacimiento: String? = null,
    val peso: Double? = null,
    val color: String? = null,
    val notasMedicas: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)