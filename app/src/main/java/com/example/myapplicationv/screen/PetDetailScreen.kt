package com.example.myapplicationv.screen

import android.content.Context
import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.myapplicationv.R
import com.example.myapplicationv.util.getStoragePermissions
import com.example.myapplicationv.util.rememberCameraPermissionLauncher
import com.example.myapplicationv.util.rememberStoragePermissionLauncher
import com.example.myapplicationv.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// --- Funciones auxiliares para manejo de archivos (se mantienen igual) ---
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

    // Su tipo es 'SelectedPetUiState'.
    val petState by vm.selectedPet.collectAsState()

    // --- Lógica de Cámara y Galería ---
    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showServerPhoto by remember { mutableStateOf(true) }

    // Función para subir foto (definida antes de usarse)
    val uploadPhoto: (String) -> Unit = remember {
        { uriString ->
            isUploadingPhoto = true
            try {
                val uri = Uri.parse(uriString)
                val imageBytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes()
                }
                
                imageBytes?.let { bytes ->
                    vm.uploadPetPhoto(petId, bytes)
                    // Después de subir, limpiar foto local para mostrar la del servidor
                    CoroutineScope(Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(2000) // Esperar a que se suba y procese
                        photoUriString = null // Limpiar foto local
                        showServerPhoto = true // Mostrar foto del servidor
                        // Forzar recarga de la mascota para actualizar el estado
                        vm.loadPetById(petId)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isUploadingPhoto = false
            }
        }
    }

    // Definir primero los launchers de actividad
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUriString = pendingCaptureUri?.toString()
            Toast.makeText(context, "Foto capturada", Toast.LENGTH_SHORT).show()
            // Subir foto automáticamente
            photoUriString?.let { uriString ->
                uploadPhoto(uriString)
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUriString = it.toString()
            Toast.makeText(context, "Foto seleccionada", Toast.LENGTH_SHORT).show()
            // Subir foto automáticamente
            uploadPhoto(it.toString())
        }
    }

    // Luego definir los permission launchers que usan los launchers anteriores
    val cameraPermissionLauncher = rememberCameraPermissionLauncher(
        onPermissionGranted = {
            try {
                val file = createTempImageFile(context)
                // Asegurar que el archivo existe
                file.parentFile?.mkdirs()
                file.createNewFile()
                val uri = getImageUriForFile(context, file)
                pendingCaptureUri = uri
                // Lanzar la cámara con el URI
                takePictureLauncher.launch(uri)
            } catch (e: SecurityException) {
                Toast.makeText(context, "Error de seguridad al acceder a la cámara", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
            }
        },
        onPermissionDenied = {
            Toast.makeText(context, "Se necesita permiso de cámara para tomar fotos", Toast.LENGTH_LONG).show()
        }
    )
    
    val storagePermissionLauncher = rememberStoragePermissionLauncher(
        onPermissionGranted = {
            pickImageLauncher.launch("image/*")
        },
        onPermissionDenied = {
            Toast.makeText(context, "Se necesita permiso de almacenamiento para seleccionar fotos", Toast.LENGTH_LONG).show()
        }
    )

    // URL de la foto del servidor - usar RemoteModule para construirla correctamente
    // Agregar timestamp para evitar cache y forzar recarga
    val serverPhotoUrl = remember(petId, petState.pet) {
        val baseUrl = com.example.myapplicationv.data.remote.RemoteModule.baseUrlFor(
            com.example.myapplicationv.data.remote.RemoteModule.Microservice.MASCOTAS
        )
        // Asegurar que baseUrl termine con / y no tenga doble slash
        val cleanBaseUrl = baseUrl.trimEnd('/')
        val timestamp = System.currentTimeMillis()
        "$cleanBaseUrl/api/mascotas/$petId/foto?t=$timestamp"
    }
    
    LaunchedEffect(petId) {
        vm.loadPetById(petId)
        // Resetear para mostrar foto del servidor cuando se carga la mascota
        showServerPhoto = true
        photoUriString = null // Limpiar foto local para mostrar la del servidor
        
        // Verificar si existe foto en el servidor
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(serverPhotoUrl)
                    .head() // Usar HEAD para verificar sin descargar
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        showServerPhoto = false
                    }
                }
                response.close()
            } catch (e: Exception) {
                // No cambiar showServerPhoto aquí, dejar que AsyncImage maneje el error
            }
        }
    }
    
    // Observar cuando se sube una foto exitosamente
    LaunchedEffect(petState.pet) {
        // Cuando se carga la mascota, intentar mostrar la foto del servidor
        if (petState.pet != null && photoUriString == null) {
            showServerPhoto = true
        }
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
                                    // Mostrar foto local (recién capturada/seleccionada)
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(Uri.parse(photoUriString))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Foto de ${pet.nombre}",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        onError = {
                                            // Si falla la foto local, intentar mostrar la del servidor
                                            photoUriString = null
                                            showServerPhoto = true
                                        }
                                    )
                                }
                                showServerPhoto -> {
                                    // Mostrar foto del servidor
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(serverPhotoUrl)
                                            .crossfade(true)
                                            .error(R.drawable.ic_pets) // Fallback si no hay foto
                                            .placeholder(R.drawable.ic_pets) // Placeholder mientras carga
                                            .memoryCachePolicy(CachePolicy.DISABLED) // Deshabilitar cache para forzar recarga
                                            .diskCachePolicy(CachePolicy.DISABLED) // Deshabilitar cache de disco
                                            .setHeader("Accept", "image/jpeg, image/png, image/*")
                                            .setHeader("Cache-Control", "no-cache")
                                            .build(),
                                        contentDescription = "Foto de ${pet.nombre}",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        onError = {
                                            // Si no hay foto en el servidor, mostrar placeholder
                                            showServerPhoto = false
                                        }
                                    )
                                }
                                else -> {
                                    // Mostrar placeholder
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_pets),
                                            contentDescription = "Icono de mascota",
                                            modifier = Modifier.size(80.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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
