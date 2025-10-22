package com.example.myapplicationv.data.local.pet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PetDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(pet: PetEntity): Long

    @Query("SELECT * FROM pets WHERE ownerId = :ownerId ORDER BY nombre ASC")
    suspend fun getPetsByOwner(ownerId: Long): List<PetEntity>

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getById(petId: Long): PetEntity?

    @Query("UPDATE pets SET peso = :nuevoPeso WHERE id = :petId")
    suspend fun updateWeight(petId: Long, nuevoPeso: Double)

    @Query("DELETE FROM pets WHERE id = :petId")
    suspend fun delete(petId: Long)

    @Query("SELECT COUNT(*) FROM pets WHERE ownerId = :ownerId")
    suspend fun countByOwner(ownerId: Long): Int
}