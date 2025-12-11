package com.example.myapplicationv.data.local.resena

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ResenaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(resena: ResenaEntity): Long

    @Update
    suspend fun actualizar(resena: ResenaEntity)

    @Delete
    suspend fun eliminar(resena: ResenaEntity)

    @Query("SELECT * FROM resenas WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): ResenaEntity?

    // ✔ cambiado usuarioId → idCliente
    @Query("SELECT * FROM resenas WHERE idCliente = :clienteId ORDER BY createdAt DESC")
    fun obtenerPorUsuario(clienteId: Long): Flow<List<ResenaEntity>>

    // mascotaId existe y está OK
    @Query("SELECT * FROM resenas WHERE mascotaId = :mascotaId ORDER BY createdAt DESC")
    fun obtenerPorMascota(mascotaId: Long): Flow<List<ResenaEntity>>

    @Query("SELECT * FROM resenas WHERE sincronizado = 0")
    suspend fun obtenerNoSincronizadas(): List<ResenaEntity>

    @Query("DELETE FROM resenas WHERE id = :id")
    suspend fun eliminarPorId(id: Long)

    @Query("SELECT AVG(calificacion) FROM resenas WHERE mascotaId = :mascotaId")
    suspend fun obtenerPromedioCalificacion(mascotaId: Long): Double?
}
