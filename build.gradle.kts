// Archivo: build.gradle.kts (Proyecto)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {
    dependencies {

    }
}
