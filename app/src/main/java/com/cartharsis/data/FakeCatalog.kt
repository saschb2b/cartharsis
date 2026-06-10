package com.cartharsis.data

/**
 * The entire "marketplace". Nothing here is real, purchasable, or shippable.
 *
 * Listings play it straight — invented brands, realistic specs and prices,
 * sincere copy — because the research behind the app says believability is
 * what makes the anticipation work. The satire lives in the frame around the
 * shelf: the reviews below, the notifications, the checkout. Reviews are
 * assigned deterministically from a shared pool so the catalog is stable
 * across runs without any persistence.
 *
 * The first 27 products predate the catalog expansion and keep their listing
 * order so persisted wishlist ids stay pointing at the same products.
 */
object FakeCatalog {

    val categories = listOf(
        "All", "Tech", "Audio", "Gaming", "Home", "Kitchen", "Fashion", "Beauty",
        "Self-Care", "Fitness", "Snacks", "Outdoors", "Pets", "Hobbies",
        "Stationery", "Chaos",
    )

    private val genericPool = listOf(
        Review("Ji-woo K.", 5, "Arrived instantly because it never shipped. Incredible logistics."),
        Review("Marta S.", 5, "I own nothing and I have never been happier."),
        Review("Dev P.", 4, "Four stars only because I wanted to want it more."),
        Review("Hannah L.", 5, "My wallet sent me a thank-you card."),
        Review("Tom B.", 5, "Bought three. Returned zero. They were never here."),
        Review("Yuki T.", 4, "The anticipation alone fixed my whole week."),
        Review("Carlos M.", 5, "10/10 would not receive again."),
        Review("Anonymous", 5, "I showed my therapist. She added it to her cart too."),
        Review("Min-seo P.", 5, "Tracking the courier was the best 90 seconds of my day."),
        Review("Greg W.", 4, "Slightly smaller than expected (it does not exist)."),
        Review("Lena F.", 5, "Shipping was immediate. Nothing arrived right on time."),
        Review("Sam O.", 5, "Finally, a purchase my bank account agrees with."),
        Review("Priya R.", 5, "Five stars for the courier who delivered nothing, on time, in the rain."),
        Review("Jae-min L.", 5, "Checkout took ten seconds and my rent is still paid. Flawless."),
        Review("Sofia G.", 4, "I keep opening this instead of my banking app. Both apps approve."),
        Review("Noah E.", 5, "It said 'only 3 left' so I panicked and bought nothing immediately."),
        Review("Eun-ji C.", 5, "Replaced my 2am shopping habit and my 2am shopping debt."),
        Review("Viktor H.", 4, "Quality of the nothing is consistent with my previous orders."),
        Review("Amara D.", 5, "Wishlisted it, watched the price drop, felt everything. Spent zero."),
        Review("Your roommate", 5, "I can finally afford my hobbies. They are also in this app."),
        Review("Priyanka V.", 3, "Three stars. The anticipation was a ten, but then I remembered."),
        Review("Klaus W.", 1, "One star. I waited by the door all day like a fool. (Reordering now.)"),
        Review("Bea L.", 2, "Two stars because my real packages disappoint me now."),
        Review("Oskar T.", 5, "My fifth order. My savings account left a five-star review of me."),
    )

    /**
     * Each category has its own regulars — a graphics card and a fried
     * chicken bucket should not share a reviewer voice.
     */
    private val categoryPools = mapOf(
        "Tech" to listOf(
            Review("Arjun M.", 5, "Specs are flagship, price is fiction, performance is theoretical. Perfect."),
            Review("Renee C.", 4, "Benchmarked it in my head. Beats everything else I don't own."),
            Review("Tobias F.", 5, "Zero thermal throttling. Hard to overheat when it never powers on."),
            Review("Gwen S.", 5, "Future-proof forever. Can't go obsolete if it never existed."),
        ),
        "Audio" to listOf(
            Review("Marco D.", 5, "The soundstage is unbelievable. Literally."),
            Review("Fei L.", 5, "Silence has never sounded this good. Genuinely zero distortion."),
            Review("Hugo B.", 4, "A/B tested it against nothing and couldn't tell the difference."),
            Review("Tessa R.", 5, "Bass so deep it never surfaced."),
        ),
        "Gaming" to listOf(
            Review("Dev K.", 5, "Zero input lag. Zero input, too, but still."),
            Review("Lara P.", 5, "My backlog can't grow if the games never arrive. Strategic purchase."),
            Review("Milo J.", 4, "Rock-solid 60fps in my imagination. 120 on the fake monitor."),
            Review("Sana A.", 5, "Finally beat every level. It ships without any."),
        ),
        "Home" to listOf(
            Review("Ingrid H.", 5, "The room feels bigger already. Nothing takes up remarkably little space."),
            Review("Paulo S.", 5, "Assembly was instant. Zero parts, zero tears, zero allen keys."),
            Review("Nora E.", 4, "Matches my decor perfectly, which is also aspirational."),
            Review("Dmitri V.", 5, "My apartment is a minimalist dream now. I bought forty things."),
        ),
        "Kitchen" to listOf(
            Review("Chef Tomas", 5, "Restaurant quality. The restaurant is also imaginary."),
            Review("Aiko N.", 5, "Cleanup is effortless. Nothing to wash, ever."),
            Review("Bruno M.", 4, "My cooking improved the moment I stopped doing it."),
            Review("Greta F.", 5, "Meal prep for the week: done, in the sense that I dreamed it."),
        ),
        "Fashion" to listOf(
            Review("Camille R.", 5, "Fits perfectly. Nothing always does."),
            Review("Theo B.", 5, "Got so many compliments on the idea of it."),
            Review("Zadie M.", 4, "True to size, if the size is a concept."),
            Review("Luca G.", 5, "Pairs with everything in my imaginary capsule wardrobe."),
        ),
        "Beauty" to listOf(
            Review("Yuna S.", 5, "My skin has never looked like this. (It looks the same. Glowing, though.)"),
            Review("Priya D.", 5, "Dermatologist-untested and flawless."),
            Review("Mara K.", 4, "The glow is internal now. Cheaper that way."),
            Review("Elif T.", 5, "Shade match was perfect. The shade was hypothetical."),
        ),
        "Self-Care" to listOf(
            Review("Jonas W.", 5, "I have never been this calm about a purchase."),
            Review("Amara O.", 5, "My therapist asked what changed. I said: nothing, and it was free."),
            Review("Saskia B.", 4, "Slept like a person with no outstanding deliveries."),
            Review("Ravi P.", 5, "Inner peace, outer savings."),
        ),
        "Fitness" to listOf(
            Review("Coach Dana", 5, "Zero missed workouts since nothing arrived to skip."),
            Review("Pieter V.", 4, "My gains are theoretical, but so is my soreness."),
            Review("Keiko M.", 5, "Personal best: lifted nothing, every day, without fail."),
            Review("Omar F.", 5, "The discipline was inside me all along. The equipment was not."),
        ),
        "Snacks" to listOf(
            Review("Min-ji K.", 5, "Tasted incredible at 2am in my mind. Zero crumbs in my bed."),
            Review("Stefan R.", 5, "The diet is intact. The craving is satisfied. Witchcraft."),
            Review("Lia C.", 4, "Four stars only because I can still smell it, somehow."),
            Review("Gustav H.", 5, "Ordered the family size. Shared it with no one. There was nothing to share."),
        ),
        "Outdoors" to listOf(
            Review("Wren A.", 5, "Packed light for the trip. Couldn't pack lighter than this."),
            Review("Jorge M.", 5, "Survived a week in the wilderness of my living room."),
            Review("Helga S.", 4, "Waterproof rating unverifiable. It has never been outside. Neither have I."),
            Review("Finn O.", 5, "The view from the summit I didn't climb was stunning."),
        ),
        "Pets" to listOf(
            Review("Rosa & Biscuit", 5, "My dog waited by the door with me. Bonding experience. Five stars."),
            Review("Henrik J.", 5, "The cat ignored it before it didn't arrive. She's consistent."),
            Review("Tilly W.", 4, "No fur on it whatsoever. Cleanest pet product I own."),
            Review("Andre B.", 5, "My goldfish seems happier. Hard to verify. Five stars."),
        ),
        "Hobbies" to listOf(
            Review("Maren L.", 5, "Finished the whole project in zero evenings."),
            Review("Diego A.", 5, "My craft room has space for this and infinitely many other nothings."),
            Review("Petra N.", 4, "The instructions were blank, but so was the kit. Consistent."),
            Review("Callum S.", 5, "First hobby I've never abandoned."),
        ),
        "Stationery" to listOf(
            Review("June P.", 5, "Writes like a dream, which is where I use it."),
            Review("Anders K.", 5, "My planner is empty and so is my anxiety. Coincidence?"),
            Review("Mei F.", 4, "Paper weight: zero grams. Featherlight. Unbeatable."),
            Review("Viola R.", 5, "Organized my entire life by adding this to a list."),
        ),
        "Chaos" to listOf(
            Review("Babs Q.", 5, "Exactly as unhinged as advertised. Nothing arrived. Chaos achieved."),
            Review("Norm T.", 5, "Bought it as a joke. The joke was on no one. Flawless transaction."),
            Review("Zelda V.", 4, "My friends asked why. There was no answer. There was no item."),
            Review("Gus P.", 5, "This is the most committed I've been to a bit. No regrets, no product."),
        ),
    )

    /**
     * 4–6 deterministic reviews per product: two in the category's voice, the
     * rest from the shared pool (which includes the occasional disappointed
     * low-star poet, so the rating bars aren't lying). Rotated by id so the
     * category regulars don't always lead.
     */
    private fun reviewsFor(id: Int, category: String): List<Review> {
        val local = categoryPools[category].orEmpty()
        val total = 4 + id % 3
        val picks = ArrayList<Review>(total)
        if (local.isNotEmpty()) {
            repeat(2) { i -> picks += local[(id + i) % local.size] }
        }
        repeat(total - picks.size) { i ->
            // Step 5 is coprime with the pool size, so picks stay distinct.
            picks += genericPool[(id * 7 + i * 5) % genericPool.size]
        }
        val cut = id % picks.size
        return picks.drop(cut) + picks.take(cut)
    }

    private var nextId = 0

    private fun product(
        name: String,
        emoji: String,
        tagline: String,
        description: String,
        priceCents: Long,
        category: String,
        rating: Double,
        reviewCount: Int,
        originalPriceCents: Long? = null,
    ): Product {
        val id = nextId++
        return Product(
            id, name, emoji, tagline, description, priceCents, category,
            rating, reviewCount, reviewsFor(id, category), originalPriceCents,
        )
    }

    val products: List<Product> = listOf(
        // ---- Original catalog (ids 0–26, order preserved for saved wishlists) ----
        product(
            "AuraPhone 17 Ultra Max", "📱",
            "The flagship, fully realized.",
            "A 6.9-inch LTPO display that scales 1–120Hz, a 200MP main sensor with " +
                "sensor-shift stabilization, and a ceramic-glass back in four finishes. " +
                "80W fast charging and seven years of software updates.",
            129_900, "Tech", 4.8, 12473, originalPriceCents = 159_900,
        ),
        product(
            "NoiseGone Pro Headphones", "🎧",
            "Hear nothing but the music.",
            "Adaptive noise cancellation driven by eleven microphones, 40 hours of " +
                "battery, plush memory-foam earcups, and multipoint Bluetooth. Folds " +
                "flat into the included hard case.",
            34_900, "Audio", 4.7, 8821,
        ),
        product(
            "RoboVac Sensei 9000", "🤖",
            "Maps your home. Minds the rugs.",
            "LiDAR navigation, 8,000Pa suction, automatic mop-pad lifting over carpet, " +
                "and a self-emptying dock that holds 60 days of dust. Per-room schedules " +
                "and no-go zones included.",
            79_900, "Tech", 4.6, 5310, originalPriceCents = 99_900,
        ),
        product(
            "Mechanical Keyboard, Extra Clacky", "⌨️",
            "Every keystroke, a small reward.",
            "Gasket-mounted case with factory-lubed linear switches, double-shot PBT " +
                "keycaps, and a hot-swappable PCB so the sound profile is yours to tune. " +
                "South-facing RGB, USB-C, 1,000Hz polling.",
            21_900, "Tech", 4.9, 15294,
        ),
        product(
            "8K Drone with Follow-Me Mode", "🛸",
            "Your life, from above.",
            "8K/30fps video on a 1-inch sensor, 34 minutes of flight time, " +
                "omnidirectional obstacle sensing, and a follow-me mode that keeps you " +
                "centered at up to 45km/h. Folds to the size of a water bottle.",
            64_900, "Tech", 4.5, 3107,
        ),
        product(
            "Cloud Sofa (3-Seater)", "🛋️",
            "Sink in. Stay a while.",
            "Feather-wrapped foam cushions on a kiln-dried hardwood frame, with a " +
                "performance fabric that shrugs off spills. Modular design splits into " +
                "three sections for narrow stairwells.",
            189_900, "Home", 4.8, 6642, originalPriceCents = 249_900,
        ),
        product(
            "Self-Watering Plant That Can't Die", "🪴",
            "Greenery for the forgetful.",
            "A potted golden pothos in a self-watering planter with a four-week " +
                "reservoir and a water-level window. Thrives in low light and forgives " +
                "missed weekends.",
            4_900, "Home", 4.9, 9210,
        ),
        product(
            "Artisan Candle: 'New Apartment Smell'", "🕯️",
            "Fresh paint, clean linen, possibility.",
            "Hand-poured soy wax with top notes of white tea, a cedar heart, and a " +
                "clean-linen base. 60-hour burn time, cotton wick, reusable amber jar.",
            3_200, "Home", 4.7, 4188,
        ),
        product(
            "Floor-to-Ceiling Bookshelf", "📚",
            "A wall that reads well.",
            "Solid oak uprights with adjustable shelves rated to 30kg each and anti-tip " +
                "wall anchors included. Flat-packs into three boxes; assembly takes an " +
                "afternoon and pays off for decades.",
            45_900, "Home", 4.6, 2871,
        ),
        product(
            "Espresso Machine, Barista-Grade", "☕",
            "Café-quality shots at home.",
            "Dual boiler with PID temperature control, 9-bar extraction through a " +
                "commercial 58mm portafilter, and a steam wand that microfoams like the " +
                "third-wave place around the corner.",
            89_900, "Kitchen", 4.8, 7754, originalPriceCents = 109_900,
        ),
        product(
            "Limited Drop Sneakers 'Phantom 1'", "👟",
            "The colorway everyone asks about.",
            "Full-grain leather upper on a cushioned cup sole, reflective lace tips, " +
                "and a numbered tongue tag. This release is limited to a single " +
                "production run.",
            27_900, "Fashion", 4.9, 18337, originalPriceCents = 39_900,
        ),
        product(
            "Cashmere Hoodie of Main Character Energy", "🧥",
            "Soft power.",
            "Grade-A Mongolian cashmere in a 12-gauge knit, with dropped shoulders and " +
                "a double-lined hood. Machine-washable on the wool cycle, somehow.",
            18_900, "Fashion", 4.7, 6029,
        ),
        product(
            "Vintage Watch, Old Money Edition", "⌚",
            "Quiet on the wrist, loud in the room.",
            "A 36mm automatic with a Swiss movement, domed sapphire crystal, and a " +
                "discreet date at six. Ships on a leather strap with a spare NATO band.",
            399_900, "Fashion", 4.8, 1543,
        ),
        product(
            "Sunglasses You'd Never Lose", "🕶️",
            "Polarized, weightless, findable.",
            "Polarized CR-39 lenses with full UV400 protection in a featherweight " +
                "acetate frame, plus a magnetic hard case that clips to any bag strap.",
            15_900, "Fashion", 4.6, 3390,
        ),
        product(
            "Midnight Tteokbokki Mega Set", "🍜",
            "Late-night spice, done right.",
            "Chewy rice cakes in a gochujang sauce that builds slowly, with fish " +
                "cakes, half-moon dumplings, and extra mozzarella. Serves two, or one " +
                "with intent.",
            2_400, "Snacks", 4.9, 22481,
        ),
        product(
            "Artisanal Fried Chicken Bucket", "🍗",
            "Double-fried. Twice as crisp.",
            "Eight pieces double-fried for a shatter-crisp crust, tossed in soy-garlic " +
                "or sweet-spicy glaze. Pickled radish included, as it must be.",
            3_100, "Snacks", 4.8, 31764, originalPriceCents = 3_900,
        ),
        product(
            "Emotional Support Cake (Whole)", "🍰",
            "Because some days need cake.",
            "A whole vanilla-cream celebration cake layered with fresh strawberries " +
                "and a light, not-too-sweet frosting. No occasion required.",
            5_600, "Snacks", 4.9, 12058,
        ),
        product(
            "Imported Snack Box: Mystery Edition", "🎁",
            "47 snacks. Zero repeats.",
            "A curated surprise box of 47 snacks from twelve countries — chips, " +
                "candies, biscuits, and at least one flavor you'll need to describe " +
                "to a friend afterward.",
            7_900, "Snacks", 4.7, 8146,
        ),
        product(
            "10-Step Skincare Ritual Kit", "🧴",
            "The full routine, bottled.",
            "Cleanser to cream in ten labeled steps: snail mucin essence, niacinamide " +
                "serum, centella toner, and a ceramide moisturizer, with a laminated " +
                "routine card for mornings and nights.",
            12_900, "Beauty", 4.8, 9931, originalPriceCents = 16_900,
        ),
        product(
            "Weighted Blanket, Anxiety-Rated", "🛌",
            "Twelve kilograms of calm.",
            "Glass-bead fill quilted into small pockets so the weight stays even, " +
                "under a removable bamboo cover that sleeps cool. Pick roughly 10% of " +
                "your body weight.",
            9_900, "Self-Care", 4.9, 14207,
        ),
        product(
            "Spa Day In A Box", "🧖",
            "The home spa, fully stocked.",
            "Four bath bombs, a jade roller, two clay masks, cucumber eye pads, and a " +
                "waffle-weave headband, packed in a keepsake box with a how-to card.",
            8_400, "Self-Care", 4.7, 5566,
        ),
        product(
            "Yoga Mat of Future Discipline", "🧘",
            "Grip that meets you halfway.",
            "A 6mm dual-layer mat with a moisture-wicking top, non-slip base, and " +
                "alignment lines printed end to end. Carry strap included.",
            6_900, "Fitness", 4.6, 7012,
        ),
        product(
            "Inflatable T-Rex Costume", "🦖",
            "The room changes when you arrive.",
            "Self-inflating in 60 seconds with a quiet battery fan; one size fits " +
                "1.5–1.9m. Machine-washable shell and surprisingly good visibility.",
            5_900, "Chaos", 4.9, 19877,
        ),
        product(
            "1,000 Live Ladybugs", "🐞",
            "Natural pest control, by the thousand.",
            "Live adult ladybugs for aphid control in gardens and greenhouses. " +
                "Release at dusk after watering for best retention. Yes, this is a " +
                "real category of commerce.",
            2_900, "Chaos", 4.8, 6203,
        ),
        product(
            "Medieval Sword (Decorative)", "⚔️",
            "Forged for the mantelpiece.",
            "Hand-forged high-carbon steel with a full tang, leather-wrapped grip, " +
                "and a carved oak display stand. Blunted display edge.",
            22_900, "Chaos", 4.7, 4419,
        ),
        product(
            "Brick (Premium)", "🧱",
            "It's a brick.",
            "A single kiln-fired clay brick, palm-sized and satisfyingly dense. " +
                "Doorstop, bookend, paperweight, statement.",
            1_900, "Chaos", 5.0, 27345, originalPriceCents = 2_900,
        ),
        product(
            "Tiny Hands (Pair)", "🤏",
            "Big laughs, small commitment.",
            "A pair of finger-puppet-sized hands for your hands. Sturdy PVC, fits " +
                "most fingers, ruins most meetings.",
            3_400, "Chaos", 4.9, 11932,
        ),

        // ---- Tech ----
        product(
            "Lumen 65\" QLED TV", "📺",
            "Cinema, wall-mounted.",
            "A 65-inch QLED panel with 120Hz refresh, Dolby Vision, and a " +
                "near-bezel-less frame. Game mode drops input lag under 10ms; the " +
                "one-cable stand keeps the console clutter behind the credenza.",
            99_900, "Tech", 4.7, 4521, originalPriceCents = 129_900,
        ),
        product(
            "PulseTrack Smartwatch 5", "⏱️",
            "Your wrist, but informed.",
            "Heart rate, sleep staging, and dual-band GPS in a 44mm aluminum case, " +
                "with a 10-day battery and 5ATM water resistance. Bands swap without " +
                "tools.",
            24_900, "Tech", 4.5, 6890,
        ),
        product(
            "PicoBeam Mini Projector", "📽️",
            "A 100-inch screen in a coat pocket.",
            "Native 1080p at 500 ANSI lumens with autofocus, auto keystone, and a " +
                "built-in speaker that's better than it has any right to be. Movie " +
                "night moves wherever you do.",
            39_900, "Tech", 4.4, 2310,
        ),
        product(
            "AuraPhone 17", "📱",
            "The flagship essentials, minus the Max.",
            "A 6.3-inch OLED at 120Hz, the same 200MP main camera as its bigger " +
                "sibling, and a battery that comfortably clears a day. Five finishes, " +
                "seven years of updates.",
            79_900, "Tech", 4.7, 9482,
        ),
        product(
            "AuraFold 3", "📲",
            "Unfolds into more screen than your tablet.",
            "A 7.8-inch folding inner display with a crease you stop noticing by day " +
                "two, app continuity across both screens, and a hinge rated for " +
                "400,000 folds.",
            179_900, "Tech", 4.5, 2071, originalPriceCents = 199_900,
        ),
        product(
            "Nimbus VR One", "🥽",
            "Presence, included.",
            "A standalone headset with dual 2.5K panels at 120Hz, inside-out " +
                "tracking, and touch controllers with finger sensing. Sets up in five " +
                "minutes, no PC required.",
            49_900, "Tech", 4.6, 5320,
        ),
        product(
            "FeatherBook 14", "💻",
            "All day, under a kilo.",
            "A 14-inch 2.8K display in a 990g magnesium chassis, 18 hours of " +
                "battery, and a keyboard people write home about. Fanless, so it " +
                "never makes a sound.",
            119_900, "Tech", 4.8, 4106, originalPriceCents = 139_900,
        ),
        product(
            "Spectra 27\" 5K Monitor", "🖥️",
            "Every pixel of the picture.",
            "A 27-inch 5K IPS panel with 99% DCI-P3 coverage and factory " +
                "calibration, plus one-cable USB-C with 90W charging for laptops.",
            109_900, "Tech", 4.7, 1843,
        ),
        product(
            "Vortex V90 Graphics Card, 16GB", "⚡",
            "Frames to spare.",
            "16GB of GDDR7 under a triple-fan cooler that idles silent and boosts " +
                "quiet. 4K gaming at high refresh, with dual encoders for streaming.",
            84_900, "Tech", 4.8, 3217,
        ),
        product(
            "Ridgeline 9 Desktop CPU", "🧠",
            "Sixteen cores, no waiting.",
            "Sixteen cores boosting to 5.6GHz with a generous cache that serves " +
                "gaming and rendering alike. Cooler sold separately.",
            44_900, "Tech", 4.9, 5630,
        ),
        product(
            "2TB NVMe SSD, Gen4", "💾",
            "Load screens, abbreviated.",
            "Sequential reads at 7,400MB/s, a five-year warranty, and a heatsink " +
                "low-profile enough for laptops and consoles alike.",
            14_900, "Tech", 4.8, 12490, originalPriceCents = 17_900,
        ),
        product(
            "GlideMaster Wireless Mouse", "🖱️",
            "Scrolls like silk, clicks like silence.",
            "An ergonomic shape with a free-spinning scroll wheel, silent switches, " +
                "and three months of battery per charge. Pairs to three devices.",
            7_900, "Tech", 4.7, 8865,
        ),

        // ---- Audio ----
        product(
            "NoiseGone Buds", "🎵",
            "The Pro sound, pocket-sized.",
            "Adaptive noise cancellation in 5-gram earbuds with multipoint, wireless " +
                "charging, and six hours per charge — thirty with the case.",
            19_900, "Audio", 4.6, 10412, originalPriceCents = 24_900,
        ),
        product(
            "Crescent Belt-Drive Turntable", "💿",
            "Side A, the proper way.",
            "A belt-drive deck with a carbon tonearm, pre-mounted moving-magnet " +
                "cartridge, and a switchable phono stage so it plays nice with any " +
                "amp. Anti-skate you can actually set.",
            34_900, "Audio", 4.7, 2964,
        ),
        product(
            "Walnut Bookshelf Speakers (Pair)", "🔊",
            "Small cabinets, honest sound.",
            "Two-way bookshelf speakers with silk dome tweeters and 5.25-inch woven " +
                "woofers in real walnut veneer. Front-ported for shelf-friendly " +
                "placement.",
            44_900, "Audio", 4.8, 1872,
        ),
        product(
            "Crescent Streaming Amplifier", "🎚️",
            "One box, whole system.",
            "An 80W-per-channel integrated amp with a built-in streamer, a phono " +
                "input for the turntable, and a volume knob with proper weight to it.",
            59_900, "Audio", 4.7, 1346,
        ),

        // ---- Gaming ----
        product(
            "Meteor MK-II Console", "🎮",
            "Launch night, every night.",
            "4K at up to 120fps, a 2TB SSD that ends storage anxiety, and cooling " +
                "you'll never hear over the soundtrack. Backward compatible with " +
                "your whole shelf.",
            49_900, "Gaming", 4.8, 21304,
        ),
        product(
            "Wisp Handheld", "📟",
            "Your library, couch optional.",
            "A 7-inch 90Hz OLED handheld with detachable grips, eight hours on a " +
                "charge for indies, and a kickstand made for tray tables.",
            34_900, "Gaming", 4.6, 8740,
        ),
        product(
            "Meteor Pro Controller", "🕹️",
            "Built for the long session.",
            "Hall-effect sticks that never drift, four remappable back paddles, " +
                "adjustable trigger stops, and 40 hours per charge.",
            6_900, "Gaming", 4.7, 13208,
        ),
        product(
            "Emberfall: Complete Edition", "🐉",
            "Two hundred hours, zero filler.",
            "The acclaimed open-world RPG with all three expansions: a hand-built " +
                "map, choices that follow you to the credits, and a photo mode that " +
                "fills your storage.",
            6_900, "Gaming", 4.9, 31876,
        ),
        product(
            "Turbo Kart Carnival", "🏎️",
            "Friendship-testing since lap one.",
            "A four-player couch kart racer with 32 tracks, unlockable vehicles of " +
                "escalating absurdity, and an item balance patched with suspicious " +
                "care.",
            5_900, "Gaming", 4.8, 18452,
        ),
        product(
            "Vortex 27\" 240Hz Monitor", "🖥️",
            "See them first.",
            "A 27-inch 1440p IPS panel at 240Hz with 1ms response and adaptive " +
                "sync, on a stand with real height and swivel range.",
            54_900, "Gaming", 4.7, 4317, originalPriceCents = 64_900,
        ),
        product(
            "Vortex Featherweight Gaming Mouse", "🖱️",
            "55 grams, zero excuses.",
            "A 55g wireless mouse with a flagship optical sensor, optical switches, " +
                "and 90 hours of battery. Grip tape in the box.",
            9_900, "Gaming", 4.8, 7626,
        ),
        product(
            "Vortex 16 Gaming Laptop", "💻",
            "Desktop frames, backpack form.",
            "A 16-inch 240Hz display driven by the V90 mobile GPU, a vapor-chamber " +
                "cooler that keeps its composure, and per-key RGB you can also just " +
                "turn off.",
            189_900, "Gaming", 4.6, 2954,
        ),

        // ---- Home ----
        product(
            "Linen Duvet Set, Hotel Weight", "🛏️",
            "The good-hotel feeling, nightly.",
            "Stonewashed French linen duvet cover and two shams, pre-softened and " +
                "breathable in every season. Corner ties that actually hold the duvet " +
                "in place.",
            17_900, "Home", 4.8, 3754,
        ),
        product(
            "AirLoom Tower Purifier", "🌬️",
            "Quietly clears the room.",
            "True HEPA H13 filtration for rooms up to 50m², a 19dB night mode, and a " +
                "filter reminder that does the remembering for you. Air quality readout " +
                "glows from across the room.",
            22_900, "Home", 4.6, 5102, originalPriceCents = 27_900,
        ),
        product(
            "Arc Floor Lamp, Brass", "💡",
            "Light that leans in.",
            "A 1.8m brushed-brass arc with a linen drum shade and a marble base that " +
                "stays put. Dims from reading-bright to evening-warm on a foot switch.",
            15_900, "Home", 4.7, 1893,
        ),
        product(
            "Ceramic Table Lamp, Dimmable", "🏮",
            "Pools of warm light.",
            "A hand-glazed ceramic base with a linen shade and a full-range dimmer " +
                "on the cord. Sized for sideboards and bedside tables.",
            8_900, "Home", 4.6, 2415,
        ),
        product(
            "Monstera Deliciosa, Large", "🍃",
            "The houseplant with presence.",
            "A 90cm monstera in a nursery pot, leaves already split. Likes bright " +
                "corners and forgives average ones.",
            6_900, "Home", 4.8, 5217,
        ),
        product(
            "Snake Plant, Low-Light", "🌱",
            "Thrives on neglect.",
            "A 60cm sansevieria that tolerates dim rooms, missed waterings, and " +
                "general indifference. Filters the air while you ignore it.",
            3_400, "Home", 4.9, 8814,
        ),
        product(
            "Windowsill Herb Garden Kit", "🌾",
            "Basil within arm's reach.",
            "Three ceramic pots, soil discs, and seed packets for basil, mint, and " +
                "chives, with a drip tray that protects the sill.",
            4_900, "Home", 4.5, 4076,
        ),
        product(
            "Hand-Loomed Wool Rug, 160×230", "🧶",
            "The room, pulled together.",
            "Hand-loomed New Zealand wool in a flat weave that hides crumbs and " +
                "survives chair legs. Naturally stain-resistant; stops shedding " +
                "after week one.",
            39_900, "Home", 4.7, 1932, originalPriceCents = 49_900,
        ),
        product(
            "Full-Length Oak Mirror", "🪞",
            "The last look before leaving.",
            "A 165cm solid oak frame that leans against the wall or mounts to it, " +
                "with bevel-edged glass and felt backing.",
            19_900, "Home", 4.8, 1567,
        ),

        // ---- Kitchen ----
        product(
            "CrispWave Air Fryer XL", "🍟",
            "Crunch without the oil.",
            "A 5.5L family-size basket, 80–200°C range, dishwasher-safe parts, and " +
                "eight presets. Fries, wings, and roast vegetables that actually crisp.",
            11_900, "Kitchen", 4.7, 18204, originalPriceCents = 14_900,
        ),
        product(
            "Cast Iron Skillet, Pre-Seasoned", "🍳",
            "The last pan you'll buy.",
            "10.25 inches of pre-seasoned cast iron, oven-safe to 260°C. Sears, " +
                "bakes, fries, and gets better every year you own it.",
            4_400, "Kitchen", 4.9, 24871,
        ),
        product(
            "Santoku Chef's Knife, 67-Layer", "🔪",
            "One knife for nearly everything.",
            "A VG-10 steel core in 67 layers of Damascus cladding, an 18cm blade, and " +
                "a walnut handle balanced for long prep sessions. Arrives scary-sharp.",
            13_900, "Kitchen", 4.8, 6755,
        ),
        product(
            "Cloud Rice Cooker, 6-Cup", "🍚",
            "Perfect rice, zero attention.",
            "Induction heating with fuzzy-logic programs for white, brown, and " +
                "porridge, a 24-hour keep-warm, and a delay timer for waking up to " +
                "breakfast already made.",
            18_900, "Kitchen", 4.9, 11240,
        ),
        product(
            "Stand Mixer, Bakery Tier", "🎂",
            "Weekend bakery, weekday counter.",
            "A 500W direct-drive motor with a 4.8L stainless bowl, dough hook, whisk, " +
                "and paddle. Ten speeds from a gentle fold to a stiff-peak whip.",
            34_900, "Kitchen", 4.8, 8412, originalPriceCents = 42_900,
        ),
        product(
            "Cold Brew Tower, Slow Drip", "🧊",
            "Eight hours well spent.",
            "A glass slow-drip tower that turns 80g of grounds into 600ml of smooth " +
                "concentrate overnight. Adjustable drip valve, sustainably sourced " +
                "wood frame.",
            8_900, "Kitchen", 4.5, 1576,
        ),
        product(
            "Petty Prep Knife, 12cm", "🔪",
            "The santoku's quick little sibling.",
            "A 12cm utility blade for shallots, garlic, and everything fiddly, in " +
                "the same VG-10 steel and walnut handle as our santoku.",
            5_900, "Kitchen", 4.8, 3122,
        ),
        product(
            "Whetstone, 1000/6000 Grit", "🪨",
            "Sharp is a habit.",
            "A dual-grit water stone on a non-slip bamboo base, with an angle guide " +
                "for beginners. Five minutes a month keeps every edge honest.",
            3_900, "Kitchen", 4.7, 5489,
        ),

        // ---- Fashion ----
        product(
            "Water-Repellent Trench, City Cut", "🌂",
            "Rain, handled. Silhouette, kept.",
            "A double-breasted trench in water-repellent cotton twill with a " +
                "removable lining for three seasons of wear. Hits just below the knee.",
            21_900, "Fashion", 4.7, 2984,
        ),
        product(
            "Everyday Leather Tote", "👜",
            "Carries the whole day.",
            "Full-grain pebbled leather with a padded laptop sleeve, three interior " +
                "pockets, and a zip top. Ages into its own color story.",
            16_900, "Fashion", 4.8, 4366,
        ),
        product(
            "Heavyweight Tee, 3-Pack", "👕",
            "The good plain tee, finally.",
            "240gsm combed cotton, pre-shrunk, with a collar that holds its shape " +
                "past fifty washes. White, black, and stone.",
            5_900, "Fashion", 4.6, 13207,
        ),

        // ---- Beauty ----
        product(
            "Glass Glow Vitamin C Serum", "✨",
            "Brighter by breakfast.",
            "15% stabilized vitamin C with ferulic acid and hyaluronic acid in an " +
                "airless amber pump. A morning staple that sits well under sunscreen.",
            3_900, "Beauty", 4.6, 9341,
        ),
        product(
            "Velvet Cushion Foundation SPF50", "🪞",
            "Skin, but on its best day.",
            "A second-skin cushion compact with SPF50+ PA++++, buildable from sheer " +
                "to medium, in sixteen shades. Refill puck included.",
            4_400, "Beauty", 4.7, 12658, originalPriceCents = 5_400,
        ),
        product(
            "Lip Tint Trio, Silk Finish", "💄",
            "Three moods per pocket.",
            "Weightless water tints in rosewood, fig, and chili, with a blurred silk " +
                "finish that survives a latte.",
            2_900, "Beauty", 4.5, 7820,
        ),
        product(
            "Overnight Sheet Mask, 30-Pack", "🌙",
            "While-you-sleep hydration.",
            "Thirty cellulose sheet masks soaked in hyaluronic acid and birch sap, " +
                "individually sealed. One a night keeps the dry season civil.",
            3_400, "Beauty", 4.6, 5934,
        ),
        product(
            "IonPro Hair Dryer", "💨",
            "Salon speed, bedroom outlet.",
            "A 110,000rpm brushless motor dries in half the time at half the noise, " +
                "with ionic care that cuts frizz and three magnetic styling nozzles.",
            32_900, "Beauty", 4.8, 6147, originalPriceCents = 39_900,
        ),
        product(
            "Eau de Quiet, 50ml", "🌸",
            "Worn close, noticed closer.",
            "A skin-scent of white musk, pear, and cedar that stays within arm's " +
                "reach. Eight hours of wear; lingers politely.",
            11_900, "Beauty", 4.7, 3289,
        ),

        // ---- Self-Care ----
        product(
            "Stoneware Aroma Diffuser", "🪔",
            "Set the room to calm.",
            "Ultrasonic mist through a matte stoneware shell, an 8-hour runtime with " +
                "auto-off, and a warm-light mode for evenings. Two essential oil " +
                "blends included.",
            6_400, "Self-Care", 4.7, 4458,
        ),
        product(
            "Mulberry Silk Sleep Mask", "😴",
            "Lights out, properly.",
            "22-momme mulberry silk with a contoured fit that blocks light without " +
                "pressing on your lashes. Adjustable strap, matching travel pouch.",
            2_700, "Self-Care", 4.8, 8112,
        ),
        product(
            "Drift White Noise Machine", "🔉",
            "A softer way to end the day.",
            "Twenty non-looping soundscapes from steady rain to brown noise, a " +
                "sunset-dim display, and a one-button bedtime routine.",
            4_900, "Self-Care", 4.6, 6890,
        ),
        product(
            "Acupressure Mat & Pillow Set", "🌵",
            "Twenty minutes of useful ouch.",
            "6,210 stimulation points across mat and pillow, with a linen cover and " +
                "coconut-fiber core. Start with a t-shirt on; graduate without it.",
            5_400, "Self-Care", 4.5, 3741,
        ),
        product(
            "Dead Sea Bath Salt Trio", "🛁",
            "Draw the good kind of bath.",
            "Lavender, eucalyptus, and unscented mineral salts in three 500g jars " +
                "with a wooden scoop. Dissolves fast, drains clean.",
            3_600, "Self-Care", 4.7, 2954,
        ),

        // ---- Fitness ----
        product(
            "Adjustable Dumbbells, 2–24kg", "🏋️",
            "A rack in two handles.",
            "Dial-select plates swap weights in seconds, replacing fifteen pairs of " +
                "dumbbells in one cradle's footprint. Knurled steel grip.",
            29_900, "Fitness", 4.8, 5371, originalPriceCents = 34_900,
        ),
        product(
            "Percussive Massage Gun Mini", "💆",
            "Recovery, pocket-sized.",
            "Four heads, three speeds, 40dB quiet, and a 10-hour battery in a 1.1kg " +
                "body. The case fits carry-on luggage rules.",
            12_900, "Fitness", 4.6, 7204,
        ),
        product(
            "CloudStride Running Shoes", "🏃",
            "Kilometers feel shorter.",
            "A nitrogen-infused midsole with an 8mm drop, engineered mesh upper, and " +
                "outsole rubber rated for 800km of road.",
            13_900, "Fitness", 4.7, 9866,
        ),
        product(
            "Smart Jump Rope", "🤸",
            "Counts so you don't.",
            "Ball-bearing handles with a magnetic counter tracking jumps, time, and " +
                "calories, plus a ropeless mode for low ceilings.",
            3_400, "Fitness", 4.4, 4109,
        ),
        product(
            "Resistance Band Set, 5 Tiers", "🪢",
            "A gym that fits in a drawer.",
            "Five latex bands from feather to formidable, with cushioned handles, " +
                "ankle straps, and a door anchor. Carry bag included.",
            2_900, "Fitness", 4.6, 11473,
        ),
        product(
            "Deep-Tissue Foam Roller", "🌀",
            "Tomorrow's legs will thank you.",
            "High-density EVA with a ridged surface for calves, quads, and the upper " +
                "back. 45cm long, supports up to 150kg.",
            2_400, "Fitness", 4.7, 8541,
        ),

        // ---- Snacks ----
        product(
            "Honey Butter Chip Case (12 Bags)", "🍯",
            "Sweet, salty, gone.",
            "Twelve full-size bags of the honey-butter chips that once caused a " +
                "national shortage. Stock up accordingly.",
            3_900, "Snacks", 4.8, 16234,
        ),
        product(
            "Ramyeon Pantry Box, 20-Pack", "🍲",
            "Twenty nights, sorted.",
            "Twenty packs across mild, spicy, and extra-spicy tiers, including two " +
                "limited editions. A pantry that plans ahead.",
            3_200, "Snacks", 4.7, 21098,
        ),
        product(
            "Mochi Assortment, 18 Pieces", "🍡",
            "Soft, softer, softest.",
            "Eighteen daifuku across matcha, black sesame, strawberry, and red bean, " +
                "individually wrapped so the box lasts longer than one evening. " +
                "Theoretically.",
            2_800, "Snacks", 4.8, 7456,
        ),
        product(
            "Korean Corn Dog Night Kit", "🌭",
            "Crunchy outside, cheese-pull inside.",
            "Everything for six mozzarella corn dogs at home: batter mix, panko, " +
                "sticks, and the sugar for the controversial-but-correct finish.",
            2_600, "Snacks", 4.6, 5872, originalPriceCents = 3_200,
        ),

        // ---- Outdoors ----
        product(
            "One-Touch Pop Tent, 4-Person", "⛺",
            "Camp set up before the kettle boils.",
            "Umbrella-frame pitch in under a minute, a 3,000mm waterproof fly, two " +
                "doors, and blackout lining for sleeping past sunrise.",
            18_900, "Outdoors", 4.7, 3982, originalPriceCents = 23_900,
        ),
        product(
            "Lowback Camp Chair", "🪑",
            "The best seat outside the house.",
            "An aluminum frame at 1.1kg that packs to bottle size and holds 120kg. " +
                "Mesh panels for summer, cup holder for always.",
            6_900, "Outdoors", 4.6, 6233,
        ),
        product(
            "Titanium Cook Set, 3-Piece", "🍵",
            "Boils fast, weighs nothing.",
            "A 750ml pot, 400ml cup, and folding spork at 285g all-in, nesting into " +
                "a mesh bag. Handles stay cool off the flame.",
            8_900, "Outdoors", 4.8, 2147,
        ),
        product(
            "Three-Season Down Sleeping Bag", "💤",
            "Warm to minus seven.",
            "650-fill duck down comfort-rated to -7°C, with a draft collar and a " +
                "two-way zip for venting. Compresses to four liters.",
            13_900, "Outdoors", 4.7, 3318,
        ),
        product(
            "Rechargeable Headlamp, 400lm", "🔦",
            "Hands-free until sunrise.",
            "400 lumens with flood and spot modes, red night vision, USB-C charging, " +
                "and IPX6 rain-proofing — at 50 grams.",
            3_400, "Outdoors", 4.6, 7726,
        ),
        product(
            "Parkside Hammock, Double", "🌳",
            "Two trees from a nap.",
            "Ripstop nylon rated to 230kg with tree-friendly straps included. Sets up " +
                "in 90 seconds, packs down to grapefruit size.",
            4_900, "Outdoors", 4.8, 5490,
        ),
        product(
            "Frostkeep 28L Cooler", "❄️",
            "Ice for days. Plural.",
            "Rotomolded walls hold ice up to four days, with bear-resistant latches " +
                "and a built-in bottle opener. Drains without tipping.",
            21_900, "Outdoors", 4.7, 1865, originalPriceCents = 24_900,
        ),

        // ---- Pets ----
        product(
            "Floor-to-Ceiling Cat Tower", "🐈",
            "Vertical territory, claimed.",
            "A tension-mounted floor-to-ceiling pole with five sisal-wrapped perches " +
                "and a hideaway pod. Rated for two determined cats.",
            13_900, "Pets", 4.7, 4106,
        ),
        product(
            "Auto Feeder with Portion Control", "🍽️",
            "Dinner at six, even when you're late.",
            "Schedules up to six meals a day in gram-level portions from a 4L sealed " +
                "hopper, with a battery backup for power cuts.",
            8_900, "Pets", 4.6, 6678, originalPriceCents = 10_900,
        ),
        product(
            "Whisper Pet Water Fountain", "⛲",
            "Fresh water, always moving.",
            "2.5L of triple-filtered circulation at under 30dB, a dishwasher-safe " +
                "stainless tray, and a low-water auto shutoff.",
            4_400, "Pets", 4.5, 8923,
        ),
        product(
            "Orthopedic Dog Bed, L", "🐶",
            "Old joints, new mornings.",
            "A 10cm memory-foam base with a bolstered rim, a washable microsuede " +
                "cover, and a non-slip bottom. For dogs up to 40kg.",
            9_900, "Pets", 4.8, 5217,
        ),
        product(
            "PetWatch Treat Camera", "📷",
            "Check in. Toss a treat.",
            "1080p wide-angle with night vision, two-way audio, bark alerts, and a " +
                "treat launcher with adjustable distance.",
            12_900, "Pets", 4.5, 3754, originalPriceCents = 15_900,
        ),
        product(
            "All-Weather Dog Raincoat", "🦮",
            "Walks happen regardless.",
            "A waterproof shell with a fleece lining, leash port, reflective trim, " +
                "and a high collar. Five sizes, dachshund cut available.",
            3_400, "Pets", 4.6, 4488,
        ),
        product(
            "Catnip Kicker, Organic", "🌿",
            "The good stuff, ethically grown.",
            "A 30cm kicker stuffed with organically grown catnip and crinkle fill, " +
                "double-stitched for committed rabbit-kicks.",
            1_600, "Pets", 4.9, 9035,
        ),

        // ---- Hobbies ----
        product(
            "1,000-Piece Puzzle: Night Market", "🧩",
            "One table, three weekends.",
            "A 1,000-piece illustrated night market scene with an anti-glare matte " +
                "finish and a poster-size reference. Pieces snap in with a " +
                "satisfying click.",
            2_400, "Hobbies", 4.8, 6651,
        ),
        product(
            "Acrylic Paint Studio Set", "🎨",
            "Start before you're ready.",
            "24 colors, five brushes, a canvas pad, and a mixing guide pitched at " +
                "absolute beginners onward. Water-based, easy cleanup.",
            4_400, "Hobbies", 4.7, 4892,
        ),
        product(
            "Mecha Model Kit, 1/100 Scale", "🦾",
            "An evening of clean assembly.",
            "Over 200 snap-fit parts with no glue or paint required, articulated " +
                "joints, and a display stand. Nippers recommended, patience supplied " +
                "by the process.",
            4_900, "Hobbies", 4.9, 7340,
        ),
        product(
            "Concert Ukulele, Mahogany", "🎸",
            "Four strings, fast friends.",
            "A mahogany body with smooth geared tuners, a padded gig bag, and a " +
                "chord chart that gets you to three songs tonight.",
            7_900, "Hobbies", 4.6, 3127,
        ),
        product(
            "Instant Camera, Retro Body", "📸",
            "One shot, one print, no edits.",
            "An auto-exposure instant film camera with a selfie mirror and a " +
                "double-exposure mode. Prints develop in your hand in 90 seconds.",
            8_900, "Hobbies", 4.7, 5563, originalPriceCents = 9_900,
        ),
        product(
            "Tournament Chess Set, Weighted", "♟️",
            "The long game, beautifully made.",
            "Triple-weighted boxwood and sheesham pieces on a folding walnut board, " +
                "with felted bases and a second queen for each side.",
            6_400, "Hobbies", 4.8, 2218,
        ),
        product(
            "Embroidery Starter Kit", "🪡",
            "Slow stitches, quiet evenings.",
            "Three pre-printed hoops, 24 thread colors, needles, and a stitch guide " +
                "that takes you from backstitch to french knots.",
            2_700, "Hobbies", 4.7, 3905,
        ),
        product(
            "LayerForge One 3D Printer", "🖨️",
            "From file to object, overnight.",
            "Auto bed leveling, 250mm/s print speeds, and an enclosed chamber that " +
                "keeps drafts off your prints. Quiet enough to share a bookshelf with.",
            27_900, "Hobbies", 4.7, 4521, originalPriceCents = 32_900,
        ),
        product(
            "PLA Filament Set, 4×1kg", "🧵",
            "Four spools, endless prototypes.",
            "Matte PLA in white, black, sage, and terracotta, vacuum-sealed with " +
                "desiccant at ±0.02mm tolerance. Tangle-free winding, genuinely.",
            7_900, "Hobbies", 4.8, 6973,
        ),

        // ---- Stationery ----
        product(
            "Brass Fountain Pen, Fine Nib", "🖋️",
            "Writing you can feel.",
            "A machined brass body that patinas with use, a German fine nib, and a " +
                "converter included for bottled ink.",
            6_900, "Stationery", 4.8, 2871,
        ),
        product(
            "Gel Pen Set, 36 Colors", "🖊️",
            "Every note, color-coded.",
            "36 quick-dry gel inks at 0.5mm in a stand-up case, from highlighter " +
                "brights to muted pastels.",
            1_900, "Stationery", 4.7, 10982,
        ),
        product(
            "Undated Weekly Planner", "📅",
            "Start any Monday.",
            "52 undated weekly spreads with habit trackers, monthly reviews, and " +
                "lay-flat binding. The 120gsm paper takes fountain pen ink without " +
                "bleed.",
            2_900, "Stationery", 4.6, 7448,
        ),
        product(
            "Washi Tape Archive, 20 Rolls", "🎀",
            "Decorate the margins.",
            "Twenty patterns from grid-minimal to botanical, ten meters each, " +
                "repositionable on most surfaces.",
            2_200, "Stationery", 4.8, 5126,
        ),
        product(
            "Leather Desk Pad, 80cm", "🗒️",
            "A desk that feels finished.",
            "Vegetable-tanned leather over a non-slip base, 80×40cm, with stitched " +
                "edges. Develops character with every meeting survived.",
            5_900, "Stationery", 4.7, 1654,
        ),
        product(
            "Sticker Sheet Mega Pack", "🌟",
            "200 small joys.",
            "Two hundred matte vinyl stickers across food, plants, and weather " +
                "moods. Water-resistant, laptop-grade adhesive.",
            1_400, "Stationery", 4.6, 8830,
        ),
        product(
            "Linen Journal, Dot Grid", "📓",
            "Blank pages, good intentions.",
            "192 dot-grid pages of 100gsm cream paper in a linen hardcover, with a " +
                "ribbon marker and an expandable back pocket.",
            2_400, "Stationery", 4.8, 6042,
        ),

        // ---- Chaos ----
        product(
            "Googly Eyes, 500 Assorted", "👀",
            "Everything is funnier with eyes.",
            "Five hundred self-adhesive googly eyes from 6mm to 40mm. Apply " +
                "responsibly, or don't.",
            1_200, "Chaos", 4.9, 14209,
        ),
        product(
            "A Single Potato, Gift-Wrapped", "🥔",
            "Say it with a potato.",
            "One premium russet, hand-wrapped in kraft paper and twine, with a blank " +
                "gift tag for your message. It is exactly what it sounds like.",
            1_500, "Chaos", 4.8, 7621,
        ),
        product(
            "Mystery Box", "❓",
            "Contents: unknown. Delivery: known.",
            "A sealed box packed by someone sworn to secrecy. What was inside is " +
                "revealed only on arrival, which is to say: nothing, plus the idea " +
                "of something. The idea is genuinely random.",
            1_999, "Chaos", 4.9, 9477,
        ),

        // ---- Current-product expansion (appended; ids stay stable) ----
        // Tech — the gadgets people actually doom-scroll into carts right now.
        product(
            "Halo Smart Ring", "💍",
            "Your sleep score, on your finger.",
            "A titanium ring that tracks sleep stages, heart-rate variability, and " +
                "readiness, with a week of battery and no screen to distract you. " +
                "Sizing kit ships first; the ring follows. Neither, here.",
            29_900, "Tech", 4.6, 8124, originalPriceCents = 34_900,
        ),
        product(
            "Leaflet Paper E-Reader", "📖",
            "A whole library, glare-free.",
            "A 7-inch warm-light e-ink display that reads like paper in direct sun, " +
                "holds thousands of books, and goes weeks between charges. " +
                "Waterproof for the bath you'll take with it.",
            15_900, "Tech", 4.8, 14302,
        ),
        product(
            "Voltstack 1000 Power Station", "🔋",
            "Wall power, anywhere.",
            "A 1,024Wh LiFePO4 battery with a silent inverter, fast 0–80% recharge " +
                "in under an hour, and enough outlets to run a fridge, a laptop, and " +
                "your anxiety about the next outage.",
            79_900, "Tech", 4.7, 5230, originalPriceCents = 99_900,
        ),
        product(
            "Vista AI Glasses", "👓",
            "Look, capture, ask. Hands free.",
            "Classic frames with an open-ear speaker, a discreet 12MP camera, and an " +
                "assistant you talk to. Prescription-ready. Records the moment, " +
                "delivers the nothing.",
            29_900, "Tech", 4.4, 3611,
        ),
        product(
            "Apex Action Cam 6", "🎥",
            "5.3K, stabilized, unbreakable.",
            "Gimbal-smooth 5.3K video, waterproof to 10m without a case, and a front " +
                "screen for framing yourself. Mounts to everything you own. Films " +
                "adventures that remain hypothetical.",
            39_900, "Tech", 4.7, 9988,
        ),
        product(
            "Nexus Mesh Wi-Fi 7 (3-Pack)", "📡",
            "Dead zones, evicted.",
            "Tri-band Wi-Fi 7 blanketing up to 600m² with seamless handoff, a " +
                "six-stream backhaul, and an app that finally makes sense. No more " +
                "walking to the router to apologize to it.",
            34_900, "Tech", 4.6, 4177,
        ),
        product(
            "Slate E-Ink Notebook", "📝",
            "Paper that never runs out.",
            "A 10.3-inch e-ink tablet with a pen that feels like a pencil, infinite " +
                "notebooks, and zero notifications by design. Converts your scrawl " +
                "to text. Organizes the thoughts; ships none of them.",
            37_900, "Tech", 4.5, 2890, originalPriceCents = 44_900,
        ),
        product(
            "FindIt Trackers (4-Pack)", "📍",
            "Lose nothing. Find everything.",
            "Coin-sized trackers for keys, bags, and the remote, with a year-long " +
                "battery and a network of millions of phones quietly helping you " +
                "look. Ironically, these will never arrive to be lost.",
            9_900, "Tech", 4.5, 17640,
        ),
        product(
            "Volt 100W GaN Charger", "🔌",
            "One brick, every device.",
            "Gallium-nitride internals shrink a 100W four-port charger to the size of " +
                "a deck of cards — laptop, phone, tablet, and buds at once. The " +
                "cable nest in your bag remains, sadly, real.",
            5_900, "Tech", 4.8, 11233,
        ),
        product(
            "Spectra Go 16\" Portable Monitor", "🖥️",
            "A second screen that folds away.",
            "A 16-inch 1440p OLED that runs off a single USB-C cable, with a built-in " +
                "kickstand and a magnetic cover. Doubles your laptop anywhere. " +
                "Currently doubling your desire and nothing else.",
            22_900, "Tech", 4.6, 3052,
        ),

        // Audio — the speakers and buds that fill carts now.
        product(
            "Pebble Go Portable Speaker", "🔈",
            "Pocket-sized, room-filling.",
            "A palm-sized Bluetooth speaker with surprising low end, IP67 dust and " +
                "water proofing, 14 hours of playback, and a strap for the shower " +
                "rail. Pairs two for stereo. Plays nothing, beautifully.",
            12_900, "Audio", 4.7, 16842, originalPriceCents = 15_900,
        ),
        product(
            "Crescent Cinema Soundbar 5.1", "📻",
            "Your TV finally sounds like the movie.",
            "A 5.1 Dolby Atmos bar with a wireless subwoofer and up-firing drivers " +
                "that bounce sound off the ceiling. One cable to the TV, one app for " +
                "the rest. The explosions are immersive and imaginary.",
            49_900, "Audio", 4.8, 4521, originalPriceCents = 59_900,
        ),
        product(
            "Crescent Reference IEMs", "🎶",
            "Studio detail, in your ears.",
            "Triple balanced-armature in-ear monitors with a detachable braided " +
                "cable and foam tips that vanish into your ears. Hears every layer " +
                "of the mix. Delivers every layer of nothing.",
            17_900, "Audio", 4.6, 2733,
        ),
        product(
            "OpenRun Bone-Conduction Headphones", "🦴",
            "Music and the world, at once.",
            "Open-ear bone-conduction headphones that leave your ears free for " +
                "traffic and conversation, sweat-proof for runs, with an eight-hour " +
                "battery. Built for the marathon you're thinking about.",
            15_900, "Audio", 4.5, 6390,
        ),
        product(
            "BoomTower 200W Party Speaker", "🔊",
            "The neighbors will remember this.",
            "A 200W floor speaker with a thumping woofer, a light show synced to the " +
                "beat, a karaoke mic input, and a battery that outlasts the party. " +
                "Wheels included. Regret not included.",
            29_900, "Audio", 4.6, 5118,
        ),

        // Gaming — the gear and titles in everyone's cart this year.
        product(
            "Pocket Arcade Retro Handheld", "🕹️",
            "Forty years of games, one pocket.",
            "A clamshell handheld with a crisp 4-inch screen, clicky shoulder " +
                "buttons, and enough power for the golden age of consoles. Twenty " +
                "hours a charge. Nostalgia ships immediately; the device does not.",
            13_900, "Gaming", 4.7, 11204, originalPriceCents = 16_900,
        ),
        product(
            "Vortex Force Racing Wheel & Pedals", "🏎️",
            "Feel every apex.",
            "A direct-drive wheel with real force feedback, a metal pedal set with a " +
                "load-cell brake, and a quick-release rim. Clamps to any desk. The " +
                "podium remains, for now, theoretical.",
            44_900, "Gaming", 4.8, 3877,
        ),
        product(
            "Vortex Throne Gaming Chair", "🪑",
            "Built for the twelve-hour session.",
            "A high-back ergonomic chair with 4D armrests, a magnetic lumbar pillow, " +
                "and cold-cure foam that survives marathons. Reclines to nap. " +
                "Assembles into the throne your posture deserves.",
            32_900, "Gaming", 4.6, 7740, originalPriceCents = 39_900,
        ),
        product(
            "Vortex Stream Capture 4K", "🎙️",
            "Go live without the dropped frames.",
            "A pass-through capture card recording 4K60 HDR with near-zero latency " +
                "over USB-C. Plug-and-play with the streaming apps. Broadcasts your " +
                "gameplay to an audience as real as the gameplay.",
            17_900, "Gaming", 4.5, 4012,
        ),
        product(
            "Hearthvale", "🌾",
            "Inherit a farm. Mend a town. Breathe.",
            "A cozy farming-and-life sim where you grow crops, befriend a village, " +
                "fish at dawn, and never once check your email. Hundreds of gentle " +
                "hours. The calm is real even when the harvest isn't.",
            3_400, "Gaming", 4.9, 41208,
        ),
        product(
            "Skybound Survivors", "🪓",
            "Build, explore, survive, repeat.",
            "A co-op survival-craft adventure across floating islands: chop, mine, " +
                "tame beasts, and raise a base with up to ten friends. Procedurally " +
                "endless. Bonds forged here outlast the nothing that arrives.",
            5_900, "Gaming", 4.7, 28533,
        ),

        // Kitchen — the viral countertop wave.
        product(
            "Quench 40oz Tumbler", "🥤",
            "Hydration as a personality.",
            "A 40oz vacuum-insulated stainless tumbler that keeps ice for 24 hours, " +
                "fits any cupholder, and comes in a color you'll defend to friends. " +
                "Handle, straw, and a faint sense of belonging included.",
            4_500, "Kitchen", 4.8, 38716, originalPriceCents = 5_500,
        ),
        product(
            "Frostbite Nugget Ice Maker", "🧊",
            "The good ice. The chewable ice.",
            "A countertop maker that produces a pound of soft, chewable nugget ice " +
                "in fifteen minutes and keeps it coming. Self-cleaning, side-tank " +
                "fill. The ice you'd switch dentists for.",
            34_900, "Kitchen", 4.7, 12905,
        ),
        product(
            "Vesuvio Portable Pizza Oven", "🍕",
            "900°F. Sixty-second Neapolitan.",
            "A gas-fired stone-floor oven that hits pizzeria temperatures on a " +
                "balcony and bakes a leopard-spotted crust in a minute. Folds flat " +
                "for storage. The first burnt pie is a rite you'll skip.",
            29_900, "Kitchen", 4.8, 6633, originalPriceCents = 34_900,
        ),
        product(
            "Cyclone Pro Blender", "🥤",
            "Pulverizes everything but your resolve.",
            "A 1,500W blender with aircraft-grade blades that turn frozen fruit, " +
                "nuts, and ice into silk, plus presets for smoothies, soups, and " +
                "nut butter. Loud enough to wake intentions. Quiet on delivery.",
            19_900, "Kitchen", 4.7, 9844,
        ),
        product(
            "Fizz Sparkling Water Maker", "🫧",
            "Bubbles on tap, bottles in the bin.",
            "Carbonates tap water in seconds with a twist, dialing fizz from gentle " +
                "to aggressive, on a CO2 cylinder that lasts months. Saves a " +
                "mountain of plastic and, here, a mountain of money.",
            8_900, "Kitchen", 4.6, 14077,
        ),
        product(
            "Gooseneck Pour-Over Kettle", "☕",
            "Precision for the morning ritual.",
            "A variable-temperature electric kettle with a counterweighted gooseneck " +
                "spout for a controlled pour, a 60-minute hold, and a built-in " +
                "timer. Your coffee phase, fully equipped, indefinitely deferred.",
            9_900, "Kitchen", 4.8, 7320, originalPriceCents = 12_900,
        ),

        // Beauty — the routine upgrades all over your feed.
        product(
            "Lumi LED Face Mask", "💆",
            "Salon light therapy at home.",
            "A flexible silicone mask with red and near-infrared LEDs for a " +
                "ten-minute glow session, clinically the kind of thing influencers " +
                "swear by. Hands-free, cordless. The eerie glow is the whole vibe.",
            27_900, "Beauty", 4.6, 8455, originalPriceCents = 34_900,
        ),
        product(
            "AirStyle Multi-Styler", "💨",
            "Dry, curl, and smooth — no extreme heat.",
            "A styling wand that curls with airflow instead of scorching plates, " +
                "with magnetic attachments for waves, volume, and a fast blow-dry. " +
                "The viral one. Transforms your hair, leaves your wallet untouched.",
            44_900, "Beauty", 4.7, 15602,
        ),
        product(
            "Jade Gua Sha Sculpt Set", "🪨",
            "The five-minute facial massage.",
            "A genuine jade gua sha stone and a dual-ended roller for a morning " +
                "de-puff and a nightly wind-down, with a guide card of the strokes. " +
                "Cool to the touch, calming by design.",
            2_900, "Beauty", 4.5, 6188,
        ),
        product(
            "Plumping Lip Oil", "💋",
            "Gloss that actually cares.",
            "A non-sticky tinted lip oil with hyaluronic acid and a peppermint " +
                "tingle that leaves lips fuller and glassy. Five sheer shades. The " +
                "one in everyone's bag this season.",
            2_400, "Beauty", 4.6, 19744,
        ),
        product(
            "Invisible SPF50 Serum", "🧴",
            "The sunscreen step you'll actually keep.",
            "A weightless SPF50 PA++++ serum that vanishes with no white cast and no " +
                "pilling under makeup, packed with niacinamide. The single best " +
                "anti-aging habit, bottled. Protects skin; spares savings.",
            3_400, "Beauty", 4.8, 22019,
        ),

        // Self-Care — the at-home recovery and wind-down wave.
        product(
            "Glow Red Light Panel", "🔴",
            "Ten minutes of warm, red calm.",
            "A panel of red and near-infrared light for a daily session said to ease " +
                "tired muscles and skin, on a stand or wall mount with a built-in " +
                "timer. The glow you bask in. Recovery, plugged in.",
            18_900, "Self-Care", 4.5, 5277,
        ),
        product(
            "Dawn Sunrise Alarm Clock", "🌅",
            "Wake to light, not a jolt.",
            "A bedside lamp that brightens like a real sunrise over thirty minutes " +
                "and eases you down at night, with gentle soundscapes and no blue " +
                "light. Mornings stop being an ambush. Stays on your nightstand of " +
                "the mind.",
            6_900, "Self-Care", 4.7, 9460, originalPriceCents = 8_900,
        ),
        product(
            "Knead Shiatsu Neck Massager", "💆",
            "Unknots the day in ten minutes.",
            "A U-shaped massager with rotating heated nodes that drape over your neck " +
                "and shoulders, hands-free, with adjustable intensity. The desk-job " +
                "antidote. Kneads away tension; adds none to your balance.",
            5_900, "Self-Care", 4.6, 13288,
        ),
        product(
            "Heated Eye Massager", "😌",
            "A spa mask for screen-tired eyes.",
            "A cordless mask with gentle heat, air-pressure kneading, and calming " +
                "tracks for a fifteen-minute reset before sleep. Folds flat for " +
                "travel. Closes the eyes; opens nothing in your cart but joy.",
            4_400, "Self-Care", 4.5, 7012,
        ),
        product(
            "Infrared Sauna Blanket", "♨️",
            "A sauna you wrap yourself in.",
            "A far-infrared blanket that delivers a deep, sweaty session on your own " +
                "couch, with a waterproof inner layer and a simple controller. The " +
                "wellness ritual without the gym membership — or the membership fee.",
            29_900, "Self-Care", 4.4, 3905, originalPriceCents = 39_900,
        ),
    )

    fun byId(id: Int): Product? = products.firstOrNull { it.id == id }

    /** The one product whose unboxing reveals what would have been inside. */
    val mysteryBox: Product = products.first { it.name == "Mystery Box" }

    /**
     * What the Mystery Box hypothetically contained: an orderId-seeded catalog
     * pick, stable per order so revisits keep the same answer, never the box
     * itself. Decorative randomness — it gates nothing and costs nothing.
     */
    fun mysteryRevealFor(orderId: Int): Product {
        val candidates = products.filterNot { it.id == mysteryBox.id }
        return candidates[(orderId * 37 + 11) % candidates.size]
    }

    /** Products eligible for the rotating flash deal slot. */
    val dealCandidates: List<Product> = products.filter { it.originalPriceCents != null }
}
