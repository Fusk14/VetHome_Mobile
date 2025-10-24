package com.example.myapplicationv.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.* // Asegúrate de que este import está
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplicationv.viewmodel.AuthViewModel

// ⬅️ CAMBIO CLAVE: Se añade la anotación para usar APIs experimentales
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
    onPetAdded: () -> Unit
) {
    val currentUser by vm.currentUser.collectAsStateWithLifecycle()

    // Estados para el formulario
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var notasMedicas by remember { mutableStateOf("") }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                        singleLine = true
                    )

                    // Campo Especie
                    OutlinedTextField(
                        value = especie,
                        onValueChange = { especie = it },
                        label = { Text("Especie *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Ej: Perro, Gato, Conejo") }
                    )

                    // Campo Raza
                    OutlinedTextField(
                        value = raza,
                        onValueChange = { raza = it },
                        label = { Text("Raza *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                            nombre = nombre,
                            especie = especie,
                            raza = raza,
                            fechaNacimiento = fechaNacimiento.ifBlank { null },
                            peso = peso.toDoubleOrNull(),
                            color = color.ifBlank { null },
                            notasMedicas = notasMedicas.ifBlank { null }
                        )
                        showSuccessDialog = true
                    } else {
                        errorMessage = "Por favor completa los campos obligatorios"
                        showErrorDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotBlank() && especie.isNotBlank() && raza.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Guardar Mascota", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("¡Mascota Agregada!") },
            text = { Text("La mascota se ha registrado correctamente.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPetAdded()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
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
            // Asegúrate de que tu AlertDialog también está importado de material3
        )
    }
}