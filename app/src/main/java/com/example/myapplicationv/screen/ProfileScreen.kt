package com.example.myapplicationv.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplicationv.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: AuthViewModel,
    onBack: () -> Unit
) {
    val profile by vm.profile.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.loadProfile()
    }

    var name by remember { mutableStateOf(profile.name) }
    var phone by remember { mutableStateOf(profile.phone) }
    var address by remember { mutableStateOf(profile.address) }
    var emergency by remember { mutableStateOf(profile.emergencyContact) }
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = profile.email, onValueChange = {}, label = { Text("Correo") }, enabled = false, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = emergency, onValueChange = { emergency = it }, label = { Text("Contacto de emergencia") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = { vm.updateProfile(name, phone, address, emergency) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }

            Divider(Modifier.padding(vertical = 8.dp))

            Text("Cambiar contraseña", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = currentPass,
                onValueChange = { currentPass = it },
                label = { Text("Contraseña actual") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = { Text("Nueva contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { vm.changePassword(currentPass, newPass, confirm) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !profile.isLoading
            ) {
                if (profile.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Actualizar contraseña")
                }
            }

            if (profile.successMessage != null)
                Text(profile.successMessage!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
            if (profile.errorMessage != null)
                Text(profile.errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
