package com.example.myapplicationv.data.remote


import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
// Línea 2: import de nuestro DTO
import com.example.myapplicationv.data.remote.dto.ResenaDto

//interfaz con endpoints del servicio de reseñas
interface ResenaApi {
    
    // Línea 6: GET /api/resenas -> devuelve lista de ResenaDto
    @GET("api/resenas")
    suspend fun getResenas(): List<ResenaDto>
    
    // Endpoint para crear una nueva reseña: POST /api/resenas
    @POST("api/resenas")
    suspend fun createResena(@Body resena: ResenaDto): ResenaDto
}










