package com.example.myapplicationv.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplicationv.data.local.appointment.AppointmentEntity
import com.example.myapplicationv.data.local.pet.PetEntity
import com.example.myapplicationv.data.local.user.ClientEntity
import com.example.myapplicationv.navigation.Route
import com.example.myapplicationv.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(viewModel: AuthViewModel, navController: NavController) {
    // ✅ CORREGIDO: Usando collectAsState con StateFlow
    val clientsState by viewModel.allClients.collectAsState()
    val petsState by viewModel.allPets.collectAsState()
    val appointmentsState by viewModel.allAppointments.collectAsState()

    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }
    var petToDelete by remember { mutableStateOf<PetEntity?>(null) }
    var appointmentToDelete by remember { mutableStateOf<AppointmentEntity?>(null) }

    fun openDeleteClientDialog(client: ClientEntity) { clientToDelete = client }
    fun closeDeleteClientDialog() { clientToDelete = null }

    fun openDeletePetDialog(pet: PetEntity) { petToDelete = pet }
    fun closeDeletePetDialog() { petToDelete = null }

    fun openDeleteAppointmentDialog(appointment: AppointmentEntity) { appointmentToDelete = appointment }
    fun closeDeleteAppointmentDialog() { appointmentToDelete = null }

    fun navigateToEditUser(userId: Long) {
        navController.navigate(Route.EditUser.createRoute(userId))
    }

    // Placeholder for pet/appointment edit navigation
    fun navigateToEditPet(petId: Long) {
        // TODO: navController.navigate(Route.EditPet.createRoute(petId))
    }

    fun navigateToEditAppointment(appointmentId: Long) {
        // TODO: navController.navigate(Route.EditAppointment.createRoute(appointmentId))
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllClients()
        viewModel.loadAllPets()
        viewModel.loadAllAppointments()
    }

    clientToDelete?.let {
        DeletionConfirmDialog(
            itemName = it.name,
            itemType = "user",
            onConfirm = {
                viewModel.deleteUserAndData(it.id)
                closeDeleteClientDialog()
            },
            onDismiss = { closeDeleteClientDialog() },
            additionalInfo = "Esto también eliminará todas sus mascotas y citas. Esta acción es irreversible."
        )
    }

    petToDelete?.let {
        DeletionConfirmDialog(
            itemName = it.nombre,
            itemType = "Mascota",
            onConfirm = {
                viewModel.deletePetById(it.id)
                closeDeletePetDialog()
            },
            onDismiss = { closeDeletePetDialog() }
        )
    }

    appointmentToDelete?.let {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        DeletionConfirmDialog(
            itemName = "cita en ${dateFormat.format(it.date)}",
            itemType = "appointment",
            onConfirm = {
                viewModel.deleteAppointmentById(it.id)
                closeDeleteAppointmentDialog()
            },
            onDismiss = { closeDeleteAppointmentDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Panel de control de administradores") })
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            item {
                Text("Usuarios Registrados", style = MaterialTheme.typography.headlineMedium)
            }
            items(clientsState.clients) { client ->
                UserItem(
                    client,
                    onDelete = { openDeleteClientDialog(client) },
                    onEdit = { navigateToEditUser(client.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Mascotas Registradas", style = MaterialTheme.typography.headlineMedium)
            }
            items(petsState.pets) { pet ->
                PetItem(
                    pet,
                    onDelete = { openDeletePetDialog(pet) },
                    onEdit = { navigateToEditPet(pet.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Todas las citas", style = MaterialTheme.typography.headlineMedium)
            }
            items(appointmentsState.appointments) { appointment ->
                AppointmentItem(
                    appointment,
                    onDelete = { openDeleteAppointmentDialog(appointment) },
                    onEdit = { navigateToEditAppointment(appointment.id) }
                )
            }
        }
    }
}

@Composable
fun UserItem(client: ClientEntity, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${client.name}")
            Text(text = "Email: ${client.email}")
            Text(text = "Telefóno: ${client.phone}")
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = onEdit) {
                    Text("Editar")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onDelete) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun PetItem(pet: PetEntity, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${pet.nombre}")
            Text(text = "Especie: ${pet.especie}")
            Text(text = "Raza: ${pet.raza}")
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = onEdit) {
                    Text("Editar")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onDelete) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(appointment: AppointmentEntity, onDelete: () -> Unit, onEdit: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    Card(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Fecha: ${dateFormat.format(appointment.date)}")
            Text(text = "Razón: ${appointment.reason}")
            Text(text = "Owner ID: ${appointment.ownerId}")
            Text(text = "Pet ID: ${appointment.petId}")
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = onEdit) {
                    Text("Editar")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onDelete) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun DeletionConfirmDialog(
    itemName: String,
    itemType: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    additionalInfo: String = "Esta acción no se puede deshacer."
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar eliminación") },
        text = { Text("Seguro que deseas eliminar el $itemType '$itemName'? $additionalInfo") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}