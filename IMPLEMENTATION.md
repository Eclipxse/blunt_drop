# Implementation plan

1. Establish a native Material 3 design system and a product-led storefront.
2. Add typed models and repository interfaces; persist local state in an atomic JSON file in private app storage. Keep user baskets, addresses and orders isolated by account; keep the one-unit drop shared on this installation.
3. Add screen ViewModels, validating authentication, address, variants, inventory and order operations in the data/domain layer.
4. Build onboarding/auth, discovery/search/details, cart/address/checkout, orders, drop and account utilities.
5. Compile and lint, test consequential state transitions, then install on an Android emulator and exercise the ordinary and Daily Drop purchase journeys and supporting destinations.

## Boundaries
No real money, shipping, emails, server-side identity or cross-device claim consistency. Device-local claim locking is a demo; a real API needs an atomic inventory reservation and idempotent checkout. No raw passwords are stored. All payment choices simulate payment without collecting financial information.

## Verification
Gradle assembleDebug, lintDebug, testDebugUnitTest, connectedDebugAndroidTest. Native screenshots for phone, dark mode, larger text and expanded layout. Record actual results and any unverified behavior in VERIFICATION.md.

## Rollback
The starting project is an Android Studio Hello Android scaffold without Git. Preserve wrapper/SDK configuration and existing project identity. New implementation lives in modular source files; runtime data can be reset in Android app storage or through a clearly labeled demonstration reset.
