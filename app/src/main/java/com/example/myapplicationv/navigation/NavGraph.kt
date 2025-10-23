package com.example.myapplicationv.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope

// IMPORTS PARA VIEWMODEl
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplicationv.data.local.database.AppDatabase
import com.example.myapplicationv.data.repository.VetRepository
import com.example.myapplicationv.viewmodel.AuthViewModel
import com.example.myapplicationv.viewmodel.AuthViewModelFactory

// IMPORTS DE COMPONENTES
import com.example.myapplicationv.ui.components.AppTopBar
import com.example.myapplicationv.ui.components.AppDrawer
import com.example.myapplicationv.ui.components.defaultDrawerItems

// IMPORTS DE PANTALLAS
import com.example.myapplicationv.ui.screen.HomeScreen
import com.example.myapplicationv.ui.screen.LoginScreenVm
import com.example.myapplicationv.ui.screen.RegisterScreenVm

@Composable
fun AppNavGraph(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // CREAR VIEWMODEL CON DEPENDENCIAS
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val vetRepository = VetRepository(
        clientDao = database.clientDao(),
        petDao = database.petDao()
    )
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(vetRepository)
    )

    // HELPERS DE NAVEGACIÓN MEJORADOS
    val goHome: () -> Unit = {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Home.path) { inclusive = true }
        }
    }

    val goLogin: () -> Unit = {
        navController.navigate(Route.Login.path) {
            // Limpiar back stack al ir a login
            popUpTo(Route.Home.path) { inclusive = false }
        }
    }

    val goRegister: () -> Unit = {
        navController.navigate(Route.Register.path) {
            popUpTo(Route.Login.path) { inclusive = false }
        }
    }

    val goMascotas: () -> Unit = {
        navController.navigate(Route.Mascotas.path)
    }

    val goCitas: () -> Unit = {
        navController.navigate(Route.Citas.path)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = null,
                items = defaultDrawerItems(
                    onHome = {
                        scope.launch { drawerState.close() }
                        goHome()
                    },
                    onMascotas = {
                        scope.launch { drawerState.close() }
                        goMascotas()
                    },
                    onCitas = {
                        scope.launch { drawerState.close() }
                        goCitas()
                    },
                    onLogin = {
                        scope.launch { drawerState.close() }
                        goLogin()
                    }
                )
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onHome = goHome,
                    onMascotas = goMascotas,
                    onCitas = goCitas,
                    onLogin = goLogin
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                // PANTALLA HOME
                composable(Route.Home.path) {
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoMascotas = goMascotas,
                        onGoCitas = goCitas
                    )
                }

                // 🆕 PANTALLA LOGIN
                composable(Route.Login.path) {
                    LoginScreenVm(
                        vm = authViewModel,
                        onLoginOkNavigateHome = goHome,
                        onGoRegister = goRegister
                    )
                }

                // 🆕 PANTALLA REGISTER
                composable(Route.Register.path) {
                    RegisterScreenVm(
                        vm = authViewModel,
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }

                // PANTALLAS TEMPORALES (para desarrollo)
                composable(Route.Mascotas.path) {
                    // Pantalla temporal de mascotas
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoMascotas = goMascotas,
                        onGoCitas = goCitas
                    )
                }

                composable(Route.Citas.path) {
                    // Pantalla temporal de citas
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoMascotas = goMascotas,
                        onGoCitas = goCitas
                    )
                }
            }
        }
    }
}