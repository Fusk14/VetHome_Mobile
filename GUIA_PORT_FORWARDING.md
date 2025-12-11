# 🚀 Guía: Usar Port Forwarding de VSCode con tu App Móvil

## ✅ ¿Funcionará con la conexión actual?

**¡SÍ!** La conexión que configuramos te permite usar Port Forwarding de VSCode. Solo necesitas cambiar las URLs en `RemoteModule.kt`.

---

## 📋 Pasos para Configurar Port Forwarding

### 1. Activar Port Forwarding en VSCode

1. Abre VSCode
2. Ve a la pestaña **"Ports"** o **"Puertos"** en la parte inferior
3. Haz clic en **"Forward a Port"** o **"Reenviar un puerto"**
4. Ingresa los puertos de tus microservicios:
   - `8081` (Usuarios)
   - `8090` (Mascotas)
   - `8091` (Consultas)
   - `8087` (Reseñas)

### 2. Hacer los Puertos Públicos

1. Haz clic derecho en cada puerto
2. Selecciona **"Port Visibility"** → **"Public"**
3. Copia la **"Forwarded Address"** que te da VSCode

**Ejemplo de URLs que te dará VSCode:**
```
https://xxxx-xxxx-xxxx.vscode.dev:8081
https://xxxx-xxxx-xxxx.vscode.dev:8090
https://xxxx-xxxx-xxxx.vscode.dev:8091
https://xxxx-xxxx-xxxx.vscode.dev:8087
```

O podría ser:
```
https://xxxx-xxxx-xxxx.github.dev:8081
```

---

## 🔧 Configurar tu App Móvil

### Paso 1: Abrir `RemoteModule.kt`

Abre el archivo:
```
app/src/main/java/com/example/myapplicationv/data/remote/RemoteModule.kt
```

### Paso 2: Cambiar la URL Base

Busca esta sección (líneas 14-20):

```kotlin
// Opción 1: Para emulador Android (localhost)
// private const val BASE_HOST = "http://10.0.2.2"

// Opción 2: Para Port Forwarding de VSCode (URL pública)
private const val BASE_HOST = "http://10.0.2.2"  // ← Cambia esto
```

**Reemplaza con tu URL de VSCode:**

```kotlin
// Para Port Forwarding de VSCode
private const val BASE_HOST = "https://xxxx-xxxx-xxxx.vscode.dev"
```

**⚠️ IMPORTANTE:**
- Si tu URL de VSCode es `https://xxxx-xxxx-xxxx.vscode.dev:8081`, usa solo la parte base: `https://xxxx-xxxx-xxxx.vscode.dev`
- Los puertos se agregan automáticamente

### Paso 3: Verificar que los Puertos Coincidan

Asegúrate de que los puertos en `RemoteModule.kt` coincidan con tus microservicios:

```kotlin
private const val PORT_USUARIOS = 8081    // ← Debe coincidir con tu microservicio
private const val PORT_MASCOTAS = 8090    // ← Debe coincidir con tu microservicio
private const val PORT_CONSULTAS = 8091   // ← Debe coincidir con tu microservicio
private const val PORT_RESENAS = 8087     // ← Debe coincidir con tu microservicio
```

---

## 🧪 Probar la Conexión

### Opción 1: Probar desde POSTMAN

1. Abre POSTMAN
2. Haz una petición GET a: `https://xxxx-xxxx-xxxx.vscode.dev:8081/api/usuarios`
3. Si funciona, tu app móvil también funcionará

### Opción 2: Probar desde la App

1. Compila y ejecuta tu app en el emulador o dispositivo físico
2. Intenta hacer login o cargar datos
3. Revisa los logs en Logcat (deberías ver las peticiones HTTP)

---

## 📱 Configuración para Dispositivo Físico

Si quieres probar en un **dispositivo físico** (no emulador):

1. Asegúrate de que el Port Forwarding esté **Público**
2. Usa la misma URL de VSCode en `RemoteModule.kt`
3. El dispositivo debe tener conexión a internet

---

## 🔄 Cambiar entre Localhost y Port Forwarding

### Para usar Localhost (Emulador):
```kotlin
private const val BASE_HOST = "http://10.0.2.2"
```

### Para usar Port Forwarding (VSCode):
```kotlin
private const val BASE_HOST = "https://xxxx-xxxx-xxxx.vscode.dev"
```

---

## ⚠️ Notas Importantes

1. **HTTPS vs HTTP**: VSCode Port Forwarding usa HTTPS, por eso debes usar `https://` en la URL
2. **Certificados**: Ya configuramos `network_security_config.xml` para aceptar certificados de VSCode
3. **Puertos Públicos**: Asegúrate de que los puertos estén marcados como **"Public"** en VSCode
4. **Firewall**: Si tienes problemas, verifica que tu firewall no bloquee las conexiones

---

## 🐛 Solución de Problemas

### Error: "Unable to resolve host"
- Verifica que la URL en `RemoteModule.kt` sea correcta
- Asegúrate de que el Port Forwarding esté activo en VSCode

### Error: "SSL handshake failed"
- Ya está configurado en `network_security_config.xml`
- Si persiste, verifica que uses `https://` y no `http://`

### Error: "Connection refused"
- Verifica que tus microservicios estén corriendo
- Verifica que los puertos en VSCode coincidan con los de tus microservicios

---

## ✅ Checklist

- [ ] Port Forwarding activo en VSCode para todos los puertos
- [ ] Puertos marcados como **"Public"**
- [ ] URL base actualizada en `RemoteModule.kt`
- [ ] Usando `https://` si es URL de VSCode
- [ ] Microservicios corriendo
- [ ] Probado en POSTMAN primero
- [ ] App compilada y ejecutada

---

## 📝 Ejemplo Completo

**Antes (Localhost):**
```kotlin
private const val BASE_HOST = "http://10.0.2.2"
```

**Después (Port Forwarding):**
```kotlin
private const val BASE_HOST = "https://abc123-def456-ghi789.vscode.dev"
```

**Resultado:**
- Usuarios: `https://abc123-def456-ghi789.vscode.dev:8081/api/usuarios`
- Mascotas: `https://abc123-def456-ghi789.vscode.dev:8090/api/mascotas`
- Consultas: `https://abc123-def456-ghi789.vscode.dev:8091/api/consultas`
- Reseñas: `https://abc123-def456-ghi789.vscode.dev:8087/api/resenas`

---

¡Listo! Ahora puedes probar tus microservicios desde cualquier dispositivo usando Port Forwarding de VSCode. 🚀










