package com.example.myapplicationv.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplicationv.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
    onPetAdded: () -> Unit
) {
    val currentUser by vm.currentUser.collectAsStateWithLifecycle()
    val petsState by vm.pets.collectAsStateWithLifecycle()

    // Estados para el formulario
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var notasMedicas by remember { mutableStateOf("") }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // 🆕 NUEVO: Observar cuando se complete exitosamente la adición
    LaunchedEffect(petsState.pets) {
        // Si hay mascotas y no estamos cargando, asumimos éxito
        if (!petsState.isLoading && petsState.error == null && petsState.pets.isNotEmpty()) {
            // Buscar si la mascota que acabamos de agregar está en la lista
            val nuevaMascota = petsState.pets.find { it.nombre == nombre && it.especie == especie }
            if (nuevaMascota != null) {
                onPetAdded()
            }
        }
    }

    // 🆕 NUEVO: Observar errores
    LaunchedEffect(petsState.error) {
        if (petsState.error != null) {
            errorMessage = petsState.error ?: "Error desconocido"
            showErrorDialog = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Agregar Mascota",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                        Icons.Filled.Pets,
                        contentDescription = "Agregar mascota",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Nueva Mascota",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Completa la información de tu mascota",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // 🆕 NUEVO: Mostrar estado de carga/error
            if (petsState.isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Guardando mascota...")
                    }
                }
            }

            // Formulario
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Campo Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre de la mascota *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = nombre.isBlank()
                    )

                    // Campo Especie
                    OutlinedTextField(
                        value = especie,
                        onValueChange = { especie = it },
                        label = { Text("Especie *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Ej: Perro, Gato, Conejo") },
                        isError = especie.isBlank()
                    )

                    // Campo Raza
                    OutlinedTextField(
                        value = raza,
                        onValueChange = { raza = it },
                        label = { Text("Raza *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = raza.isBlank()
                    )

                    // Campo Fecha Nacimiento
                    OutlinedTextField(
                        value = fechaNacimiento,
                        onValueChange = { fechaNacimiento = it },
                        label = { Text("Fecha de nacimiento") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("YYYY-MM-DD") }
                    )

                    // Campo Peso
                    OutlinedTextField(
                        value = peso,
                        onValueChange = { peso = it },
                        label = { Text("Peso (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Ej: 5.5") }
                    )

                    // Campo Color
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Campo Notas Médicas
                    OutlinedTextField(
                        value = notasMedicas,
                        onValueChange = { notasMedicas = it },
                        label = { Text("Notas médicas") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )
                }
            }

            // Información de campos obligatorios
            Text(
                text = "* Campos obligatorios",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            // Botón Guardar
            Button(
                onClick = {
                    if (nombre.isNotBlank() && especie.isNotBlank() && raza.isNotBlank()) {
                        vm.addPet(
                            nombre = nombre.trim(),
                            especie = especie.trim(),
                            raza = raza.trim(),
                            fechaNacimiento = fechaNacimiento.ifBlank { null },
                            peso = peso.toDoubleOrNull(),
                            color = color.ifBlank { null },
                            notasMedicas = notasMedicas.ifBlank { null }
                        )
                    } else {
                        errorMessage = "Por favor completa los campos obligatorios"
                        showErrorDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotBlank() && especie.isNotBlank() && raza.isNotBlank() && !petsState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (petsState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Text("Guardar Mascota", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}