# CLAUDE.md

## Project: Cartharsis — a dopamine shopping simulator

A fake marketplace Android app (inspired by South Korea's "dopamine sites" trend).
Users browse a fake catalog, fill a cart, "check out" with no money, and watch a
simulated courier deliver nothing. See README.md for the full concept.

**Core invariant: nothing real ever happens.** No network calls, no real payments,
no accounts, no analytics. All data is local and fake. Any feature that would break
this invariant is out of scope by design.

## Build & test

The Android Studio JBR provides the JDK (no system java on PATH):

```bash
JAVA_HOME=/app/extra/jbr ./gradlew assembleDebug
JAVA_HOME=/app/extra/jbr ./gradlew test
JAVA_HOME=/app/extra/jbr ./gradlew lint
```

SDK location comes from `local.properties` (`/home/sascha/Android/Sdk`).

## Architecture

Single-activity Jetpack Compose app, MVVM, no DI framework.

```
app/src/main/java/com/example/myapplication/
├── MainActivity.kt          # NavHost + bottom bar, notification permission request
├── ShopViewModel.kt         # All app state: catalog, cart, wishlist, price drops,
│                            #   orders, delivery sim, stats (AndroidViewModel)
├── Notifier.kt              # Notification channel + delivery/price-drop pings
├── data/
│   ├── Models.kt            # Product, Review, CartItem, Order, OrderStatus,
│   │                        #   Product.withPriceOverride (fake price drops)
│   └── FakeCatalog.kt       # Hardcoded products, categories, review generator
└── ui/
    ├── theme/               # Vibrant "dopamine" palette (pink/purple/orange)
    └── screens/             # One file per screen + Common.kt shared pieces
        ├── HomeScreen.kt        # Flash deal banner, category chips, product grid
        ├── ProductDetailScreen.kt
        ├── WishlistScreen.kt    # Hearted items + price-drop badges
        ├── CartScreen.kt
        ├── CheckoutScreen.kt    # Fake payment → confetti success
        ├── TrackingScreen.kt    # Animated courier delivery simulation
        └── OrdersScreen.kt      # Order history + "money not spent" stats
```

- One `ShopViewModel` (an `AndroidViewModel`, for notification context) scoped to
  the activity, passed to screens; state exposed as `StateFlow`.
- The delivery simulation is a coroutine in the ViewModel that advances an order
  through `OrderStatus` stages on a timer; screens only render the state. Delivery
  completion and wishlist price drops post real notifications via `Notifier`
  (permission-guarded; channel `cartharsis.events`).
- Price drops are an overlay map (`priceDrops: id → cents`) applied at display/
  cart-add time via `Product.withPriceOverride` — the catalog itself is immutable.
  Cart lines snapshot the price at add time.
- Navigation Compose with plain string routes (`home`, `product/{id}`, `wishlist`,
  `cart`, `checkout`, `tracking/{orderId}`, `orders`).
- Persistence is intentionally in-memory for now (stats reset on process death);
  DataStore is a listed future task in tasks.md.

## Conventions

- Kotlin official code style; Compose-idiomatic (state hoisting, `Modifier` as the
  last default param, previews where cheap).
- Product "images" are emoji — no image loading library, keep it that way.
- All animations are hand-rolled Compose/Canvas (confetti, courier) — no animation deps.
- Versions live in `gradle/libs.versions.toml`; add dependencies there, not inline.
- Copy/tone: playful, self-aware, slightly absurd ("Imagination Express card").
  Keep fake product names obviously fake-adjacent; never imitate a real brand.

## Workflow

- tasks.md is the living task list — update checkboxes as work lands.
- Build with `assembleDebug` after meaningful changes; it's the main correctness gate.
