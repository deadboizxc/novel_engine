# Android App Template

Базовый шаблон Android приложения с Jetpack Compose.

## Структура

```
app-template/
├── app/
│   └── src/main/
│       ├── java/com/deadboizxc/template/
│       │   ├── ui/           # UI слой
│       │   │   ├── theme/    # Тема, цвета, типографика
│       │   │   ├── screens/  # Экраны
│       │   │   └── components/ # Переиспользуемые компоненты
│       │   ├── data/         # Данные (репозитории, API)
│       │   ├── domain/       # Бизнес-логика
│       │   └── di/           # Dependency Injection
│       └── res/              # Ресурсы
└── gradle/                   # Gradle wrapper
```

## Возможности

- ✅ Jetpack Compose UI
- ✅ Material 3
- ✅ Фиолетовая тема (настраиваемая)
- ✅ MVVM архитектура
- ✅ Navigation Compose
- ✅ Kotlin Coroutines
- ✅ Готовые компоненты (кнопки, карточки, ввод)

## Использование

1. Скопировать `app-template/` в новый проект
2. Переименовать пакет `com.deadboizxc.template` → `com.yourname.appname`
3. Обновить `applicationId` в `build.gradle.kts`
4. Заменить иконки
5. Начать разработку!

## Компоненты

### PurpleButton
```kotlin
PurpleButton(
    text = "Нажми меня",
    onClick = { /* action */ }
)
```

### PurpleCard
```kotlin
PurpleCard {
    Text("Контент карточки")
}
```

### PurpleTextField
```kotlin
PurpleTextField(
    value = text,
    onValueChange = { text = it },
    placeholder = "Введите текст"
)
```

## Цвета темы

| Цвет | Hex | Использование |
|------|-----|---------------|
| Primary | #7B2CBF | Основной |
| PrimaryLight | #9D4EDD | Акценты |
| PrimaryDark | #5A189A | Тёмные элементы |
| Background | #10002B | Фон |
| Surface | #240046 | Карточки |
| Text | #E0E0E0 | Текст |

## Лицензия

MIT
