# Product brief

**Status:** direction approved by the user on 2026-10-21; implementation not started.

GameMaps provides ordinary GPS navigation over real geography, with a map appearance inspired by video games. The initial reference is the GTA San Andreas radar, adapted to the normal map area of a car display rather than requiring a circular viewport.

## Confirmed requirements

| ID | Requirement | User evidence |
|---|---|---|
| S1 | A standalone navigation app, not a modification to Waze or Google Maps. | "Si una propia" |
| S2 | Ordinary GPS navigation with a San Andreas-inspired map initially. | "seria como un gps normal pero con esa capa que se vea como los mapas de gta san andreas por el momento" |
| S3 | Additional game-inspired themes later. | "luego implementar mas" |
| S4 | Android and iPhone are target platforms. | "Posiblemente sea multiplataforma android y ihpone" |
| S5 | Display navigation through Android Auto and CarPlay. | "Mediante android auto y car playt" |
| S6 | Use Costa Rica as the initial real-world validation context, not an exclusive region. | "Soy de costa rica por cierto" |
| S7 | Start with Google Maps technology. | "Te parece si empezamos por la opcion de google maps?" |
| S8 | Use Flutter as the proposed framework accepted in conversation. | "Me parece bien dime si debemos hacer algo en google cloud console" |
| S9 | Document implementation phases before creating the app. | "OK bueno comencemos a crear el proyecto , pero antes creemos una serie de documentos con las fases de implementacion" |

## Initial navigation scope

Working interpretation of a normal GPS: choose a destination, calculate a route, follow location and heading, receive turn instructions and voice guidance, and recover when the driver leaves the route. These capabilities are the proposed MVP boundary, not evidence of implementation.

## Visual direction

- Real Costa Rican roads, not the fictional San Andreas territory.
- Black road shapes, white/gray blocks, muted green land areas, and high-contrast markers, following the supplied reference.
- A clearly distinguishable route and current-position indicator.
- Keep essential navigation information readable; do not reproduce unrelated game HUD elements.

Google styling may not reproduce the reference's building detail, geometry, icons, or colors exactly. Visual fidelity must be demonstrated on the projected car map before it is promised.

## Constraints and non-goals

- The user has no Mac. iOS/CarPlay compilation and validation remain blocked until suitable access is available.
- Android Auto and CarPlay have platform-owned interaction templates and approval requirements.
- A normal internet-connected navigation experience is assumed initially; offline routing is not promised.
- No Waze-style incident-reporting community, accounts, subscriptions, game mechanics, or backend is included in the initial proposal.
- No game artwork, fonts, music, or icons are assumed to be licensed for redistribution. Use original assets for the implementation.
- Existing reference imagery is design input, not an application asset.

## Open decisions

Resolve each only when its phase needs it:
- Acceptable visual fidelity after the Google styling experiment.
- App identifiers and signing configuration before API-key restrictions.
- Destination-search integration before enabling an additional Google service.
- Access to macOS/Xcode and the Apple development requirements before iOS validation.
- Personal testing versus public distribution before store preparation.

## Definition of the initial outcome

Android navigation works on a phone and an actual Android Auto head unit with the accepted map style and observed cost controls. This is an Android milestone, not completion of the Android/iPhone product. The full initial product additionally requires verified iPhone and CarPlay navigation.
