package com.example.myapplicationv.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.myapplicationv.data.local.storage.UserPreferences
import com.example.myapplicationv.data.repository.VetRepository
import com.example.myapplicationv.data.local.user.ClientEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val repository: VetRepository = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock de UserPreferences
        coEvery { userPreferences.isLoggedIn } returns flowOf(false)
        coEvery { userPreferences.userEmail } returns flowOf("")
        coEvery { userPreferences.userName } returns flowOf("")
        coEvery { userPreferences.userId } returns flowOf("")
        coEvery { userPreferences.userRole } returns flowOf("user")
        coEvery { userPreferences.setUserInfo(any(), any(), any(), any()) } returns Unit
        coEvery { userPreferences.setLoggedIn(any()) } returns Unit
        coEvery { userPreferences.clearUserData() } returns Unit

        viewModel = AuthViewModel(repository, userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with valid credentials updates state correctly`() = runTest {
        // Arrange
        val email = "test@test.cl"
        val password = "Pass123!"
        val clientEntity = ClientEntity(
            id = 1L,
            nombre = "Test User",
            correo = email,
            contrasena = password,
            rolNombre = "CLIENTE"
        )

        coEvery { repository.login(email, password) } returns Result.success(clientEntity)
        coEvery { repository.getPetsByOwner(1L) } returns flowOf(emptyList())
        coEvery { repository.getAppointmentsByOwner(1L) } returns flowOf(emptyList())

        // Act
        viewModel.onLoginEmailChange(email)
        viewModel.onLoginPassChange(password)
        viewModel.submitLogin()

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { repository.login(email, password) }
    }

    @Test
    fun `register with valid data calls repository`() = runTest {
        // Arrange
        val name = "Juan Pérez"
        val email = "juan@test.cl"
        val phone = "12345678"
        val password = "Pass123!"

        coEvery { repository.register(name, email, phone, null, null, password) } returns Result.success(1L)

        // Act
        viewModel.onNameChange(name)
        viewModel.onRegisterEmailChange(email)
        viewModel.onPhoneChange(phone)
        viewModel.onRegisterPassChange(password)
        viewModel.onConfirmChange(password)
        viewModel.submitRegister()

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { repository.register(name, email, phone, null, null, password) }
    }

    @Test
    fun `logout clears user data`() = runTest {
        // Arrange - Configurar un usuario logueado primero
        coEvery { userPreferences.isLoggedIn } returns flowOf(true)
        coEvery { userPreferences.userEmail } returns flowOf("test@test.cl")
        coEvery { userPreferences.userName } returns flowOf("Test User")
        coEvery { userPreferences.userId } returns flowOf("1")
        coEvery { userPreferences.userRole } returns flowOf("CLIENTE")

        // Act
        viewModel.logout()

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { userPreferences.clearUserData() }
    }
}