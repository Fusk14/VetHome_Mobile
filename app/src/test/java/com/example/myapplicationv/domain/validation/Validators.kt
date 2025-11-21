package com.example.myapplicationv.domain.validation

import java.util.regex.Pattern

// ==================== PATRONES MANUALES PARA TESTS ====================

// Reemplaza Patterns.EMAIL_ADDRESS con patrón manual
private val EMAIL_PATTERN = Pattern.compile(
    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
)

// ==================== VALIDACIONES EXISTENTES (CLIENTES) ====================

// Valida que el email no esté vacío y cumpla patrón de email
fun validateEmail(email: String): String? {
    if (email.isBlank()) return "El email es obligatorio"
    val ok = EMAIL_PATTERN.matcher(email).matches()
    return if (!ok) "Formato de email inválido" else null
}

// Valida que el nombre contenga solo letras y espacios (sin números)
fun validateNameLettersOnly(name: String): String? {
    if (name.isBlank()) return "El nombre es obligatorio"
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    return if (!regex.matches(name)) "Solo letras y espacios" else null
}

// Valida que el teléfono tenga solo dígitos y una longitud razonable
fun validatePhoneDigitsOnly(phone: String): String? {
    if (phone.isBlank()) return "El teléfono es obligatorio"
    if (!phone.all { it.isDigit() }) return "Solo números"
    if (phone.length !in 8..15) return "Debe tener entre 8 y 15 dígitos"
    return null
}

// Valida seguridad de la contraseña (mín. 8, mayús, minús, número y símbolo; sin espacios)
fun validateStrongPassword(pass: String): String? {
    if (pass.isBlank()) return "La contraseña es obligatoria"
    if (pass.length < 8) return "Mínimo 8 caracteres"
    if (!pass.any { it.isUpperCase() }) return "Debe incluir una mayúscula"
    if (!pass.any { it.isLowerCase() }) return "Debe incluir una minúscula"
    if (!pass.any { it.isDigit() }) return "Debe incluir un número"
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo"
    if (pass.contains(' ')) return "No debe contener espacios"
    return null
}

// Valida que la confirmación coincida con la contraseña
fun validateConfirm(pass: String, confirm: String): String? {
    if (confirm.isBlank()) return "Confirma tu contraseña"
    return if (pass != confirm) "Las contraseñas no coinciden" else null
}

// ==================== NUEVAS VALIDACIONES PARA VETERINARIA ====================

// Valida que el nombre de la mascota no esté vacío y tenga longitud mínima
fun validatePetName(nombre: String): String? {
    if (nombre.isBlank()) return "El nombre de la mascota es obligatorio"
    if (nombre.length < 2) return "El nombre debe tener al menos 2 caracteres"
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    return if (!regex.matches(nombre)) "Solo letras y espacios" else null
}

// Valida que la especie esté entre las permitidas
fun validateSpecies(especie: String): String? {
    if (especie.isBlank()) return "La especie es obligatoria"
    val especiesValidas = listOf("Perro", "Gato", "Conejo", "Ave", "Otro")
    return if (!especiesValidas.contains(especie)) "Selecciona una especie válida" else null
}

// Valida que la raza no esté vacía
fun validateBreed(raza: String): String? {
    if (raza.isBlank()) return "La raza es obligatoria"
    if (raza.length < 2) return "La raza debe tener al menos 2 caracteres"
    return null
}

// Valida el formato de fecha (YYYY-MM-DD) - opcional
fun validateBirthDate(fecha: String): String? {
    if (fecha.isBlank()) return null
    val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    return if (!regex.matches(fecha)) "Formato de fecha inválido (YYYY-MM-DD)" else null
}

// Valida que el peso sea un número positivo y razonable
fun validateWeight(peso: String): String? {
    if (peso.isBlank()) return null
    val weightValue = peso.toDoubleOrNull()
    return when {
        weightValue == null -> "El peso debe ser un número válido"
        weightValue <= 0 -> "El peso debe ser mayor a 0"
        weightValue > 200 -> "El peso parece incorrecto (máx. 200 kg)"
        else -> null
    }
}

// Valida el color (solo letras y espacios) - opcional
fun validateColor(color: String): String? {
    if (color.isBlank()) return null
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    return if (!regex.matches(color)) "Solo letras y espacios" else null
}

// Valida notas médicas (longitud máxima) - opcional
fun validateMedicalNotes(notas: String): String? {
    if (notas.isBlank()) return null
    if (notas.length > 500) return "Máximo 500 caracteres"
    return null
}

// Valida dirección (longitud mínima y máxima) - opcional
fun validateAddress(direccion: String): String? {
    if (direccion.isBlank()) return null
    if (direccion.length < 5) return "Mínimo 5 caracteres"
    if (direccion.length > 200) return "Máximo 200 caracteres"
    return null
}

// Valida contacto de emergencia (mismo formato que teléfono) - opcional
fun validateEmergencyContact(contacto: String): String? {
    if (contacto.isBlank()) return null
    if (!contacto.all { it.isDigit() }) return "Solo números"
    if (contacto.length !in 8..15) return "Debe tener entre 8 y 15 dígitos"
    return null
}