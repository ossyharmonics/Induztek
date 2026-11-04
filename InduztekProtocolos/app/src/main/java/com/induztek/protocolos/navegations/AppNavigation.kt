package com.induztek.protocolos.navegations

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.induztek.protocolos.view.ui.CameraScreen
import com.induztek.protocolos.view.ui.CompararScreen
import com.induztek.protocolos.view.ui.HomeScreen
import com.induztek.protocolos.view.ui.LoginScreen
import com.induztek.protocolos.view.ui.PruebaDetailScreen
import com.induztek.protocolos.view.ui.PruebaFormScreen
import com.induztek.protocolos.view.ui.PruebaListScreen
import com.induztek.protocolos.view.ui.TipoUsuarioScreen
import com.induztek.protocolos.view.ui.UserFormScreen
import com.induztek.protocolos.view.ui.UserListScreen
import com.induztek.protocolos.view.utils.navEnterTransition
import com.induztek.protocolos.view.utils.navExitTransition
import com.induztek.protocolos.view.utils.navPopEnterTransition
import com.induztek.protocolos.view.utils.navPopExitTransition
import com.induztek.protocolos.viewmodels.EmpresaViewModel
import com.induztek.protocolos.viewmodels.LoginViewModel
import com.induztek.protocolos.viewmodels.PruebaViewModel
import com.induztek.protocolos.viewmodels.TipoUsuarioViewModel
import com.induztek.protocolos.viewmodels.UserViewModel
import kotlinx.coroutines.launch

/**
 * RUTAS DE NAVEGACIÓN
 *
 * Se usa un object con constantes y funciones helper:
 *   - LOGIN es el destino inicial.
 *   - HOME es el centro de la app después de autenticarse.
 *   - PRUEBAS es el historial (equivalente a Productos del profesor).
 *   - USERS y TIPOS son los CRUD de usuarios y tipos de usuario.
 *   - CAMERA es la pantalla de demostración del recurso nativo.
 *
 * Los formularios llevan argumento id:
 *   prueba_form?id=0  -> crear
 *   prueba_form?id=7  -> editar
 *   user_form?id=0    -> crear
 *   user_form?id=3    -> editar
 */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PRUEBAS = "pruebas"
    const val PRUEBA_FORM = "prueba_form?id={id}"
    const val PRUEBA_DETAIL = "prueba_detalle/{idPrueba}"
    const val COMPARAR = "comparar/{idPrueba}"
    const val USERS = "users"
    const val USER_FORM = "user_form?id={id}"
    const val MI_PERFIL = "mi_perfil"
    const val CAMERA = "camera"
    const val TIPOS = "tipos_usuario"

    fun pruebaForm(id: Long = 0L) = "prueba_form?id=$id"
    fun userForm(id: Int = 0) = "user_form?id=$id"
    fun detalle(id: Long) = "prueba_detalle/$id"
    fun comparar(id: Long) = "comparar/$id"
}

/**
 * Opción del menú lateral.
 * route: destino al que navega.
 * label: texto visible.
 * icon: ícono Material.
 */
data class MenuItem(val route: String, val label: String, val icon: ImageVector)

/**
 * Opciones del menú lateral, en el mismo orden y con la misma idea del profesor:
 * Inicio, la lista principal (Historial), Usuarios, Tipos y Cámara.
 */
val menuItems = listOf(
    MenuItem(Routes.HOME, "Inicio", Icons.Default.Home),
    MenuItem(Routes.PRUEBAS, "Historial", Icons.Default.History),
    MenuItem(Routes.USERS, "Usuarios", Icons.Default.People),
    MenuItem(Routes.TIPOS, "Tipos de usuario", Icons.Default.Badge),
    MenuItem(Routes.CAMERA, "Cámara", Icons.Default.PhotoCamera)
)

// Id del tipo "Técnico" sembrado por AppDatabase.seedCallback
// (1=Administrador, 2=Técnico). El técnico no administra usuarios: solo su perfil.
private const val ID_TIPO_TECNICO = 2

// Menú del rol Técnico: se quitan Usuarios y Tipos, y se agrega Mi perfil.
private val menuTecnico = listOf(
    MenuItem(Routes.HOME, "Inicio", Icons.Default.Home),
    MenuItem(Routes.PRUEBAS, "Historial", Icons.Default.History),
    MenuItem(Routes.MI_PERFIL, "Mi perfil", Icons.Default.Person),
    MenuItem(Routes.CAMERA, "Cámara", Icons.Default.PhotoCamera)
)

/** Devuelve el título de la barra superior según la ruta actual. */
private fun titleFor(route: String?): String = when (route) {
    Routes.HOME -> "Inicio"
    Routes.PRUEBAS -> "Historial"
    Routes.PRUEBA_FORM -> "Prueba"
    Routes.PRUEBA_DETAIL -> "Detalle de prueba"
    Routes.COMPARAR -> "Comparar anterior"
    Routes.USERS -> "Usuarios"
    Routes.USER_FORM -> "Usuario"
    Routes.MI_PERFIL -> "Mi perfil"
    Routes.CAMERA -> "Cámara"
    Routes.TIPOS -> "Tipos de usuario"
    else -> ""
}

/**
 * NAVEGACIÓN PRINCIPAL (capa View).
 *
 *
 *   - ModalNavigationDrawer con datos del usuario logueado y logout.
 *   - Scaffold con TopAppBar global (menú en pantallas principales,
 *     flecha de volver en formularios/detalles).
 *   - NavHost con las pantallas y las tres animaciones de navegación.
 *
 * ¿Cómo se comunica?
 *   MainActivity entrega los cinco ViewModels.
 *   LoginViewModel -> loginState -> decide si se muestra el drawer y quién es
 *   el usuario actual.
 *   PruebaViewModel -> totalPruebas lo observa HomeScreen.
 *   Cada pantalla recibe sus ViewModels y callbacks de navegación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    pruebaViewModel: PruebaViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    empresaViewModel: EmpresaViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Estado de sesión: usuario logueado (o null si todavía no entra).
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // El login no muestra barra superior ni permite abrir el drawer con gestos.
    val isLogin = currentRoute == null || currentRoute == Routes.LOGIN
    val isTopLevel = menuItems.any { it.route == currentRoute }

    // El técnico no administra usuarios ni tipos: su menú solo incluye su perfil.
    val esTecnico = loginState.loggedUser?.tipoUsuarioId == ID_TIPO_TECNICO
    val opcionesMenu = if (esTecnico) menuTecnico else menuItems

    // Al volver al Login se cierra cualquier drawer abierto para que no quede
    // visible sobre la pantalla de inicio de sesión.
    LaunchedEffect(isLogin) {
        if (isLogin && drawerState.isOpen) drawerState.close()
    }

    /**
     * Navegación entre opciones principales.
     * popUpTo(HOME) mantiene Inicio como raíz y evita apilar pantallas
     * principales una sobre otra. launchSingleTop evita duplicar la actual.
     */
    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = false }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isLogin,
        drawerContent = {
            // En el Login no se compone ningún menú lateral.
            if (!isLogin) {
                ModalDrawerSheet {
                    Spacer(Modifier.height(16.dp))

                    // Datos del usuario autenticado (vienen de LoginViewModel).
                    Text(
                        loginState.loggedUser?.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Text(
                        loginState.loggedUser?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    // Opciones principales según el rol del usuario.
                    opcionesMenu.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.label) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            selected = currentRoute == item.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navigateTopLevel(item.route)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    // Cerrar sesión: limpia LoginViewModel y vuelve al Login
                    // borrando toda la pila para que "atrás" no regrese a Home.
                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            loginViewModel.logout()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // La barra superior es global
                if (!isLogin) {
                    TopAppBar(
                        title = { Text(titleFor(currentRoute)) },
                        navigationIcon = {
                            if (isTopLevel) {
                                // En pantallas principales se abre el menú lateral.
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            } else {
                                // En formularios y detalles se vuelve atrás.
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                loginViewModel = loginViewModel,
                pruebaViewModel = pruebaViewModel,
                userViewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                empresaViewModel = empresaViewModel,
                currentUserId = loginState.loggedUser?.id,
                onNavigateTopLevel = ::navigateTopLevel
            )
        }
    }
}

/**
 * GRAFO DE NAVEGACIÓN.
 *
 * Conecta cada ruta con su pantalla y traduce los argumentos de la URL a
 * tipos Kotlin (Long para pruebas, Int para usuarios).
 *
 * Las transiciones de entrada/salida vienen de view/utils/Animations.kt
 * (ANIMACIÓN 3: deslizamiento horizontal).
 */
@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    loginViewModel: LoginViewModel,
    pruebaViewModel: PruebaViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    empresaViewModel: EmpresaViewModel,
    currentUserId: Int?,
    onNavigateTopLevel: (String) -> Unit
) {
    // Argumento id para el formulario de prueba (Long, porque Prueba.id es Long).
    val longIdArg = listOf(
        navArgument("id") {
            type = NavType.LongType
            defaultValue = 0L
        }
    )

    // Argumento id para el formulario de usuario (Int, porque User.id es Int).
    val intIdArg = listOf(
        navArgument("id") {
            type = NavType.IntType
            defaultValue = 0
        }
    )

    // Argumento idPrueba para detalle y comparación.
    val idPruebaArg = listOf(
        navArgument("idPrueba") { type = NavType.LongType }
    )

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,
        enterTransition = navEnterTransition,
        exitTransition = navExitTransition,
        popEnterTransition = navPopEnterTransition,
        popExitTransition = navPopExitTransition
    ) {

        // ---------------- LOGIN ----------------
        composable(Routes.LOGIN) {
            LoginScreen(loginViewModel) {
                // Al entrar, Home reemplaza al Login en la pila.
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }

        // ---------------- INICIO ----------------
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = pruebaViewModel,
                onNuevaPrueba = { navController.navigate(Routes.pruebaForm()) },
                onVerHistorial = { onNavigateTopLevel(Routes.PRUEBAS) }
            )
        }

        // ---------------- HISTORIAL / LISTA DE PRUEBAS ----------------
        composable(Routes.PRUEBAS) {
            PruebaListScreen(
                viewModel = pruebaViewModel,
                userViewModel = userViewModel,
                onAdd = { navController.navigate(Routes.pruebaForm()) },
                onEdit = { id -> navController.navigate(Routes.pruebaForm(id)) },
                onDetail = { id -> navController.navigate(Routes.detalle(id)) }
            )
        }

        // ---------------- FORMULARIO DE PRUEBA ----------------
        composable(Routes.PRUEBA_FORM, arguments = longIdArg) { entry ->
            PruebaFormScreen(
                viewModel = pruebaViewModel,
                empresaViewModel = empresaViewModel,
                pruebaId = entry.arguments?.getLong("id") ?: 0L,
                currentUserId = currentUserId,
                onDone = { navController.popBackStack() }
            )
        }

        // ---------------- DETALLE DE PRUEBA ----------------
        composable(Routes.PRUEBA_DETAIL, arguments = idPruebaArg) { entry ->
            val id = entry.arguments?.getLong("idPrueba") ?: 0L

            PruebaDetailScreen(
                viewModel = pruebaViewModel,
                userViewModel = userViewModel,
                idPrueba = id,
                onComparar = { navController.navigate(Routes.comparar(id)) }
            )
        }

        // ---------------- COMPARAR CON LA ANTERIOR ----------------
        composable(Routes.COMPARAR, arguments = idPruebaArg) { entry ->
            CompararScreen(
                viewModel = pruebaViewModel,
                idPrueba = entry.arguments?.getLong("idPrueba") ?: 0L,
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------------- LISTA DE USUARIOS ----------------
        composable(Routes.USERS) {
            UserListScreen(
                viewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                currentUserId = currentUserId,
                onAdd = { navController.navigate(Routes.userForm()) },
                onEdit = { id -> navController.navigate(Routes.userForm(id)) }
            )
        }

        // ---------------- FORMULARIO DE USUARIO ----------------
        composable(Routes.USER_FORM, arguments = intIdArg) { entry ->
            UserFormScreen(
                viewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                userId = entry.arguments?.getInt("id") ?: 0,
                onDone = { navController.popBackStack() }
            )
        }

        // ---------------- MI PERFIL (solo rol Técnico) ----------------
        // Reutiliza UserFormScreen con el id del usuario logueado y bloquea el
        // cambio de rol: el técnico edita sus datos, no sus permisos.
        composable(Routes.MI_PERFIL) {
            currentUserId?.let { userId ->
                UserFormScreen(
                    viewModel = userViewModel,
                    tipoUsuarioViewModel = tipoUsuarioViewModel,
                    userId = userId,
                    onDone = { navController.popBackStack() },
                    bloquearTipoUsuario = true
                )
            }
        }

        // ---------------- CÁMARA ----------------
        composable(Routes.CAMERA) {
            CameraScreen()
        }

        // ---------------- TIPOS DE USUARIO ----------------
        composable(Routes.TIPOS) {
            TipoUsuarioScreen(tipoUsuarioViewModel)
        }
    }
}
