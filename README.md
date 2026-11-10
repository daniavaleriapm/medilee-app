# MediLee

Aplicación Android que **lee en voz alta la información de un medicamento**. La persona toma una foto del empaque, la app reconoce el nombre y la dosis, verifica el medicamento en una API pública, muestra una descripción breve de su uso y puede leerla en voz alta. Está pensada para personas con dificultad para leer letra pequeña.

**Versión:** v26.1.1

## Funcionalidades

- **Cámara en vivo** (CameraX) para fotografiar el empaque.
- **Reconocimiento de texto (OCR)** con ML Kit para detectar nombre y dosis (ej. *Paracetamol 500 mg*).
- **Consulta a una API real** (CIMA, AEMPS) para verificar que el medicamento existe.
- **Pantalla de resultado** con nombre, dosis y breve descripción de uso.
- **Lectura en voz alta** (Text-to-Speech de Android) con botón *Repetir*.
- **Historial** guardado en base de datos local (Room), que se conserva al cerrar la app.
- **Botón de micrófono** en el inicio: lee el último medicamento guardado.
- **Ajustes:** velocidad de lectura, tamaño de texto e idioma (Español / English).
- **Manejo de errores:** foto sin texto, sin conexión (con botón *Reintentar*), medicamento no encontrado y permiso de cámara denegado.

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje / UI | Kotlin, Jetpack Compose (Material 3) |
| Arquitectura | MVVM (ViewModel, StateFlow, Repository) |
| Red | Retrofit 2.11.0, OkHttp 4.12.0, Gson |
| API externa | CIMA – AEMPS (`https://cima.aemps.es/cima/rest/`) |
| Base de datos local | Room 2.8.4 con KSP |
| Cámara | CameraX 1.4.1 |
| OCR | ML Kit Text Recognition 16.0.1 (en el dispositivo) |
| Voz | Android TextToSpeech |
| Build | Android Gradle Plugin 9.4.1, Kotlin 2.2.10, compileSdk 37, minSdk 24 |

## Arquitectura MVVM

```
com.ejemplo.medilee
├── MainActivity.kt / MediLeeApp.kt      Actividad de entrada y navegación entre pantallas
├── MediLeeApplication.kt                 Clase Application (crea el AppContainer)
├── core/common/                          Tipos de resultado y de error
├── di/AppContainer.kt                    Construcción de Retrofit, repositorio y servicios
├── domain/
│   ├── model/                            Medicamento, DeteccionMedicamento
│   ├── repository/                       Contrato MedicamentoRepository
│   └── usecase/                          DetectorMedicamento, DescripcionMedicamento
├── data/
│   ├── remote/api, remote/dto/           CimaApi (Retrofit) y DTOs
│   ├── repository/                       MedicamentoRepositoryImpl
│   └── (Room)                            AppDatabase, MedicamentoDao, MedicamentoEntity
├── integration/
│   ├── camara/                           Captura de foto con CameraX
│   └── ocr/                              Lectura de texto con ML Kit
├── presentation/
│   ├── camara/                           CamaraScreen, CamaraViewModel, CamaraUiState
│   └── analisis/                         AnalisisScreen, AnalisisViewModel, AnalisisUiState
└── util/TTSManager.kt                    Voz (Text-to-Speech)
```

### Flujo de información

```
CamaraScreen ──foto──▶ AnalisisScreen ──▶ AnalisisViewModel ──▶ MedicamentoRepository
                                                                    │
                                  ReconocedorTexto (ML Kit) ◀───────┤
                                  DetectorMedicamento (nombre/dosis)◀┤
                                  CimaApi (Retrofit) ◀──────────────┘
Resultado ◀── UiState (cargando / éxito / error) ◀── AnalisisViewModel
```

Las pantallas no hacen peticiones HTTP ni consultas SQL: observan el estado del ViewModel, y el repositorio decide qué fuente consultar.

## API y almacenamiento

- **API:** CIMA (Centro de Información de Medicamentos, AEMPS de España). Se consulta `GET /medicamentos?nombre=<texto>&comerc=1`. No requiere clave.
- **Almacenamiento local:** Room guarda el historial de medicamentos (nombre, dosis, descripción y fecha).

## Requisitos

- Android Studio compatible con AGP 9.4.1 (versión reciente).
- JDK 11 o superior (incluido con Android Studio).
- Dispositivo o emulador con Android 7.0 (API 24) o superior.
- Conexión a internet para consultar la API.
- Cámara y permiso de cámara (para probar el OCR se recomienda un celular real).

## Cómo ejecutar

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/daniavaleriapm/medilee-app.git
   ```
2. Abrir la carpeta en Android Studio y esperar a que termine **Sync**.
3. Conectar un celular (con depuración USB) o iniciar un emulador.
4. Pulsar **Run ▶**.
5. Aceptar el permiso de cámara, tomar una foto del empaque de un medicamento y revisar el resultado.

También se puede instalar directamente el archivo **APK** de la sección *Releases* del repositorio.

## Pruebas

- `DetectorMedicamentoTest` (prueba unitaria): comprueba la detección de nombre y dosis a partir del texto del empaque.

## Limitaciones conocidas

- CIMA es una base de datos de España: reconoce bien nombres genéricos (paracetamol, losartán, metformina) pero puede no encontrar marcas comerciales de otros países.
- La calidad del OCR depende de la luz y del enfoque de la foto.
- Las descripciones de uso se toman de un diccionario local de medicamentos comunes; para los demás se muestra un texto genérico que recomienda consultar al médico.
- Los ajustes (velocidad, tamaño, idioma) se mantienen mientras la app está abierta, pero no se guardan al cerrarla.
- La inyección de dependencias es manual (`AppContainer`), sin Hilt.

> MediLee es una herramienta de apoyo para lectura. No sustituye la indicación de un médico ni de un farmacéutico.

## Flujo de trabajo en Git

Ramas `feature/...` creadas desde `develop`, integradas mediante Pull Request (nunca directo a `main`).

| Rama | Contenido | Responsable |
|---|---|---|
| `feature/vista-camara` | Cámara en vivo con CameraX | Persona A |
| `feature/integracion-ocr-api` | OCR + API CIMA + pantalla "Analizando" | Persona A |
| `feature/pantalla-resultado` | Pantalla de resultado con datos reales | Persona A |
| `feature/integracion-tts` | Lectura en voz alta | Persona B |
| `feature/base-de-datos-historial` | Room e historial | Persona B |
| `feature/pantalla-ajustes` | Ajustes de voz, tamaño e idioma | Persona B |

## Evidencias

Capturas de los recorridos principales en la carpeta `docs/capturas/`:

- Inicio, cámara en vivo, "Analizando" y resultado
- Historial (después de cerrar y reabrir la app)
- Ajustes

## Equipo

- **Persona A:** Valeria (`daniavaleriapm`) — cámara, OCR/API y pantalla de resultado.
- **Persona B:** Carlos Castro (`CarlosCastro253385`) — voz (TTS), base de datos e historial, ajustes.
