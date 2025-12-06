package com.example.myapplicationv.domain.error

import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.io.IOException
import java.net.ConnectException
import java.util.concurrent.TimeoutException

/**
 * Utilidad para convertir errores técnicos en mensajes entendibles para usuarios
 */
object ErrorMessages {

    /**
     * Convierte una excepción en un mensaje de error amigable para el usuario
     */
    fun getFriendlyMessage(exception: Throwable?): String {
        if (exception == null) return "Ha ocurrido un error desconocido"

        return when (exception) {
            is UnknownHostException -> 
                "No se pudo conectar al servidor. Verifica tu conexión a internet."
            
            is SocketTimeoutException -> 
                "La conexión tardó demasiado. Por favor, intenta nuevamente."
            
            is TimeoutException -> 
                "La operación tardó demasiado. Por favor, intenta nuevamente."
            
            is ConnectException -> 
                "No se pudo conectar al servidor. Verifica tu conexión a internet."
            
            is IOException -> 
                "Error de conexión. Verifica tu conexión a internet e intenta nuevamente."
            
            is retrofit2.HttpException -> {
                when (exception.code()) {
                    400 -> "Los datos ingresados no son válidos. Por favor, revisa la información."
                    401 -> "Credenciales incorrectas. Verifica tu correo y contraseña."
                    403 -> "No tienes permisos para realizar esta acción."
                    404 -> "No se encontró la información solicitada."
                    409 -> "Ya existe un registro con estos datos."
                    500 -> "Error en el servidor. Por favor, intenta más tarde."
                    503 -> "El servicio no está disponible temporalmente. Intenta más tarde."
                    else -> "Error del servidor (${exception.code()}). Por favor, intenta más tarde."
                }
            }
            
            is IllegalArgumentException -> 
                exception.message ?: "Los datos ingresados no son válidos."
            
            is IllegalStateException -> {
                val message = exception.message ?: ""
                when {
                    message.contains("timeout", ignoreCase = true) -> 
                        "La conexión tardó demasiado. Por favor, intenta nuevamente."
                    message.contains("connection", ignoreCase = true) || 
                    message.contains("conexión", ignoreCase = true) -> 
                        "Error de conexión. Verifica tu conexión a internet e intenta nuevamente."
                    message.contains("network", ignoreCase = true) -> 
                        "Error de conexión. Verifica tu conexión a internet."
                    else -> message.ifBlank { "No se pudo completar la operación. Intenta nuevamente." }
                }
            }
            
            else -> {
                val message = exception.message ?: ""
                when {
                    message.contains("network", ignoreCase = true) -> 
                        "Error de conexión. Verifica tu conexión a internet."
                    message.contains("timeout", ignoreCase = true) -> 
                        "La operación tardó demasiado. Intenta nuevamente."
                    message.contains("unauthorized", ignoreCase = true) -> 
                        "No estás autorizado para realizar esta acción."
                    message.contains("not found", ignoreCase = true) -> 
                        "No se encontró la información solicitada."
                    message.contains("already exists", ignoreCase = true) -> 
                        "Ya existe un registro con estos datos."
                    else -> "Ha ocurrido un error: ${exception.message ?: "Error desconocido"}"
                }
            }
        }
    }

    /**
     * Convierte un mensaje de error genérico en uno más amigable
     */
    fun getFriendlyMessage(errorMessage: String?): String {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "Ha ocurrido un error desconocido"
        }

        return when {
            errorMessage.contains("network", ignoreCase = true) || 
            errorMessage.contains("connection", ignoreCase = true) -> 
                "Error de conexión. Verifica tu conexión a internet."
            
            errorMessage.contains("timeout", ignoreCase = true) -> 
                "La operación tardó demasiado. Intenta nuevamente."
            
            errorMessage.contains("unauthorized", ignoreCase = true) || 
            errorMessage.contains("invalid credentials", ignoreCase = true) -> 
                "Credenciales incorrectas. Verifica tu correo y contraseña."
            
            errorMessage.contains("not found", ignoreCase = true) -> 
                "No se encontró la información solicitada."
            
            errorMessage.contains("already exists", ignoreCase = true) || 
            errorMessage.contains("duplicate", ignoreCase = true) -> 
                "Ya existe un registro con estos datos."
            
            errorMessage.contains("validation", ignoreCase = true) || 
            errorMessage.contains("invalid", ignoreCase = true) -> 
                "Los datos ingresados no son válidos. Por favor, revisa la información."
            
            else -> errorMessage
        }
    }

    /**
     * Mensajes de éxito predefinidos
     */
    object Success {
        const val PROFILE_UPDATED = "Perfil actualizado correctamente"
        const val PASSWORD_CHANGED = "Contraseña actualizada correctamente"
        const val PET_ADDED = "Mascota agregada correctamente"
        const val PET_DELETED = "Mascota eliminada correctamente"
        const val APPOINTMENT_ADDED = "Cita agendada correctamente"
        const val APPOINTMENT_DELETED = "Cita cancelada correctamente"
        const val RESENA_CREATED = "Reseña creada correctamente"
        const val RESENA_DELETED = "Reseña eliminada correctamente"
        const val LOGIN_SUCCESS = "Sesión iniciada correctamente"
        const val REGISTER_SUCCESS = "Cuenta creada correctamente"
        const val PASSWORD_RECOVERY_SENT = "Se ha enviado un correo con instrucciones para recuperar tu contraseña"
    }

    /**
     * Mensajes de validación predefinidos (ya están en Validators.kt, pero aquí para referencia)
     */
    object Validation {
        const val EMAIL_REQUIRED = "El email es obligatorio"
        const val EMAIL_INVALID = "Formato de email inválido"
        const val PASSWORD_REQUIRED = "La contraseña es obligatoria"
        const val PASSWORD_WEAK = "La contraseña debe tener al menos 8 caracteres, incluyendo mayúsculas, minúsculas, números y símbolos"
        const val PASSWORDS_DONT_MATCH = "Las contraseñas no coinciden"
        const val CURRENT_PASSWORD_REQUIRED = "Debes ingresar tu contraseña actual"
        const val CURRENT_PASSWORD_INCORRECT = "La contraseña actual es incorrecta"
    }
}

