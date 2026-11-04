package com.induztek.protocolos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.induztek.protocolos.navegations.AppNavigation
import com.induztek.protocolos.view.theme.InduztekTheme
import com.induztek.protocolos.viewmodels.EmpresaViewModel
import com.induztek.protocolos.viewmodels.LoginViewModel
import com.induztek.protocolos.viewmodels.PruebaViewModel
import com.induztek.protocolos.viewmodels.TipoUsuarioViewModel
import com.induztek.protocolos.viewmodels.UserViewModel

/**
 * MAINACTIVITY: única Activity de la app (patrón una Activity + Compose).
 *
 * Es la misma estructura del MainActivity del profesor:
 *   - Los cinco ViewModels se crean con by viewModels() y viven mientras
 *     la Activity esté viva.
 *   - Cada ViewModel es AndroidViewModel, por lo que puede obtener la base
 *     de datos con AppDatabase.getDatabase(application) sin un Factory manual.
 *   - AppNavigation recibe los ViewModels y construye el grafo de pantallas.
 *
 * Flujo de comunicación:
 *   MainActivity -> AppNavigation -> pantallas (view/ui)
 *                        |
 *                        +-> LoginViewModel   (sesión y login)
 *                        +-> PruebaViewModel  (historial, detalle, comparación)
 *                        +-> UserViewModel    (CRUD de usuarios)
 *                        +-> TipoUsuarioViewModel (catálogo de roles)
 *                        +-> EmpresaViewModel (catálogo de empresas/clientes)
 */
class MainActivity : ComponentActivity() {

    // ViewModels con alcance de Activity (capa ViewModel de MVVM).
    private val loginViewModel: LoginViewModel by viewModels()
    private val pruebaViewModel: PruebaViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val tipoUsuarioViewModel: TipoUsuarioViewModel by viewModels()
    private val empresaViewModel: EmpresaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // La interfaz respeta las barras del sistema (borde a borde).
        enableEdgeToEdge()

        setContent {
            // El tema envuelve toda la app
            InduztekTheme {
                AppNavigation(
                    loginViewModel = loginViewModel,
                    pruebaViewModel = pruebaViewModel,
                    userViewModel = userViewModel,
                    tipoUsuarioViewModel = tipoUsuarioViewModel,
                    empresaViewModel = empresaViewModel
                )
            }
        }
    }
}
