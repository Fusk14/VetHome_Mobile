package com.example.myapplicationv.ui.screen // o com.example.myapplicationv.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onGoLogin: () -> Unit,
    onGoMascotas: () -> Unit,
    onGoCitas: () -> Unit,
    onLogout: () -> Unit,
    isUserLoggedIn: Boolean = false,
    userName: String = "",
    sessionMessage: String? = null,
    onMessageShown: () -> Unit = {}
) {
    val context = LocalContext.current

    // Mostrar Toast cuando hay mensajes de sesión
    if (sessionMessage != null) {
        LaunchedEffect(sessionMessage) {
            Toast.makeText(context, sessionMessage, Toast.LENGTH_LONG).show()
            onMessageShown()
        }
    }

    // Diálogo para login requerido (puede ser útil mantenerlo por si otra función lo necesita)
    var showLoginRequiredDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter // Alineado arriba para el scroll
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()), // Permite scroll
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con información de usuario si está logueado
            if (isUserLoggedIn && userName.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Usuario",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "¡Hola, $userName!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Gestiona tus citas y mascotas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Logo y título
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Pets,
                    contentDescription = "VetHome Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "VetHome",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(24.dp))

            // Tarjeta de bienvenida
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Cuidamos de tu mejor amigo",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Servicios veterinarios de calidad con el cariño que tu mascota merece",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(32.dp)) // Aumentado el espacio

            // ==================== BOTONES DE NAVEGACIÓN PRINCIPALES ====================

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón Mis Mascotas
                Button(
                    onClick = onGoMascotas,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Icon(Icons.Filled.Pets, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mis Mascotas")
                }

                // Botón Agendar Cita
                Button(
                    onClick = onGoCitas,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Filled.Event, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agendar Cita")
                }

                // Botón Nuestros Servicios
                OutlinedButton(
                    onClick = { /* TODO: Navegar a servicios */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Filled.MedicalServices, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nuestros Servicios")
                }

                Spacer(Modifier.height(24.dp))

                // Lógica condicional para Iniciar Sesión / Cerrar Sesión
                if (isUserLoggedIn) {
                    // Usuario logueado -> mostrar cerrar sesión
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar Sesión")
                    }
                } else {
                    // Usuario no logueado -> mostrar iniciar sesión
                    Button(
                        onClick = onGoLogin,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar Sesión o Registrarse")
                    }
                }
            }
        }
    }
}
