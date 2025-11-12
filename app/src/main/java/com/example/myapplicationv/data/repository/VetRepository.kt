package com.example.myapplicationv.data.repository

import com.example.myapplicationv.data.local.appointment.AppointmentDao
import com.example.myapplicationv.data.local.appointment.AppointmentEntity
import com.example.myapplicationv.data.local.user.ClientDao
import com.example.myapplicationv.data.local.user.ClientEntity
import com.example.myapplicationv.data.local.pet.PetDao
import com.example.myapplicationv.data.local.pet.PetEntity
import com.example.myapplicationv.domain.validation.* // ← Validadores centralizados
import kotlinx.coroutines.flow.Flow
import java.util.Date

class VetRepository(
    private val clientDao: ClientDao,
    private val petDao: PetDao,
    private val appointmentDao: AppointmentDao
) {
    // ─────────────────────────────────────────────
    // 🟢 LOGIN Y REGISTRO
    // ─────────────────────────────────────────────

    suspend fun login(email: String, password: String): Result<ClientEntity> {
        val emailError = validateEmail(email)
        if (emailError != null)
            return Result.failure(IllegalArgumentException(emailError))

        val client = clientDao.login(email, password)
        return if (client != null) {
            Result.success(client)
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas"))
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        address: String? = null,
        emergencyContact: String? = null,
        password: String
    ): Result<Long> {
        val nameError = validateNameLettersOnly(name)
        val emailError = validateEmail(email)
        val phoneError = validatePhoneDigitsOnly(phone)
        val addressError = validateAddress(address ?: "")
        val emergencyContactError = validateEmergencyContact(emergencyContact ?: "")
        val passwordError = validateStrongPassword(password)

        val errors = listOf(nameError, emailError, phoneError, addressError, emergencyContactError, passwordError)
        val firstError = errors.firstOrNull { it != null }
        if (firstError != null)
            return Result.failure(IllegalArgumentException(firstError))

        val exists = clientDao.getByEmail(email) != null
        if (exists)
            return Result.failure(IllegalStateException("El correo ya está registrado"))

        val id = clientDao.insert(
            ClientEntity(
                name = name,
                email = email,
                phone = phone,
                address = address,
                emergencyContact = emergencyContact,
                password = password
            )
        )
        return Result.success(id)
    }

    // ─────────────────────────────────────────────
    // 🟢 CLIENTE: Obtener y actualizar información
    // ─────────────────────────────────────────────

    suspend fun getClientById(clientId: Long): ClientEntity? {
        return clientDao.getClientById(clientId)
    }

    suspend fun updateClientInfo(
        clientId: Long,
        name: String,
        phone: String,
        address: String?,
        emergencyContact: String?
    ): Result<Unit> {
        val nameError = validateNameLettersOnly(name)
        val phoneError = validatePhoneDigitsOnly(phone)
        val addressError = validateAddress(address ?: "")
        val emergencyError = validateEmergencyContact(emergencyContact ?: "")

        val firstError = listOf(nameError, phoneError, addressError, emergencyError)
            .firstOrNull { it != null }

        if (firstError != null)
            return Result.failure(IllegalArgumentException(firstError))

        clientDao.updateClientInfo(clientId, name, phone, address, emergencyContact)
        return Result.success(Unit)
    }

    suspend fun changePassword(clientId: Long, newPassword: String): Result<Unit> {
        val passError = validateStrongPassword(newPassword)
        if (passError != null)
            return Result.failure(IllegalArgumentException(passError))

        clientDao.updatePassword(clientId, newPassword)
        return Result.success(Unit)
    }

    // ─────────────────────────────────────────────
    // 🟣 MASCOTAS
    // ─────────────────────────────────────────────

    suspend fun getPetById(petId: Long): PetEntity? {
        return petDao.getById(petId)
    }

    suspend fun addPet(
        ownerId: Long,
        nombre: String,
        especie: String,
        raza: String,
        fechaNacimiento: String? = null,
        peso: Double? = null,
        color: String? = null,
        notasMedicas: String? = null
    ): Result<Long> {
        val nombreError = validatePetName(nombre)
        val especieError = validateSpecies(especie)
        val razaError = validateBreed(raza)
        val fechaError = if (!fechaNacimiento.isNullOrBlank()) validateBirthDate(fechaNacimiento) else null
        val pesoError = if (peso != null) validateWeight(peso.toString()) else null
        val colorError = if (!color.isNullOrBlank()) validateColor(color) else null
        val notasError = if (!notasMedicas.isNullOrBlank()) validateMedicalNotes(notasMedicas) else null

        val errors = listOf(nombreError, especieError, razaError, fechaError, pesoError, colorError, notasError)
        val firstError = errors.firstOrNull { it != null }
        if (firstError != null)
            return Result.failure(IllegalArgumentException(firstError))

        val owner = clientDao.getClientById(ownerId)
        if (owner == null)
            return Result.failure(IllegalArgumentException("Cliente no encontrado"))

        val petId = petDao.insert(
            PetEntity(
                ownerId = ownerId,
                nombre = nombre,
                especie = especie,
                raza = raza,
                fechaNacimiento = fechaNacimiento,
                peso = peso,
                color = color,
                notasMedicas = notasMedicas
            )
        )
        return Result.success(petId)
    }

    fun getPetsByOwner(ownerId: Long): Flow<List<PetEntity>> {
        return petDao.getPetByOwnerId(ownerId)
    }

    suspend fun updatePetWeight(petId: Long, nuevoPeso: Double) {
        val pesoError = validateWeight(nuevoPeso.toString())
        if (pesoError != null)
            throw IllegalArgumentException(pesoError)
        petDao.updateWeight(petId, nuevoPeso)
    }

    suspend fun deletePet(petId: Long) {
        petDao.deleteById(petId)
    }

    suspend fun getPetCountByOwner(ownerId: Long): Int {
        return petDao.countByOwner(ownerId)
    }

    // ─────────────────────────────────────────────
    // 🟡 CITAS (Appointments)
    // ─────────────────────────────────────────────

    fun getAppointmentsByOwner(ownerId: Long): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAppointmentsByOwner(ownerId)
    }

    suspend fun addAppointment(
        ownerId: Long,
        petId: Long,
        date: Date,
        reason: String
    ): Result<Long> {
        val owner = clientDao.getClientById(ownerId)
        if (owner == null)
            return Result.failure(IllegalArgumentException("Cliente no encontrado"))

        val appointmentId = appointmentDao.insert(
            AppointmentEntity(
                ownerId = ownerId,
                petId = petId,
                date = date,
                reason = reason
            )
        )
        return Result.success(appointmentId)
    }
}
