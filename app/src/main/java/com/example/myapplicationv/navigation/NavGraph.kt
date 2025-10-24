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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle // 🆕 IMPORT CORRECTO
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplicationv.data.local.database.AppDatabase
import com.example.myapplicationv.data.repository.VetRepository
import com.example.myapplicationv.viewmodel.AuthViewModel
import com.example.myapplicationv.viewmodel.AuthViewModelFactory
import com.example.myapplicationv.data.local.storage.UserPreferences

import com.example.myapplicationv.ui.components.AppTopBar
import com.example.myapplicationv.ui.components.AppDrawer
import com.example.myapplicationv.ui.components.defaultDrawerItems

import com.example.myapplicationv.ui.screen.HomeScreen
import com.example.myapplicationv.ui.screen.LoginScreenVm
import com.example.myapplicationv.ui.screen.RegisterScreenVm



@Composable
fun AppNavGraph(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val vetRepository = remember {
        VetRepository(
            clientDao = database.clientDao(),
            petDao = database.petDao()
        )
    }
    val userPreferences = remember { UserPreferences.getInstance(context) }
    val viewModelFactory = remember {
        AuthViewModelFactory(vetRepository, userPreferences)
    }

    // ✅ SOLUCIÓN ALTERNATIVA: Crear el ViewModel una sola vez
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)

    // Helpers de navegación
    val goHome: () -> Unit = {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Home.path) { inclusive = true }
        }
    }

    val goLogin: () -> Unit = {
        navController.navigate(Route.Login.path)
    }
    val goRegister: () -> Unit = {
        navController.navigate(Route.Register.path)
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
            // ✅ Obtener estados aquí para el drawer
            val isLoggedIn by authViewModel.isUserLoggedIn.collectAsStateWithLifecycle()
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

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
                        if (!isLoggedIn) {
                            goLogin()
                        }
                    }
                )
            )
        }
    ) {
        // ✅ Obtener estados aquí para el Scaffold
        val isLoggedIn by authViewModel.isUserLoggedIn.collectAsStateWithLifecycle()
        val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
        val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                AppTopBar(
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onHome = goHome,
                    onMascotas = goMascotas,
                    onCitas = goCitas,
                    onLogin = goLogin,
                    isUserLoggedIn = isLoggedIn,
                    userName = currentUser.name
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Route.Home.path) {
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoMascotas = goMascotas,
                        onGoCitas = goCitas,
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Route.Home.path) {
                                popUpTo(Route.Home.path) { inclusive = true }
                            }
                        },
                        isUserLoggedIn = isLoggedIn,
                        userName = currentUser.name,
                        sessionMessage = if (sessionState.showMessage) {
                            sessionState.loginMessage ?: sessionState.logoutMessage
                        } else null,
                        onMessageShown = { authViewModel.clearSessionMessage() }
                    )
                }

                composable(Route.Login.path) {
                    LoginScreenVm(
                        vm = authViewModel, // ✅ Usar la misma instancia
                        onLoginOkNavigateHome = goHome,
                        onGoRegister = goRegister
                    )
                }

                composable(Route.Register.path) {
                    RegisterScreenVm(
                        vm = authViewModel, // ✅ Usar la misma instancia
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }

                // ... resto de pantallas
            }
        }
    }
}