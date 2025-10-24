package com.example.myapplicationv.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplicationv.data.local.database.AppDatabase
import com.example.myapplicationv.data.repository.VetRepository
import com.example.myapplicationv.viewmodel.AuthViewModel
import com.example.myapplicationv.viewmodel.AuthViewModelFactory
import com.example.myapplicationv.data.local.storage.UserPreferences

import androidx.compose.material3.ExperimentalMaterial3Api

import com.example.myapplicationv.ui.components.AppTopBar
import com.example.myapplicationv.ui.components.AppDrawer
import com.example.myapplicationv.ui.components.defaultDrawerItems

import com.example.myapplicationv.ui.screen.HomeScreen
import com.example.myapplicationv.ui.screen.LoginScreenVm
import com.example.myapplicationv.ui.screen.RegisterScreenVm
import com.example.myapplicationv.screen.PetListScreen
import com.example.myapplicationv.screen.AddPetScreen
import com.example.myapplicationv.screen.AppointmentsScreen
import com.example.myapplicationv.screen.AddAppointmentScreen


@OptIn(ExperimentalMaterial3Api::class)
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

    val goAddMascota: () -> Unit = {
        navController.navigate(Route.AddMascota.path)
    }

    val goCitas: () -> Unit = {
        navController.navigate(Route.Citas.path)
    }

    val goAddCita: () -> Unit = {
        navController.navigate(Route.AddCita.path)
    }

    val goBack: () -> Unit = {
        navController.popBackStack()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Obtener estados aquí para el drawer
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
                        if (isLoggedIn) {
                            goMascotas()
                        } else {
                            goLogin()
                        }
                    },
                    onCitas = {
                        scope.launch { drawerState.close() }
                        if (isLoggedIn) {
                            goCitas()
                        } else {
                            goLogin()
                        }
                    },
                    onLogin = {
                        scope.launch { drawerState.close() }
                        if (!isLoggedIn) {
                            goLogin()
                        } else {
                            goHome()
                        }
                    },
                    isUserLoggedIn = isLoggedIn
                )
            )
        }
    ) {
        // Obtener estados aquí para el Scaffold
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
                        vm = authViewModel,
                        onLoginOkNavigateHome = goHome,
                        onGoRegister = goRegister
                    )
                }

                composable(Route.Register.path) {
                    RegisterScreenVm(
                        vm = authViewModel,
                        onRegisteredNavigateLogin = {
                            navController.popBackStack()
                        },
                        onGoLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // PANTALLAS REALES DE MASCOTAS
                composable(Route.Mascotas.path) {
                    if (isLoggedIn) {
                        PetListScreen(
                            vm = authViewModel,
                            onBack = goBack,
                            onAddPet = goAddMascota,
                            onPetDetail = { petId ->
                                navController.navigate(Route.PetDetail.createRoute(petId))
                            }
                        )
                    } else {
                        LaunchedEffect(key1 = true) {
                            navController.navigate(Route.Login.path) {
                                popUpTo(Route.Mascotas.path) { inclusive = true }
                            }
                        }
                    }
                }

                composable(Route.AddMascota.path) {
                    if (isLoggedIn) {
                        AddPetScreen(
                            vm = authViewModel,
                            onBack = goBack,
                            onPetAdded = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        LaunchedEffect(key1 = true) {
                            navController.navigate(Route.Login.path) {
                                popUpTo(Route.AddMascota.path) { inclusive = true }
                            }
                        }
                    }
                }

                // PANTALLAS REALES DE CITAS
                composable(Route.Citas.path) {
                    if (isLoggedIn) {
                        AppointmentsScreen(
                            onBack = goBack,
                            onAddAppointment = goAddCita
                        )
                    } else {
                        LaunchedEffect(key1 = true) {
                            navController.navigate(Route.Login.path) {
                                popUpTo(Route.Citas.path) { inclusive = true }
                            }
                        }
                    }
                }

                composable(Route.AddCita.path) {
                    if (isLoggedIn) {
                        AddAppointmentScreen(
                            vm = authViewModel,
                            onBack = goBack,
                            onAppointmentAdded = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        LaunchedEffect(key1 = true) {
                            navController.navigate(Route.Login.path) {
                                popUpTo(Route.AddCita.path) { inclusive = true }
                            }
                        }
                    }
                }

                // Pantalla de detalle de mascota (placeholder por ahora)
                composable(Route.PetDetail.path) {
                    if (isLoggedIn) {
                        // El CenterAlignedTopAppBar está AQUÍ
                        Scaffold(
                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = { Text("Detalle de Mascota") },
                                    navigationIcon = {
                                        IconButton(onClick = goBack) {
                                            Icon(
                                                imageVector = Icons.Filled.ArrowBack,
                                                contentDescription = "Volver"
                                            )
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pantalla de detalle de mascota\n(En desarrollo)",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LaunchedEffect(key1 = true) {
                            navController.navigate(Route.Login.path) {
                                popUpTo(Route.PetDetail.path) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}