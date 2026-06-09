package com.example.myapplication.data

/**
 * The entire "marketplace". Nothing here is real, purchasable, or shippable.
 * Reviews are assigned deterministically from a shared pool so the catalog is
 * stable across runs without any persistence.
 */
object FakeCatalog {

    val categories = listOf("All", "Tech", "Home", "Fashion", "Snacks", "Self-Care", "Chaos")

    private val reviewPool = listOf(
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
    )

    private fun reviewsFor(id: Int): List<Review> {
        val start = id % reviewPool.size
        return (0 until 3).map { reviewPool[(start + it * 5) % reviewPool.size] }
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
            rating, reviewCount, reviewsFor(id), originalPriceCents,
        )
    }

    val products: List<Product> = listOf(
        // Tech
        product(
            "AuraPhone 17 Ultra Max", "📱",
            "The phone your phone dreams about.",
            "A bezel-free slab of pure status. Features a 900MP camera that photographs " +
                "your future regrets and a battery that lasts exactly one day, like all the others. " +
                "You don't need it. That's why it feels so good in the cart.",
            129_900, "Tech", 4.8, 12473, originalPriceCents = 159_900,
        ),
        product(
            "NoiseGone Pro Headphones", "🎧",
            "Silence everything, including doubt.",
            "Active noise cancellation so strong it mutes the voice asking 'do you really " +
                "need these?'. Yes. In the cart, you always do.",
            34_900, "Tech", 4.7, 8821,
        ),
        product(
            "RoboVac Sensei 9000", "🤖",
            "It cleans. It judges. It learns.",
            "A robot vacuum with laser mapping, pet detection, and a quiet dignity you'll " +
                "never have to live up to, because it's never coming.",
            79_900, "Tech", 4.6, 5310, originalPriceCents = 99_900,
        ),
        product(
            "Mechanical Keyboard, Extra Clacky", "⌨️",
            "Thock therapy.",
            "Hand-lubed switches, artisan keycaps, a sound profile reviewers describe as " +
                "'rain on a tin roof, but expensive'. Your coworkers are safe — it ships nowhere.",
            21_900, "Tech", 4.9, 15294,
        ),
        product(
            "8K Drone with Follow-Me Mode", "🛸",
            "Be followed, cinematically.",
            "Films you from above as you walk dramatically through your life. Comes with " +
                "zero batteries because it comes with zero drone.",
            64_900, "Tech", 4.5, 3107,
        ),

        // Home
        product(
            "Cloud Sofa (3-Seater)", "🛋️",
            "Sitting, perfected.",
            "Memory foam that remembers you fondly. Stain-proof, nap-certified, and " +
                "guaranteed to fit through your imaginary doorway on the first try.",
            189_900, "Home", 4.8, 6642, originalPriceCents = 249_900,
        ),
        product(
            "Self-Watering Plant That Can't Die", "🪴",
            "Immortal. Unkillable. Unreal.",
            "Finally, a plant that survives your care schedule. Photosynthesizes pure " +
                "ambiance. Pairs beautifully with the bookshelf you're about to not buy.",
            4_900, "Home", 4.9, 9210,
        ),
        product(
            "Artisan Candle: 'New Apartment Smell'", "🕯️",
            "Smells like a fresh start.",
            "Top notes of optimism, base notes of security deposit. Burns for 60 hypothetical " +
                "hours. The dopamine is in the checkout, not the wick.",
            3_200, "Home", 4.7, 4188,
        ),
        product(
            "Floor-to-Ceiling Bookshelf", "📚",
            "For the books you'll also pretend to buy.",
            "Solid oak presence, infinite intellectual promise. Assembly required, " +
                "in the sense that it will remain entirely unassembled forever.",
            45_900, "Home", 4.6, 2871,
        ),
        product(
            "Espresso Machine, Barista-Grade", "☕",
            "Your café phase begins (in spirit).",
            "Dual boiler, brass everything, latte art potential off the charts. Saves you " +
                "$2,400 a year in coffee shop visits by costing you nothing at all.",
            89_900, "Home", 4.8, 7754, originalPriceCents = 109_900,
        ),

        // Fashion
        product(
            "Limited Drop Sneakers 'Phantom 1'", "👟",
            "So exclusive they don't exist.",
            "Only 0 pairs will ever ship, making these the rarest sneakers on Earth. " +
                "Resale value: emotional. Comfort rating: hypothetical but excellent.",
            27_900, "Fashion", 4.9, 18337, originalPriceCents = 39_900,
        ),
        product(
            "Cashmere Hoodie of Main Character Energy", "🧥",
            "Soft power.",
            "The hoodie influencers wear in airports. Buttery, cloud-spun, and yours in " +
                "every way that matters except physically.",
            18_900, "Fashion", 4.7, 6029,
        ),
        product(
            "Vintage Watch, Old Money Edition", "⌚",
            "Time you didn't spend money.",
            "Swiss movement, heirloom aura. Every glance at your empty wrist will remind " +
                "you of the $4,000 still in your account.",
            399_900, "Fashion", 4.8, 1543,
        ),
        product(
            "Sunglasses You'd Never Lose", "🕶️",
            "Impossible to leave in a taxi.",
            "Polarized, face-flattering, and permanently safe in the one place you can't " +
                "lose them: the cart.",
            15_900, "Fashion", 4.6, 3390,
        ),

        // Snacks
        product(
            "Midnight Tteokbokki Mega Set", "🍜",
            "Spicy. Glorious. Calorie-free by absence.",
            "Extra cheese, extra rice cakes, extra everything. All the late-night comfort, " +
                "none of the 2am regret. The courier will heroically deliver zero of it.",
            2_400, "Snacks", 4.9, 22481,
        ),
        product(
            "Artisanal Fried Chicken Bucket", "🍗",
            "Double-fried, zero-delivered.",
            "Crispy on the outside, hypothetical on the inside. Korea's favorite stress " +
                "meal, now with 100% fewer consequences.",
            3_100, "Snacks", 4.8, 31764, originalPriceCents = 3_900,
        ),
        product(
            "Emotional Support Cake (Whole)", "🍰",
            "You were never going to share it anyway.",
            "A full-size celebration cake for celebrating nothing in particular. Eating " +
                "the whole thing is impossible, which is the healthiest it's ever been.",
            5_600, "Snacks", 4.9, 12058,
        ),
        product(
            "Imported Snack Box: Mystery Edition", "🎁",
            "47 snacks. 0 shipping. ∞ intrigue.",
            "A surprise box of international snacks you'll never taste, which means " +
                "they all taste perfect.",
            7_900, "Snacks", 4.7, 8146,
        ),

        // Self-Care
        product(
            "10-Step Skincare Ritual Kit", "🧴",
            "Glass skin, crystal-clear savings.",
            "Snail mucin, niacinamide, and eleven other words that feel expensive. Your " +
                "skin barrier is fine. Your dopamine barrier needed this.",
            12_900, "Self-Care", 4.8, 9931, originalPriceCents = 16_900,
        ),
        product(
            "Weighted Blanket, Anxiety-Rated", "🛌",
            "A firm hug from no one.",
            "Twelve kilograms of calm. Sleeping under it is theoretical, but so is " +
                "your stress once checkout completes.",
            9_900, "Self-Care", 4.9, 14207,
        ),
        product(
            "Spa Day In A Box", "🧖",
            "Treat yourself to the idea of it.",
            "Bath bombs, eye masks, cucumber ambiance. The relaxing part was never the " +
                "spa — it was pressing 'Place order' and owing nothing.",
            8_400, "Self-Care", 4.7, 5566,
        ),
        product(
            "Yoga Mat of Future Discipline", "🧘",
            "Day one starts in the cart.",
            "Extra-grip surface for the workouts you're 100% going to do. Currently " +
                "holding your potential, indefinitely, at no charge.",
            6_900, "Self-Care", 4.6, 7012,
        ),

        // Chaos
        product(
            "Inflatable T-Rex Costume", "🦖",
            "For meetings that need you most.",
            "One size fits all ambitions. Battery-powered fan included (not included). " +
                "The single greatest purchase you will never make.",
            5_900, "Chaos", 4.9, 19877,
        ),
        product(
            "1,000 Live Ladybugs", "🐞",
            "Why? Exactly.",
            "Real marketplaces actually sell this. Ours has the decency not to deliver " +
                "them. Your garden, your rules, their freedom.",
            2_900, "Chaos", 4.8, 6203,
        ),
        product(
            "Medieval Sword (Decorative)", "⚔️",
            "Conversation starter. Argument ender.",
            "Full tang, display stand included. Legal everywhere, because it exists nowhere. " +
                "Your landlord can't object to a sword that never ships.",
            22_900, "Chaos", 4.7, 4419,
        ),
        product(
            "Brick (Premium)", "🧱",
            "It's a brick.",
            "Kiln-fired, palm-sized gravitas. People genuinely buy bricks online when the " +
                "dopamine runs low. Yours is free, weightless, and emotionally identical.",
            1_900, "Chaos", 5.0, 27345, originalPriceCents = 2_900,
        ),
        product(
            "Tiny Hands (Pair)", "🤏",
            "Big laughs, small commitment.",
            "Finger-puppet-sized hands for your hands. The peak of internet commerce. " +
                "Adding these to the cart is the entire product experience.",
            3_400, "Chaos", 4.9, 11932,
        ),
    )

    fun byId(id: Int): Product? = products.firstOrNull { it.id == id }

    /** Products eligible for the rotating flash deal slot. */
    val dealCandidates: List<Product> = products.filter { it.originalPriceCents != null }
}
