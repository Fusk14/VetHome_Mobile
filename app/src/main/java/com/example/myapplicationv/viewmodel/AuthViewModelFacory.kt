///package com.example.myapplicationv.viewmodel

//import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplicationv.data.repository.VetRepository

import com.example.myapplicationv.data.local.storage.UserPreferences

//class AuthViewModelFactory(
//    private val repository: VetRepository,
//    private val userPreferences: UserPreferences // Requerir UserPreferences en el constructor
//) : ViewModelProvider.Factory {

  //  @Suppress("UNCHECKED_CAST")
    //override fun <T : ViewModel> create(modelClass: Class<T>): T {
      ///  // Pasamos ambas dependencias al constructor de AuthViewModel
        //if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
          //  return AuthViewModel(repository, userPreferences) as T
        //}
        // Si piden otra clase, lanzamos error descriptivo.
       // throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
//    }
//}