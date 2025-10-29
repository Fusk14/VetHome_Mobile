package com.example.myapplicationv.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplicationv.data.local.pet.PetEntity
import com.example.myapplicationv.viewmodel.AuthViewModel
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
    onAppointmentAdded: () -> Unit
) {
    val context = LocalContext.current

    // Estados para el formulario
    var selectedPet by remember { mutableStateOf<PetEntity?>(null) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var reason by remember { mutableStateOf("") }

    var isPetDropdownExpanded by remember { mutableStateOf(false) }

    val appointmentsState by vm.appointments.collectAsStateWithLifecycle()
    val petsState by vm.pets.collectAsStateWithLifecycle()
    val currentUserState by vm.currentUser.collectAsStateWithLifecycle()

    // Cargar mascotas si es necesario
    LaunchedEffect(currentUserState.clientId) {
        if (currentUserState.clientId > 0) {
            vm.loadPetsForClient(currentUserState.clientId)
        }
    }

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

            // Selector de mascota
            ExposedDropdownMenuBox(
                expanded = isPetDropdownExpanded,
                onExpandedChange = { isPetDropdownExpanded = !isPetDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedPet?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mascota *") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPetDropdownExpanded) },
                    placeholder = { Text(if (petsState.pets.isEmpty()) "No tienes mascotas" else "Selecciona una mascota") },
                    enabled = petsState.pets.isNotEmpty()
                )
                ExposedDropdownMenu(
                    expanded = isPetDropdownExpanded,
                    onDismissRequest = { isPetDropdownExpanded = false }
                ) {
                    petsState.pets.forEach {
                        DropdownMenuItem(
                            text = { Text(it.nombre) },
                            onClick = {
                                selectedPet = it
                                isPetDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Selector de fecha y hora
            Button(onClick = { showDateTimePicker(context) { selectedDate = it } }) {
                Text(text = selectedDate?.toString() ?: "Seleccionar fecha y hora")
            }

            // Motivo de la cita
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Motivo de la cita") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Botón para agendar
            Button(
                onClick = {
                    val pet = selectedPet
                    val date = selectedDate
                    if (pet != null && date != null && reason.isNotBlank()) {
                        vm.addAppointment(pet.id, date, reason)
                        onAppointmentAdded()
                    } else {
                        // Manejar el caso de campos incompletos
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedPet != null && selectedDate != null && reason.isNotBlank()
            ) {
                if (appointmentsState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Agendar Cita")
                }
            }

            appointmentsState.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun showDateTimePicker(context: android.content.Context, onDateTimeSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(context, {
        _, year, month, dayOfMonth ->
        TimePickerDialog(context, {
            _, hourOfDay, minute ->
            calendar.set(year, month, dayOfMonth, hourOfDay, minute)
            onDateTimeSelected(calendar.time)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
}