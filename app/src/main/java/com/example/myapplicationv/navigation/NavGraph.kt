package com.example.myapplicationv.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.myapplicationv.ui.components.AppTopBar
import com.example.myapplicationv.ui.components.AppDrawer
import com.example.myapplicationv.ui.components.defaultDrawerItems
import com.example.myapplicationv.ui.screen.HomeScreen
import com.example.myapplicationv.ui.screen.LoginScreenVm
import com.example.myapplicationv.ui.screen.RegisterScreenVm
import com.example.myapplicationv.data.repository.VetRepository
import com.example.myapplicationv.data.local.database.AppDatabase
import com.example.myapplicationv.viewmodel.AuthViewModel
import com.example.myapplicationv.viewmodel.AuthViewModelFactory

@Composable
fun AppNavGraph(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Crear el repositorio y ViewModel Factory
    val database = AppDatabase.getInstance(androidx.compose.ui.platform.LocalContext.current)
    val repository = VetRepository(database.clientDao(), database.petDao())
    val viewModelFactory = AuthViewModelFactory(repository)

    // Helpers de navegacion
    val goHome: () -> Unit = { navController.navigate(Route.Home.path) }
    val goLogin: () -> Unit = { navController.navigate(Route.Login.path) }
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) }
    val goMascotas: () -> Unit = { navController.navigate(Route.Mascotas.path) }
    val goCitas: () -> Unit = { navController.navigate(Route.Citas.path) }

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
                // Pantalla Home
                composable(Route.Home.path) {
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoMascotas = goMascotas,
                        onGoCitas = goCitas
                    )
                }

                // Pantalla Login - CONECTADA con ViewModel y navegacion a Home
                composable(Route.Login.path) {
                    val vm: AuthViewModel = viewModel(factory = viewModelFactory)
                    LoginScreenVm(
                        vm = vm,
                        onLoginOkNavigateHome = goHome,  // Navega a Home cuando login es exitoso
                        onGoRegister = goRegister
                    )
                }

                // Pantalla Register - CONECTADA con ViewModel y navegacion a Login
                composable(Route.Register.path) {
                    val vm: AuthViewModel = viewModel(factory = viewModelFactory)
                    RegisterScreenVm(
                        vm = vm,
                        onRegisteredNavigateLogin = goLogin,  // Navega a Login cuando registro es exitoso
                        onGoLogin = goLogin
                    )
                }

                // Pantallas temporales (para desarrollo)
                composable(Route.Mascotas.path) {
                    // Pantalla temporal de Mascotas
                    HomeScreen(onGoLogin = goLogin, onGoMascotas = goMascotas, onGoCitas = goCitas)
                }

                composable(Route.Citas.path) {
                    // Pantalla temporal de Citas
                    HomeScreen(onGoLogin = goLogin, onGoMascotas = goMascotas, onGoCitas = goCitas)
                }
            }
        }
    }
}