# Map-first Home and Place Search Implementation Plan

> **For agentic workers:** Use `superpowers:executing-plans` for native inline execution. Delegate only when repository routing triggers apply; credential, permission, dependency and asynchronous-result changes require scoped independent verification. Read this plan and its spec before work.

**Goal:** Launch directly into a location-aware map and select a real destination using Google's simplest native search UI.

**Architecture:** Keep the existing Google map integration and introduce a small Flutter home controller plus an Android platform bridge for foreground location and official Places Autocomplete (New). Inject the bridge and map surface into tests; no backend, web-service calls from Dart, routing session or custom prediction list.

**Tech Stack:** Current Flutter3.47.7/Dart3.13.5, Google Navigation Flutter0.11.2, lifecycle2.0.26 and Android minSdk24. Trial published Places Android4.3.1 in isolated compatibility checks before adding it to the product; do not assume newest6.x has the same interfaces.

**Spec:** [Approved design](../specs/2026-10-21-map-first-place-search-design.md), approved with "Si me parece correcto" on 2026-10-21.

**Status:** approved for native inline execution on 2026-10-21. T1-T3 source and automated/native checks pass; physical-device/live Places validation and work-unit closure remain pending. Checkboxes below are execution instructions; observed evidence is in the feature logbook.

## Global Constraints

- Android first. Preserve existing map provider, ignored native key and Google attribution.
- Map on launch, no intermediate `Open map`/setup page. App-owned strings remain English.
- Foreground location only, including approximate-location acceptance; no background service, history or location/query logging.
- Denied permission, disabled location and missing fix retain a usable map/search with an explicitly non-current San Jose reference.
- Official full-screen Places search, one active search, one selected destination, cancellation preserves prior selection.
- Search only on explicit interaction; no route calculation, guidance, themes, car integration or Apple native changes.
- No Cloud edits, public upload, secret transfer to scratch or blanket removal of key restrictions.
- A live Places test needs its own user authorization; existing map-load authorization does not cover it.

## Review Focus

1. Approximate-only permission is a usable grant, not denial: pin in Task2 and device verification.
2. Position completion after a destination was chosen must not recenter away from it: pin in Task2.
3. Search cancellation/failure preserves the previous destination and its marker: pin in Task3.
4. Duplicate search taps and activity disposal cannot produce overlapping native activities or late UI writes: pin in Tasks1/3.
5. Null/invalid coordinates or failed detail lookup must not become a destination at `(0,0)`: pin in Tasks1/3.

## File map and interfaces

| Surface | Responsibility |
|---|---|
| `app/lib/app.dart` | Direct map-home entry and production dependency assembly |
| `app/lib/features/map_home/map_home_page.dart` | Map, search/recenter controls, state/error and destination summary |
| `app/lib/features/map_home/map_home_controller.dart` | Screen state transitions and late-result guards |
| `app/lib/features/map_home/map_home_models.dart` | Immutable position, destination and result states |
| `app/lib/features/map_home/map_home_gateway.dart` | Platform contract and injected test seam |
| `app/lib/features/map_home/method_channel_map_home_gateway.dart` | MethodChannel transport and strict response parsing |
| `app/lib/features/map_home/google_map_surface.dart` | Existing SDK map/controller, camera, position indicator and single destination marker |
| `app/android/app/src/main/kotlin/com/example/gamemaps/MainActivity.kt` | Register/forward bridge and permission/activity callbacks |
| `.../gamemaps/maphome/MapHomeBridge.kt` | Channel lifecycle and one pending result per operation |
| `.../gamemaps/maphome/ForegroundLocationProvider.kt` | Android permission, service/fix and cleanup boundary |
| `.../gamemaps/maphome/PlacesSearchProvider.kt` | Official search activity, session token and minimal detail request |
| `app/android/app/build.gradle.kts`, `app/android/app/src/main/AndroidManifest.xml` | Verified SDK pin and foreground-only capabilities |
| `app/test/features/map_home/` and `app/android/app/src/test/kotlin/com/example/gamemaps/maphome/` | Fakes and tests beside the behavior they verify |

The `.../gamemaps/` entries above expand to the exact Kotlin root in the MainActivity row. No new broad architecture framework or state-management dependency.

Shared Dart contracts:

- `GeoPoint(double latitude, double longitude)`; reject non-finite/out-of-range values.
- `Destination(String placeId, String name, String? address, GeoPoint position)`.
- `LocationResult` with statuses `available`, `denied`, `disabled`, `unavailable`; `available` requires a position and an approximate/precise accuracy flag.
- `SearchResult` with statuses `selected`, `cancelled`, `failed`; `selected` requires a destination; `failed` carries only a sanitized category.
- `abstract interface class MapHomeGateway`: `Future<LocationResult> locate({required bool requestPermission})`, `Future<SearchResult> search({GeoPoint? bias})`.
- `MapHomeController(MapHomeGateway gateway) extends ChangeNotifier`: `Future<void> initialize()`, `Future<void> recenter()`, `Future<void> search()`, `void clearDestination()`, `void dispose()`; getters expose location, destination, pending/error states and intended camera target.
- `GameMapsApp` accepts optional injected gateway and map builder for tests. Defaults must always assemble the real production integration, never a fake success.
- `MapHomeMapState` carries optional current position, optional destination and camera target/revision. `MapSurfaceBuilder = Widget Function(BuildContext context, MapHomeMapState state)`. The real surface applies revisions when its native controller is ready; tests record states without creating a platform view.

## Task0 — Close the proven preview and verify dependency interfaces

**Files:** existing `odd/tasks/google-map-preview.md`, approved design, this plan, and implementation-time new `odd/tasks/map-first-place-search.md`. No product source write yet.

- [ ] Reconcile actual Git state and previous full Engram observation121/task file. Preserve unrelated `.codegraph/` and the user's ignored key. Do not re-run the old consumed bootstrap review.
- [ ] Close the already-proven preview using its applicable check/review/commit policy; handle any candidate consent before starting the next implementation. Keep this plan/spec separate from preview source staging.
- [ ] Create the new ODD feature document/mirror before the first product-source write. Quote the user's exact map-first/search/location approvals, record task routes and checks. Work inline; tasks share interfaces and do not justify parallel writers.
- [ ] Forecast approximately700 authored implementation/test lines, excluding generated output. Before the first next-feature work-unit commit, resolve the repository's ask-on-risk slicing decision once; no push/PR creation implied.
- [ ] In fresh outside-worktree scratch using dummy key only, inspect published Places4.3.1 artifact/docs and validate its graph against current Navigation/lifecycle/toolchain. Verify `PlaceAutocomplete`, token extraction, selected prediction, detail fields and `Places.initializeWithNewPlacesApiEnabled` against this exact version. Google's6.0.2 release notes remove older detail interfaces; do not substitute6.x into4.x examples.
- [ ] Verify the actual cached Google map controller's camera/marker/position-indicator methods before adapting it. No navigation initialization for location. Run scratch `flutter build apk --debug --no-pub` after the candidate dependency change; retain sanitized result and merged-manifest evidence. If incompatible, stop with the exact constraint; no cache patch or verification suppression.

No meaningful RED applies to this read-only compatibility step. No production dependency is selected as verified until this probe passes.

## Task1 — Open directly into the map home

**Files:** `app/lib/app.dart`; new map-home models/gateway/controller/page/map-surface files; `app/test/app_test.dart`; new controller/model/transport tests under `app/test/features/map_home/`. Retire the old preview page only after no production/test reference remains.

**Interfaces:** implement the shared contracts above. A cancelled/denied fake is test-only. Method channel name `gamemaps/map_home`, methods `locate` and `search`; no credential in arguments/results. Before native Tasks2/3 exist, unsupported calls become explicit unavailable/failed states, never success.

- [ ] Write RED tests `opens map home without launcher`, `renders search and recenter controls`, and `unavailable integration keeps map usable`. Assert one MaterialApp, a rendered injected map surface, no `Open map`, no `Navigation setup pending`, no `Map preview`, and no route/guidance control. Replace obsolete launcher assertions rather than preserving the temporary launcher.
- [ ] Write RED model/transport tests rejecting missing selected-place data, non-finite/out-of-range coordinates, unknown payload status and malformed platform replies; failures are sanitized and no fake `(0,0)` destination appears.
- [ ] Run `cd app && flutter test test/app_test.dart test/features/map_home/`; observe failing tests for missing requested behavior before implementation.
- [ ] Implement direct map entry, visible `Search places`/`Recenter` controls and real map surface around the existing SDK. Retain bounded map area and Google attribution. Controller/gateway injection must not change production behavior.
- [ ] Re-run the focused tests, then `flutter analyze`; expected all new tests pass and no analyzer issues. Normalize before functional/review freeze. Record actual results, not expectations.
- [ ] Update app documentation for direct startup and commit the coherent home behavior under the approved slicing policy, with tests alongside it. Risk: item3 (existing app entry changes) and item4 (late-result handling).

## Task2 — Foreground location and recenter

**Files:** map-home controller/gateway/transport/surface and tests; Kotlin MainActivity, MapHomeBridge, ForegroundLocationProvider and their tests; Android manifest and app Gradle test configuration only if required.

**Interfaces:** Dart `locate(requestPermission: true)` sends `{requestPermission: true}`. Native reply is `{status, latitude?, longitude?, approximate?}` with position fields only for `available`. No location broadcast/event stream or background storage. Use platform `LocationManager` to avoid adding a Flutter permission/location package or initializing guidance.

- [ ] RED tests: `approximate grant is available`; `denied permission preserves search`; `disabled service preserves search`; `location timeout clears pending state`; `recenter uses last valid foreground result`; `initial location does not replace selected destination camera`; `completion after dispose is ignored`. Assert view state and gateway calls, not just returned values.
- [ ] Implement native runtime permission checks and one grouped coarse/fine request when missing; accept coarse-only grants. Existing background/service/notification/boot/wake removal markers stay. Replace only coarse/fine removal markers with declarations. Do not auto-open Settings or grant OS consent via ADB.
- [ ] Use enabled platform providers with single-fix listener handling and a15-second timeout, unregistering listeners/timer on completion, pause/destroy or cancellation. Return `disabled` if services are off, `denied` if not permitted and `unavailable` on timeout/provider failure. Do not return a stale cached fix as a current position. Recheck permissions after returning from OS settings; no prompt loop on resume.
- [ ] Center once after the initial valid fix unless a destination is already selected; explicit Recenter may move back to the user. Enable the SDK position indicator only after permission grant. Foreground display only; this increment does not promise continuous tracking or route following.
- [ ] Native tests isolate provider/permission callbacks: coarse-only success, denial, timeout and lifecycle teardown each complete pending result at most once and remove listeners. Run `cd app/android && ./gradlew :app:testDebugUnitTest`; use dummy-key scratch, never copy original local.properties. If Android's test harness needs new test-only dependencies, pin/verify them in this task, no broad toolchain upgrades.
- [ ] Run focused Flutter tests and analyzer; independently verify actual merged manifest includes only intended foreground location permissions and no newly restored background/service permissions. Physical Samsung permission/service toggles require the human's native consent, not automated setting changes.
- [ ] Document observed/unverified states and commit coherent location behavior with its tests. Risk: items2/4 (foreground permission and asynchronous lifecycle).

## Task3 — Native Google Places search and selected destination

**Files:** Kotlin MapHomeBridge/PlacesSearchProvider/MainActivity; verified Places dependency in Gradle; Dart controller/transport/page/map-surface and tests; app/environment docs. No API-key file edits.

**Interfaces:** Dart `search(bias: GeoPoint?)` sends only optional bias latitude/longitude. Native result `{status: selected, placeId, name, address?, latitude, longitude}`, `{status: cancelled}` or `{status: failed, category}`. Categories are `unavailable`, `network`, `credentials`, `quota`, `resolution`; do not forward raw SDK exception messages.

- [ ] RED tests: `selects one destination marker and summary`; `cancel keeps prior destination`; `failed replacement keeps prior destination`; `clear removes only destination`; `duplicate taps launch one search`; `late result after disposal is ignored`; `invalid detail coordinates fail without replacement`. Tests assert gateway invocation count, camera/marker state and visible summary/error.
- [ ] Add only the Places version proven in Task0. Initialize it from existing native metadata; never pass the key through Dart or logs. Use the official new widget, not the legacy autocomplete UI or direct REST.
- [ ] Register activity-result handling compatible with the actual FlutterActivity host. Keep MainActivity thin; forward to one pending search result. Do not change the host superclass merely to copy an example. Clear pending handlers on destroy/cancel and reject overlapping launches.
- [ ] On explicit search, create one token, open native search with optional location bias and no country restriction, then use that same session for selected-place detail lookup. Request identity, display name, formatted address and coordinates only. Resolve the selected suggestion through the proven4.x contract; cancellation does not call detail lookup.
- [ ] Missing key/API permission, network/quota failure and invalid details return sanitized typed outcomes. Stop pending UI on every terminal outcome. Do not retry automatically or clear a previous valid destination on failure.
- [ ] Native fake tests cover activity cancellation, detail failure, same-session token propagation, minimal requested fields and single-completion guard; run the native test command from Task2. Run focused Flutter tests and analyzer, then independent scratch dummy-key Android build/manifest verification for items2/4/5.
- [ ] Update docs with user-owned Places API(New) enablement/key restriction instructions and commit the coherent search behavior under the approved slicing policy. No live Places request yet.

## Task4 — Verify the integrated phone journey

**Files:** feature task logbook, app/root documentation and environment report. Fixes may touch only the responsible task's surfaces after reproducing the issue.

- [ ] Before final checks, run `cd app && dart format lib test`, then `flutter analyze`, `flutter test`, native tests and dummy-key debug build. Keep source-mutating formatting before any review freeze. Expected results are exit0 with all applicable tests passing; a required failure is partial, not success.
- [ ] One scoped independent verifier covers the whole feature's named risks with fresh isolated dummy-key state. Give the verifier the full ODD feature document and baseline; do not send actual key/location/search data. Parent spot-check one reported command. Apply repository correction bounds rather than loop-until-clean.
- [ ] On the connected authorized Samsung, install/launch without touching other apps. Observe immediate map, user-controlled foreground permission, approximate/denied/service-off fallback and Recenter. Do not programmatically reset permissions, switch location services or accept the OS dialog. Ask the user for any needed native action.
- [ ] With Places configured by the user and separate explicit authorization, perform one bounded live query/selection for a public Costa Rican place; observe actual marker/name, cancel another search without changing it, then clear. No route request. Failure becomes one sanitized actionable blocker, not repeated requests or removed restrictions.
- [ ] Keep screenshots containing personal location private/ignored; redact all logs. Clean only task-owned scratch; preserve unrelated user files and ADB/device settings. Do not publish/push.
- [ ] Record every passed/failed/skipped check in the ODD document/mirror. Apply the user-owned native review switch and exact candidate consent lifecycle before reporting the implementation complete. A successful phone test alone does not complete car/theme/P1 or iPhone milestones.

## Implementation handoff

Recommended route: native inline execution in this chat because tasks share map/controller/bridge interfaces; use a scoped independent verifier for the named risk items rather than a writer/reviewer per small step. Review/consent policy and the user's control of Cloud/device actions remain unchanged.

The plan's SDK compatibility task is a real prerequisite, not a claim that Places4.3.1 already builds here. If it fails, preserve the working map and report the exact compatibility decision needed before more product writes.

## Source references

- [Official autocomplete widget and session flow](https://developers.google.com/maps/documentation/places/android-sdk/place-autocomplete)
- [Places version changes](https://developers.google.com/maps/documentation/places/android-sdk/release-notes)
- [Android approximate/precise runtime permissions](https://developer.android.com/develop/sensors-and-location/location/permissions/runtime)
