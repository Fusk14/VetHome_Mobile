package com.example.myapplicationv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplicationv.data.repository.VetRepository
import com.example.myapplicationv.data.local.storage.UserPreferences

// Factory simple para crear todos los ViewModels con sus dependencias.
class ViewModelFactory(
    private val repository: VetRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(repository, userPreferences) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}



