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
/** A fantasy card "pulled" from a delivered trading-card order. */
data class CardPull(
    val emoji: String,
    val name: String,
    val rarity: String,
    /** The one-line flavor text under the art — deadpan, like the listings. */
    val flavor: String = "",
    /**
     * The type line between art and text box, in the idiom of the real game
     * each invented one homages: Pocket Critters wears Pokémon stage lines
     * ("Basic Flame Critter"), Duelbound wears Yu-Gi-Oh brackets
     * ("[Spellcaster / Effect]"), Manaforge wears Magic's em-dash
     * ("Legendary Creature — Human Wizard").
     */
    val type: String = "",
    /**
     * The battle stat, only where its genre prints one: Pokémon HP for
     * Critters ("120 HP"), Yu-Gi-Oh ATK/DEF for Duelbound monsters
     * ("ATK/2400 DEF/2100"), Magic power/toughness for Manaforge creatures
     * ("3/4"). Spells, traps, relics and artifacts stay blank, like the
     * real ones.
     */
    val stat: String = "",
)

object FakeCatalog {

    val categories = listOf(
        "All", "Tech", "Audio", "Gaming", "Home", "Kitchen", "Fashion", "Beauty",
        "Self-Care", "Fitness", "Snacks", "Outdoors", "Pets", "Hobbies",
        "Trading Cards", "Stationery", "Chaos",
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
        "Trading Cards" to listOf(
            Review("Kenji T.", 5, "Pulled the chase card on my first imaginary pack. The odds love me here."),
            Review("Maddie R.", 5, "Mint condition forever. Nothing grades higher than a card that doesn't exist."),
            Review("Lukas B.", 4, "Sealed product never loses value if it never arrives. I'm an investor now."),
            Review("Aria S.", 5, "Completed the whole set in one evening. My binder is empty. Both feel true."),
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
        includes: List<String> = emptyList(),
        variantGroup: String? = null,
        variantLabel: String? = null,
        variantAxis: String = "Color",
    ): Product {
        val id = nextId++
        return Product(
            id, name, emoji, tagline, description, priceCents, category,
            rating, reviewCount, reviewsFor(id, category), originalPriceCents,
            includes = includes,
            variantGroup = variantGroup,
            variantLabel = variantLabel,
            variantAxis = variantAxis,
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
            variantGroup = "meteor-pro-controller", variantLabel = "Carbon Black",
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

        // Fitness — the home-setup gear trending right now.
        product(
            "StrideDesk Walking Pad", "🚶",
            "Ten thousand steps under your desk.",
            "A slim, foldable treadmill that slides under a standing desk and walks " +
                "you through the workday at a gentle pace, with a remote and quiet " +
                "motor. Steps you'd have taken anyway, now indoors and imaginary.",
            29_900, "Fitness", 4.6, 12877, originalPriceCents = 36_900,
        ),
        product(
            "PulseRecover Compression Boots", "🦵",
            "The athlete recovery, at home.",
            "Knee-high boots with sequential air compression that flush tired legs " +
                "after a long run or a long day, with multiple modes and a quiet " +
                "pump. Twenty minutes to fresh legs. Recovery from exertion not had.",
            34_900, "Fitness", 4.7, 4622,
        ),
        product(
            "ShiftBell Adjustable Kettlebell", "🏋️",
            "Six kettlebells in one.",
            "A single bell that dials from 5 to 18kg with a twist of the handle, " +
                "saving a rack of iron and a corner of the room. Smooth swings, " +
                "honest weight. The home gym you keep meaning to use.",
            14_900, "Fitness", 4.7, 6105,
        ),
        product(
            "BodyScan Smart Scale", "⚖️",
            "Weight is just the first number.",
            "A glass scale that reads body composition — fat, muscle, water, bone — " +
                "and syncs the trend to your phone so you watch the line, not the " +
                "day. Greets you each morning with data you didn't pay for.",
            5_900, "Fitness", 4.5, 15330,
        ),
        product(
            "Ruck Weighted Vest, 9kg", "🎽",
            "Turn a walk into a workout.",
            "A snug 9kg vest with evenly distributed plates and a no-bounce fit for " +
                "rucking, walks, and bodyweight days. The simplest way to make any " +
                "movement count. Adds load to your stride, not your statement.",
            8_900, "Fitness", 4.6, 5471, originalPriceCents = 10_900,
        ),

        // Snacks — the foods your feed won't stop showing you.
        product(
            "Freeze-Dried Candy Crunch Mix", "🍬",
            "Your favorite candy, now a crunchy puff.",
            "Classic chewy candies freeze-dried until they balloon into airy, " +
                "shatter-crisp puffs with the flavor turned all the way up. The " +
                "sound is half the fun. A whole bag, zero dentist visits.",
            1_900, "Snacks", 4.7, 18204,
        ),
        product(
            "Sichuan Chili Crisp Jar", "🌶️",
            "Put it on literally everything.",
            "Crunchy chili oil layered with garlic, shallot, and a numbing Sichuan " +
                "tingle — equally at home on dumplings, eggs, ice cream, or a spoon. " +
                "The jar that disappears in a week. This one disappears faster.",
            1_200, "Snacks", 4.9, 26611,
        ),
        product(
            "Hot Honey Drizzle", "🍯",
            "Sweet, then a slow burn.",
            "Wildflower honey infused with chili for the drizzle that makes pizza, " +
                "fried chicken, and cheese boards sing. Sticky, glossy, dangerously " +
                "versatile. Pairs with everything you're not actually eating.",
            1_400, "Snacks", 4.8, 11290,
        ),
        product(
            "Electrolyte Hydration Mix, 30-Stick", "⚡",
            "Water, but it finally works.",
            "Single-serve sticks with a high-sodium electrolyte ratio and no sugar, " +
                "for mornings, workouts, and the day after. Citrus-salty and weirdly " +
                "craveable. Hydrates the idea of you.",
            2_900, "Snacks", 4.6, 14855, originalPriceCents = 3_500,
        ),
        product(
            "Pistachio Kunafa Chocolate Bar", "🍫",
            "The viral bar, fully loaded.",
            "A thick chocolate bar stuffed with pistachio cream and crispy shredded " +
                "kunafa pastry — the one that broke the internet and sold out " +
                "everywhere. Snaps loud, eats rich. Sold out here too, technically.",
            2_200, "Snacks", 4.8, 33107,
        ),
        product(
            "Ceremonial Matcha Starter Kit", "🍵",
            "The slow, green morning ritual.",
            "Stone-ground ceremonial matcha with a bamboo whisk, a sifter, and a " +
                "measuring scoop, for a frothy bowl that's calmer than coffee. The " +
                "aesthetic morning you keep saving to a folder.",
            3_400, "Snacks", 4.7, 8642,
        ),

        // Outdoors — the commute-and-trail gear of the moment.
        product(
            "Glide E-Scooter", "🛴",
            "Skip the traffic, fold the commute.",
            "A folding electric scooter with a 40km range, hill-eating torque, " +
                "puncture-proof tires, and a one-second fold for the train. App-" +
                "locked and light enough to carry upstairs. The car-free errand, " +
                "deferred.",
            49_900, "Outdoors", 4.6, 7188, originalPriceCents = 59_900,
        ),
        product(
            "SunFold 100W Solar Panel", "🌞",
            "Sunlight in, devices charged.",
            "A foldable 100W panel that unrolls at camp and feeds a power station or " +
                "phones directly, weatherproof with a built-in kickstand. Off-grid " +
                "power from a clear sky. Pairs with the Voltstack you also won't get.",
            17_900, "Outdoors", 4.5, 2940,
        ),
        product(
            "Summit 32oz Insulated Bottle", "🍶",
            "Cold for 24 hours, rugged for years.",
            "A double-wall stainless bottle with a leakproof lid, a chug spout and a " +
                "straw cap in the box, and a powder coat that shrugs off drops. The " +
                "trail-and-desk companion you'll never quite fill.",
            3_900, "Outdoors", 4.8, 21055,
        ),
        product(
            "Trail Carbon Trekking Poles", "🥾",
            "Save your knees on the way down.",
            "Featherweight carbon poles with quick-flip locks, cork grips that wick " +
                "sweat, and snow baskets included, collapsing to fit a daypack. The " +
                "descent gets kinder. The summit stays hypothetical.",
            6_900, "Outdoors", 4.7, 4318,
        ),
        product(
            "Featherlite Packable Down Jacket", "🧥",
            "Real warmth that stuffs into its pocket.",
            "An 800-fill down jacket that packs to the size of a grapefruit, blocks " +
                "wind, and weighs almost nothing in the bag. The just-in-case layer " +
                "for every trip. Warms the version of you that travels.",
            12_900, "Outdoors", 4.8, 8807, originalPriceCents = 15_900,
        ),

        // Pets — the smart-pet gear owners obsess over now.
        product(
            "PurrClean Self-Cleaning Litter Box", "🐈",
            "Scoop nothing, ever again.",
            "An enclosed litter robot that sifts and seals after every visit, tracks " +
                "your cat's habits in an app, and warns you before it's full. The " +
                "chore, automated away. The cat remains skeptical. So does delivery.",
            49_900, "Pets", 4.6, 9233, originalPriceCents = 59_900,
        ),
        product(
            "Roam GPS Pet Tracker", "📡",
            "Always know where they wandered.",
            "A lightweight collar tag with live GPS, a virtual fence that alerts you " +
                "at the edge, and activity tracking, all on a clip that lasts days. " +
                "Peace of mind for an escape artist who, here, never escapes.",
            8_900, "Pets", 4.5, 11604,
        ),
        product(
            "Slow-Feeder Puzzle Bowl", "🦴",
            "Dinner that takes more than four seconds.",
            "A maze-bottom bowl that turns gulped meals into a ten-minute foraging " +
                "puzzle, easing bloat and boredom in one dishwasher-safe dish. " +
                "Engages the brain at mealtime. The meal, naturally, is imaginary.",
            1_900, "Pets", 4.7, 14288,
        ),
        product(
            "Calming Donut Pet Bed", "🛏️",
            "A nest they'll never leave.",
            "A plush faux-fur donut bed with raised rims for head-resting and a " +
                "machine-washable cover, sized for cats and small dogs who like to " +
                "curl. The coziest spot in the house, theoretically arriving.",
            4_400, "Pets", 4.8, 18950,
        ),
        product(
            "Dog DNA Breed & Health Kit", "🧬",
            "Finally settle the 'what is he' debate.",
            "A cheek-swab kit that maps your dog's breed mix, traits, and genetic " +
                "health markers, with a report you'll read aloud to everyone. " +
                "Answers the mystery. Becomes one, by never arriving.",
            9_900, "Pets", 4.6, 6077,
        ),

        // Fashion — the silhouette everyone's wearing this season.
        product(
            "Parachute Cargo Pants", "👖",
            "Billowy, pocketed, everywhere.",
            "Lightweight ripstop cargos with a toggle hem, a relaxed parachute leg, " +
                "and pockets for things you'll never carry. The pant that took over " +
                "the feed. Looks effortless; arrives as effort, unrewarded.",
            6_900, "Fashion", 4.6, 13420,
        ),
        product(
            "Everyday Sling Bag", "🎒",
            "Phone, keys, charger — across the chest.",
            "A compact crossbody sling in water-repellent nylon with a magnetic " +
                "buckle and just-enough organization for a daily carry or a travel " +
                "day. Swings to the front in a second. Carries nothing, stylishly.",
            4_400, "Fashion", 4.7, 16702, originalPriceCents = 5_900,
        ),
        product(
            "Suede Shearling Clogs", "🥿",
            "The cozy slip-on that goes outside.",
            "Suede clogs with a contoured cork-latex footbed and a wool shearling " +
                "lining, backless for the kick-on-and-go life. The comfort shoe that " +
                "became a flex. Pairs with everything you won't be wearing it with.",
            8_900, "Fashion", 4.7, 9531,
        ),
        product(
            "Quilted Puffer Vest", "🦺",
            "The layer that does all the work.",
            "A lightly insulated quilted vest that throws over a hoodie or under a " +
                "coat, with a stand collar and zip pockets. The transitional-weather " +
                "MVP. Keeps your core warm in theory, your wallet warm in fact.",
            7_900, "Fashion", 4.6, 7044,
        ),
        product(
            "Wide-Leg Rigid Denim", "👖",
            "Denim, finally relaxed.",
            "A high-rise, wide-leg jean in rigid cotton that breaks in beautifully " +
                "and puddles just right over a sneaker or a clog. The fit that " +
                "retired the skinny. Sized true; shipped never.",
            8_900, "Fashion", 4.5, 10288, originalPriceCents = 11_900,
        ),

        // Hobbies — the crafts and kits trending across feeds.
        product(
            "ChirpCam Smart Bird Feeder", "🐦",
            "Meet the birds in your own yard.",
            "A solar bird feeder with an AI camera that snaps close-ups, identifies " +
                "the species, and pings your phone when a visitor lands. A nature " +
                "documentary on your windowsill. The cast shows up; the feeder won't.",
            17_900, "Hobbies", 4.8, 13266, originalPriceCents = 21_900,
        ),
        product(
            "Tabletop Pottery Wheel Kit", "🏺",
            "Get your hands muddy at the kitchen table.",
            "A compact, quiet pottery wheel with clay, trimming tools, and an apron, " +
                "sized for a beginner and a countertop. Center, pull, wobble, laugh. " +
                "The messy, meditative hobby you keep almost starting.",
            8_900, "Hobbies", 4.5, 5912,
        ),
        product(
            "Diamond Painting Kit, Galaxy", "💎",
            "Sparkle therapy, one tile at a time.",
            "A large canvas, trays of shimmering resin gems, and the tools to place " +
                "them into a glittering galaxy scene — the craft that's equal parts " +
                "ASMR and accomplishment. Hours of calm. The frame stays empty here.",
            2_400, "Hobbies", 4.7, 9803,
        ),
        product(
            "Closed Terrarium Kit", "🌿",
            "A tiny world under glass.",
            "A sealed glass globe with moss, substrate, charcoal, and miniatures to " +
                "build a self-sustaining ecosystem that waters itself for years. " +
                "Living art for a shelf. The shelf, for now, stays bare.",
            4_900, "Hobbies", 4.6, 6448,
        ),
        product(
            "Botanical Brick Bouquet", "💐",
            "Flowers that never wilt.",
            "An 800-piece building set that assembles into a vase of detailed brick " +
                "blooms — the grown-up build that doubles as decor. An evening of " +
                "clicks and a forever bouquet. Petals not included, by definition.",
            5_900, "Hobbies", 4.8, 15077,
        ),
        product(
            "Gourmet Mushroom Grow Kit", "🍄",
            "Harvest dinner off your counter.",
            "A ready-to-fruit block that sprouts a flush of oyster mushrooms in ten " +
                "days with nothing but a daily mist — the most rewarding thing you'll " +
                "grow indoors. Spores of satisfaction; zero actual mushrooms shipped.",
            2_200, "Hobbies", 4.6, 7219,
        ),

        // Stationery — the journaling and annotation haul.
        product(
            "Fineliner Pen Set, 24 Colors", "🖊️",
            "Bullet-journal spreads, sorted.",
            "Twenty-four fine-tip pens in a gradient of colors that don't bleed " +
                "through thin paper, perfect for headers, habit trackers, and " +
                "doodles. The set that launches a thousand spreads you'll never fill.",
            1_900, "Stationery", 4.8, 16433,
        ),
        product(
            "Self-Sharpening Mechanical Pencil", "✏️",
            "A point that never goes blunt.",
            "A 0.5mm mechanical pencil whose lead rotates as you write to stay " +
                "needle-sharp, with a cushioned grip and a break-resistant sleeve. " +
                "The drafting cult favorite. Writes the to-do list it never delivers.",
            1_400, "Stationery", 4.7, 8902,
        ),
        product(
            "Book Annotation Kit", "📑",
            "Mark up your novels like a scholar.",
            "Pastel highlighters that won't ghost through pages, a wall of sticky " +
                "tabs, and tiny page flags — the BookTok annotation starter every " +
                "reader suddenly needs. Turns reading into a craft. Pages sold " +
                "separately, and imaginarily.",
            2_200, "Stationery", 4.8, 12715, originalPriceCents = 2_900,
        ),
        product(
            "Shimmer Fountain Pen Ink", "🫗",
            "Words that catch the light.",
            "A bottle of richly saturated fountain-pen ink threaded with fine " +
                "shimmer that settles into the strokes as they dry. Pairs with the " +
                "brass pen you also won't receive. Sheens gloriously on paper unseen.",
            1_600, "Stationery", 4.6, 4188,
        ),
        product(
            "Brush Lettering Marker Set", "🖌️",
            "Hand-lettering, starter to show-off.",
            "Dual-tip brush markers with a flexible nib for thick-and-thin strokes " +
                "and a practice pad with guides, in soft and bold colorways. The " +
                "calligraphy habit you keep meaning to pick up. Strokes free, forever.",
            2_400, "Stationery", 4.7, 6510,
        ),

        // Home — the connected-home upgrades on every wishlist.
        product(
            "DoorWatch Video Doorbell", "🔔",
            "See who's there from anywhere.",
            "A 2K video doorbell with package detection, two-way talk, and on-device " +
                "person alerts, wired or battery, with no monthly fee for local " +
                "clips. Watches your porch faithfully — including the parcels that " +
                "never come.",
            12_900, "Home", 4.6, 10588, originalPriceCents = 15_900,
        ),
        product(
            "BoltSmart Deadbolt Lock", "🔐",
            "Leave the keys behind.",
            "A retrofit smart deadbolt with a fingerprint pad, app unlock, and " +
                "auto-lock when you leave, installing over your existing latch in " +
                "minutes. Guards the door against everyone but the courier of " +
                "nothing.",
            16_900, "Home", 4.5, 6240,
        ),
        product(
            "Almanac E-Ink Family Calendar", "🗓️",
            "The whole household, one glance.",
            "A wall-mounted e-ink display that syncs everyone's calendars, chores, " +
                "and meal plans into a paper-like dashboard with no glare and no " +
                "blue light. The command center for a life. Schedules nothing, " +
                "beautifully.",
            34_900, "Home", 4.4, 2877,
        ),
        product(
            "Lumastrip LED Light Strip", "💡",
            "Paint the room any color.",
            "Sixteen million colors of app- and voice-controlled LED strip that " +
                "cuts to length, syncs to music, and tucks behind the TV for that " +
                "glow. The ambiance upgrade. Sets a mood for an evening in.",
            3_400, "Home", 4.6, 22410,
        ),
        product(
            "Switchlet Smart Plug, 4-Pack", "🔌",
            "Make anything smart, instantly.",
            "Four compact plugs that put lamps, fans, and the coffee maker on a " +
                "schedule or a voice command, with energy monitoring per outlet. " +
                "The easiest first step into a smart home. Powers nothing, on cue.",
            2_900, "Home", 4.7, 18033, originalPriceCents = 3_900,
        ),

        // Chaos — the absurd-but-real novelties of the moment.
        product(
            "Giant Plush Crocodile, 2m", "🐊",
            "Two meters of unconditional support.",
            "A two-metre stuffed crocodile, alarmingly soft, that takes over a couch " +
                "and starts a conversation. People genuinely buy these. Hugs back in " +
                "spirit; arrives in none.",
            5_900, "Chaos", 4.9, 16722,
        ),
        product(
            "Bigfoot Garden Statue", "🦶",
            "Cryptid lawn presence.",
            "A weatherproof resin sasquatch, knee-high, peeking from the flowerbed to " +
                "delight some neighbors and unsettle the rest. The yard art nobody " +
                "asked for and everybody photographs. Stays a legend; never ships.",
            3_900, "Chaos", 4.8, 8104,
        ),
        product(
            "Duck With A Knife (Figurine)", "🦆",
            "He has a knife. That's the product.",
            "A small, sincere figurine of a duck holding a tiny knife. No backstory, " +
                "no purpose, no notes. The internet's favorite menace, on your shelf. " +
                "Threatens nothing, delivers nothing.",
            1_500, "Chaos", 4.9, 24503,
        ),
        product(
            "200 Rubber Ducks, Assorted", "🐤",
            "Why two hundred? Why not.",
            "A bulk bag of two hundred tiny rubber ducks in costumes — pirates, " +
                "knights, dinosaurs — for desk armies, jeep-ducking, or sheer " +
                "abundance. The quantity is the joke. The shipment is the punchline.",
            2_400, "Chaos", 4.8, 9911, originalPriceCents = 2_900,
        ),
        product(
            "Inflatable Sumo Suit (Pair)", "🤼",
            "Two suits. One questionable evening.",
            "A pair of self-inflating sumo costumes with padded gloves, ready for a " +
                "backyard tournament nobody will forget. One size flattens all " +
                "dignity. The single greatest purchase you will never unbox.",
            7_900, "Chaos", 4.7, 5288,
        ),

        // Gaming — the current-gen console ecosystem and accessories.
        product(
            "Meteor Swift Hybrid Console", "🎮",
            "Dock it for the TV, grab it for the couch.",
            "A hybrid console that plays in 4K docked and snaps into a 7-inch OLED " +
                "handheld in one motion, with detachable controllers and a 90Hz " +
                "screen. The whole library, anywhere in the house. Travels nowhere, " +
                "here.",
            39_900, "Gaming", 4.8, 24611, originalPriceCents = 44_900,
            variantGroup = "meteor-swift", variantLabel = "Standard", variantAxis = "Edition",
        ),
        product(
            "Vortex Aurora Wireless Headset", "🎧",
            "Hear the footsteps before they hear you.",
            "A low-latency wireless gaming headset with 50mm drivers, spatial audio, " +
                "a flip-to-mute boom mic, and a 40-hour battery that recharges while " +
                "you play. Plush enough for a raid night. Hears every gunshot that " +
                "never fires.",
            14_900, "Gaming", 4.7, 13402,
        ),
        product(
            "Meteor Charge Base + Extra Controller", "🔋",
            "A second pad, always topped up.",
            "A spare wireless controller in midnight blue plus a magnetic dock that " +
                "charges two pads to full in an hour and ends the dead-battery " +
                "scramble mid-match. Co-op, sorted. The friend to use it with sold " +
                "separately.",
            9_400, "Gaming", 4.6, 7188, originalPriceCents = 11_900,
        ),
        product(
            "Meteor 2TB Storage Expansion Card", "💾",
            "Stop uninstalling to install.",
            "A plug-and-play expansion card that doubles your console's storage at " +
                "full internal speed — no enclosure, no fuss, just slot it in. Forty " +
                "more games you'll mean to finish. Zero of them arriving.",
            27_900, "Gaming", 4.7, 5530,
        ),
        product(
            "Wisp Travel Case", "🧳",
            "Armor for your handheld.",
            "A hard-shell case molded for the Wisp, with a shock-absorbing liner, " +
                "mesh pockets for cables and game cards, and a low-profile carry " +
                "loop. Protects the handheld on every trip it won't be taking.",
            2_900, "Gaming", 4.8, 9044,
        ),
        product(
            "Vortex Arcade FightStick", "🕹️",
            "Pull off the combo you keep dropping.",
            "A tournament-grade arcade stick with a clicky lever, eight low-travel " +
                "buttons, and swappable parts for the fighting-game faithful. Lap-" +
                "sized and rock-solid. Lands the finisher in a match that never " +
                "loads.",
            19_900, "Gaming", 4.6, 4377,
        ),
        product(
            "Meteor Vertical Cooling Stand", "🌀",
            "Stand it up, cool it down, charge the pads.",
            "A vertical stand with twin whisper-fans, two controller charging slots, " +
                "and game-case storage that tidies the whole console corner. Keeps " +
                "the system cool during marathons it'll never run.",
            3_900, "Gaming", 4.5, 6622,
        ),
        product(
            "Wisp Grip & Power Bank", "🔌",
            "Doubles the battery, fixes the cramp.",
            "A clip-on grip that wraps the handheld in ergonomic handles and a " +
                "built-in battery for a full extra charge on the go, with " +
                "pass-through play. The long flight's best friend. Boards no flight, " +
                "naturally.",
            4_400, "Gaming", 4.6, 5119, originalPriceCents = 5_400,
        ),
        product(
            "Meteor VR Headset", "🥽",
            "Step inside the console.",
            "A console-tethered VR headset with a 4K OLED per eye, inside-out eye " +
                "tracking, headset haptics, and a pair of orb controllers with " +
                "adaptive triggers. One cable to the Meteor. The worlds are vast; " +
                "the package, empty.",
            54_900, "Gaming", 4.6, 8233, originalPriceCents = 64_900,
        ),
        product(
            "Vortex Broadcast Mic", "🎙️",
            "Sound like the streamers you watch.",
            "A USB condenser mic with a built-in shock mount, tap-to-mute, and a " +
                "studio-quality cardioid pickup that ignores the keyboard clatter. " +
                "Plug in and go live. Broadcasts your voice to an audience as real " +
                "as the stream.",
            11_900, "Gaming", 4.7, 10455,
        ),
        product(
            "Vortex Stream Control Pad", "🎛️",
            "Run the whole stream with one press.",
            "Fifteen customizable LCD keys that switch scenes, fire clips, mute " +
                "audio, and launch anything mid-broadcast, with endless folders of " +
                "actions. The streamer's command deck. Controls a show that never " +
                "airs.",
            14_900, "Gaming", 4.8, 7611, originalPriceCents = 17_900,
        ),

        // Gaming — a believable current-gen library for the console.
        product(
            "Ashen Banner", "🗡️",
            "Die, learn, conquer. Repeat.",
            "A punishing open-world action-RPG of crumbling kingdoms, hidden " +
                "shortcuts, and bosses that earn their reputation. The one everyone " +
                "argues about online. A hundred hours of glorious suffering, none " +
                "of it delivered.",
            6_900, "Gaming", 4.8, 52188, originalPriceCents = 7_900,
        ),
        product(
            "Override: Tactical", "🎯",
            "Five heroes. One objective. Pure chaos.",
            "A team-based hero shooter with a roster of distinct abilities, ranked " +
                "ladders, and a new agent every season. The headset is recommended; " +
                "the friends are mandatory. Wins ranked matches that never queue.",
            4_900, "Gaming", 4.5, 38740,
        ),
        product(
            "Nightshift Anomaly", "👻",
            "Clock in. Survive the shift. Maybe.",
            "A co-op horror about underpaid contractors collecting data in places " +
                "they shouldn't, where proximity chat turns terror into comedy. The " +
                "streamer-favorite scream machine. Frights guaranteed; cartridge not.",
            1_900, "Gaming", 4.7, 44902,
        ),
        product(
            "Pitch Legends 26", "⚽",
            "This year's squad, this year's glory.",
            "The annual football sim with refreshed rosters, deeper career mode, and " +
                "online seasons your group chat will live in. Yes, it's basically " +
                "last year's. Yes, you'll buy it. Here, gloriously, you won't.",
            6_900, "Gaming", 4.3, 29055, originalPriceCents = 7_900,
        ),
        product(
            "Redline GT 5", "🏁",
            "Every car, every track, photoreal.",
            "A sim-grade racer with hundreds of licensed-feeling cars, dynamic " +
                "weather, and a career that rewards a clean apex. Pairs perfectly " +
                "with a force-feedback wheel. The podium remains, as ever, imaginary.",
            5_900, "Gaming", 4.6, 21344,
        ),

        // Gaming — console bundles (separate listings + "What's included",
        // Amazon-style). Each bundle price is honestly below the sum of its
        // parts, so the savings shown are real, not a fabricated strikethrough.
        product(
            "Meteor Swift + Turbo Kart Bundle", "🎮",
            "The console and the game everyone starts with.",
            "The hybrid Meteor Swift paired with a full download of Turbo Kart " +
                "Carnival — the pack-in bundle that is most people's first box. One " +
                "price, one unboxing, zero of it arriving.",
            42_900, "Gaming", 4.8, 18402, originalPriceCents = 45_800,
            includes = listOf(
                "Meteor Swift Hybrid Console",
                "Turbo Kart Carnival (full game download)",
                "Two detachable controllers + grip",
            ),
        ),
        product(
            "Meteor Swift + Extra Controller Bundle", "🎮",
            "Co-op ready, straight out of the box.",
            "The Meteor Swift with a second wireless controller in midnight blue and " +
                "a magnetic charging dock, so the couch is two-player from minute " +
                "one. The friend to play with is, as ever, sold separately.",
            45_900, "Gaming", 4.7, 9120, originalPriceCents = 49_300,
            includes = listOf(
                "Meteor Swift Hybrid Console",
                "Extra wireless controller (midnight blue)",
                "Magnetic dual charging dock",
            ),
        ),
        product(
            "Meteor Swift Starter Kit", "🎮",
            "Console plus the accessories you'd buy next.",
            "The Meteor Swift bundled with the protection a new handheld needs — a " +
                "hard travel case, a tempered-glass guard, and a spare cable. " +
                "Everything to keep safe the device that never ships.",
            41_900, "Gaming", 4.8, 6755, originalPriceCents = 44_700,
            includes = listOf(
                "Meteor Swift Hybrid Console",
                "Hard-shell travel case",
                "Tempered-glass screen protector",
                "Extra USB-C charging cable",
            ),
        ),
        product(
            "Meteor Swift Everything Bundle", "🎮",
            "Console, a game, and a year online.",
            "The Meteor Swift with a full game of Pitch Legends 26 and a 12-month " +
                "Meteor Online membership for the seasons your group chat will live " +
                "in. The whole first year, packed into one box of nothing.",
            47_900, "Gaming", 4.7, 5288, originalPriceCents = 51_700,
            includes = listOf(
                "Meteor Swift Hybrid Console",
                "Pitch Legends 26 (full game)",
                "12-month Meteor Online membership",
                "Cloud-save expansion",
            ),
        ),
        product(
            "Meteor MK-II + Redline GT Bundle", "🎮",
            "The 4K console, set up for the sim seat.",
            "The flagship Meteor MK-II with a full download of Redline GT 5 and an " +
                "extra controller — the home-console bundle built for the living " +
                "room. Add a force-feedback wheel and the podium is yours, " +
                "hypothetically.",
            57_900, "Gaming", 4.8, 7611, originalPriceCents = 62_700,
            includes = listOf(
                "Meteor MK-II Console (2TB)",
                "Redline GT 5 (full game)",
                "Extra Meteor Pro Controller",
            ),
        ),

        // Gaming — Meteor Pro Controller colorways (variant siblings of the
        // base black controller above; the PDP shows a Color swatch row). Each
        // is its own listing, like Amazon's separate-ASIN colors; the limited
        // editions cost a touch more.
        product(
            "Meteor Pro Controller — Volcanic Red", "🎮",
            "Built for the long session, dressed to be seen.",
            "The same drift-free Hall-effect sticks, back paddles, and 40-hour " +
                "battery, in a molten red finish with matching thumbsticks. The " +
                "colorway that photographs best and arrives least.",
            7_400, "Gaming", 4.7, 5102,
            variantGroup = "meteor-pro-controller", variantLabel = "Volcanic Red",
        ),
        product(
            "Meteor Pro Controller — Starlight Blue", "🎮",
            "Built for the long session, finished in cosmos.",
            "Drift-free Hall-effect sticks, remappable paddles, and a 40-hour " +
                "battery in a deep starlight-blue fade. Quietly the best-looking pad " +
                "in the lineup. Quietly never delivered.",
            7_400, "Gaming", 4.8, 4488,
            variantGroup = "meteor-pro-controller", variantLabel = "Starlight Blue",
        ),
        product(
            "Meteor Pro Controller — Sterling Silver", "🎮",
            "Built for the long session, plated for the shelf.",
            "The pro controller in a brushed sterling finish with metallic face " +
                "buttons — the limited edition that sells out on sight. Premium to " +
                "the touch, theoretical in the hand.",
            7_900, "Gaming", 4.8, 3677, originalPriceCents = 8_900,
            variantGroup = "meteor-pro-controller", variantLabel = "Sterling Silver",
        ),
        // Console edition variant — the standard/digital split (Switch-2 style),
        // a separate listing that shares the Swift's Edition swatch row.
        product(
            "Meteor Swift — Digital Edition", "🎮",
            "The same console, download-only.",
            "The full Meteor Swift hardware without the game-card slot, for players " +
                "who buy everything from the store. Lighter on the shelf and on the " +
                "price. The library is digital; the delivery, nonexistent.",
            34_900, "Gaming", 4.6, 9882,
            variantGroup = "meteor-swift", variantLabel = "Digital", variantAxis = "Edition",
        ),

        // Gaming — Orbit, the rival console ecosystem (the other half of the
        // console duopoly), with its own bundle and Disc/Digital editions.
        product(
            "Orbit One Console", "🎮",
            "Cinematic power, under the TV.",
            "The rival flagship: a 4K disc console with a custom SSD that kills load " +
                "screens, a haptic controller in the box, and a library of " +
                "blockbuster exclusives. The other console everyone argues about. " +
                "Ships nowhere, loudly.",
            49_900, "Gaming", 4.8, 28744,
            variantGroup = "orbit-one", variantLabel = "Disc Edition", variantAxis = "Edition",
        ),
        product(
            "Orbit One — Digital Edition", "🎮",
            "Same power, no disc drive.",
            "The slimmer Orbit One for the all-download crowd — identical internals, " +
                "minus the disc slot, minus a chunk of the price. The digital future, " +
                "delivered digitally, which is to say not at all.",
            44_900, "Gaming", 4.7, 12033,
            variantGroup = "orbit-one", variantLabel = "Digital Edition", variantAxis = "Edition",
        ),
        product(
            "Orbit Glide Controller", "🎮",
            "Haptics you feel in your palms.",
            "The Orbit pad with fine-grained haptics, adaptive triggers that fight " +
                "back, a built-in mic, and a charge that lasts the weekend. Pairs to " +
                "the Orbit One in a tap. Rumbles for a game that never loads.",
            7_400, "Gaming", 4.7, 16920,
        ),
        product(
            "Orbit One + Skybound Survivors Bundle", "🎮",
            "The rival console with a co-op epic to start.",
            "The Orbit One paired with a full download of Skybound Survivors — the " +
                "console-and-game box for the other camp. One price, one unboxing, " +
                "zero of it real.",
            52_900, "Gaming", 4.8, 8401, originalPriceCents = 55_800,
            includes = listOf(
                "Orbit One Console (Disc Edition)",
                "Skybound Survivors (full game download)",
                "Orbit Glide Controller",
            ),
        ),

        // ---- Trading Cards ----
        // Three invented games, each in collectible series. Per series the
        // formats (pack → display → tin/box) are Format-axis variant siblings,
        // so the PDP flips between them and the grid leads with the cheap
        // entry pack. Tins and collector boxes are bundles ("What's
        // included"), priced honestly below their parts. As everywhere:
        // listings deadpan-sincere, satire stays in the frame.

        // Pocket Critters TCG — series: Emberglow, Abyssal Tides.
        product(
            "Pocket Critters: Emberglow Booster Pack", "🎴",
            "Eleven cards. One spark of maybe.",
            "An 11-card booster from the Emberglow expansion — 203 critters of " +
                "the volcanic valley, a reverse-holo in every pack, and a rare or " +
                "better guaranteed. The wrapper art alone is worth keeping.",
            449, "Trading Cards", 4.8, 21458,
            variantGroup = "critters-emberglow", variantLabel = "Booster Pack",
            variantAxis = "Format",
        ),
        product(
            "Pocket Critters: Emberglow Booster Display", "📦",
            "Thirty-six packs, factory sealed.",
            "A sealed display of 36 Emberglow boosters straight from the case — " +
                "the classic way to chase a full set. Pull rates feel kinder by " +
                "the dozen, and the box stores the binder overflow afterward.",
            13_999, "Trading Cards", 4.9, 3211, originalPriceCents = 16_164,
            variantGroup = "critters-emberglow", variantLabel = "Display (36 Packs)",
            variantAxis = "Format",
        ),
        product(
            "Pocket Critters: Emberglow Collector Tin", "🎁",
            "Four packs and a holo, in keepsake metal.",
            "An embossed tin with the Emberwing art lid, four Emberglow boosters " +
                "inside, and an exclusive holo promo you can't pull from packs. " +
                "The tin outlives the cards; the cards never arrive at all.",
            2_199, "Trading Cards", 4.7, 6840,
            includes = listOf(
                "4 Emberglow booster packs",
                "Emberwing holo promo card",
                "Embossed storage tin with art lid",
                "Card divider set",
            ),
            variantGroup = "critters-emberglow", variantLabel = "Collector Tin",
            variantAxis = "Format",
        ),
        product(
            "Pocket Critters: Emberglow Elite Collector Box", "🧰",
            "The whole hobby in one lid-lift.",
            "Nine boosters, a full-art promo, and the accessories a serious " +
                "collection runs on — sleeves, dividers, condition counters, and " +
                "a set guide. The box every Emberglow shelf is built around.",
            4_999, "Trading Cards", 4.9, 4102, originalPriceCents = 5_890,
            includes = listOf(
                "9 Emberglow booster packs",
                "Full-art Emberwing promo card",
                "65 art sleeves + 4 dividers",
                "Acrylic condition counters + flip die",
                "Collector's guide to the Emberglow set",
            ),
            variantGroup = "critters-emberglow", variantLabel = "Elite Box",
            variantAxis = "Format",
        ),
        product(
            "Pocket Critters: Abyssal Tides Booster Pack", "🎴",
            "The deep-sea set, eleven cards down.",
            "An 11-card booster from Abyssal Tides — 198 critters of the trench, " +
                "glow-foil treatment on the deep dwellers, and a rare or better " +
                "in every pack. Best opened with the lights off.",
            449, "Trading Cards", 4.8, 17204,
            variantGroup = "critters-abyssal", variantLabel = "Booster Pack",
            variantAxis = "Format",
        ),
        product(
            "Pocket Critters: Abyssal Tides Booster Display", "📦",
            "A sealed case-fresh dive, 36 packs deep.",
            "The full sealed display of 36 Abyssal Tides boosters. Enough glow-" +
                "foil to read by, statistically — and the sturdiest shelf piece " +
                "the set ships in, for collections that stay sealed.",
            14_499, "Trading Cards", 4.8, 2380,
            variantGroup = "critters-abyssal", variantLabel = "Display (36 Packs)",
            variantAxis = "Format",
        ),
        product(
            "Pocket Critters: Abyssal Tides Premium Collection", "🧰",
            "Six packs, two promos, one oversized legend.",
            "Six Abyssal Tides boosters with two exclusive holo promos, an " +
                "oversized Tidelord Mawra display card, and a sticker sheet for " +
                "the laptop the hobby quietly takes over.",
            3_999, "Trading Cards", 4.7, 3055,
            includes = listOf(
                "6 Abyssal Tides booster packs",
                "2 exclusive holo promo cards",
                "Oversized Tidelord Mawra display card",
                "Abyssal Tides sticker sheet",
            ),
            variantGroup = "critters-abyssal", variantLabel = "Premium Collection",
            variantAxis = "Format",
        ),

        // Duelbound TCG — series: Forbidden Archive, Crimson Eclipse.
        product(
            "Duelbound: Forbidden Archive Booster Pack", "🃏",
            "Nine cards from the sealed stacks.",
            "A 9-card booster from the Forbidden Archive set — banished spells, " +
                "vault guardians, and a guaranteed foil in every pack. The " +
                "archive is forbidden; the shipping, nonexistent.",
            429, "Trading Cards", 4.7, 19034,
            variantGroup = "duelbound-archive", variantLabel = "Booster Pack",
            variantAxis = "Format",
        ),
        product(
            "Duelbound: Forbidden Archive Booster Display", "📦",
            "Twenty-four packs, one sealed shelf of secrets.",
            "The sealed 24-pack display of Forbidden Archive, case-fresh with " +
                "the wax untouched. The format the tournament crowd splits four " +
                "ways and the collectors never open at all.",
            8_999, "Trading Cards", 4.8, 2744, originalPriceCents = 10_296,
            variantGroup = "duelbound-archive", variantLabel = "Display (24 Packs)",
            variantAxis = "Format",
        ),
        product(
            "Duelbound: Forbidden Archive Mega Tin", "🎁",
            "Three mega packs in vault-grade metal.",
            "The annual mega tin with the vault-door art lid: three 16-card " +
                "mega packs, dividers, and one of three secret-rare promos " +
                "sealed inside. Which one? The tin isn't telling.",
            2_199, "Trading Cards", 4.8, 7912,
            includes = listOf(
                "3 Forbidden Archive mega packs (16 cards each)",
                "1 of 3 secret-rare promo cards",
                "Vault-door art tin",
                "4 card dividers",
            ),
            variantGroup = "duelbound-archive", variantLabel = "Mega Tin",
            variantAxis = "Format",
        ),
        product(
            "Duelbound: Forbidden Archive Secret Chest", "🧰",
            "The collector's cut of the archive.",
            "Eight boosters, two chest-exclusive ghost-foil promos, and a " +
                "numbered art print, latched inside a keepsake chest. The set's " +
                "ceiling, boxed — opened once, remembered indefinitely.",
            4_499, "Trading Cards", 4.9, 3168, originalPriceCents = 5_220,
            includes = listOf(
                "8 Forbidden Archive booster packs",
                "2 chest-exclusive ghost-foil promos",
                "Numbered art print",
                "Latched keepsake chest",
            ),
            variantGroup = "duelbound-archive", variantLabel = "Secret Chest",
            variantAxis = "Format",
        ),
        product(
            "Duelbound: Crimson Eclipse Booster Pack", "🃏",
            "The midnight set, nine cards at a time.",
            "A 9-card booster from Crimson Eclipse — eclipse dragons, blood-moon " +
                "rituals, and a foil in every pack, with red-foil chase cards " +
                "that catch light the way regret catches Sundays.",
            429, "Trading Cards", 4.7, 14881,
            variantGroup = "duelbound-eclipse", variantLabel = "Booster Pack",
            variantAxis = "Format",
        ),
        product(
            "Duelbound: Crimson Eclipse Booster Display", "📦",
            "The full eclipse, sealed: 24 packs.",
            "A sealed display of 24 Crimson Eclipse boosters. The red-foil " +
                "pull rates are the forum's favorite argument; the sealed box " +
                "is the only answer everyone respects.",
            8_499, "Trading Cards", 4.7, 1932,
            variantGroup = "duelbound-eclipse", variantLabel = "Display (24 Packs)",
            variantAxis = "Format",
        ),
        product(
            "Duelbound: Crimson Eclipse Collector's Vault", "🧰",
            "Seven packs and the moon itself.",
            "Seven Crimson Eclipse boosters with an alternate-art promo, a " +
                "metal field center piece shaped like the eclipse, and sleeves " +
                "to match. The vault locks; the contents were never in it.",
            4_999, "Trading Cards", 4.8, 2451,
            includes = listOf(
                "7 Crimson Eclipse booster packs",
                "Alternate-art Eclipse Devourer promo",
                "Metal eclipse field center piece",
                "60 blood-moon sleeves",
            ),
            variantGroup = "duelbound-eclipse", variantLabel = "Collector's Vault",
            variantAxis = "Format",
        ),

        // Manaforge — series: Ashveil, The Verdant Throne.
        product(
            "Manaforge: Ashveil Play Booster", "🔮",
            "Fourteen cards from the burned plane.",
            "A 14-card play booster from the Ashveil set — draftable, " +
                "collectible, and carrying a foil in every pack. The " +
                "plane burned so the deck-building could begin.",
            549, "Trading Cards", 4.8, 16772,
            variantGroup = "manaforge-ashveil", variantLabel = "Play Booster",
            variantAxis = "Format",
        ),
        product(
            "Manaforge: Ashveil Booster Display", "📦",
            "Thirty play boosters, draft night settled.",
            "The sealed 30-pack Ashveil display — three full draft pods or one " +
                "very honest month of pack-a-day discipline. Case-fresh, foil " +
                "odds as printed, delivery as imagined.",
            14_999, "Trading Cards", 4.9, 2807, originalPriceCents = 16_470,
            variantGroup = "manaforge-ashveil", variantLabel = "Display (30 Packs)",
            variantAxis = "Format",
        ),
        product(
            "Manaforge: Ashveil Bundle", "🎁",
            "Eight packs and the table setup to match.",
            "Eight Ashveil play boosters with twenty full-art mana cards, an " +
                "oversized spindown life die, a promo Archmage, and the set-art " +
                "storage box the whole pile lives in afterward.",
            4_499, "Trading Cards", 4.8, 5230,
            includes = listOf(
                "8 Ashveil play boosters",
                "20 full-art mana cards",
                "Oversized spindown life die",
                "Promo: Archmage of the Ashveil",
                "Set-art storage box",
            ),
            variantGroup = "manaforge-ashveil", variantLabel = "Bundle",
            variantAxis = "Format",
        ),
        product(
            "Manaforge: Ashveil Collector Booster", "✨",
            "Fifteen cards, all of them shiny.",
            "The premium pack: fifteen Ashveil cards where every slot is foil, " +
                "extended-art, or rarer — including a shot at the serialized " +
                "Archmage, 1 of 500. The pack you open slowly, hypothetically.",
            2_499, "Trading Cards", 4.7, 4915,
            variantGroup = "manaforge-ashveil", variantLabel = "Collector Booster",
            variantAxis = "Format",
        ),
        product(
            "Manaforge: The Verdant Throne Play Booster", "🔮",
            "Fourteen cards from the overgrown court.",
            "A 14-card play booster from The Verdant Throne — a kingdom " +
                "reclaimed by forest, court intrigue at sorcery speed, and a " +
                "foil in every pack. The throne grows back; the cards don't.",
            549, "Trading Cards", 4.8, 12490,
            variantGroup = "manaforge-verdant", variantLabel = "Play Booster",
            variantAxis = "Format",
        ),
        product(
            "Manaforge: The Verdant Throne Booster Display", "📦",
            "Thirty packs of the forest court, sealed.",
            "The sealed 30-pack display of The Verdant Throne. Draft it, vault " +
                "it, or shelve it next to Ashveil and call the shelf a format. " +
                "Sealed is a lifestyle; so is nothing arriving.",
            15_499, "Trading Cards", 4.8, 2114,
            variantGroup = "manaforge-verdant", variantLabel = "Display (30 Packs)",
            variantAxis = "Format",
        ),
        product(
            "Manaforge: The Verdant Throne Bundle", "🎁",
            "The set, the dice, the box it lives in.",
            "Eight Verdant Throne play boosters, twenty full-art mana cards, a " +
                "moss-green spindown die, and a promo of the Throne itself in " +
                "the keepsake box the kingdom is stored in.",
            4_699, "Trading Cards", 4.7, 3866,
            includes = listOf(
                "8 The Verdant Throne play boosters",
                "20 full-art mana cards",
                "Moss-green oversized spindown die",
                "Promo: The Verdant Throne, Reborn",
                "Keepsake storage box",
            ),
            variantGroup = "manaforge-verdant", variantLabel = "Bundle",
            variantAxis = "Format",
        ),

        // Trading-card accessories (the hobby around the hobby).
        product(
            "Cardkeeper Zip Binder (480 Slots)", "📒",
            "Every pull, side-loaded and safe.",
            "A zip-closed 480-slot binder with side-loading pockets, acid-free " +
                "archival pages, and a spine that lies flat at any page. Fits " +
                "standard sleeves; survives being shown to everyone you know.",
            2_499, "Trading Cards", 4.9, 11203,
        ),
        product(
            "Dragonhide Matte Sleeves (100-Pack)", "🛡️",
            "Shuffle-feel of legend, glare of none.",
            "One hundred matte sleeves at 90 microns — opaque backs, " +
                "tournament-legal, and a shuffle feel the table will ask about. " +
                "Sized for every standard card you'll never receive.",
            999, "Trading Cards", 4.8, 24580,
        ),
        product(
            "Vaultline Magnetic Deck Box", "🗃️",
            "A hundred sleeved cards, click-shut.",
            "A 100-card deck box in soft-touch shell with self-aligning " +
                "magnetic closure and a separate lid well for dice and tokens. " +
                "Guards the deck on every trip it won't be taking.",
            1_999, "Trading Cards", 4.8, 8347,
        ),

        // ---- Craving coverage (researched): blind-box collectibles — the
        // designer-toy mystery format. Invented IP, deadpan-sincere copy,
        // lineup counts stated as fact, never as odds bait.
        product(
            "Moppling Blind Box: Bog Friends Series", "🧸",
            "One of twelve. Sealed in the dark.",
            "A palm-sized vinyl Moppling in a sealed inner bag — twelve to " +
                "collect across the Bog Friends wave, lineup card included, " +
                "sold strictly one mystery at a time. The box knows which " +
                "one. The box has known for months.",
            1_699, "Hobbies", 4.9, 31204,
        ),
        product(
            "Moppling Blind Box: Cloud Court Series (6-Pack)", "📦",
            "Six sealed doors into the Cloud Court.",
            "Six factory-sealed Cloud Court blind boxes in the collector " +
                "sleeve. Twelve figures in the wave; trade the doubles, " +
                "frame the favorites, keep the sleeve.",
            9_499, "Hobbies", 4.8, 9871,
        ),
        product(
            "Moppling Plush Bag Charm (Sealed)", "🎀",
            "A soft stranger for your bag strap.",
            "One Moppling plush charm, 11cm, sealed bag with clip — fuzzy, " +
                "slightly smug, ready to dangle from a tote it will " +
                "immediately upstage. Eight charms in the lineup; yours " +
                "arrives already loyal.",
            2_799, "Hobbies", 4.9, 27343,
        ),
        product(
            "Curio Capsule: Desk Gremlins Vol. 3", "🥚",
            "Twist, pop, gremlin.",
            "A capsule-machine classic for the desk: one of ten micro " +
                "gremlins, each molded mid-task — filing, sulking, holding " +
                "a tiny cone. Caps double as display domes. Volume 3, the " +
                "office wave.",
            799, "Hobbies", 4.7, 18764,
        ),
    )

    fun byId(id: Int): Product? = products.firstOrNull { it.id == id }

    /** All sibling listings in a variant group, in catalog order. */
    fun variantsOf(group: String): List<Product> = products.filter { it.variantGroup == group }

    /**
     * Collapse variant siblings to one entry per group for grid/search display
     * (Amazon-style: one card with swatches). The representative is the group's
     * base — its lowest id — or, when the base isn't present (e.g. a search that
     * only matched one color), the lowest-id sibling that is. Order preserved;
     * non-variant products pass through untouched.
     */
    fun collapseVariants(items: List<Product>): List<Product> {
        if (items.none { it.variantGroup != null }) return items
        val repId = items
            .filter { it.variantGroup != null }
            .groupBy { it.variantGroup }
            .mapValues { (_, members) -> members.minByOrNull { it.id }!!.id }
        return items.filter { it.variantGroup == null || repId[it.variantGroup] == it.id }
    }

    /**
     * Curated "frequently bought together" companions, by product name (names
     * are stable and unique; ids shift as the catalog grows). The PDP shows the
     * product plus these with an honest combined total — convenience, not a
     * fabricated saving.
     */
    private val boughtTogetherByName: Map<String, List<String>> = mapOf(
        "Meteor Swift Hybrid Console" to listOf("Meteor Pro Controller", "Turbo Kart Carnival"),
        "Meteor MK-II Console" to listOf("Vortex Aurora Wireless Headset", "Meteor 2TB Storage Expansion Card"),
        "Orbit One Console" to listOf("Orbit Glide Controller", "Vortex Aurora Wireless Headset"),
        "Wisp Handheld" to listOf("Wisp Travel Case", "Wisp Grip & Power Bank"),
        "AuraPhone 17 Ultra Max" to listOf("Volt 100W GaN Charger", "FindIt Trackers (4-Pack)"),
        "Espresso Machine, Barista-Grade" to listOf("Gooseneck Pour-Over Kettle", "Quench 40oz Tumbler"),
        "Pocket Critters: Emberglow Booster Pack" to
            listOf("Dragonhide Matte Sleeves (100-Pack)", "Cardkeeper Zip Binder (480 Slots)"),
        "Duelbound: Forbidden Archive Booster Pack" to
            listOf("Cardkeeper Zip Binder (480 Slots)", "Dragonhide Matte Sleeves (100-Pack)"),
        "Manaforge: Ashveil Play Booster" to
            listOf("Vaultline Magnetic Deck Box", "Dragonhide Matte Sleeves (100-Pack)"),
        // A sealed display is binder fodder by definition — the accessories
        // belong beside it just as much as beside the single pack.
        "Pocket Critters: Emberglow Booster Display" to
            listOf("Cardkeeper Zip Binder (480 Slots)", "Dragonhide Matte Sleeves (100-Pack)"),
        "Pocket Critters: Abyssal Tides Booster Display" to
            listOf("Cardkeeper Zip Binder (480 Slots)", "Dragonhide Matte Sleeves (100-Pack)"),
        "Duelbound: Forbidden Archive Booster Display" to
            listOf("Dragonhide Matte Sleeves (100-Pack)", "Vaultline Magnetic Deck Box"),
        "Duelbound: Crimson Eclipse Booster Display" to
            listOf("Dragonhide Matte Sleeves (100-Pack)", "Cardkeeper Zip Binder (480 Slots)"),
        "Manaforge: Ashveil Booster Display" to
            listOf("Vaultline Magnetic Deck Box", "Cardkeeper Zip Binder (480 Slots)"),
        "Manaforge: The Verdant Throne Booster Display" to
            listOf("Vaultline Magnetic Deck Box", "Dragonhide Matte Sleeves (100-Pack)"),
    )

    /** The companion products bought alongside [product], in listed order. */
    fun boughtTogether(product: Product): List<Product> =
        boughtTogetherByName[product.name].orEmpty().mapNotNull { name -> products.firstOrNull { it.name == name } }

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

    /**
     * The chase cards, keyed by series (variant group) — each series is a
     * themed set with its own content language, the way real expansions
     * have their own world. The wrapper already names the series; the cards
     * inside belong to it.
     *
     * The six content languages:
     * - Emberglow (critters): hearth, dawn, kept warmth — critters with
     *   cozy habits. Flavor: gentle, domestic, one wink per card.
     * - Abyssal Tides (critters): deep water, night currents, soft glow in
     *   the dark. Flavor: serene, drifting, unhurried.
     * - Forbidden Archive (duelbound): a haunted library. The dread is
     *   bureaucratic — indexes, late fees, shushing.
     * - Crimson Eclipse (duelbound): a blood-moon vigil. Apocalyptic but
     *   unbothered; everything is on schedule.
     * - Ashveil (manaforge): the volcanic forge. Flavor reads as smithing
     *   proverbs, work and patience.
     * - The Verdant Throne (manaforge): a fallen court overgrown — royal
     *   language gone to seed; nature wins politely.
     */
    private val cardPullPools: Map<String, List<CardPull>> = mapOf(
        "critters-emberglow" to listOf(
            CardPull(
                "🔥", "Emberwing, Ascendant", "Secret holo · 1 in 2,304 packs",
                "It molts once a century. The valley keeps every feather.",
                type = "Stage 2 Flame Critter",
                stat = "180 HP",
            ),
            CardPull(
                "⚡", "Voltifox", "Holo rare · 1 in 96 packs",
                "Static cling is its love language.",
                type = "Stage 1 Spark Critter",
                stat = "120 HP",
            ),
            CardPull(
                "🌿", "Sprigbloom, Waking", "Reverse holo · pleasantly common",
                "Every Sprigbloom believes it is the rarest card in the set.",
                type = "Stage 1 Bloom Critter",
                stat = "110 HP",
            ),
            CardPull(
                "🦚", "Dawnplume Radiant", "Full-art holo · 1 in 720 packs",
                "Fans its tail and the morning decides to stay.",
                type = "Stage 2 Sky Critter",
                stat = "150 HP",
            ),
        ),
        "critters-abyssal" to listOf(
            CardPull(
                "🌊", "Tidelord Mawra", "Full-art holo · 1 in 850 packs",
                "The tide doesn't come in. Mawra lets it out.",
                type = "Stage 2 Tide Critter",
                stat = "170 HP",
            ),
            CardPull(
                "🌙", "Lunavale, Dreaming", "Alt-art holo · 1 in 1,200 packs",
                "It sleeps through every battle and has never lost one.",
                type = "Stage 2 Dream Critter",
                stat = "160 HP",
            ),
            CardPull(
                "🦑", "Vellamora, Deepcrowned", "Secret holo · 1 in 1,800 packs",
                "Wears the pressure of the deep as a crown. It fits.",
                type = "Stage 2 Tide Critter",
                stat = "170 HP",
            ),
            CardPull(
                "🐋", "Brinesong", "Holo rare · 1 in 128 packs",
                "Sings to the surface once a year. The surface writes back.",
                type = "Stage 2 Dream Critter",
                stat = "140 HP",
            ),
        ),
        "duelbound-archive" to listOf(
            CardPull(
                "👁️", "The Nameless Archivist", "Ghost rare · 1 in 1,920 packs",
                "It knows your deck list. It filed it centuries ago.",
                type = "[Spellcaster / Effect]",
                stat = "ATK/2400 DEF/2100",
            ),
            CardPull(
                "🐍", "Serpent of the Sealed Vault", "Ultimate foil · 1 in 480 packs",
                "The vault was sealed to keep it in. It signs for deliveries anyway.",
                type = "[Serpent / Ritual / Effect]",
                stat = "ATK/2900 DEF/2500",
            ),
            CardPull(
                "🏺", "Relic of the First Duel", "Gold rare · 1 in 240 packs",
                "Nobody remembers who won. The urn isn't telling.",
                type = "[Relic / Continuous]",
            ),
            CardPull(
                "📇", "Index of Forbidden Names", "Secret rare · 1 in 960 packs",
                "Your name is in it. It always was.",
                type = "[Spell / Ritual]",
            ),
        ),
        "duelbound-eclipse" to listOf(
            CardPull(
                "🌑", "Eclipse Devourer", "Secret rare · 1 in 720 packs",
                "It ate the moon once. The moon got better.",
                type = "[Fiend / Fusion / Effect]",
                stat = "ATK/3000 DEF/2500",
            ),
            CardPull(
                "🧛", "Crimson Regent, Twice-Risen", "Ghost rare · 1 in 1,440 packs",
                "Abdicated once. Death didn't take.",
                type = "[Vampire / Fusion / Effect]",
                stat = "ATK/2800 DEF/2200",
            ),
            CardPull(
                "🌘", "The Unfinished Moon", "Gold rare · 1 in 320 packs",
                "Someone is still carving it.",
                type = "[Spell / Continuous]",
            ),
            CardPull(
                "🗡️", "Bloodbound Duelist", "Ultra rare · 1 in 240 packs",
                "Signs every duel in advance. In something.",
                type = "[Warrior / Ritual / Effect]",
                stat = "ATK/2500 DEF/2000",
            ),
        ),
        "manaforge-ashveil" to listOf(
            CardPull(
                "🧙", "Archmage of the Ashveil", "Serialized foil · 1 of 500",
                "She numbered the copies herself. She is not in any of them.",
                type = "Legendary Creature — Human Wizard",
                stat = "3/4",
            ),
            CardPull(
                "🌋", "Caldera Sovereign", "Borderless mythic · 1 in 640 packs",
                "Its throne room has no borders. Neither does this card.",
                type = "Legendary Creature — Elemental Dragon",
                stat = "6/6",
            ),
            CardPull(
                "⏳", "Hourglass of Convergence", "Foil rare · 1 in 64 packs",
                "Turn it over and somewhere, a draft begins.",
                type = "Legendary Artifact",
            ),
            CardPull(
                "🔨", "Vulkhammer, First Tool", "Extended-art mythic · 1 in 480 packs",
                "It remembers being the mountain.",
                type = "Legendary Artifact — Equipment",
            ),
        ),
        "manaforge-verdant" to listOf(
            CardPull(
                "🌳", "The Verdant Throne, Reborn", "Extended-art mythic · 1 in 510 packs",
                "The kingdom fell. The garden won.",
                type = "Legendary Enchantment — Saga",
            ),
            CardPull(
                "👑", "Crown of Living Oak", "Serialized foil · 1 of 350",
                "Crowns only those who stop reaching for it.",
                type = "Legendary Artifact — Equipment",
            ),
            CardPull(
                "🌺", "Bloomheart Sovereign", "Borderless mythic · 1 in 580 packs",
                "The garden won. She is what winning looks like.",
                type = "Legendary Creature — Dryad",
                stat = "5/5",
            ),
            CardPull(
                "🕊️", "Pact of Quiet Growth", "Foil rare · 1 in 72 packs",
                "Year one: a seed. Year ten: a verdict.",
                type = "Enchantment — Saga",
            ),
        ),
    )

    /** Display titles for the card games, keyed by their variant-group prefix. */
    val cardGameTitles: Map<String, String> = mapOf(
        "critters" to "Pocket Critters",
        "duelbound" to "Duelbound",
        "manaforge" to "Manaforge",
    )

    /** The series groups of a game, in checklist order. */
    private fun seriesGroupsOf(game: String): List<String> = cardSeriesTitles.keys.filter { it.startsWith("$game-") }

    /**
     * Every chase card a game can pull — the binder's checklist, in series
     * order.
     */
    fun chaseCardsOf(game: String): List<CardPull> = seriesGroupsOf(game).flatMap { cardPullPools[it].orEmpty() }

    /** The same checklist grouped by series title — the binder's set pages. */
    fun chaseChecklistOf(game: String): List<Pair<String, List<CardPull>>> =
        seriesGroupsOf(game).map { cardSeriesTitles.getValue(it) to cardPullPools[it].orEmpty() }

    /**
     * The tiny collector print in the card's bottom corner, in each genre's
     * idiom — and now per series, the way real expansions number their own
     * sets: Pokémon's set fraction, Yu-Gi-Oh's per-set code, Magic's padded
     * number with set code and rarity letter. The number is the card's slot
     * in its series checklist (commons first, chases last), so it never
     * renumbers unless the set itself changes.
     */
    fun collectorNumberOf(game: String, card: CardPull): String {
        for (group in seriesGroupsOf(game)) {
            val set = cardCommonPools[group].orEmpty() + cardPullPools[group].orEmpty()
            val slot = set.indexOfFirst { it.name == card.name } + 1
            if (slot == 0) continue
            val letter = when {
                card.rarity.startsWith("Common") -> "C"
                card.rarity.startsWith("Uncommon") -> "U"
                else -> "M"
            }
            // Set sizes match the listings' own claims (203 critters of the
            // valley, 198 of the trench) — the copy and the card agree.
            return when (group) {
                "critters-emberglow" -> "%03d/203".format(slot)
                "critters-abyssal" -> "%03d/198".format(slot)
                "duelbound-archive" -> "DAR-EN%03d".format(slot)
                "duelbound-eclipse" -> "DCE-EN%03d".format(slot)
                "manaforge-ashveil" -> "ASH · %04d/0184 $letter".format(slot)
                else -> "VER · %04d/0166 $letter".format(slot)
            }
        }
        return ""
    }

    /** Display titles for each collectible series, keyed by variant group. */
    val cardSeriesTitles: Map<String, String> = mapOf(
        "critters-emberglow" to "Emberglow",
        "critters-abyssal" to "Abyssal Tides",
        "duelbound-archive" to "Forbidden Archive",
        "duelbound-eclipse" to "Crimson Eclipse",
        "manaforge-ashveil" to "Ashveil",
        "manaforge-verdant" to "The Verdant Throne",
    )

    /**
     * The hypothetical best card inside [product] for [orderId]: a seeded,
     * stable pick from the matching game's chase pool, or null when the
     * product isn't a trading-card game item (accessories included). Same
     * rules as the Mystery Box: decorative, free, gates nothing.
     */
    fun cardPullFor(orderId: Int, product: Product, packIndex: Int = 0): CardPull? {
        val pool = cardPullPools[product.variantGroup] ?: return null
        return pool[Math.floorMod(orderId * 31 + product.id * 7 + packIndex * 17 + 5, pool.size)]
    }

    /**
     * The rest of the pack: commons and uncommons that pad the rip out
     * before the chase card, keyed by series like the chases. Each pool
     * holds 8 so a coprime index step keeps any four picks distinct.
     */
    private val cardCommonPools: Map<String, List<CardPull>> = mapOf(
        "critters-emberglow" to listOf(
            CardPull(
                "🐭", "Nibbletuft", "Common", "Hoards crumbs by the hearth. Eating them is not the point.",
                type = "Basic Meadow Critter",
                stat = "50 HP",
            ),
            CardPull(
                "🐛", "Larvalume", "Common", "Glows brighter the less it knows.",
                type = "Basic Spark Critter",
                stat = "40 HP",
            ),
            CardPull(
                "🐦", "Chirplet", "Common", "Knows one song. Commits to it at first light.",
                type = "Basic Sky Critter",
                stat = "40 HP",
            ),
            CardPull(
                "🦔", "Bramblepin", "Common", "Sleeps against warm chimney stones. Hugs technically possible.",
                type = "Basic Bloom Critter",
                stat = "70 HP",
            ),
            CardPull(
                "🦊", "Sunkit", "Common", "Naps wherever the light pools. The light has learned to pool around it.",
                type = "Basic Flame Critter",
                stat = "60 HP",
            ),
            CardPull(
                "🐝", "Cinderbee", "Common", "Makes honey that tastes faintly of campfire. Will not elaborate.",
                type = "Basic Spark Critter",
                stat = "40 HP",
            ),
            CardPull(
                "🦎", "Emberlisk", "Uncommon", "Suns itself on stones it warmed up first.",
                type = "Stage 1 Flame Critter",
                stat = "80 HP",
            ),
            CardPull(
                "🐓", "Dawnstrut", "Uncommon", "Announces the sunrise. Accepts full credit.",
                type = "Stage 1 Sky Critter",
                stat = "90 HP",
            ),
        ),
        "critters-abyssal" to listOf(
            CardPull(
                "🐸", "Paddlehop", "Common", "Has never once landed where it aimed. The tide approves.",
                type = "Basic Tide Critter",
                stat = "60 HP",
            ),
            CardPull(
                "🐑", "Cloudlamb", "Common", "Counts itself to fall asleep.",
                type = "Basic Dream Critter",
                stat = "60 HP",
            ),
            CardPull(
                "🐚", "Murmurshell", "Common", "Repeats the ocean back to itself, slightly improved.",
                type = "Basic Tide Critter",
                stat = "50 HP",
            ),
            CardPull(
                "🦀", "Pinchdrift", "Common", "Walks sideways. Arrives anyway.",
                type = "Basic Tide Critter",
                stat = "60 HP",
            ),
            CardPull(
                "🐙", "Inkpip", "Common", "Dreams in ink. Wakes in clouds.",
                type = "Basic Dream Critter",
                stat = "40 HP",
            ),
            CardPull(
                "🐟", "Glintfin", "Common", "A school of one. Perpetually on time.",
                type = "Basic Tide Critter",
                stat = "40 HP",
            ),
            CardPull(
                "🐌", "Glimmersnail", "Uncommon", "Arrives last. Shines anyway.",
                type = "Stage 1 Spark Critter",
                stat = "80 HP",
            ),
            CardPull(
                "🦉", "Duskhoot", "Uncommon", "Asks 'who?' rhetorically. It knows.",
                type = "Stage 1 Dream Critter",
                stat = "90 HP",
            ),
        ),
        "duelbound-archive" to listOf(
            CardPull(
                "🕯️", "Vault Candle", "Common", "Lit before the archive. Will outlast it.",
                type = "[Relic / Normal]",
            ),
            CardPull(
                "📜", "Scroll of Echoes", "Common", "Repeats your last move, judgmentally.",
                type = "[Spell / Quick-Play]",
            ),
            CardPull(
                "🗝️", "Key to the Lower Stacks", "Common", "Opens a door best left described.",
                type = "[Relic / Equip]",
            ),
            CardPull(
                "🌫️", "Shade of the Reading Room", "Common", "Shushes duelists from three stacks away.",
                type = "[Ghost / Effect]",
                stat = "ATK/1200 DEF/800",
            ),
            CardPull(
                "📚", "Stack Wyrm", "Common", "Eats footnotes first. Savors the citations.",
                type = "[Wyrm / Effect]",
                stat = "ATK/800 DEF/1200",
            ),
            CardPull(
                "🖋️", "Censor's Quill", "Common", "Strikes through one truth per turn.",
                type = "[Spell / Equip]",
            ),
            CardPull(
                "🧾", "Late Fee Wraith", "Uncommon", "Compounds nightly.",
                type = "[Fiend / Effect]",
                stat = "ATK/900 DEF/600",
            ),
            CardPull(
                "🦇", "Crypt Flitter", "Uncommon", "Files itself under 'bird'. Nobody argues.",
                type = "[Winged Beast / Effect]",
                stat = "ATK/900 DEF/600",
            ),
        ),
        "duelbound-eclipse" to listOf(
            CardPull(
                "🪦", "Tombstone Sentry", "Common", "Guards a grave nobody is in.",
                type = "[Zombie / Normal]",
                stat = "ATK/0 DEF/1900",
            ),
            CardPull(
                "⚱️", "Sealed Urn", "Common", "Do not open. It gets cold.",
                type = "[Trap / Counter]",
            ),
            CardPull(
                "🌒", "Waning Acolyte", "Common", "Prays the moon thinner every night. It's working.",
                type = "[Spellcaster / Effect]",
                stat = "ATK/700 DEF/500",
            ),
            CardPull(
                "🩸", "Bloodglass Vial", "Common", "Bottled at the last eclipse. Still warm.",
                type = "[Relic / Normal]",
            ),
            CardPull(
                "🐺", "Vigil Hound", "Common", "Howls at the eclipse on schedule. Very professional.",
                type = "[Beast / Effect]",
                stat = "ATK/1100 DEF/700",
            ),
            CardPull(
                "🔔", "Curfew Bell", "Common", "Rings at moonrise. The town pretends not to hear.",
                type = "[Relic / Continuous]",
            ),
            CardPull(
                "🌹", "Thorn of the Red Vigil", "Uncommon", "Blooms once per eclipse, out of spite.",
                type = "[Plant / Effect]",
                stat = "ATK/800 DEF/1000",
            ),
            CardPull(
                "🕸️", "Warding Web", "Uncommon", "The spider moved out. The lease holds.",
                type = "[Trap / Continuous]",
            ),
        ),
        "manaforge-ashveil" to listOf(
            CardPull(
                "🔥", "Cinder Wisp", "Common", "A spark with ambitions and no plan.",
                type = "Creature — Elemental",
                stat = "1/1",
            ),
            CardPull(
                "🪨", "Forge Stone", "Common", "It was here before the forge. It waits.",
                type = "Artifact",
            ),
            CardPull(
                "🧪", "Alchemist's Vial", "Common", "Contents: hope, approximately.",
                type = "Artifact — Potion",
            ),
            CardPull(
                "💨", "Ashwind Current", "Common", "The veil lifts. The veil chooses what it shows.",
                type = "Instant",
            ),
            CardPull(
                "⚒️", "Anvilbound Sprite", "Common", "Sentenced to a thousand years of honest work. Thriving.",
                type = "Creature — Elemental",
                stat = "1/2",
            ),
            CardPull(
                "🫙", "Slag Tithe", "Common", "The forge keeps a tenth of everything.",
                type = "Artifact",
            ),
            CardPull(
                "🗡️", "Ashveil Blade", "Uncommon", "Forged in the fire it was named after.",
                type = "Artifact — Equipment",
            ),
            CardPull(
                "✨", "Spark of Convergence", "Uncommon", "Two ideas touched. This got out.",
                type = "Sorcery",
            ),
        ),
        "manaforge-verdant" to listOf(
            CardPull(
                "🍃", "Leaf of the Throne", "Common", "Fell from the crown. Still royalty.",
                type = "Enchantment — Aura",
            ),
            CardPull(
                "🛡️", "Wovenroot Shield", "Common", "Grows back faster than it dents.",
                type = "Artifact — Equipment",
            ),
            CardPull(
                "💧", "Mana Droplet", "Common", "Every flood starts somewhere small.",
                type = "Instant",
            ),
            CardPull(
                "🍄", "Court Toadstool", "Common", "Holds the throne room's only surviving seat.",
                type = "Creature — Fungus",
                stat = "0/3",
            ),
            CardPull(
                "🦌", "Crownshade Stag", "Common", "Wears the king's antler crown. Grew it himself.",
                type = "Creature — Elk",
                stat = "2/2",
            ),
            CardPull(
                "🌾", "Tithe of Seasons", "Common", "The fields still pay. Nobody collects.",
                type = "Sorcery",
            ),
            CardPull(
                "🌱", "Sapling Usurper", "Uncommon", "Patience is a siege engine.",
                type = "Creature — Treefolk",
                stat = "1/3",
            ),
            CardPull(
                "🦢", "Stillpond Regent", "Uncommon", "Rules the reflection. The reflection is enough.",
                type = "Creature — Spirit",
                stat = "2/3",
            ),
        ),
    )

    /**
     * The whole pack for the rip ceremony: four seeded, distinct commons and
     * the chase card dealt last — commons first, the payoff at the back, the
     * way the genre's best openers stage it. Stable per (order, product,
     * pack); [packIndex] varies the deal so a multi-pack order doesn't rip
     * the same five cards twice. Null for anything that isn't a trading-card
     * game product.
     */
    fun packRipFor(orderId: Int, product: Product, packIndex: Int = 0): List<CardPull>? {
        val chase = cardPullFor(orderId, product, packIndex) ?: return null
        val pool = cardCommonPools.getValue(product.variantGroup!!)
        val start = Math.floorMod(orderId * 13 + product.id * 3 + packIndex * 5, pool.size)
        // Step 3 is coprime with the pool size, so the four picks are distinct.
        return List(4) { pool[(start + it * 3) % pool.size] } + chase
    }

    /** Products eligible for the rotating flash deal slot. */
    val dealCandidates: List<Product> = products.filter { it.originalPriceCents != null }
}
