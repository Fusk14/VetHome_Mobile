package com.example.myapplicationv.data.remote


import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
// Línea 2: import de nuestro DTO
import com.example.myapplicationv.data.remote.dto.MascotaDto

// interfaz con endpoints del servicio de mascotas
interface MascotaApi {
    
    //  GET /api/mascotas -> devuelve lista de MascotaDto
    @GET("api/mascotas")
    suspend fun getMascotas(): List<MascotaDto>
    
    // Endpoint para obtener una mascota por ID: GET /api/mascotas/{id}
    @GET("api/mascotas/{id}")
    suspend fun getMascotaById(@Path("id") id: Long): MascotaDto
    
    // Endpoint para crear una nueva mascota: POST /api/mascotas
    @POST("api/mascotas")
    suspend fun createMascota(@Body mascota: MascotaDto): MascotaDto
    
    // Endpoint para eliminar una mascota: DELETE /api/mascotas/{id}
    @DELETE("api/mascotas/{id}")
    suspend fun deleteMascota(@Path("id") id: Long)
}







