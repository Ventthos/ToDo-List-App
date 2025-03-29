// Archivo: build.gradle.kts (Módulo `app`)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.ventthos.todo_list_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ventthos.todo_list_app"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 🚀 Agrega dependencias de Firebase
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1") // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore-ktx:24.9.1") // Firestore
    implementation("com.google.firebase:firebase-database-ktx:20.3.0") // Realtime Database
}


apply(plugin = "com.google.gms.google-services")
