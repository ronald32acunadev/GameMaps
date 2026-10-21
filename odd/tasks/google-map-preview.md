# Android Google map preview
Objective: execute the approved map-preview plan on `feat/android-bootstrap`; delivery: ask-on-risk, no remote publication. Baseline: `1b0c1fe`.
Runner: Flutter analyzer/tests, Android debug build and one authorized emulator map-load journey. Forecast: approximately 250 authored changed lines, generated lockfile excluded.

## Specs

S1. User answered "si" to approval of `docs/superpowers/plans/2026-10-21-google-map-preview.md` and to: "¿Aprobás el plan y autorizás una prueba en el emulador usando la clave guardada para cargar mapas de Google, con posible consumo facturable en tu proyecto?"
S2. Implement that plan's explicit `Open map` action and real Android `Map preview`; preserve `GameMaps`, `Navigation setup pending`, and no `Start navigation` action. Camera reference: approximate San Jose, `LatLng(9.9281, -84.0907)`, zoom 12.
S3. Use the existing ignored `MAPS_API_KEY` only for the authorized Google map-load test on the task emulator. No routes, navigation session, device-location access, Cloud changes, credential disclosure/transfer, car integration, Apple native changes, or publication.
S4. Observe widget RED/GREEN, analyzer, suite, Android debug build, independent risk-2/5 verification with dummy scratch credentials, and actual map tiles plus return-to-home evidence. Authentication failures become a sanitized configuration blocker, not repeated retries or weakened restrictions.

S5. User answered "Si+" to "¿Autorizás ajustar las versiones de dependencias para resolverlo, manteniendo Flutter y Google Maps?". User then requested: "Ya tengo conectado mi telfono samsung puedes probar con el". Test only GameMaps on the connected Samsung using the existing authorized ADB connection; no personal-data inspection or device-setting changes.

## Tasks

- T1 (S1,S3,S4): inline; Android dependency, desugaring and native key boundary; verified; dependency pin, key probes, merged permissions and dummy/real-key builds pass; commit09217de. Risk: items 2/5. Configuration checks use isolated Gradle probes rather than artificial Dart RED.
- T2 (S1-S4): inline; explicit preview action/page with widget RED/GREEN; implemented; focused and full suite2pass, analyzer clean; commit09217de. Same coherent feature unit as T1.
- T3 (S1,S3,S4): inline coordination plus one independent odd-verify worker for items 2/5; functional proof verified on Samsung; real tiles, Back and reopen pass; native review/commit09217de; commit09217de. No parallel writer trigger: sequential, coupled behavior.

## Log

L1 (2026-10-21). User's authorization: "si". Read the approved product brief, actual plan and full memory observation before implementation. Existing bootstrap commits preserved; reuse suitable checkout/feature branch.
L2. API enablement is user-reported. Local key presence and Git exclusion were verified without emitting its value; no live request has occurred. Real key must not be copied to worker scratch; parent live test only.
L3. Scope is a real bare-map preview, not navigation or GTA styling. P1 remains incomplete until the separately planned theme/car feasibility work.

L4. Added/pinned google_navigation_flutter0.11.2; resolved4 new packages, no unrelated upgrades. Inspected its cached Kotlin DSL: native Navigation7.9.0, AGP9 handling, and attribution-only startup initializer. Bare map requires an onViewCreated callback and named LatLng(latitude, longitude); implementation follows the actual API, not positional shorthand in the brief.
L5. Native boundary uses built-in java.util.Properties to avoid another Gradle plugin dependency, maps key through a manifest placeholder, minSdk24 and documented NIO desugaring2.0.4. No literal key or location permission added to app sources. Plugin's dormant unexported service comes from its own manifest; no navigation session is initialized by app code.
L6. Widget RED observed: original bootstrap test passed, new preview test failed because no Open map FilledButton existed. Implemented MapPreviewPage and explicit home button only after RED. Formatting0changes; focused2tests, analyzer and suite results pending final command completion.
L7. One plan-status update initially used an app-relative cwd and failed without changing the document; corrected via absolute root path. No credential or user file was modified.

L8. Parent checks passed: focused widget tests2/2, analyzer no issues and full suite2/2. Independent verifier started with exact skills and read-only workspace scope, fresh /tmp scratch and dummy credential only. Existing task emulator launched headlessly with output redirected to a task-owned temporary log; no live map has been opened.

L9. Independent first pass: analyzer/tests2/format passed; baseline suite1passed; missing and blank key Gradle probes failed with required sanitized message. APK build failed checkDebugAarMetadata: Navigation7.9.0 requires desugar>=2.1.5, but the published Flutter README example used2.0.4. Build implicitly installed Android Platform35rev2 with an already accepted license.
L10. Ruling/correction batch: use desugar_jdk_libs_nio2.1.5, verified against actual metadata failure and Google's native setup documentation, without bypassing checks or upgrading other tooling. Remove SDK-inherited fine/coarse/background location, foreground/location-service, notification, boot and wake permissions via manifest-merger removal markers so this preview cannot acquire those capabilities. Dormant unexported SDK helper service declarations do not themselves establish runtime location access. Targeted recheck will verify native build and actual merged permissions, not launch a new full review.

L11. Targeted recheck resolved desugaring and verified the actual merged manifest: all8 targeted permissions absent, minSdk24 and dummy API-key metadata present. A separate blocker then failed :google_navigation_flutter:checkDebugAarMetadata: transitive flutter_plugin_android_lifecycle2.0.35 requires compileSdk>=36, while google_navigation_flutter0.11.2 hardcodes35. No APK produced.
L12. Correction boundary reached on a new finding: no second automatic correction/build loop, no pub-cache patch, forced metadata suppression or unverified dependency downgrade. Needs user decision: authorize a separately scoped dependency-compatibility adjustment while retaining Flutter/Google Maps, preferring supported published versions.
L13. Preserved dummy-only logs/merged manifest in ignored app/build/map-preview-verification/; removed /tmp/gamemaps-verify-x0VnS3 and task emulator log, shut down task-owned emulator. Real key not copied, built into a new artifact, or used for a Google request. Existing bootstrap APK is not a map-preview artifact. No commits/push/native review for this incomplete feature.

L14 (2026-10-21). User authorized dependency compatibility adjustment with "Si+" and requested testing on the connected Samsung. ADB lists one authorized SM_A175F. No install or map request yet.
L15. Published lifecycle2.0.26 declares compileSdk35 and AndroidX annotation1.9.1, within Google plugin declared ^2.0.16. Trial an explicit app pin rather than cache patches or disabling AAR validation. Tradeoff: older lifecycle support plugin until Google plugin supports the current Android compile SDK. Existing native failure supplies RED; independent dummy build must prove compatibility before device install.

L16. Independent fresh scratch dummy-key build passed with lifecycle2.0.26. Actual merged manifest passed minSdk24/dummy-key/eight-permission absence assertions. Parent analyzer and widget suite2/2 passed. Parent real-key build now running for authorized connected-Samsung test; live proof remains pending.

L17. Explicit lifecycle2.0.26 pin passed independent dummy-key native build and merged permission assertions. Parent analyzer, two widget tests and real-key debug build passed. Samsung SM-A175F install returned Success and launch intent succeeded. Package-filtered UI found no GameMaps nodes; stopped without touching unrelated UI or tapping Open map. Await user opening GameMaps for real tile/back proof. No review/commit/push yet.

L18. User reported black map, then: "Actualize el api key por favor recompila a ver si funciona" and "ya esta habilitado" after reconnect request. Rebuild exited0 with ignored updated key; no key disclosure. Samsung install Success/launch succeeded. Screenshot shows real San Jose/Heredia streets and labels, unlike prior black view. No source or Cloud-setting change by agent in this test; exact prior cause not proven. App-bar Back restored GameMaps/Navigation setup pending/Open map; explicit Open map reopened preview. Functional proof passes; native review/commit remain pending, no push. Ignored screenshot/log names samsung-updated-key-map.png and samsung-updated-key-diagnostic.log.

L19. Source commit09217de closes the verified preview unit. Fresh format0changes/analyzer clean/tests2pass. User selected review option1; four native immutable review lenses had no findings; exact acknowledgement returned action acknowledged/authority burned for review-5fcd2bd1bc56495f. No push. The next approved map-first/search feature replaces the temporary launcher intentionally.
