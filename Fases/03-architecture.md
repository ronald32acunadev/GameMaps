# Proposed architecture

**Status:** architectural direction approved by the user on 2026-10-21; concrete SDK integration remains subject to P1 feasibility evidence.

Use Flutter for the shared phone application and Google Navigation for maps, routing, and guidance. Add platform-specific Android Auto and CarPlay integrations; the car display is not a mirrored Flutter widget tree.

## Responsibilities

| Component | Owns | Does not own |
|---|---|---|
| Phone application | Destination workflow, status presentation, theme selection | Google route calculation |
| Navigation coordinator | App-level active-route lifecycle and state transitions | Map pixels or car templates |
| Google integration | SDK calls, events, map initialization, routing/guidance bridge | Product decisions |
| Theme configuration | Style identifiers and original visual assets | Routing or fake geography |
| Android Auto integration | Native car service, surface, templates, connection lifecycle | A second independent trip |
| CarPlay integration | Native scene, templates, connection lifecycle | Arbitrary game HUD rendering |

These are boundaries, not an instruction to create an interface or folder for every row. Choose concrete files and supported SDK calls in the P0/P1 implementation plan.

## State flow

Destination selection → navigation coordinator → Google route/session → observed state → phone and connected car presentation.

Theme selection → supported map initialization/styling configuration → phone/car maps. A theme change must not silently change the destination or create a duplicate navigation session.

## Invariants to prove

- At most one active app-level trip is presented across the phone and connected car.
- Guidance and displayed route refer to the same destination.
- Disconnect/reconnect does not accidentally start duplicate guidance.
- Permission denial, unavailable location, network failures, and quota failures do not appear as a successful trip.
- Appearance changes do not replace real road data or hide essential guidance and attribution.

## Platform constraints

- Both official cross-platform Google Navigation libraries were labeled Beta when researched on 2026-10-21. Pin versions only after confirming compatibility during setup.
- Google's documented Android Auto and CarPlay integrations were in Preview when researched; do not assume all phone APIs are supported on car displays.
- Car interaction remains subject to platform templates and review requirements.
- Flutter does not expand the styling capabilities of the native Google map renderer.
- A `mapId` on the Flutter navigation view is initialization-time configuration; investigate car-view configuration separately.
- Local iOS build and simulator validation require macOS/Xcode. No Mac is currently available.

## Deliberately deferred

No custom backend, authentication, analytics service, state-management package, remote build provider, or additional map provider is selected. Add a dependency only for a demonstrated requirement and record its tradeoff.

## References

- [Google Navigation for Flutter](https://github.com/googlemaps/flutter-navigation-sdk)
- [Android Auto integration](https://developers.google.com/maps/documentation/navigation/android-sdk/android-auto)
- [CarPlay integration](https://developers.google.com/maps/documentation/navigation/ios-sdk/carplay)
- [Flutter iOS setup](https://docs.flutter.dev/platform-integration/ios/setup)
