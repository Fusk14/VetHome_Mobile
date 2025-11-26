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
import com.example.myapplicationv.ui.theme.VetHomeTheme
import com.example.myapplicationv.viewmodel.AuthViewModel
import com.example.myapplicationv.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VetHomeApp()
        }
    }
}

@Composable
fun VetHomeApp() {
    VetHomeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            val context = LocalContext.current

            val database = remember {
                AppDatabase.getInstance(context)
            }

            val vetRepository = remember {
                VetRepository(
                    clientDao = database.clientDao(),
                    petDao = database.petDao(),
                    appointmentDao = database.appointmentDao(),
                    resenaDao = database.resenaDao()
                )
            }

            val userPreferences = remember {
                UserPreferences.getInstance(context)
            }

            val viewModelFactory = remember {
                ViewModelFactory(vetRepository, userPreferences)
            }

            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)

            AppNavGraph(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}