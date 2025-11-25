// test/data/local/appointment/AppointmentDaoTest.kt
package com.example.myapplicationv.data.local.appointment

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
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AppointmentDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var appointmentDao: AppointmentDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        appointmentDao = database.appointmentDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAppointment_andGetAll_worksCorrectly() = runBlocking {
        // Arrange
        val appointment = AppointmentEntity(
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

        // Act
        appointmentDao.insert(appointment)
        val allAppointments = appointmentDao.getAllAppointments().first()

        // Assert
        assertEquals(1, allAppointments.size)
        val retrieved = allAppointments[0]
        assertEquals(1L, retrieved.id)
        assertEquals(1L, retrieved.idMascota)
        assertEquals(2L, retrieved.idVeterinario)
        assertEquals(3L, retrieved.idCliente)
        assertEquals("2023-12-01", retrieved.fecha)
        assertEquals("Consulta general", retrieved.motivo)
        assertEquals("Saludable", retrieved.diagnostico)
        assertEquals("Control en 6 meses", retrieved.tratamiento)
    }

    @Test
    fun getAppointmentsByOwner_returnsCorrectAppointments() = runBlocking {
        // Arrange
        val appointment1 = AppointmentEntity(
            id = 1L, idMascota = 1L, idVeterinario = 2L, idCliente = 1L,
            fecha = "2023-12-01", motivo = "Consulta 1"
        )
        val appointment2 = AppointmentEntity(
            id = 2L, idMascota = 2L, idVeterinario = 2L, idCliente = 1L,
            fecha = "2023-12-02", motivo = "Consulta 2"
        )
        val appointment3 = AppointmentEntity(
            id = 3L, idMascota = 3L, idVeterinario = 2L, idCliente = 2L,
            fecha = "2023-12-03", motivo = "Consulta 3"
        )

        // Act
        appointmentDao.insert(appointment1)
        appointmentDao.insert(appointment2)
        appointmentDao.insert(appointment3)
        val ownerAppointments = appointmentDao.getAppointmentsByOwner(1L).first()

        // Assert
        assertEquals(2, ownerAppointments.size)
        assertTrue(ownerAppointments.all { it.idCliente == 1L })
        assertTrue(ownerAppointments.any { it.motivo == "Consulta 1" })
        assertTrue(ownerAppointments.any { it.motivo == "Consulta 2" })
    }

    @Test
    fun deleteAppointmentById_removesAppointment() = runBlocking {
        // Arrange
        val appointment = AppointmentEntity(
            id = 1L, idMascota = 1L, idVeterinario = 2L, idCliente = 1L,
            fecha = "2023-12-01", motivo = "To Delete"
        )
        appointmentDao.insert(appointment)

        // Act
        appointmentDao.deleteAppointmentById(1L)
        val allAppointments = appointmentDao.getAllAppointments().first()

        // Assert
        assertEquals(0, allAppointments.size)
    }

    @Test
    fun deleteByOwnerId_removesAllOwnerAppointments() = runBlocking {
        // Arrange
        val appointment1 = AppointmentEntity(
            id = 1L, idMascota = 1L, idVeterinario = 2L, idCliente = 1L,
            fecha = "2023-12-01", motivo = "Consulta 1"
        )
        val appointment2 = AppointmentEntity(
            id = 2L, idMascota = 2L, idVeterinario = 2L, idCliente = 1L,
            fecha = "2023-12-02", motivo = "Consulta 2"
        )
        val appointment3 = AppointmentEntity(
            id = 3L, idMascota = 3L, idVeterinario = 2L, idCliente = 2L,
            fecha = "2023-12-03", motivo = "Consulta 3"
        )

        // Act
        appointmentDao.insert(appointment1)
        appointmentDao.insert(appointment2)
        appointmentDao.insert(appointment3)
        appointmentDao.deleteByOwnerId(1L)

        val owner1Appointments = appointmentDao.getAppointmentsByOwner(1L).first()
        val owner2Appointments = appointmentDao.getAppointmentsByOwner(2L).first()

        // Assert
        assertEquals(0, owner1Appointments.size)
        assertEquals(1, owner2Appointments.size)
    }

    @Test
    fun count_returnsCorrectCount() = runBlocking {
        // Arrange
        val appointment1 = AppointmentEntity(
            id = 1L, idMascota = 1L, idVeterinario = 2L, idCliente = 1L,
            fecha = "2023-12-01", motivo = "Consulta 1"
        )
        val appointment2 = AppointmentEntity(
            id = 2L, idMascota = 2L, idVeterinario = 2L, idCliente = 1L,
            fecha = "2023-12-02", motivo = "Consulta 2"
        )

        // Act
        appointmentDao.insert(appointment1)
        val count1 = appointmentDao.count()
        appointmentDao.insert(appointment2)
        val count2 = appointmentDao.count()

        // Assert
        assertEquals(1, count1)
        assertEquals(2, count2)
    }
}