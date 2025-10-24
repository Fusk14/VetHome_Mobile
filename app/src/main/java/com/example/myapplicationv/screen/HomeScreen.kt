package com.example.myapplicationv.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
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
    onGoCitas: () -> Unit
) {
    val context = LocalContext.current

    // ==================== ESTADOS PARA CÁMARA Y GALERÍA ====================

    // Estado para la última foto tomada/seleccionada
    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }

    // Estado temporal para la Uri de la cámara
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    // Estado para controlar diálogo de eliminación
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ==================== LAUNCHERS PARA CÁMARA Y GALERÍA ====================

    // Launcher para la cámara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Si se tomó la foto correctamente
            photoUriString = pendingCaptureUri?.toString()
            Toast.makeText(context, "Foto de mascota guardada", Toast.LENGTH_SHORT).show()
        } else {
            pendingCaptureUri = null
            Toast.makeText(context, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para la galería
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUriString = it.toString()
            Toast.makeText(context, "Foto seleccionada de galería", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== INTERFAZ PRINCIPAL ====================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con logo
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

            // ==================== SECCIÓN CÁMARA Y GALERÍA ====================

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Gestión de Fotos de Mascotas",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    // Mostrar imagen si existe
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

                    // Botones para cámara y galería
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón Cámara
                        Button(
                            onClick = {
                                val file = createTempImageFile(context)
                                val uri = getImageUriForFile(context, file)
                                pendingCaptureUri = uri
                                takePictureLauncher.launch(uri)
                            },
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
                            onClick = {
                                pickImageLauncher.launch("image/*")
                            },
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
                    }

                    // Botón eliminar foto (solo si hay foto)
                    if (!photoUriString.isNullOrEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        TextButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Text("Eliminar Foto")
                        }
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

                TextButton(onClick = onGoLogin) {
                    Text("Iniciar Sesión")
                }
            }
        }
    }

    // ==================== DIÁLOGO DE CONFIRMACIÓN ====================

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
}