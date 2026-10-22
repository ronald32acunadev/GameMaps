# Android Auto Feasibility Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans for native inline execution, or superpowers:subagent-driven-development only if that execution method is explicitly selected. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** prove car-first launch, one shared simulated route, and the achievable game-inspired map appearance on Samsung/Android Auto before declaring P1 supported.

**Architecture:** retain one application-owned Flutter engine for phone and car, reuse the pinned Auto screen helper, and keep one Dart navigation coordinator independent of the phone widget lifecycle. A startup/listener-recovery compatibility gate precedes the complete demo; missing physical evidence is a blocker, not a successful build.

**Tech Stack:** Flutter, `google_navigation_flutter 0.11.2`, Android Navigation `7.9.0`, Car App/Projected `1.7.0`, existing Places `4.3.1` with standalone Maps excluded; Kotlin/JUnit and Flutter tests.

**Spec:** [Approved design](../specs/2026-10-21-android-auto-feasibility-design.md). Read it and `odd/tasks/android-auto-feasibility.md` before execution.

## Global Constraints

- Android only. Preserve the existing phone map/location/Places behavior, ignored credentials and `.codegraph/`; no cache patches, dependency upgrades, remote publication or provider replacement.
- The prototype uses one fixed public Costa Rican route and visibly labeled debug simulation; no real-key request on launch. Car destination search belongs to the later complete flow.
- One engine, one navigation coordinator, one route; never create a second engine just to display the phone UI.
- Human accepts SDK terms/OS permission. No automatic dialog acceptance, Cloud changes, background-location permission, boot-started tracking or location history.
- Car/native code does not send API credentials through Dart. No raw keys, personal positions, place queries or SDK exception messages in diagnostics.
- Dummy credentials for automatic/independent builds. Any physical or billable navigation test needs its own bounded authorization; terms acceptance is not billing-test consent.
- Keep the current checkout and approved local feature-branch-chain delivery strategy. Source changes are sequential inline units; independent permission/lifecycle checks and long builds use the required bounded verifier. No writer-parallelism reason exists.

## Review Focus

1. Car creates its surface before a phone Activity exists: one engine/registry and an explicit readiness result, never a blank screen labeled ready (Task 1).
2. Phone Activity is destroyed while car remains connected: progress/arrival listeners recover once without a second route (Tasks 1–3).
3. Start is pending when stop/disconnect/reconnect occurs: stale completion cannot start guidance or overwrite stopped state (Task 2).
4. Phone is locked or location permission is revoked: eligible service/setup failure is visible, with no silent success or automatic retry loop (Task 3).
5. Style changes or is rejected during guidance: preserve route, attribution, maneuvers and readable fallback; do not claim visual fidelity from a return value (Task 4).

## Execution conventions and proof

Run Flutter commands from `app/`: `flutter test`, `flutter analyze`. Run native commands from `app/android/`: `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Run `flutter build apk --debug` only after source normalization; an existing ignored real-key build is not independent dummy proof.

Before copying a workspace for checks, enumerate tracked and task-owned untracked source, exclude `.codegraph`, `local.properties`, build output and credentials, then create a fresh dummy `local.properties` with the installed SDK/Flutter paths. Use a home-directory worktree if CodeGraph is needed; ordinary dummy verification scratch can use `mktemp -d` outside the workspace. Workers report scratch paths; parent cleans them after retaining sanitized evidence.

Observe RED before each runnable app-owned behavior change, then GREEN and focused/full checks. Native SDK rendering/OS behavior cannot be proved by fake unit tests: record that exception and use separately authorized physical scenarios. Normalize before native review freezes bytes. Functional checks, applicable independent verification, native review consent and ordinary commit policy are separate.

## File and interface map

Paths below are repository-relative. Create only the files owned by the active task; do not scaffold downstream units.

| Surface | Responsibility |
|---|---|
| `app/android/app/src/main/kotlin/com/example/gamemaps/car/CarEngineOwner.kt` | Pure idempotent ownership policy; injected factory produces one retained engine. |
| `app/android/app/src/main/kotlin/com/example/gamemaps/car/GameMapsApplication.kt` | Lazily construct/register/start the actual shared engine once; no route request in Application startup. |
| `app/android/app/src/main/kotlin/com/example/gamemaps/car/CarHostRegistry.kt` | Pure versioned host identity/replacement policy and stale-detach guard. |
| `app/android/app/src/main/kotlin/com/example/gamemaps/car/CarNavigationBridge.kt` | Engine-scoped host/readiness and demo command/state channel; no Activity-owned navigator. |
| `app/android/app/src/main/kotlin/com/example/gamemaps/car/GameMapsCarService.kt` | Certificate-based host validation and session creation. |
| `app/android/app/src/main/kotlin/com/example/gamemaps/car/GameMapsCarScreen.kt` | Pinned Auto helper, templates and surface lifecycle, not route ownership. |
| `app/lib/features/car_navigation/{car_navigation_gateway.dart,car_navigation_coordinator.dart,car_navigation_models.dart,google_car_navigation_gateway.dart,car_navigation_bridge.dart}` | SDK seam, serial state, DTOs, pinned SDK adapter and engine-level channel binding. |
| `app/lib/features/car_navigation/{demo_route.dart,car_navigation_setup.dart,car_navigation_phone_surface.dart}` | Fixed public fixture, explicit parked setup/status and phone presentation of the shared navigation session. |
| `app/lib/features/car_navigation/car_map_style.dart`, `app/assets/map_styles/san_andreas_inspired.json` | Original style configuration, no route effects. |

Reuse `GeoPoint`, existing destination/location validation and `MapHomeBridge`; do not duplicate foreground location or Places session logic. `MainActivity.kt`, `app/lib/main.dart`, `app/lib/app.dart`, `map_home_page.dart` and `google_map_surface.dart` are the integration seams. Tests mirror the native `car/` and Dart `features/car_navigation/` paths. Existing phone tests remain regression checks.

### Task 0: establish a recoverable phone baseline

**Owns:** existing `odd/tasks/map-first-place-search.md`, associated phone source/tests/docs and coherent local work-unit boundaries; no car runtime edits.

- [x] Read the actual phone task and full mirror153, preserve their updated user-reported search evidence, then observe analyzer/Flutter/native checks for the current source. Do not rerun billable Places queries.
- [x] Stage only coherent phone behavior with its tests/docs, excluding `.codegraph`, local properties and build artifacts. Record each source boundary and applicable independent/native review outcome under the existing approved strategy; never reopen burned preview authority.
- [x] Keep remaining physical phone scenarios explicitly pending if unavailable. Record a committed source baseline before mixing car edits into those same runtime files; do not claim the prior feature is fully accepted merely to start this work.

### Task 1: gate car-first engine and listener compatibility

**Owns:** `CarEngineOwner.kt`, `CarHostRegistry.kt`, `GameMapsApplication.kt`, `CarNavigationBridge.kt`, `MainActivity.kt`, Activity adapter teardown in `MapHomeBridge.kt`, `app/lib/main.dart`, `car_navigation_bootstrap.dart`, and debug-only `BootstrapProbe.kt`/instrumentation declaration; tests `car/CarEngineOwnerTest.kt` and `features/car_navigation/car_bootstrap_test.dart`. Any runtime probe is dummy-only and isolated until this gate is accepted.

**Interfaces:** `CarEngineOwner<T>(create: () -> T).getOrCreate(): T`; `GameMapsApplication.requireEngine(): FlutterEngine`. MainActivity implements `provideFlutterEngine(context)` and `shouldDestroyEngineWithHost() = false`. Engine channel `gamemaps/car_navigation` exposes `getHostState` and `hostChanged`, with `{revision, phoneAttached, carAttached}`; accept only increasing revisions. Install handlers before reading initial host state.

- [ ] RED: `same_engine_for_car_then_phone`, `same_engine_for_phone_then_car`, `factory_and_dart_entry_execute_once`, `stale_host_snapshot_does_not_override_event`. Assert identical engine identity, factory/entry count1 and latest host revision. Run focused native/Dart tests and observe missing contracts fail.
- [ ] Implement lazy Application ownership with normal once-only plugin registration. Preserve per-Activity `MapHomeBridge` permission/result forwarding and dispose only that adapter on Activity teardown, not the retained engine. No duplicate engine or generated-plugin registration. Initialize the engine-level Dart coordinator without automatically requesting location, terms or routes when only the car connects; mount phone UI only for a phone host and reconcile attachment with current state.
- [ ] Add GREEN recovery probes for `activity_detach_does_not_lose_car_progress`, `reattach_registers_once`, `missing_activity_never_opens_terms`. The pinned plugin unregisters listeners on non-configuration detach: explicitly reinitialize the already-created session and recover default/custom subscriptions without restarting guidance. Do not infer this from engine retention alone.
- [ ] Compile the pinned helper/engine APIs with a dummy key, then request a bounded no-real-key Android runtime probe of car-first startup, headless message roundtrip, phone attach/detach and engine identity. A fake engine/JVM PASS or APK compilation alone does not satisfy this runtime gate. Do not install/use a real-key build by implication.
- [ ] **Gate:** proceed only after observed startup/listener roundtrip evidence. If device proof is unavailable or listener recovery is unsupported, report the exact missing proof and preserve state; no full car demo, SDK patch or silent native-only rewrite follows automatically.

### Task 2: one explicit simulated trip lifecycle

**Owns:** Dart car-navigation gateway/models/coordinator/Google adapter/demo fixture and corresponding tests. No new car search implementation.

**Interfaces:** `CarNavigationCoordinator.initialize()`, `prepareDemo()`, `startDemo()`, `stop()`, `reconcileListeners()`, `dispose()`, plus immutable `CarNavigationState` (`idle`, `needsSetup`, `preparing`, `ready`, `guiding`, `arrived`, `failed`; revision, remaining seconds/meters, sanitized failure). `CarNavigationGateway` provides `prepare(DemoRoute)`, `start()`, `stopAndClear()`, `restoreListeners()`, `Stream<CarNavigationUpdate> updates`. Native/phone views consume state, not a second gateway instance.

- [ ] RED: `prepare_requires_explicit_action`, `start_only_after_successful_route`, `stop_during_prepare_rejects_late_success`, `duplicate_start_is_single_guidance`, `arrival_and_stop_clear_simulation`, `reconnect_does_not_recalculate`, and `failed_request_is_not_ready`. Assert state, call counts and no guidance after failed/stale work. Run focused Flutter tests.
- [ ] Add fixed fixtures: Parque La Sabana center (9.9355,-84.1048) to Teatro Nacional (9.9339,-84.0779), marked synthetic/public test coordinates, not the user's position. Travel by road; unsupported fixture access is a visible route error, never a fabricated polyline. Use simulator origin only in debug demo mode.
- [ ] Implement serial/generation-aware transitions. On explicit prepare, check accepted terms/location eligibility, initialize one session with `TaskRemovedBehavior.quitService`, set simulator origin, then `setDestinations(Destinations(waypoints: [...], displayOptions: NavigationDisplayOptions()))`. Only a successful route status enables start. Map failures to fixed categories, no raw SDK exceptions.
- [ ] Start guidance/audio and simulated motion only on explicit start. Use pinned simulator `simulateLocationsAlongExistingRouteWithOptions(SimulationOptions(speedMultiplier: 1))`; stop through `removeUserLocation()`, `stopGuidance()` and `clearDestinations()`. Cleanup on explicit owner shutdown, not every display disposal. Release subscriptions exactly once; keep stop cleanup running even if one step fails, and report incomplete cleanup.
- [ ] Read distance/time via pinned `getCurrentTimeAndDistance` and progress events; bind arrival/NavInfo streams using the exact installed signatures. Add `stop_cleanup_failure_remains_visible` and `recovered_listener_delivers_once`. GREEN focused/full Flutter tests and analyzer; test old map/location/search paths without any navigation instance.

### Task 3: safe car template, setup and background lifecycle

**Owns:** native service/screen/bridge integration, `MainActivity.kt`, Android manifest, new `res/xml/automotive_app_desc.xml`, `res/values/car_app.xml`, `car_navigation_setup.dart`, `car_navigation_phone_surface.dart`, phone app/home integration and corresponding native/Dart tests. Only if compile visibility requires it, add explicit Car App/Projected `1.7.0` declarations already present in the plugin graph; no version change.

**Interfaces:** channel methods `prepareDemo`, `startDemo`, `stopDemo`, `publishState`, carrying only state revision/status/remaining metrics/fixed error category. Native screen emits commands; Dart owner invokes gateway once and publishes immutable state. `GameMapsCarService.createHostValidator()` uses `HostValidator.Builder(...).addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample).build()`; no allow-all branch.

- [ ] RED: `car_connect_does_not_prepare_route`, `blocked_setup_never_starts_trip`, `surface_destroy_does_not_stop_trip`, `explicit_stop_updates_phone_and_car`, `stale_state_revision_is_ignored`, `location_revoked_does_not_report_guidance`. Run focused policy/bridge tests with SDK calls replaced by fakes.
- [ ] Declare the CarAppService/template metadata and required navigation-template/surface permissions using pinned Car App interfaces. Preserve superclass callbacks in `AndroidAutoBaseScreen`. Use a setup pane until prerequisites are satisfied, then a `NavigationTemplate` with clearly labeled demo prepare/start/stop controls, supported maneuvers and meters/time; never project the phone Places Activity or obscure attribution.
- [ ] Present `GoogleMapsNavigationView` in `car_navigation_phone_surface.dart` during the explicit demo, using the same application coordinator/session; do not create a second session from that widget's init/dispose. Keep `GoogleMapsMapView` as the existing idle map. Test `phone_and_car_show_same_demo` and `phone_surface_disposal_keeps_car_session` before wiring the screen; setup/dismissal must not silently prepare a route.
- [ ] Keep the SDK's non-exported `com.google.android.libraries.geo.navcore.service.base.NavigationService`, type `location`, distinct from plugin `GoogleMapsNavigationNavUpdatesService`. Restore only justified FGS/location-FGS/notification capabilities; retain removal of background-location and boot permissions. Do not restore wake-lock permission without documenting its requirement from SDK behavior/merged manifest; prove screen-off behavior rather than guessing.
- [ ] Explicit parked phone setup presents terms, foreground precise/approximate choice and applicable notification consent; user handles OS dialogs. Initialize eligible navigation from an allowed context. If starting solely from locked/background car context is ineligible, show setup-required and report that car-first limitation; do not bypass Android restrictions or add background permission automatically.
- [ ] Test stop/task removal/reconnect, current phone bridge disposal and listener recovery. Inspect merged permissions/service types/exports and certificate host validation in dummy build; run native/full Flutter checks. Independent verification covers risks2/4/5. Only an authorized physical run can prove screen-off/voice/host acceptance.

### Task 4: compare original map styling on both surfaces

**Owns:** `car_map_style.dart`, original JSON asset/pubspec asset declaration, phone map style application and native car map options; style tests and `docs/07-android-auto-feasibility.md` evidence structure.

**Interfaces:** `CarMapStyle` supplies optional Android map ID plus local candidate JSON; the coordinator's destination/route state is not mutable through styling. `GameMapsCarScreen.getAutoMapOptions()` provides the map ID before surface creation. Never insert API credentials in the map configuration.

- [ ] RED: `style_does_not_change_route_or_guidance`, `rejected_style_keeps_baseline_map`, `car_options_ready_before_surface`, `debug_simulation_unavailable_in_release`. Assert unchanged route identity/call count and visible fallback. Use a distinct variant only for debug testing; the production default remains safe.
- [ ] Author an original black/gray/white/muted-green local style with distinguishable route/position and no copied game assets. Configure the same candidate intention separately on phone/car with pinned `setMapStyle(String?)`/initial map options; a method return is not visual proof.
- [ ] If guidance fidelity requires a Cloud map ID, stop for the specific user-owned map/style configuration and bounded cost/request consent. Do not silently create a project/style, relax key restrictions or treat existing Places approval as navigation/map-ID request approval.
- [ ] GREEN structural/style tests and regression checks. Capture baseline/candidate idle/guiding day/night evidence only on authorized app surfaces; record observed styling limits. Ask for visual acceptance before marking this task accepted. Runtime appearance cannot be tested through JSON assertions alone.

### Task 5: final proof and feasibility result

**Owns:** existing tracker, `docs/07-android-auto-feasibility.md`, `docs/02-implementation-phases.md`, `docs/06-development-environment.md`, app README and applicable regression probes discovered by verification.

- [ ] After final normalization run focused/full native and Flutter checks, analyzer and isolated dummy debug build once; collect observed exit codes and sanitized evidence. Independent verifier checks named permission/host, listener/concurrency and manifest/dependency risks on the whole spec; parent reruns one reported command. Bound corrections to the authorized batch/recheck policy.
- [ ] Before real-key navigation testing, obtain explicit consent naming fixed route/demo, destination request bound, device and styling-load scope. No repeated requests on reconnect. Forecast actual build/test time and billing side effects before launch; stop if expected boundaries change.
- [ ] Run authorized DHU checks if available, labeled preliminary; do not install new host tools without an explicit scope. Then test real Samsung/car while parked: discover, cold open, setup-denied, prepare/start, screen off, Activity destroyed, progress/voice/arrival, reconnect, stop, network failure and baseline/candidate style. Human owns dialogs. Unsupported/unavailable scenarios remain pending.
- [ ] Publish no remote artifact. Record supported/limited/unsupported per scenario, exact SDK/build evidence, user visual acceptance and costs only when measured. P1 closes only with required physical/style evidence; otherwise report one concrete blocked decision. Full car search and production navigation remain future P2/P3 work.
- [ ] Close coherent local work units with tests/docs and Conventional Commits, recording boundaries in the tracker; apply required native assessment/review to the exact source slice, not accumulated branch or task checkbox. Candidate consent is separate; no auto-grant, no push/PR. Preserve user work and cleanup task-owned scratch only.

## Handoff

The user approved this plan and native inline execution on 2026-10-21 ("dale"). The parent implements the coupled engine/channel/session tasks; delegate only mapped exploration, long checks and named-risk verification required by the harness. This approval does not grant device/live-request/Cloud consent.

Follow the startup gate and continue authorized in-scope steps without asking at every task boundary. If the gate fails, return its actual evidence and the specific decision needed.
