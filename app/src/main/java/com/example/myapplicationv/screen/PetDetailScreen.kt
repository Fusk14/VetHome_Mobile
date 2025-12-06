package com.example.myapplicationv.screen

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import coil.request.ImageRequest
import com.example.myapplicationv.R
import com.example.myapplicationv.viewmodel.AuthViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// --- Funciones auxiliares para manejo de archivos ---
private fun createTempImageFile(context: Context): File? {
    return try {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.cacheDir
        val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        // Asegurar que el archivo existe y es escribible
        if (!imageFile.exists()) {
            imageFile.createNewFile()
        }
        imageFile
    } catch (e: Exception) {
        null
    }
}

private fun getImageUriForFile(context: Context, file: File?): Uri? {
    if (file == null || !file.exists()) return null
    return try {
    val authority = "${context.packageName}.fileprovider"
        FileProvider.getUriForFile(context, authority, file)
    } catch (e: Exception) {
        null
    }
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
    // Inicializar con la imagen guardada de la mascota si existe
    var photoUriString by rememberSaveable(petState.pet?.imagenUri) { 
        mutableStateOf<String?>(petState.pet?.imagenUri) 
    }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    // IMPORTANTE: Declarar takePictureLauncher primero para que pueda ser usado por requestPermissionLauncher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCaptureUri != null) {
            // Verificar que el archivo realmente existe después de tomar la foto
            try {
                val uri = pendingCaptureUri!!
                val file = File(uri.path ?: "")
                if (file.exists() && file.length() > 0) {
                    val uriString = uri.toString()
                    photoUriString = uriString
                    // Guardar la imagen en la base de datos
                    petState.pet?.id?.let { petId ->
                        vm.updatePetImage(petId, uriString)
                    }
                    Toast.makeText(context, "Foto guardada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "La foto no se guardó correctamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al procesar la foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No se pudo tomar la foto. Intenta nuevamente.", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para solicitar permisos de cámara
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si se otorga el permiso, tomar la foto
            pendingCaptureUri?.let { uri ->
                try {
                    // Verificar que el URI es válido antes de lanzar
                    if (uri.scheme == "content" || uri.scheme == "file") {
                        takePictureLauncher.launch(uri)
                    } else {
                        Toast.makeText(context, "URI inválido para la cámara", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(context, "Error de permisos: ${e.message}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } ?: run {
                Toast.makeText(context, "No se pudo preparar el archivo para la foto", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Se necesita permiso de cámara para tomar fotos", Toast.LENGTH_LONG).show()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val uriString = it.toString()
            photoUriString = uriString
            // Guardar la imagen en la base de datos
            petState.pet?.id?.let { petId ->
                vm.updatePetImage(petId, uriString)
            }
            Toast.makeText(context, "Foto seleccionada", Toast.LENGTH_SHORT).show()
        }
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
                            if (photoUriString != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(Uri.parse(photoUriString))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto de ${pet.nombre}",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
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

                        Text(
                            text = pet.nombre,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // --- Botones para gestión de la foto ---
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(onClick = {
                                try {
                                    // Crear archivo temporal primero
                                val file = createTempImageFile(context)
                                    if (file == null) {
                                        Toast.makeText(
                                            context,
                                            "Error al crear archivo temporal",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                    
                                val uri = getImageUriForFile(context, file)
                                    if (uri == null) {
                                        Toast.makeText(
                                            context,
                                            "Error al obtener URI del archivo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                    
                                pendingCaptureUri = uri
                                    
                                    // Verificar permiso de cámara
                                    when {
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED -> {
                                            // Permiso ya otorgado, tomar foto
                                takePictureLauncher.launch(uri)
                                        }
                                        else -> {
                                            // Solicitar permiso
                                            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Error al preparar la cámara: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Tomar Foto")
                                Spacer(Modifier.width(8.dp))
                                Text("Tomar Foto")
                            }
                            OutlinedButton(onClick = { pickImageLauncher.launch("image/*") }) {
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
