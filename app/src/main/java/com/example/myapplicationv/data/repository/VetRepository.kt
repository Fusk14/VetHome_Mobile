package com.example.myapplicationv.data.repository

import com.example.myapplicationv.data.local.user.ClientDao
import com.example.myapplicationv.data.local.user.ClientEntity
import com.example.myapplicationv.data.local.pet.PetDao
import com.example.myapplicationv.data.local.pet.PetEntity
import kotlinx.coroutines.flow.Flow

class VetRepository(
    private val clientDao: ClientDao,
    private val petDao: PetDao
) {

    //validacion del login
    suspend fun login(email: String, password: String): Result<ClientEntity> {
        val client = clientDao.getByEmail(email)                         // Busca cliente
        return if (client != null && client.password == password) {      // Verifica pass
            Result.success(client)
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas"))
        }
    }

    //valida que en el sistema no exista el email y crea el cliente retornando el id del cliente creado
    suspend fun register(
        name: String,
        email: String,
        phone: String,
        address: String? = null,
        emergencyContact: String? = null,
        password: String
    ): Result<Long> {
        val exists = clientDao.getByEmail(email) != null               // validamos si existe el email aqui
        if (exists) {
            return Result.failure(IllegalStateException("El correo ya está registrado"))
        }

        val id = clientDao.insert(                                     // Inserta nuevo cliente
            ClientEntity(
                name = name,
                email = email,
                phone = phone,
                address = address,
                emergencyContact = emergencyContact,
                password = password
            )
        )
        return Result.success(id)                                      // Devuelve ID generado
    }

    //esta funcion busca al cliente por su id retornando el cliente o un null
    suspend fun getClientById(clientId: Long): ClientEntity? {
        return clientDao.getById(clientId)
    }


    fun getAllClientsFlow(): Flow<List<ClientEntity>> {
        // Convertimos la función suspend a Flow para observar cambios en tiempo real
        return kotlinx.coroutines.flow.flow {
            emit(clientDao.getAll())
        }
    }

    //agregar una mascota
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
        // Verificar que el dueño existe
        val owner = clientDao.getById(ownerId)
        if (owner == null) {
            return Result.failure(IllegalArgumentException("Cliente no encontrado"))
        }

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

    //funcion para traer a todas las mascotas de un cliente
    suspend fun getPetsByOwner(ownerId: Long): List<PetEntity> {
        return petDao.getPetsByOwner(ownerId)
    }

    //observa los cambios de la mascota de un cliente
    fun getPetsByOwnerFlow(ownerId: Long): Flow<List<PetEntity>> {
        return kotlinx.coroutines.flow.flow {
            emit(petDao.getPetsByOwner(ownerId))
        }
    }

    //actualiza el peso
    suspend fun updatePetWeight(petId: Long, nuevoPeso: Double) {
        petDao.updateWeight(petId, nuevoPeso)  // ← CORREGIDO: newWeight → nuevoPeso
    }

    //elimina una mascota
    suspend fun deletePet(petId: Long) {
        petDao.delete(petId)
    }

    //cuenta la cantidad de mascotas que tiene un cliente
    suspend fun getPetCountByOwner(ownerId: Long): Int {
        return petDao.countByOwner(ownerId)
    }

    //esta funcion Cuenta el total de clientes registrados
    //devuelve el Número total de clientes
    suspend fun getTotalClientsCount(): Int {
        return clientDao.count()
    }

    // esta funcion cuenta total de mascotas registradas
    // el return devuelve el Número total de mascotas
    suspend fun getTotalPetsCount(): Int {
        // Para esta demo, contamos todas las mascotas
        return petDao.getPetsByOwner(1).size // Simplificado para demo
    }
}