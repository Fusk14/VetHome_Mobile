// test/data/local/user/ClientEntityTest.kt
package com.example.myapplicationv.data.local.user

import org.junit.Assert.assertEquals
import org.junit.Test

class ClientEntityTest {

    @Test
    fun clientEntity_propiedadesCompatibilidad_funcionanCorrectamente() {
        // Arrange
        val entity = ClientEntity(
            id = 1L,
            rut = "12345678-9",
            nombre = "Juan",
            apellido = "Pérez",
            correo = "juan@test.cl",
            telefono = "12345678",
            contrasena = "Pass123!",
            rolNombre = "CLIENTE",
            address = "Calle 123",
            emergencyContact = "98765432"
        )

        // Assert - Propiedades de compatibilidad
        assertEquals("Juan", entity.name)
        assertEquals("juan@test.cl", entity.email)
        assertEquals("12345678", entity.phone)
        assertEquals("Pass123!", entity.password)
        assertEquals("cliente", entity.role) // Verifica lowercase conversion
    }

    @Test
    fun clientEntity_rolAdmin_conversionCorrecta() {
        // Arrange
        val entity = ClientEntity(
            id = 1L,
            rut = "12345678-9",
            nombre = "Admin",
            apellido = "User",
            correo = "admin@test.cl",
            telefono = "12345678",
            contrasena = "Pass123!",
            rolNombre = "ADMIN"
        )

        // Assert
        assertEquals("admin", entity.role)
    }

    @Test
    fun clientEntity_camposOpcionales_nullPermitido() {
        // Arrange
        val entity = ClientEntity(
            id = 1L,
            rut = "12345678-9",
            nombre = "Test",
            apellido = "User",
            correo = "test@test.cl",
            telefono = "12345678",
            contrasena = "Pass123!",
            rolNombre = "CLIENTE",
            address = null,
            emergencyContact = null
        )

        // Assert
        assertEquals(null, entity.address)
        assertEquals(null, entity.emergencyContact)
    }
}