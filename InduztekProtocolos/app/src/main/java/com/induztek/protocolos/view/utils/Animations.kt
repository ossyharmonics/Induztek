package com.induztek.protocolos.view.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavBackStackEntry

/*
 * ANIMACIONES DE LA APP, reunidas en un solo lugar (igual que el profesor).
 *
 *   ANIMACIÓN 1: AnimacionEntradaFormulario -> PruebaFormScreen y UserFormScreen
 *   ANIMACIÓN 2: Modifier.pulsoYRotacion()  -> logo de LoginScreen
 *   ANIMACIÓN 3: transiciones de navegación -> NavHost de AppNavigation
 *
 * Al centralizarlas aquí, todas las pantallas se comportan igual y no se
 * repite código de animación en cada archivo.
 */

// ---------------------------------------------------------------------------
// ANIMACIÓN 1: el contenido aparece con fade + deslizamiento vertical.
// Se usa al entrar a un formulario para dar una entrada suave a la pantalla.
// ---------------------------------------------------------------------------
@Composable
fun AnimacionEntradaFormulario(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 },
        content = content
    )
}

// ---------------------------------------------------------------------------
// ANIMACIÓN 2: animación infinita de pulso + rotación.
// Uso: Modifier.size(96.dp).pulsoYRotacion()
// Se aplica al logo de LoginScreen.
// ---------------------------------------------------------------------------
@Composable
fun Modifier.pulsoYRotacion(): Modifier {
    val transition = rememberInfiniteTransition(label = "pulsoYRotacion")

    val scale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "rotation"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationZ = rotation
    }
}

// ---------------------------------------------------------------------------
// ANIMACIÓN 3: transiciones horizontales entre pantallas.
// AppNavigation las entrega al NavHost: enter/exit para avanzar y
// popEnter/popExit para volver atrás.
// ---------------------------------------------------------------------------
private const val DURACION_NAV = 400

val navEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        tween(DURACION_NAV)
    ) + fadeIn(tween(DURACION_NAV))
}

val navExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        tween(DURACION_NAV)
    ) + fadeOut(tween(DURACION_NAV))
}

val navPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        tween(DURACION_NAV)
    ) + fadeIn(tween(DURACION_NAV))
}

val navPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        tween(DURACION_NAV)
    ) + fadeOut(tween(DURACION_NAV))
}
