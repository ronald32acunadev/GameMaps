# Implementation phases

**Status:** roadmap approved by the user on 2026-10-21; not an execution-ready task plan.
**Current position:** the [Android bootstrap plan](superpowers/plans/2026-10-21-android-bootstrap.md) is approved and executing. Toolchain, shell tests, Android debug APK and Android emulator launch pass; native review is approved and acknowledged; the local bootstrap is committed as `e2ddf18`. P0 is not complete. The bare map preview now builds and renders real roads on Samsung; Back and reopening pass. Preview review/commit remain pending; themed/car feasibility is not complete.

The sequence validates the highest-uncertainty part first: a styled Google navigation map on the car display. Do not build the complete app before proving that premise.

| Phase | Outcome | Depends on | Status |
|---|---|---|---|
| P0 | Development and Cloud prerequisites ready | Document review | In progress; Cloud readiness unverified |
| P1 | Google styling and Android Auto feasibility demonstrated | P0 | Not started |
| P2 | Functional Android phone navigation MVP | P1 visual acceptance | Not started |
| P3 | Validated Android Auto MVP | P2 | Not started |
| P4 | Validated iPhone and CarPlay counterpart | P1 findings; Mac access | Not started; Mac unavailable |
| P5 | Extensible theme selection | Stable navigation; initial theme accepted | Future |
| P6 | Distribution readiness for the chosen audience | Applicable platform validation | Future |

## P0 — Prepare the development environment

**Covers:** S4, S7, S8, S9.

Work:
- Review this documentation before source implementation.
- Inspect installed Flutter, Android tooling, and test-device availability.
- Select compatible supported Flutter/plugin/SDK versions from current documentation; record the versions actually installed.
- Create the Flutter project and actual app identifiers once its implementation design is approved.
- Prepare the dedicated Cloud project, billing, restricted Android credentials, and monitoring using the [setup guide](04-google-cloud-setup.md).

Deliverable: an Android debug app that builds and launches, with no credentials in tracked source.

Exit gate: observe toolchain diagnostics and a device/emulator launch. Identify missing head-unit access explicitly rather than declaring the car platform ready.

## P1 — Prove the styled-car-map premise

**Covers:** S2, S5, S6, S7.

Work:
- Display real geography in Costa Rica using Google Navigation.
- Create a candidate San Andreas-inspired style and the appropriate map configuration.
- Test that style in the phone map and the Android Auto map separately; passing the phone map's configuration does not prove the projected map uses it.
- Use a fixed test destination and simulated position updates to demonstrate route guidance on the car surface.
- Capture screenshots, configuration, device details, limitations, and observed usage metrics.
- Compare the result with the user's reference and request acceptance of the achievable appearance.

Deliverable: a bounded technical prototype and a feasibility result: supported, limited, or unsupported.

Exit gate: verify projected styling, legible route guidance, and actual SDK/API behavior. A Desktop Head Unit result is preliminary; actual car testing remains required. If the appearance is inadequate, stop dependent visual work and obtain a provider/scope decision. Do not silently replace Google Maps.

## P2 — Build the Android phone navigation MVP

**Covers:** S1, S2, S6, S7, S8.

Work:
- Implement location permission and location-service states.
- Add destination entry and the selected search integration, enabling only the service it needs.
- Support route preview, start/stop guidance, voice instructions, route deviation recovery, and arrival.
- Apply the accepted initial style without removing Google attribution.
- Present actionable failures for unavailable location, network, credentials, quota, and missing routes.

Deliverable: usable phone navigation for representative Costa Rican routes.

Exit gate: automated tests cover application-owned behavior; observed device tests cover Google navigation, voice, and route recovery. Do not assume the Google Maps consumer app's features all exist in our app.

## P3 — Complete Android Auto integration

**Covers:** S2, S5, S6.

Work:
- Build the native car service and approved navigation-template interactions around the P1 result.
- Keep one active route/session synchronized between phone and car.
- Verify connect, disconnect, reconnect, background behavior, audio, and day/night readability.
- Test on an actual head unit while parked or with a passenger operating the app.

Deliverable: the Android/Android Auto milestone described in the product brief.

Exit gate: the [validation matrix](05-validation-and-release.md) has observed Android and Android Auto evidence. Simulator-only proof must remain labeled preliminary.

## P4 — Implement and validate iPhone and CarPlay

**Covers:** S4, S5.

Work:
- Obtain authorized access to macOS/Xcode, signing, and suitable test devices.
- Configure an iOS-specific key and app identifier rather than weakening Android key restrictions.
- Implement the iOS navigation adapter and native CarPlay scene/templates.
- Obtain the CarPlay entitlement needed for the intended development/distribution path.
- Verify theme fidelity, guidance, connection lifecycle, and audio independently on iPhone and CarPlay.

Deliverable: a verified iOS/CarPlay counterpart, with documented platform differences.

Exit gate: successful signed build and observed phone/car tests. Shared Flutter code alone is not proof. Cloud compilation alone is not proof of CarPlay behavior. No remote build service or credentials may be used without explicit authorization.

## P5 — Extend the theme system

**Covers:** S3.

Work:
- Turn the validated first style into a theme definition that separates appearance from routing.
- Add a phone-based selector and persist the selection; avoid introducing distracting car interactions.
- Validate any required map-view recreation or session handling before promising seamless switching.
- Add further original game-inspired themes only after their visual scope is agreed.

Deliverable: tested theme selection without changing the destination or navigation outcome.

Exit gate: switching supported themes preserves route/session invariants or follows an explicitly accepted safe transition. Do not assume a Google map ID can be replaced on an existing view.

## P6 — Prepare the selected distribution path

**Covers:** S1, S4, S5 and the agreed audience.

Work:
- Decide personal testing versus public release before store-specific work.
- Check attribution, asset provenance, location disclosure, signing, API restrictions, and measured usage.
- Complete platform review and distribution requirements applicable to each target.
- Test release builds; keep unverified platforms visibly pending.

Deliverable: release-ready artifacts for the approved audience, not an automatic publication.

Exit gate: required evidence exists and the user explicitly authorizes any upload or publication.

## Execution discipline

After reviewing the product brief and proposed architecture, create a detailed plan for P0/P1 only. Later plans must use their actual findings rather than invent SDK interfaces now. Use test-first development for runnable application-owned behavior; use observed structural/device checks for setup and native SDK integration. Never mark an expected test result as an observed pass.

No dates or effort estimates are committed. Hardware access, Google styling limitations, billing setup, and Apple approval can change the sequence or elapsed time.
