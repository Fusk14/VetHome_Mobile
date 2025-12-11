package com.example.myapplicationv.data.remote


import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
// Línea 2: import de nuestro DTO
import com.example.myapplicationv.data.remote.dto.ConsultaDto

// interfaz con endpoints del servicio de consultas
interface ConsultaApi {
    
    // Línea 6: GET /api/consultas -> devuelve lista de ConsultaDto
    @GET("api/consultas")
    suspend fun getConsultas(): List<ConsultaDto>
    
    // Endpoint para obtener una consulta por ID: GET /api/consultas/{id}
    @GET("api/consultas/{id}")
    suspend fun getConsultaById(@Path("id") id: Long): ConsultaDto
    
    // Endpoint para crear una nueva consulta: POST /api/consultas
    @POST("api/consultas")
    suspend fun createConsulta(@Body consulta: ConsultaDto): ConsultaDto
    
    // Endpoint para eliminar una consulta: DELETE /api/consultas/{id}
    @DELETE("api/consultas/{id}")
    suspend fun deleteConsulta(@Path("id") id: Long)
}









