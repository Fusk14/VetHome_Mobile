// test/data/local/appointment/AppointmentEntityTest.kt
package com.example.myapplicationv.data.local.appointment

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class AppointmentEntityTest {

    @Test
    fun appointmentEntity_propiedadesBasicas_correctas() {
        // Arrange
        val entity = AppointmentEntity(
            id = 1L,
            idMascota = 1L,
            idVeterinario = 2L,
            idCliente = 3L,
            fecha = "2023-12-01",
            motivo = "Consulta general",
            diagnostico = "Saludable",
            tratamiento = "Control en 6 meses",
            date = Date(),
            reason = "Consulta general"
        )

        // Assert
        assertEquals(1L, entity.id)
        assertEquals(1L, entity.idMascota)
        assertEquals(2L, entity.idVeterinario)
        assertEquals(3L, entity.idCliente)
        assertEquals("2023-12-01", entity.fecha)
        assertEquals("Consulta general", entity.motivo)
        assertEquals("Saludable", entity.diagnostico)
        assertEquals("Control en 6 meses", entity.tratamiento)
    }

    @Test
    fun appointmentEntity_propiedadesIgnoradas_correctas() {
        // Arrange
        val entity = AppointmentEntity(
            id = 1L,
            idMascota = 1L,
            idVeterinario = 2L,
            idCliente = 3L,
            fecha = "2023-12-01",
            motivo = "Vacunación"
        )

        // Assert - Propiedades @Ignore
        assertEquals(3L, entity.ownerId)
        assertEquals(1L, entity.petId)
    }

    @Test
    fun appointmentEntity_camposOpcionales_nullPermitido() {
        // Arrange
        val entity = AppointmentEntity(
            id = 1L,
            idMascota = 1L,
            idVeterinario = 2L,
            idCliente = 3L,
            fecha = "2023-12-01",
            motivo = null,
            diagnostico = null,
            tratamiento = null,
            date = null,
            reason = null
        )

        // Assert
        assertEquals(null, entity.motivo)
        assertEquals(null, entity.diagnostico)
        assertEquals(null, entity.tratamiento)
        assertEquals(null, entity.date)
        assertEquals(null, entity.reason)
    }
}