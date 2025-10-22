package com.example.myapplicationv.domain.validation

import android.util.Patterns // Usamos el patrón estándar de Android para emails

// ==================== VALIDACIONES EXISTENTES (CLIENTES) ====================

// Valida que el email no esté vacío y cumpla patrón de email
fun validateEmail(email: String): String? {                            // Retorna String? (mensaje) o null si está OK
    if (email.isBlank()) return "El email es obligatorio"              // Regla 1: no vacío
    val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches()           // Regla 2: coincide con patrón de email
    return if (!ok) "Formato de email inválido" else null              // Si no cumple, devolvemos mensaje
}

// Valida que el nombre contenga solo letras y espacios (sin números)
fun validateNameLettersOnly(name: String): String? {                   // Valida nombre
    if (name.isBlank()) return "El nombre es obligatorio"              // Regla 1: no vacío
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")                      // Regla 2: solo letras y espacios (con tildes/ñ)
    return if (!regex.matches(name)) "Solo letras y espacios" else null// Mensaje si falla
}

// Valida que el teléfono tenga solo dígitos y una longitud razonable
fun validatePhoneDigitsOnly(phone: String): String? {                  // Valida teléfono
    if (phone.isBlank()) return "El teléfono es obligatorio"           // Regla 1: no vacío
    if (!phone.all { it.isDigit() }) return "Solo números"             // Regla 2: todos dígitos
    if (phone.length !in 8..15) return "Debe tener entre 8 y 15 dígitos" // Regla 3: tamaño razonable
    return null                                                        // OK
}

// Valida seguridad de la contraseña (mín. 8, mayús, minús, número y símbolo; sin espacios)
fun validateStrongPassword(pass: String): String? {                    // Requisitos mínimos de seguridad
    if (pass.isBlank()) return "La contraseña es obligatoria"          // No vacío
    if (pass.length < 8) return "Mínimo 8 caracteres"                  // Largo mínimo
    if (!pass.any { it.isUpperCase() }) return "Debe incluir una mayúscula" // Al menos 1 mayúscula
    if (!pass.any { it.isLowerCase() }) return "Debe incluir una minúscula" // Al menos 1 minúscula
    if (!pass.any { it.isDigit() }) return "Debe incluir un número"         // Al menos 1 número
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo" // Al menos 1 símbolo
    if (pass.contains(' ')) return "No debe contener espacios"          // Sin espacios
    return null                                                         // OK
}

// Valida que la confirmación coincida con la contraseña
fun validateConfirm(pass: String, confirm: String): String? {          // Confirmación de contraseña
    if (confirm.isBlank()) return "Confirma tu contraseña"             // No vacío
    return if (pass != confirm) "Las contraseñas no coinciden" else null // Deben ser iguales
}

// ==================== NUEVAS VALIDACIONES PARA VETERINARIA ====================

// Valida que el nombre de la mascota no esté vacío y tenga longitud mínima
fun validatePetName(nombre: String): String? {                         // Valida nombre de mascota
    if (nombre.isBlank()) return "El nombre de la mascota es obligatorio" // Regla 1: no vacío
    if (nombre.length < 2) return "El nombre debe tener al menos 2 caracteres" // Regla 2: longitud mínima
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")                      // Regla 3: solo letras y espacios
    return if (!regex.matches(nombre)) "Solo letras y espacios" else null // Mensaje si falla
}

// Valida que la especie esté entre las permitidas
fun validateSpecies(especie: String): String? {                        // Valida especie
    if (especie.isBlank()) return "La especie es obligatoria"          // Regla 1: no vacío
    val especiesValidas = listOf("Perro", "Gato", "Conejo", "Ave", "Otro") // Regla 2: especies permitidas
    return if (!especiesValidas.contains(especie)) "Selecciona una especie válida" else null
}

// Valida que la raza no esté vacía
fun validateBreed(raza: String): String? {                             // Valida raza
    if (raza.isBlank()) return "La raza es obligatoria"                // Regla 1: no vacío
    if (raza.length < 2) return "La raza debe tener al menos 2 caracteres" // Regla 2: longitud mínima
    return null                                                        // OK
}

// Valida el formato de fecha (YYYY-MM-DD) - opcional
fun validateBirthDate(fecha: String): String? {                        // Valida fecha nacimiento
    if (fecha.isBlank()) return null                                   // Opcional, si está vacío OK
    val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")                       // Regla: formato YYYY-MM-DD
    return if (!regex.matches(fecha)) "Formato de fecha inválido (YYYY-MM-DD)" else null
}

// Valida que el peso sea un número positivo y razonable
fun validateWeight(peso: String): String? {                            // Valida peso
    if (peso.isBlank()) return null                                    // Opcional, si está vacío OK
    val weightValue = peso.toDoubleOrNull()                            // Convertir a número
    return when {
        weightValue == null -> "El peso debe ser un número válido"     // Regla 1: debe ser número
        weightValue <= 0 -> "El peso debe ser mayor a 0"               // Regla 2: positivo
        weightValue > 200 -> "El peso parece incorrecto (máx. 200 kg)" // Regla 3: máximo razonable
        else -> null                                                   // OK
    }
}

// Valida el color (solo letras y espacios) - opcional
fun validateColor(color: String): String? {                            // Valida color
    if (color.isBlank()) return null                                   // Opcional, si está vacío OK
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")                      // Regla: solo letras y espacios
    return if (!regex.matches(color)) "Solo letras y espacios" else null
}

// Valida notas médicas (longitud máxima) - opcional
fun validateMedicalNotes(notas: String): String? {                     // Valida notas médicas
    if (notas.isBlank()) return null                                   // Opcional, si está vacío OK
    if (notas.length > 500) return "Máximo 500 caracteres"             // Regla: longitud máxima
    return null                                                        // OK
}

// Valida dirección (longitud mínima y máxima) - opcional
fun validateAddress(direccion: String): String? {                      // Valida dirección
    if (direccion.isBlank()) return null                               // Opcional, si está vacío OK
    if (direccion.length < 5) return "Mínimo 5 caracteres"             // Regla 1: longitud mínima
    if (direccion.length > 200) return "Máximo 200 caracteres"         // Regla 2: longitud máxima
    return null                                                        // OK
}

// Valida contacto de emergencia (mismo formato que teléfono) - opcional
fun validateEmergencyContact(contacto: String): String? {              // Valida contacto emergencia
    if (contacto.isBlank()) return null                                // Opcional, si está vacío OK
    if (!contacto.all { it.isDigit() }) return "Solo números"          // Regla 1: todos dígitos
    if (contacto.length !in 8..15) return "Debe tener entre 8 y 15 dígitos" // Regla 2: tamaño razonable
    return null                                                        // OK
}