# =============================================================================
# TUI Novel Android - ProGuard Rules
# Правила для минификации в релизной сборке
# =============================================================================

# Chaquopy - не обфусцировать Python bridge классы
-keep class com.chaquo.python.** { *; }
-keepclassmembers class com.chaquo.python.** { *; }
-dontwarn com.chaquo.python.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Compose - не трогать Composable функции
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Приложение - сохраняем классы для Python bridge
-keep class com.deadboizxc.tuinovel.python.** { *; }
-keepclassmembers class com.deadboizxc.tuinovel.python.** { *; }
