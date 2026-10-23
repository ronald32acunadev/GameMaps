# Android Auto feasibility evidence

**Current result: partial.** The isolated retained-engine/host-channel probe passed on Samsung; the application is not yet advertised as an Android Auto navigation app. No car surface, route or style acceptance has been demonstrated.

## Source boundaries

The verified phone baseline is `ab21f2a`, following Flutter-home commit `f325713`. These commits do not contain car navigation. The approved [car design](superpowers/specs/2026-10-21-android-auto-feasibility-design.md) and [plan](superpowers/plans/2026-10-21-android-auto-feasibility.md) define the remaining gates.

The first runtime increment retains one engine in `GameMapsApplication`; the phone borrows it instead of owning its lifetime. A versioned engine channel mounts the existing phone UI when the phone attaches. Car-only startup does not mount that UI or initialize a navigator, request permissions or prepare a route.

## Bounded bootstrap probe

`BootstrapProbe` exists only in debug source. It refuses to run unless the installed package is `com.example.gamemaps.bootstrapprobe` and its metadata contains a dummy-prefixed API key. It checks headless engine identity and Dart host-event acknowledgements, including replacement/disconnection. It does **not** prove native navigation listener recovery, terms/permission handling, an Android Auto surface, background guidance or styling.

Build the probe in an isolated source copy with the dummy key and suffixed application ID. Do not replace the working GameMaps installation with a dummy-key APK. Native application/activity class names stay in `com.example.gamemaps`; `${applicationId}` sets the instrumentation target package. Never change the real key to run this check.

Only after explicit authorization naming the device and isolated package, install the probe APK and run:

```bash
adb -s <authorized-device> shell am instrument -w \
  com.example.gamemaps.bootstrapprobe/com.example.gamemaps.car.BootstrapProbe
```

Required output is `bootstrap=pass`; any `failed_step` is a blocker to investigate. Preserve the test's scope limitation even when it passes. The application must initially have no phone Activity; relaunching the probe over an existing attached phone UI is not a cold-start test. Do not touch unrelated apps or native permission dialogs.

## Proof ledger

| Scenario | Evidence | Status |
|---|---|---|
| Prior phone regression suite/build | Fresh dummy checks, 13 Flutter and 13 native tests, Android debug build | Passed before car edits |
| Engine ownership and versioned host policy | RED missing contracts observed; fresh independent analyzer, 17 Flutter/19 native tests and debug build pass; parent Flutter spot-check passes | Automated subset passed; runtime gate remains pending |
| Headless Dart runtime roundtrip | Authorized isolated dummy-key instrumentation on Samsung: install success, `bootstrap=pass`, uninstall success; normal app installation unchanged | Passed for engine/host channel only, not an Android Auto surface |
| Navigation session setup | Authorized normal-app update succeeded; zero-destination preflight returned `unlock_phone` before opening Activity/terms/SDK | Pending unlocked Samsung; no destination allowance consumed |
| Phone Activity destroyed while native navigation is active | Requires accepted terms, eligible location service and bounded live SDK test | Pending; not replaced by engine-policy unit tests |
| Android Auto discovery/cold launch/surface | Car service/template not implemented in bootstrap increment | Pending |
| Shared demo route, voice, screen-off, reconnect and stop | Navigation owner and physical scenarios not implemented/run | Pending |
| Candidate style during guidance on phone/car | No Cloud map/style action or rendered candidate yet | Pending |
| User visual acceptance | No acceptance capture yet | Pending |

## Cost, permissions and delivery

Automated checks use dummy credentials. No live route/style request, Cloud mutation, extra runtime permission, location history or publication follows from this probe. Existing foreground-only permission exclusions remain until the navigation-service task demonstrates the required changes.

Current native review mode reads globally disabled; ordinary automated and independent risk checks still apply. No fabricated review approval or automatic mode change is recorded.

The independently built isolated APK is retained under ignored `app/build/android-auto-verification/bootstrap-isolated/app-debug.apk`; sanitized build/test/manifest evidence is in adjacent directories. The normal user APK is not replaced. Release-manifest verification confirms the debug instrumentation is excluded from release.

## Authorized Samsung bootstrap result

On 2026-10-21 the user answered "SI" to installing, running and removing the isolated dummy-key probe via ADB. APK package/key boundaries were checked before transfer. The probe passed with no phone Activity initially open, no process crash and no missing instrumentation. The isolated package was then removed successfully; the original GameMaps installation path remained unchanged. No live route, navigation terms/permission acceptance, car service or head-unit interaction occurred.

Sanitized machine evidence is retained in ignored `app/build/android-auto-verification/samsung-bootstrap-result.json`. The native Navigation progress/arrival-listener gate remains pending and must not be inferred from synthetic host events. Any next real-key navigation test needs separate bounded request/device-update consent.

## Zero-destination Navigation setup preflight

The user separately approved updating normal GameMaps and at most one fixed public San José demo route request with the existing key, with possible usage charges and no Cloud/key-restriction changes. Before consuming that allowance, a narrow debug-only gateway checks terms and session initialization, then cleans up. It exposes no destination/guidance/simulation method. The human accepts or declines SDK terms and native permission prompts.

`NavigationSetupProbe` requires `allow_setup=true`, the normal package and an unlocked phone. Its Dart endpoint exists only in debug mode. The existing foreground-only manifest is unchanged. Invoke only under the recorded device consent:

```bash
adb -s <authorized-device> shell am instrument -w -e allow_setup true \
  com.example.gamemaps/com.example.gamemaps.car.NavigationSetupProbe
```

Current physical result (2026-10-21): normal-app update `Success`; preflight `unlock_phone`, no crash and zero destination requests. It stopped before opening the phone Activity, SDK terms or a navigator. Unlock the Samsung before resuming this already-authorized check. Do not reinterpret the locked-phone state as an API/key failure or an authorization success. The single destination allowance remains unused. Evidence: ignored `app/build/android-auto-verification/samsung-navigation-setup-result.json`.

Automated source checks passed after observed RED: 22 Flutter tests, 19 native tests, analyzer, dummy debug build and release-manifest checks. Both instrumentation runners are absent from release. A separately authorized local real-key build succeeded; the real credential was neither copied into verification scratch nor printed.
