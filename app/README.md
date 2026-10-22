# GameMaps Android map home

**Current status:** direct map home and native foreground-location/Google Places connectors are implemented. Analyzer,13Flutter tests,13native production tests, independent lifecycle probes and debug builds pass. The integrated APK has not been installed on the Samsung: ADB currently has no attached device. Physical location/Places behavior, final feature review and commits remain pending.

## Phone flow

Open GameMaps to show the real map without a launcher. With foreground location permission and a fresh fix, the camera centers on the user. Recenter requests another foreground fix. Denial, disabled providers or a15-second timeout leave the map/search usable with an explicitly non-current San Jose reference.

Search places opens Google's native autocomplete UI. Selecting a place resolves its identity/name/address/coordinates and displays one destination marker/summary. Cancellation or failure preserves the previous selection; Clear destination removes it. There are no routes, guidance, game themes or car integrations yet.

## Android configuration

Keep `MAPS_API_KEY` in ignored `android/local.properties`, never in Dart or version control. The native build validates a nonblank key and injects it through a manifest placeholder. APK credentials remain extractable: use Android package/signing-certificate and API restrictions, not secrecy of the binary.

The project/key needs the existing map/navigation services and **Places API (New)** for search. Billing applies according to Google's service/field/session rules. Do not remove key restrictions to work around a failure. Cloud configuration is user-owned; no automatic service enablement.

Pinned integration:
- `google_navigation_flutter`0.11.2 and lifecycle support2.0.26 preserve compileSdk35 plugin compatibility.
- Native Navigation7.9.0 is also explicit on the app compile classpath.
- Places4.3.1 excludes standalone `play-services-maps`, because Navigation bundles Maps.
- Android minSdk24, NIO desugaring2.1.5, Java17 bytecode.

Only foreground coarse/fine location is requested; approximate permission works. Background location/service, notifications, boot and wake permissions remain removed. No app-owned location history or key/query/coordinate logging is added.

## Verification

```bash
flutter pub get
flutter analyze
flutter test
flutter build apk --debug --no-pub
```

Native unit tests use `./gradlew :app:testDebugUnitTest` from `android/`. Independent native/build checks run in fresh outside-worktree copies with dummy local properties; never copy the actual key/signing files into them.

Device validation must separately observe the actual OS permission/fix and a bounded live Places selection. The user accepts native OS consent; the agent does not click permission dialogs or alter device settings. An agent-run live Places query requires explicit authorization. Build/widget proof alone is not real-service proof.

Android-only current evidence does not establish iOS/CarPlay support. No Mac is available. Release-signing defaults remain development-only. No push/publication is authorized.

See [feature progress](../odd/tasks/map-first-place-search.md) and [environment report](../docs/06-development-environment.md).
