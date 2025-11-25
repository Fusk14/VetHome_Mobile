package com.example.myapplicationv.data.repository

import com.example.myapplicationv.data.local.appointment.AppointmentDao
import com.example.myapplicationv.data.local.pet.PetDao
import com.example.myapplicationv.data.local.pet.PetEntity
import com.example.myapplicationv.data.local.resena.ResenaDao
import com.example.myapplicationv.data.local.user.ClientDao
import com.example.myapplicationv.data.local.user.ClientEntity
import com.example.myapplicationv.data.remote.*
import com.example.myapplicationv.data.remote.dto.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class VetRepositoryTest {

    private lateinit var repository: VetRepository
    private lateinit var clientDao: ClientDao
    private lateinit var petDao: PetDao
    private lateinit var appointmentDao: AppointmentDao
    private lateinit var resenaDao: ResenaDao
    private lateinit var usuarioApi: UsuarioApi
    private lateinit var mascotaApi: MascotaApi
    private lateinit var consultaApi: ConsultaApi
    private lateinit var resenaApi: ResenaApi

    @Before
    fun setUp() {
        clientDao = mockk(relaxed = true)
        petDao = mockk(relaxed = true)
        appointmentDao = mockk(relaxed = true)
        resenaDao = mockk(relaxed = true)
        usuarioApi = mockk(relaxed = true)
        mascotaApi = mockk(relaxed = true)
        consultaApi = mockk(relaxed = true)
        resenaApi = mockk(relaxed = true)

        // Mock del RemoteModule usando mockkObject
        mockkObject(RemoteModule)
        every { RemoteModule.usuarioApi } returns usuarioApi
        every { RemoteModule.mascotaApi } returns mascotaApi
        every { RemoteModule.consultaApi } returns consultaApi
        every { RemoteModule.resenaApi } returns resenaApi

        repository = VetRepository(clientDao, petDao, appointmentDao, resenaDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun login_emailYPasswordValidos_retornaCliente() = runBlocking {
        // Arrange
        val email = "test@test.cl"
        val password = "Pass123!"
        val clientEntity = ClientEntity(
            id = 1L,
            nombre = "Test User",
            correo = email,
            contrasena = password,
            rolNombre = "CLIENTE"
        )
        val usuarioDto = UsuarioDto(
            id = 1L,
            rut = "12345678",
            nombre = "Test",
            apellido = "User",
            correo = email,
            telefono = "12345678",
            rol = RolDto(1L, "CLIENTE")
        )

        coEvery { clientDao.getByEmail(email) } returns clientEntity
        coEvery { usuarioApi.login(any()) } returns "token"
        coEvery { usuarioApi.getUsuarioByCorreo(email) } returns usuarioDto
        coEvery { clientDao.insert(any()) } returns 1L

        // Act
        val result = repository.login(email, password)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.id)
    }

    @Test
    fun login_emailInvalido_retornaError() = runBlocking {
        // Arrange
        val email = "emailinvalido"
        val password = "Pass123!"

        // Act
        val result = repository.login(email, password)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Formato de email inválido", result.exceptionOrNull()?.message)
    }

    @Test
    fun login_usuarioNoEncontrado_retornaError() = runBlocking {
        // Arrange
        val email = "notfound@test.cl"
        val password = "Pass123!"

        coEvery { clientDao.getByEmail(email) } returns null

        // Act
        val result = repository.login(email, password)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Usuario no encontrado. Por favor regístrese primero.", result.exceptionOrNull()?.message)
    }

    @Test
    fun register_datosValidos_retornaIdCliente() = runBlocking {
        // Arrange
        val name = "Juan Pérez"
        val email = "juan@test.cl"
        val phone = "12345678"
        val password = "Pass123!"
        val usuarioDto = UsuarioDto(
            id = 1L,
            rut = "juan",
            nombre = "Juan",
            apellido = "Pérez",
            correo = email,
            telefono = phone,
            rol = RolDto(1L, "CLIENTE")
        )

        coEvery { usuarioApi.register(any()) } returns usuarioDto
        coEvery { clientDao.insert(any()) } returns 1L

        // Act
        val result = repository.register(name, email, phone, null, null, password)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun register_nombreInvalido_retornaError() = runBlocking {
        // Arrange
        val name = "Juan123" // Nombre con números
        val email = "juan@test.cl"
        val phone = "12345678"
        val password = "Pass123!"

        // Act
        val result = repository.register(name, email, phone, null, null, password)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Solo letras y espacios", result.exceptionOrNull()?.message)
    }

    @Test
    fun addPet_datosValidos_retornaIdMascota() = runBlocking {
        // Arrange
        val ownerId = 1L
        val nombre = "Firulais"
        val especie = "Perro"
        val raza = "Labrador"
        val mascotaDto = MascotaDto(
            id = 1L,
            idCliente = ownerId,
            nombre = nombre,
            especie = especie,
            raza = raza,
            edad = 2
        )
        val clientEntity = ClientEntity(id = ownerId, nombre = "Test User")

        coEvery { clientDao.getById(ownerId) } returns clientEntity
        coEvery { mascotaApi.createMascota(any()) } returns mascotaDto
        coEvery { petDao.insert(any()) } returns 1L

        // Act
        val result = repository.addPet(ownerId, nombre, especie, raza, "2020-01-01", 15.5, "Negro", null)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun addPet_especieInvalida_retornaError() = runBlocking {
        // Arrange
        val ownerId = 1L
        val nombre = "Firulais"
        val especie = "Elefante" // Especie inválida
        val raza = "Labrador"
        val clientEntity = ClientEntity(id = ownerId, nombre = "Test User")

        coEvery { clientDao.getById(ownerId) } returns clientEntity

        // Act
        val result = repository.addPet(ownerId, nombre, especie, raza, null, null, null, null)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Selecciona una especie válida", result.exceptionOrNull()?.message)
    }

    @Test
    fun crearResena_datosValidos_retornaIdResena() = runBlocking {
        // Arrange
        val usuarioId = 1L
        val mascotaId = 1L
        val mascotaNombre = "Firulais"
        val calificacion = 5
        val comentario = "Excelente servicio"
        val fecha = "2023-01-01"
        val resenaDto = ResenaDto(
            id = 1L,
            idCliente = usuarioId,
            idVeterinario = usuarioId,
            calificacion = calificacion,
            comentario = comentario
        )

        coEvery { resenaApi.createResena(any()) } returns resenaDto
        coEvery { resenaDao.insertar(any()) } returns 1L

        // Act
        val result = repository.crearResena(usuarioId, mascotaId, mascotaNombre, calificacion, comentario, fecha)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun crearResena_calificacionInvalida_retornaError() = runBlocking {
        // Arrange
        val usuarioId = 1L
        val mascotaId = 1L
        val mascotaNombre = "Firulais"
        val calificacion = 6 // Calificación inválida
        val comentario = "Excelente servicio"
        val fecha = "2023-01-01"

        // Act
        val result = repository.crearResena(usuarioId, mascotaId, mascotaNombre, calificacion, comentario, fecha)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("La calificación debe ser entre 1 y 5", result.exceptionOrNull()?.message)
    }

    @Test
    fun crearResena_comentarioVacio_retornaError() = runBlocking {
        // Arrange
        val usuarioId = 1L
        val mascotaId = 1L
        val mascotaNombre = "Firulais"
        val calificacion = 5
        val comentario = "" // Comentario vacío
        val fecha = "2023-01-01"

        // Act
        val result = repository.crearResena(usuarioId, mascotaId, mascotaNombre, calificacion, comentario, fecha)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("El comentario no puede estar vacío", result.exceptionOrNull()?.message)
    }

    @Test
    fun getAllPets_retornaListaMascotas() = runBlocking {
        // Arrange
        val petsList = listOf(
            PetEntity(id = 1L, idCliente = 1L, nombre = "Firulais", especie = "Perro"),
            PetEntity(id = 2L, idCliente = 1L, nombre = "Michi", especie = "Gato")
        )
        coEvery { petDao.getAllPets() } returns flowOf(petsList)

        // Act
        val result = repository.getAllPets()

        // Assert
        result.collect { pets ->
            assertEquals(2, pets.size)
            assertEquals("Firulais", pets[0].nombre)
        }
    }

    @Test
    fun getPetsByOwner_retornaMascotasDelPropietario() = runBlocking {
        // Arrange
        val ownerId = 1L
        val petsList = listOf(
            PetEntity(id = 1L, idCliente = ownerId, nombre = "Firulais", especie = "Perro")
        )
        coEvery { petDao.getPetByOwnerId(ownerId) } returns flowOf(petsList)

        // Act
        val result = repository.getPetsByOwner(ownerId)

        // Assert
        result.collect { pets ->
            assertEquals(1, pets.size)
            assertEquals(ownerId, pets[0].idCliente)
        }
    }
}