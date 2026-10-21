# Validation and release gates

**Status:** bootstrap shell tests, Android APK and Android emulator launch passed on 2026-10-21. All navigation/car checks below remain pending; the shell is not a GPS implementation.

## Evidence rules

For each run, record the date, app revision, device/head-unit model, OS, SDK/plugin versions, test route, expected outcome, actual outcome, and sanitized screenshots/logs. Keep exact home addresses and personal trip history out of public evidence.

Use `Pass`, `Fail`, `Blocked`, or `Not run`; distinguish emulator/simulator evidence from physical-device evidence. Never mark iOS or CarPlay passed because Android or shared Dart tests pass.

## Required validation matrix

| Scenario | Android phone | Android Auto | iPhone | CarPlay |
|---|---|---|---|---|
| Real map and accepted visual style | Pending | Pending | Blocked: no Mac | Blocked: no Mac |
| Destination selection / supported handoff | Pending | Pending | Blocked | Blocked |
| Route, location, heading and maneuvers agree | Pending | Pending | Blocked | Blocked |
| Voice guidance and audio behavior | Pending | Pending | Blocked | Blocked |
| Off-route recovery and arrival | Pending | Pending | Blocked | Blocked |
| Location permission denied / location unavailable | Pending | Pending | Blocked | Blocked |
| Network / route / credential / quota failure | Pending | Pending | Blocked | Blocked |
| Connect, disconnect, reconnect and background | Pending | Pending | Blocked | Blocked |
| Day/night route and instruction readability | Pending | Pending | Blocked | Blocked |

Destination entry and controls may differ by platform; test the supported workflow rather than requiring identical phone and car controls.

## Costa Rica test context

Select representative routes with the user rather than inventing exact personal destinations: an urban route, an interurban route, and a route containing locally relevant destination-search input. Compare outcomes against real geography and observed SDK guidance. Do not assume every feature of the consumer Google Maps app is available in the SDK.

Start with simulated movement and parked tests. A passenger should operate the app during any subsequent road test; do not require the driver to manipulate the prototype while moving.

## Automated checks

Once source exists, use focused unit/widget tests for application-owned permission states, destination flow, trip lifecycle, errors, and theme configuration. Observe a failing test before implementing a deterministic behavior where meaningful. Mock tests cannot establish real route quality, projected styling, audio behavior, or platform approval.

Planned baseline checks after Flutter scaffolding: `flutter analyze` and `flutter test`. These commands have not been run and do not replace device tests. Add native build/test commands to the phase plan once the selected toolchain is known.

## Release gate

- [ ] Relevant matrix rows have observed results; blocked targets remain clearly labeled.
- [ ] Release signing and API restrictions match the distributed app.
- [ ] Google attribution and required notices remain visible/available.
- [ ] Original visual assets have documented provenance.
- [ ] Location permissions, background behavior and privacy disclosures match actual behavior.
- [ ] Measured usage and cost controls are acceptable.
- [ ] Required Android Auto / CarPlay review and entitlements are satisfied for the selected distribution path.
- [ ] The user has explicitly authorized the specific upload/publication.

Store approval is an external outcome, not something development completion can guarantee. Additional themes and public distribution must not conceal an unfinished core navigation or car-platform validation gate.

## Bootstrap evidence

See the [environment report](06-development-environment.md) for observed commands, the independent verification result, failed/recovered launch attempts and the APK hash. No rows of the navigation/car matrix are passed by the bootstrap.
