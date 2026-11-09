package com.ejemplo.medilee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ejemplo.medilee.data.AppDatabase
import com.ejemplo.medilee.data.MedicamentoEntity
import com.ejemplo.medilee.util.TTSManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- Colores ----------
val VerdeOscuro = Color(0xFF6E9B84)
val VerdeMenta = Color(0xFF8FC1AA)
val Amarillo = Color(0xFFE0E3B0)
val FondoClaro = Color(0xFFDDF0E7)
val BordeAmarillo = Color(0xFFE6E0A0)
val TextoVerde = Color(0xFF5E8A74)

enum class Pantalla { Inicio, Historial, Ajustes, Camara, Analizando, Resultado }

// ---------- App principal ----------
@Composable
fun MediLeeApp() {
    var pantalla by remember { mutableStateOf(Pantalla.Inicio) }

    // Estados globales de ajustes
    var velocidadLectura by remember { mutableFloatStateOf(1.0f) }
    var multiplicadorTexto by remember { mutableIntStateOf(0) } // -2: Pequeño, 0: Normal, 3: Grande
    var idiomaEspanol by remember { mutableStateOf(true) }

    val context = LocalContext.current
    var ttsManager by remember { mutableStateOf<TTSManager?>(null) }
    val scope = rememberCoroutineScope()

    // Inicialización segura de TTSManager en el ciclo de vida
    DisposableEffect(context) {
        val manager = TTSManager(context)
        ttsManager = manager
        onDispose {
            manager.shutdown()
        }
    }

    // Actualización de parámetros TTS en tiempo real
    LaunchedEffect(velocidadLectura, idiomaEspanol, ttsManager) {
        ttsManager?.setSpeechRate(velocidadLectura)
        val locale = if (idiomaEspanol) Locale("es", "ES") else Locale.US
        ttsManager?.setLanguage(locale)
    }

    // Instancia de Room Database
    val database = remember { AppDatabase.getDatabase(context) }
    val medicamentoDao = database.medicamentoDao()
    val historialList by medicamentoDao.obtenerHistorial().collectAsState(initial = emptyList())

    Scaffold(
        containerColor = VerdeMenta,
        bottomBar = {
            BarraInferior(
                actual = pantalla,
                onIr = { nuevaPantalla ->
                    ttsManager?.stop()
                    pantalla = nuevaPantalla
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .background(FondoClaro, RoundedCornerShape(20.dp))
                .padding(12.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                Text(
                    "MediLee",
                    color = TextoVerde,
                    fontSize = (22 + multiplicadorTexto).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Spacer(Modifier.height(8.dp))

                when (pantalla) {
                    Pantalla.Inicio -> PantallaInicio(
                        historial = historialList,
                        deltaTamaño = multiplicadorTexto,
                        onCamara = { pantalla = Pantalla.Camara }
                    )
                    Pantalla.Historial -> PantallaHistorial(
                        historial = historialList,
                        deltaTamaño = multiplicadorTexto
                    )
                    Pantalla.Ajustes -> PantallaAjustes(
                        velocidad = velocidadLectura,
                        onVelocidadChange = { velocidadLectura = it },
                        multiplicadorTexto = multiplicadorTexto,
                        onTamañoChange = { multiplicadorTexto = it },
                        esEspanol = idiomaEspanol,
                        onIdiomaChange = { idiomaEspanol = it },
                        onProbarVoz = {
                            val demo = if (idiomaEspanol) "Prueba de velocidad de lectura" else "Speech rate test"
                            ttsManager?.speak(demo)
                        }
                    )
                    Pantalla.Camara -> PantallaCamara(
                        deltaTamaño = multiplicadorTexto,
                        onCapturar = { pantalla = Pantalla.Analizando }
                    )
                    Pantalla.Analizando -> PantallaAnalizando(
                        deltaTamaño = multiplicadorTexto,
                        onTerminar = { pantalla = Pantalla.Resultado }
                    )
                    Pantalla.Resultado -> {
                        val nombreMed = "PARACETAMOL"
                        val dosisMed = "500 mg"
                        val descMed = if (idiomaEspanol) {
                            "Se usa para aliviar el dolor leve o moderado y bajar la fiebre."
                        } else {
                            "Used to relieve mild to moderate pain and reduce fever."
                        }
                        val textoLectura = "$nombreMed $dosisMed. $descMed"

                        LaunchedEffect(Unit) {
                            ttsManager?.speak(textoLectura)
                        }

                        PantallaResultado(
                            deltaTamaño = multiplicadorTexto,
                            onGuardar = {
                                ttsManager?.stop()
                                scope.launch {
                                    val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                    medicamentoDao.insertarMedicamento(
                                        MedicamentoEntity(
                                            nombre = nombreMed,
                                            dosis = dosisMed,
                                            descripcion = descMed,
                                            fecha = fechaActual
                                        )
                                    )
                                    pantalla = Pantalla.Historial
                                }
                            },
                            onRepetir = {
                                ttsManager?.speak(textoLectura)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ---------- Componentes reutilizables ----------
@Composable
fun TarjetaVerde(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .shadow(4.dp, forma)
            .background(VerdeOscuro, forma)
            .border(2.dp, BordeAmarillo, forma)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(12.dp),
        content = content
    )
}

@Composable
fun TarjetaAmarilla(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .shadow(4.dp, forma)
            .background(Amarillo, forma)
            .padding(12.dp),
        content = content
    )
}

@Composable
fun TextoSerif(
    texto: String,
    color: Color = Amarillo,
    size: Int = 15,
    peso: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Start
) {
    Text(
        texto,
        color = color,
        fontSize = size.sp,
        fontWeight = peso,
        fontFamily = FontFamily.Serif,
        textAlign = align
    )
}

// ---------- Barra inferior ----------
@Composable
fun BarraInferior(actual: Pantalla, onIr: (Pantalla) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VerdeMenta)
            .navigationBarsPadding()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ItemBarra(Icons.Filled.Home, actual != Pantalla.Historial && actual != Pantalla.Ajustes) {
            onIr(Pantalla.Inicio)
        }
        ItemBarra(Icons.Filled.History, actual == Pantalla.Historial) { onIr(Pantalla.Historial) }
        ItemBarra(Icons.Filled.Settings, actual == Pantalla.Ajustes) { onIr(Pantalla.Ajustes) }
    }
}

@Composable
fun ItemBarra(icono: ImageVector, seleccionado: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 16.dp)
    ) {
        Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        Box(
            Modifier
                .padding(top = 2.dp)
                .width(32.dp)
                .height(2.dp)
                .background(if (seleccionado) TextoVerde else Color.Transparent)
        )
    }
}

// ---------- Pantallas ----------
@Composable
fun PantallaInicio(
    historial: List<MedicamentoEntity>,
    deltaTamaño: Int,
    onCamara: () -> Unit
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TarjetaVerde(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            onClick = onCamara
        ) {
            Icon(
                Icons.Filled.CameraAlt, contentDescription = "Abrir cámara",
                tint = Color.White, modifier = Modifier.size(90.dp).align(Alignment.Center)
            )
        }
        Spacer(Modifier.height(12.dp))

        TarjetaAmarilla(Modifier.fillMaxWidth()) {
            TextoSerif("Medicamentos recientes:", TextoVerde, 14 + deltaTamaño, FontWeight.Bold)
            if (historial.isEmpty()) {
                TextoSerif("• No hay medicamentos escaneados", TextoVerde, 13 + deltaTamaño)
            } else {
                historial.take(2).forEach { item ->
                    TextoSerif("• ${item.nombre} ${item.dosis} (${item.fecha})", TextoVerde, 13 + deltaTamaño)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Filled.Mic, contentDescription = "Hablar",
            tint = Color(0xFF666666), modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun PantallaHistorial(historial: List<MedicamentoEntity>, deltaTamaño: Int) {
    if (historial.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TextoSerif("Aún no tienes historial guardado", TextoVerde, 16 + deltaTamaño, FontWeight.Bold)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(historial) { med ->
                TarjetaVerde(Modifier.fillMaxWidth()) {
                    Column {
                        TextoSerif("${med.nombre} ${med.dosis}", size = 16 + deltaTamaño, peso = FontWeight.Bold)
                        TextoSerif(med.descripcion, size = 13 + deltaTamaño)
                        TextoSerif("Guardado: ${med.fecha}", size = 11 + deltaTamaño, color = Amarillo.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaAjustes(
    velocidad: Float,
    onVelocidadChange: (Float) -> Unit,
    multiplicadorTexto: Int,
    onTamañoChange: (Int) -> Unit,
    esEspanol: Boolean,
    onIdiomaChange: (Boolean) -> Unit,
    onProbarVoz: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Velocidad de texto
        TarjetaVerde(Modifier.fillMaxWidth()) {
            Column {
                TextoSerif("Velocidad de texto: ${String.format("%.1fx", velocidad)}", size = 15 + multiplicadorTexto, peso = FontWeight.Bold)
                Slider(
                    value = velocidad,
                    onValueChange = onVelocidadChange,
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Amarillo,
                        activeTrackColor = Amarillo,
                        inactiveTrackColor = FondoClaro
                    )
                )
            }
        }

        // 2. Tamaño de texto
        TarjetaVerde(Modifier.fillMaxWidth()) {
            Column {
                TextoSerif("Tamaño de texto", size = 15 + multiplicadorTexto, peso = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onTamañoChange(-2) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (multiplicadorTexto == -2) Amarillo else FondoClaro
                        )
                    ) {
                        Text("Pequeño", color = TextoVerde)
                    }
                    Button(
                        onClick = { onTamañoChange(0) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (multiplicadorTexto == 0) Amarillo else FondoClaro
                        )
                    ) {
                        Text("Normal", color = TextoVerde)
                    }
                    Button(
                        onClick = { onTamañoChange(3) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (multiplicadorTexto == 3) Amarillo else FondoClaro
                        )
                    ) {
                        Text("Grande", color = TextoVerde)
                    }
                }
            }
        }

        // 3. Idioma de lectura
        TarjetaVerde(Modifier.fillMaxWidth()) {
            Column {
                TextoSerif("Idioma de lectura", size = 15 + multiplicadorTexto, peso = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onIdiomaChange(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esEspanol) Amarillo else FondoClaro
                        )
                    ) {
                        Text("Español", color = TextoVerde)
                    }
                    Button(
                        onClick = { onIdiomaChange(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!esEspanol) Amarillo else FondoClaro
                        )
                    ) {
                        Text("English", color = TextoVerde)
                    }
                }
            }
        }

        // Botón para probar la voz
        TarjetaVerde(
            modifier = Modifier.fillMaxWidth(),
            onClick = onProbarVoz
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextoSerif("🔊 Probar voz actual", size = 16 + multiplicadorTexto, peso = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PantallaCamara(deltaTamaño: Int, onCapturar: () -> Unit) {
    val forma = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp)
            .shadow(4.dp, forma)
            .background(Color.Black, forma),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextoSerif("(Cámara en vivo)", Color.White, 20 + deltaTamaño)
            Spacer(Modifier.height(16.dp))
            Icon(
                Icons.Filled.CameraAlt, contentDescription = "Tomar foto",
                tint = Color.White,
                modifier = Modifier.size(40.dp).clickable { onCapturar() }
            )
        }
    }
}

@Composable
fun PantallaAnalizando(deltaTamaño: Int, onTerminar: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTerminar()
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(180.dp).shadow(6.dp, CircleShape).background(VerdeOscuro, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.MoreHoriz, null, tint = Color.White, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(32.dp))
        TarjetaAmarilla(Modifier.fillMaxWidth()) {
            TextoSerif(
                "Analizando el medicamento, un momento",
                TextoVerde, 14 + deltaTamaño, align = TextAlign.Center
            )
        }
    }
}

@Composable
fun PantallaResultado(
    deltaTamaño: Int,
    onGuardar: () -> Unit,
    onRepetir: () -> Unit
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TarjetaVerde(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                TextoSerif("PARACETAMOL", Color.White, 26 + deltaTamaño, FontWeight.Bold)
                TextoSerif("500 mg", Color.White, 26 + deltaTamaño, FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))

        TarjetaAmarilla(Modifier.fillMaxWidth()) {
            TextoSerif("Descripción:", TextoVerde, 14 + deltaTamaño, FontWeight.Bold)
            TextoSerif(
                "“Se usa para aliviar el dolor leve o moderado y bajar la fiebre.”",
                TextoVerde, 14 + deltaTamaño
            )
        }
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaVerde(Modifier.weight(1f), onClick = onGuardar) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextoSerif("Guardar", size = 14 + deltaTamaño)
                }
            }
            TarjetaVerde(Modifier.weight(1f), onClick = onRepetir) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextoSerif("Repetir", size = 14 + deltaTamaño)
                }
            }
        }
    }
}