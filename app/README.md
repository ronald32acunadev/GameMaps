# GameMaps application shell

This Flutter application currently shows `GameMaps` and `Navigation setup pending`. It makes no Google requests, requests no location permission and does not provide GPS navigation or car integration yet.

## Run checks

```bash
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
```

For a device launch, run `flutter devices`, then `flutter run -d <android-device-id>` using an actual Android device or emulator ID. Do not treat a web/Linux target as Android validation.

Android and iOS wrappers are generated. iOS/CarPlay has not been built or tested; macOS/Xcode access is required. The generated release-signing configuration is not production signing and must not be used as a publication-ready artifact.

The development identifier `com.example.gamemaps` is provisional. Google API keys and additional dependencies are intentionally deferred to the next phase. Keep local settings and signing credentials out of Git.

See the [project documentation](../README.md) and [bootstrap progress](../odd/tasks/android-bootstrap.md) for the approved scope and actual verification results.
