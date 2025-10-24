package com.example.myapplicationv.screen


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Data class para citas
data class Appointment(
    val id: Long,
    val petName: String,
    val date: String,
    val time: String,
    val service: String,
    val status: String // "pending", "confirmed", "completed", "cancelled"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    onBack: () -> Unit,
    onAddAppointment: () -> Unit
) {
    // Datos de ejemplo - en una app real estos vendrían de tu base de datos
    val appointments = listOf(
        Appointment(1, "Firulais", "2024-01-15", "10:00", "Consulta general", "confirmed"),
        Appointment(2, "Michi", "2024-01-20", "11:30", "Vacunación", "pending"),
        Appointment(3, "Toby", "2024-01-25", "09:00", "Control de peso", "completed"),
        Appointment(4, "Firulais", "2024-02-01", "14:00", "Limpieza dental", "pending")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mis Citas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onAddAppointment) {
                        Icon(Icons.Filled.Add, contentDescription = "Agendar cita")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAppointment,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agendar cita")
            }
        }
    ) { innerPadding ->
        if (appointments.isEmpty()) {
            EmptyAppointmentsState(onAddAppointment = onAddAppointment, modifier = Modifier.padding(innerPadding))
        } else {
            AppointmentsList(
                appointments = appointments,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun EmptyAppointmentsState(onAddAppointment: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Filled.Event,
                contentDescription = "Sin citas",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No tienes citas programadas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Agenda tu primera cita para tu mascota",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAddAppointment,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agendar Cita")
            }
        }
    }
}

@Composable
private fun AppointmentsList(
    appointments: List<Appointment>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(appointments, key = { it.id }) { appointment ->
            AppointmentCard(appointment = appointment)
        }
    }
}

@Composable
private fun AppointmentCard(appointment: Appointment) {
    val statusColor = when (appointment.status) {
        "confirmed" -> MaterialTheme.colorScheme.primary
        "pending" -> MaterialTheme.colorScheme.secondary
        "completed" -> MaterialTheme.colorScheme.tertiary
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val statusText = when (appointment.status) {
        "confirmed" -> "Confirmada"
        "pending" -> "Pendiente"
        "completed" -> "Completada"
        "cancelled" -> "Cancelada"
        else -> appointment.status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.EventAvailable,
                contentDescription = "Cita",
                tint = statusColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.petName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${appointment.date} a las ${appointment.time}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = appointment.service,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}