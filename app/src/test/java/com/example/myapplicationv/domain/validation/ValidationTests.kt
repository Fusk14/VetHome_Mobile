package com.example.myapplicationv.domain.validation

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests completos para todas las validaciones de la aplicación móvil VetHome
 */
class ValidationTests {

    // ==================== TESTS DE VALIDACIÓN DE EMAIL ====================

    @Test
    fun `validateEmail - email válido retorna null`() {
        assertNull(validateEmail("usuario@ejemplo.com"))
        assertNull(validateEmail("test.email+tag@domain.co.uk"))
    }

    @Test
    fun `validateEmail - email vacío retorna mensaje de error`() {
        assertEquals("El email es obligatorio", validateEmail(""))
        assertEquals("El email es obligatorio", validateEmail("   "))
    }

    @Test
    fun `validateEmail - email con formato inválido retorna mensaje de error`() {
        assertEquals("Formato de email inválido", validateEmail("sinarroba"))
        assertEquals("Formato de email inválido", validateEmail("@sinusuario.com"))
        assertEquals("Formato de email inválido", validateEmail("usuario@"))
        assertEquals("Formato de email inválido", validateEmail("usuario@dominio"))
    }

    // ==================== TESTS DE VALIDACIÓN DE NOMBRE ====================

    @Test
    fun `validateNameLettersOnly - nombre válido retorna null`() {
        assertNull(validateNameLettersOnly("Juan"))
        assertNull(validateNameLettersOnly("María José"))
        assertNull(validateNameLettersOnly("José María"))
        assertNull(validateNameLettersOnly("Ángel"))
    }

    @Test
    fun `validateNameLettersOnly - nombre vacío retorna mensaje de error`() {
        assertEquals("El nombre es obligatorio", validateNameLettersOnly(""))
        assertEquals("El nombre es obligatorio", validateNameLettersOnly("   "))
    }

    @Test
    fun `validateNameLettersOnly - nombre con números retorna mensaje de error`() {
        assertEquals("Solo letras y espacios", validateNameLettersOnly("Juan123"))
        assertEquals("Solo letras y espacios", validateNameLettersOnly("123"))
    }

    @Test
    fun `validateNameLettersOnly - nombre con símbolos retorna mensaje de error`() {
        assertEquals("Solo letras y espacios", validateNameLettersOnly("Juan-Pérez"))
        assertEquals("Solo letras y espacios", validateNameLettersOnly("Juan@Pérez"))
    }

    // ==================== TESTS DE VALIDACIÓN DE TELÉFONO ====================

    @Test
    fun `validatePhoneDigitsOnly - teléfono válido retorna null`() {
        assertNull(validatePhoneDigitsOnly("12345678"))
        assertNull(validatePhoneDigitsOnly("123456789012345"))
        assertNull(validatePhoneDigitsOnly("987654321"))
    }

    @Test
    fun `validatePhoneDigitsOnly - teléfono vacío retorna mensaje de error`() {
        assertEquals("El teléfono es obligatorio", validatePhoneDigitsOnly(""))
        assertEquals("El teléfono es obligatorio", validatePhoneDigitsOnly("   "))
    }

    @Test
    fun `validatePhoneDigitsOnly - teléfono con letras retorna mensaje de error`() {
        assertEquals("Solo números", validatePhoneDigitsOnly("1234567a"))
        assertEquals("Solo números", validatePhoneDigitsOnly("abc123"))
    }

    @Test
    fun `validatePhoneDigitsOnly - teléfono muy corto retorna mensaje de error`() {
        assertEquals("Debe tener entre 8 y 15 dígitos", validatePhoneDigitsOnly("1234567"))
    }

    @Test
    fun `validatePhoneDigitsOnly - teléfono muy largo retorna mensaje de error`() {
        assertEquals("Debe tener entre 8 y 15 dígitos", validatePhoneDigitsOnly("1234567890123456"))
    }

    // ==================== TESTS DE VALIDACIÓN DE CONTRASEÑA ====================

    @Test
    fun `validateStrongPassword - contraseña válida retorna null`() {
        assertNull(validateStrongPassword("Password123!"))
        assertNull(validateStrongPassword("MiClave2024#"))
        assertNull(validateStrongPassword("Segura@123"))
    }

    @Test
    fun `validateStrongPassword - contraseña vacía retorna mensaje de error`() {
        assertEquals("La contraseña es obligatoria", validateStrongPassword(""))
    }

    @Test
    fun `validateStrongPassword - contraseña muy corta retorna mensaje de error`() {
        assertEquals("Mínimo 8 caracteres", validateStrongPassword("Pass1!"))
    }

    @Test
    fun `validateStrongPassword - contraseña sin mayúscula retorna mensaje de error`() {
        assertEquals("Debe incluir una mayúscula", validateStrongPassword("password123!"))
    }

    @Test
    fun `validateStrongPassword - contraseña sin minúscula retorna mensaje de error`() {
        assertEquals("Debe incluir una minúscula", validateStrongPassword("PASSWORD123!"))
    }

    @Test
    fun `validateStrongPassword - contraseña sin número retorna mensaje de error`() {
        assertEquals("Debe incluir un número", validateStrongPassword("Password!"))
    }

    @Test
    fun `validateStrongPassword - contraseña sin símbolo retorna mensaje de error`() {
        assertEquals("Debe incluir un símbolo", validateStrongPassword("Password123"))
    }

    @Test
    fun `validateStrongPassword - contraseña con espacios retorna mensaje de error`() {
        assertEquals("No debe contener espacios", validateStrongPassword("Password 123!"))
    }

    // ==================== TESTS DE VALIDACIÓN DE CONFIRMACIÓN ====================

    @Test
    fun `validateConfirm - contraseñas coinciden retorna null`() {
        assertNull(validateConfirm("Password123!", "Password123!"))
    }

    @Test
    fun `validateConfirm - confirmación vacía retorna mensaje de error`() {
        assertEquals("Confirma tu contraseña", validateConfirm("Password123!", ""))
    }

    @Test
    fun `validateConfirm - contraseñas no coinciden retorna mensaje de error`() {
        assertEquals("Las contraseñas no coinciden", validateConfirm("Password123!", "Password1234!"))
    }

    // ==================== TESTS DE VALIDACIÓN DE NOMBRE DE MASCOTA ====================

    @Test
    fun `validatePetName - nombre válido retorna null`() {
        assertNull(validatePetName("Max"))
        assertNull(validatePetName("Luna"))
        assertNull(validatePetName("Bella"))
    }

    @Test
    fun `validatePetName - nombre vacío retorna mensaje de error`() {
        assertEquals("El nombre de la mascota es obligatorio", validatePetName(""))
    }

    @Test
    fun `validatePetName - nombre muy corto retorna mensaje de error`() {
        assertEquals("El nombre debe tener al menos 2 caracteres", validatePetName("M"))
    }

    @Test
    fun `validatePetName - nombre con números retorna mensaje de error`() {
        assertEquals("Solo letras y espacios", validatePetName("Max123"))
    }

    // ==================== TESTS DE VALIDACIÓN DE ESPECIE ====================

    @Test
    fun `validateSpecies - especie válida retorna null`() {
        assertNull(validateSpecies("Perro"))
        assertNull(validateSpecies("Gato"))
        assertNull(validateSpecies("Conejo"))
        assertNull(validateSpecies("Ave"))
        assertNull(validateSpecies("Otro"))
    }

    @Test
    fun `validateSpecies - especie vacía retorna mensaje de error`() {
        assertEquals("La especie es obligatoria", validateSpecies(""))
    }

    @Test
    fun `validateSpecies - especie inválida retorna mensaje de error`() {
        assertEquals("Selecciona una especie válida", validateSpecies("Hamster"))
        assertEquals("Selecciona una especie válida", validateSpecies("perro")) // case sensitive
    }

    // ==================== TESTS DE VALIDACIÓN DE RAZA ====================

    @Test
    fun `validateBreed - raza válida retorna null`() {
        assertNull(validateBreed("Labrador"))
        assertNull(validateBreed("Siames"))
    }

    @Test
    fun `validateBreed - raza vacía retorna mensaje de error`() {
        assertEquals("La raza es obligatoria", validateBreed(""))
    }

    @Test
    fun `validateBreed - raza muy corta retorna mensaje de error`() {
        assertEquals("La raza debe tener al menos 2 caracteres", validateBreed("L"))
    }

    // ==================== TESTS DE VALIDACIÓN DE FECHA ====================

    @Test
    fun `validateBirthDate - fecha válida retorna null`() {
        assertNull(validateBirthDate("2020-01-15"))
        assertNull(validateBirthDate("2015-12-31"))
    }

    @Test
    fun `validateBirthDate - fecha vacía retorna null (opcional)`() {
        assertNull(validateBirthDate(""))
    }

    @Test
    fun `validateBirthDate - formato inválido retorna mensaje de error`() {
        assertEquals("Formato de fecha inválido (YYYY-MM-DD)", validateBirthDate("15-01-2020"))
        assertEquals("Formato de fecha inválido (YYYY-MM-DD)", validateBirthDate("2020/01/15"))
        assertEquals("Formato de fecha inválido (YYYY-MM-DD)", validateBirthDate("2020-1-15"))
    }

    // ==================== TESTS DE VALIDACIÓN DE PESO ====================

    @Test
    fun `validateWeight - peso válido retorna null`() {
        assertNull(validateWeight("10.5"))
        assertNull(validateWeight("25"))
        assertNull(validateWeight("0.5"))
    }

    @Test
    fun `validateWeight - peso vacío retorna null (opcional)`() {
        assertNull(validateWeight(""))
    }

    @Test
    fun `validateWeight - peso no numérico retorna mensaje de error`() {
        assertEquals("El peso debe ser un número válido", validateWeight("abc"))
        assertEquals("El peso debe ser un número válido", validateWeight("10kg"))
    }

    @Test
    fun `validateWeight - peso cero o negativo retorna mensaje de error`() {
        assertEquals("El peso debe ser mayor a 0", validateWeight("0"))
        assertEquals("El peso debe ser mayor a 0", validateWeight("-5"))
    }

    @Test
    fun `validateWeight - peso excesivo retorna mensaje de error`() {
        assertEquals("El peso parece incorrecto (máx. 200 kg)", validateWeight("201"))
        assertEquals("El peso parece incorrecto (máx. 200 kg)", validateWeight("500"))
    }

    // ==================== TESTS DE VALIDACIÓN DE COLOR ====================

    @Test
    fun `validateColor - color válido retorna null`() {
        assertNull(validateColor("Negro"))
        assertNull(validateColor("Blanco y Negro"))
    }

    @Test
    fun `validateColor - color vacío retorna null (opcional)`() {
        assertNull(validateColor(""))
    }

    @Test
    fun `validateColor - color con números retorna mensaje de error`() {
        assertEquals("Solo letras y espacios", validateColor("Negro123"))
    }

    // ==================== TESTS DE VALIDACIÓN DE NOTAS MÉDICAS ====================

    @Test
    fun `validateMedicalNotes - notas válidas retorna null`() {
        assertNull(validateMedicalNotes("Vacunado"))
        assertNull(validateMedicalNotes("Requiere seguimiento"))
    }

    @Test
    fun `validateMedicalNotes - notas vacías retorna null (opcional)`() {
        assertNull(validateMedicalNotes(""))
    }

    @Test
    fun `validateMedicalNotes - notas muy largas retorna mensaje de error`() {
        val notasLargas = "a".repeat(501)
        assertEquals("Máximo 500 caracteres", validateMedicalNotes(notasLargas))
    }

    // ==================== TESTS DE VALIDACIÓN DE DIRECCIÓN ====================

    @Test
    fun `validateAddress - dirección válida retorna null`() {
        assertNull(validateAddress("Calle Principal 123"))
        assertNull(validateAddress("Av. Los Leones 456"))
    }

    @Test
    fun `validateAddress - dirección vacía retorna null (opcional)`() {
        assertNull(validateAddress(""))
    }

    @Test
    fun `validateAddress - dirección muy corta retorna mensaje de error`() {
        assertEquals("Mínimo 5 caracteres", validateAddress("Casa"))
        assertEquals("Mínimo 5 caracteres", validateAddress("123"))
        assertEquals("Mínimo 5 caracteres", validateAddress("Ab"))
    }

    @Test
    fun `validateAddress - dirección muy larga retorna mensaje de error`() {
        val direccionLarga = "a".repeat(201)
        assertEquals("Máximo 200 caracteres", validateAddress(direccionLarga))
    }

    // ==================== TESTS DE VALIDACIÓN DE CONTACTO DE EMERGENCIA ====================

    @Test
    fun `validateEmergencyContact - contacto válido retorna null`() {
        assertNull(validateEmergencyContact("12345678"))
        assertNull(validateEmergencyContact("987654321"))
    }

    @Test
    fun `validateEmergencyContact - contacto vacío retorna null (opcional)`() {
        assertNull(validateEmergencyContact(""))
    }

    @Test
    fun `validateEmergencyContact - contacto con letras retorna mensaje de error`() {
        assertEquals("Solo números", validateEmergencyContact("1234567a"))
    }

    @Test
    fun `validateEmergencyContact - contacto muy corto retorna mensaje de error`() {
        assertEquals("Debe tener entre 8 y 15 dígitos", validateEmergencyContact("1234567"))
    }

    @Test
    fun `validateEmergencyContact - contacto muy largo retorna mensaje de error`() {
        assertEquals("Debe tener entre 8 y 15 dígitos", validateEmergencyContact("1234567890123456"))
    }
}

