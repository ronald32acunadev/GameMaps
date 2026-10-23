# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Read `AGENTS.md` first: it defines the mandatory English-only rule and the code-authorship workflow (local LLM providers `pumbastudio`/`pumbalama` synthesize source code, with a Library Context Brief built from the lockfile and the `.context/` cache).

## Project

GameMaps: game-inspired navigation on real roads. A Flutter phone app (`app/`) using Google Navigation for maps/routing, with native Android Auto (and later CarPlay) integrations. Planning and decision docs live in `Fases/` (product brief, phases, architecture, Google Cloud setup, validation, environment, Android Auto feasibility). Current work is Android-only; there is no Mac, so iOS/CarPlay is unverified.

## Commands

Run from `app/`:

```bash
flutter pub get
flutter analyze
flutter test
flutter test test/features/map_home/map_home_controller_test.dart   # single file
flutter test --plain-name "<test name>"                              # single test
flutter build apk --debug --no-pub
```

Native unit tests, from `app/android/`:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:testDebugUnitTest --tests "com.example.gamemaps.car.CarEngineOwnerTest"
```

The Android build fails unless `app/android/local.properties` contains a nonblank `MAPS_API_KEY`. It is injected via a manifest placeholder; never put it in Dart or version control. Independent build checks run in fresh copies outside the worktree with a dummy key — never copy the real key or signing files.

## Architecture

**Engine ownership.** The Flutter engine is owned by `GameMapsApplication` (via `CarEngineOwner`), not by the Activity. `MainActivity` borrows it (`provideFlutterEngine`, `shouldDestroyEngineWithHost() = false`) so the phone UI and a car surface share one runtime. `CarHostRegistry`/`CarNavigationBridge` track which hosts (phone, car) are attached and push `hostChanged` revisions to Dart.

**Dart startup.** `main.dart` does not call `runApp` directly: `CarNavigationBootstrap` listens on `gamemaps/car_navigation` and runs `GameMapsApp` only once a phone host is attached. A car may start the engine with no phone UI.

**Platform channels** (Dart ↔ Kotlin, one bridge per feature):
- `gamemaps/car_navigation` — host attach state; deliberately exposes no routing commands.
- `gamemaps/map_home` — `locate` (foreground location fix) and `search` (Google Places autocomplete). Dart side: `MapHomeGateway` interface + `MethodChannelMapHomeGateway`; Kotlin side: `MapHomeBridge` delegating to `AndroidLocationPort`/`AndroidPlacesPort`.
- `gamemaps/navigation_setup_probe` — debug-only (`kDebugMode`); the matching Kotlin probes live in `android/app/src/debug/`.

**Features** are organized by folder under `lib/features/` (Dart) and matching packages under `com.example.gamemaps.*` (Kotlin). Widgets take the gateway and map builder as constructor parameters so tests inject fakes instead of hitting channels or Google SDKs.

## Constraints

- Pinned versions are deliberate (compileSdk 35 plugin compatibility): `google_navigation_flutter` 0.11.2, `flutter_plugin_android_lifecycle` 2.0.26, native Navigation 7.9.0, Places 4.3.1 (excludes `play-services-maps` because Navigation bundles Maps), minSdk 24, Java 17. Do not bump without re-verifying compatibility.
- Only foreground location permissions. No background location, notifications, boot/wake permissions, and no logging of keys, queries, or coordinates.
- No state-management package, backend, analytics, or extra map provider has been chosen; add a dependency only for a demonstrated requirement (`Fases/03-architecture.md`).
- Invariants from the architecture: at most one active trip across phone and car; route and guidance refer to the same destination; reconnects never start duplicate guidance; failures never look like a successful trip.
- Device validation: the user accepts OS permission dialogs; the agent does not tap them or change device settings. Live Places queries need explicit authorization. Build/widget proof is not real-service proof.
- `.atl/` and `.codegraph/` are local tooling state — never stage with a blanket `git add`.
