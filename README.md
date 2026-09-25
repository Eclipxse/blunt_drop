# Blunt — Android shopping MVP

A complete local shopping prototype built with Kotlin, Jetpack Compose and Material 3. Open this directory in Android Studio, let Gradle sync, select the **app** configuration and run on an emulator or device (Android 8 / API 26 or newer).

## Try it

**Demo account:** `demo@example.com` / `password123`.

Or create a local account from the login screen. After registration, log in with the new credentials.

1. Browse Home, Categories or Search. Search recognizes names, brands and categories.
2. Open a product, select its color and size, adjust the quantity and add it to your bag.
3. Open the bag, then checkout. Add a sample delivery address, choose a mock payment method and place the order.
4. Track the order in My Orders. The labeled demo control advances its delivery status; cancellation works before shipment, and rating works after delivery.
5. Open Daily Deal. For an upcoming drop, use **Demo preview: start drop now**. Confirm a claim to reserve the one unit for 10 minutes, then complete its separate ₹99 checkout.

Use **Profile → Start a new demo Daily Drop** for another run. This replaces any unpaid reservation while keeping existing orders. Login as another account to see that the claimed drop is sold out, while their bag and addresses remain separate.

## Included

- Three-page onboarding, returning-session startup, registration validation, email/phone login and account help.
- 18 products, eight category entry points, promotional edit, trending recommendations and best sellers.
- Search and recent searches, price/rating sorting, availability/price/rating filters, wishlist, gallery with full/detail views, product options and quantities.
- Cart editing, save for later, address create/edit/delete/select, price breakdown, three mock payment choices, simulated payment failure and order confirmation.
- Active/delivered/cancelled order lists, tracking timeline, cancellation, local ratings and contextual notifications.
- Shared one-unit Daily Drop claim state, persisted reservation deadline, duplicate-claim protection, owner checks and idempotent checkout.
- Dark scheme, system font scaling, 48dp controls, image-loading skeletons, empty/error states, Android share sheet and navigation rail on wider screens.
- All photography is bundled for offline use. No network permission, payment credentials or analytics SDK.

## Structure

`model/` holds typed immutable state. `repository/Contracts.kt` defines replaceable interfaces. `Fake*Repository` implementations own validation and business operations. `data/LocalStore.kt` writes a versioned JSON snapshot atomically in private app storage; updates are serialized with a coroutine mutex and published through StateFlow only after persistence succeeds. `viewmodel/` exposes screen state and events; `navigation/` and `ui/` handle rendering and navigation. MainActivity contains only app setup.

The app stores a salted PBKDF2 password hash, not raw passwords. Cart, addresses, favorites, searches, notifications and orders belong to each account. Onboarding and the current drop belong to the installation. Orders retain a snapshot of the purchased product, price and shipping address.

## Build and verify on Windows

Use Android Studio's bundled JBR (the original project uses AGP 9.4.1 / Gradle 9.6).

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:lintDebug :app:testDebugUnitTest
```

The installable debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Run the Android UI tests on a **clean demo installation**; the journey creates sample accounts and orders. Use a dedicated test emulator rather than clearing an installation containing data you want to keep.

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

See `VERIFICATION.md` for the recorded results and `docs/ASSET-SOURCES.md` for image origins.

## Prototype boundaries

Offers, prices, reviews, inventory, addresses entered for testing, delivery estimates and payments are demonstration data. Stock locking is atomic within this installation only. Reservation time uses the device wall clock; a production server must own time, identity, authorization, inventory and expiry. Local demo payment choices never collect or charge financial details. The UI identifies photography as illustrative: it may differ from the named SKU, variant or specification.

A future API should implement atomic inventory reservations, server-side authentication, idempotency keys, payment-provider confirmation/webhooks, reconciliation, verified product assets and legal/support policies. Replace the repository implementations through AppContainer; do not rely on this local authentication or local stock state for a commercial launch. Backend integration, real notifications, real payments and physical-device performance profiling are outside this MVP.
