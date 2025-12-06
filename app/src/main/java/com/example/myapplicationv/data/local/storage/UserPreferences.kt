package com.example.myapplicationv.data.local.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//  Delegate de DataStore (debe estar fuera de la clase)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    // Preference keys
    private val USER_EMAIL = stringPreferencesKey("user_email")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_ID = stringPreferencesKey("user_id")
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_ROLE = stringPreferencesKey("user_role")

    // Flows para observar los valores
    val userEmail: Flow<String> = dataStore.data
        .map { preferences -> preferences[USER_EMAIL] ?: "" }

    val userName: Flow<String> = dataStore.data
        .map { preferences -> preferences[USER_NAME] ?: "" }

    val userId: Flow<String> = dataStore.data
        .map { preferences -> preferences[USER_ID] ?: "" }

    val isLoggedIn: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[IS_LOGGED_IN] ?: false }

    val userRole: Flow<String> = dataStore.data
        .map { preferences -> preferences[USER_ROLE] ?: "client" }

    // Funciones para guardar datos
    suspend fun setUserInfo(email: String, name: String, userId: String, role: String = "client") {
        dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
            preferences[USER_NAME] = name
            preferences[USER_ID] = userId
            preferences[USER_ROLE] = role
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = loggedIn
        }
    }

    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null

        // Función getInstance corregida
        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreferences(context.dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}