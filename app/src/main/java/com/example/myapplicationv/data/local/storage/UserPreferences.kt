package com.example.myapplicationv.data.local.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension para manipular el DataStore
val Context.dataStore by preferencesDataStore("vet_home_prefs")

//Constructor hecho PRIVADO para forzar el uso del getInstance.
class UserPreferences private constructor(private val context: Context) {

    // Keys para guardar datos en DataStore
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userNameKey = stringPreferencesKey("user_name")
    private val userIdKey = stringPreferencesKey("user_id")

    // ==================== FUNCIONES PARA GUARDAR DATOS ====================

    suspend fun setLoggedIn(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[isLoggedInKey] = value
        }
    }

    suspend fun setUserInfo(email: String, name: String, id: String) {
        context.dataStore.edit { prefs ->
            prefs[userEmailKey] = email
            prefs[userNameKey] = name
            prefs[userIdKey] = id
        }
    }

    suspend fun clearUserData() {
        context.dataStore.edit { prefs ->
            prefs.remove(isLoggedInKey)
            prefs.remove(userEmailKey)
            prefs.remove(userNameKey)
            prefs.remove(userIdKey)
        }
    }

    // ==================== FLOWS PARA LEER DATOS ====================

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { prefs ->
            prefs[isLoggedInKey] ?: false
        }

    val userEmail: Flow<String> = context.dataStore.data
        .map { prefs ->
            prefs[userEmailKey] ?: ""
        }

    val userName: Flow<String> = context.dataStore.data
        .map { prefs ->
            prefs[userNameKey] ?: ""
        }

    val userId: Flow<String> = context.dataStore.data
        .map { prefs ->
            prefs[userIdKey] ?: ""
        }

    // 🆕 CAMBIO CLAVE: Implementación correcta del Singleton
    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                // Usamos applicationContext para evitar fugas de memoria
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}