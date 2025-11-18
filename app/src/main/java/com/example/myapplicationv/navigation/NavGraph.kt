package com.example.myapplicationv.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplicationv.viewmodel.AuthViewModel
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
import com.example.myapplicationv.screen.PetDetailScreen
import com.example.myapplicationv.screen.ProfileScreen
import com.example.myapplicationv.screen.AdminDashboard
import com.example.myapplicationv.screen.EditUserScreen
import com.example.myapplicationv.screen.ResenasScreen
import com.example.myapplicationv.screen.AddResenaScreen
import com.example.myapplicationv.screen.ResenaDetailScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- Funciones de navegación ---
    val goHome: () -> Unit = {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Home.path) { inclusive = true }
        }
    }
    val goLogin: () -> Unit = { navController.navigate(Route.Login.path) }
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) }
    val goMascotas: () -> Unit = { navController.navigate(Route.Mascotas.path) }
    val goAddMascota: () -> Unit = { navController.navigate(Route.AddMascota.path) }
    val goCitas: () -> Unit = { navController.navigate(Route.Citas.path) }
    val goAddCita: () -> Unit = { navController.navigate(Route.AddCita.path) }
    val goBack: () -> Unit = { navController.popBackStack() }
    val goAdminDashboard: () -> Unit = { navController.navigate(Route.AdminDashboard.path) }
    val goProfile: () -> Unit = { navController.navigate(Route.Perfil.path) }

    // ✅ ACTUALIZADO: Rutas para Reseñas
    val goResenas: () -> Unit = { navController.navigate(Route.Resenas.path) }
    val goAddResena: () -> Unit = { navController.navigate(Route.AddResena.path) }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val isLoggedIn by authViewModel.isUserLoggedIn.collectAsStateWithLifecycle()

            AppDrawer(
                currentRoute = null,
                items = defaultDrawerItems(
                    onHome = {
                        scope.launch { drawerState.close() }
                        goHome()
                    },
                    onMascotas = {
                        scope.launch { drawerState.close() }
                        if (isLoggedIn) goMascotas() else goLogin()
                    },
                    onCitas = {
                        scope.launch { drawerState.close() }
                        if (isLoggedIn) goCitas() else goLogin()
                    },
                    // ✅ ACTUALIZADO: Navegación a Reseñas en Drawer
                    onResenas = {
                        scope.launch { drawerState.close() }
                        if (isLoggedIn) goResenas() else goLogin()
                    },
                    onLogin = {
                        scope.launch { drawerState.close() }
                        if (!isLoggedIn) goLogin() else goHome()
                    },
                    onProfile = {
                        scope.launch { drawerState.close() }
                        if (isLoggedIn) goProfile() else goLogin()
                    },
                    onLogout = {
                        scope.launch {
                            drawerState.close()
                            authViewModel.logout()
                            goHome()
                        }
                    },
                    isUserLoggedIn = isLoggedIn
                )
            )
        }
    ) {
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
                    onResenas = goResenas, // ✅ NUEVO: Pasar la función goResenas al TopBar
                    onLogin = goLogin,
                    isUserLoggedIn = isLoggedIn,
                    userName = currentUser.name,
                    onProfile = { if (isLoggedIn) goProfile() else goLogin() }
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
                            goHome()
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
                        onLoginOkNavigate = { isAdmin ->
                            if (isAdmin) {
                                goAdminDashboard()
                            } else {
                                goHome()
                            }
                        },
                        onGoRegister = goRegister
                    )
                }

                composable(Route.Register.path) {
                    RegisterScreenVm(
                        vm = authViewModel,
                        onRegisteredNavigateLogin = goBack,
                        onGoLogin = goBack
                    )
                }

                composable(Route.AdminDashboard.path) {
                    if (isLoggedIn) {
                        AdminDashboard(viewModel = authViewModel, navController = navController)
                    } else {
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }

                composable(
                    route = Route.EditUser.path,
                    arguments = listOf(navArgument("userId") { type = NavType.LongType })
                ) { backStackEntry ->
                    if (isLoggedIn) {
                        val userId = backStackEntry.arguments?.getLong("userId")
                        if (userId != null) {
                            EditUserScreen(
                                userId = userId,
                                viewModel = authViewModel,
                                onUserUpdated = goBack
                            )
                        }
                    } else {
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }

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
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }

                composable(Route.AddMascota.path) {
                    if (isLoggedIn) {
                        AddPetScreen(
                            vm = authViewModel,
                            onBack = goBack,
                            onPetAdded = goBack
                        )
                    } else {
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }

                composable(Route.Citas.path) {
                    if (isLoggedIn) {
                        AppointmentsScreen(
                            vm = authViewModel,
                            onBack = goBack,
                            onAddAppointment = goAddCita
                        )
                    } else {
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }

                composable(Route.AddCita.path) {
                    if (isLoggedIn) {
                        AddAppointmentScreen(
                            vm = authViewModel,
                            onBack = goBack,
                            onAppointmentAdded = goBack
                        )
                    } else {
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }

                composable(
                    route = Route.PetDetail.path,
                    arguments = listOf(navArgument("petId") { type = NavType.LongType })
                ) { backStackEntry ->
                    if (isLoggedIn) {
                        val petId = backStackEntry.arguments?.getLong("petId")
                        if (petId != null) {
                            PetDetailScreen(
                                vm = authViewModel,
                                petId = petId,
                                onBack = goBack
                            )
                        }
                    } else {
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }

                composable(Route.Perfil.path) {
                    if (isLoggedIn) {
                        ProfileScreen(
                            vm = authViewModel,
                            onBack = goBack
                        )
                    } else {
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }

                // 🆕 PANTALLA DE LISTADO DE RESEÑAS
                composable(Route.Resenas.path) {
                    if (isLoggedIn) {
                        ResenasScreen(
                            vm = authViewModel,
                            onBack = goBack,
                            onAddResena = goAddResena,
                            onResenaDetail = { resenaId ->
                                navController.navigate(Route.ResenaDetail.createRoute(resenaId))
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }

                // 🆕 PANTALLA PARA AGREGAR RESEÑA
                composable(Route.AddResena.path) {
                    if (isLoggedIn) {
                        AddResenaScreen(
                            vm = authViewModel,
                            onBack = goBack,
                            onResenaAdded = goBack
                        )
                    } else {
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }

                // 🆕 PANTALLA DE DETALLE DE RESEÑA
                composable(
                    route = Route.ResenaDetail.path,
                    arguments = listOf(navArgument("resenaId") { type = NavType.LongType })
                ) { backStackEntry ->
                    if (isLoggedIn) {
                        val resenaId = backStackEntry.arguments?.getLong("resenaId")
                        if (resenaId != null) {
                            ResenaDetailScreen(
                                vm = authViewModel,
                                resenaId = resenaId,
                                onBack = goBack
                            )
                        }
                    } else {
                        LaunchedEffect(Unit) { goLogin() }
                    }
                }
            }
        }
    }
}