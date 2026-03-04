// =============================================================================
// TUI Novel Android - App Module Build Gradle
// Native C Engine with JNI + Jetpack Compose UI
// =============================================================================

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Kotlin Serialization для сохранений
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.deadboizxc.tuinovel"
    compileSdk = 35
    ndkVersion = "21.4.7075529"

    defaultConfig {
        applicationId = "com.deadboizxc.tuinovel.native"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // NDK configuration
        externalNativeBuild {
            cmake {
                cppFlags += ""
                arguments += "-DANDROID_STL=c++_static"
            }
        }
        
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }
    
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.18.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // Исключаем дубликаты файлов Python
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // ==========================================================================
    // AndroidX Core
    // ==========================================================================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // ==========================================================================
    // Jetpack Compose - современный UI toolkit
    // ==========================================================================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Compose Animation - для психоделических эффектов
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.graphics)

    // ==========================================================================
    // Coroutines - для асинхронной работы с Python
    // ==========================================================================
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // ==========================================================================
    // Kotlin Serialization для сохранений
    // ==========================================================================
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // ==========================================================================
    // SnakeYAML для парсинга story файлов
    // ==========================================================================
    implementation("org.yaml:snakeyaml:2.2")

    // ==========================================================================
    // Debug tools
    // ==========================================================================
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
