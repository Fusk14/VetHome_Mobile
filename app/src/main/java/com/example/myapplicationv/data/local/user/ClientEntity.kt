package com.example.myapplicationv.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,
    val email: String,
    val phone: String,
    val address: String? = null,
    val emergencyContact: String? = null,
    val password: String,
    val createdAt: Long = System.currentTimeMillis()
)