package com.example.myapplicationv.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplicationv.R
import com.example.myapplicationv.util.getStoragePermissions
import com.example.myapplicationv.util.rememberCameraPermissionLauncher
import com.example.myapplicationv.util.rememberStoragePermissionLauncher
import com.example.myapplicationv.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// --- Funciones auxiliares para manejo de archivos ---
private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "images")
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }
    return File(storageDir, "JPEG_${timeStamp}_${System.currentTimeMillis()}.jpg")
}

private fun getImageUriForFile(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    vm: AuthViewModel,
    petId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val petState by vm.selectedPet.collectAsState()

    // Estados para foto
    var photoUriString by remember { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    // Función para subir foto
    val uploadPhoto: (String) -> Unit = { uriString ->
        isUploadingPhoto = true
        try {
            val uri = Uri.parse(uriString)
            val imageBytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
            
            imageBytes?.let { bytes ->
                vm.uploadPetPhoto(petId, bytes)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(1500)
                    photoUriString = null
                    vm.loadPetById(petId)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isUploadingPhoto = false
        }
    }

    // Launchers de actividad
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUriString = pendingCaptureUri?.toString()
            photoUriString?.let { uploadPhoto(it) }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUriString = it.toString()
            uploadPhoto(it.toString())
        }
    }

    // Permission launchers
    val cameraPermissionLauncher = rememberCameraPermissionLauncher(
        onPermissionGranted = {
            try {
                val file = createTempImageFile(context)
                file.parentFile?.mkdirs()
                file.createNewFile()
                val uri = getImageUriForFile(context, file)
                pendingCaptureUri = uri
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
            }
        },
        onPermissionDenied = {
            Toast.makeText(context, "Se necesita permiso de cámara", Toast.LENGTH_LONG).show()
        }
    )
    
    val storagePermissionLauncher = rememberStoragePermissionLauncher(
        onPermissionGranted = {
            pickImageLauncher.launch("image/*")
        },
        onPermissionDenied = {
            Toast.makeText(context, "Se necesita permiso de almacenamiento", Toast.LENGTH_LONG).show()
        }
    )

    // URL de la foto del servidor
    val serverPhotoUrl = remember(petId) {
        val baseUrl = com.example.myapplicationv.data.remote.RemoteModule.baseUrlFor(
            com.example.myapplicationv.data.remote.RemoteModule.Microservice.MASCOTAS
        )
        // Asegurar que baseUrl termine con / y no tenga doble slash
        val cleanBaseUrl = baseUrl.trimEnd('/')
        val timestamp = System.currentTimeMillis()
        val url = "$cleanBaseUrl/api/mascotas/$petId/foto?t=$timestamp"
        Log.d("PetDetailScreen", "URL de foto del servidor: $url")
        url
    }
    
    LaunchedEffect(petId) {
        vm.loadPetById(petId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(petState.pet?.nombre ?: "Detalle de Mascota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        // carga, error y éxito.
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Estado de Carga
                petState.isLoading -> {
                    CircularProgressIndicator()
                    Text(
                        "Cargando datos de la mascota...",
                        modifier = Modifier.padding(top = 80.dp)
                    )
                }
                // Estado de Error
                petState.error != null -> {
                    Text(
                        text = petState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                // Estado de Éxito (la mascota no es nula)
                petState.pet != null -> {
                    val pet = petState.pet!! // Creamos una variable local para no repetir 'petState.pet'

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- Contenedor de la imagen ---
                        Box(
                            modifier = Modifier.size(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                photoUriString != null -> {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(Uri.parse(photoUriString))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Foto de ${pet.nombre}",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(serverPhotoUrl)
                                            .crossfade(true)
                                            .error(R.drawable.ic_pets)
                                            .placeholder(R.drawable.ic_pets)
                                            .build(),
                                        contentDescription = "Foto de ${pet.nombre}",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        Text(
                            text = pet.nombre,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // --- Botones para gestión de la foto ---
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                },
                                enabled = !isUploadingPhoto,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isUploadingPhoto) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Tomar Foto")
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("Cámara")
                            }
                            OutlinedButton(
                                onClick = {
                                    storagePermissionLauncher.launch(getStoragePermissions())
                                },
                                enabled = !isUploadingPhoto,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Elegir de Galería")
                                Spacer(Modifier.width(8.dp))
                                Text("Galería")
                            }
                        }



                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InfoRow("Especie:", pet.especie)
                                InfoRow("Raza:", pet.raza)
                                InfoRow("Fecha de Nacimiento:", pet.fechaNacimiento ?: "No especificada")
                                InfoRow("Peso:", "${pet.peso ?: "N/A"} kg")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
        Text(value ?: "No especificado")
    }
}
