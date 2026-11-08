package com.ejemplo.medilee   // <-- CAMBIA esto por el package de tu proyecto

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ---------- Colores (aprox. de tu diseño) ----------
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

    Scaffold(
        containerColor = VerdeMenta,
        bottomBar = {
            BarraInferior(
                actual = pantalla,
                onIr = { pantalla = it }
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
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Spacer(Modifier.height(8.dp))

                when (pantalla) {
                    Pantalla.Inicio -> PantallaInicio(
                        onCamara = { pantalla = Pantalla.Camara }
                    )
                    Pantalla.Historial -> PantallaHistorial()
                    Pantalla.Ajustes -> PantallaAjustes()
                    Pantalla.Camara -> PantallaCamara(
                        onCapturar = { pantalla = Pantalla.Analizando }
                    )
                    Pantalla.Analizando -> PantallaAnalizando(
                        onTerminar = { pantalla = Pantalla.Resultado }
                    )
                    Pantalla.Resultado -> PantallaResultado(
                        onGuardar = { pantalla = Pantalla.Historial },
                        onRepetir = { /* aquí irá el texto a voz */ }
                    )
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
fun PantallaInicio(onCamara: () -> Unit) {
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
            TextoSerif("Medicamentos recientes:", TextoVerde, 14, FontWeight.Bold)
            TextoSerif("• Paracetamol (hoy 8:00am)", TextoVerde, 13)
            TextoSerif("• Losartán (ayer)", TextoVerde, 13)
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
fun PantallaHistorial() {
    val items = listOf(
        "Paracetamol 500mg\nhoy, 8:00am",
        "Losartán 50mg\nayer, 9:00pm",
        "Metformina 850mg\nlun, 2:00pm"
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { texto ->
            TarjetaVerde(Modifier.fillMaxWidth()) { TextoSerif(texto, size = 14) }
        }
    }
}

@Composable
fun PantallaAjustes() {
    val opciones = listOf(
        "Velocidad de texto (-- 0 --)",
        "Tamaño de texto ( A A A )",
        "Idioma de lectura ( Español)"
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        opciones.forEach { texto ->
            TarjetaVerde(Modifier.fillMaxWidth(), onClick = { /* luego: abrir opción */ }) {
                TextoSerif(texto, size = 14, peso = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PantallaCamara(onCapturar: () -> Unit) {
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
            TextoSerif("(Cámara en vivo)", Color.White, 20)
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
fun PantallaAnalizando(onTerminar: () -> Unit) {
    // Simula que analiza durante 2 segundos y pasa al resultado
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
                TextoVerde, 14, align = TextAlign.Center
            )
        }
    }
}

@Composable
fun PantallaResultado(onGuardar: () -> Unit, onRepetir: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TarjetaVerde(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                TextoSerif("PARACETAMOL", Color.White, 26, FontWeight.Bold)
                TextoSerif("500 mg", Color.White, 26, FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))

        TarjetaAmarilla(Modifier.fillMaxWidth()) {
            TextoSerif("Descripción:", TextoVerde, 14, FontWeight.Bold)
            TextoSerif(
                "“Se usa para aliviar el dolor leve o moderado y bajar la fiebre. " +
                        "Tómalo según lo indicado por el médico”",
                TextoVerde, 14
            )
        }
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaVerde(Modifier.weight(1f), onClick = onGuardar) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextoSerif("Guardar", size = 14)
                }
            }
            TarjetaVerde(Modifier.weight(1f), onClick = onRepetir) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextoSerif("Repetir", size = 14)
                }
            }
        }
    }
}