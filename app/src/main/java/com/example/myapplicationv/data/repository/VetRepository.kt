package com.example.myapplicationv.data.repository

import java.time.LocalDate
import java.time.Period
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.InputStream
import okhttp3.MediaType.Companion.toMediaType

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
import com.example.myapplicationv.domain.error.ErrorMessages
import retrofit2.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
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

    // FUNCIÓN LOGIN MODIFICADA CON FALLBACK (API -> LOCAL)
    suspend fun login(email: String, password: String): Result<ClientEntity> {
        val emailError = validateEmail(email)
        if (emailError != null)
            return Result.failure(IllegalArgumentException(emailError))

        return try {
            // Intentar directamente con microservicios
            println("🔍 Intentando login con microservicio para: $email")

            val loginRequest = LoginRequestDto(
                correo = email,
                contrasena = password
            )

            // 1. Hacer login en microservicio
            usuarioApi.login(loginRequest)

            // 2. Obtener datos del usuario
            val usuarioDto = usuarioApi.getUsuarioByCorreo(email)

            println("Login exitoso con microservicio: ${usuarioDto.correo}")

            // 3. Buscar si existe localmente para mantener datos adicionales
            val localClient = clientDao.getByEmail(email)

            val clientEntity = ClientEntity(
                id = usuarioDto.id ?: 0L,
                rut = usuarioDto.rut,
                nombre = usuarioDto.nombre,
                apellido = usuarioDto.apellido,
                correo = usuarioDto.correo,
                telefono = usuarioDto.telefono,
                contrasena = password, // Guardar la contraseña para login offline
                rolNombre = usuarioDto.rol?.nombre ?: "CLIENTE",
                address = localClient?.address, // Mantener datos locales si existen
                emergencyContact = localClient?.emergencyContact
            )


            clientDao.insert(clientEntity)

            Result.success(clientEntity)

        } catch (e: retrofit2.HttpException) {
            println("Falló microservicio (HTTP ${e.code()}): ${e.message}")
            // Intentar con base local como fallback
            println("🔍 Intentando login local para: $email")
            val client = clientDao.login(email, password)

            if (client != null) {
                println(" Login exitoso local: ${client.correo}")
                Result.success(client)
            } else {
                println(" Login fallido - Usuario no encontrado o credenciales incorrectas")
                Result.failure(IllegalArgumentException(ErrorMessages.getFriendlyMessage(e)))
            }
        } catch (e: Exception) {
            println("Falló microservicio: ${e.message}")

            // Intentar con base local
            println("🔍 Intentando login local para: $email")
            val client = clientDao.login(email, password)

            if (client != null) {
                println(" Login exitoso local: ${client.correo}")
                Result.success(client)
            } else {
                println(" Login fallido - Usuario no encontrado o credenciales incorrectas")
                Result.failure(IllegalArgumentException(ErrorMessages.getFriendlyMessage(e)))
            }
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

            val response = usuarioApi.register(registerRequest)
            
            if (response.isSuccessful) {
                val usuarioDto = response.body()
                if (usuarioDto == null) {
                    return Result.failure(IllegalStateException("No se recibió respuesta del servidor"))
                }

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
            } else {
                // Manejar errores HTTP
                val errorBody = response.errorBody()?.string() ?: ""
                val errorMessage = when (response.code()) {
                    400 -> {
                        // Intentar extraer el mensaje del servidor
                        when {
                            errorBody.contains("RUT", ignoreCase = true) -> 
                                "El RUT ya está registrado. Por favor, usa otro correo electrónico."
                            errorBody.contains("correo", ignoreCase = true) || errorBody.contains("email", ignoreCase = true) -> 
                                "El correo electrónico ya está registrado. Por favor, usa otro correo."
                            errorBody.contains("ya está registrado", ignoreCase = true) -> 
                                "Ya existe un usuario con estos datos. Por favor, verifica la información."
                            else -> 
                                ErrorMessages.getFriendlyMessage(retrofit2.HttpException(response))
                        }
                    }
                    else -> ErrorMessages.getFriendlyMessage(retrofit2.HttpException(response))
                }
                Result.failure(IllegalStateException(errorMessage))
            }

        } catch (e: retrofit2.HttpException) {
            Result.failure(IllegalStateException(ErrorMessages.getFriendlyMessage(e)))
        } catch (e: Exception) {
            Result.failure(IllegalStateException(ErrorMessages.getFriendlyMessage(e)))
        }
    }



    suspend fun isAdmin(clientId: Long): Boolean {
        val client = clientDao.getById(clientId)
        // Convertimos a mayúsculas para evitar errores de texto
        val role = client?.rolNombre?.uppercase() ?: ""
        // Se corrigió 'role' por 'rolNombre'
        return role == "ADMIN" || role == "ADMINISTRATIVO" || role == "ROLE_ADMIN"
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

    suspend fun changePassword(clientId: Long, currentPassword: String, newPassword: String): Result<Unit> {
        // Validar que la contraseña actual sea correcta
        val client = clientDao.getById(clientId)
        if (client == null) {
            return Result.failure(IllegalArgumentException("Usuario no encontrado"))
        }
        
        if (client.contrasena != currentPassword) {
            return Result.failure(IllegalArgumentException("La contraseña actual es incorrecta"))
        }
        
        val passError = validateStrongPassword(newPassword)
        if (passError != null)
            return Result.failure(IllegalArgumentException(passError))

        clientDao.updatePassword(clientId, newPassword)
        return Result.success(Unit)
    }
    
    suspend fun forgotPassword(email: String): Result<Unit> {
        val emailError = validateEmail(email)
        if (emailError != null)
            return Result.failure(IllegalArgumentException(emailError))
        
        return try {
            val request = ForgotPasswordRequestDto(correo = email)
            val response = usuarioApi.forgotPassword(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(IllegalStateException("Error al solicitar recuperación: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
        notasMedicas: String?,
        fotoUri: String? = null
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
    
    suspend fun uploadPetPhotoBytes(petId: Long, imageBytes: ByteArray): Result<Unit> {
        return try {
            Log.d("VetRepository", "📤 Subiendo foto para mascota ID: $petId, tamaño: ${imageBytes.size} bytes")
            val mediaType = "image/jpeg".toMediaType()
            val requestFile = okhttp3.RequestBody.create(mediaType, imageBytes)
            val fotoPart = okhttp3.MultipartBody.Part.createFormData("foto", "pet_photo.jpg", requestFile)
            
            val response = mascotaApi.uploadPetPhoto(petId, fotoPart)
            Log.d("VetRepository", "📥 Respuesta del servidor: código ${response.code()}, éxito: ${response.isSuccessful}")
            if (response.isSuccessful) {
                Log.d("VetRepository", "✅ Foto subida exitosamente para mascota ID: $petId")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                Log.e("VetRepository", "❌ Error al subir foto: ${response.code()} - $errorBody")
                Result.failure(IllegalStateException("Error al subir foto: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("VetRepository", "❌ Excepción al subir foto: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getPetPhotoUrl(petId: Long): String? {
        return try {
            // Construir URL para obtener la foto
            val baseUrl = RemoteModule.baseUrlFor(RemoteModule.Microservice.MASCOTAS)
            "$baseUrl/api/mascotas/$petId/foto"
        } catch (e: Exception) {
            null
        }
    }


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

    // FUNCIÓN PARA BUSCAR VETERINARIO VÁLIDO
    // Busca en la base de datos local primero, luego en el servidor si es necesario
    private suspend fun obtenerVeterinarioValido(): Long {
        // Primero intentar buscar en la base de datos local
        try {
            val todosLosClientes = clientDao.getAllClients()
            val clientes = todosLosClientes.first()
            
            val veterinarioLocal = clientes.firstOrNull { 
                it.rolNombre.equals("VETERINARIO", ignoreCase = true) 
            }
            
            if (veterinarioLocal != null) {
                Log.d("VetRepository", "✅ Veterinario encontrado localmente: ID ${veterinarioLocal.id}")
                return veterinarioLocal.id
            }
        } catch (e: Exception) {
            Log.w("VetRepository", "Error buscando veterinario localmente: ${e.message}")
        }
        
        // Si no se encuentra localmente, buscar en el servidor
        try {
            Log.d("VetRepository", "🔍 Buscando veterinario en el servidor...")
            val usuarios = usuarioApi.getAllUsuarios()
            
            val veterinario = usuarios.firstOrNull { usuario ->
                usuario.rol?.nombre?.equals("VETERINARIO", ignoreCase = true) == true
            }
            
            if (veterinario != null && veterinario.id != null) {
                Log.d("VetRepository", "✅ Veterinario encontrado en servidor: ID ${veterinario.id}")
                return veterinario.id!!
            }
        } catch (e: Exception) {
            Log.e("VetRepository", "❌ Error buscando veterinario en servidor: ${e.message}")
        }
        
        // Fallback: intentar con IDs comunes que podrían ser veterinarios
        val posiblesVeterinarios = listOf(1L, 2L, 3L, 4L, 5L, 10L)
        for (id in posiblesVeterinarios) {
            try {
                val usuario = usuarioApi.getUsuarioById(id)
                if (usuario.rol?.nombre?.equals("VETERINARIO", ignoreCase = true) == true) {
                    Log.d("VetRepository", "✅ Veterinario encontrado por ID: $id")
                    return id
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        // Si no se encuentra ningún veterinario, usar un ID por defecto y dejar que el servidor valide
        Log.w("VetRepository", "⚠️ No se encontró veterinario válido, usando ID por defecto: 1L")
        return 1L
    }

    // FUNCIÓN CREAR RESEÑA MODIFICADA
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

        // Nueva validación de longitud máxima
        if (comentario.length > 500)
            return Result.failure(IllegalArgumentException("El comentario es demasiado largo"))

        val veterinarioId = obtenerVeterinarioValido()
        
        return try {
            val dto = ResenaDto(
                idCliente = usuarioId,
                idVeterinario = veterinarioId,
                calificacion = calificacion,
                comentario = comentario
            )

            Log.d("VetRepository", "📤 Enviando reseña al servidor: Cliente=$usuarioId, Veterinario=$veterinarioId")

            val creada = resenaApi.createResena(dto)
            Log.d("VetRepository", "✅ Reseña creada exitosamente en servidor: ${creada.id}")

            val entity = ResenaEntity(
                id = creada.id ?: 0L,
                idCliente = creada.idCliente,
                idVeterinario = creada.idVeterinario,
                calificacion = creada.calificacion,
                comentario = creada.comentario,
                mascotaId = mascotaId,
                mascotaNombre = mascotaNombre,
                fecha = fecha,
                sincronizado = true // MARCADA COMO SINCRONIZADA
            )

            resenaDao.insertar(entity)
            Result.success(entity.id)

        } catch (e: retrofit2.HttpException) {
            val errorBody = try {
                e.response()?.errorBody()?.string() ?: e.message
            } catch (ex: Exception) {
                e.message ?: "Error desconocido"
            }
            Log.e("VetRepository", "❌ Error HTTP al crear reseña: ${e.code()} - $errorBody")
            
            // Si es un error 400, puede ser un problema de validación (roles, etc.)
            if (e.code() == 400) {
                val friendlyMessage = ErrorMessages.getFriendlyMessage(e)
                val errorBodyStr = errorBody ?: ""
                val finalMessage = if (errorBodyStr.isNotBlank() && !errorBodyStr.contains(friendlyMessage)) {
                    "$friendlyMessage: $errorBodyStr"
                } else {
                    friendlyMessage
                }
                return Result.failure(IllegalArgumentException(finalMessage))
            }
            
            // Guardar localmente como no sincronizada (FALLBACK)
            val localId = resenaDao.insertar(
                ResenaEntity(
                    idCliente = usuarioId,
                    idVeterinario = veterinarioId,
                    mascotaId = mascotaId,
                    mascotaNombre = mascotaNombre,
                    calificacion = calificacion,
                    comentario = comentario,
                    fecha = fecha,
                    sincronizado = false // ❌ NO SINCRONIZADA
                )
            )
            Log.d("VetRepository", "Reseña guardada localmente con ID: $localId")
            Result.success(localId)
        } catch (e: Exception) {
            Log.e("VetRepository", "❌ Error inesperado al crear reseña: ${e.message}", e)

            // Guardar localmente como no sincronizada (FALLBACK)
            val localId = resenaDao.insertar(
                ResenaEntity(
                    idCliente = usuarioId,
                    idVeterinario = veterinarioId,
                    mascotaId = mascotaId,
                    mascotaNombre = mascotaNombre,
                    calificacion = calificacion,
                    comentario = comentario,
                    fecha = fecha,
                    sincronizado = false // ❌ NO SINCRONIZADA
                )
            )
            Log.d("VetRepository", "Reseña guardada localmente con ID: $localId")
            Result.success(localId)
        }
    }

    // UNCIÓN DE SINCRONIZACIÓN DE RESEÑAS PENDIENTES
    suspend fun sincronizarResenasPendientes() {
        try {
            // Asume que resenaDao.obtenerNoSincronizadas() existe y devuelve List<ResenaEntity>
            val resenasPendientes = resenaDao.obtenerNoSincronizadas()
            println("Sincronizando ${resenasPendientes.size} reseñas pendientes...")

            for (resenaLocal in resenasPendientes) {
                try {
                    // USAR UN VETERINARIO VÁLIDO para las pendientes también
                    val veterinarioValido = obtenerVeterinarioValido() // Usamos la función ya definida

                    val dto = ResenaDto(
                        idCliente = resenaLocal.idCliente,
                        idVeterinario = veterinarioValido,
                        calificacion = resenaLocal.calificacion,
                        comentario = resenaLocal.comentario
                    )

                    println("📤 Sincronizando reseña local ID: ${resenaLocal.id}")
                    val resenaRemota = resenaApi.createResena(dto)

                    // Actualizar la reseña local con el ID del servidor
                    // Asume que resenaDao.actualizar(ResenaEntity) existe
                    resenaDao.actualizar(
                        resenaLocal.copy(
                            id = resenaRemota.id ?: resenaLocal.id,
                            idVeterinario = veterinarioValido,
                            sincronizado = true
                        )
                    )
                    println("Reseña ${resenaLocal.id} sincronizada exitosamente")

                } catch (e: Exception) {
                    println(" Error sincronizando reseña ${resenaLocal.id}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println(" Error en sincronización general: ${e.message}")
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