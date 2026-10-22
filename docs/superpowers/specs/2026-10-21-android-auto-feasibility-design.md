# Prove the Android Auto map and route experience

**Status:** written design approved by the user on 2026-10-21 ("OK me parece bien"). Android only. Concrete implementation plan review and execution-method selection remain pending; this approval does not authorize Cloud changes or live navigation requests.

Prove the project's main premise before building the complete phone GPS: connect the Samsung to Android Auto, open GameMaps on the car display, and display a real-road route with an original San Andreas-inspired appearance. Preserve the working phone map, foreground location and Places search.

## User intent and milestone boundary

- "No tengo iphone concentremonos por ahora en android y completemos todo lo faltante de esta fase 2"
- "Es decir el documento 02-implementation-phases.md"
- "Voy a conectar mi telefono por android auto y de ahi abrire el mapa que estamos construyendo y trasare la ruta"
- "Ese el objetivo principal del proyecto"
- "Listo continuemos"

The desired finished experience includes choosing a destination on the car. This P1 prototype does not claim that complete experience: the approved roadmap first calls for a fixed destination and simulated guidance to validate the car map and style. Full Android phone navigation and car destination search remain P2/P3 work after the feasibility result. iPhone/CarPlay are deferred, not prerequisites.

## Proposed approach

Reuse the pinned Google Navigation integration: Flutter plugin `0.11.2`, Android Navigation `7.9.0`. Add the car entry point around the plugin's existing Android Auto support. Do not upgrade dependencies or introduce a second route engine merely because current documentation has newer examples.

Keep one application-level navigation owner. The phone and car present the same destination, route and guidance session; reconnecting a display must not create another trip. The existing phone Places Activity remains a phone interaction, not a car search screen.

The alternative is a native-owned car surface using `NavigationViewForAuto`. It can avoid reliance on a phone Activity, but adds a different integration path. First probe the existing helper's cold-start. If it cannot support the required lifecycle, report that finding and revise the design before switching approaches.

## First gate: car-first startup

The installed `AndroidAutoBaseScreen` looks up the Flutter plugin's view/image registries when its surface is created. A declared car service alone does not prove this works without a phone Activity.

Before implementing the complete prototype, compile and test a dummy-key startup probe that establishes the engine/plugin/coordinator from the car service. It must not require the phone Activity to own or keep the navigator alive. Check both phone-first and car-first ordering, repeated connections and teardown. Compilation proves interface compatibility only; actual rendering still needs device evidence.

If cold-start cannot be demonstrated with this integration, stop dependent implementation and report the precise limitation. Do not patch cached packages, silently upgrade the SDK, or call a phone-first-only result complete.

## Prototype behavior

1. Android Auto discovers a debug GameMaps navigation app through its native car service, session and approved navigation template. Configure host validation; do not ship an unrestricted validator.
2. If terms or location consent are missing, show an actionable parked-setup state. The user accepts SDK terms and Android permissions on the phone. Do not accept dialogs or open setup automatically while driving.
3. Show the car map and a clearly labeled demonstration route to one public Costa Rican destination. The origin/destination will be fixed test fixtures, not the user's stored location. No route request is made merely because the app opens.
4. Explicitly start/stop a simulated trip, display route status and maneuvers, and prove that route and voice come from the same navigator. Simulation is debug-only, visibly labeled and cannot silently replace real-position guidance.
5. A car-surface disconnect releases that presentation without creating a second session. Explicit stop clears the demo route, simulation and guidance. Define task-removal behavior explicitly rather than inheriting the SDK's service-persistence default.
6. Compare baseline and candidate styling separately on phone and car, before and during guidance, including day/night readability. Preserve attribution and safety information.

## Lifecycle and permission boundary

Current source deliberately removes navigation-service capabilities because it only implements foreground phone location. Car display visibility is not proof the phone Activity is foreground.

The implementation plan must identify the SDK-owned foreground location service, its manifest requirements, eligible startup and notification behavior for the supported Android versions before changing permissions. Request only the capabilities that demonstrated navigation requires. Do not add background-location permission as a speculative workaround, allow boot-started tracking, or store location history.

When setup is incomplete or service startup is ineligible, return a clear blocked state rather than an apparently successful route. Test phone screen off, permission revocation, disconnect/reconnect and explicit stop. Existing phone location/search behavior must remain usable when no trip is running.

## Style and Cloud boundary

Use original colors and assets inspired by the supplied reference: black road shapes, white/gray built areas, muted green land, and legible route/current-position indicators. Do not copy game artwork or replace real geography.

Evaluate styling through supported Android map options on both surfaces. Local JSON may support an initial visual probe; a method returning success is not visual acceptance. If a Cloud-backed Android map ID is needed for guidance styling, stop for the user's specific Cloud configuration action and bounded request/cost consent. No remote Cloud changes, key restriction removal, new billing services or live requests follow merely from this design.

The achievable appearance may differ from the reference. Record supported/limited/unsupported behavior; get the user's visual acceptance before treating the style as ready for P2/P3. Do not silently change providers.

## Verification and acceptance

- Application-owned state transitions use runnable RED → GREEN tests, including cancel/stop, failure, stale callbacks and reconnection without duplicate guidance. Use fakes for routine tests; do not treat them as rendering proof.
- Dummy credentials and isolated checks establish pinned native/plugin compatibility, manifest/service composition and startup interfaces. Never copy the ignored real key into worker scratch.
- Run focused checks and the complete applicable Flutter/native suite, analyzer and debug build. Preserve regression coverage for the existing map, location and Places flow.
- Named permission/lifecycle/dependency risks require independent verification. Record failed, skipped and unavailable checks as such.
- A Desktop Head Unit result is preliminary. Actual Samsung/head-unit testing remains necessary, with the car parked or a passenger operating the application.
- Human device acceptance covers discoverability, car-first launch, fixed route rendering, simulated guidance/voice, screen-off behavior, reconnect, stop, denied location/network failure and style visibility during guidance.
- Obtain specific consent before billable route/style requests and any new device-operation scope. Keep raw keys, personal coordinates and query strings out of logs, chat and tracked artifacts.

Completion means an observed P1 feasibility result and accepted visual limits, not completion of Android Auto destination search, public distribution, P2 or P3. A blocker is a legitimate result; it must not be relabeled as support.

## Delivery and next step

Preserve and reconcile the existing uncommitted phone feature before adding new runtime behavior. Reuse the current checkout and the approved same-branch work-unit strategy; no push, pull request or publication is authorized. Keep tests/docs with each coherent behavior and isolate high-risk work units for their applicable checks.

Review this design, then prepare the concrete implementation plan against the pinned interfaces and choose its execution method. The first implementation unit is the cold-start compatibility gate, not the whole car app at once.

## Evidence and references

- Installed plugin: `android/src/main/kotlin/com/google/maps/flutter/navigation/AndroidAutoBaseScreen.kt`, especially the registry lookup in its map callback; pinned helper and current native examples must not be assumed identical.
- [Original roadmap](../../02-implementation-phases.md), especially P1 and its visual acceptance gate.
- [Shared navigation architecture](../../03-architecture.md).
- [Google Android Auto integration](https://developers.google.com/maps/documentation/navigation/android-sdk/android-auto).
- [Navigation map styling](https://developers.google.com/maps/documentation/navigation/android-sdk/customize-map-styles).
- [CarAppService lifecycle and location](https://developer.android.com/reference/androidx/car/app/CarAppService).
- [Android location foreground-service prerequisites](https://developer.android.com/develop/background-work/services/fgs/service-types#location).
