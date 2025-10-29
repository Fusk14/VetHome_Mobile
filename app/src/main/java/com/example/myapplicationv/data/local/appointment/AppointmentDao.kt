package com.example.myapplicationv.data.local.appointment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity): Long

    @Query("SELECT * FROM appointments WHERE ownerId = :ownerId ORDER BY date DESC")
    suspend fun getAppointmentsByOwner(ownerId: Long): List<AppointmentEntity>

    @Query("SELECT COUNT(*) FROM appointments")
    suspend fun count(): Int
}