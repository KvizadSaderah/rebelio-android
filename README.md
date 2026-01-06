# Rebelio Android

[![Build Status](https://github.com/KvizadSaderah/rebelio-android/actions/workflows/build-apk.yml/badge.svg)](https://github.com/KvizadSaderah/rebelio-android/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Android client for Rebelio — a censorship-resilient end-to-end encrypted messenger.

## Features

- 🔐 End-to-end encryption (Signal Protocol)
- 🚀 Native Rust core via UniFFI
- 📱 Modern UI with Jetpack Compose
- 🔄 Multi-transport support (WebSocket, QUIC)

## Requirements

- Android Studio Hedgehog or newer
- Android SDK 24+ (Android 7.0)
- JDK 17

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew testDebugUnitTest
```

## Architecture

```
app/
├── src/main/
│   ├── java/          # Kotlin source code
│   │   ├── ui/        # Compose UI components
│   │   └── uniffi/    # Generated Rust bindings
│   └── jniLibs/       # Native .so libraries
│       ├── arm64-v8a/
│       ├── armeabi-v7a/
│       └── x86_64/
```

## CI/CD

Native libraries are automatically updated via PR from [rebelio-client-lib](https://github.com/KvizadSaderah/rebelio-client-lib).

## License

MIT License — see [LICENSE](LICENSE) for details.
