# Induztek Protocolos

Aplicación Android para registrar pruebas eléctricas en terreno. Permite asociar mediciones a un equipo y a una empresa, adjuntar evidencia y comparar el resultado con una prueba anterior del mismo equipo. Los datos se guardan localmente en el dispositivo.

## Integrantes

- Xaviera Bradanovich
- Ossmar Herrera

## Funcionalidades

- Inicio de sesión local con menús para administración y técnicos.
- Registro y edición de pruebas con código de equipo, empresa, mediciones y observaciones.
- Captura de fotos con CameraX y registro de ubicación con los permisos de Android. Ambos datos son opcionales.
- Historial con búsqueda por equipo o empresa y comparación con la prueba anterior más reciente del mismo código.
- Administración de usuarios y tipos de usuario. El catálogo de empresas se puede ampliar desde el formulario.

## Cómo ejecutar

1. Clona el repositorio y abre la carpeta `InduztekProtocolos` en Android Studio.
2. Selecciona JDK 17 para Gradle e instala Android SDK 36 desde el SDK Manager. Sincroniza el proyecto para descargar las dependencias. El proyecto incluye el wrapper de Gradle 8.13.
3. Selecciona un emulador o conecta un dispositivo con Android 7.0 o superior (API 24). Para probar la ubicación, usa un dispositivo o emulador con servicios de Google Play y ubicación habilitada.
4. Ejecuta el módulo `app`. Puedes ingresar con la cuenta local de demostración `admin@duoc.cl` y la contraseña `admin123`.

Para generar el APK desde PowerShell, ejecuta `.\gradlew.bat :app:assembleDebug` desde la carpeta `InduztekProtocolos`.

## Organización del código

| Carpeta | Responsabilidad |
| --- | --- |
| `navegations` | Rutas, menú y navegación entre pantallas. |
| `view` | Pantallas Compose, tema, animaciones y utilidades de cámara. |
| `viewmodels` | Estado de sesión, operaciones de la app y reglas de validación. |
| `repository` | Entidades, DAO y configuración de Room sobre SQLite. |
