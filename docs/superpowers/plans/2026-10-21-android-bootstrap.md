# Android Bootstrap Implementation Plan

> **For agentic workers:** Use `superpowers:executing-plans` for direct inline execution under the repository routing rules. Steps use checkboxes for tracking. Delegate only when an applicable named trigger fires.

**Goal:** Create a tested Flutter application shell and establish an observed Android development baseline without making Google navigation requests.

**Architecture:** Keep the Flutter project in `app/` so generation cannot overwrite the approved root documentation. Use one application widget and one entry point; do not introduce routing, service layers, state-management libraries, or mock map screens yet.

**Tech Stack:** Flutter stable, Dart, generated native Android/iOS wrappers, Flutter's SDK-provided test tools. Actual Flutter/Gradle/JDK versions will be recorded after the toolchain is checked, rather than inferred from the Snap package version.

**Spec:** [Product brief](../../01-product-brief.md), S1, S4, S8 and S9; [P0 roadmap](../../02-implementation-phases.md); [approved architecture direction](../../03-architecture.md).

**Status:** approved for execution by the user on 2026-10-21 ("si dale comencemos"). This plan covers the local bootstrap portion of P0, not all P0 and not P1. The user approved the project direction with "LIsto perfecto comencemos"; this detailed plan had not yet been presented at that point.

## Global Constraints

- Android and iPhone are target platforms.
- The user has no Mac. iOS/CarPlay compilation and validation remain blocked until suitable access is available.
- A standalone navigation app, not a modification to Waze or Google Maps.
- Use Flutter as the proposed framework accepted in conversation.
- No app keys, cloud actions, additional Google services, navigation requests, or publication in this bootstrap unit.
- Generated source and UI copy use English. The temporary UI is explicitly a setup screen, not a simulated working GPS.
- `com.example.gamemaps` is a provisional development identifier only, not a publication identifier or a key restriction to create without checking actual generated output.
- Before source implementation, create the applicable ODD feature logbook and memory mirror, and establish a Git baseline/feature branch under the authorized work-unit policy. Preserve all existing documentation; no push or remote is required.

## Review Focus

1. Launcher exists but SDK is not initialized: Task 1 records real Flutter diagnostics instead of declaring readiness from PATH.
2. Java/Gradle incompatibility or missing Android tools: Task 1 records findings; Task 2 requires a real Android debug build.
3. Generated project overwrites documentation: Task 2 generates only inside new `app/` and verifies the root documentation is preserved.
4. Bootstrap screen misrepresents working navigation: Task 2 asserts that the screen says setup is pending and provides no start-navigation control.
5. Android build is mistaken for iOS/CarPlay success: Task 2 records platform-specific proof and leaves Apple targets unverified.

## Task 1: Establish the actual toolchain baseline

**Files:** update `docs/06-development-environment.md` only; SDK installations, if needed, are outside the repository and must be recorded.

**Interfaces:** consumes the observed inventory; produces actual Flutter/Dart versions, doctor findings, Android tooling paths, compatible Java selection, and the available test target. This is setup evidence, not a Dart API.

- [ ] Announce download/disk and system-change implications before an SDK initialization or installation. Do not uninstall existing tools, accept licenses for the user, or use a remote machine/session.
- [ ] Run `flutter --version` and `flutter doctor -v`. Record their exact outcomes, not the Snap package's version as the Flutter version.
- [ ] If Android tools are missing, follow the current official Android setup instructions. Stop only for a required native consent/license, unavailable install permission, or another concrete unresolved constraint; preserve progress.
- [ ] Select a compatible JDK based on the actual Flutter/Gradle diagnostics. Do not assume the observed Java 25 is either supported or broken, and do not change system-wide Java defaults as a shortcut.
- [ ] Run `flutter devices`. Record an authorized Android target or explicitly mark device launch blocked. A Linux/web target does not satisfy the Android launch gate.
- [ ] Update the inventory with actual versions, device availability and remaining blockers.

**Verification:** toolchain setup has no meaningful deterministic RED test. `flutter doctor -v` must report a usable Android toolchain before Android build readiness is claimed. Device readiness is a separate result. Cloud credentials are not required for this task.

## Task 2: Create a tested, honest application shell

**Files:** create generated project under `app/`; author `app/lib/main.dart`, `app/lib/app.dart`, and `app/test/app_test.dart`; update `README.md` with commands; preserve existing `docs/`.

**Interfaces:** `GameMapsApp` is a public `StatelessWidget` in `app/lib/app.dart` with a `const GameMapsApp({super.key})` constructor and `Widget build(BuildContext context)`. `void main()` in `app/lib/main.dart` launches `const GameMapsApp()`. No Google SDK integration is exposed yet.

- [ ] Confirm `app/` does not already exist; if it does, inspect and preserve its content instead of regenerating over it.
- [ ] Generate the project with `flutter create --empty --project-name gamemaps --org com.example --platforms=android,ios --no-pub app`. Platform wrappers may be generated without proving they build.
- [ ] Inspect generated files and configure repository ignores before fetching dependencies. Ignore keys, signing files, SDK/build caches and local settings without ignoring required manifests or lockfiles.
- [ ] In `app/`, run `flutter pub get` and retain the app lockfile. No third-party package is needed in this unit.
- [ ] Write the following test before implementing the application widget:

```dart
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gamemaps/app.dart';

void main() {
  testWidgets('shows an honest navigation setup screen', (tester) async {
    await tester.pumpWidget(const GameMapsApp());

    expect(find.text('GameMaps'), findsOneWidget);
    expect(find.text('Navigation setup pending'), findsOneWidget);
    expect(find.text('Start navigation'), findsNothing);
    expect(find.byType(MaterialApp), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
```

- [ ] Run `flutter test test/app_test.dart`; observe RED because the application widget is not implemented. Record the observed failure rather than claiming an expected failure occurred.
- [ ] Implement `GameMapsApp` as a Material application with a simple screen containing exactly the tested title and status; launch it from `main()`. Do not add a decorative fake map or the default counter demo.
- [ ] Run the focused test again; observe GREEN. Refactor only while it remains green.
- [ ] Normalize authored Dart files with `dart format lib test` before final verification.
- [ ] Run `flutter analyze`, `flutter test`, and `flutter build apk --debug` in `app/`; record each observed result. Missing Android build tools means partial verification, not a passing bootstrap.
- [ ] On an authorized Android target, run `flutter run -d <observed-device-id>` and confirm the same screen launches. Substitute the ID returned by Task 1, not an invented device ID; stop the app after the check.
- [ ] Review the diff for preserved root documentation, no secrets, no unexpected platform targets, and no unrelated tooling changes. Record iOS/CarPlay as not run.
- [ ] Update the README, environment inventory and feature logbook with observed commands/results. Commit the coherent work unit only under the repository's applicable authorized work-unit policy; do not push.

**Verification:** the focused widget test establishes RED/GREEN for the shell. The suite and debug build establish different evidence; a passing test suite alone is not an Android build or device launch. This adds a new app and does not change an existing application command, so there is no baseline application behavior to run differentially.

## Handoff to the rest of P0 and P1

Do not mark P0 complete after this shell. Next, confirm the Cloud project/billing/API setup, exact Android identifiers and key restrictions, then plan the Google integration and the bounded Android Auto style probe using the installed compatible SDK versions. Keep credentials out of the plan and tests. The detailed probe plan must name the real supported phone and car-view styling APIs rather than infer them from shared Flutter UI.

P1 must still demonstrate Costa Rican geography, accepted San Andreas-inspired styling on the car map, route/voice guidance and measured usage. P2-P6 remain deferred; generating iOS files does not remove the Mac blocker.

## References

- [Flutter Android setup](https://docs.flutter.dev/platform-integration/android/setup)
- [Google Navigation Flutter requirements](https://github.com/googlemaps/flutter-navigation-sdk#requirements)
- [Google Android Auto integration](https://developers.google.com/maps/documentation/navigation/android-sdk/android-auto)
