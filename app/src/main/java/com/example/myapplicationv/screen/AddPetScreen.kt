package com.example.myapplicationv.screen

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.myapplicationv.viewmodel.AuthViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    vm: AuthViewModel,
    onPetAdded: () -> Unit,
    onBack: () -> Unit
) {
    // --- Estados para los campos del formulario ---
    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    // --- Lógica del Calendario ---
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            // Formateamos la fecha para guardarla como "YYYY-MM-DD"
            birthDate = "$year-${month + 1}-${dayOfMonth}"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    // No se pueden seleccionar fechas futuras para un nacimiento
    datePickerDialog.datePicker.maxDate = calendar.timeInMillis

    // --- Estructura de la Pantalla ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Agregar Mascota", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState()) // Permite hacer scroll si el contenido es muy largo
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), // Espacio entre elementos
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Icono de Cabecera ---
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = "Icono de Mascota",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "Nueva Mascota",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Campos del Formulario ---
            OutlinedTextField(
                value = petName,
                onValueChange = { petName = it },
                label = { Text("Nombre de la mascota *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Especie (ej. Perro, Gato) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Raza *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            // --- Selector de Fecha de Nacimiento ---
            OutlinedTextField(
                value = birthDate,
                onValueChange = { }, // El valor cambia solo desde el diálogo
                label = { Text("Fecha de Nacimiento") },
                readOnly = true, // Evita que el usuario escriba manualmente
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha de nacimiento"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Botón para Agregar Mascota ---
            Button(
                onClick = {
                    // Normalizamos el input: primera letra mayúscula, resto minúscula.
                    val normalizedSpecies = species.trim().replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }

                    vm.addPet(
                        nombre = petName.trim(),
                        especie = normalizedSpecies,
                        raza = breed.trim(),
                        fechaNacimiento = birthDate,
                        // otros campos como peso, color, etc., irían aquí si los tuvieras
                    )

                    // Una vez que se agrega, volvemos a la pantalla anterior
                    onPetAdded()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                // El botón solo se activa si los campos obligatorios están llenos
                enabled = petName.isNotBlank() && species.isNotBlank() && breed.isNotBlank()
            ) {
                Text("Agregar Mascota", style = MaterialTheme.typography.titleMedium)
            }

            Text("* Campos obligatorios", style = MaterialTheme.typography.labelSmall)
        }
    }
}
