# 🛒 Cartharsis

> **Add to cart. Feel better. Buy nothing.**

Cartharsis is a **dopamine shopping simulator** for Android — a fully fake marketplace
that gives you the complete emotional arc of online shopping (browsing, deal-hunting,
cart-filling, checkout, delivery tracking) with one crucial difference: **nothing is real,
nothing is charged, and nothing arrives.**

## Why does this exist?

The app is inspired by the *"dopamine sites"* trend that took off in South Korea, where
stressed and burned-out Gen Z users flock to fake delivery and shopping platforms to get
the pleasant anticipation of ordering — the browsing, the star ratings, the courier
tracking — without spending money or feeding compulsive habits.

- [Korea Times — Gen Z turn to 'dopamine sites' for quick comfort](https://www.koreatimes.co.kr/lifestyle/trends/20260527/gen-z-turn-to-dopamine-sites-for-quick-comfort)
- [SCMP — South Korea's lonely, stressed Gen Z find comfort in apps that do nothing](https://www.scmp.com/news/asia/east-asia/article/3354966/south-koreas-lonely-stressed-gen-z-find-comfort-apps-do-nothing)
- [Nevsedoma — Fake marketplaces are gaining popularity in South Korea](https://nevsedoma.com.ua/en/706966-fake-marketplaces-are-gaining-popularity-in-south-korea-4-photos.html)

The psychology is simple: most of the dopamine in shopping comes from **anticipation**,
not ownership. Cartharsis keeps the anticipation and throws away the credit card bill.
It doubles as a harm-reduction toy for impulse shoppers — when the urge hits, shop here
instead, and watch your "money not spent" counter grow.

## Features

- 🏬 **Fake marketplace** — 120+ products across 15 categories (tech, audio,
  gaming, kitchen, beauty, pets, outdoors…) that read like a real shop: invented
  brands, realistic specs and prices, star ratings, and glowing fake reviews. The
  listings play it straight on purpose — the research says believability is what
  makes the anticipation work; the satire lives in everything around them
- ⚡ **Flash deals** — a rotating "deal of the moment" with a live countdown timer,
  because urgency is half the fun
- 🛒 **The cart ritual** — add to cart with haptic feedback and a bouncing cart badge
- 💳 **Zero-risk checkout** — pay with the *Imagination Express* card (balance: ∞)
- 🎉 **Confetti on purchase** — the order-placed dopamine spike, fully simulated
- 🚚 **Live courier tracking** — watch a courier carry your nothing across town in
  real time, status by status, until "delivery" completes
- 🔔 **Delivery notifications** — a real push notification announcing that
  *"Your nothing has been delivered"*
- 💖 **Wishlist** — heart the things you want; wanting is free and stays free
- 🔻 **Price-drop fakery** — wishlisted items randomly "drop" 15–40% for a short
  window, with a breathless notification, exactly like the real apps that train you
  to check back — except the deal was always $0.00
- 📈 **Savings stats** — total fake orders, items "bought", and a running counter of
  real money you did **not** spend
- 🔍 **Catalog search** — find the exact nothing you're looking for, by name,
  tagline, or category
- 🛍️ **"Customers also bought"** — the recommendation rabbit hole, faithfully
  reproduced on every product page
- 🔥 **Fake scarcity** — "Only 3 left in stock (the stock is imaginary, hurry anyway)"
- 💾 **A wishlist that remembers** — hearts and your urge-resisted streak survive
  app restarts (DataStore); orders intentionally evaporate
- 🔁 **Urge-resisted streak** — one fake order a day keeps the real spending away;
  miss a day and the streak breaks, exactly like the apps that train you
- 📲 **Notification deep links** — tap "your nothing was delivered" and land on
  that order's live tracking, like a real shop would

## What this app will never do

- ❌ No real payments, no payment SDKs, no prices that mean anything
- ❌ No network calls — the entire catalog is local and fake
- ❌ No accounts, no tracking, no ads, no data leaving the device
- ❌ No dark patterns *with consequences* — all the manipulation, none of the harm

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | Single activity, MVVM (`ViewModel` + `StateFlow`) |
| Navigation | Navigation Compose |
| Data | Hardcoded in-memory fake catalog; no backend |
| Min / target SDK | 24 / 36 |

## Building

```bash
./gradlew assembleDebug      # build the APK
./gradlew test               # run unit tests
./gradlew installDebug       # install on a connected device/emulator
```

Open the project in Android Studio and hit ▶ for the easy route.

## Disclaimer

Cartharsis is a parody/wellness toy. It is not a store. If you try to return a product,
the product was never there. That's the point. 🧘
