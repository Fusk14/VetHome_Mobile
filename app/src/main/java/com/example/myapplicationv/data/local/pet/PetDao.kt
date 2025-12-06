package com.example.myapplicationv.data.local.pet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: PetEntity): Long

    @Query("SELECT * FROM pets")
    fun getAllPets(): Flow<List<PetEntity>>

    // ownerId → idCliente
    @Query("SELECT * FROM pets WHERE idCliente = :clienteId")
    fun getPetByOwnerId(clienteId: Long): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getById(petId: Long): PetEntity?

    @Query("UPDATE pets SET peso = :nuevoPeso WHERE id = :petId")
    suspend fun updateWeight(petId: Long, nuevoPeso: Double)

    @Query("UPDATE pets SET imagenUri = :imagenUri WHERE id = :petId")
    suspend fun updateImageUri(petId: Long, imagenUri: String?)

    @Query("DELETE FROM pets WHERE id = :petId")
    suspend fun deleteById(petId: Long)

    // ownerId → idCliente
    @Query("DELETE FROM pets WHERE idCliente = :clienteId")
    suspend fun deleteByOwnerId(clienteId: Long)

    @Query("SELECT COUNT(*) FROM pets WHERE idCliente = :clienteId")
    suspend fun countByOwner(clienteId: Long): Int
}
