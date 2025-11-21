// test/data/local/pet/PetEntityTest.kt
package com.example.myapplicationv.data.local.pet

import org.junit.Assert.*
import org.junit.Test

class PetEntityTest {

    @Test
    fun petEntity_propiedadesBasicas_correctas() {
        // Arrange
        val entity = PetEntity(
            id = 1L,
            idCliente = 1L,
            nombre = "Firulais",
            especie = "Perro",
            raza = "Labrador",
            edad = 3,
            fechaNacimiento = "2020-01-01",
            peso = 15.5,
            color = "Negro",
            notasMedicas = "Alergia al pollo"
        )

        // Assert
        assertEquals(1L, entity.id)
        assertEquals(1L, entity.idCliente)
        assertEquals("Firulais", entity.nombre)
        assertEquals("Perro", entity.especie)
        assertEquals("Labrador", entity.raza)
        assertEquals(3, entity.edad)
        assertEquals("2020-01-01", entity.fechaNacimiento)
        assertEquals("Negro", entity.color)
        assertEquals("Alergia al pollo", entity.notasMedicas)
        assertTrue(entity.createdAt > 0)

        // Para el peso nullable, necesitamos verificar que no es null y luego el valor
        assertNotNull(entity.peso)
        assertEquals(15.5, entity.peso!!, 0.01)
    }

    @Test
    fun petEntity_camposOpcionales_nullPermitido() {
        // Arrange
        val entity = PetEntity(
            id = 1L,
            idCliente = 1L,
            nombre = "Michi",
            especie = "Gato",
            raza = null,
            edad = 0,
            fechaNacimiento = null,
            peso = null,
            color = null,
            notasMedicas = null
        )

        // Assert
        assertEquals("Michi", entity.nombre)
        assertEquals("Gato", entity.especie)
        assertNull(entity.raza)
        assertNull(entity.fechaNacimiento)
        assertNull(entity.peso) // ✅ Ahora verifica correctamente que es null
        assertNull(entity.color)
        assertNull(entity.notasMedicas)
        assertEquals(0, entity.edad)
    }

    @Test
    fun petEntity_conPesoYsinPeso_casosDiferentes() {
        // Arrange - Entity con peso
        val entityConPeso = PetEntity(
            id = 1L,
            idCliente = 1L,
            nombre = "Con Peso",
            especie = "Perro",
            peso = 20.0
        )

        // Arrange - Entity sin peso
        val entitySinPeso = PetEntity(
            id = 2L,
            idCliente = 1L,
            nombre = "Sin Peso",
            especie = "Gato",
            peso = null
        )

        // Assert
        assertNotNull(entityConPeso.peso)
        assertEquals(20.0, entityConPeso.peso!!, 0.01)

        assertNull(entitySinPeso.peso)
    }

    @Test
    fun petEntity_valoresPorDefecto_funcionan() {
        // Arrange
        val entity = PetEntity(
            id = 1L,
            idCliente = 1L,
            nombre = "SinEspecie"
            // Los demás campos usarán valores por defecto
        )

        // Assert
        assertEquals(1L, entity.id)
        assertEquals(1L, entity.idCliente)
        assertEquals("SinEspecie", entity.nombre)
        assertNull(entity.especie)
        assertNull(entity.raza)
        assertEquals(0, entity.edad)
        assertNull(entity.fechaNacimiento)
        assertNull(entity.peso) // ✅ Peso por defecto es null
        assertNull(entity.color)
        assertNull(entity.notasMedicas)
        assertTrue(entity.createdAt > 0)
    }

    @Test
    fun petEntity_pesoConDecimales_correcto() {
        // Arrange
        val entity = PetEntity(
            id = 1L,
            idCliente = 1L,
            nombre = "Mascota Decimal",
            especie = "Perro",
            peso = 12.75
        )

        // Assert
        assertNotNull(entity.peso)
        assertEquals(12.75, entity.peso!!, 0.001) // Delta pequeño para decimales
    }
}