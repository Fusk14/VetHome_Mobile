package com.example.myapplicationv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.myapplicationv.data.local.database.AppDatabase
import com.example.myapplicationv.data.local.storage.UserPreferences
import com.example.myapplicationv.data.repository.VetRepository
import com.example.myapplicationv.navigation.AppNavGraph
import com.example.myapplicationv.ui.theme.VetHomeTheme // Asegúrate de que el nombre del tema es correcto
import com.example.myapplicationv.viewmodel.AuthViewModel
import com.example.myapplicationv.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // ✅ LLamamos a la función Composable principal que contiene toda la lógica.
            VetHomeApp()
        }
    }
}

@Composable
fun VetHomeApp() {
    // ✅ CAMBIO 1: El tema ahora envuelve toda la lógica de inicialización.
    VetHomeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            val context = LocalContext.current

            // ✅ CAMBIO 2: Se crea la factoría y el ViewModel en el nivel más alto.
            // Esta es la parte que faltaba en tu código.
            val database = remember { AppDatabase.getInstance(context) }
            val vetRepository = remember {
                VetRepository(
                    clientDao = database.clientDao(),
                    petDao = database.petDao(),
                    appointmentDao = database.appointmentDao()
                )
            }
            val userPreferences = remember { UserPreferences.getInstance(context) }

            val viewModelFactory = remember {
                AuthViewModelFactory(vetRepository, userPreferences)
            }

            // Se crea la instancia ÚNICA del ViewModel.
            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)

            // ✅ CAMBIO 3: Se pasa la instancia única de authViewModel al NavGraph.
            AppNavGraph(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}
