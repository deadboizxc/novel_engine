plugins {
    kotlin("multiplatform") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
}

group = "com.novelengine"
version = "2.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm("desktop") {
        jvmToolchain(17)
        withJava()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.common)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.novelengine.desktop.MainKt"
        
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Rpm,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg
            )
            packageName = "NovelEngine"
            packageVersion = "2.0.0"
            description = "Cross-platform Visual Novel Engine"
            copyright = "© 2024 Novel Engine Team"
            vendor = "Novel Engine"
            
            linux {
                iconFile.set(project.file("icons/icon.png"))
            }
            windows {
                iconFile.set(project.file("icons/icon.ico"))
                menuGroup = "Novel Engine"
            }
            macOS {
                iconFile.set(project.file("icons/icon.icns"))
            }
        }
    }
}
