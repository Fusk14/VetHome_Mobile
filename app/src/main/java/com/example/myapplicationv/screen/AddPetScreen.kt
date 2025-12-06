package com.example.myapplicationv.screen

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplicationv.viewmodel.AuthViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Funciones auxiliares para manejo de archivos (reutilizadas de PetDetailScreen)
private fun createTempImageFile(context: Context): File? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.cacheDir
        val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        if (!imageFile.exists()) {
            imageFile.createNewFile()
        }
        imageFile
    } catch (_: Exception) {
        null
    }
}

private fun getImageUriForFile(context: Context, file: File?): Uri? {
    if (file == null || !file.exists()) return null
    return try {
        val authority = "${context.packageName}.fileprovider"
        FileProvider.getUriForFile(context, authority, file)
    } catch (_: Exception) {
        null
    }
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
    var gender by remember { mutableStateOf<String?>(null) }
    var hasSubmitted by remember { mutableStateOf(false) } // ✅ Nuevo flag de control
    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    // --- Estado global (desde el ViewModel) ---
    val petsState by vm.pets.collectAsStateWithLifecycle()

    // --- Lógica del selector de fecha ---
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // --- Lógica de Cámara y Galería ---
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCaptureUri != null) {
            try {
                val uri = pendingCaptureUri!!
                val file = File(uri.path ?: "")
                if (file.exists() && file.length() > 0) {
                    photoUriString = uri.toString()
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

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingCaptureUri?.let { uri ->
                try {
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
            photoUriString = it.toString()
            Toast.makeText(context, "Foto seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val formattedMonth = String.format(Locale.getDefault(), "%02d", month + 1)
            val formattedDay = String.format(Locale.getDefault(), "%02d", dayOfMonth)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            // --- Contenedor de la imagen de la mascota ---
            Box(
                modifier = Modifier.size(120.dp),
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
                            imageVector = Icons.Default.Pets,
                            contentDescription = "Icono de Mascota",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Botones para tomar/seleccionar foto ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        try {
                            val file = createTempImageFile(context)
                            if (file == null) {
                                Toast.makeText(context, "Error al crear archivo temporal", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val uri = getImageUriForFile(context, file)
                            if (uri == null) {
                                Toast.makeText(context, "Error al obtener URI del archivo", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            pendingCaptureUri = uri
                            
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED) {
                                takePictureLauncher.launch(uri)
                            } else {
                                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al preparar la cámara: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Tomar Foto", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cámara")
                }
                
                OutlinedButton(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Elegir de Galería", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Galería")
                }
            }

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

            GenderSelector(
                selectedGender = gender,
                onGenderSelected = { gender = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Botón para agregar ---
            Button(
                onClick = {
                    hasSubmitted = true
                    val normalizedSpecies = species.trim().replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }

                    vm.addPet(
                        nombre = petName.trim(),
                        especie = normalizedSpecies,
                        raza = breed.trim(),
                        genero = gender,
                        fechaNacimiento = birthDate.ifBlank { null },
                        peso = null,
                        color = null,
                        notasMedicas = null,
                        imagenUri = photoUriString
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = petName.isNotBlank() &&
                        species.isNotBlank() &&
                        breed.isNotBlank() &&
                        gender != null &&
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
@Composable
fun GenderSelector(
    selectedGender: String?,
    onGenderSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Género *", style = MaterialTheme.typography.bodyLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onGenderSelected("Macho") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedGender == "Macho") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Text("Macho")
            }
            OutlinedButton(
                onClick = { onGenderSelected("Hembra") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedGender == "Hembra") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Text("Hembra")
            }
        }
    }
}