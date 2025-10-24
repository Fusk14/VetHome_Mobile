package com.example.myapplicationv.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
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
fun AddAppointmentScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
    onAppointmentAdded: () -> Unit
) {
    // Estados para el formulario
    var selectedPet by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Obtener mascotas del usuario
    val petsState by vm.pets.collectAsStateWithLifecycle()
    val currentUserState by vm.currentUser.collectAsStateWithLifecycle()
    val currentUser = currentUserState

    // Cargar mascotas si no están cargadas
    LaunchedEffect(currentUser.clientId) {
        currentUser.clientId
            .takeIf { id: Long -> id > 0 }
            ?.let { clientId ->
                vm.loadPetsForClient(clientId)
            }
    }

    // Datos de servicios
    val services = listOf(
        "Consulta general",
        "Vacunación",
        "Control de peso",
        "Desparasitación",
        "Limpieza dental",
        "Cirugía",
        "Urgencia",
        "Peluquería"
    )

    // Obtener nombres de mascotas para el selector
    val petNames = petsState.pets.map { it.nombre }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Agendar Cita",
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
                        Icons.Filled.Event,
                        contentDescription = "Agendar cita",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Nueva Cita",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Programa una cita para tu mascota",
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
                    // Selector de mascota
                    OutlinedTextField(
                        value = selectedPet,
                        onValueChange = { selectedPet = it },
                        label = { Text("Mascota *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                if (petNames.isEmpty()) "No tienes mascotas registradas"
                                else "Selecciona una mascota"
                            )
                        },
                        enabled = petNames.isNotEmpty()
                    )

                    // Información sobre mascotas
                    if (petNames.isEmpty()) {
                        Text(
                            text = "💡 Primero debes registrar una mascota para agendar una cita",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Fecha
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("Fecha *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("YYYY-MM-DD") }
                    )

                    // Hora
                    OutlinedTextField(
                        value = selectedTime,
                        onValueChange = { selectedTime = it },
                        label = { Text("Hora *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("HH:MM") }
                    )

                    // Servicio
                    OutlinedTextField(
                        value = selectedService,
                        onValueChange = { selectedService = it },
                        label = { Text("Servicio *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Selecciona un servicio") }
                    )

                    // Notas
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas adicionales") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        maxLines = 3,
                        placeholder = { Text("Síntomas, observaciones, etc.") }
                    )
                }
            }

            // Información de campos obligatorios
            Text(
                text = "* Campos obligatorios",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            // Botón Agendar
            Button(
                onClick = {
                    if (selectedPet.isNotBlank() && selectedDate.isNotBlank() &&
                        selectedTime.isNotBlank() && selectedService.isNotBlank()) {
                        // Aquí integrarías con tu ViewModel para guardar la cita
                        // Por ahora mostramos éxito
                        showSuccessDialog = true
                    } else {
                        errorMessage = "Por favor completa todos los campos obligatorios"
                        showErrorDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedPet.isNotBlank() && selectedDate.isNotBlank() &&
                        selectedTime.isNotBlank() && selectedService.isNotBlank() &&
                        petNames.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Agendar Cita", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("¡Cita Agendada!") },
            text = { Text("La cita se ha programado correctamente para $selectedDate a las $selectedTime.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onAppointmentAdded()
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
        )
    }
}