# GameMaps Android map preview

**Current verification:** Android debug build and Samsung installation pass with the user-updated local API key. Real map streets/labels around San Jose were visually verified; app-bar Back restores GameMaps / Navigation setup pending / Open map, and reopening works. Previous analyzer/two-widget-test and independent dummy-key build/manifest checks passed. Feature review and commit remain pending.

The home screen preserves `GameMaps` and `Navigation setup pending`. Tap `Open map` to open a Google map centered approximately on San Jose, Costa Rica; return with the app bar or Android Back. This preview does not track your position, request location permission, calculate routes, initialize guidance, or provide Android Auto/CarPlay support.

## Configure Android locally

Keep your own key in ignored `android/local.properties`, alongside existing SDK paths:

```properties
MAPS_API_KEY=YOUR_API_KEY_HERE
```

Do not replace SDK properties or commit the key. Gradle requires a nonblank key and supplies the manifest metadata through a placeholder. API/application restrictions remain necessary because a compiled mobile app contains the credential; an ignored local file does not make the distributed APK secret.

The development application ID is `com.example.gamemaps`; use the actual signing-certificate SHA-1 for restrictions. Google Navigation and Maps SDK for Android must be enabled in the same billed Cloud project. Do not send keys in chat or relax restrictions just to make a test pass.

## Run checks

```bash
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
```

For a live test, use `flutter devices`, then `flutter run -d <android-device-id>` with an authorized Android target. Opening the map can produce metered Google service usage; automated tests do not use the actual key or prove remote authentication. Native platform-view creation alone is not proof of rendered map tiles.

The official `google_navigation_flutter` dependency is pinned to 0.11.2. Android minSdk is 24 with NIO core-library desugaring2.1.5, required by native Navigation7.9.0. The preview manifest explicitly removes inherited location, foreground-service, notification, boot and wake-lock permissions; add actual navigation capabilities only in their separately approved phase. No map ID or game style is configured yet. Do not initialize a navigation session for this bare-map preview.

Android and iOS wrappers exist, but only the Android preview is in this unit. iOS/CarPlay is unverified and needs macOS/Xcode plus its own key/configuration. Generated debug release-signing defaults are not production signing. No publication is authorized.

See [project documentation](../README.md), [map-preview progress](../odd/tasks/google-map-preview.md), and the [environment report](../docs/06-development-environment.md) for observed—not assumed—verification.
