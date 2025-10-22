package com.example.myapplicationv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationv.data.local.pet.PetEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.myapplicationv.domain.validation.*
import com.example.myapplicationv.data.repository.VetRepository

// ----------------- ESTADOS DE UI (observable con StateFlow) -----------------

data class LoginUiState(                                   // Estado de la pantalla Login
    val email: String = "",                                // Campo email
    val pass: String = "",                                 // Campo contraseña (texto)
    val emailError: String? = null,                        // Error de email
    val passError: String? = null,                         // (Opcional) error de pass en login
    val isSubmitting: Boolean = false,                     // Flag de carga
    val canSubmit: Boolean = false,                        // Habilitar botón
    val success: Boolean = false,                          // Resultado OK
    val errorMsg: String? = null,                          // Error global (credenciales inválidas)
    val currentClient: ClientUiState? = null               // Info del cliente logueado
)

data class RegisterUiState(                                // Estado de la pantalla Registro
    val name: String = "",                                 // Nombre
    val email: String = "",                                // Email
    val phone: String = "",                                // Teléfono
    val address: String = "",                              // Dirección
    val emergencyContact: String = "",                     // Contacto emergencia
    val pass: String = "",                                 // Contraseña
    val confirm: String = "",                              // Confirmación

    val nameError: String? = null,                         // Errores por campo
    val emailError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val emergencyContactError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,

    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

// Estado para información del cliente logueado
data class ClientUiState(
    val clientId: Long = 0L,
    val name: String = "",
    val email: String = "",
    val petsCount: Int = 0
)

//  Estado para gestión de mascotas
data class PetsUiState(
    val pets: List<PetEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPet: PetEntity? = null
)

class AuthViewModel(
    private val repository: VetRepository
) : ViewModel() {

    // Flujos de estado para observar desde la UI
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    //  Estado para mascotas
    private val _pets = MutableStateFlow(PetsUiState())
    val pets: StateFlow<PetsUiState> = _pets

    // Login handlers

    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(500)

            // CONSULTA REAL a la BD vía VetRepository
            val result = repository.login(s.email.trim(), s.pass)

            _login.update {
                if (result.isSuccess) {
                    val client = result.getOrNull()
                    // 🆕 NUEVO: Guardar info del cliente y cargar sus mascotas
                    val clientState = client?.let {
                        ClientUiState(
                            clientId = it.id,
                            name = it.name,
                            email = it.email
                        )
                    }
                    // Cargar mascotas del cliente
                    client?.id?.let { loadPetsForClient(it) }

                    it.copy(
                        isSubmitting = false,
                        success = true,
                        errorMsg = null,
                        currentClient = clientState
                    )
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error de autenticación"
                    )
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // Registro handlers

    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update {
            it.copy(name = filtered, nameError = validateNameLettersOnly(filtered))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update {
            it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly))
        }
        recomputeRegisterCanSubmit()
    }

    // Handler para dirección
    fun onAddressChange(value: String) {
        _register.update { it.copy(address = value, addressError = validateAddress(value)) }
        recomputeRegisterCanSubmit()
    }

    // Handler para contacto de emergencia
    fun onEmergencyContactChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update {
            it.copy(
                emergencyContact = digitsOnly,
                emergencyContactError = validateEmergencyContact(digitsOnly)
            )
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) }
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        // Incluir nuevos campos en la validación
        val noErrors = listOf(
            s.nameError, s.emailError, s.phoneError,
            s.addressError, s.emergencyContactError, s.passError, s.confirmError
        ).all { it == null }

        val filled = s.name.isNotBlank() && s.email.isNotBlank() &&
                s.phone.isNotBlank() && s.pass.isNotBlank() &&
                s.confirm.isNotBlank() // Dirección y contacto son opcionales

        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(700)

            // INSERTA EN BD vía VetRepository (con nuevos campos)
            val result = repository.register(
                name = s.name.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),
                address = s.address.ifBlank { null },        // 🆕 NUEVO: Dirección
                emergencyContact = s.emergencyContact.ifBlank { null }, // 🆕 NUEVO: Contacto
                password = s.pass
            )

            _register.update {
                if (result.isSuccess) {
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "No se pudo registrar"
                    )
                }
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    // OPERACIONES PARA MASCOTAS

    fun loadPetsForClient(clientId: Long) {
        viewModelScope.launch {
            _pets.update { it.copy(isLoading = true, error = null) }
            try {
                val petsList = repository.getPetsByOwner(clientId)
                _pets.update { it.copy(pets = petsList, isLoading = false) }

                // 🆕 Actualizar contador de mascotas en el estado de login
                _login.update { loginState ->
                    loginState.currentClient?.let { client ->
                        loginState.copy(
                            currentClient = client.copy(petsCount = petsList.size)
                        )
                    } ?: loginState
                }
            } catch (e: Exception) {
                _pets.update { it.copy(error = "Error al cargar mascotas", isLoading = false) }
            }
        }
    }

    // Agregar mascota
    fun addPet(
        nombre: String,
        especie: String,
        raza: String,
        fechaNacimiento: String? = null,
        peso: Double? = null,
        color: String? = null,
        notasMedicas: String? = null
    ) {
        val clientId = _login.value.currentClient?.clientId ?: return

        viewModelScope.launch {
            _pets.update { it.copy(isLoading = true, error = null) }
            try {
                val result = repository.addPet(
                    ownerId = clientId,
                    nombre = nombre,
                    especie = especie,
                    raza = raza,
                    fechaNacimiento = fechaNacimiento,
                    peso = peso,
                    color = color,
                    notasMedicas = notasMedicas
                )

                if (result.isSuccess) {
                    // Recargar la lista de mascotas
                    loadPetsForClient(clientId)
                } else {
                    _pets.update { it.copy(
                        error = result.exceptionOrNull()?.message ?: "Error al agregar mascota",
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                _pets.update { it.copy(error = "Error al agregar mascota", isLoading = false) }
            }
        }
    }

    // Actualizar peso de mascota
    fun updatePetWeight(petId: Long, nuevoPeso: Double) {
        viewModelScope.launch {
            try {
                repository.updatePetWeight(petId, nuevoPeso)
                // Recargar mascotas para reflejar el cambio
                _login.value.currentClient?.clientId?.let { loadPetsForClient(it) }
            } catch (e: Exception) {
                _pets.update { it.copy(error = "Error al actualizar peso") }
            }
        }
    }

    // Eliminar mascota
    fun deletePet(petId: Long) {
        viewModelScope.launch {
            try {
                repository.deletePet(petId)
                // Recargar mascotas para reflejar el cambio
                _login.value.currentClient?.clientId?.let { loadPetsForClient(it) }
            } catch (e: Exception) {
                _pets.update { it.copy(error = "Error al eliminar mascota") }
            }
        }
    }

    // Cerrar sesión
    fun logout() {
        _login.update { LoginUiState() }
        _pets.update { PetsUiState() }
    }
}