package com.example.myapplicationv.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplicationv.data.local.pet.PetEntity
import com.example.myapplicationv.viewmodel.AuthViewModel

// 1. CORRECCIÓN: Se añade la anotación para CenterAlignedTopAppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetListScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
    onAddPet: () -> Unit,
    onPetDetail: (Long) -> Unit
) {
    // 2. CORRECCIÓN: Los estados deben ser declarados ANTES de usar el delegado 'by'
    val petsState by vm.pets.collectAsStateWithLifecycle()
    val currentUserState by vm.currentUser.collectAsStateWithLifecycle() // Renombrado para claridad
    val currentUser = currentUserState // Se usa aquí para acceder al valor, no como delegado

    // Cargar mascotas cuando se abre la pantalla
    LaunchedEffect(currentUser.clientId) {
        // 3. CORRECCIÓN: Se especifica el tipo explícitamente para takeIf
        // También se usa el valor directo de currentUser
        currentUser.clientId
            .takeIf { id: Long -> id > 0 }
            ?.let { clientId ->
                vm.loadPetsForClient(clientId)
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mis Mascotas",
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
                    IconButton(onClick = onAddPet) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar mascota")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPet,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar mascota")
            }
        }
    ) { innerPadding ->
        if (petsState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (petsState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error al cargar mascotas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = petsState.error ?: "Error desconocido",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (petsState.pets.isEmpty()) {
            EmptyPetsState(onAddPet = onAddPet, modifier = Modifier.padding(innerPadding))
        } else {
            PetsList(
                pets = petsState.pets,
                onPetClick = onPetDetail,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun EmptyPetsState(onAddPet: () -> Unit, modifier: Modifier = Modifier) {
    // ... (rest of the code unchanged)
}

@Composable
private fun PetsList(
    pets: List<PetEntity>,
    onPetClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // ... (rest of the code unchanged)
}

@Composable
private fun PetCard(pet: PetEntity, onClick: () -> Unit) {
    // ... (rest of the code unchanged)
}