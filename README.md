# Novel Engine

> Кроссплатформенный движок для текстовых визуальных новелл с C-ядром и Kotlin Multiplatform GUI.

## 🏗️ Архитектура

```
┌─────────────────────────────────────────────────────────────────┐
│              Kotlin Multiplatform Compose (GUI)                 │
├─────────────────┬─────────────────┬─────────────────────────────┤
│    Desktop      │     Android     │      iOS (future)           │
├─────────────────┴─────────────────┴─────────────────────────────┤
│                     JNI / cinterop                              │
├─────────────────────────────────────────────────────────────────┤
│                    libnovel_engine.so                           │
│                        (C Core)                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 📁 Структура проекта

```
novel_engine/
├── core/                  # C ядро движка
│   ├── include/           # Публичный API (novel_engine.h)
│   ├── src/               # Реализация
│   ├── deps/              # Зависимости (cJSON, libyaml)
│   └── tests/             # Тесты ядра
├── bindings/              # Биндинги для языков
│   ├── python/            # ctypes обёртка
│   ├── kotlin/            # JNI мост
│   └── go/                # cgo биндинги
├── gui/                   # Kotlin Multiplatform GUI
│   ├── shared/            # Общий UI код
│   ├── android/           # Android приложение
│   └── desktop/           # Desktop приложение
├── cli/                   # CLI версии
│   ├── python/            # Python TUI
│   └── c/                 # ncurses TUI
├── stories/               # Истории (YAML/JSON)
├── tools/                 # Утилиты (компилятор, валидатор)
├── assets/                # Ресурсы (шрифты, звуки, изображения)
└── docs/                  # Документация
```

## 🔧 Сборка

### C Core

```bash
cd core
cmake -B build
cmake --build build
```

### Python CLI

```bash
cd bindings/python
pip install -e .
novel-engine-cli
```

### Kotlin GUI

```bash
cd gui
./gradlew :desktop:run      # Desktop
./gradlew :android:build    # Android APK
```

## 📖 Формат историй

Истории описываются в YAML или JSON:

```yaml
start:
  text: |
    Вы открываете глаза.
    Комната погружена во тьму.
  choices:
    - text: "Включить свет"
      next: "light_on"
    - text: "Закрыть глаза"
      next: "sleep"

light_on:
  text: "Лампа мигает. В зеркале — не вы."
  actions:
    - add_coin: -10
    - set_flag: mirror_seen
  choices:
    - text: "Подойти"
      next: "approach"
      conditions:
        - coins: 50
```

## 📄 Лицензия

MIT License — см. [LICENSE](LICENSE)
