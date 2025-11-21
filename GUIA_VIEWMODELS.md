# 📱 Guía: ViewModels Separados (Patrón del Docente)

## ✅ ViewModels Creados

He creado ViewModels separados siguiendo el patrón del proyecto de tu docente:

1. **`AuthViewModel`** - Solo para autenticación (login/register)
2. **`MascotaViewModel`** - Para gestionar mascotas
3. **`ConsultaViewModel`** - Para gestionar consultas/citas
4. **`ResenaViewModel`** - Para gestionar reseñas

## 📁 Estructura

```
viewmodel/
├── AuthViewModel.kt          (solo autenticación)
├── MascotaViewModel.kt       (nuevo - para mascotas)
├── ConsultaViewModel.kt       (nuevo - para consultas)
├── ResenaViewModel.kt         (nuevo - para reseñas)
└── ViewModelFactory.kt       (factory para crear todos)
```

## 🔄 ¿Cómo Usarlos?

### Opción 1: Usar en las Pantallas (Recomendado)

**Ejemplo: Actualizar `PetListScreen` para usar `MascotaViewModel`:**

```kotlin
@Composable
fun PetListScreen(
    mascotaViewModel: MascotaViewModel = viewModel(),  // ← Nuevo ViewModel
    authViewModel: AuthViewModel,                       // ← Para obtener userId
    onBack: () -> Unit,
    onAddPet: () -> Unit,
    onPetDetail: (Long) -> Unit
) {
    val mascotasState by mascotaViewModel.uiState
    
    // Cargar mascotas al iniciar
    LaunchedEffect(Unit) {
        mascotaViewModel.loadMascotas()
    }
    
    // Usar mascotasState.mascotas en lugar de petsState.pets
    // ...
}
```

### Opción 2: Mantener AuthViewModel (Temporal)

Si prefieres mantener todo en `AuthViewModel` por ahora, puedes seguir usándolo. Los nuevos ViewModels están listos para cuando quieras migrar.

## 🎯 Patrón del Docente

Cada ViewModel sigue el mismo patrón que `PostViewModel`:

```kotlin
// 1. Estado UI
data class MascotasUiState(
    val isLoading: Boolean = false,
    val mascotas: List<MascotaDto> = emptyList(),
    val error: String? = null,
    val lastActionMessage: String? = null
)

// 2. ViewModel
class MascotaViewModel(
    private val repository: VetRepository
) : ViewModel() {
    var uiState by mutableStateOf(MascotasUiState())
        private set
    
    fun loadMascotas() { ... }
    fun createMascota(mascota: MascotaDto) { ... }
    fun deleteMascota(id: Long) { ... }
}
```

## 📝 Próximos Pasos

1. **Actualizar `NavGraph.kt`** para pasar los nuevos ViewModels a las pantallas
2. **Actualizar las pantallas** para usar los nuevos ViewModels
3. **Mantener `AuthViewModel`** solo para autenticación

## ⚠️ Nota Importante

Los ViewModels nuevos usan **DTOs directamente** (como el docente), no las Entities locales. Esto significa:
- ✅ Comunicación directa con microservicios
- ✅ Más simple y funcional
- ⚠️ Si necesitas datos locales, puedes combinarlos con `AuthViewModel` o agregar sincronización

## 🔗 Ejemplo Completo

Ver `MascotaViewModel.kt` para ver cómo está implementado siguiendo exactamente el patrón de `PostViewModel` del docente.






