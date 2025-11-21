package com.example.myapplicationv.data.repository

import java.time.LocalDate
import java.time.Period

import com.example.myapplicationv.data.local.appointment.AppointmentDao
import com.example.myapplicationv.data.local.appointment.AppointmentEntity
import com.example.myapplicationv.data.local.user.ClientDao
import com.example.myapplicationv.data.local.user.ClientEntity
import com.example.myapplicationv.data.local.pet.PetDao
import com.example.myapplicationv.data.local.pet.PetEntity
import com.example.myapplicationv.data.local.resena.ResenaDao
import com.example.myapplicationv.data.local.resena.ResenaEntity
import com.example.myapplicationv.data.remote.RemoteModule
import com.example.myapplicationv.data.remote.*
import com.example.myapplicationv.data.remote.dto.*
import com.example.myapplicationv.domain.validation.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

class VetRepository(
    private val clientDao: ClientDao,
    private val petDao: PetDao,
    private val appointmentDao: AppointmentDao,
    private val resenaDao: ResenaDao
) {

    private val usuarioApi = RemoteModule.usuarioApi
    private val mascotaApi = RemoteModule.mascotaApi
    private val consultaApi = RemoteModule.consultaApi
    private val resenaApi = RemoteModule.resenaApi


    //login, registro, admin

    suspend fun login(email: String, password: String): Result<ClientEntity> {
        val emailError = validateEmail(email)
        if (emailError != null)
            return Result.failure(IllegalArgumentException(emailError))

        return try {
            val localClient = clientDao.getByEmail(email)
                ?: return Result.failure(IllegalArgumentException("Usuario no encontrado. Por favor regístrese primero."))

            val loginRequest = LoginRequestDto(
                correo = email,
                contrasena = password
            )

            usuarioApi.login(loginRequest)

            val usuarioDto = usuarioApi.getUsuarioByCorreo(email)

            val clientEntity = ClientEntity(
                id = usuarioDto.id ?: 0L,
                rut = usuarioDto.rut,
                nombre = usuarioDto.nombre,
                apellido = usuarioDto.apellido,
                correo = usuarioDto.correo,
                telefono = usuarioDto.telefono,
                contrasena = password,
                rolNombre = usuarioDto.rol?.nombre ?: "CLIENTE",
                address = localClient.address,
                emergencyContact = localClient.emergencyContact
            )

            clientDao.insert(clientEntity)
            Result.success(clientEntity)

        } catch (e: Exception) {
            val client = clientDao.getByEmail(email)
            if (client != null && client.password == password)
                Result.success(client)
            else
                Result.failure(IllegalArgumentException("Error de conexión: ${e.message}"))
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

        val firstError = listOf(nameError, emailError, phoneError, addressError, emergencyContactError, passwordError)
            .firstOrNull { it != null }

        if (firstError != null)
            return Result.failure(IllegalArgumentException(firstError))

        return try {
            val (nombre, apellido) = name.trim().split(" ", limit = 2).let {
                it[0] to it.getOrNull(1).orEmpty()
            }

            val rut = email.substringBefore("@")

            val registerRequest = RegisterRequestDto(
                rut = rut,
                nombre = nombre,
                apellido = apellido,
                correo = email,
                telefono = phone,
                contrasena = password,
                rolNombre = "CLIENTE"
            )

            val usuarioDto = usuarioApi.register(registerRequest)

            val clientEntity = ClientEntity(
                id = usuarioDto.id ?: 0L,
                rut = usuarioDto.rut,
                nombre = usuarioDto.nombre,
                apellido = usuarioDto.apellido,
                correo = usuarioDto.correo,
                telefono = usuarioDto.telefono,
                contrasena = password,
                rolNombre = usuarioDto.rol?.nombre ?: "CLIENTE",
                address = address,
                emergencyContact = emergencyContact
            )

            clientDao.insert(clientEntity)
            Result.success(clientEntity.id)

        } catch (e: Exception) {
            Result.failure(IllegalStateException("Error de conexión: ${e.message}"))
        }
    }

    suspend fun isAdmin(clientId: Long): Boolean {
        val client = clientDao.getById(clientId)
        return client?.role == "admin"
    }

    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAllClients()

    suspend fun getClientById(clientId: Long): ClientEntity? = clientDao.getById(clientId)

    suspend fun deleteUserAndData(clientId: Long) {
        appointmentDao.deleteByOwnerId(clientId)
        petDao.deleteByOwnerId(clientId)
        clientDao.deleteById(clientId)
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

    //mascotas

    fun getAllPets(): Flow<List<PetEntity>> = petDao.getAllPets()

    suspend fun getPetById(petId: Long): PetEntity? = petDao.getById(petId)

    suspend fun addPet(
        ownerId: Long,
        nombre: String,
        especie: String,
        raza: String,
        fechaNacimiento: String?,
        peso: Double?,
        color: String?,
        notasMedicas: String?
    ): Result<Long> {

        val nombreError = validatePetName(nombre)
        val especieError = validateSpecies(especie)
        val razaError = validateBreed(raza)
        val fechaError = fechaNacimiento?.let { validateBirthDate(it) }
        val pesoError = peso?.let { validateWeight(it.toString()) }
        val colorError = color?.let { validateColor(it) }
        val notasError = notasMedicas?.let { validateMedicalNotes(it) }

        val firstError = listOf(nombreError, especieError, razaError, fechaError, pesoError, colorError, notasError)
            .firstOrNull { it != null }

        if (firstError != null)
            return Result.failure(IllegalArgumentException(firstError))

        val owner = clientDao.getById(ownerId)
            ?: return Result.failure(IllegalArgumentException("Cliente no encontrado"))

        val edad = fechaNacimiento?.let {
            try {
                val birth = java.time.LocalDate.parse(it)
                java.time.Period.between(birth, java.time.LocalDate.now()).years
            } catch (_: Exception) {
                0
            }
        } ?: 0

        return try {
            val mascotaDto = MascotaDto(
                idCliente = ownerId,
                nombre = nombre,
                especie = especie,
                raza = raza,
                edad = edad
            )

            val creado = mascotaApi.createMascota(mascotaDto)

            val petEntity = PetEntity(
                id = creado.id ?: 0L,
                idCliente = creado.idCliente,
                nombre = creado.nombre,
                especie = creado.especie,
                raza = creado.raza,
                edad = creado.edad,
                fechaNacimiento = fechaNacimiento,
                peso = peso,
                color = color,
                notasMedicas = notasMedicas
            )

            petDao.insert(petEntity)
            Result.success(petEntity.id)

        } catch (_: Exception) {
            val localId = petDao.insert(
                PetEntity(
                    idCliente = ownerId,
                    nombre = nombre,
                    especie = especie,
                    raza = raza,
                    edad = edad,
                    fechaNacimiento = fechaNacimiento,
                    peso = peso,
                    color = color,
                    notasMedicas = notasMedicas
                )
            )
            Result.success(localId)
        }
    }

    fun getPetsByOwner(ownerId: Long): Flow<List<PetEntity>> = petDao.getPetByOwnerId(ownerId)

    suspend fun updatePetWeight(petId: Long, nuevoPeso: Double) {
        val err = validateWeight(nuevoPeso.toString())
        if (err != null) throw IllegalArgumentException(err)
        petDao.updateWeight(petId, nuevoPeso)
    }

    suspend fun deletePet(petId: Long) = petDao.deleteById(petId)

    suspend fun getPetCountByOwner(ownerId: Long): Int = petDao.countByOwner(ownerId)


    //consultas/appointments
    fun getAllAppointments(): Flow<List<AppointmentEntity>> = appointmentDao.getAllAppointments()

    fun getAppointmentsByOwner(ownerId: Long): Flow<List<AppointmentEntity>> = appointmentDao.getAppointmentsByOwner(ownerId)

    suspend fun deleteAppointmentById(appointmentId: Long) =
        appointmentDao.deleteAppointmentById(appointmentId)

    suspend fun addAppointment(
        ownerId: Long,
        petId: Long,
        date: Date,
        reason: String
    ): Result<Long> {

        val owner = clientDao.getById(ownerId)
            ?: return Result.failure(IllegalArgumentException("Cliente no encontrado"))

        // Convertir Date → "yyyy-MM-dd"
        val fechaStr = java.time.Instant.ofEpochMilli(date.time)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .toString()

        return try {
            // DTO para API
            val consultaDto = ConsultaDto(
                idMascota = petId,
                idVeterinario = ownerId,
                idCliente = ownerId,
                fecha = fechaStr,
                motivo = reason
            )

            val creada = consultaApi.createConsulta(consultaDto)

            // Convertir fecha STRING → Date
            val fechaDate = try {
                java.time.LocalDate.parse(creada.fecha).let {
                    Date.from(it.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                }
            } catch (_: Exception) {
                date
            }

            // ENTIDAD CORRECTA — con motivo y reason sincronizados
            val entity = AppointmentEntity(
                id = creada.id ?: 0L,
                idMascota = creada.idMascota,
                idVeterinario = creada.idVeterinario,
                idCliente = creada.idCliente,
                fecha = creada.fecha,          // string desde backend
                motivo = creada.motivo,        // motivo desde backend
                diagnostico = creada.diagnostico,
                tratamiento = creada.tratamiento,
                date = fechaDate,              // date convertido
                reason = creada.motivo         // sincronizar reason con motivo
            )

            appointmentDao.insert(entity)
            Result.success(entity.id)

        } catch (_: Exception) {

            // GUARDADO OFFLINE — con motivo y reason sincronizados
            val localId = appointmentDao.insert(
                AppointmentEntity(
                    idCliente = ownerId,
                    idMascota = petId,
                    idVeterinario = ownerId,
                    fecha = fechaStr,
                    motivo = reason,
                    reason = reason,  // sincronizar reason con motivo
                    date = date
                )
            )

            Result.success(localId)
        }
    }


    //reseñas

    suspend fun crearResena(
        usuarioId: Long,
        mascotaId: Long,
        mascotaNombre: String,
        calificacion: Int,
        comentario: String,
        fecha: String
    ): Result<Long> {

        if (calificacion !in 1..5)
            return Result.failure(IllegalArgumentException("La calificación debe ser entre 1 y 5"))

        if (comentario.isBlank())
            return Result.failure(IllegalArgumentException("El comentario no puede estar vacío"))

        if (comentario.length > 500)
            return Result.failure(IllegalArgumentException("El comentario es demasiado largo"))

        return try {
            val dto = ResenaDto(
                idCliente = usuarioId,
                idVeterinario = usuarioId,
                calificacion = calificacion,
                comentario = comentario
            )

            val creada = resenaApi.createResena(dto)

            val entity = ResenaEntity(
                id = creada.id ?: 0L,
                idCliente = creada.idCliente,
                idVeterinario = creada.idVeterinario,
                calificacion = creada.calificacion,
                comentario = creada.comentario,
                mascotaId = mascotaId,
                mascotaNombre = mascotaNombre,
                fecha = fecha
            )

            resenaDao.insertar(entity)
            Result.success(entity.id)

        } catch (_: Exception) {
            val localId = resenaDao.insertar(
                ResenaEntity(
                    idCliente = usuarioId,
                    idVeterinario = usuarioId,
                    mascotaId = mascotaId,
                    mascotaNombre = mascotaNombre,
                    calificacion = calificacion,
                    comentario = comentario,
                    fecha = fecha
                )
            )
            Result.success(localId)
        }
    }

    fun obtenerResenasPorUsuario(usuarioId: Long): Flow<List<ResenaEntity>> =
        resenaDao.obtenerPorUsuario(usuarioId)

    suspend fun obtenerResenaPorId(id: Long): ResenaEntity? =
        resenaDao.obtenerPorId(id)

    suspend fun eliminarResena(id: Long) =
        resenaDao.eliminarPorId(id)

    suspend fun obtenerPromedioCalificacionMascota(mascotaId: Long): Double? =
        resenaDao.obtenerPromedioCalificacion(mascotaId)
}
