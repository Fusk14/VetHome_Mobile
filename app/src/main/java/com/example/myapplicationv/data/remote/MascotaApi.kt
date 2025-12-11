package com.example.myapplicationv.data.remote


import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
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
    // Retorna 201 Created según la documentación actualizada
    @POST("api/mascotas")
    suspend fun createMascota(@Body mascota: MascotaDto): retrofit2.Response<MascotaDto>
    
    // Endpoint para eliminar una mascota: DELETE /api/mascotas/{id}
    @DELETE("api/mascotas/{id}")
    suspend fun deleteMascota(@Path("id") id: Long)
    
    // Endpoint para subir foto de una mascota: POST /api/mascotas/{id}/foto
    @Multipart
    @POST("api/mascotas/{id}/foto")
    suspend fun uploadPetPhoto(
        @Path("id") id: Long,
        @Part foto: MultipartBody.Part
    ): retrofit2.Response<okhttp3.ResponseBody>
    
    // Endpoint para obtener foto de una mascota: GET /api/mascotas/{id}/foto
    @GET("api/mascotas/{id}/foto")
    suspend fun getPetPhoto(@Path("id") id: Long): retrofit2.Response<okhttp3.ResponseBody>
}









