package com.example.myapplicationv.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun AppDrawer(
    currentRoute: String?,
    items: List<DrawerItem>,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Text(
            text = "VetHome",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = false,
                onClick = item.onClick,
                icon = { Icon(item.icon, contentDescription = item.label) },
                colors = NavigationDrawerItemDefaults.colors()
            )
        }
    }
}

@Composable
fun defaultDrawerItems(
    onHome: () -> Unit,
    onMascotas: () -> Unit,
    onCitas: () -> Unit,
    onLogin: () -> Unit,
    isUserLoggedIn: Boolean = false
): List<DrawerItem> {
    val baseItems = mutableListOf(
        DrawerItem("Inicio", Icons.Filled.Home, onHome),
        DrawerItem("Mis Mascotas", Icons.Filled.Pets, onMascotas),
        DrawerItem("Mis Citas", Icons.Filled.Event, onCitas)
    )

    // Solo agregar Login si no está logueado
    if (!isUserLoggedIn) {
        baseItems.add(DrawerItem("Login", Icons.Filled.Person, onLogin))
    }

    return baseItems
}