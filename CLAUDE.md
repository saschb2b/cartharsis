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
JAVA_HOME=/app/extra/jbr ./gradlew lint           # warningsAsErrors; baseline in app/lint-baseline.xml
JAVA_HOME=/app/extra/jbr ./gradlew spotlessCheck  # ktlint formatting gate; spotlessApply fixes
```

SDK location comes from `local.properties` (`/home/sascha/Android/Sdk`).

CI (`.github/workflows/ci.yml`) runs spotlessCheck + lint + test + assembleDebug
on every push/PR; the emulator smoke test runs weekly/on-demand
(`instrumented.yml`). Run `spotlessApply` before committing — formatting is a
hard gate. ktlint settings live in the root `build.gradle.kts`
(editorConfigOverride) with a mirrored `.editorconfig` for IDEs; change both.

## Architecture

Single-activity Jetpack Compose app, MVVM, no DI framework.

```
app/src/main/java/com/cartharsis/
├── MainActivity.kt          # NavHost + bottom bar, notification permission
│                            #   request, branded splash (core-splashscreen:
│                            #   bag-with-heart mark on cream/night, held via
│                            #   setKeepOnScreenCondition until the DataStore
│                            #   profile loads — no white flash, no blank frame)
├── ShopViewModel.kt         # All app state: catalog, cart, wishlist, price drops,
│                            #   orders, delivery sim, stats (AndroidViewModel)
├── Notifier.kt              # Notification channel + delivery/price-drop pings
├── Chime.kt                 # SoundPool UI sounds (res/raw, Ogg/Vorbis only —
│                            #   SoundPool can't decode Opus/FLAC)
├── data/
│   ├── Models.kt            # Product, Review, CartItem, Order, OrderStatus,
│   │                        #   Product.withPriceOverride (fake price drops),
│   │                        #   bundle `includes` + `variantGroup`/`variantLabel`,
│   │                        #   fakeStockLeft, streak math, price/date formatting
│   ├── FakeCatalog.kt       # Hardcoded products, categories, review generator,
│   │                        #   variantsOf(group) for swatch siblings
│   ├── Storefront.kt        # Pure per-open home variety: homeGreeting, homeOrder,
│   │                        #   homeShelves (seeded, time-of-day, daily-stable)
│   ├── Impact.kt            # Pure Orders payoff: keptEquivalent, savings
│   │                        #   milestones, badges, newlyEarned
│   ├── NotificationPolicy.kt # Pure when-to-ping rules (background-only, quiet
│   │                        #   hours, price-drop cooldown)
│   └── WishlistStore.kt     # DataStore persistence: wishlist, profile, stats,
│                            #   streak, user reviews (single "cartharsis" file)
└── ui/
    ├── theme/               # Vibrant "dopamine" palette (pink/purple/orange)
    └── screens/             # One file per screen + Common.kt shared pieces
        ├── OnboardingScreen.kt  # First-run fake signup: name → address → card
        ├── HomeScreen.kt        # Per-open dynamic storefront: greeting, flash
        │                        #   deal, chips, themed shelves, shuffled grid
        ├── ProductDetailScreen.kt
        ├── WishlistScreen.kt    # Hearted items + price-drop badges
        ├── CartScreen.kt
        ├── CheckoutScreen.kt    # Fake payment → confetti success
        ├── TrackingScreen.kt    # Animated courier delivery simulation
        ├── OrdersScreen.kt      # "Your impact" payoff: kept-money hero, savings
        │                        #   vault, milestones entry row, order cards,
        │                        #   empty state
        └── MilestonesScreen.kt  # Trophy room (pushed from Orders): career stat
                                 #   chips + badge grid
```

- One `ShopViewModel` (an `AndroidViewModel`, for notification context) scoped to
  the activity, passed to screens; state exposed as `StateFlow`.
- The delivery simulation is a coroutine in the ViewModel that advances an order
  through `OrderStatus` stages on a timer; screens only render the state. Delivery
  completion and wishlist price drops post real notifications via `Notifier`
  (permission-guarded; channels `cartharsis.deliveries` + silent
  `cartharsis.wishlist`), gated by `data/NotificationPolicy` — background-only,
  quiet hours and a cooldown for drops. Notifications must stay calm; this is a
  wellness app wearing a shopping app's clothes. A courier-nearby ping fires at
  ~80% trip progress and the delivered ping updates the same shade entry in
  place (shared id, alert-once) — one buzz per order. Arrival is interactive:
  a live-watched delivery presents a sealed parcel and the celebration
  (confetti, haptic, money tick, Mystery Box reveal) fires on the unbox tap.
- Price drops are an overlay map (`priceDrops: id → cents`) applied at display/
  cart-add time via `Product.withPriceOverride` — the catalog itself is immutable.
  Cart lines snapshot the price at add time.
- Bundles & variants mimic Amazon (researched). A bundle is an ordinary product
  with a non-empty `includes` list (rendered as a "What's included" card);
  bundle prices stay honestly below the sum of parts — no fabricated
  strikethroughs. Variants are sibling listings sharing a `variantGroup` (each
  its own id/price/reviews, like Amazon's separate ASINs); the PDP shows a
  `variantAxis` swatch row ("Color"/"Edition") and swaps the displayed sibling
  in place via a local `selectedId`. Invariants are locked by FakeShopTest.
- Trading cards: three invented games (Pocket Critters, Duelbound, Manaforge),
  each in two collectible series. Per series the formats — booster pack, sealed
  display, tin, collector box — are variant siblings on the "Format" axis, so
  the grid leads with the cheap entry pack (locked by test) and the PDP flips
  formats in place; tins/collector boxes are bundles. A delivered card order
  unboxes into the pack-rip ceremony (modeled on the genre's best opener):
  present the booster, drag across the foil to tear it (haptic ticks, spring-
  back on release, taps work too), tap through four seeded commons, then the
  chase card last — face-down under a building glow — and the flip lands with
  confetti + haptic + chime together (the flow's one big celebration; the
  parcel tap deliberately doesn't fire it for card orders).
  `FakeCatalog.packRipFor` deals the pack (commons + chase last, seeded per
  order/product, tested); the celebration card keeps the "top pull" line as
  the permanent record. Decorative randomness in the Mystery Box mold — shown
  only after delivery, never as a pre-purchase odds tease.
- Orders = the "your impact" payoff screen (researched), not a dry list: a
  full-width count-up hero of money kept with relatable equivalents ("≈ 2 movie
  nights"), a hand-rolled filling savings vault toward the next milestone, and
  order cards reframed as "you kept $X". The trophy case (badge grid) and the
  career stat chips live on a separate pushed `MilestonesScreen`, reached via a
  slim "🏆 Milestones · N of M ›" entry row — the Orders page stays a glanceable
  payoff, not a stats wall; the milestone-cross celebration (confetti+haptic+
  chime, only on a genuine in-session crossing) still fires on Orders. Big
  numbers auto-size (BasicText/TextAutoSize) and stat chips wrap (FlowRow) so
  nothing cramps on narrow phones. A brand-new user gets an inviting value-first
  empty state with a ghost preview, not a lonely $0. The pure logic
  (equivalents, milestones, badges, newlyEarned) lives in `data/Impact.kt`,
  tested. Framing is always celebratory (what you kept), never loss-framed.
- Dynamic storefront (researched): the home screen varies every open from a fixed
  catalog + one `ShopViewModel.homeSeed`, refreshed on app foreground (a
  ProcessLifecycle ON_START observer) so a genuine re-open is a fresh wander but
  internal navigation never reshuffles mid-browse. `data/Storefront.kt` holds the
  pure seeded logic — rotating time-of-day greeting, shuffled grid, and a deck of
  themed shelves (a daily-stable collection for continuity + a personalized
  Rediscover row + explore shelves). Stable anchors (greeting position, chips,
  grid) stay put; only the middle band varies. The variety is the reward — never
  add urgency/FOMO/infinite-scroll traps. Shelves show on the default view only.
- Navigation Compose with plain string routes (`home`, `product/{id}`, `wishlist`,
  `cart`, `checkout`, `tracking/{orderId}`, `orders`, `milestones`).
- Persistence: wishlist, urge streak, lifetime stats, the user's reviews, and
  the onboarding profile (name/address, used by checkout and the card) live in
  DataStore Preferences (single "cartharsis" file); orders and cart are
  intentionally in-memory and reset on process death. Persisting orders is a
  listed future task in tasks.md. First run is gated by `Profile.onboarded`
  (installs with existing data are treated as onboarded).
- Notification taps deep-link via a route-string intent extra
  (`Notifier.EXTRA_ROUTE`) consumed by MainActivity into Navigation Compose.

## Conventions

- Kotlin official code style; Compose-idiomatic (state hoisting, `Modifier` as the
  last default param, previews where cheap).
- UX register (from the Baymard/Amazon/Apple research pass): dense, colorful
  treasure-hunt on browse surfaces (home grid); calm and roomy on decision
  surfaces (PDP buy box, cart summary, checkout, tracking). The accent palette
  is reserved for primary CTAs and savings; prices render in ink (onSurface),
  one badge max per card. Shared shop components (cards, stepper, price row,
  section header, and the `NestedTopBar` worn by every pushed screen) live in
  `ui/screens/Common.kt` — reuse, don't fork. Nested (pushed) screens — PDP,
  checkout, tracking — get a standard Material small top app bar via
  `NestedTopBar`: a clearly identifiable back button (a hand-rolled Material-spec
  arrow, `BackArrowIcon`, in a full 48dp touch target — no material-icons
  dependency), an optional title, and an actions slot. Don't reintroduce bare
  `Text("←")` glyph back buttons.
- Reward moments (from the reward-psychology research pass): ceremony = gesture →
  suspense → resolution (hold-to-pay → fake-labor lines → checkmark + haptic +
  chime at stroke completion). Sound, haptic, and visual fire at the same
  instant — `Chime.play*()` sits next to `performHapticFeedback()`, never on
  its own timeline. One big celebration per flow, scaled to the milestone;
  cumulative numbers tick up rather than appear; randomness stays decorative and
  free. Never: confirmshaming, loss-framed streaks, urgency that pressures the
  decision (post-commit anticipation countdowns are fine).
- Checkout dramaturgy: the cart (decision surface) is honest about $0.00; the
  checkout/payment ceremony plays it straight at the full fake price (a
  commitment to $0.00 carries no weight); the $0.00 truth lands as the success
  screen's punchline. Don't reintroduce pre-commit "$0.00" spoilers.
- Product "images" are emoji — no image loading library, keep it that way.
- All animations are hand-rolled Compose/Canvas (confetti, courier) — no animation deps.
- Versions live in `gradle/libs.versions.toml`; add dependencies there, not inline.
- Copy/tone, two registers (per the dopamine-site research: believability drives
  the anticipation, so the satire lives in the frame, not on the shelf):
  - Product listings play it straight — realistic specs, prices, and sincere
    marketing copy, like a real shop. Names stay invented and fake-adjacent
    ("AuraPhone", "CloudStride"); never imitate a real brand. Chaos-category
    items may be absurd, but their copy stays deadpan-sincere.
  - App chrome is playful and self-aware ("Imagination Express card", reviews,
    delivery-of-nothing notifications, order statuses) — that's where the wink
    belongs.

## Workflow

- tasks.md is the living task list — update checkboxes as work lands.
- Build with `assembleDebug` after meaningful changes; it's the main correctness gate.
