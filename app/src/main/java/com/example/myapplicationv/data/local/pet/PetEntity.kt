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
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String? = null,
    val weight: Double? = null,
    val color: String? = null,
    val medicalNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)