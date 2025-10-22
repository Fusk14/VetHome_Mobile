package com.example.myapplicationv.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(client: ClientEntity): Long

    @Query("SELECT * FROM clients WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): ClientEntity?

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun count(): Int

    @Query("SELECT * FROM clients ORDER BY id ASC")
    suspend fun getAll(): List<ClientEntity>

    @Query("SELECT * FROM clients WHERE id = :clientId LIMIT 1")
    suspend fun getById(clientId: Long): ClientEntity?
}