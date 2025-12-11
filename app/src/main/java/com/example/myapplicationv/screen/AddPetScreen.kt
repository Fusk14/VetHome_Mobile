package com.example.myapplicationv.screen

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplicationv.R
import com.example.myapplicationv.util.getStoragePermissions
import com.example.myapplicationv.util.rememberCameraPermissionLauncher
import com.example.myapplicationv.util.rememberStoragePermissionLauncher
import com.example.myapplicationv.viewmodel.AuthViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Funciones auxiliares para manejo de archivos
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
fun AddPetScreen(
    vm: AuthViewModel,
    onPetAdded: () -> Unit,
    onBack: () -> Unit
) {
    // --- Estados locales del formulario ---
    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var hasSubmitted by remember { mutableStateOf(false) } // ✅ Nuevo flag de control
    var photoUriString by remember { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    // --- Estado global (desde el ViewModel) ---
    val petsState by vm.pets.collectAsStateWithLifecycle()

    // --- Lógica del selector de fecha ---
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // --- Lógica de permisos y cámara ---
    var showPermissionRationale by remember { mutableStateOf(false) }
    
    // Definir primero los launchers de actividad
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUriString = pendingCaptureUri?.toString()
            Toast.makeText(context, "Foto capturada", Toast.LENGTH_SHORT).show()
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
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        },
        onPermissionDenied = {
            showPermissionRationale = true
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

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val formattedMonth = String.format("%02d", month + 1)
            val formattedDay = String.format("%02d", dayOfMonth)
            birthDate = "$year-$formattedMonth-$formattedDay"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.maxDate = calendar.timeInMillis

    //Navegar solo después de agregar una mascota correctamente
    LaunchedEffect(petsState.isLoading, petsState.pets.size) {
        if (hasSubmitted && !petsState.isLoading && petsState.error == null) {
            onPetAdded()
            hasSubmitted = false
        }
    }

    // --- Estructura general ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Agregar Mascota", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Icono principal ---
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = "Icono de Mascota",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "Nueva Mascota",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // --- Mostrar error si hay ---
            petsState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
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
                        contentDescription = "Foto de la mascota",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
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
            
            // --- Botones para gestión de la foto ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Tomar Foto")
                    Spacer(Modifier.width(8.dp))
                    Text("Cámara")
                }
                OutlinedButton(
                    onClick = {
                        storagePermissionLauncher.launch(getStoragePermissions())
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Elegir de Galería")
                    Spacer(Modifier.width(8.dp))
                    Text("Galería")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // --- Campos del formulario ---
            OutlinedTextField(
                value = petName,
                onValueChange = { petName = it },
                label = { Text("Nombre de la mascota *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Especie (ej. Perro, Gato) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Raza *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            // --- Campo de fecha de nacimiento ---
            OutlinedTextField(
                value = birthDate,
                onValueChange = { },
                label = { Text("Fecha de Nacimiento") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha de nacimiento"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Botón para agregar ---
            Button(
                onClick = {
                    hasSubmitted = true
                    val normalizedSpecies = species.trim().replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }

                    // Convertir foto a bytes si existe
                    val imageBytes = photoUriString?.let { uriString ->
                        try {
                            val uri = Uri.parse(uriString)
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                inputStream.readBytes()
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    vm.addPet(
                        nombre = petName.trim(),
                        especie = normalizedSpecies,
                        raza = breed.trim(),
                        fechaNacimiento = if (birthDate.isNotBlank()) birthDate else null,
                        peso = null,
                        color = null,
                        notasMedicas = null,
                        fotoBytes = imageBytes
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = petName.isNotBlank() &&
                        species.isNotBlank() &&
                        breed.isNotBlank() &&
                        !petsState.isLoading
            ) {
                if (petsState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Agregar Mascota", style = MaterialTheme.typography.titleMedium)
                }
            }

            Text("* Campos obligatorios", style = MaterialTheme.typography.labelSmall)
        }
    }
}
