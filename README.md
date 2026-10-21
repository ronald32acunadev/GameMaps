# GameMaps

A proposed Flutter navigation app that displays real roads with video-game-inspired map themes, starting with GTA San Andreas, on phones, Android Auto, and Apple CarPlay.

**Status:** Flutter application shell created in `app/`; unit/widget checks pass. Android debug APK and emulator launch are verified; native review is approved and acknowledged; bootstrap committed as `e2ddf18`. Google map preview builds and renders real San Jose-area streets on Samsung SM-A175F; return-to-home and reopening the map are verified. Preview review/commit remain pending. Navigation, game styling and car integrations are not implemented. No Google Cloud resources have been configured by this work.

## Start here

1. Review the [product brief](docs/01-product-brief.md).
2. Follow the [implementation phases](docs/02-implementation-phases.md).
3. Check the [proposed architecture](docs/03-architecture.md) before implementation.

Supporting guides:
- [Google Cloud setup and costs](docs/04-google-cloud-setup.md)
- [Validation and release gates](docs/05-validation-and-release.md)
- [Observed development environment](docs/06-development-environment.md)
- [Android bootstrap implementation plan](docs/superpowers/plans/2026-10-21-android-bootstrap.md)

## Current priorities

- Validate Google map styling on Android Auto before committing to the full product.
- Build Android first; retain iOS and CarPlay as required targets, not verified capabilities.
- Do not treat a phone screenshot or a simulator result as proof that the car experience works.

The user approved the brief, architectural direction, and roadmap on 2026-10-21. The user also approved the Android bootstrap plan; later integration plans depend on actual feasibility findings. No commits, installations, cloud changes, or publication are authorized by the documents themselves.

## Development

```bash
cd app
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
flutter run -d <android-device-id>
```

Use an actual Android ID reported by `flutter devices`. The home screen says `Navigation setup pending`; the new `Open map` action is a bare-map preview, not GPS navigation. Configure your own ignored Android API key before building the preview. The provisional package is `com.example.gamemaps`.

See [bootstrap progress](odd/tasks/android-bootstrap.md) for observed checks, known blockers and commit status.

Current work: [Google map preview plan](docs/superpowers/plans/2026-10-21-google-map-preview.md) and [feature logbook](odd/tasks/google-map-preview.md).
