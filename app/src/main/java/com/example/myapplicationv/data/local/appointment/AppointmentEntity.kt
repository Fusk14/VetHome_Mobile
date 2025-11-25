package com.example.myapplicationv.data.local.appointment

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myapplicationv.data.local.appointment.Converters
import java.util.Date

@Entity(tableName = "appointments")
@TypeConverters(Converters::class)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 0,

    // Campos del microservicio
    val idMascota: Long,
    val idVeterinario: Long,
    val idCliente: Long,
    val fecha: String, // "YYYY-MM-DD"
    val motivo: String? = null,
    val diagnostico: String? = null,
    val tratamiento: String? = null,

    // Campos locales opcionales
    val date: Date? = null,
    val reason: String? = null
) {
    @androidx.room.Ignore
    val ownerId: Long = idCliente

    @androidx.room.Ignore
    val petId: Long = idMascota
}
