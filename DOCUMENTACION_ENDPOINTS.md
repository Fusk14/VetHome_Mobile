# 📚 Documentación de Endpoints - Microservicios VetHome

Esta documentación describe todos los endpoints disponibles en los microservicios de VetHome y cómo se utilizan desde la aplicación móvil.

---

## 🔐 Microservicio de Usuarios (Puerto 8081)

### Base URL
```
https://rvhcfwb0-8081.brs.devtunnels.ms/
```

### Endpoints

#### 1. Login de Usuario
- **Método:** `POST`
- **Ruta:** `/api/auth/login`
- **Descripción:** Autentica un usuario con correo y contraseña
- **Request Body:**
  ```json
  {
    "correo": "usuario@ejemplo.com",
    "contrasena": "password123"
  }
  ```
- **Response:** `String` (token o mensaje de éxito)
- **Códigos de Respuesta:**
  - `200 OK`: Login exitoso
  - `401 Unauthorized`: Credenciales inválidas
  - `500 Internal Server Error`: Error del servidor
- **Uso en la App:**
  ```kotlin
  val loginRequest = LoginRequestDto(
      correo = email,
      contrasena = password
  )
  usuarioApi.login(loginRequest)
  ```

#### 2. Registro de Usuario
- **Método:** `POST`
- **Ruta:** `/api/auth/register`
- **Descripción:** Registra un nuevo usuario en el sistema
- **Request Body:**
  ```json
  {
    "rut": "12345678-9",
    "nombre": "Juan",
    "apellido": "Pérez",
    "correo": "juan@ejemplo.com",
    "telefono": "+56912345678",
    "contrasena": "password123",
    "rolNombre": "CLIENTE"
  }
  ```
- **Response:** `UsuarioDto`
  ```json
  {
    "id": 1,
    "rut": "12345678-9",
    "nombre": "Juan",
    "apellido": "Pérez",
    "correo": "juan@ejemplo.com",
    "telefono": "+56912345678",
    "rol": {
      "id": 1,
      "nombre": "CLIENTE"
    }
  }
  ```
- **Códigos de Respuesta:**
  - `200 OK`: Usuario registrado exitosamente
  - `400 Bad Request`: Datos inválidos
  - `409 Conflict`: Usuario ya existe
- **Uso en la App:**
  ```kotlin
  val registerRequest = RegisterRequestDto(
      rut = rut,
      nombre = nombre,
      apellido = apellido,
      correo = email,
      telefono = phone,
      contrasena = password,
      rolNombre = "CLIENTE"
  )
  val usuarioDto = usuarioApi.register(registerRequest)
  ```

#### 3. Obtener Todos los Usuarios
- **Método:** `GET`
- **Ruta:** `/api/usuarios`
- **Descripción:** Obtiene la lista de todos los usuarios (requiere permisos de administrador)
- **Response:** `List<UsuarioDto>`
- **Códigos de Respuesta:**
  - `200 OK`: Lista de usuarios
  - `403 Forbidden`: Sin permisos
- **Uso en la App:**
  ```kotlin
  val usuarios = usuarioApi.getAllUsuarios()
  ```

#### 4. Obtener Usuario por ID
- **Método:** `GET`
- **Ruta:** `/api/usuarios/{id}`
- **Descripción:** Obtiene los datos de un usuario específico por su ID
- **Parámetros:**
  - `id` (Path): ID del usuario (Long)
- **Response:** `UsuarioDto`
- **Códigos de Respuesta:**
  - `200 OK`: Usuario encontrado
  - `404 Not Found`: Usuario no encontrado
- **Uso en la App:**
  ```kotlin
  val usuario = usuarioApi.getUsuarioById(userId)
  ```

#### 5. Obtener Usuario por Correo
- **Método:** `GET`
- **Ruta:** `/api/usuarios/correo/{correo}`
- **Descripción:** Obtiene los datos de un usuario por su correo electrónico
- **Parámetros:**
  - `correo` (Path): Correo electrónico del usuario (String)
- **Response:** `UsuarioDto`
- **Códigos de Respuesta:**
  - `200 OK`: Usuario encontrado
  - `404 Not Found`: Usuario no encontrado
- **Uso en la App:**
  ```kotlin
  val usuario = usuarioApi.getUsuarioByCorreo(email)
  ```

---

## 🐾 Microservicio de Mascotas (Puerto 8090)

### Base URL
```
https://rvhcfwb0-8090.brs.devtunnels.ms/
```

### Endpoints

#### 1. Obtener Todas las Mascotas
- **Método:** `GET`
- **Ruta:** `/api/mascotas`
- **Descripción:** Obtiene la lista de todas las mascotas registradas
- **Response:** `List<MascotaDto>`
  ```json
  [
    {
      "id": 1,
      "idCliente": 1,
      "nombre": "Max",
      "especie": "Perro",
      "raza": "Labrador",
      "edad": 3
    }
  ]
  ```
- **Códigos de Respuesta:**
  - `200 OK`: Lista de mascotas
- **Uso en la App:**
  ```kotlin
  val mascotas = mascotaApi.getMascotas()
  ```

#### 2. Obtener Mascota por ID
- **Método:** `GET`
- **Ruta:** `/api/mascotas/{id}`
- **Descripción:** Obtiene los datos de una mascota específica por su ID
- **Parámetros:**
  - `id` (Path): ID de la mascota (Long)
- **Response:** `MascotaDto`
- **Códigos de Respuesta:**
  - `200 OK`: Mascota encontrada
  - `404 Not Found`: Mascota no encontrada
- **Uso en la App:**
  ```kotlin
  val mascota = mascotaApi.getMascotaById(petId)
  ```

#### 3. Crear Mascota
- **Método:** `POST`
- **Ruta:** `/api/mascotas`
- **Descripción:** Registra una nueva mascota en el sistema
- **Request Body:**
  ```json
  {
    "idCliente": 1,
    "nombre": "Max",
    "especie": "Perro",
    "raza": "Labrador",
    "edad": 3
  }
  ```
- **Response:** `MascotaDto` (con ID generado)
- **Códigos de Respuesta:**
  - `200 OK`: Mascota creada exitosamente
  - `400 Bad Request`: Datos inválidos
  - `404 Not Found`: Cliente no encontrado
- **Uso en la App:**
  ```kotlin
  val mascotaDto = MascotaDto(
      idCliente = ownerId,
      nombre = nombre,
      especie = especie,
      raza = raza,
      edad = edad
  )
  val creada = mascotaApi.createMascota(mascotaDto)
  ```

#### 4. Eliminar Mascota
- **Método:** `DELETE`
- **Ruta:** `/api/mascotas/{id}`
- **Descripción:** Elimina una mascota del sistema
- **Parámetros:**
  - `id` (Path): ID de la mascota (Long)
- **Response:** Sin contenido
- **Códigos de Respuesta:**
  - `200 OK`: Mascota eliminada exitosamente
  - `404 Not Found`: Mascota no encontrada
- **Uso en la App:**
  ```kotlin
  mascotaApi.deleteMascota(petId)
  ```

---

## 🏥 Microservicio de Consultas (Puerto 8091)

### Base URL
```
https://rvhcfwb0-8091.brs.devtunnels.ms/
```

### Endpoints

#### 1. Obtener Todas las Consultas
- **Método:** `GET`
- **Ruta:** `/api/consultas`
- **Descripción:** Obtiene la lista de todas las consultas/citas registradas
- **Response:** `List<ConsultaDto>`
  ```json
  [
    {
      "id": 1,
      "idMascota": 1,
      "idVeterinario": 2,
      "idCliente": 1,
      "fecha": "2024-01-15",
      "motivo": "Control anual",
      "diagnostico": null,
      "tratamiento": null
    }
  ]
  ```
- **Códigos de Respuesta:**
  - `200 OK`: Lista de consultas
- **Uso en la App:**
  ```kotlin
  val consultas = consultaApi.getConsultas()
  ```

#### 2. Obtener Consulta por ID
- **Método:** `GET`
- **Ruta:** `/api/consultas/{id}`
- **Descripción:** Obtiene los datos de una consulta específica por su ID
- **Parámetros:**
  - `id` (Path): ID de la consulta (Long)
- **Response:** `ConsultaDto`
- **Códigos de Respuesta:**
  - `200 OK`: Consulta encontrada
  - `404 Not Found`: Consulta no encontrada
- **Uso en la App:**
  ```kotlin
  val consulta = consultaApi.getConsultaById(consultaId)
  ```

#### 3. Crear Consulta
- **Método:** `POST`
- **Ruta:** `/api/consultas`
- **Descripción:** Crea una nueva consulta/cita para una mascota
- **Request Body:**
  ```json
  {
    "idMascota": 1,
    "idVeterinario": 2,
    "idCliente": 1,
    "fecha": "2024-01-15",
    "motivo": "Control anual"
  }
  ```
- **Response:** `ConsultaDto` (con ID generado)
- **Códigos de Respuesta:**
  - `200 OK`: Consulta creada exitosamente
  - `400 Bad Request`: Datos inválidos
  - `404 Not Found`: Mascota, veterinario o cliente no encontrado
- **Uso en la App:**
  ```kotlin
  val consultaDto = ConsultaDto(
      idMascota = petId,
      idVeterinario = ownerId,
      idCliente = ownerId,
      fecha = fechaStr, // Formato: "YYYY-MM-DD"
      motivo = reason
  )
  val creada = consultaApi.createConsulta(consultaDto)
  ```

#### 4. Eliminar Consulta
- **Método:** `DELETE`
- **Ruta:** `/api/consultas/{id}`
- **Descripción:** Elimina una consulta del sistema
- **Parámetros:**
  - `id` (Path): ID de la consulta (Long)
- **Response:** Sin contenido
- **Códigos de Respuesta:**
  - `200 OK`: Consulta eliminada exitosamente
  - `404 Not Found`: Consulta no encontrada
- **Uso en la App:**
  ```kotlin
  consultaApi.deleteConsulta(consultaId)
  ```

---

## ⭐ Microservicio de Reseñas (Puerto 8086)

### Base URL
```
https://rvhcfwb0-8086.brs.devtunnels.ms/
```

### Endpoints

#### 1. Obtener Todas las Reseñas
- **Método:** `GET`
- **Ruta:** `/api/resenas`
- **Descripción:** Obtiene la lista de todas las reseñas registradas
- **Response:** `List<ResenaDto>`
  ```json
  [
    {
      "id": 1,
      "idCliente": 1,
      "idVeterinario": 2,
      "calificacion": 5,
      "comentario": "Excelente atención"
    }
  ]
  ```
- **Códigos de Respuesta:**
  - `200 OK`: Lista de reseñas
- **Uso en la App:**
  ```kotlin
  val resenas = resenaApi.getResenas()
  ```

#### 2. Crear Reseña
- **Método:** `POST`
- **Ruta:** `/api/resenas`
- **Descripción:** Crea una nueva reseña para un veterinario
- **Request Body:**
  ```json
  {
    "idCliente": 1,
    "idVeterinario": 2,
    "calificacion": 5,
    "comentario": "Excelente atención y profesionalismo"
  }
  ```
- **Response:** `ResenaDto` (con ID generado)
- **Códigos de Respuesta:**
  - `200 OK`: Reseña creada exitosamente
  - `400 Bad Request`: Datos inválidos (calificación fuera de rango, comentario vacío, etc.)
  - `404 Not Found`: Cliente o veterinario no encontrado
- **Validaciones:**
  - `calificacion`: Debe estar entre 1 y 5
  - `comentario`: No puede estar vacío, máximo 500 caracteres
- **Uso en la App:**
  ```kotlin
  val dto = ResenaDto(
      idCliente = usuarioId,
      idVeterinario = veterinarioId,
      calificacion = calificacion, // 1-5
      comentario = comentario
  )
  val creada = resenaApi.createResena(dto)
  ```

---

## 🔧 Configuración de Retrofit

Los endpoints se configuran en `RemoteModule.kt` usando Retrofit:

```kotlin
object RemoteModule {
    private enum class Microservice(val port: Int) {
        USUARIOS(8081),
        MASCOTAS(8090),
        CONSULTAS(8091),
        RESENAS(8086)
    }
    
    val usuarioApi: UsuarioApi
    val mascotaApi: MascotaApi
    val consultaApi: ConsultaApi
    val resenaApi: ResenaApi
}
```

---

## 📝 Notas Importantes

1. **Formato de Fechas:** Las fechas se envían en formato `YYYY-MM-DD` (ISO 8601)
2. **Autenticación:** Actualmente el login devuelve un String (token o mensaje). Se recomienda implementar JWT en el futuro.
3. **Manejo de Errores:** Todos los endpoints pueden lanzar excepciones que deben ser manejadas con try-catch.
4. **Offline Support:** La app implementa un sistema de fallback que guarda datos localmente cuando falla la conexión.
5. **Validaciones:** La app valida los datos antes de enviarlos al servidor para mejorar la experiencia del usuario.

---

## 🚨 Códigos de Error Comunes

- **400 Bad Request:** Datos inválidos o faltantes en el request
- **401 Unauthorized:** Credenciales inválidas o token expirado
- **403 Forbidden:** Sin permisos para realizar la operación
- **404 Not Found:** Recurso no encontrado
- **500 Internal Server Error:** Error interno del servidor
- **503 Service Unavailable:** Servicio temporalmente no disponible

---

## 📞 Soporte

Para más información sobre los endpoints, consultar la documentación de cada microservicio o contactar al equipo de desarrollo.

