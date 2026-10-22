# Map-first home and destination search

**Status:** written design approved by the user on 2026-10-21 ("Si me parece correcto"); implementation plan approved for native inline execution. Android first; no implementation or Cloud changes authorized by this document alone.

Open directly on the real map, show the user's position with foreground permission, and use Google's native place-search interface to select a destination. This replaces the temporary preview launcher, not the map provider.

## User intent

- "no quiero que tenga presionar ver mapa sino que abra solo"
- "quiero que funcione como una especie de waze sabes buscar un lugar o como el mismo google maps"
- "Si es lo mejor" — confirms centering on current location with permission only while using the app.
- "Sii lo mas simple y parecido a google maps" — accepts Google Places as the simplest search approach after disclosure of additional billable consumption.

The user wants familiar interactions, not a promise to reproduce every Google Maps or Waze feature.

## This increment

1. Opening GameMaps immediately shows the existing Google map. Remove the setup-only home screen, `Open map` button and `Map preview` title.
2. A search control remains visible above the map; keep Google attribution and native map controls unobstructed.
3. Request foreground location permission through the operating system. With permission and a valid position, center the camera there and display the position indicator. Include a recenter action.
4. If permission is denied, location services are off, or a fix cannot be obtained, keep the map usable with the existing San Jose reference. Explain the state; do not imply that reference is the user's location. No automatic permission-prompt loop.
5. Tapping search opens Google's native Places Autocomplete (New) screen. Search is explicit, not triggered simply by opening the app. Use the device language in Google's UI and existing English conventions for app-owned strings.
6. Selecting a suggestion resolves its place identity and coordinates, centers the map on the destination, and displays one destination marker plus a compact name/address summary. Cancelling search preserves the previous selection; clearing removes it without moving the user's location.
7. Selecting a destination does not calculate a route or start guidance in this increment. Route preview, travel time and start/stop guidance follow as the next navigation increment.

Use available foreground position as search bias, not a country-only restriction. If unavailable, search must remain possible. Costa Rica is the initial validation context, not an exclusive supported country.

## Selected approach and tradeoff

Use the official native Places SDK for Android with its ready-made autocomplete activity, connected to Flutter through a narrow platform bridge. This reduces custom prediction-list, attribution and search-session handling compared with building a custom Flutter search interface.

The tradeoff is Google's full-screen search UI rather than a fully custom embedded search bar/list. A custom UI would provide more styling control but require additional integration and request-state handling; it is not needed for the user's simplest-first preference. No third-party Flutter Places package or custom server is selected.

Choose and pin a published native SDK version only after checking its compatibility with the current Android toolchain and Google Navigation dependency. Preserve working AAR checks; do not patch downloaded packages or silently replace the map provider.

## Boundaries

- **Flutter map home:** automatic map entry, visible controls, location/destination states and marker presentation.
- **Foreground location adapter:** OS permission/service/fix outcomes and recentering. It must not initialize turn-by-turn guidance merely to obtain a location.
- **Native Places adapter:** opens one search activity, resolves the selected place and returns a small structured result. Cancellation and failure are explicit outcomes, not a successful empty destination.

Keep the working native map integration. The destination model carries place ID, display name, optional address and coordinates, not API credentials. Prevent overlapping search launches and ignore completion after the screen is disposed. Preserve the last valid destination on a failed replacement search.

## Permission, key and cost boundary

- Request foreground location only. Support Android's approximate-location choice; no background location, background service or location-history persistence in this increment.
- Do not write coordinates, query text or keys to diagnostic logs.
- Keep Android credentials native and ignored by Git; do not expose the API key through the Flutter bridge or invoke Places web-service endpoints directly from Dart.
- Places Autocomplete (New) needs **Places API (New)** enabled and allowed on the Android-restricted key. Existing billing/map enablement does not establish this. User-owned Cloud changes require an explicit instruction; never remove package/SHA-1 restrictions as a workaround.
- Use one autocomplete session token through the selected-place detail resolution, following the actual pinned SDK contract. Request only the identity/display/address/coordinate fields needed for this UI. No photos, ratings or reviews.
- The user accepted the search service's additional consumption in principle. Obtain specific approval before a bounded real-key search test; use fakes/dummy credentials for automated and independent checks.

## Failure behavior

Permission denial or disabled location does not block destination search. Cancelled search is not an error. Network, credential, quota and place-resolution failures show an actionable non-sensitive message; no fake result, infinite spinner, repeated API retry or request loop. A failed search leaves the map and prior destination intact.

## Verification and acceptance

- Deterministic Flutter tests prove direct map entry without the old button, permission/fallback presentation, recenter intent, destination selection/clearing/cancellation and error outcomes. Use an injectable map/integration seam, not a fake success state in production.
- Run analyzer and the full test suite. Retain test-first evidence for app-owned behavior; native SDK rendering and OS dialogs require device proof, not fabricated widget assertions.
- Independent isolated dummy-key verification covers native dependency compatibility, foreground-only permissions and credential boundaries.
- On the authorized Samsung, observe cold launch, permission acceptance/denial, location-disabled behavior, recentering and search cancellation. After separately authorized live search, select a representative Costa Rican destination and confirm its actual marker/name. Keep personal-location evidence private and out of version control.
- Do not claim route, voice, car, iPhone or game-style support from these checks.

## Non-goals and sequencing

No routes, guidance, traffic/incident reports, accounts, backend, saved searches, background tracking, new theme, Android Auto or iOS/CarPlay implementation in this increment. Those remain in the roadmap.

Before implementation, reconcile the previous map-preview feature's pending review/commit and preserve unrelated workspace changes. This spec deliberately changes the previous preview's temporary launcher acceptance criteria; it does not invalidate its historical physical-map proof.

## References

- [Places Autocomplete (New)](https://developers.google.com/maps/documentation/places/android-sdk/place-autocomplete)
- [Places Android usage and billing](https://developers.google.com/maps/documentation/places/android-sdk/usage-and-billing)
- [Maps API security guidance](https://developers.google.com/maps/api-security-best-practices)
- [Implementation phases](../../02-implementation-phases.md)
- [Existing architecture](../../03-architecture.md)

## Next step

Review this written design, then create the implementation plan against the verified SDK interfaces. No product-source implementation has started for this increment.
