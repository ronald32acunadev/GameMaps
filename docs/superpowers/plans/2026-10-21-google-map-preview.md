# Google Map Preview Implementation Plan

> **For agentic workers:** Use `superpowers:executing-plans` for inline execution. Delegate only when an applicable repository trigger fires; dependency/credential integration requires independent verification.

**Goal:** Open a real Google map on Android from an explicit preview action, without location tracking, route calculation, or guidance.

**Architecture:** Keep the current setup home screen and add a dedicated map-preview page. Read the Android credential from ignored local properties during the native build; do not pass it through Dart or put it in source control. Use the official navigation package's bare map view rather than initialize a navigation session.

**Tech Stack:** Existing Flutter 3.47.7 / Dart 3.13.5, `google_navigation_flutter` 0.11.2 (pin and retain lockfile), Android Kotlin/Gradle. The published plugin requires Android API 24+ and desugaring below API 34.

**Spec:** [Product brief](../../01-product-brief.md), S1, S2, S6-S8; the initial phone-map portion of [P1](../../02-implementation-phases.md). This unit does not complete P1's themed Android Auto feasibility gate.

**Status:** user-approved; Android preview builds and real map rendering plus Back/reopen are verified on Samsung SM-A175F after the user updated the local key. No exact Cloud-setting root cause is proven. Preview source `09217de` is reviewed and acknowledged; historical blocked attempts below describe earlier evidence.

## Global Constraints

- No change of map provider, actual location access, routing, voice, car service, theme switching, or Apple native configuration in this unit.
- Preserve `GameMaps`, `Navigation setup pending` and the absence of a `Start navigation` control on the home screen.
- Runtime UI/source are English; new entry action: `Open map`; preview title: `Map preview`.
- Use a fixed camera reference `LatLng(9.9281, -84.0907)` at zoom 12 as an approximate San Jose, Costa Rica preview, not a destination or user location.
- No map ID or custom cloud style yet; validate basic authentication/rendering first. This is not a promise of the GTA appearance.
- Only Google's Android map services may use the key already saved in `app/android/local.properties`, and only after the user authorizes that test. Do not open Cloud Console sessions or change resources/restrictions.
- Do not display credential values, inspect the user's other credentials, or include keys in committed files, screenshots, build reports, or memory.
- Development APKs embed mobile credentials. Keep the artifact ignored and confirm API/application restrictions before distribution. No publication is authorized.

## Review Focus

1. Key missing/empty: native configuration must fail with an actionable message that contains no credential value.
2. Wrong key restriction/API enablement: native view creation alone is not successful map authentication; require visibly rendered tiles and inspect sanitized errors.
3. Native platform view in widget tests: tests cover app-owned UI, while actual platform rendering is verified separately; do not mock a successful Google response and call it integration proof.
4. Accidental guidance or location access: the probe must not initialize a navigation session, request device location or call destinations APIs.
5. Incompatible plugin/build tooling: require an Android debug build using the actual locked versions, not only analyzer success.

## Task 1: Configure the Android SDK boundary

**Files:** `app/pubspec.yaml`, `app/pubspec.lock`, `app/android/app/build.gradle.kts`, `app/android/app/src/main/AndroidManifest.xml`; document the configuration in `app/README.md`.

**Interfaces:** consumes existing local `MAPS_API_KEY`. Produces the manifest's `com.google.android.geo.API_KEY` metadata, with its value supplied by a Gradle manifest placeholder, never a literal in tracked XML. The actual key must not be emitted when checking the merged manifest.

- [ ] Establish the feature logbook/mirror and baseline commit before source edits. Keep current branch unless repository state requires isolation.
- [ ] Add the pinned official plugin and resolve/retain its lockfile. Do not upgrade unrelated tooling or introduce a second map plugin.
- [ ] Load local properties with Java `Properties`, preserving Flutter SDK settings. Require a nonblank `MAPS_API_KEY` with the error `Set MAPS_API_KEY in android/local.properties before building Android.` Do not include the value in that error.
- [ ] Supply the key through a manifest placeholder and add the required internet permission. Do not add location, background service or Android Auto permissions for this preview.
- [ ] Set minSdk to 24 and enable the core-library desugaring required by the published plugin instructions. Follow Kotlin DSL syntax and the documented compatible desugaring library rather than disabling compatibility checks. Execution ruling: native Navigation7.9.0 requires NIO desugaring2.1.5; use2.1.5 instead of the stale Flutter README example2.0.4.
- [ ] In isolated scratch configuration, test a missing/blank key and observe the sanitized failure. With the local key present, build must succeed. This build-configuration check replaces an artificial Dart RED for Gradle configuration.

## Task 2: Add explicit map preview

**Files:** `app/lib/app.dart`, new `app/lib/features/map_preview/map_preview_page.dart`, and `app/test/app_test.dart`.

**Interfaces:** `MapPreviewPage` is a `StatelessWidget` with `const MapPreviewPage({super.key})`. `GameMapsApp` keeps its current constructor. A home `FilledButton` labeled `Open map` pushes a `MaterialPageRoute<void>` containing the preview page. The preview uses `GoogleMapsMapView` inside bounded scaffold content, with the fixed initial camera above and no navigation-session initialization.

- [ ] Preserve the existing bootstrap test. Add a widget test named `offers a map preview without starting guidance`: pump the real app, assert an enabled `FilledButton` labeled `Open map`, retain both original labels and assert no `Start navigation` control.
- [ ] Run `flutter test test/app_test.dart --reporter expanded`; observe the new test fail because the action does not yet exist. Record observed RED.
- [ ] Implement the entry action and preview page using the SDK's bare map view; avoid a fake map, fabricated loaded state or extra state-management package.
- [ ] Run the focused tests and observe GREEN. Widget tests intentionally do not authenticate Google or prove native platform rendering; the live tap/preview/back journey belongs to Task 3.
- [ ] Normalize authored Dart with `dart format lib test` before final tests/review freeze.

## Task 3: Verify the native preview and preserve evidence

**Files:** update `docs/06-development-environment.md`, the feature logbook, `app/README.md`, and the roadmap's observed status only. Build artifacts/evidence stay ignored under `app/build/`.

- [ ] Run `flutter analyze`, the full `flutter test` suite, and `flutter build apk --debug`; report failures, do not claim iOS compatibility from Android success.
- [ ] Obtain one independent verifier for risk items 2/5 (credential wiring and dependencies/build setup). Verification uses scratch state outside the worktree and a non-credential placeholder for build-only checks. Do not copy the real key into verifier scratch or make unsolicited Google requests. Live authentication/rendering belongs to the parent-authorized emulator test only.
- [ ] Before the live test, confirm user authorization covers Google map loads from the Android emulator using this app's saved key. If not, stop that test only; do not infer authorization from the key's existence.
- [ ] Install the debug APK on the observed authorized emulator; tap `Open map` once, confirm visible real map tiles/Google attribution, capture non-secret evidence, then navigate back and confirm the setup screen remains intact. Do not repeatedly retry authentication failure or silently remove Cloud restrictions.
- [ ] If the key fails, preserve a sanitized error and request the exact required configuration decision; never echo the key from native logs or the merged manifest.
- [ ] Record locked versions, observed test/build/rendering outcomes, any SDK side effects and estimated request scope. Observe usage reporting when the user can inspect it; absence of immediate billing metrics is not proof of zero consumption.
- [ ] Stop the app and task-owned emulator, clean worker scratch, and commit the coherent preview unit only after required checks and the applicable native review lifecycle. No push or publication.

## Acceptance and next boundary

Success means a user-triggered Android map preview actually renders and returns to the home screen, with no navigation/location workflow added. Do not mark San Andreas styling, Android Auto or the overall P1 phase complete. After basic authentication/rendering, separately plan the phone/car styling probe using its actual API constraints and the user's accepted visual target.

## References

- [Published plugin and requirements](https://pub.dev/packages/google_navigation_flutter/versions/0.11.2)
- [Google Flutter plugin documentation](https://github.com/googlemaps/flutter-navigation-sdk)
- [Example local-key configuration](https://github.com/googlemaps/flutter-navigation-sdk/blob/main/example/README.md)

## Execution rulings

The first dummy-key Android build failed AAR metadata validation because native Navigation7.9.0 requires desugaring2.1.5 or newer. Use the native SDK requirement2.1.5, not the older plugin README example. The SDK also contributes location/background/notification/boot/wake capabilities to the merged manifest; remove those permissions with Android manifest-merger markers for this preview-only unit. Their later use requires the separately planned navigation permission/session work.

The single targeted recheck resolved desugaring/merged-permission scope but failed on a new incompatibility: lifecycle2.0.35 requires compileSdk36 while the pinned Google plugin compiles35. No preview APK or Google authentication proof exists. Stop automatic correction/build attempts and request a scoped dependency-version decision; do not patch downloaded package caches or disable AAR checks.
