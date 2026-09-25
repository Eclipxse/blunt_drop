# Blunt

Blunt is an Android shopping MVP built with **Kotlin**, **Jetpack Compose**, and **Material 3**.

It demonstrates a complete local commerce experience including authentication, product discovery, cart and checkout, order tracking, wishlist, account-specific data, and a one-unit **Daily Deal** system.

> **Note:** This repository is currently an MVP / prototype. Payments, authentication, inventory, pricing, notifications, and Daily Deal reservations are simulated or stored locally and are not intended for commercial deployment.

---

## Overview

Blunt provides a complete shopping flow with:

- User registration and login
- Product browsing
- Categories
- Search
- Product details
- Product variants
- Wishlist
- Shopping cart
- Address management
- Checkout
- Mock payments
- Order confirmation
- Order tracking
- Ratings
- Notifications
- Daily Deal reservations
- Dark theme
- Responsive layouts

The architecture is designed so the current local data layer can later be replaced with a real backend/API without rebuilding the UI.

---

## Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **StateFlow**
- **Coroutines**
- **Repository Pattern**
- **Local JSON Persistence**
- **Gradle 9.6**
- **Android Gradle Plugin 9.4.1**

Minimum supported Android version:

Android 8.0 / API 26+

---

## Getting Started

Clone the repository and open the project in Android Studio.

Allow Gradle to finish syncing.

Select the `app` run configuration, choose an emulator or connected Android device, and run the application.

---

## Demo Account

A demo account is included:

Email: demo@example.com  
Password: password123

You can also create a new account directly from the registration screen.

New accounts are stored locally on the device.

---

## Main User Flow

Launch  
↓  
Onboarding  
↓  
Register / Login  
↓  
Home  
↓  
Browse Products  
↓  
Product Details  
↓  
Add to Cart  
↓  
Cart  
↓  
Delivery Address  
↓  
Checkout  
↓  
Payment Selection  
↓  
Order Confirmation  
↓  
Order Tracking

Users can also access:

- Categories
- Search
- Wishlist
- Orders
- Notifications
- Profile
- Daily Deal

---

## Daily Deal

Blunt includes a special **Daily Deal** system built around a single promotional product.

Example:

Normal Price: ₹4,490  
Daily Deal Price: ₹99  
Available Stock: 1

The user flow is:

Upcoming Deal  
↓  
Deal Opens  
↓  
User Claims Deal  
↓  
10-Minute Reservation  
↓  
Dedicated Checkout  
↓  
Order Confirmation

For demonstration purposes, an upcoming deal can be activated using:

`Demo preview: start drop now`

Once successfully claimed, the item is reserved for **10 minutes**.

The current MVP includes:

- One-unit stock
- Duplicate-claim protection
- Reservation deadline
- Owner validation
- Dedicated checkout
- Sold-out state
- Claim persistence
- Idempotent checkout behavior

To run another demo:

Profile  
↓  
Start a new demo Daily Drop

Starting another demo replaces any unpaid reservation while keeping completed orders.

Logging in using another account shows the shared sold-out state while preserving each user's separate:

- Cart
- Addresses
- Wishlist
- Orders
- Search history

---

## Product Experience

The current build includes:

- 18 demo products
- 8 category entry points
- Promotional content
- Trending products
- Recommended products
- Best sellers
- Search
- Recent searches
- Sorting
- Price filters
- Rating filters
- Availability filters
- Product galleries
- Product specifications
- Color selection
- Size selection
- Quantity selection
- Wishlist functionality

All product photography is bundled locally so the MVP can work without network access.

---

## Search

Search supports matching by:

- Product name
- Brand
- Category

Users can also:

- View recent searches
- Sort products
- Filter by price
- Filter by rating
- Filter by availability

---

## Cart

The cart supports:

- Quantity updates
- Remove from cart
- Save for later
- Price calculations
- Discount calculations
- Delivery charge calculations
- Final total

---

## Address Management

Users can:

- Add addresses
- Edit addresses
- Delete addresses
- Select a delivery address

Address data is stored locally per user.

---

## Checkout

The checkout flow includes:

- Delivery address
- Product summary
- Price breakdown
- Discount
- Delivery fee
- Final amount
- Payment method selection

Available demo payment methods:

- UPI
- Credit / Debit Card
- Cash on Delivery

The application also contains a simulated payment failure state for testing.

> No financial details are collected and no real payments are processed.

---

## Orders

Orders are organized into:

- Active
- Delivered
- Cancelled

Each order contains:

- Order ID
- Product snapshot
- Purchase price
- Delivery address
- Payment method
- Order date
- Current status
- Tracking timeline

Example order states:

Order Confirmed  
↓  
Packed  
↓  
Shipped  
↓  
Out for Delivery  
↓  
Delivered

The demo includes a control to advance the order status.

Orders can be cancelled before shipment.

Delivered products can be rated locally.

---

## Wishlist

Users can:

- Add products to wishlist
- Remove products from wishlist
- Open wishlist products
- Add wishlist products to cart

Wishlist data is stored separately for each user.

---

## Notifications

The MVP includes contextual local notifications such as:

- Daily Drop begins soon
- Your order has shipped
- New offers are available
- Your Daily Deal reservation expires soon

These are currently local demo notifications.

---

## Local Persistence

Application state is stored inside the app's private local storage.

Persisted information includes:

- Accounts
- Authentication state
- Cart
- Wishlist
- Addresses
- Search history
- Notifications
- Orders
- Daily Deal state
- Onboarding completion

Account-specific data remains isolated between users.

The Daily Deal state belongs to the installation and is shared between local accounts.

---

## Security

Passwords are **not stored in plain text**.

The local MVP authentication system stores passwords using:

PBKDF2  
+  
Unique Salt  
+  
Password Hash

This protects passwords within the local prototype environment.

However, local authentication must **not** be used for production.

A production version should use secure server-side authentication and authorization.

---

## Architecture

The application follows a layered architecture:

UI  
↓  
ViewModel  
↓  
Repository  
↓  
Data Source

---

## Project Structure

### `model/`

Contains immutable application models and typed state.

### `repository/`

Defines replaceable repository interfaces.

Main contracts are located in:

`repository/Contracts.kt`

Local implementations use:

`Fake*Repository`

These repositories handle validation and business operations.

### `data/`

Contains local persistence logic.

`LocalStore.kt` writes application state using a versioned JSON snapshot.

Updates are:

- Serialized using a coroutine mutex
- Written atomically
- Published through `StateFlow`
- Exposed only after persistence succeeds

### `viewmodel/`

Contains:

- Screen state
- UI events
- Business orchestration
- Repository interaction

### `navigation/`

Contains:

- Navigation routes
- Screen destinations
- Navigation logic

### `ui/`

Contains:

- Compose screens
- Reusable UI components
- Layouts
- States
- Animations

### `MainActivity`

Contains application setup only.

Business logic is intentionally kept outside `MainActivity`.

---

## UI / UX Features

The MVP includes:

- Material 3
- Dark theme
- System font scaling
- 48dp touch targets
- Loading skeletons
- Empty states
- Error states
- Android share sheet
- Responsive navigation
- Navigation rail on wider screens
- Product image galleries
- Order progress timeline
- Daily Deal countdown
- Reservation countdown

---

## Build

The project was developed using Android Studio's bundled JBR.

On Windows:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'

.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:testDebugUnitTest