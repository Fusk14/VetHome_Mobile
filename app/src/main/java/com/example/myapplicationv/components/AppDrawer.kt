package com.example.myapplicationv.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp // Para Logout
import androidx.compose.material.icons.filled.Star
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
                // La selección de la ruta actual requiere comparar item.label con currentRoute,
                // pero por simplicidad se deja en false. Si usaras Routes.kt aquí, podrías hacer la comparación.
                selected = false,
                onClick = item.onClick,
                icon = { Icon(item.icon, contentDescription = item.label) },
                colors = NavigationDrawerItemDefaults.colors()
            )
        }
    }
}

// En defaultDrawerItems, agregar el ítem de reseñas:
@Composable
fun defaultDrawerItems(
    onHome: () -> Unit,
    onMascotas: () -> Unit,
    onCitas: () -> Unit,
    onResenas: () -> Unit, // ✅ NUEVO: Reseñas
    onLogin: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit,
    isUserLoggedIn: Boolean = false
): List<DrawerItem> {
    val baseItems = mutableListOf(
        DrawerItem("Inicio", Icons.Filled.Home, onHome)
    )

    if (isUserLoggedIn) {
        baseItems.apply {
            add(DrawerItem("Mi Perfil", Icons.Filled.Person, onProfile))
            add(DrawerItem("Mis Mascotas", Icons.Filled.Pets, onMascotas))
            add(DrawerItem("Mis Citas", Icons.Filled.Event, onCitas))
            add(DrawerItem("Mis Reseñas", Icons.Filled.Star, onResenas)) // ✅ NUEVO
            add(DrawerItem("", Icons.Filled.Person) {}) // Separador
            add(DrawerItem("Cerrar Sesión", Icons.Filled.ExitToApp, onLogout))
        }
    } else {
        baseItems.add(DrawerItem("Login", Icons.Filled.Person, onLogin))
    }

    return baseItems
}