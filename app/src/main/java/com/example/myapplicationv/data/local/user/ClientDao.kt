package com.example.myapplicationv.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientEntity): Long

    @Query("SELECT * FROM clients")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE correo = :email AND contrasena = :password LIMIT 1")
    suspend fun login(email: String, password: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE id = :clientId LIMIT 1")
    suspend fun getById(clientId: Long): ClientEntity?

    @Query("SELECT * FROM clients WHERE rut = :rut LIMIT 1")
    suspend fun getByRut(rut: String): ClientEntity?

    @Query("DELETE FROM clients WHERE id = :clientId")
    suspend fun deleteById(clientId: Long)

    @Query("""
        UPDATE clients SET 
            nombre = :name,
            telefono = :phone,
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

    @Query("UPDATE clients SET contrasena = :newPassword WHERE id = :clientId")
    suspend fun updatePassword(clientId: Long, newPassword: String)

    @Query("SELECT * FROM clients WHERE correo = :email LIMIT 1")
    suspend fun getByEmail(email: String): ClientEntity?

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun count(): Int
}
