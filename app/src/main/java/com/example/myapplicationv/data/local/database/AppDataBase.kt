package com.example.myapplicationv.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplicationv.data.local.user.ClientDao
import com.example.myapplicationv.data.local.user.ClientEntity
import com.example.myapplicationv.data.local.pet.PetDao
import com.example.myapplicationv.data.local.pet.PetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * DATABASE EXPLICACIÓN:
 * - @Database: Define la base de datos Room
 * - entities: Lista de TODAS las tablas que tendrá la base de datos
 * - version: Número de versión del esquema (incrementar cuando hagas cambios)
 * - exportSchema: True para debugging (puedes ver el esquema SQL generado)
 */
@Database(
    entities = [ClientEntity::class, PetEntity::class],  // se agrega client y pet entity
    version = 1,
    exportSchema = true // Mantener true para ver el esquema SQL
)
abstract class AppDatabase : RoomDatabase() {

    // nuevos daos
    abstract fun clientDao(): ClientDao  // ← Cambiamos userDao por clientDao
    abstract fun petDao(): PetDao        // ← NUEVO DAO para mascotas

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "vet_home.db"

        // Obtiene la instancia única de la base (SINGLETON)
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Construimos la DB con callback de precarga
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // corrutina para insertar datos iniciales
                            CoroutineScope(Dispatchers.IO).launch {
                                val clientDao = getInstance(context).clientDao()
                                val petDao = getInstance(context).petDao()

                                // datos prueba de la veterinaria
                                val clientsSeed = listOf(
                                    ClientEntity(
                                        name = "Admin VetHome",
                                        email = "admin@vethome.cl",
                                        phone = "+56911111111",
                                        address = "Av. Principal 123",
                                        emergencyContact = "+56999999999",
                                        password = "Admin123!"
                                    ),
                                    ClientEntity(
                                        name = "María González",
                                        email = "maria@vethome.cl",
                                        phone = "+56922222222",
                                        address = "Calle Secundaria 456",
                                        emergencyContact = "+56988888888",
                                        password = "Maria123!"
                                    )
                                )

                                // mascotas pruebas
                                val petsSeed = listOf(
                                    PetEntity(
                                        ownerId = 1, // ID del admin se asigna autamaticamente
                                        name = "Firulais",
                                        species = "Perro",
                                        breed = "Labrador Retriever",
                                        birthDate = "2022-05-15",
                                        weight = 25.5,
                                        color = "Dorado",
                                        medicalNotes = "Vacunas al día. Alérgico a algunos granos."
                                    ),
                                    PetEntity(
                                        ownerId = 1,
                                        name = "Michi",
                                        species = "Gato",
                                        breed = "Siamés",
                                        birthDate = "2023-01-20",
                                        weight = 4.2,
                                        color = "Blanco y marrón",
                                        medicalNotes = "Castrado. Dieta especial para riñón."
                                    ),
                                    PetEntity(
                                        ownerId = 2, // ID del cliente
                                        name = "Toby",
                                        species = "Perro",
                                        breed = "Beagle",
                                        birthDate = "2021-11-10",
                                        weight = 12.0,
                                        color = "Tricolor",
                                        medicalNotes = "Energético. Necesita ejercicio diario."
                                    )
                                )

                                // solo insertamos si la tabla esta vacia
                                if (clientDao.count() == 0) {
                                    // insertar primero los clientes
                                    clientsSeed.forEach { clientDao.insert(it) }

                                    // pausa para asegurar que los clientes fueron insertados
                                    kotlinx.coroutines.delay(100)

                                   //insert de la mascota
                                    petsSeed.forEach { petDao.insert(it) }
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}