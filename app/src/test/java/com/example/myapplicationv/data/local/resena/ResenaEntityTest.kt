// test/data/local/resena/ResenaEntityTest.kt
package com.example.myapplicationv.data.local.resena

import org.junit.Assert.assertEquals
import org.junit.Test

class ResenaEntityTest {

    @Test
    fun resenaEntity_propiedadesBasicas_correctas() {
        // Arrange
        val entity = ResenaEntity(
            id = 1L,
            idCliente = 1L,
            idVeterinario = 2L,
            calificacion = 5,
            comentario = "Excelente servicio",
            mascotaId = 1L,
            mascotaNombre = "Firulais",
            fecha = "2023-12-01",
            sincronizado = true
        )

        // Assert
        assertEquals(1L, entity.id)
        assertEquals(1L, entity.idCliente)
        assertEquals(2L, entity.idVeterinario)
        assertEquals(5, entity.calificacion)
        assertEquals("Excelente servicio", entity.comentario)
        assertEquals(1L, entity.mascotaId)
        assertEquals("Firulais", entity.mascotaNombre)
        assertEquals("2023-12-01", entity.fecha)
        assertEquals(true, entity.sincronizado)
    }

    @Test
    fun resenaEntity_valoresPorDefecto_correctos() {
        // Arrange
        val entity = ResenaEntity(
            id = 1L,
            idCliente = 1L,
            idVeterinario = 2L,
            calificacion = 4,
            comentario = "Buen servicio",
            mascotaId = null,
            mascotaNombre = null,
            fecha = null,
            sincronizado = false
        )

        // Assert - createdAt debería tener valor por defecto
        assertEquals(false, entity.sincronizado)
        assertEquals(null, entity.mascotaId)
        assertEquals(null, entity.mascotaNombre)
        assertEquals(null, entity.fecha)
    }
}