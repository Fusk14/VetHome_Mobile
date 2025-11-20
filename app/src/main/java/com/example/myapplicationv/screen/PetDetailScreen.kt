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
import coil.request.ImageRequest
import com.example.myapplicationv.R
import com.example.myapplicationv.viewmodel.AuthViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// --- Funciones auxiliares para manejo de archivos (se mantienen igual) ---
private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.cacheDir
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
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
    // ✅ CAMBIO 1: La variable ahora se llama 'petState' para mayor claridad.
    // Su tipo es 'SelectedPetUiState'.
    val petState by vm.selectedPet.collectAsState()

    // --- Lógica de Cámara y Galería ---
    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUriString = pendingCaptureUri?.toString()
            Toast.makeText(context, "Foto guardada", Toast.LENGTH_SHORT).show()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUriString = it.toString()
            Toast.makeText(context, "Foto seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(petId) {
        vm.loadPetById(petId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // ✅ CAMBIO 2: Accedemos al nombre a través de 'petState.pet?.nombre'
                title = { Text(petState.pet?.nombre ?: "Detalle de Mascota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        // ✅ CAMBIO 3: Manejamos los tres posibles estados: carga, error y éxito.
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
                                val file = createTempImageFile(context)
                                val uri = getImageUriForFile(context, file)
                                pendingCaptureUri = uri
                                takePictureLauncher.launch(uri)
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

                        // --- Tarjeta con información detallada ---
                        // ✅ CAMBIO 4: Usamos la variable local 'pet' para acceder a las propiedades
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
