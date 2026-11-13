# GameMaps

Game-inspired navigation on real roads. A standalone Flutter mobile application designed for phones and projected car displays (Android Auto and Apple CarPlay), beginning with an initial visual theme inspired by the GTA San Andreas radar map rendered over real Costa Rican geography.

---

## Table of Contents

- [Overview](#overview)
- [Current Status & Roadmap](#current-status--roadmap)
- [Architecture & Engine Ownership](#architecture--engine-ownership)
- [Prerequisites & Android Setup](#prerequisites--android-setup)
- [AI Workflow & Local LLM Assistance](#ai-workflow--local-llm-assistance)
- [Development & Verification](#development--verification)
- [Project Structure](#project-structure)
- [Security & Permissions](#security--permissions)
- [Documentation Index](#documentation-index)

---

## Overview

GameMaps delivers real-world GPS turn-by-turn navigation through a distinct video-game aesthetic without modifying third-party navigation apps.

Key characteristics:
- **Real Geography**: Navigation routes, street geometry, and traffic flow represent real locations (initial validation context: Costa Rica / San José).
- **Stylized Map Surface**: High-contrast, game-inspired vector map styling (dark roads, muted terrain, distinct player and destination markers).
- **Multi-Surface Target**: Designed from the core to serve both handheld phone viewports and automotive head units (Android Auto and CarPlay).

---

## Current Status & Roadmap

The project follows a phased implementation roadmap detailed in [`Fases/02-implementation-phases.md`](Fases/02-implementation-phases.md):

| Phase | Milestone | Status | Key Focus |
| :--- | :--- | :--- | :--- |
| **P0** | Development & Cloud Setup | **In Progress** | Flutter shell, native Android architecture, Cloud key restrictions. |
| **P1** | Styled Car Map Feasibility | Planned | Proving Google Navigation custom styling on projected Android Auto surfaces. |
| **P2** | Android Phone Navigation MVP | Planned | End-to-end routing, turn guidance, and destination search on phone. |
| **P3** | Validated Android Auto MVP | Planned | Full driving guidance on in-vehicle head units via Android Auto. |
| **P4** | iOS & Apple CarPlay | Deferred | Requires macOS development environment and Apple Developer access. |
| **P5** | Extensible Theme Engine | Future | Additional visual map styles beyond the initial game theme. |
| **P6** | Release & Distribution | Future | Production signing, store compliance, and distribution. |

**Current Implementation State:**
- The Flutter application shell lives in [`app/`](app/).
- Map home renders using `google_navigation_flutter` (`0.11.2`).
- Native Android location port (`AndroidLocationPort`) provides foreground GPS fixes.
- Google Places autocomplete (`AndroidPlacesPort`) provides place search and destination selection.
- All Flutter widget/unit tests (22 tests) and Android native unit tests (19 tests) pass.

---

## Architecture & Engine Ownership

To support seamless transitions between phone displays and vehicle head units, GameMaps decouples the Flutter engine lifecycle from standard Android Activity lifecycles:

1. **Application-Owned Flutter Engine**: The Flutter engine is instantiated and managed by `GameMapsApplication` via `CarEngineOwner`. `MainActivity` borrows the running engine (`shouldDestroyEngineWithHost() = false`), allowing head units to initialize or maintain state independently.
2. **Dynamic Host Bootstrap**: `main.dart` defers UI mounting via `CarNavigationBootstrap`. The phone UI (`GameMapsApp`) mounts only once a phone host is attached, enabling headless automotive startup.
3. **Dedicated Platform Channels**:
   - `gamemaps/car_navigation`: Dispatches host attachment and connectivity revisions (`hostChanged`).
   - `gamemaps/map_home`: Bridges `locate` (foreground fix) and `search` (Google Places autocomplete) between Dart and Kotlin.
   - `gamemaps/navigation_setup_probe`: Debug-only platform probes (scoped to `app/android/app/src/debug/`).

For architectural specifications and class designs, see [`Fases/03-architecture.md`](Fases/03-architecture.md).

---

## Prerequisites & Android Setup

### Environment Requirements
- **Flutter SDK**: `^3.13.5` (Dart SDK compatible)
- **Java**: JDK 17
- **Android SDK**: `minSdk 24`, `compileSdk 35`
- **Android NDK & Desugaring**: Java 8+ API desugaring 2.1.5

### Pinned Dependencies & Plugin Compatibility
Dependencies are pinned to guarantee compatibility with Android `compileSdk 35`:
- `google_navigation_flutter`: `0.11.2`
- `flutter_plugin_android_lifecycle`: `2.0.26`
- Native Google Navigation SDK: `7.9.0`
- Google Places SDK: `4.3.1` (excludes transitive `play-services-maps` to prevent duplicate runtime symbols with Navigation)

### Google Maps API Key Configuration
The Android build requires a nonblank `MAPS_API_KEY` defined in `app/android/local.properties`. This key is injected into `AndroidManifest.xml` via manifest placeholders and must **never** be committed to version control.

Add the following to `app/android/local.properties`:

```properties
MAPS_API_KEY=your_actual_google_maps_api_key_here
```

Required Google Cloud APIs on the key:
- **Maps SDK for Android**
- **Navigation SDK for Android**
- **Places API (New)**

See [`Fases/04-google-cloud-setup.md`](Fases/04-google-cloud-setup.md) for billing considerations and SHA-1 certificate restriction instructions.

---

## AI Workflow & Local LLM Assistance

This repository enforces an AI agent coding contract governed by [`AGENTS.md`](AGENTS.md) and [`CLAUDE.md`](CLAUDE.md):
- **Local-First Code Authorship**: Source code and tests are synthesized by local LLM providers (`pumbastudio` on LM Studio or `pumbalama` on Ollama) using an offline cache-aside documentation flow (`.context/`).
- **Local LLM Helper Script**: If an MCP connection times out or reasoning cannot be turned off, use [`scripts/ask_local_llm.py`](scripts/ask_local_llm.py):
  ```bash
  python3 scripts/ask_local_llm.py --user prompt.md --out response.md --code-out src/file.dart
  ```

---

## Development & Verification

All standard application commands are executed from the `app/` subfolder, while native tests run from `app/android/`:

### Flutter Quality Checks
```bash
cd app

# Fetch dependencies
flutter pub get

# Static analysis
flutter analyze

# Run Flutter test suite
flutter test

# Run a specific test file
flutter test test/features/map_home/map_home_controller_test.dart

# Build debug APK
flutter build apk --debug --no-pub
```

### Native Android Tests
```bash
cd app/android

# Run all native unit tests
./gradlew :app:testDebugUnitTest

# Run specific engine owner tests
./gradlew :app:testDebugUnitTest --tests "com.example.gamemaps.car.CarEngineOwnerTest"
```

---

## Project Structure

```text
GameMaps/
├── AGENTS.md                  # Unified AI coding agent workflow and rules
├── CLAUDE.md                  # Project commands and architectural quick-reference
├── README.md                  # Root project overview and developer guide
├── Fases/                     # Phase documentation, architecture, and specifications
│   ├── 01-product-brief.md
│   ├── 02-implementation-phases.md
│   ├── 03-architecture.md
│   ├── 04-google-cloud-setup.md
│   ├── 05-validation-and-release.md
│   ├── 06-development-environment.md
│   └── 07-android-auto-feasibility.md
├── scripts/                   # Local LLM and orchestration helper scripts
│   ├── ask_local_llm.py       # Local LLM querying CLI with fallback support
│   └── configure_ollama_models.sh
└── app/                       # Flutter project root
    ├── README.md              # Android map home & connector details
    ├── pubspec.yaml           # Dependencies and Flutter manifest
    ├── lib/                   # Dart source code
    │   ├── main.dart          # Bootstrap entrypoint
    │   ├── app.dart           # Top-level widget
    │   └── features/          # Feature modules (map_home, car_navigation)
    ├── test/                  # Flutter unit and widget tests
    └── android/               # Native Android project (Kotlin)
        ├── local.properties   # Local configuration (MAPS_API_KEY, unversioned)
        └── app/src/           # Native source, platform channels, and tests
```

---

## Security & Permissions

- **Foreground Only**: Only `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` are requested. The app runs without background location services, notifications, boot, or wake permissions.
- **Privacy & Telemetry**: No user search queries, GPS coordinates, or location history are logged, stored remotely, or tracked.
- **Credential Protection**: The Google Maps API key is injected strictly at packaging time and excluded from version control.

---

## Documentation Index

- [`Fases/01-product-brief.md`](Fases/01-product-brief.md) — Product vision, confirmed requirements, and MVP scope.
- [`Fases/02-implementation-phases.md`](Fases/02-implementation-phases.md) — Roadmap milestones and phase exit criteria.
- [`Fases/03-architecture.md`](Fases/03-architecture.md) — Detailed engine architecture, multi-host registry, and platform channels.
- [`Fases/04-google-cloud-setup.md`](Fases/04-google-cloud-setup.md) — Cloud credentials, service enablement, and cost controls.
- [`Fases/05-validation-and-release.md`](Fases/05-validation-and-release.md) — Validation checklist and device testing rules.
- [`Fases/06-development-environment.md`](Fases/06-development-environment.md) — Toolchain status, device setup, and diagnostic records.
- [`Fases/07-android-auto-feasibility.md`](Fases/07-android-auto-feasibility.md) — Android Auto integration analysis and constraints.
- [`app/README.md`](app/README.md) — Android map home implementation details.
