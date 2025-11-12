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

    @Query("SELECT * FROM appointments WHERE ownerId = :ownerId")
    fun getAppointmentsByOwner(ownerId: Long): Flow<List<AppointmentEntity>> // <-- DEBE SER FLOW


    @Query("SELECT COUNT(*) FROM appointments")
    suspend fun count(): Int
}