package com.example.myapplicationv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationv.data.local.appointment.AppointmentEntity
import com.example.myapplicationv.data.local.pet.PetEntity
import com.example.myapplicationv.data.local.storage.UserPreferences
import com.example.myapplicationv.data.repository.VetRepository
import com.example.myapplicationv.domain.validation.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

// --- ESTADOS DE PERFIL (NUEVOS) ---
data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

// --- DATA CLASSES DE ESTADO (EXISTENTES) ---
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
    val error: String? = null
)

data class SelectedPetUiState(
    val pet: PetEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AppointmentsUiState(
    val appointments: List<AppointmentEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

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

    // --- ESTADOS EXISTENTES ---
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(ClientUiState())
    val currentUser: StateFlow<ClientUiState> = _currentUser.asStateFlow()

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register.asStateFlow()

    private val _pets = MutableStateFlow(PetsUiState())
    val pets: StateFlow<PetsUiState> = _pets.asStateFlow()

    private val _selectedPet = MutableStateFlow(SelectedPetUiState())
    val selectedPet: StateFlow<SelectedPetUiState> = _selectedPet.asStateFlow()

    private val _appointments = MutableStateFlow(AppointmentsUiState())
    val appointments: StateFlow<AppointmentsUiState> = _appointments.asStateFlow()

    // --- ESTADO DE PERFIL (NUEVO) ---
    private val _profile = MutableStateFlow(ProfileUiState())
    val profile: StateFlow<ProfileUiState> = _profile.asStateFlow()


    init {
        // ✅ MEJORA COMBINADA: Un único launch para las subscripciones
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user.clientId != 0L) {
                    // Subscripción reactiva al Flow de mascotas
                    launch {
                        repository.getPetsByOwner(user.clientId).collect { petsList ->
                            _pets.update { it.copy(pets = petsList, isLoading = false) }
                            _currentUser.update { it.copy(petsCount = petsList.size) }
                        }
                    }
                    // Subscripción reactiva al Flow de citas
                    launch {
                        repository.getAppointmentsByOwner(user.clientId).collect { appointmentsList ->
                            _appointments.update { it.copy(appointments = appointmentsList, isLoading = false) }
                        }
                    }
                } else {
                    // Limpia las listas si el usuario cierra sesión
                    _pets.update { PetsUiState() }
                    _appointments.update { AppointmentsUiState() }
                    _profile.value = ProfileUiState() // Limpiar el estado del perfil
                }
            }
        }

        checkUserSession()
    }

    // --- LÓGICA DE SESIÓN (LOGIN/LOGOUT/REGISTER) ---
    private fun checkUserSession() {
        viewModelScope.launch {
            val loggedIn = userPreferences.isLoggedIn.first()
            if (loggedIn) {
                val email = userPreferences.userEmail.first()
                val name = userPreferences.userName.first()
                val id = userPreferences.userId.first()
                // Al actualizar currentUser, se activan los collects en el bloque init
                _currentUser.value = ClientUiState(
                    clientId = id.toLongOrNull() ?: 0L,
                    name = name,
                    email = email
                )
                loadProfile() // Cargar el perfil inmediatamente después de iniciar sesión
            }
            _isUserLoggedIn.value = loggedIn
            _sessionState.update { it.copy(isLoggedIn = loggedIn) }
        }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null) }
            val result = repository.login(s.email.trim(), s.pass)

            if (result.isSuccess) {
                val client = result.getOrNull()!!
                userPreferences.setUserInfo(client.email, client.name, client.id.toString())
                userPreferences.setLoggedIn(true)

                // La actualización de _currentUser activará los flows de mascotas/citas
                _currentUser.value = ClientUiState(
                    clientId = client.id,
                    name = client.name,
                    email = client.email
                )
                _isUserLoggedIn.value = true
                _login.update { it.copy(currentClient = _currentUser.value) }

                _sessionState.update {
                    it.copy(
                        isLoggedIn = true,
                        loginMessage = "¡Bienvenido ${client.name}!",
                        showMessage = true
                    )
                }
                _login.update { it.copy(isSubmitting = false, success = true) }
                loadProfile() // Cargar el perfil después del login exitoso
            } else {
                val error = result.exceptionOrNull()?.message ?: "Error de autenticación"
                _sessionState.update { it.copy(loginMessage = error, showMessage = true) }
                _login.update { it.copy(isSubmitting = false, success = false, errorMsg = error) }
            }
        }
    }

    // [ ... submitRegister y logout se mantienen iguales, excepto por la limpieza de _profile en logout ]
    fun submitRegister() {
        // ... (código submitRegister anterior)
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null) }
            val result = repository.register(
                name = s.name.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),
                address = s.address.ifBlank { null },
                emergencyContact = s.emergencyContact.ifBlank { null },
                password = s.pass
            )

            if (result.isSuccess) {
                _sessionState.update {
                    it.copy(loginMessage = "¡Cuenta creada! Ya puedes iniciar sesión.", showMessage = true)
                }
                _register.update { it.copy(isSubmitting = false, success = true) }
            } else {
                val error = result.exceptionOrNull()?.message ?: "No se pudo registrar"
                _register.update { it.copy(isSubmitting = false, success = false, errorMsg = error) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUserData()
            _isUserLoggedIn.value = false
            _currentUser.value = ClientUiState()
            _login.value = LoginUiState()
            _register.value = RegisterUiState()
            _selectedPet.value = SelectedPetUiState()
            _profile.value = ProfileUiState() // ✅ Limpiar estado de perfil
            _sessionState.update {
                it.copy(
                    isLoggedIn = false,
                    logoutMessage = "Sesión cerrada.",
                    showMessage = true
                )
            }
        }
    }

    // --- LÓGICA DE PERFIL (NUEVA) ---

    // ✅ Cargar información del perfil
    fun loadProfile() {
        viewModelScope.launch {
            val userId = _currentUser.value.clientId
            if (userId == 0L) return@launch

            _profile.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }

            val client = repository.getClientById(userId)
            client?.let {
                // Actualizar _profile con los datos del repositorio
                _profile.value = ProfileUiState(
                    name = it.name,
                    email = it.email,
                    phone = it.phone,
                    address = it.address ?: "",
                    emergencyContact = it.emergencyContact ?: "",
                    isLoading = false
                )
            } ?: _profile.update { it.copy(isLoading = false, errorMessage = "No se encontró el perfil.") }
        }
    }

    // ✅ Actualizar información personal
    fun updateProfile(name: String, phone: String, address: String, emergency: String) {
        viewModelScope.launch {
            val id = _currentUser.value.clientId
            _profile.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }

            val result = repository.updateClientInfo(id, name, phone, address, emergency)
            if (result.isSuccess) {
                // También actualizamos el currentUser de la sesión si el nombre cambió
                _currentUser.update { it.copy(name = name) }

                _profile.update {
                    it.copy(
                        isLoading = false,
                        name = name, // Actualizar estado local para reflejar el cambio
                        phone = phone,
                        address = address,
                        emergencyContact = emergency,
                        successMessage = "Información actualizada con éxito"
                    )
                }
            } else {
                _profile.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    // ✅ Cambiar contraseña
    fun changePassword(newPass: String, confirm: String) {
        if (newPass != confirm) {
            _profile.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }

        viewModelScope.launch {
            val id = _currentUser.value.clientId
            _profile.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }

            val result = repository.changePassword(id, newPass)
            if (result.isSuccess) {
                _profile.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Contraseña actualizada correctamente",
                        // Limpiar campos de contraseña después del éxito
                        newPassword = "",
                        confirmPassword = ""
                    )
                }
            } else {
                _profile.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    // --- OPERACIONES PARA MASCOTAS ---
    fun addPet(
        nombre: String,
        especie: String,
        raza: String,
        fechaNacimiento: String?,
        peso: Double?,
        color: String?,
        notasMedicas: String?
    ) {
        // ... (código addPet anterior)
        val clientId = _currentUser.value.clientId
        if (clientId == 0L) {
            _pets.update { it.copy(error = "Error: Usuario no identificado.") }
            return
        }

        viewModelScope.launch {
            _pets.update { it.copy(isLoading = true, error = null) }
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

            if (result.isFailure) {
                _pets.update {
                    it.copy(
                        error = result.exceptionOrNull()?.message ?: "Error al agregar mascota.",
                        isLoading = false
                    )
                }
            } else {
                _pets.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadPetById(petId: Long) {
        // ... (código loadPetById anterior)
        viewModelScope.launch {
            _selectedPet.update { it.copy(isLoading = true, error = null, pet = null) }
            try {
                val pet = repository.getPetById(petId)
                _selectedPet.update { it.copy(pet = pet, isLoading = false) }
            } catch (e: Exception) {
                _selectedPet.update { it.copy(error = "No se pudo cargar la mascota.", isLoading = false) }
            }
        }
    }

    fun deletePet(petId: Long) {
        // ... (código deletePet anterior)
        val clientId = _currentUser.value.clientId
        if (clientId == 0L) return

        viewModelScope.launch {
            try {
                repository.deletePet(petId)
            } catch (e: Exception) {
                _pets.update { it.copy(error = "Error al eliminar mascota.") }
            }
        }
    }

    // --- OPERACIONES PARA CITAS ---
    fun addAppointment(petId: Long, date: Date, reason: String) {
        // ... (código addAppointment anterior)
        val clientId = _currentUser.value.clientId
        if (clientId == 0L) {
            _appointments.update { it.copy(error = "Error: Usuario no identificado.") }
            return
        }
        if (date.before(Date())) {
            _appointments.update { it.copy(error = "No se pueden crear citas en fechas pasadas.") }
            return
        }

        viewModelScope.launch {
            val result = repository.addAppointment(
                ownerId = clientId,
                petId = petId,
                date = date,
                reason = reason
            )
            if (result.isFailure) {
                _appointments.update {
                    it.copy(
                        error = result.exceptionOrNull()?.message ?: "Error al agregar la cita."
                    )
                }
            }
        }
    }

    // --- LIMPIEZA Y HELPERS ---
    fun clearSessionMessage() = _sessionState.update { it.copy(showMessage = false, loginMessage = null, logoutMessage = null) }
    fun clearLoginResult() = _login.update { it.copy(success = false, errorMsg = null) }
    fun clearRegisterResult() = _register.update { it.copy(success = false, errorMsg = null) }

    // ✅ NUEVO HELPER: Limpiar mensajes de perfil
    fun clearProfileMessage() = _profile.update { it.copy(successMessage = null, errorMessage = null) }


    // --- HANDLERS DE FORMULARIOS ---
    fun onLoginEmailChange(value: String) { _login.update { it.copy(email = value, emailError = validateEmail(value)) }; recomputeLoginCanSubmit() }
    fun onLoginPassChange(value: String) { _login.update { it.copy(pass = value) }; recomputeLoginCanSubmit() }
    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        _login.update { it.copy(canSubmit = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()) }
    }

    fun onNameChange(value: String) { _register.update { it.copy(name = value, nameError = validateNameLettersOnly(value)) }; recomputeRegisterCanSubmit() }
    fun onRegisterEmailChange(value: String) { _register.update { it.copy(email = value, emailError = validateEmail(value)) }; recomputeRegisterCanSubmit() }
    fun onPhoneChange(value: String) { _register.update { it.copy(phone = value, phoneError = validatePhoneDigitsOnly(value)) }; recomputeRegisterCanSubmit() }
    fun onAddressChange(value: String) { _register.update { it.copy(address = value, addressError = validateAddress(value)) }; recomputeRegisterCanSubmit() }
    fun onEmergencyContactChange(value: String) { _register.update { it.copy(emergencyContact = value, emergencyContactError = validateEmergencyContact(value)) }; recomputeRegisterCanSubmit() }
    fun onRegisterPassChange(value: String) { _register.update { it.copy(pass = value, passError = validateStrongPassword(value), confirmError = validateConfirm(value, _register.value.confirm)) }; recomputeRegisterCanSubmit() }
    fun onConfirmChange(value: String) { _register.update { it.copy(confirm = value, confirmError = validateConfirm(_register.value.pass, value)) }; recomputeRegisterCanSubmit() }
    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.addressError, s.emergencyContactError, s.passError, s.confirmError).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    // ✅ NUEVOS HANDLERS: Para el formulario de edición de perfil
    fun onProfileNameChange(value: String) = _profile.update { it.copy(name = value, errorMessage = null) }
    fun onProfilePhoneChange(value: String) = _profile.update { it.copy(phone = value, errorMessage = null) }
    fun onProfileAddressChange(value: String) = _profile.update { it.copy(address = value, errorMessage = null) }
    fun onProfileEmergencyContactChange(value: String) = _profile.update { it.copy(emergencyContact = value, errorMessage = null) }
    fun onProfileNewPasswordChange(value: String) = _profile.update { it.copy(newPassword = value, errorMessage = null) }
    fun onProfileConfirmPasswordChange(value: String) = _profile.update { it.copy(confirmPassword = value, errorMessage = null) }
}