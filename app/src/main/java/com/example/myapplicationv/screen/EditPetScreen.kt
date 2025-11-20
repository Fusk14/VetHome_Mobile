package com.example.myapplicationv.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplicationv.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetScreen(
    petId: Long,
    viewModel: AuthViewModel,
    onPetUpdated: () -> Unit,
    onBack: () -> Unit
) {
    // ✅ CORRECCIÓN: Usar el estado correcto del ViewModel
    val selectedPetState by viewModel.selectedPet.collectAsStateWithLifecycle()

    // Variables de estado para los campos del formulario
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var medicalNotes by remember { mutableStateOf("") }

    // Variable para controlar si los datos ya se han cargado
    var isDataLoaded by remember { mutableStateOf(false) }

    // Cargar la mascota cuando se inicia la pantalla
    LaunchedEffect(petId) {
        viewModel.loadPetById(petId)
    }

    // Actualizar el formulario cuando los datos de la mascota estén disponibles
    LaunchedEffect(selectedPetState.pet) {
        selectedPetState.pet?.let { pet ->
            if (!isDataLoaded) {
                name = pet.nombre
                species = pet.especie ?: ""
                breed = pet.raza ?: ""
                birthDate = pet.fechaNacimiento ?: ""
                weight = pet.peso?.toString() ?: ""
                color = pet.color ?: ""
                medicalNotes = pet.notasMedicas ?: ""
                isDataLoaded = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Mascota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mostrar estado de carga
            if (selectedPetState.isLoading) {
                CircularProgressIndicator()
                Text("Cargando datos de la mascota...")
            }
            // Mostrar error si existe
            else if (selectedPetState.error != null) {
                Text(
                    text = selectedPetState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // Mostrar formulario cuando los datos estén cargados
            else if (isDataLoaded) {
                // Campos del formulario
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Especie *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Raza *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Fecha de Nacimiento (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    placeholder = { Text("Ej: 2020-05-15") }
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    isError = weight.isNotEmpty() && weight.toDoubleOrNull() == null
                )

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = medicalNotes,
                    onValueChange = { medicalNotes = it },
                    label = { Text("Notas Médicas") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Validar campos obligatorios
                        if (name.isBlank() || species.isBlank() || breed.isBlank()) {
                            // Podrías mostrar un error aquí
                            return@Button
                        }

                        // Convertir valores opcionales
                        val weightValue = weight.toDoubleOrNull()
                        val birthDateValue = if (birthDate.isNotBlank()) birthDate else null
                        val colorValue = if (color.isNotBlank()) color else null
                        val medicalNotesValue = if (medicalNotes.isNotBlank()) medicalNotes else null

                        // Aquí necesitarías agregar una función updatePet en tu ViewModel
                        // viewModel.updatePet(petId, name, species, breed, birthDateValue, weightValue, colorValue, medicalNotesValue)

                        // Por ahora, navegamos de regreso
                        onPetUpdated()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && species.isNotBlank() && breed.isNotBlank()
                ) {
                    Text("Guardar Cambios")
                }

                Text(
                    text = "* Campos obligatorios",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}