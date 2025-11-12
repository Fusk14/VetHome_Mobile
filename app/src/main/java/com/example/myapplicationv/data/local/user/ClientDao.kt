package com.example.myapplicationv.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientEntity): Long

    @Query("SELECT * FROM clients WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE id = :clientId LIMIT 1")
    suspend fun getClientById(clientId: Long): ClientEntity?

    // ✅ Nueva función: actualizar datos personales
    @Query("""
        UPDATE clients SET 
            name = :name,
            phone = :phone,
            address = :address,
            emergencyContact = :emergencyContact
        WHERE id = :clientId
    """)
    suspend fun updateClientInfo(
        clientId: Long,
        name: String,
        phone: String,
        address: String?,
        emergencyContact: String?
    )

    // ✅ Nueva función: cambiar contraseña
    @Query("UPDATE clients SET password = :newPassword WHERE id = :clientId")
    suspend fun updatePassword(clientId: Long, newPassword: String)
    @Query("SELECT * FROM clients WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): ClientEntity?
    @Query("SELECT COUNT(*) FROM clients")
    suspend fun count(): Int

}