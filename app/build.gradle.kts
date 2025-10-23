plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" // <-- VERSIÓN AGREGADA
}

android {
    namespace = "com.example.myapplicationv"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplicationv"
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

    // TUS DEPENDENCIAS ACTUALES
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    //navegacion
    implementation("androidx.navigation:navigation-compose:2.9.5") // ACTUALIZADA
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4") // ACTUALIZADA
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2") // MANTENIDA (no estaba en el primero)
    implementation("androidx.compose.animation:animation") // MANTENIDA (no estaba en el primero)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3:1.1.2")


    //room y ksp
    implementation("androidx.room:room-runtime:2.6.1") // ACTUALIZADA
    implementation("androidx.room:room-ktx:2.6.1")  // ACTUALIZADA
    ksp("androidx.room:room-compiler:2.6.1")       // ACTUALIZADA

    //corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2") // ACTUALIZADA

    // NUEVAS DEPENDENCIAS DEL PRIMER ARCHIVO
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4") // <-- NUEVA
}