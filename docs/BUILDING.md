# Building Novel Engine

## Prerequisites

### All Platforms

- **CMake** 3.16 or higher
- **C Compiler** (GCC, Clang, or MSVC)
- **Python** 3.8+ (for Python bindings)
- **JDK** 17+ (for Kotlin GUI)
- **Gradle** 8.0+ (for Kotlin GUI)

### Linux

```bash
sudo apt install build-essential cmake python3-dev openjdk-17-jdk
```

### macOS

```bash
brew install cmake python openjdk@17
```

### Windows

- Visual Studio 2019+ with C++ workload
- CMake (via Visual Studio or separate install)
- Python from python.org
- JDK from adoptium.net

---

## Building the C Core

### Linux/macOS

```bash
cd core
cmake -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build --parallel

# Run tests
cd build && ctest --output-on-failure
```

### Windows

```powershell
cd core
cmake -B build -G "Visual Studio 17 2022"
cmake --build build --config Release

# Run tests
cd build
ctest -C Release --output-on-failure
```

### Build Options

| Option | Default | Description |
|--------|---------|-------------|
| `CMAKE_BUILD_TYPE` | Debug | Build type (Debug, Release, RelWithDebInfo) |
| `BUILD_SHARED_LIBS` | ON | Build shared library |
| `BUILD_TESTS` | ON | Build test suite |
| `ENABLE_YAML` | OFF | Enable YAML support (requires libyaml) |

```bash
cmake -B build \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_SHARED_LIBS=ON \
    -DBUILD_TESTS=ON
```

### Output

After building:
- `core/build/libnovel_engine.so` (Linux)
- `core/build/libnovel_engine.dylib` (macOS)
- `core/build/Release/novel_engine.dll` (Windows)

---

## Building Python Bindings

### Install in Development Mode

```bash
cd bindings/python
pip install -e .
```

### Configure Library Path

The Python bindings need to find `libnovel_engine.so`. Set the path:

```python
from novel_engine import NovelEngine

# Automatic detection (searches common paths)
engine = NovelEngine()

# Explicit path
engine = NovelEngine(lib_path="/path/to/libnovel_engine.so")
```

Or set environment variable:

```bash
export NOVEL_ENGINE_LIB=/path/to/libnovel_engine.so
```

### Run TUI

```bash
cd bindings/python
python -m novel_engine.tui ../../stories/blue_frequency
```

---

## Building Kotlin JNI Library

### Prerequisites

Set `JAVA_HOME`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

### Build

```bash
cd bindings/kotlin/jni

cmake -B build \
    -DCMAKE_BUILD_TYPE=Release \
    -DNOVEL_ENGINE_LIB=/path/to/core/build/libnovel_engine.so

cmake --build build
```

### Output

- `bindings/kotlin/jni/build/libnovel_engine_jni.so` (Linux)
- `bindings/kotlin/jni/build/libnovel_engine_jni.dylib` (macOS)
- `bindings/kotlin/jni/build/Release/novel_engine_jni.dll` (Windows)

---

## Building Kotlin GUI

### Prerequisites

1. Build C core library
2. Build JNI library
3. Copy libraries to accessible path

### Configure Library Path

Edit `gui/build.gradle.kts` or set environment:

```bash
export LD_LIBRARY_PATH=/path/to/libs:$LD_LIBRARY_PATH
```

### Build and Run

```bash
cd gui

# Run in development mode
./gradlew run

# Build distribution
./gradlew packageDistributionForCurrentOS

# Build installers
./gradlew packageDeb    # Linux .deb
./gradlew packageRpm    # Linux .rpm
./gradlew packageMsi    # Windows .msi
./gradlew packageDmg    # macOS .dmg
```

### Output

Distributions in `gui/build/compose/binaries/`:
- `gui/build/compose/binaries/main/deb/novelengine_2.0.0_amd64.deb`
- `gui/build/compose/binaries/main/msi/NovelEngine-2.0.0.msi`
- `gui/build/compose/binaries/main/dmg/NovelEngine-2.0.0.dmg`

---

## Building Everything (Full Build)

### Linux

```bash
#!/bin/bash
set -e

# Build core
echo "Building C core..."
cd core
cmake -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build --parallel
cd ..

# Test core
echo "Testing C core..."
cd core/build && ctest --output-on-failure && cd ../..

# Build JNI
echo "Building JNI bindings..."
cd bindings/kotlin/jni
cmake -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build
cd ../../..

# Install Python bindings
echo "Installing Python bindings..."
cd bindings/python
pip install -e .
cd ../..

# Build GUI
echo "Building GUI..."
export LD_LIBRARY_PATH=$PWD/core/build:$PWD/bindings/kotlin/jni/build:$LD_LIBRARY_PATH
cd gui
./gradlew build

echo "Build complete!"
```

---

## Cross-Compilation

### Linux → Windows (MinGW)

```bash
cd core
cmake -B build-win \
    -DCMAKE_TOOLCHAIN_FILE=../cmake/mingw-w64.cmake \
    -DCMAKE_BUILD_TYPE=Release
cmake --build build-win
```

### Android (NDK)

```bash
cd core
cmake -B build-android \
    -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK/build/cmake/android.toolchain.cmake \
    -DANDROID_ABI=arm64-v8a \
    -DANDROID_PLATFORM=android-24
cmake --build build-android
```

---

## Troubleshooting

### Library Not Found

```
Error: libnovel_engine.so: cannot open shared object file
```

Solution:
```bash
export LD_LIBRARY_PATH=/path/to/lib:$LD_LIBRARY_PATH
```

### JNI Load Failed

```
java.lang.UnsatisfiedLinkError: no novel_engine_jni in java.library.path
```

Solution:
```bash
java -Djava.library.path=/path/to/jni/lib -jar app.jar
```

### CMake Can't Find JNI

```
Could NOT find JNI
```

Solution:
```bash
export JAVA_HOME=/path/to/jdk
cmake -B build -DJAVA_HOME=$JAVA_HOME
```

### Tests Fail

Run with verbose output:
```bash
ctest --output-on-failure --verbose
```

---

## IDE Setup

### CLion (C Core)

1. Open `core/` as CMake project
2. Configure toolchain in Settings → Build → Toolchains
3. Build and run tests

### IntelliJ IDEA (Kotlin GUI)

1. Open `gui/` as Gradle project
2. Wait for sync
3. Run `Main.kt`

### VS Code

Install extensions:
- C/C++ (Microsoft)
- CMake Tools
- Kotlin
- Gradle

Open project root and use CMake Tools for C, Gradle for Kotlin.
