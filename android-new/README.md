# TUI Novel Android

> Психоделический терминал для визуальной новеллы с эффектами в стиле Doki Doki Literature Club и Serial Experiments Lain.

## Описание

Android-обёртка для Python движка TUI Novel с полной интеграцией через Chaquopy. Приложение эмулирует хакерский терминал с зелёным текстом на чёрном фоне и множеством психоделических эффектов.

## Особенности

- 🖥️ **Терминальный UI** - полноэкранный интерфейс в стиле хакерского терминала
- 🐍 **Python интеграция** - встроенный Python через Chaquopy
- ✨ **Психоделические эффекты**:
  - Глитч-эффекты с RGB сдвигом
  - Тряска экрана
  - Инверсия цветов
  - Шум и помехи
  - VHS искажения
  - Scanlines (CRT эффект)
  - Matrix rain
  - "Глаза" оверлей (Lain style)
- 🎮 **Игровой движок** - полноценная визуальная новелла с:
  - Ветвящимся сюжетом
  - Системой выборов
  - Сохранениями
  - Условиями и флагами
- 📱 **Android оптимизация** - поддержка API 24+

## Технологии

- **Kotlin** - основной язык
- **Jetpack Compose** - UI фреймворк
- **Coroutines** - асинхронность
- **Material 3** - дизайн система

## Структура проекта

```
android-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/deadboizxc/tuinovel/
│   │   │   ├── MainActivity.kt          # Точка входа
│   │   │   ├── TuiNovelApp.kt           # Application класс
│   │   │   ├── python/
│   │   │   │   └── PythonBridge.kt      # Мост Python-Kotlin
│   │   │   └── ui/
│   │   │       ├── components/          # UI компоненты
│   │   │       ├── effects/             # Психоделические эффекты
│   │   │       ├── screens/             # Экраны
│   │   │       └── theme/               # Тема приложения
│   │   ├── python/                      # Python код
│   │   │   ├── android_engine.py        # Адаптер движка
│   │   │   └── engine/                  # Оригинальный движок
│   │   ├── assets/                      # Story файлы
│   │   └── res/                         # Android ресурсы
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml               # Версии зависимостей
└── build.gradle.kts
```

## Сборка

### Требования

- Android Studio 2024.1+ (Koala)
- JDK 17+
- Android SDK 35
- Python 3.8+ (для Chaquopy)

### Шаги

1. Откройте проект в Android Studio
2. Синхронизируйте Gradle
3. Подключите устройство или запустите эмулятор
4. Нажмите Run

Если возникают проблемы:
```bash
# Очистка кэша
./gradlew clean

# Пересборка
./gradlew assembleDebug
```

## Триггеры эффектов

Эффекты активируются автоматически по ключевым словам в тексте:

| Ключевые слова | Эффект |
|----------------|--------|
| глитч, ошибка, сбой | Glitch |
| психоз, безумие, кровь | Psycho flash + глаза |
| страх, ужас, крик | Тряска экрана |
| воспоминание, прошлое | VHS эффект |
| матрица, код, система | Matrix rain |

## Настройка историй

Истории хранятся в `assets/story/` в формате YAML:

```yaml
scene_name:
  text: |
    Текст сцены с **маркдаун** форматированием.
  actions:
    - animate:
        type: "glitch"
        duration: 2
    - add_coin: 10
    - set_var: { sanity: 80 }
  choices:
    - text: "Вариант 1"
      next: "other_scene"
    - text: "Вариант 2"
      conditions:
        - flag: has_key
      next: "locked_scene"
```

## Лицензия

MIT License

## Авторы

- Основной движок: [deadboizxc](https://github.com/deadboizxc)
- Android версия: GitHub Copilot
