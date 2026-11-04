plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" // KSP: procesador de anotaciones para Room (genera el código del DAO/DB)
}

android {
    namespace = "com.induztek.protocolos"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.induztek.protocolos"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //librerias nuevas
    implementation("androidx.navigation:navigation-compose:2.9.5")            // Navegación entre las 5 pantallas (Navigation Compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")    // viewModel() dentro de Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")      // collectAsStateWithLifecycle()
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2") // Corrutinas (tareas asíncronas)
    // Material icons (cámara, ubicación, historial, búsqueda, etc.)
    implementation("androidx.compose.material:material-icons-extended")
    // Animaciones de navegación y de entrada del formulario
    implementation("androidx.compose.animation:animation")

    // CameraX: cámara en vivo con PreviewView (igual que el proyecto del profesor)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Room (SQLite) - runtime y extensiones KTX
    implementation("androidx.room:room-runtime:2.6.1")    // Runtime de Room
    implementation("androidx.room:room-ktx:2.6.1")        // Extensiones Kotlin (Flow, suspend)

    // Compilador de Room vía KSP
    ksp("androidx.room:room-compiler:2.6.1")              // Genera DAO_Impl y AppDatabase_Impl

    //manipular la carga de imagenes en el cache temporal
    implementation("io.coil-kt:coil-compose:2.7.0")       // Carga la foto capturada (Uri) en la UI

    // Ubicación: recurso nativo del dispositivo
    implementation("com.google.android.gms:play-services-location:21.1.0")              // FusedLocationProviderClient
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")     // await() para las Tasks de Google Play
}