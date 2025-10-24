package com.example.myapplicationv.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ==================== FUNCIONES PARA MANEJO DE ARCHIVOS ====================

// Crear archivo temporal para imágenes
private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "vet_images").apply {
        if (!exists()) mkdirs()
    }
    return File(storageDir, "VET_IMG_${timeStamp}.jpg")
}

// Obtener Uri del archivo para compartir con otras apps
private fun getImageUriForFile(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

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

    // Estados para cámara y galería
    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) } // 🆕 NUEVO: Diálogo para login requerido

    // Launchers para cámara y galería
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUriString = pendingCaptureUri?.toString()
            Toast.makeText(context, "Foto de mascota guardada", Toast.LENGTH_SHORT).show()
        } else {
            pendingCaptureUri = null
            Toast.makeText(context, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUriString = it.toString()
            Toast.makeText(context, "Foto seleccionada de galería", Toast.LENGTH_SHORT).show()
        }
    }

    // 🆕 NUEVO: Función para manejar el acceso a cámara/galería
    fun handleCameraAccess() {
        if (isUserLoggedIn) {
            val file = createTempImageFile(context)
            val uri = getImageUriForFile(context, file)
            pendingCaptureUri = uri
            takePictureLauncher.launch(uri)
        } else {
            showLoginRequiredDialog = true
        }
    }

    // 🆕 NUEVO: Función para manejar el acceso a galería
    fun handleGalleryAccess() {
        if (isUserLoggedIn) {
            pickImageLauncher.launch("image/*")
        } else {
            showLoginRequiredDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
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
                                text = "Sesión activa - Puedes subir fotos de tus mascotas",
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

            Spacer(Modifier.height(24.dp))

            // ==================== SECCIÓN CÁMARA Y GALERÍA MEJORADA ====================

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 🆕 NUEVO: Header condicional basado en estado de login
                    if (isUserLoggedIn) {
                        Text(
                            text = "📸 Gestión de Fotos de Mascotas",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Sube fotos de tus mascotas para mantener su historial visual",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = "🔒 Fotos de Mascotas",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Inicia sesión para subir fotos de tus mascotas",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Mostrar imagen si existe y usuario está logueado
                    if (isUserLoggedIn) {
                        if (photoUriString.isNullOrEmpty()) {
                            Text(
                                text = "No hay fotos de mascotas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(16.dp))
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(Uri.parse(photoUriString))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto de mascota",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    } else {
                        // 🆕 NUEVO: Mostrar placeholder cuando no hay sesión
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.Pets,
                                    contentDescription = "Iniciar sesión",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Inicia sesión para acceder",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Botones para cámara y galería - CONDICIONAL BASADO EN LOGIN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isUserLoggedIn) {
                            // 🟢 USUARIO LOGUEADO: Botones activos
                            // Botón Cámara
                            Button(
                                onClick = { handleCameraAccess() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Filled.PhotoCamera,
                                    contentDescription = "Cámara",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Tomar Foto")
                            }

                            // Botón Galería
                            OutlinedButton(
                                onClick = { handleGalleryAccess() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.PhotoLibrary,
                                    contentDescription = "Galería",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Galería")
                            }
                        } else {
                            // 🔴 USUARIO NO LOGUEADO: Botones desactivados con mensaje
                            Button(
                                onClick = { showLoginRequiredDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                ),
                                enabled = false
                            ) {
                                Icon(
                                    Icons.Filled.PhotoCamera,
                                    contentDescription = "Cámara - Inicia sesión",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Tomar Foto")
                            }

                            OutlinedButton(
                                onClick = { showLoginRequiredDialog = true },
                                modifier = Modifier.weight(1f),
                                enabled = false
                            ) {
                                Icon(
                                    Icons.Filled.PhotoLibrary,
                                    contentDescription = "Galería - Inicia sesión",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Galería")
                            }
                        }
                    }

                    // Botón eliminar foto (solo si hay foto y usuario logueado)
                    if (isUserLoggedIn && !photoUriString.isNullOrEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        TextButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Text("Eliminar Foto")
                        }
                    }

                    // 🆕 NUEVO: Información adicional para usuarios no logueados
                    if (!isUserLoggedIn) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "💡 Inicia sesión para desbloquear todas las funciones",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ==================== BOTONES DE NAVEGACIÓN PRINCIPALES ====================

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onGoMascotas,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Pets, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mis Mascotas")
                }

                Button(
                    onClick = onGoCitas,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Filled.Event, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agendar Cita")
                }

                OutlinedButton(
                    onClick = { /* Navegar a servicios */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.MedicalServices, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nuestros Servicios")
                }

                // Lógica condicional para Iniciar Sesión / Cerrar Sesión
                if (isUserLoggedIn) {
                    // Usuario logueado - mostrar cerrar sesión
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar Sesión (${userName.take(15)})")
                    }
                } else {
                    // Usuario no logueado - mostrar iniciar sesión
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onGoLogin,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Iniciar Sesión")
                        }

                        Text(
                            text = "o",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        TextButton(
                            onClick = onGoLogin,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("¿No tienes cuenta? Regístrate aquí")
                        }
                    }
                }
            }
        }
    }

    // ==================== DIÁLOGOS ====================

    // Diálogo de eliminación de foto
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Foto") },
            text = { Text("¿Estás seguro de que quieres eliminar esta foto de mascota?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        photoUriString = null
                        showDeleteDialog = false
                        Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 🆕 NUEVO: Diálogo para login requerido
    if (showLoginRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showLoginRequiredDialog = false },
            title = {
                Text(
                    "🔒 Inicio de Sesión Requerido",
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text("Para subir fotos de tus mascotas, necesitas iniciar sesión en tu cuenta de VetHome.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginRequiredDialog = false
                        onGoLogin()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Iniciar Sesión")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLoginRequiredDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}