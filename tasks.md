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
- [x] Courier-nearby notification at ~80% trip progress that posts under the same
      id as the delivered one, so it updates in place — anticipation spike with
      zero extra shade noise; same background-only policy
- [x] CLAUDE.md tracking notes updated for the unbox beat

## Phase 20 — Current-product catalog expansion (autopilot)
- [x] 125 → 209 products; every category lifted to 10+, Tech to 26, 54 sale anchors
- [x] Filled the "current real-life product" gap with invented brands per niche:
      Tech (smart ring, e-reader, power station, AI glasses, action cam, Wi-Fi 7,
      e-ink notebook, trackers, GaN charger, portable monitor); Audio (portable
      speaker, soundbar, IEMs, bone-conduction, party speaker); Gaming (retro
      handheld, racing wheel, chair, capture card, cozy + co-op titles); Kitchen
      (40oz tumbler, nugget ice, pizza oven, blender, soda maker, gooseneck kettle);
      Beauty (LED mask, multi-styler, gua sha, lip oil, SPF serum); Self-Care (red
      light, sunrise alarm, neck/eye massagers, sauna blanket); Fitness (walking
      pad, recovery boots, adjustable kettlebell, smart scale, weighted vest);
      Snacks (freeze-dried candy, chili crisp, hot honey, electrolytes, kunafa bar,
      matcha); Outdoors (e-scooter, solar panel, insulated bottle, trekking poles,
      packable down); Pets (self-cleaning litter, GPS tracker, slow feeder, donut
      bed, DNA kit); Fashion (parachute cargos, sling bag, shearling clogs, puffer
      vest, wide-leg denim); Hobbies (bird-feeder cam, pottery wheel, diamond
      painting, terrarium, brick bouquet, mushroom kit); Stationery (fineliners,
      mechanical pencil, annotation kit, shimmer ink, brush markers); Home (video
      doorbell, smart lock, e-ink calendar, LED strip, smart plugs); Chaos (plush
      crocodile, Bigfoot statue, knife duck, 200 rubber ducks, sumo suits)
- [x] Appended after Mystery Box so all existing product ids stay stable
- [x] README product count + examples refreshed; one commit per category, tests green

## Phase 21 — Current-gen console ecosystem (autopilot)
- [x] Gaming 14 → 30 products: filled the bare accessory shelf around the existing
      flagship console + handhelds
- [x] Hardware/accessories: dock+handheld hybrid console (Switch archetype), wireless
      gaming headset, extra-controller + charging-dock bundle, 2TB storage expansion
      card, handheld travel case, arcade fightstick, vertical cooling/charging stand,
      clip-on handheld grip + power bank
- [x] Immersion/streaming: console-tethered VR headset, USB broadcast mic, LCD-key
      stream control pad
- [x] Library: 5 titles across the genres people search for — Ashen Banner
      (soulslike), Override: Tactical (hero shooter), Nightshift Anomaly (co-op
      horror), Pitch Legends 26 (annual sports), Redline GT 5 (sim racer, pairs with
      the Vortex Force wheel)
- [x] Brands stay coherent (Meteor console family, Vortex peripherals, Wisp handheld);
      ids appended so existing state stays stable; 209 → 225 products; tests green
- Proposal (needs owner call): a second/rival console brand and console+game starter
      bundles would add realism, but the bundle concept needs a data-model change and
      a rival ecosystem is a judgment call on how far to mirror the real duopoly.

## Phase 22 — Console bundles & variants (Amazon pattern, researched)
- [x] Researched Amazon's Switch-2-style bundle/variant merchandising; built to the
      real pattern, dark patterns (fake strikethroughs, sum-as-savings) excluded
- [x] Bundles: additive `Product.includes` + "What's included" PDP card; 5 console
      bundles as separate listings (pack-in game, extra controller, starter kit,
      everything, MK-II combo), each priced honestly below the sum of its parts
- [x] Variants: additive `variantGroup`/`variantLabel`/`variantAxis`; sibling
      listings + a PDP swatch row that swaps the displayed sibling in place
      (selectedId, no re-nav). Meteor Pro Controller colorways (Carbon Black +
      Volcanic Red/Starlight Blue/Sterling Silver) and a Swift console
      Standard/Digital Edition split. Color axis shows dots; other axes label-only
- [x] Tests: bundle honesty (real contents + real savings) and variant-group
      integrity (≥2 siblings, one axis, distinct labels). 28 → 30 tests
- [x] 230 → 234 products; docs (CLAUDE.md model/conventions) updated; verified on
      emulator (bundle includes card; color + edition swaps change name/price/reviews)
- Open proposal (still owner's call): a rival console brand, console+game cart
      bundles via a real "Frequently bought together / Add all" block (honest sum),
      and a swatch UI on the home grid card

## Phase 23 — The three approved bundle/variant proposals
- [x] Rival console brand: Orbit ecosystem (Orbit One disc console with a
      Disc/Digital edition variant, Orbit Glide controller, Orbit One + Skybound
      bundle) — proves the patterns travel across brands. 234 → 238 products
- [x] Frequently bought together: curated companion sets in
      `FakeCatalog.boughtTogether` (consoles, AuraPhone, espresso machine); PDP
      block with product + companions, honest combined Total (no fake saving),
      and "Add all N to cart" → new `ShopViewModel.addAllToCart`
- [x] Variant collapse: grid/search shows one card per variant group
      (`FakeCatalog.collapseVariants`), representative = base or matched sibling;
      the "N colors/editions" card hint carries the rest
- [x] Tests: collapse (both rep branches) + FBT companion resolution. 30 → 33
- [x] Verified on emulator: Orbit listings; FBT total $527 + Add-all snackbar;
      "Pro Controller" collapses 4→1, "Volcanic" surfaces the red variant

## Phase 24 — Dynamic storefront (fresh on every open)
- [x] Research: how a fixed local catalog feels fresh per open (one seed drives
      it); ethical variable reward, no urgency/FOMO/infinite-scroll traps
- [x] `ShopViewModel.homeSeed` refreshed on app foreground (ProcessLifecycle
      ON_START); stays put on internal nav so browsing doesn't reshuffle
- [x] `data/Storefront.kt` (pure, tested): homeGreeting (time-of-day rotating),
      homeOrder (seeded grid shuffle), homeShelves (deck of ~10 themed
      generators: daily-stable collection + personalized Rediscover + explore
      shelves, deduped, time-of-day shelf)
- [x] HomeScreen: rotating greeting, shuffled "Everything" grid (search stays
      stable), themed shelf rows on the default view; stable anchors kept
- [x] Tests: greeting/order determinism + permutation, shelf fullness/dedup/
      daily-stability/personalization. Smoke test searches to surface a product
      (robust to the shuffled grid). 33 → 36
- [x] Verified on emulator across re-opens: greeting + grid + shelves change,
      daily collection stays stable within the day

## Phase 25 — Orders "your impact" redesign (researched)
- [x] Research: how impact/year-in-review screens turn stats into an emotional
      payoff (hero number, equivalents, vault, ethical milestones, empty state,
      responsive layouts). Behance link was JS-only; worked from broader research
- [x] data/Impact.kt (pure, tested): keptEquivalent + keptInCoffees, savings
      milestones + from-zero progress, badges, newlyEarned
- [x] Hero: full-width warm-gradient count-up of money kept + relatable
      equivalent line; auto-sizes (BasicText/TextAutoSize) so it never clips
- [x] Cramp fix: secondary stats moved to a wrapping FlowRow (3-across → 2-up on
      narrow phones) instead of the old squeezed 4-column row — verified at 600px
- [x] Filling savings vault (hand-rolled Canvas jar) toward the next milestone
- [x] Milestone trophy shelf: earned bright, locked faint "next up"; "N of 8"
- [x] Order cards reframed as "you kept $X" with emoji collages
- [x] Inviting first-visit empty state: value pitch + ghost-preview hero + Browse
      CTA (new onBrowse nav) instead of a lonely $0
- [x] Milestone-cross celebration (confetti+haptic+chime), only on a genuine
      in-session crossing (tested newlyEarned)
- [x] Tests 36 → 40; smoke test green; verified on emulator (normal + narrow,
      empty state, vault, milestones)

## Phase 26 — Orders declutter: Milestones trophy room
- [x] Extracted the trophy shelf + career stat chips from Orders into a pushed
      MilestonesScreen (route `milestones`, NestedTopBar, 3-up badge grid with
      room to breathe instead of a cramped horizontal strip)
- [x] Orders keeps hero + savings vault + a slim "🏆 Milestones · N of M ›"
      entry row; the milestone-cross celebration still fires on Orders

## Phase 27 — Trading cards (collectibles)
- [x] New "Trading Cards" category: three invented games (Pocket Critters,
      Duelbound, Manaforge), two series each, sold as packs / sealed displays /
      tins / collector boxes — formats are Format-axis variant siblings, tins
      and boxes are honest bundles; plus binder/sleeves/deck-box accessories
      wired into frequently-bought-together
- [x] Collector-voice review pool; "Collector's corner 🎴" home shelf; Trading
      Cards joins the night-owl shelf set
- [x] Pack-rip payoff: a seeded chase-card "top pull" revealed on the unbox
      screen for delivered card orders (FakeCatalog.cardPullFor, Mystery Box
      mold — post-delivery only, gates nothing)
- [x] Tests: pull determinism/game-matching/card-only + grids lead with the
      entry-price pack, not the $150 display
- [x] Pack-rip ceremony v2 (modeled on Pokémon TCG Pocket's staging): card
      orders unbox into a presented booster → drag-to-tear foil (haptic ticks,
      spring-back, tap fallback) → tap through 4 seeded commons → chase card
      last, face-down under a glow → 3D flip with confetti+haptic+chime at
      completion → celebration card keeps the "top pull" record
      (FakeCatalog.packRipFor, tested; verified live on emulator)

## Phase 28 — Branded launch screen
- [x] Replaced the default icon-on-white system splash with a branded one
      (androidx core-splashscreen): hand-drawn bag-with-heart vector on the
      app's cream/night background (values-night variant), gentle scale+fade
      exit animation
- [x] Splash held via setKeepOnScreenCondition until the DataStore profile
      loads — kills both the white flash and the blank `profile == null`
      window; Theme.Cartharsis windowBackground now matches the Compose
      background for an invisible handoff
- [x] Verified cold start on emulator in light and dark mode

## Phase 29 — Trading cards autopilot: rip depth, card design, binder, tooling
- [x] Multi-pack rips: one rip per quantity of each card line (cap 3), each
      its own seeded deal; "Pack N of M", haptic+chime per chase, confetti
      saved for the finale
- [x] Rip a11y: dots announce "Card N of M"; faces read "name, rarity"
- [x] Card binder: every flipped chase persists (BinderStore + codec, tested);
      Milestones gains a per-game checklist — owned bright, unpulled "???"
- [x] Card design: real card anatomy (name bar + rarity gem, framed art,
      footer), deadpan flavor text on all 37 cards, tinted foil + gilt frame
      on chases, series name on the booster wrapper (cardSeriesTitles, tested)
- [x] Tooling: Compose Preview screenshot gallery (screenshotTest source set,
      updateDebugScreenshotTest to iterate, validateDebugScreenshotTest as a
      visual gate; references checked in) — first renders caught two real
      truncation bugs
- [ ] Proposal: add validateDebugScreenshotTest to CI (plugin is alpha; may
      flake on runner rendering — owner call)
- [ ] Proposal: pack-rip "tilt" parallax on the chase card (gyro/touch), in
      the Pocket mold — juicy but gimmick-adjacent, owner taste call

## Phase 30 — TCG authenticity pass (toward MTG/Pokémon/Yu-Gi-Oh standards)
- [x] Type line on every card, each game in its genre's idiom: Pokémon stage
      lines ("Basic Flame Critter"), Yu-Gi-Oh brackets ("[Spellcaster /
      Effect]"), Magic em-dash ("Legendary Creature — Human Wizard") —
      slim bar over a hairline rule, shrink-to-fit, read by TalkBack, locked
      by the flavor-sweep test
- [x] Collector print in the bottom corner (Pokémon set fraction / YGO set
      code / MTG padded number + rarity letter), seeded from the card name —
      FakeCatalog.collectorNumberOf, stability/idiom/uniqueness tested
- [x] Card back as a designed object: bordered inner panel, ringed medallion,
      wordmark, TRADING CARD GAME caption
- [x] Booster wrapper foil dress: serrated crimps (top strip + bottom seal),
      still diagonal gloss, honest "5 CARDS · 1 FOIL INSIDE" contents line
- [x] Rarity gem shape-coded the genre's way: circle common, diamond
      uncommon, five-point star for chases (color-blind safe, too)
- [x] Binder: tap a pulled chase to view the full card face in a dialog
      (foil, type line, collector print); locked slots stay "???"
- [x] Booster displays join the also-bought accessory rows
- [x] Battle stats in the genre's corner: Pokémon-position HP in the Critters
      name bar, Yu-Gi-Oh ATK/DEF and Magic P/T bottom-right — only on
      monsters/creatures, never on spells/traps/relics (mapping tested)
- [x] Per-series booster wrappers (Abyssal teal, Eclipse crimson, Verdant
      green) while faces/backs stay game-wide like real card backs; booster
      preview renders all six series
- [x] Verified live on emulator: order → courier → unbox → tear → commons →
      face-down back → chase flip (confetti) → binder → inspect dialog

## Phase 31 — Card backs with their genre's soul (CardBacks.kt)
- [x] Studied the three real backs' grammar: Pokémon = marbled cobalt swirls +
      one glossy dimensional emblem + chunky outlined wordmark; MTG =
      leather bookbinding (mottle, beads, oval, serif, lore emblem);
      Yu-Gi-Oh = textless lacquer + beveled spiral medallion + star gleam
- [x] CrittersBack: arc-marbled cobalt field, pink-to-orange critter ball
      with the app's heart as latch (homage, not the genre's red), specular
      gleam, grounded shadow, navy printed-edge rim
- [x] DuelboundBack: aubergine lacquer, edge vignette, four tapering bronze
      spiral arms (segment chains, fading as they unwind), molten core,
      bevel faked with highlight/shadow arcs, one star gleam — no text
- [x] ManaforgeBack: speckle-mottled leather, beaded trim, double gold oval,
      serif MANAFORGE, five hand-drawn element orbs (sun/tide/flame/void/
      growth) around a dark world-sphere, FORGEMASTER foot
- [x] Face-down cards are full-bleed (GameCardBack owns the surface, no
      wrapper frame); face renders proven byte-identical through the
      restructure; all hand-rolled Canvas, no deps
- [x] Verified live: Crimson Eclipse 2-pack order — wrapper, rip, vortex
      back under the pre-flip glow, chase flip, pack 2 staging

## Phase 32 — Card faces in their genre's layout (CardFaces.kt)
- [x] Fronts move to per-game layouts (GameCardFace) and become
      print-constant like the backs — a card doesn't re-ink for dark mode
- [x] CrittersFace (Pokémon skeleton): element-tinted body parsed from the
      type line (flame/tide/spark/bloom/dream/sky/meadow), stage chip → name
      → element-accent HP header, gilt art frame, italic species strip,
      boxed flavor, rarity gem in the bottom corner
- [x] DuelboundFace (Yu-Gi-Oh skeleton): kind-colored frame (amber monster,
      ice-blue ritual, violet fusion, green relic/spell, rose trap), metallic
      name plate + attribute orb, level stars derived from printed ATK/DEF,
      heavy art frame, set code under the art, tan text box closing with
      ATK/DEF over a rule
- [x] ManaforgeFace (Magic skeleton): color-identity frame (red elemental,
      blue wizard/instant, green enchantment, colorless artifact), serif
      parchment plates — title banner with seeded cost orb + identity pip,
      type banner wearing the rarity-colored set gem, italic text box,
      serif P/T plate in the corner, collector print along the foot
- [x] Two new gallery gates: Duelbound kinds (5 frames) and Manaforge
      identities (4 frames); name autosize fixed (wrap-based fit detection)
- [x] Verified live: Manaforge rip (artifact common, serif plates) and the
      binder dialog showing the new Critters face

## Phase 33 — Six series, six content languages (72-card catalog)
- [x] Content language per series, defined in FakeCatalog's pool docs:
      Emberglow = hearth/dawn/kept warmth; Abyssal Tides = deep water/night
      currents/soft glow; Forbidden Archive = haunted library, bureaucratic
      dread; Crimson Eclipse = blood-moon vigil, apocalyptic but unbothered;
      Ashveil = volcanic forge, smithing proverbs; The Verdant Throne =
      court gone to seed, nature wins politely
- [x] Pools keyed per series (a booster deals its own set's cards); each
      set filled to 8 commons + 4 chases — 22 new cards (Sunkit, Cinderbee,
      Dawnplume Radiant, Murmurshell, Inkpip, Vellamora, Brinesong, Stack
      Wyrm, Late Fee Wraith, Index of Forbidden Names, Waning Acolyte,
      Vigil Hound, Crimson Regent, The Unfinished Moon, Bloodbound Duelist,
      Ashwind Current, Anvilbound Sprite, Vulkhammer, Court Toadstool,
      Sapling Usurper, Bloomheart Sovereign, Pact of Quiet Growth, …)
- [x] Existing cards refined into their series' register (Shade of the
      Reading Room no longer says 'sorcery speed' — wrong game's words)
- [x] Collector prints number per series like real expansions: 012/96 vs
      012/102, DAR-EN/DCE-EN set codes, ASH/VER · 0042/0184 with rarity
      letter — slot-indexed in the set checklist, not name-hashed
- [x] Color identity learns the Verdant court (Treefolk/Fungus/Elk/Dryad
      green, Spirit blue); kinds/identities previews curated per frame
- [x] Binder groups into set pages with per-set pulled counts
      (chaseChecklistOf, tested); tests sweep every series product
- [x] Verified live: an Abyssal Tides rip deals only sea-critters; new
      chase Vellamora, Deepcrowned pulled into its set page
- [ ] Proposal: a one-time "set complete" celebration when a binder page
      fills (confetti+chime on the binder, scaled like a milestone cross).
      Reward-design call: it adds a second collection-long goal per series —
      owner taste on whether the binder should celebrate at all

## Phase 34 — Craving coverage (researched inventory gaps)
- [x] Researched what people impulse-buy / crave (2025 surveys + viral
      commerce): clothes 55%, groceries 50%, household 42%, toys/games/books
      #2 overall; TikTok categories = beauty/skincare, fragrance, gadgets,
      satisfying cleaning; "little treat culture" (57% of Gen Z weekly);
      blind-box collectibles a multi-billion craze
- [x] Gap-filled the catalog (+15 products): Moppling blind-box line
      (single, 6-pack, plush charm, capsule toy — mystery stated as fact,
      never odds bait), fragrance wardrobe (EDP, discovery set, solid tin),
      CleanTok trio (eraser blocks, drill brushes, squeegee broom), BookTok
      shelf (sprayed-edge romantasy, cozy mystery, book sleeve), home-café
      treats (boba kit, syrup trio); also-bought cross-sells wired
- [x] (Approved) Blind-box reveal + collection — became Phase 35

## Phase 35 — The Moppling reveal & shelf (blind boxes open)
- [x] Data: four waves matching the listing copy exactly (12 Bog Friends,
      12 Cloud Court, 8 Plush Charms, 10 Desk Gremlins — tested);
      mopplingPullsFor deals seeded figures per (order, product, box), a
      6-pack opens six distinct; decorative and free like the card chases
- [x] Ceremony: shake-and-pop reveal — wave-colored sealed box, three
      shakes (wobble + haptic tick, escalating copy), then figures spring
      in with haptic+chime+confetti together; boxes queue like pack rips;
      mixed card+box orders rip first, then shake
- [x] Collection: MopplingShelfStore persists "wave␁figure" entries (the
      binder's codec); Milestones gains the Moppling shelf — 42 slots
      across four waves, found figures named, locked ones "???";
      ChaseCardPill generalized into CollectiblePill
- [x] Verified live end-to-end: Bog Friends box shaken thrice popped
      Slowmop №3 of 12 with the burst; figure persisted across reinstall
      onto the shelf (1 of 42 found)

## Backlog (future)
- [ ] Persist the order list with DataStore (wishlist + stats done; orders need serialization)
