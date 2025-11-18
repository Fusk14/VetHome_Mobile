package com.example.myapplicationv.data.local.resena

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ResenaDao {
    @Insert
    suspend fun insertar(resena: ResenaEntity): Long

    @Update
    suspend fun actualizar(resena: ResenaEntity)

    @Delete
    suspend fun eliminar(resena: ResenaEntity)

    @Query("SELECT * FROM resenas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): ResenaEntity?

    @Query("SELECT * FROM resenas WHERE usuarioId = :usuarioId ORDER BY createdAt DESC")
    fun obtenerPorUsuario(usuarioId: Long): Flow<List<ResenaEntity>>

    @Query("SELECT * FROM resenas WHERE mascotaId = :mascotaId ORDER BY createdAt DESC")
    fun obtenerPorMascota(mascotaId: Long): Flow<List<ResenaEntity>>

    @Query("SELECT * FROM resenas WHERE sincronizado = 0")
    suspend fun obtenerNoSincronizadas(): List<ResenaEntity>

    @Query("DELETE FROM resenas WHERE id = :id")
    suspend fun eliminarPorId(id: Long)

    @Query("SELECT AVG(calificacion) FROM resenas WHERE mascotaId = :mascotaId")
    suspend fun obtenerPromedioCalificacion(mascotaId: Long): Double?
}