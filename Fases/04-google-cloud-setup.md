# Google Cloud setup and cost controls

**Status:** instructions only; no project, billing link, key, budget, or quota has been verified as configured.

Prepare only the services required by the current phase. Cloud changes and any billable operation require the user's authorization; this document does not grant account access.

## Android prerequisites

1. Create or explicitly select a dedicated development project, such as `GameMaps Dev`.
2. Link a billing account and review the applicable Google Maps Platform terms and prices.
3. Enable `Navigation SDK` and `Maps SDK for Android` for the initial Android work.
4. Configure billing alerts and inspect the supported service quotas. A budget alert alone is not a hard spending cap.
5. Once the Flutter app exists, record its actual Android package name and signing-certificate SHA-1.
6. Create a development key restricted to that Android application and the required APIs. Do not invent identifiers or leave the key unrestricted for convenience.
7. Load credentials through an appropriate local build configuration excluded from version control. Do not paste keys in chat, screenshots, or documentation.

Use separate credentials for release signing and for iOS when those phases begin. Keep ordinary mobile-key exposure mitigated through application/API restrictions; storing a key outside source control alone does not prevent extraction from a distributed app.

## Style configuration

In P1, create an Android map ID and an associated candidate cloud map style. Record non-secret identifiers and screenshots as evidence. Verify the chosen style actually applies to the phone and projected navigation maps; platform-specific setup may be required.

Defer the iOS map configuration until it can be tested. Do not enable Places, Geocoding, or Routes API merely because the app navigates; the destination-search design will determine any additional service.

## Pricing baseline

Research date: **2026-10-21**. Values below are Google's global pay-as-you-go list in USD, before applicable taxes or negotiated terms. Recheck before enabling billing or release; these are not a guaranteed total bill.

| Usage | Monthly free allowance | Initial paid tier |
|---|---|---|
| Navigation Request | 1,000 destination events | $25 per 1,000 additional events |
| Dynamic Maps | 10,000 map loads | $7 per 1,000 additional loads |

Using a map ID with the mobile Maps or Navigation SDK can trigger Dynamic Maps billing. Do not describe all mobile styled maps as unlimited and free. Navigation is charged by destinations in route requests, not by every GPS update; automatic route changes for an already requested destination add no navigation charge. Additional services have their own billing rules.

Example: 2,000 destination events and 2,000 Dynamic Maps loads in a month would total $25 for these two services under otherwise unused allowances and the listed terms. Free allowances are not granted separately to every app user; account-wide usage matters.

## Readiness evidence

- [ ] Project selected and ownership confirmed.
- [ ] Billing and current price terms reviewed.
- [ ] Only required services enabled.
- [ ] Android application/API restrictions verified using the actual package and SHA-1.
- [ ] Key absent from tracked files and logs.
- [ ] Alerts and applicable quota behavior checked.
- [ ] Initial test usage observed in Cloud reporting; reporting delay acknowledged.

## References

- [Cloud and credential setup](https://developers.google.com/maps/documentation/navigation/android-sdk/get-api-key)
- [Flutter plugin requirements](https://github.com/googlemaps/flutter-navigation-sdk#requirements)
- [Global prices](https://developers.google.com/maps/billing-and-pricing/pricing)
- [SKU triggers](https://developers.google.com/maps/billing-and-pricing/sku-details)
- [Navigation billing](https://developers.google.com/maps/documentation/navigation/android-sdk/pricing)
- [Cloud budgets](https://docs.cloud.google.com/billing/docs/how-to/budgets)
