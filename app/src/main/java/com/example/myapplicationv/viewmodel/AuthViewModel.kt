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
import com.example.myapplicationv.data.local.storage.UserPreferences
import kotlinx.coroutines.flow.first

// ----------------- ESTADOS DE UI (observable con StateFlow) -----------------

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
    val currentClient: ClientUiState? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val pass: String = "",
    val confirm: String = "",

    val nameError: String? = null,
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

data class ClientUiState(
    val clientId: Long = 0L,
    val name: String = "",
    val email: String = "",
    val petsCount: Int = 0
)

data class PetsUiState(
    val pets: List<PetEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPet: PetEntity? = null
)

// 🆕 NUEVO: Estado para manejar mensajes de sesión
data class SessionState(
    val isLoggedIn: Boolean = false,
    val loginMessage: String? = null,
    val logoutMessage: String? = null,
    val showMessage: Boolean = false
)

class AuthViewModel(
    private val repository: VetRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // Estados para sesión
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    private val _currentUser = MutableStateFlow(ClientUiState())
    val currentUser: StateFlow<ClientUiState> = _currentUser

    // 🆕 NUEVO: Estado para mensajes de sesión
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState

    // Flujos de estado existentes
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    private val _pets = MutableStateFlow(PetsUiState())
    val pets: StateFlow<PetsUiState> = _pets

    // Bloque INIT para verificar sesión al inicio del ViewModel
    init {
        checkUserSession()
    }

    // Verificar si hay sesión activa y restaurar el estado
    private fun checkUserSession() {
        viewModelScope.launch {
            val loggedIn = userPreferences.isLoggedIn.first()
            _isUserLoggedIn.value = loggedIn
            _sessionState.update { it.copy(isLoggedIn = loggedIn) }

            if (loggedIn) {
                val email = userPreferences.userEmail.first()
                val name = userPreferences.userName.first()
                val id = userPreferences.userId.first()

                val clientState = ClientUiState(
                    clientId = id.toLongOrNull() ?: 0L,
                    name = name,
                    email = email
                )

                _currentUser.value = clientState
                _login.update { it.copy(currentClient = clientState) }
                id.toLongOrNull()?.let { loadPetsForClient(it) }
            }
        }
    }

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

    // 🔄 MODIFICADO: submitLogin con mensajes de sesión
    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(500)

            val result = repository.login(s.email.trim(), s.pass)

            _login.update {
                if (result.isSuccess) {
                    val client = result.getOrNull()

                    client?.let {
                        userPreferences.setUserInfo(it.email, it.name, it.id.toString())
                        userPreferences.setLoggedIn(true)
                    }

                    val clientState = client?.let {
                        ClientUiState(
                            clientId = it.id,
                            name = it.name,
                            email = it.email
                        )
                    }

                    _isUserLoggedIn.value = true
                    _currentUser.value = clientState ?: ClientUiState()

                    // 🆕 NUEVO: Mostrar mensaje de éxito
                    _sessionState.update { session ->
                        session.copy(
                            isLoggedIn = true,
                            loginMessage = "¡Bienvenido ${client?.name ?: "Usuario"}!",
                            showMessage = true,
                            logoutMessage = null
                        )
                    }

                    client?.id?.let { loadPetsForClient(it) }

                    it.copy(
                        isSubmitting = false,
                        success = true,
                        errorMsg = null,
                        currentClient = clientState
                    )
                } else {
                    // 🆕 NUEVO: Mensaje de error
                    _sessionState.update { session ->
                        session.copy(
                            loginMessage = "Error al iniciar sesión: ${result.exceptionOrNull()?.message}",
                            showMessage = true
                        )
                    }
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

    // 🆕 NUEVO: Función para limpiar mensajes de sesión
    fun clearSessionMessage() {
        _sessionState.update {
            it.copy(
                showMessage = false,
                loginMessage = null,
                logoutMessage = null
            )
        }
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

    fun onAddressChange(value: String) {
        _register.update { it.copy(address = value, addressError = validateAddress(value)) }
        recomputeRegisterCanSubmit()
    }

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
        val noErrors = listOf(
            s.nameError, s.emailError, s.phoneError,
            s.addressError, s.emergencyContactError, s.passError, s.confirmError
        ).all { it == null }

        val filled = s.name.isNotBlank() && s.email.isNotBlank() &&
                s.phone.isNotBlank() && s.pass.isNotBlank() &&
                s.confirm.isNotBlank()

        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(700)

            val result = repository.register(
                name = s.name.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),
                address = s.address.ifBlank { null },
                emergencyContact = s.emergencyContact.ifBlank { null },
                password = s.pass
            )

            _register.update {
                if (result.isSuccess) {
                    // 🆕 NUEVO: Mensaje de registro exitoso
                    _sessionState.update { session ->
                        session.copy(
                            loginMessage = "¡Cuenta creada exitosamente! Ya puedes iniciar sesión",
                            showMessage = true
                        )
                    }
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

                _currentUser.update { client ->
                    client.copy(petsCount = petsList.size)
                }

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

    fun updatePetWeight(petId: Long, nuevoPeso: Double) {
        viewModelScope.launch {
            try {
                repository.updatePetWeight(petId, nuevoPeso)
                _login.value.currentClient?.clientId?.let { loadPetsForClient(it) }
            } catch (e: Exception) {
                _pets.update { it.copy(error = "Error al actualizar peso") }
            }
        }
    }

    fun deletePet(petId: Long) {
        viewModelScope.launch {
            try {
                repository.deletePet(petId)
                _login.value.currentClient?.clientId?.let { loadPetsForClient(it) }
            } catch (e: Exception) {
                _pets.update { it.copy(error = "Error al eliminar mascota") }
            }
        }
    }

    // 🔄 MODIFICADO: logout con mensaje de sesión
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUserData()

            // 🆕 NUEVO: Mensaje de logout
            _sessionState.update { session ->
                session.copy(
                    isLoggedIn = false,
                    logoutMessage = "Sesión cerrada correctamente",
                    showMessage = true,
                    loginMessage = null
                )
            }

            _isUserLoggedIn.value = false
            _currentUser.value = ClientUiState()
            _pets.update { PetsUiState() }
            _login.update { LoginUiState() }
            _register.update { RegisterUiState() }
        }
    }
}