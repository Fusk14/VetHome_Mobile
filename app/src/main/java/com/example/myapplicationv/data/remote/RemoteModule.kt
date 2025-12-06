package com.example.myapplicationv.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RemoteModule {

    private const val DEV_TUNNEL_ID = "rvhcfwb0"
    private const val DEV_TUNNEL_DOMAIN = "brs.devtunnels.ms"

    private enum class Microservice(val port: Int) {
        USUARIOS(8081),
        MASCOTAS(8090),
        CONSULTAS(8091),
        RESENAS(8086)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Tiempo para establecer conexión
            .readTimeout(30, TimeUnit.SECONDS) // Tiempo para leer respuesta
            .writeTimeout(30, TimeUnit.SECONDS) // Tiempo para escribir request
            .build()
    }

    private val retrofitCache = mutableMapOf<Microservice, Retrofit>()

    private fun baseUrlFor(service: Microservice) =
        "https://$DEV_TUNNEL_ID-${service.port}.$DEV_TUNNEL_DOMAIN/"

    private fun retrofitFor(service: Microservice): Retrofit =
        retrofitCache.getOrPut(service) {
            Retrofit.Builder()
                .baseUrl(baseUrlFor(service))
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

    val usuarioApi: UsuarioApi by lazy {
        retrofitFor(Microservice.USUARIOS).create(UsuarioApi::class.java)
    }

    val mascotaApi: MascotaApi by lazy {
        retrofitFor(Microservice.MASCOTAS).create(MascotaApi::class.java)
    }

    val consultaApi: ConsultaApi by lazy {
        retrofitFor(Microservice.CONSULTAS).create(ConsultaApi::class.java)
    }

    val resenaApi: ResenaApi by lazy {
        retrofitFor(Microservice.RESENAS).create(ResenaApi::class.java)
    }
}
