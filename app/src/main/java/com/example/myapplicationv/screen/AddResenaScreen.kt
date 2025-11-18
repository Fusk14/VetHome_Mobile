package com.example.myapplicationv.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.myapplicationv.viewmodel.AuthViewModel
import kotlinx.coroutines.delay // Asegúrate de que esta importación esté presente
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResenaScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
    onResenaAdded: () -> Unit
) {
    var selectedMascotaId by remember { mutableStateOf<Long?>(null) }
    var calificacion by remember { mutableStateOf(0) }
    var comentario by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    // ✅ CORRECCIÓN: Agregar esta variable al inicio con las otras
    var showSuccess by remember { mutableStateOf(false) }

    val petsState by vm.pets.collectAsState()
    val resenasState by vm.resenas.collectAsState()

    // Cargar mascotas al iniciar
    LaunchedEffect(Unit) {
        if (petsState.pets.isEmpty()) {
            // El ViewModel ya carga las mascotas automáticamente
        }
    }

    // ✅ CORRECCIÓN: Mover LaunchedEffect aquí afuera, reacciona a showSuccess
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(500) // Esperar un poco para mostrar feedback
            onResenaAdded()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nueva Reseña") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
                .padding(16.dp)
        ) {
            // Mostrar error si existe
            if (showError) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Selección de Mascota
            Text(
                text = "Selecciona una mascota",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (petsState.pets.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No tienes mascotas registradas. Primero agrega una mascota para poder hacer una reseña.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    petsState.pets.forEach { mascota ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .selectable(
                                    selected = selectedMascotaId == mascota.id,
                                    onClick = { selectedMascotaId = mascota.id }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedMascotaId == mascota.id) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = mascota.nombre,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${mascota.especie} - ${mascota.raza}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (selectedMascotaId == mascota.id) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Calificación
            Text(
                text = "Calificación",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { index ->
                    IconButton(
                        onClick = { calificacion = index + 1 }
                    ) {
                        Icon(
                            imageVector = if (index < calificacion) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Estrella ${index + 1}",
                            tint = if (index < calificacion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            // Comentario
            Text(
                text = "Comentario",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(120.dp),
                placeholder = { Text("Escribe tu experiencia con la mascota...") },
                singleLine = false,
                maxLines = 5
            )

            // Contador de caracteres
            Text(
                text = "${comentario.length}/500 caracteres",
                style = MaterialTheme.typography.bodySmall,
                color = if (comentario.length > 500) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Botón de enviar - VERSIÓN CORREGIDA
            Button(
                onClick = {
                    if (selectedMascotaId == null) {
                        showError = true
                        errorMessage = "Por favor selecciona una mascota"
                        return@Button
                    }

                    if (calificacion == 0) {
                        showError = true
                        errorMessage = "Por favor selecciona una calificación"
                        return@Button
                    }

                    if (comentario.isBlank()) {
                        showError = true
                        errorMessage = "Por favor escribe un comentario"
                        return@Button
                    }

                    if (comentario.length > 500) {
                        showError = true
                        errorMessage = "El comentario no puede tener más de 500 caracteres"
                        return@Button
                    }

                    val mascotaSeleccionada = petsState.pets.find { it.id == selectedMascotaId }
                    val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    vm.crearResena(
                        mascotaId = selectedMascotaId!!,
                        mascotaNombre = mascotaSeleccionada?.nombre ?: "Mascota",
                        calificacion = calificacion,
                        comentario = comentario.trim(),
                        fecha = fechaActual
                    )

                    // ✅ CORRECCIÓN: Usar un estado para controlar la navegación
                    showSuccess = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                // ✅ CORRECCIÓN: Deshabilitar también si está cargando
                enabled = selectedMascotaId != null && calificacion > 0 && comentario.isNotBlank() && comentario.length <= 500 && !resenasState.isLoading
            ) {
                if (resenasState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Reseña")
                }
            }
        }
    }
}