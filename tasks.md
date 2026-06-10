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
- [x] Flash deal banner applies live price-drop overrides
- [x] Compose previews for shared product cards
- [x] Rotating add-to-cart snackbar copy
- [x] Cart merge logic extracted to pure, tested plusProduct
- [x] Instrumented smoke test: browse → cart → checkout → order placed
      (bumped espresso/junit-ext test deps for Android 16 compatibility)

## Phase 8 — Calm notifications rework
- [x] NotificationPolicy (pure, tested): pings only when the app is backgrounded;
      price-drop pings also gated by quiet hours (22:00–08:00) and a 30-min cooldown
- [x] Split channels: deliveries (default importance) vs. wishlist drifts (low/silent);
      legacy `cartharsis.events` channel deleted on upgrade
- [x] Slow the drop engine: ~60s interval, 5-min drop lifetime (was 25s/90s)
- [x] Calmer price-drop copy (no urgency, no countdown framing)

## Phase 9 — Catalog realism + expansion
- [x] Research check: Korean dopamine sites simulate *real* ordering — believability
      is the mechanism; satire moved from listings to the app frame
- [x] Rewrite all 27 original listings straight-faced (realistic specs/prices,
      invented brands kept); reviews stay satirical — that's where the wink lives
- [x] Horizontal: 6 → 13 categories (+ Kitchen, Beauty, Fitness, Outdoors, Pets,
      Hobbies, Stationery); espresso/skincare/yoga-mat recategorized accordingly
- [x] Vertical: 27 → 93 products, 7–8 per category; 21 sale-anchored products feed
      the flash deal rotation; original ids preserved for saved wishlists
- [x] Review pool 12 → 20 entries
- [x] Docs updated: CLAUDE.md two-register tone convention, README marketplace bullet
- [x] Niche-coverage pass: 93 → 124 products, 13 → 15 categories. New Audio
      (earbuds, turntable, speakers, amp) and Gaming (console, handheld, controller,
      two games, 240Hz monitor, gaming mouse/laptop) categories; Tech deepened with
      phones (base + fold), VR, ultrabook, 5K monitor, PC parts (GPU/CPU/SSD), mouse;
      knives + whetstone in Kitchen; plants, lamps, rug, mirror in Home; 3D printer
      + filament in Hobbies. Invented brands kept coherent (Vortex, Crescent, Meteor,
      AuraPhone line)

## Phase 10 — Identity cleanup
- [x] Rename package/namespace/applicationId com.example.myapplication → com.cartharsis
      (sources moved to java/com/cartharsis, imports rewritten)
- [x] Theme renames: MyApplicationTheme → CartharsisTheme, Theme.MyApplication →
      Theme.Cartharsis (manifest + themes.xml)
- [x] Gradle project name "My Application" → "Cartharsis"
      (note: new applicationId = fresh install identity; old install's DataStore
      does not carry over)

## Phase 11 — Buying-experience polish (Baymard/Amazon/Apple patterns, no dark ones)
- [x] Research pass: Baymard + Amazon/Apple/Temu UX patterns, dark patterns excluded
- [x] Design system: type scale w/ tight display tracking, per-product gradient tiles,
      compact rating ("★ 4.8 · 12.5k"), single-badge rule, shared QuantityStepper,
      SectionHeader; accent color reserved for CTAs + savings, prices in ink
- [x] PDP rebuilt in canonical order: hero → title → rating → price ("You save X",
      de-fanged scarcity) → delivery promise → Buy now → bullets/specs → rating
      distribution bars + avatar reviews → also-bought; sticky buy bar with stepper,
      "Added ✓" button state, snackbar with View-cart action
- [x] Cart: line cards w/ stepper, deal-savings summary row, total-on-button checkout CTA
- [x] Checkout: back affordance, labeled sections, emoji-tile order summary, cycling
      processing copy, confirmation with order # + arrival promise + two CTAs
- [x] Home: savings pill, emoji category chips, results header, animateItem grid
- [x] Nav transitions: fade-through tabs, slide-in pushed screens; Buy now → checkout
- [x] Tracking headline ETA first; Orders status chips
- [x] Verified on emulator: smoke test green + screenshot tour (home/PDP/reviews/cart)

## Phase 12 — Payment ceremony + order-experience dopamine pass
- [x] Research pass: reward-moment psychology (anticipation > consummation, peak-end,
      goal-gradient, operational transparency/labor illusion) + Apple Pay ceremony,
      Duolingo celebrations, Domino's tracker; dark variants catalogued and avoided
- [x] Hold-to-pay button: ~0.9s fill with rising haptic ticks, spring-back on early
      release, accessibility onClick action as the screen-reader path (smoke test
      uses it after performClick proved gesture-incompatible)
- [x] Ceremony sequence: rotating fake-labor lines (600ms each) → stroke-drawn
      checkmark (380ms, haptic at completion) → 250ms breath → celebration
- [x] Success screen: spring-in hero, order # + arrival promise, money-kept counter
      ticking up, rotating punchline, confetti scaled to milestones (first/every 10th)
- [x] Tracking: horizontal pizza-tracker (endowed progress, pulsing current node,
      courier leg fills live), map trail fills solid behind a bobbing courier with
      goal-gradient acceleration, en-route micro-event vignettes, ~1-in-8 rocket
      courier (decorative, stable per order), arrival confetti only on live
      transition, delivered card counts the kept money up
- [x] Orders: all four stats tick up, order cards with gradient item tiles + chevron
- [x] Verified on emulator: instrumented test green + screenshot tour of checkout
      form → processing → checkmark → success → tracking (packing / on-the-way /
      delivered)

## Phase 13 — Review section: quantity + quality (autopilot run)
- [x] Review pool 20 → 84: 24 shared entries (now with 1–3★ satire so the
      distribution bars have visible witnesses) + four category regulars × 15
      categories; products carry 4–6 deterministic reviews (was a flat 3), two in
      the category's voice; tested (count range, distinctness, rating sanity,
      low-star presence)
- [x] Review cards: "✓ Verified non-buyer" badge, posted-age label, "N found this
      helpful" — derived stably from (product, slot) since pool reviews are shared
- [x] "Show all N reviews" expander (3 visible by default)
- [x] PDP rating row jumps to the review section (chevron affordance, a11y label)
- [x] Reviews sorted most-helpful-first

- [x] "Write a review" flow (approved by owner): star picker + text editor on the
      PDP, one review per product, pinned "Your review" card with Edit/Delete,
      persisted via ReviewStore (DataStore string set, control-char codec with
      round-trip + garbage-rejection tests). Verified on emulator incl. process-
      death persistence.

Declined by owner: review sort/filter controls.

## Phase 14 — Checkout dramaturgy fix
- [x] Hold-to-pay charges the full fake total ("Hold to pay $1,299.00") — a
      commitment to $0.00 carried no weight; the $0.00 reveal moves entirely to
      the success screen's punchline
- [x] Pre-commit spoilers removed: "Charged to your card $0.00" summary row cut;
      processing lines play it straight ("Charging $1,299.00…"), checkmark caption
      now "Payment complete"
- [x] Back-press gaps: consumed during processing (no backing out mid-payment),
      confirmation back goes to shopping instead of the dead cart
- [x] Verified on emulator (form → charging → reveal) + instrumented test green

## Phase 15 — Toolchain: formatter, lint gate, CI
- [x] Spotless + ktlint (android_studio style, trailing commas enforced, Composable
      naming exemption, argument-list-wrapping disabled); one-time normalize applied
- [x] Android Lint promoted to a hard gate (warningsAsErrors + checked-in baseline
      absorbing 14 historical advisories)
- [x] GitHub Actions: ci.yml (spotlessCheck + lint + test + assembleDebug per
      push/PR, Gradle cache, wrapper validation), instrumented.yml (emulator smoke
      test, weekly + on-demand)

## Phase 16 — Believable onboarding
- [x] First-run signup played straight: welcome → create account (name) → delivery
      address (prefilled imaginary defaults, editable) → payment method, where the
      Imagination Express is "issued" bearing the user's name
- [x] ProfileStore (DataStore): name/street/city/onboarded; installs with existing
      data are grandfathered as onboarded; checkout's Deliver-to and card holder
      personalize from the profile
- [x] ImaginationCard promoted to Common.kt with a cardHolder param (lint gate
      caught and fixed the Modifier-param ordering on the way)
- [x] Smoke test walks the signup on fresh installs; verified on emulator end to
      end (welcome → name → address → card with name → shop → personalized checkout)

## Phase 17 — Bottom nav to current Material guidelines
- [x] NavigationBar → M3 Expressive ShortNavigationBar (compact 64dp)
- [x] Badges carry plain counts per spec (the wishlist badge's "🔻3" emoji is gone)
- [x] Selection emphasis for emoji icons (no filled/outlined variants): full
      opacity + springy scale on the active tab, dimmed inactive tabs; cart
      bounce-on-add retained

## Phase 18 — Payment success chime
- [x] Chime helper (SoundPool, sonification audio attributes) + success_chime in
      res/raw; fires together with the haptic at checkmark stroke completion
- [x] Source file was Ogg/FLAC, which SoundPool silently fails to load —
      transcoded to Ogg/Vorbis; playback verified via the audio service's player
      events on the emulator

## Phase 19 — Arrival reward beat (autopilot plan)
- [x] Interactive unboxing: arrival presents a sealed, gently wiggling parcel
      ("Tap to unbox"); the tap triggers the opening, haptic, confetti, and the
      kept-money tick — celebration moves onto the user's action. Orders opened
      from history skip to the opened state, no re-celebration
- [x] Mystery Box product (Chaos): unboxing reveals what would hypothetically
      have been inside — an orderId-seeded catalog pick (decorative randomness,
      never gates value); deterministic pick unit-tested
- [ ] Courier-nearby notification at ~80% trip progress that posts under the same
      id as the delivered one, so it updates in place — anticipation spike with
      zero extra shade noise; same background-only policy
- [ ] CLAUDE.md tracking notes updated for the unbox beat

## Backlog (future)
- [ ] Persist the order list with DataStore (wishlist + stats done; orders need serialization)
