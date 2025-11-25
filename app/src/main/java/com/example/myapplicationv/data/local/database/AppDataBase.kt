package com.example.myapplicationv.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplicationv.data.local.appointment.AppointmentDao
import com.example.myapplicationv.data.local.appointment.AppointmentEntity
import com.example.myapplicationv.data.local.user.ClientDao
import com.example.myapplicationv.data.local.user.ClientEntity
import com.example.myapplicationv.data.local.pet.PetDao
import com.example.myapplicationv.data.local.pet.PetEntity
import com.example.myapplicationv.data.local.resena.ResenaDao
import com.example.myapplicationv.data.local.resena.ResenaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.myapplicationv.data.local.appointment.Converters


@Database(
    entities = [
        ClientEntity::class,
        PetEntity::class,
        AppointmentEntity::class,
        ResenaEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun petDao(): PetDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun resenaDao(): ResenaDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DB_NAME = "vet_home.db"
        private val applicationScope = CoroutineScope(Dispatchers.IO)

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback() {

                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            applicationScope.launch {
                                INSTANCE?.let { populateDatabase(it) }
                            }
                        }

                    })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(db: AppDatabase) {

            val clientDao = db.clientDao()

            if (clientDao.count() == 0) {

                val clients = listOf(
                    ClientEntity(
                        id = 1L,
                        rut = "11.111.111-1",
                        nombre = "Admin",
                        apellido = "VetHome",
                        correo = "admin@vethome.cl",
                        telefono = "+56911111111",
                        contrasena = "Admin123!",
                        rolNombre = "ADMINISTRATIVO",  // ✅ CAMBIAR de "ADMIN" a "ADMINISTRATIVO"
                        address = "Av. Principal 123",
                        emergencyContact = "+56999999999"
                    ),
                    ClientEntity(
                        id = 2L,
                        rut = "22.222.222-2",
                        nombre = "María",
                        apellido = "González",
                        correo = "maria@vethome.cl",
                        telefono = "+56922222222",
                        contrasena = "Maria123!",
                        rolNombre = "CLIENTE",
                        address = "Calle Secundaria 456",
                        emergencyContact = "+56988888888"
                    )
                )

                clients.forEach { clientDao.insert(it) }
            }

                delay(100)
        }
    }
}
