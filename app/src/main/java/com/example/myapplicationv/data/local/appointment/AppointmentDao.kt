package com.example.myapplicationv.data.local.appointment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity): Long

    @Query("SELECT * FROM appointments")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    // ownerId → idCliente
    @Query("SELECT * FROM appointments WHERE idCliente = :clienteId")
    fun getAppointmentsByOwner(clienteId: Long): Flow<List<AppointmentEntity>>

    @Query("DELETE FROM appointments WHERE id = :appointmentId")
    suspend fun deleteAppointmentById(appointmentId: Long)

    // ownerId → idCliente
    @Query("DELETE FROM appointments WHERE idCliente = :clienteId")
    suspend fun deleteByOwnerId(clienteId: Long)

    @Query("SELECT COUNT(*) FROM appointments")
    suspend fun count(): Int
}
