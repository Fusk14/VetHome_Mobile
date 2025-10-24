package com.example.myapplicationv.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onOpenDrawer: () -> Unit,
    onHome: () -> Unit,
    onMascotas: () -> Unit,
    onCitas: () -> Unit,
    onLogin: () -> Unit,
    isUserLoggedIn: Boolean = false,
    userName: String = ""
) {
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            Text(
                text = if (isUserLoggedIn && userName.isNotBlank()) {
                    "VetHome - Hola, ${userName.take(10)}" //TÍTULO PERSONALIZADO
                } else {
                    "VetHome - Veterinaria"
                },
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menú",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onHome) {
                Icon(Icons.Filled.Home, contentDescription = "Inicio")
            }
            IconButton(onClick = onMascotas) {
                Icon(Icons.Filled.Pets, contentDescription = "Mascotas")
            }
            IconButton(onClick = onCitas) {
                Icon(Icons.Filled.Event, contentDescription = "Citas")
            }

            //  Mostrar icono de persona solo si no está logueado
            if (!isUserLoggedIn) {
                IconButton(onClick = onLogin) {
                    Icon(Icons.Filled.Person, contentDescription = "Login")
                }
            }

            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Inicio") },
                    onClick = { showMenu = false; onHome() }
                )
                DropdownMenuItem(
                    text = { Text("Mis Mascotas") },
                    onClick = { showMenu = false; onMascotas() }
                )
                DropdownMenuItem(
                    text = { Text("Mis Citas") },
                    onClick = { showMenu = false; onCitas() }
                )
                // Mostrar login solo si no está logueado
                if (!isUserLoggedIn) {
                    DropdownMenuItem(
                        text = { Text("Login") },
                        onClick = { showMenu = false; onLogin() }
                    )
                }
            }
        }
    )
}