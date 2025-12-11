package com.example.myapplicationv.data.remote


import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
// Línea 2: import de nuestro DTO
import com.example.myapplicationv.data.remote.dto.UsuarioDto
import com.example.myapplicationv.data.remote.dto.LoginRequestDto
import com.example.myapplicationv.data.remote.dto.RegisterRequestDto
import com.example.myapplicationv.data.remote.dto.ForgotPasswordRequestDto

// interfaz con endpoints del servicio de usuarios
interface UsuarioApi {
    
    // Línea 6: POST /api/auth/login -> login de usuario
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): String
    
    // Endpoint para registrar usuario: POST /api/auth/register
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): retrofit2.Response<UsuarioDto>
    
    // Endpoint para obtener todos los usuarios: GET /api/usuarios
    @GET("api/usuarios")
    suspend fun getAllUsuarios(): List<UsuarioDto>
    
    // Endpoint para obtener usuario por ID: GET /api/usuarios/{id}
    @GET("api/usuarios/{id}")
    suspend fun getUsuarioById(@Path("id") id: Long): UsuarioDto
    
    // Endpoint para obtener usuario por correo: GET /api/usuarios/correo/{correo}
    @GET("api/usuarios/correo/{correo}")
    suspend fun getUsuarioByCorreo(@Path("correo") correo: String): UsuarioDto
    
    // Endpoint para recuperar contraseña: POST /api/auth/forgot-password
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): retrofit2.Response<okhttp3.ResponseBody>
    
    // Endpoint para actualizar información del usuario: PUT /api/usuarios/{id}
    @PUT("api/usuarios/{id}")
    suspend fun actualizarInformacion(
        @Path("id") id: Long,
        @Body datos: Map<String, String>
    ): retrofit2.Response<UsuarioDto>
    
    // Endpoint para cambiar contraseña: PUT /api/usuarios/{id}/contrasena
    @PUT("api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(
        @Path("id") id: Long,
        @Body datos: Map<String, String>
    ): retrofit2.Response<okhttp3.ResponseBody>
}









