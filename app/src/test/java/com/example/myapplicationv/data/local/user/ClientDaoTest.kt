// test/data/local/user/ClientDaoTest.kt
package com.example.myapplicationv.data.local.user

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.myapplicationv.data.local.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ClientDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var clientDao: ClientDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        clientDao = database.clientDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertClient_andGetById_worksCorrectly() = runBlocking {
        // Arrange
        val client = ClientEntity(
            id = 1L,
            rut = "12345678-9",
            nombre = "Juan Pérez",
            apellido = "González",
            correo = "juan@test.cl",
            telefono = "12345678",
            contrasena = "Pass123!",
            rolNombre = "CLIENTE"
        )

        // Act
        clientDao.insert(client)
        val retrieved = clientDao.getById(1L)

        // Assert
        assertNotNull(retrieved)
        assertEquals("Juan Pérez", retrieved?.nombre)
        assertEquals("juan@test.cl", retrieved?.correo)
        assertEquals("12345678", retrieved?.telefono)
        assertEquals("CLIENTE", retrieved?.rolNombre)
    }

    @Test
    fun insertClient_andGetByEmail_worksCorrectly() = runBlocking {
        // Arrange
        val client = ClientEntity(
            id = 1L,
            rut = "12345678-9",
            nombre = "María López",
            apellido = "Martínez",
            correo = "maria@test.cl",
            telefono = "87654321",
            contrasena = "Pass123!",
            rolNombre = "CLIENTE"
        )

        // Act
        clientDao.insert(client)
        val retrieved = clientDao.getByEmail("maria@test.cl")

        // Assert
        assertNotNull(retrieved)
        assertEquals("María López", retrieved?.nombre)
        assertEquals("87654321", retrieved?.telefono)
    }

    @Test
    fun getAllClients_returnsAllClients() = runBlocking {
        // Arrange
        val client1 = ClientEntity(
            id = 1L,
            nombre = "Cliente 1",
            correo = "cliente1@test.cl",
            rut = "11111111-1",
            apellido = "Apellido1",
            telefono = "11111111",
            contrasena = "pass1",
            rolNombre = "CLIENTE"
        )
        val client2 = ClientEntity(
            id = 2L,
            nombre = "Cliente 2",
            correo = "cliente2@test.cl",
            rut = "22222222-2",
            apellido = "Apellido2",
            telefono = "22222222",
            contrasena = "pass2",
            rolNombre = "CLIENTE"
        )

        // Act
        clientDao.insert(client1)
        clientDao.insert(client2)
        val allClients = clientDao.getAllClients().first()

        // Assert
        assertEquals(2, allClients.size)
        assertTrue(allClients.any { it.nombre == "Cliente 1" })
        assertTrue(allClients.any { it.nombre == "Cliente 2" })
    }

    @Test
    fun updateClientInfo_updatesCorrectFields() = runBlocking {
        // Arrange
        val client = ClientEntity(
            id = 1L,
            rut = "12345678-9",
            nombre = "Original",
            apellido = "Name",
            correo = "original@test.cl",
            telefono = "11111111",
            contrasena = "Pass123!",
            rolNombre = "CLIENTE",
            address = "Old Address",
            emergencyContact = "Old Contact"
        )
        clientDao.insert(client)

        // Act
        clientDao.updateClientInfo(
            clientId = 1L,
            name = "Updated Name",
            phone = "99999999",
            address = "New Address",
            emergencyContact = "New Contact"
        )
        val updated = clientDao.getById(1L)

        // Assert
        assertNotNull(updated)
        assertEquals("Updated Name", updated?.nombre)
        assertEquals("99999999", updated?.telefono)
        assertEquals("New Address", updated?.address)
        assertEquals("New Contact", updated?.emergencyContact)
        // Campos que NO deberían cambiar
        assertEquals("original@test.cl", updated?.correo)
        assertEquals("Pass123!", updated?.contrasena)
        assertEquals("CLIENTE", updated?.rolNombre)
    }

    @Test
    fun updatePassword_updatesPasswordCorrectly() = runBlocking {
        // Arrange
        val client = ClientEntity(
            id = 1L,
            rut = "12345678-9",
            nombre = "Test User",
            apellido = "Test",
            correo = "test@test.cl",
            telefono = "12345678",
            contrasena = "OldPass123!",
            rolNombre = "CLIENTE"
        )
        clientDao.insert(client)

        // Act
        clientDao.updatePassword(1L, "NewPass456!")
        val updated = clientDao.getById(1L)

        // Assert
        assertNotNull(updated)
        assertEquals("NewPass456!", updated?.contrasena)
        // Otros campos no deben cambiar
        assertEquals("Test User", updated?.nombre)
        assertEquals("test@test.cl", updated?.correo)
    }

    @Test
    fun deleteClient_removesClient() = runBlocking {
        // Arrange
        val client = ClientEntity(
            id = 1L,
            nombre = "To Delete",
            correo = "delete@test.cl",
            rut = "12345678-9",
            apellido = "Delete",
            telefono = "12345678",
            contrasena = "Pass123!",
            rolNombre = "CLIENTE"
        )
        clientDao.insert(client)

        // Act
        clientDao.deleteById(1L)
        val retrieved = clientDao.getById(1L)

        // Assert
        assertNull(retrieved)
    }

    @Test
    fun count_returnsCorrectCount() = runBlocking {
        // Arrange
        val client1 = ClientEntity(
            id = 1L, nombre = "Client 1", correo = "client1@test.cl",
            rut = "11111111-1", apellido = "A", telefono = "11111111",
            contrasena = "pass1", rolNombre = "CLIENTE"
        )
        val client2 = ClientEntity(
            id = 2L, nombre = "Client 2", correo = "client2@test.cl",
            rut = "22222222-2", apellido = "B", telefono = "22222222",
            contrasena = "pass2", rolNombre = "CLIENTE"
        )

        // Act
        clientDao.insert(client1)
        val count1 = clientDao.count()
        clientDao.insert(client2)
        val count2 = clientDao.count()

        // Assert
        assertEquals(1, count1)
        assertEquals(2, count2)
    }
}