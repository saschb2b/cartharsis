# Cartharsis — Task List

Living task list. Checked = done and compiling.

## Phase 0 — Research & docs
- [x] Research the Korean "dopamine sites" / fake marketplace trend
- [x] Write README.md (concept, sources, features, stack)
- [x] Write CLAUDE.md (build commands, architecture, conventions)
- [x] Write tasks.md (this file)

## Phase 1 — Foundation
- [x] Add dependencies: navigation-compose, lifecycle-viewmodel-compose
- [x] Rebrand app: name "Cartharsis" in strings.xml
- [x] Dopamine theme: vibrant pink/purple/orange palette, dark + light
- [x] Data models: Product, Review, CartItem, Order, OrderStatus
- [x] FakeCatalog: 26 products across 6 categories, emoji art, fake reviews

## Phase 2 — Core state
- [x] ShopViewModel: catalog, cart ops (add/remove/qty), totals
- [x] Flash deal rotation + countdown ticker (90s per deal)
- [x] Order placement + delivery simulation coroutine (status stages over time)
- [x] Stats: orders placed, items bought, total money NOT spent

## Phase 3 — Screens
- [x] MainActivity: NavHost + bottom navigation (Home / Cart / Orders) with cart badge
- [x] HomeScreen: flash deal banner with countdown, category chips, product grid
- [x] ProductDetailScreen: emoji hero, rating stars, description, reviews, add to cart
- [x] CartScreen: line items, qty steppers, total, checkout CTA
- [x] CheckoutScreen: fake address + Imagination Express card, processing → success
- [x] Confetti animation on order success (hand-rolled Canvas particles)
- [x] TrackingScreen: status stepper + animated courier progress, delivered zen state
- [x] OrdersScreen: order history + savings stats

## Phase 4 — Dopamine polish
- [x] Haptic feedback on add-to-cart
- [x] Animated cart badge bounce when items added
- [x] Animated "money saved" counter

## Phase 5 — Verify
- [x] `gradlew assembleDebug` passes
- [x] `gradlew test` passes (unit tests for catalog, cart and order logic)

## Phase 6 — Notifications + wishlist fakery
- [x] POST_NOTIFICATIONS permission + channel + runtime request (Android 13+)
- [x] Notifier helper (delivery + price-drop notifications, permission-guarded)
- [x] Delivery notification when the courier sim completes ("Your nothing has been delivered")
- [x] Wishlist: heart toggle on product cards + detail screen, in-memory set
- [x] Wishlist tab with price-drop badges, add-to-cart, unwish
- [x] Price-drop engine: every ~25s a random wishlisted item drops 15–40% for 90s,
      with notification; dropped price flows into cards, detail, cart snapshots
- [x] Price-drop count badge on the Wishlist tab
- [x] Unit tests for withPriceOverride (anchor keeping, non-drops, idempotency)

## Phase 7 — Autopilot polish round
- [x] Catalog search on home (name/tagline/category, clear button, empty state)
- [x] Order timestamps ("Placed Jun 9, 8:54 PM" in history + tracking)
- [x] "Customers also bought" suggestion row on product detail
- [x] Accessibility: semantics + 48dp targets for hearts, steppers, back/clear; rating stars read naturally
- [x] Cart line totals for quantity > 1
- [x] Locale-pinned price formatting, real plurals, ETA derived from courier constant
- [x] Persist wishlist with DataStore (survives process death)
- [x] Notification deep links (delivery → tracking, price drop → product)
- [x] Fake scarcity line on product pages ("Only N left", imaginary stock)
- [x] "Save for later" cart action (moves line to wishlist)
- [x] Lint cleanup: template colors.xml, redundant activity label
- [x] Daily "shopping urge resisted" streak (persisted, on Orders stats)
- [x] "Keep browsing" recently-viewed row on Home (session-only)
- [x] Persist lifetime stats (orders/items/cents kept) with DataStore

## Backlog (future)
- [ ] Persist the order list with DataStore (wishlist + stats done; orders need serialization)
- [ ] Rename package from com.example.myapplication to a real id
