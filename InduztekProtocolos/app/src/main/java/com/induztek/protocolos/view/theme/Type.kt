package com.induztek.protocolos.view.theme

import androidx.compose.material3.Typography      // Sistema de tipografías de Material 3
import androidx.compose.ui.text.TextStyle         // Estilo de texto
import androidx.compose.ui.text.font.FontFamily   // Familia tipográfica
import androidx.compose.ui.text.font.FontWeight   // Grosor de la fuente
import androidx.compose.ui.unit.sp                // Unidad de tamaño (sp)

// Tipografía base del proyecto (valores por defecto de Material 3).
// Se define explícitamente para poder ajustar estilos en el futuro
// sin tener que tocar ninguna pantalla.
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
