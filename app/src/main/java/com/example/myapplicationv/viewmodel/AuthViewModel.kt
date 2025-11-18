package com.example.myapplicationv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationv.data.local.appointment.AppointmentEntity
import com.example.myapplicationv.data.local.pet.PetEntity
import com.example.myapplicationv.data.local.storage.UserPreferences
import com.example.myapplicationv.data.local.user.ClientEntity
import com.example.myapplicationv.data.local.resena.ResenaEntity
import com.example.myapplicationv.data.repository.VetRepository
import com.example.myapplicationv.domain.validation.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

// --------------------------------------------------------------------------------------
// --- DATA CLASSES DE ESTADO (NUEVAS: RESEÑAS) ---
// --------------------------------------------------------------------------------------

data class ResenasUiState(
    val resenas: List<ResenaEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ResenaDetailUiState(
    val resena: ResenaEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// --- ESTADOS DE PERFIL (EXISTENTES) ---
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
    val petsCount: Int = 0,
    val role: String = "user"
)

data class EditUserUiState(
    val user: ClientEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUserUpdated: Boolean = false
)

data class AllClientsUiState(
    val clients: List<ClientEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AllPetsUiState(
    val pets: List<PetEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AllAppointmentsUiState(
    val appointments: List<AppointmentEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
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

    private val _userRole = MutableStateFlow("user")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _editUser = MutableStateFlow(EditUserUiState())
    val editUser: StateFlow<EditUserUiState> = _editUser.asStateFlow()

    private val _allClients = MutableStateFlow(AllClientsUiState())
    val allClients: StateFlow<AllClientsUiState> = _allClients.asStateFlow()

    private val _allPets = MutableStateFlow(AllPetsUiState())
    val allPets: StateFlow<AllPetsUiState> = _allPets.asStateFlow()

    private val _allAppointments = MutableStateFlow(AllAppointmentsUiState())
    val allAppointments: StateFlow<AllAppointmentsUiState> = _allAppointments.asStateFlow()

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register.asStateFlow()

    // ✅ NUEVO ESTADO: Éxito en el registro
    private val _showRegistrationSuccess = MutableStateFlow(false)
    val showRegistrationSuccess: StateFlow<Boolean> = _showRegistrationSuccess.asStateFlow()

    private val _pets = MutableStateFlow(PetsUiState())
    val pets: StateFlow<PetsUiState> = _pets.asStateFlow()

    private val _selectedPet = MutableStateFlow(SelectedPetUiState())
    val selectedPet: StateFlow<SelectedPetUiState> = _selectedPet.asStateFlow()

    private val _appointments = MutableStateFlow(AppointmentsUiState())
    val appointments: StateFlow<AppointmentsUiState> = _appointments.asStateFlow()

    private val _profile = MutableStateFlow(ProfileUiState())
    val profile: StateFlow<ProfileUiState> = _profile.asStateFlow()

    // 🆕 ESTADOS DE RESEÑAS
    private val _resenas = MutableStateFlow(ResenasUiState())
    val resenas: StateFlow<ResenasUiState> = _resenas.asStateFlow()

    private val _resenaDetail = MutableStateFlow(ResenaDetailUiState())
    val resenaDetail: StateFlow<ResenaDetailUiState> = _resenaDetail.asStateFlow()


    init {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user.clientId != 0L) {
                    launch {
                        repository.getPetsByOwner(user.clientId).collect { petsList ->
                            _pets.update { it.copy(pets = petsList, isLoading = false) }
                            _currentUser.update { it.copy(petsCount = petsList.size) }
                        }
                    }
                    launch {
                        repository.getAppointmentsByOwner(user.clientId).collect { appointmentsList ->
                            _appointments.update { it.copy(appointments = appointmentsList, isLoading = false) }
                        }
                    }
                    // 🆕 Cargar reseñas al iniciar sesión
                    launch {
                        loadResenas()
                    }
                } else {
                    _pets.update { PetsUiState() }
                    _appointments.update { AppointmentsUiState() }
                    _profile.value = ProfileUiState()
                    _resenas.value = ResenasUiState() // 🆕 Limpiar reseñas al cerrar sesión
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
                val role = userPreferences.userRole.first()

                _currentUser.value = ClientUiState(
                    clientId = id.toLongOrNull() ?: 0L,
                    name = name,
                    email = email,
                    role = role
                )
                _userRole.value = role
                loadProfile()
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

                userPreferences.setUserInfo(
                    client.email,
                    client.name,
                    client.id.toString(),
                    client.role
                )
                userPreferences.setLoggedIn(true)

                val clientState = ClientUiState(
                    clientId = client.id,
                    name = client.name,
                    email = client.email,
                    role = client.role
                )

                _currentUser.value = clientState
                _userRole.value = client.role

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
                loadProfile()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Error de autenticación"
                _sessionState.update { it.copy(loginMessage = error, showMessage = true) }
                _login.update { it.copy(isSubmitting = false, success = false, errorMsg = error) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUserData()
            _isUserLoggedIn.value = false
            _currentUser.value = ClientUiState()
            _userRole.value = "user"
            _login.value = LoginUiState()
            _register.value = RegisterUiState()
            _selectedPet.value = SelectedPetUiState()
            _profile.value = ProfileUiState()
            _resenas.value = ResenasUiState() // 🆕 Limpiar reseñas
            _resenaDetail.value = ResenaDetailUiState() // 🆕 Limpiar detalle de reseña
            _sessionState.update {
                it.copy(
                    isLoggedIn = false,
                    logoutMessage = "Sesión cerrada.",
                    showMessage = true
                )
            }
        }
    }

    fun isCurrentUserAdmin(): Boolean {
        return _userRole.value == "admin"
    }

    // --------------------------------------------------------------------------------------
    // --- LÓGICA DE PERFIL ---
    // --------------------------------------------------------------------------------------

    fun loadProfile() {
        viewModelScope.launch {
            val userId = _currentUser.value.clientId
            if (userId == 0L) return@launch

            _profile.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }

            val client = repository.getClientById(userId)
            client?.let {
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

    fun updateProfile(name: String, phone: String, address: String, emergency: String) {
        viewModelScope.launch {
            val id = _currentUser.value.clientId
            _profile.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }

            val result = repository.updateClientInfo(id, name, phone, address, emergency)
            if (result.isSuccess) {
                if (_currentUser.value.name != name) {
                    _currentUser.update { it.copy(name = name) }
                }

                _profile.update {
                    it.copy(
                        isLoading = false,
                        name = name,
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

    // --------------------------------------------------------------------------------------
    // --- FUNCIONES DE ADMINISTRACIÓN Y DATOS GLOBALES ---
    // --------------------------------------------------------------------------------------

    fun loadUserById(userId: Long) {
        viewModelScope.launch {
            _editUser.update { it.copy(isLoading = true, error = null, isUserUpdated = false) }
            val user = repository.getClientById(userId)
            _editUser.update { it.copy(user = user, isLoading = false) }
        }
    }

    fun updateUser(userId: Long, name: String, phone: String, address: String, emergencyContact: String) {
        viewModelScope.launch {
            repository.updateClientInfo(userId, name, phone, address, emergencyContact)
            _editUser.update { it.copy(isUserUpdated = true) }
        }
    }

    fun loadAllClients() {
        viewModelScope.launch {
            _allClients.update { it.copy(isLoading = true, error = null) }
            repository.getAllClients().collect { clientsList ->
                _allClients.update {
                    it.copy(
                        clients = clientsList,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadAllPets() {
        viewModelScope.launch {
            _allPets.update { it.copy(isLoading = true, error = null) }
            repository.getAllPets().collect { petsList ->
                _allPets.update {
                    it.copy(
                        pets = petsList,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadAllAppointments() {
        viewModelScope.launch {
            _allAppointments.update { it.copy(isLoading = true, error = null) }
            repository.getAllAppointments().collect { appointmentsList ->
                _allAppointments.update {
                    it.copy(
                        appointments = appointmentsList,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteUserAndData(clientId: Long) {
        viewModelScope.launch {
            repository.deleteUserAndData(clientId)
        }
    }

    fun deletePetById(petId: Long) {
        viewModelScope.launch {
            repository.deletePet(petId)
        }
    }

    fun deleteAppointmentById(appointmentId: Long) {
        viewModelScope.launch {
            repository.deleteAppointmentById(appointmentId)
        }
    }

    // ✅ MODIFICACIÓN: Función submitRegister
    fun submitRegister() {
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
                _register.update { it.copy(isSubmitting = false, success = true) }
                _showRegistrationSuccess.value = true // ✅ Mostrar alerta de éxito
                clearRegistrationForm() // ✅ Limpiar formulario
            } else {
                val error = result.exceptionOrNull()?.message ?: "No se pudo registrar"
                _register.update { it.copy(isSubmitting = false, success = false, errorMsg = error) }
            }
        }
    }

    // ✅ NUEVA FUNCIÓN: Limpiar el formulario de registro
    fun clearRegistrationForm() {
        _register.update {
            RegisterUiState() // Esto reinicia todo a valores por defecto
        }
    }

    // ✅ NUEVA FUNCIÓN: Ocultar la alerta de éxito
    fun hideRegistrationSuccess() {
        _showRegistrationSuccess.value = false
    }


    fun addPet(
        nombre: String,
        especie: String,
        raza: String,
        fechaNacimiento: String?,
        peso: Double?,
        color: String?,
        notasMedicas: String?
    ) {
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

    fun addAppointment(petId: Long, date: Date, reason: String) {
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

    // --------------------------------------------------------------------------------------
    // ⭐ FUNCIONES DE RESEÑAS
    // --------------------------------------------------------------------------------------

    fun loadResenas() {
        val userId = _currentUser.value.clientId
        if (userId == 0L) return

        viewModelScope.launch {
            _resenas.update { it.copy(isLoading = true, error = null) }
            // Recolectar Flow del repositorio
            repository.obtenerResenasPorUsuario(userId).collect { resenasList ->
                _resenas.update {
                    it.copy(
                        resenas = resenasList,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun crearResena(
        mascotaId: Long,
        mascotaNombre: String,
        calificacion: Int,
        comentario: String,
        fecha: String
    ) {
        val userId = _currentUser.value.clientId
        if (userId == 0L) {
            _resenas.update { it.copy(error = "Error: Usuario no identificado.") }
            return
        }

        viewModelScope.launch {
            _resenas.update { it.copy(isLoading = true, error = null) }
            val result = repository.crearResena(
                usuarioId = userId,
                mascotaId = mascotaId,
                mascotaNombre = mascotaNombre,
                calificacion = calificacion,
                comentario = comentario,
                fecha = fecha
            )

            if (result.isFailure) {
                _resenas.update {
                    it.copy(
                        error = result.exceptionOrNull()?.message ?: "Error al crear reseña.",
                        isLoading = false
                    )
                }
            } else {
                // Éxito: el Flow de loadResenas se encargará de actualizar la lista automáticamente.
                _resenas.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadResenaById(resenaId: Long) {
        viewModelScope.launch {
            _resenaDetail.update { it.copy(isLoading = true, error = null, resena = null) }
            try {
                val resena = repository.obtenerResenaPorId(resenaId)
                _resenaDetail.update { it.copy(resena = resena, isLoading = false) }
            } catch (e: Exception) {
                _resenaDetail.update { it.copy(error = "No se pudo cargar la reseña.", isLoading = false) }
            }
        }
    }

    fun deleteResena(resenaId: Long) {
        viewModelScope.launch {
            try {
                repository.eliminarResena(resenaId)
                // Recargar la lista después de eliminar (aunque el Flow debería hacerlo)
                loadResenas()
            } catch (e: Exception) {
                _resenas.update { it.copy(error = "Error al eliminar reseña.") }
            }
        }
    }

    // --------------------------------------------------------------------------------------
    // --- CLEAR HANDLERS ---
    // --------------------------------------------------------------------------------------

    fun clearSessionMessage() = _sessionState.update { it.copy(showMessage = false, loginMessage = null, logoutMessage = null) }
    fun clearLoginResult() = _login.update { it.copy(success = false, errorMsg = null) }
    fun clearRegisterResult() = _register.update { it.copy(success = false, errorMsg = null) }
    fun clearProfileMessage() = _profile.update { it.copy(successMessage = null, errorMessage = null) }

    // --------------------------------------------------------------------------------------
    // --- HANDLERS DE FORMULARIOS ---
    // --------------------------------------------------------------------------------------

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

    fun onProfileNameChange(value: String) = _profile.update { it.copy(name = value, errorMessage = null) }
    fun onProfilePhoneChange(value: String) = _profile.update { it.copy(phone = value, errorMessage = null) }
    fun onProfileAddressChange(value: String) = _profile.update { it.copy(address = value, errorMessage = null) }
    fun onProfileEmergencyContactChange(value: String) = _profile.update { it.copy(emergencyContact = value, errorMessage = null) }
    fun onProfileNewPasswordChange(value: String) = _profile.update { it.copy(newPassword = value, errorMessage = null) }
    fun onProfileConfirmPasswordChange(value: String) = _profile.update { it.copy(confirmPassword = value, errorMessage = null) }
}