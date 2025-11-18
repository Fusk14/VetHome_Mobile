package com.example.myapplicationv.data.local.database // Asegúrate de que el paquete sea correcto

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplicationv.data.local.appointment.AppointmentDao
import com.example.myapplicationv.data.local.appointment.AppointmentEntity
import com.example.myapplicationv.data.local.appointment.Converters // Suponiendo que Converters está en 'appointment'
import com.example.myapplicationv.data.local.user.ClientDao
import com.example.myapplicationv.data.local.user.ClientEntity
import com.example.myapplicationv.data.local.pet.PetDao
import com.example.myapplicationv.data.local.pet.PetEntity
import com.example.myapplicationv.data.local.resena.ResenaDao // 🆕 Suponiendo que ResenaDao está aquí
import com.example.myapplicationv.data.local.resena.ResenaEntity // 🆕 Suponiendo que ResenaEntity está aquí
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * DATABASE EXPLICACIÓN:
 * - @Database: Define la base de datos Room
 * - entities: Lista de TODAS las tablas que tendrá la base de datos
 * - version: Número de versión del esquema (incrementar cuando hagas cambios)
 */
@Database(
    entities = [
        ClientEntity::class,
        PetEntity::class,
        AppointmentEntity::class,
        ResenaEntity::class // ✅ NUEVO: Agregar ResenaEntity
    ],
    version = 5, // ✅ Versión incrementada (de 4 a 5)
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun petDao(): PetDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun resenaDao(): ResenaDao // ✅ NUEVO: Agregar el DAO para Reseñas

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "vet_home.db"

        // Scope para la precarga, usar un scope apropiado (Hilt/Koin) es mejor,
        // pero para este ejemplo usaremos un CoroutineScope básico.
        private val ApplicationScope = CoroutineScope(Dispatchers.IO)

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // ✅ Usar el callback con el ApplicationScope para la precarga
                    .addCallback(DatabaseCallback(ApplicationScope))
                    .fallbackToDestructiveMigration() // Manejo simple de migración
                    .build()

                INSTANCE = instance
                instance
            }
        }

        // ✅ Callback como clase interna estática para manejar la precarga
        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            // Se llama solo la primera vez que se crea la DB
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Lanzar la inserción de datos iniciales en el scope proporcionado (Dispatchers.IO)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(database)
                    }
                }
            }

            // Función suspendida que maneja la inserción de datos
            private suspend fun populateDatabase(database: AppDatabase) {
                val clientDao = database.clientDao()
                // petDao no es usado en el if(clientDao.count() == 0) pero se mantiene para claridad
                val petDao = database.petDao()

                // ✅ Solo insertar si está vacío
                if (clientDao.count() == 0) {
                    // Datos de prueba (Admin y María)
                    val clientsSeed = listOf(
                        ClientEntity(
                            name = "Admin VetHome",
                            email = "admin@vethome.cl",
                            phone = "+56911111111",
                            address = "Av. Principal 123",
                            emergencyContact = "+56999999999",
                            password = "Admin123!",
                            role = "admin"
                        ),
                        ClientEntity(
                            name = "María González",
                            email = "maria@vethome.cl",
                            phone = "+56922222222",
                            address = "Calle Secundaria 456",
                            emergencyContact = "+56988888888",
                            password = "Maria123!",
                            role = "client"
                        )
                    )

                    // Insertar clientes
                    clientsSeed.forEach { clientDao.insert(it) }

                    // ✅ Pausa breve para asegurar que la inserción de clientes ha terminado
                    // y los IDs (1 y 2) están disponibles para las mascotas.
                    delay(100)
                }
            }
        }
    }
}