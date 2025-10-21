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
import com.example.myapplicationv.screen.HomeScreen

import com.example.myapplicationv.ui.components.AppTopBar
import com.example.myapplicationv.ui.components.AppDrawer
import com.example.myapplicationv.ui.components.defaultDrawerItems


@Composable
fun AppNavGraph(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Helpers de navegación
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
                composable(Route.Home.path) {
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoMascotas = goMascotas,
                        onGoCitas = goCitas
                    )
                }
                // Agregaremos Login y Register después
                composable(Route.Login.path) {
                    // Temporal - pantalla vacía
                    HomeScreen(onGoLogin = goLogin, onGoMascotas = goMascotas, onGoCitas = goCitas)
                }
                composable(Route.Register.path) {
                    // Temporal - pantalla vacía
                    HomeScreen(onGoLogin = goLogin, onGoMascotas = goMascotas, onGoCitas = goCitas)
                }
            }
        }
    }
}