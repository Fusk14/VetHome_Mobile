plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

android {
    namespace = "com.example.myapplicationv"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.myapplicationv"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}


dependencies {

    // ===== ANDROIDX Y COMPOSE =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navegación / ViewModels
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

    // Animaciones e íconos
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material:material-icons-extended")

    // ===== ROOM + KSP =====
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ===== COROUTINAS =====
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // ===== DATASTORE =====
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ===== RETROFIT + OKHTTP =====
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // ===== COIL =====
    implementation("io.coil-kt:coil-compose:2.4.0")

    // 🔥 DESUGARING — NECESARIO PARA LocalDate, Period, Instant, etc.
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    //librerias de test locales
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.robolectric:robolectric:4.13")
    //Test de implementacion de UI
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    //reglas adicionales
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test:rules:1.5.0")
    // FALTANTES PARA CORRUTINAS EN TEST
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // FALTANTES PARA ARCHITECTURE COMPONENTS TEST
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // FALTANTES PARA VIEWMODEL Y LIVE DATA TEST
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")

    // FALTANTES PARA ROBOELECTRIC MÁS RECIENTE (opcional pero recomendado)
    testImplementation("org.robolectric:robolectric:4.11.1")
}
