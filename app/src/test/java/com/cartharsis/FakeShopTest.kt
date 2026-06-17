package com.cartharsis

import com.cartharsis.data.CartItem
import com.cartharsis.data.Couriers
import com.cartharsis.data.Currency
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.HomeShelf
import com.cartharsis.data.NotificationPolicy
import com.cartharsis.data.Order
import com.cartharsis.data.OrderStatus
import com.cartharsis.data.UserReview
import com.cartharsis.data.advanceStreak
import com.cartharsis.data.badges
import com.cartharsis.data.decodeBinderCard
import com.cartharsis.data.decodeUserReview
import com.cartharsis.data.deliveriesTogetherLine
import com.cartharsis.data.effectiveStreak
import com.cartharsis.data.encodeBinderCard
import com.cartharsis.data.encodeUserReview
import com.cartharsis.data.fakeStockLeft
import com.cartharsis.data.formatPrice
import com.cartharsis.data.homeGreeting
import com.cartharsis.data.homeOrder
import com.cartharsis.data.homeShelves
import com.cartharsis.data.keptEquivalent
import com.cartharsis.data.keptInCoffees
import com.cartharsis.data.lastSavingsMilestone
import com.cartharsis.data.newlyEarned
import com.cartharsis.data.nextSavingsMilestone
import com.cartharsis.data.ordinal
import com.cartharsis.data.plusProduct
import com.cartharsis.data.savingsMilestoneProgress
import com.cartharsis.data.trackingCode
import com.cartharsis.data.withPriceOverride
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeShopTest {

    @Test
    fun `catalog has unique ids and lookup works`() {
        val ids = FakeCatalog.products.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        FakeCatalog.products.forEach { assertEquals(it, FakeCatalog.byId(it.id)) }
    }

    @Test
    fun `every product belongs to a listed category`() {
        FakeCatalog.products.forEach {
            assertTrue("${it.name} has unknown category ${it.category}", it.category in FakeCatalog.categories)
        }
    }

    @Test
    fun `product names are unique`() {
        // Names feed search and the by-name Mystery Box lookup; a collision
        // would make that lookup ambiguous and confuse results.
        val names = FakeCatalog.products.map { it.name }
        val dupes = names.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        assertTrue("duplicate product names: $dupes", dupes.isEmpty())
    }

    @Test
    fun `bundles list real contents and only ever show honest savings`() {
        val bundles = FakeCatalog.products.filter { it.isBundle }
        assertTrue("expected some bundles in the catalog", bundles.isNotEmpty())
        bundles.forEach { b ->
            // A bundle is two or more things, by definition.
            assertTrue("${b.name} lists fewer than 2 items", b.includes.size >= 2)
            // If it advertises a saving, the strikethrough must be a genuinely
            // higher reference and the discount sane — never a fabricated deal.
            b.originalPriceCents?.let { original ->
                assertTrue("${b.name} strikethrough isn't a saving", original > b.priceCents)
                assertTrue("${b.name} discount looks fabricated: ${b.discountPercent}", b.discountPercent!! in 1..40)
            }
        }
    }

    @Test
    fun `variant groups are well-formed`() {
        val grouped = FakeCatalog.products.filter { it.variantGroup != null }
        assertTrue("expected some variant siblings", grouped.isNotEmpty())
        grouped.groupBy { it.variantGroup }.forEach { (group, members) ->
            // A real variant set is 2+ siblings on a single shared axis, each
            // with a distinct label, so the swatch row reads cleanly.
            assertTrue("group $group has one member", members.size >= 2)
            assertEquals("group $group mixes axes", 1, members.map { it.variantAxis }.toSet().size)
            val labels = members.map { it.variantLabel }
            assertTrue("group $group has a null label", labels.none { it == null })
            assertEquals("group $group repeats a label", labels.size, labels.toSet().size)
        }
    }

    @Test
    fun `collapseVariants keeps one rep per group and leaves singles alone`() {
        val all = FakeCatalog.products
        val collapsed = FakeCatalog.collapseVariants(all)
        // Every variant group is now represented exactly once...
        all.mapNotNull { it.variantGroup }.toSet().forEach { group ->
            assertEquals("group $group not collapsed to one", 1, collapsed.count { it.variantGroup == group })
        }
        // ...by its base (lowest id), and non-variant products all survive.
        all.filter { it.variantGroup != null }.groupBy { it.variantGroup }.forEach { (group, members) ->
            val rep = collapsed.first { it.variantGroup == group }
            assertEquals("group $group rep isn't the base", members.minByOrNull { it.id }!!.id, rep.id)
        }
        assertEquals(all.count { it.variantGroup == null }, collapsed.count { it.variantGroup == null })
    }

    @Test
    fun `collapseVariants surfaces the matched sibling when the base is absent`() {
        // Simulate a search that matched only one non-base colorway.
        val redOnly = FakeCatalog.products.filter { it.variantLabel == "Volcanic Red" }
        assertEquals(1, redOnly.size)
        assertEquals(redOnly, FakeCatalog.collapseVariants(redOnly))
    }

    @Test
    fun `frequently-bought-together companions all resolve to real products`() {
        val hubs = FakeCatalog.products.filter { FakeCatalog.boughtTogether(it).isNotEmpty() }
        assertTrue("no FBT sets wired up", hubs.isNotEmpty())
        hubs.forEach { hub ->
            val companions = FakeCatalog.boughtTogether(hub)
            // A typo'd companion name silently drops out; require all to resolve
            // and none to be the product itself.
            assertTrue("${hub.name} has unresolved companions", companions.size >= 2)
            assertTrue("${hub.name} lists itself", companions.none { it.id == hub.id })
        }
    }

    @Test
    fun `home greeting is deterministic per seed and time-bucketed`() {
        // Same inputs → same line (stable within a session).
        assertEquals(homeGreeting(42L, 9), homeGreeting(42L, 9))
        // Each time bucket draws from its own pool, so morning != night for a
        // shared seed (the pools are disjoint).
        listOf(8 to 23, 14 to 2, 20 to 3).forEach { (day, night) ->
            assertTrue(homeGreeting(7L, day) != homeGreeting(7L, night))
        }
        // Never blank, for any hour.
        (0..23).forEach { h -> assertTrue(homeGreeting(1L, h).isNotBlank()) }
    }

    @Test
    fun `home order is a deterministic permutation of the catalog`() {
        val catalog = FakeCatalog.products
        val ordered = homeOrder(catalog, 123L)
        assertEquals("not a permutation", catalog.toSet(), ordered.toSet())
        assertEquals(catalog.size, ordered.size)
        // Same seed → same order; the shuffle is reproducible within a session.
        assertEquals(ordered, homeOrder(catalog, 123L))
        // Different seeds almost surely reorder a 200+ item list.
        assertTrue(homeOrder(catalog, 1L) != homeOrder(catalog, 2L))
    }

    @Test
    fun `home shelves are full, non-repeating, daily-stable, and personalize`() {
        val cat = FakeCatalog.products
        val shelves = homeShelves(
            cat, seed = 99L, recentlyViewedIds = emptyList(), wishlistIds = emptySet(),
            hourOfDay = 14, epochDay = 20_000L,
        )
        // Up to `count` shelves, each filled (no thin rows), no product repeated
        // down the page (deduped across shelves).
        assertTrue(shelves.isNotEmpty() && shelves.size <= 5)
        shelves.forEach { assertTrue("${it.title} too thin", it.products.size >= 4) }
        val allIds = shelves.flatMap { it.products.map { p -> p.id } }
        assertEquals("a product repeated across shelves", allIds.size, allIds.toSet().size)

        // The daily collection is stable within a day regardless of open-seed...
        val a = homeShelves(cat, 1L, emptyList(), emptySet(), 14, 20_000L).first()
        val b = homeShelves(cat, 2L, emptyList(), emptySet(), 14, 20_000L).first()
        assertEquals(a.title, b.title)
        assertEquals(a.products.map { it.id }, b.products.map { it.id })

        // ...and the personalized row appears only when there's history.
        val titles = { s: List<HomeShelf> -> s.map { it.title } }
        val withHistory = homeShelves(cat, 5L, listOf(cat[3].id, cat[7].id), setOf(cat[1].id), 14, 20_000L)
        assertTrue(titles(withHistory).any { it.startsWith("Rediscover") })
    }

    @Test
    fun `kept equivalent picks the grandest affordable unit and counts it`() {
        assertEquals(null, keptEquivalent(0))
        assertEquals(null, keptEquivalent(499)) // below one fancy coffee
        // $50 → grandest affordable is the movie-night tier... actually burger
        // ($18) is grandest ≤ $50; 5000/1800 = 2.
        assertEquals("≈ 2 burger combos", keptEquivalent(5_000)!!.text)
        // $6,344 → grandest affordable is the dream getaway ($3,500); 2 of them.
        assertEquals("≈ 1 dream getaway", keptEquivalent(350_000)!!.text)
        assertEquals(12, keptInCoffees(6_000))
    }

    @Test
    fun `savings milestones advance and the progress bar stays in bounds`() {
        assertEquals(10_000L, nextSavingsMilestone(0))
        assertEquals(100_000L, nextSavingsMilestone(50_000))
        assertEquals(null, nextSavingsMilestone(9_000_000)) // past the top
        assertEquals(50_000L, lastSavingsMilestone(60_000))
        // Fill is the fraction of the way to the next milestone, from zero.
        assertEquals(0.5f, savingsMilestoneProgress(25_000), 0.001f) // $250 toward the $500 goal
        assertTrue(savingsMilestoneProgress(30_000) in 0f..1f)
        assertEquals(1f, savingsMilestoneProgress(9_000_000), 0.001f) // capped past the top
    }

    @Test
    fun `newly earned fires only on a genuine in-session crossing`() {
        // First observation of a session (previous == null) celebrates nothing,
        // so an existing collection doesn't re-fire on every launch.
        assertTrue(newlyEarned(null, setOf("first", "kept100")).isEmpty())
        // No change → nothing.
        assertTrue(newlyEarned(setOf("first"), setOf("first")).isEmpty())
        // A real crossing → just the new ones.
        assertEquals(setOf("kept100"), newlyEarned(setOf("first"), setOf("first", "kept100")))
    }

    @Test
    fun `badges earn at their thresholds`() {
        val none = badges(ordersPlaced = 0, centsKept = 0, streakDays = 0)
        assertTrue(none.none { it.earned })
        val some = badges(ordersPlaced = 12, centsKept = 120_000, streakDays = 8)
        assertTrue(some.first { it.id == "first" }.earned)
        assertTrue(some.first { it.id == "orders10" }.earned)
        assertFalse(some.first { it.id == "orders50" }.earned)
        assertTrue(some.first { it.id == "kept1k" }.earned)
        assertFalse(some.first { it.id == "kept10k" }.earned)
        assertTrue(some.first { it.id == "streak7" }.earned)
        assertFalse(some.first { it.id == "streak30" }.earned)
    }

    @Test
    fun `every browsable category is stocked deep enough to not look thin`() {
        // The "All" chip isn't a real category; every other one should fill
        // the grid. Locks in the catalog-depth pass against silent regressions.
        FakeCatalog.categories.filterNot { it == "All" }.forEach { category ->
            val count = FakeCatalog.products.count { it.category == category }
            assertTrue("$category has only $count products", count >= 8)
        }
    }

    @Test
    fun `deal candidates are discounted and discount percent is sane`() {
        assertTrue(FakeCatalog.dealCandidates.isNotEmpty())
        FakeCatalog.dealCandidates.forEach { product ->
            val original = product.originalPriceCents!!
            assertTrue(product.priceCents < original)
            assertTrue(product.discountPercent!! in 1..99)
        }
    }

    @Test
    fun `cart item total multiplies price by quantity`() {
        val product = FakeCatalog.products.first()
        assertEquals(product.priceCents * 3, CartItem(product, 3).totalCents)
    }

    @Test
    fun `order counts items across lines`() {
        val items = FakeCatalog.products.take(2).map { CartItem(it, 2) }
        val order = Order(id = 1, items = items, totalCents = items.sumOf { it.totalCents })
        assertEquals(4, order.itemCount)
    }

    @Test
    fun `price override drops the price and keeps the old one as anchor`() {
        val product = FakeCatalog.products.first { it.originalPriceCents == null }
        val dropped = product.withPriceOverride(product.priceCents / 2)
        assertEquals(product.priceCents / 2, dropped.priceCents)
        assertEquals(product.priceCents, dropped.originalPriceCents)
    }

    @Test
    fun `price override keeps a higher existing sale anchor`() {
        val product = FakeCatalog.dealCandidates.first()
        val dropped = product.withPriceOverride(product.priceCents / 2)
        assertEquals(product.originalPriceCents, dropped.originalPriceCents)
    }

    @Test
    fun `price override ignores null and non-drops`() {
        val product = FakeCatalog.products.first()
        assertEquals(product, product.withPriceOverride(null))
        assertEquals(product, product.withPriceOverride(product.priceCents))
        assertEquals(product, product.withPriceOverride(product.priceCents * 2))
    }

    @Test
    fun `price override is idempotent on an already dropped product`() {
        val product = FakeCatalog.products.first()
        val price = product.priceCents / 3
        val once = product.withPriceOverride(price)
        assertEquals(once, once.withPriceOverride(price))
    }

    @Test
    fun `fake scarcity is stable, small, and hits part of the catalog`() {
        val scarce = FakeCatalog.products.filter { it.fakeStockLeft != null }
        assertTrue(scarce.isNotEmpty())
        assertTrue(scarce.size < FakeCatalog.products.size)
        scarce.forEach { product ->
            assertTrue(product.fakeStockLeft!! in 2..5)
            assertEquals(product.fakeStockLeft, product.fakeStockLeft) // deterministic
        }
    }

    @Test
    fun `adding a product appends a new line or merges into an existing one`() {
        val (first, second) = FakeCatalog.products.take(2)
        val cart = emptyList<CartItem>()
            .plusProduct(first, 1)
            .plusProduct(second, 2)
            .plusProduct(first, 3)
        assertEquals(2, cart.size)
        assertEquals(4, cart.first { it.product.id == first.id }.quantity)
        assertEquals(2, cart.first { it.product.id == second.id }.quantity)
    }

    @Test
    fun `merging keeps the original line's price snapshot`() {
        val product = FakeCatalog.products.first()
        val dropped = product.withPriceOverride(product.priceCents / 2)
        // First added at the dropped price, then again at full price after the
        // drop expired: the line keeps its original snapshot.
        val cart = emptyList<CartItem>().plusProduct(dropped, 1).plusProduct(product, 1)
        assertEquals(1, cart.size)
        assertEquals(2, cart.single().quantity)
        assertEquals(dropped.priceCents, cart.single().product.priceCents)
    }

    @Test
    fun `streak advances on consecutive days, holds same-day, restarts after a gap`() {
        assertEquals(4, advanceStreak(3, lastEpochDay = 100, todayEpochDay = 101))
        assertEquals(3, advanceStreak(3, lastEpochDay = 100, todayEpochDay = 100))
        assertEquals(1, advanceStreak(3, lastEpochDay = 100, todayEpochDay = 103))
        assertEquals(1, advanceStreak(0, lastEpochDay = 0, todayEpochDay = 20_000))
    }

    @Test
    fun `saved streak survives one missed day but not two`() {
        assertEquals(5, effectiveStreak(5, lastEpochDay = 100, todayEpochDay = 100))
        assertEquals(5, effectiveStreak(5, lastEpochDay = 100, todayEpochDay = 101))
        assertEquals(0, effectiveStreak(5, lastEpochDay = 100, todayEpochDay = 102))
    }

    @Test
    fun `every product carries 4 to 6 distinct reviews with sane ratings`() {
        FakeCatalog.products.forEach { product ->
            val reviews = product.reviews
            assertTrue("${product.name} has ${reviews.size} reviews", reviews.size in 4..6)
            assertEquals("${product.name} repeats a reviewer", reviews.size, reviews.toSet().size)
            reviews.forEach { assertTrue(it.rating in 1..5) }
        }
    }

    @Test
    fun `mystery reveal is deterministic, in-catalog, and never the box itself`() {
        (1..50).forEach { orderId ->
            val pick = FakeCatalog.mysteryRevealFor(orderId)
            assertEquals(pick, FakeCatalog.mysteryRevealFor(orderId))
            assertTrue(pick in FakeCatalog.products)
            assertTrue(pick.id != FakeCatalog.mysteryBox.id)
        }
    }

    @Test
    fun `card pull reveal is deterministic, game-matched, and for card games only`() {
        val pack = FakeCatalog.products.first { it.variantGroup == "critters-emberglow" }
        val pull = FakeCatalog.cardPullFor(7, pack)
        // Stable per (order, product), so revisiting the screen keeps the answer.
        assertEquals(pull, FakeCatalog.cardPullFor(7, pack))
        assertTrue(pull != null && pull.name.isNotBlank() && pull.rarity.isNotBlank())
        // Every trading-card game product pulls something...
        FakeCatalog.products
            .filter { it.category == "Trading Cards" && it.variantGroup != null }
            .forEach { assertTrue("${it.name} pulled nothing", FakeCatalog.cardPullFor(3, it) != null) }
        // ...and different orders eventually pull different cards.
        assertTrue((1..20).map { FakeCatalog.cardPullFor(it, pack) }.toSet().size > 1)
        // Accessories and non-card variant groups never pull.
        val sleeves = FakeCatalog.products.first { it.name.startsWith("Dragonhide") }
        assertEquals(null, FakeCatalog.cardPullFor(7, sleeves))
        val controller = FakeCatalog.products.first { it.variantGroup == "meteor-pro-controller" }
        assertEquals(null, FakeCatalog.cardPullFor(7, controller))
    }

    @Test
    fun `pack rip deals a full pack, chase card last, deterministically`() {
        val pack = FakeCatalog.products.first { it.variantGroup == "duelbound-eclipse" }
        val rip = FakeCatalog.packRipFor(11, pack)
        // Stable per (order, product) so a re-render never reshuffles the pack.
        assertEquals(rip, FakeCatalog.packRipFor(11, pack))
        assertTrue(rip != null && rip.size == 5)
        // The chase card is dealt last — commons first, payoff at the back —
        // and the commons are distinct and never duplicate the chase.
        assertEquals(FakeCatalog.cardPullFor(11, pack), rip!!.last())
        val commons = rip.dropLast(1)
        assertEquals(commons.size, commons.toSet().size)
        assertTrue(commons.none { it == rip.last() })
        // Every card-game product deals a pack; nothing else ever does.
        FakeCatalog.products
            .filter { it.category == "Trading Cards" && it.variantGroup != null }
            .forEach { assertTrue("${it.name} dealt no pack", FakeCatalog.packRipFor(2, it) != null) }
        val binder = FakeCatalog.products.first { it.name.startsWith("Cardkeeper") }
        assertEquals(null, FakeCatalog.packRipFor(11, binder))
    }

    @Test
    fun `multi-pack orders deal each pack differently but still deterministically`() {
        val pack = FakeCatalog.products.first { it.variantGroup == "duelbound-eclipse" }
        val first = FakeCatalog.packRipFor(11, pack, packIndex = 0)!!
        val second = FakeCatalog.packRipFor(11, pack, packIndex = 1)!!
        // A second pack of the same product is a fresh deal — different chase,
        // different commons — not the same five cards twice.
        assertTrue("pack 2 dealt identically to pack 1", first != second)
        assertTrue(first.last() != second.last())
        // Still seeded: the same pack index always re-deals the same cards.
        assertEquals(second, FakeCatalog.packRipFor(11, pack, packIndex = 1))
        // And the default index is pack 0, so older call sites keep their deal.
        assertEquals(first, FakeCatalog.packRipFor(11, pack))
    }

    @Test
    fun `binder codec round-trips and rejects garbage`() {
        val card = "critters" to "Emberwing, Ascendant"
        assertEquals(card, decodeBinderCard(encodeBinderCard(card.first, card.second)))
        assertEquals(null, decodeBinderCard(""))
        assertEquals(null, decodeBinderCard("no separator"))
        assertEquals(null, decodeBinderCard("nameless game"))
        assertEquals(null, decodeBinderCard("gameless name"))
    }

    @Test
    fun `every card game has a title and a non-empty chase checklist`() {
        // The binder UI iterates these; a game missing from either map would
        // silently vanish from the collection.
        val gamesInCatalog = FakeCatalog.products
            .filter { it.category == "Trading Cards" && it.variantGroup != null }
            .map { it.variantGroup!!.substringBefore('-') }
            .toSet()
        assertEquals(gamesInCatalog, FakeCatalog.cardGameTitles.keys)
        // Every series in the catalog has a display title for the wrapper.
        val seriesInCatalog = FakeCatalog.products
            .filter { it.category == "Trading Cards" && it.variantGroup != null }
            .map { it.variantGroup!! }
            .toSet()
        assertEquals(seriesInCatalog, FakeCatalog.cardSeriesTitles.keys)
        gamesInCatalog.forEach { game ->
            assertTrue("$game has no chase cards", FakeCatalog.chaseCardsOf(game).isNotEmpty())
            assertTrue("$game has no title", !FakeCatalog.cardGameTitles[game].isNullOrBlank())
        }
        // Chase names are unique within a game — they're the binder's keys.
        gamesInCatalog.forEach { game ->
            val names = FakeCatalog.chaseCardsOf(game).map { it.name }
            assertEquals("$game repeats a chase name", names.size, names.toSet().size)
            // The series-grouped checklist is the same cards, same order —
            // the binder's set pages can't drop or reshuffle anything.
            assertEquals(
                FakeCatalog.chaseCardsOf(game),
                FakeCatalog.chaseChecklistOf(game).flatMap { it.second },
            )
            FakeCatalog.chaseChecklistOf(game).forEach { (series, cards) ->
                assertTrue("$series has no chase cards", cards.isNotEmpty())
            }
        }
        // Every card that can ever be dealt carries flavor text and a type
        // line. Eight pack indexes walk the whole commons pool (the start
        // step is coprime with its size), and every card product is swept so
        // both series of a game get covered.
        FakeCatalog.products.filter { it.category == "Trading Cards" && it.variantGroup != null }.forEach { pack ->
            val game = pack.variantGroup!!.substringBefore('-')
            (0..7).flatMap { FakeCatalog.packRipFor(5, pack, it).orEmpty() }.forEach { card ->
                assertTrue("${card.name} has no flavor text", card.flavor.isNotBlank())
                assertTrue("${card.name} has no type line", card.type.isNotBlank())
                // Stats appear exactly where the genre prints them: every
                // Critter has HP; Manaforge P/T belongs to creatures alone;
                // Duelbound ATK/DEF belongs to monsters, never spells/traps.
                when (game) {
                    "critters" -> assertTrue(
                        "${card.name} should have HP",
                        Regex("""\d+ HP""").matches(card.stat),
                    )
                    "manaforge" -> assertEquals(
                        "${card.name} P/T must match creatureness",
                        card.type.contains("Creature"),
                        Regex("""\d+/\d+""").matches(card.stat),
                    )
                    "duelbound" -> {
                        if (card.stat.isNotBlank()) {
                            assertTrue(
                                "${card.name} has a malformed ATK/DEF",
                                Regex("""ATK/\d+ DEF/\d+""").matches(card.stat),
                            )
                        }
                        // The race is the first bracket segment ("Spellcaster"
                        // is a monster; "Spell" is not).
                        val race = card.type.removePrefix("[").substringBefore(" /").removeSuffix("]")
                        if (race in listOf("Spell", "Trap", "Relic")) {
                            assertTrue("${card.name} is a spell with stats", card.stat.isBlank())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `collector numbers are stable, well-formed, and unique per game`() {
        // Every game numbers per series — the set code names the series.
        val idioms = mapOf(
            "critters-emberglow" to Regex("""\d{3}/203"""),
            "critters-abyssal" to Regex("""\d{3}/198"""),
            "duelbound-archive" to Regex("""DAR-EN\d{3}"""),
            "duelbound-eclipse" to Regex("""DCE-EN\d{3}"""),
            "manaforge-ashveil" to Regex("""ASH · \d{4}/0184 [CUM]"""),
            "manaforge-verdant" to Regex("""VER · \d{4}/0166 [CUM]"""),
        )
        val numbersByGame = mutableMapOf<String, MutableMap<String, String>>()
        FakeCatalog.products.filter { it.category == "Trading Cards" && it.variantGroup != null }.forEach { pack ->
            val group = pack.variantGroup!!
            val game = group.substringBefore('-')
            val idiom = idioms[group] ?: idioms.getValue(game)
            // Eight pack indexes sweep the whole dealable set, commons + chases.
            (0..7).flatMap { FakeCatalog.packRipFor(5, pack, it).orEmpty() }.toSet().forEach { card ->
                val number = FakeCatalog.collectorNumberOf(game, card)
                // Stable — the binder's permanent record never renumbers.
                assertEquals(number, FakeCatalog.collectorNumberOf(game, card))
                assertTrue("'$number' (${card.name}) breaks the $group idiom", idiom.matches(number))
                numbersByGame.getOrPut(game) { mutableMapOf() }[card.name] = number
            }
        }
        // No two cards in a game share a print — series codes/sizes differ,
        // and within a series each card has its own checklist slot.
        numbersByGame.forEach { (game, byName) ->
            assertEquals("$game repeats a collector number", byName.size, byName.values.toSet().size)
        }
        assertEquals("", FakeCatalog.collectorNumberOf("not-a-game", FakeCatalog.chaseCardsOf("critters").first()))
    }

    @Test
    fun `blind boxes deal seeded figures that honor the listing's lineup claims`() {
        // The copy states the lineup sizes as fact — the waves must agree:
        // twelve Bog Friends, twelve Cloud Court, eight charms, ten gremlins.
        val sizes = mapOf("bog" to 12, "cloud" to 12, "charm" to 8, "gremlin" to 10)
        assertEquals(sizes.keys, FakeCatalog.mopplingWaves.map { it.key }.toSet())
        FakeCatalog.mopplingWaves.forEach { wave ->
            assertEquals("${wave.title} lineup size", sizes.getValue(wave.key), wave.figures.size)
            val names = wave.figures.map { it.name }
            assertEquals("${wave.title} repeats a figure", names.size, names.toSet().size)
        }
        val single = FakeCatalog.products.first { it.name == "Moppling Blind Box: Bog Friends Series" }
        val sixPack = FakeCatalog.products.first { it.name.contains("Cloud Court Series (6-Pack)") }
        // Stable per (order, product, box) and box-varied, like the pack rips.
        val (wave, figures) = FakeCatalog.mopplingPullsFor(11, single)!!
        assertEquals("bog", wave.key)
        assertEquals(1, figures.size)
        assertEquals(wave to figures, FakeCatalog.mopplingPullsFor(11, single))
        assertTrue(figures != FakeCatalog.mopplingPullsFor(11, single, boxIndex = 1)!!.second)
        // A 6-pack opens six distinct figures from its own wave.
        val (cloudWave, six) = FakeCatalog.mopplingPullsFor(11, sixPack)!!
        assertEquals("cloud", cloudWave.key)
        assertEquals(6, six.size)
        assertEquals(6, six.toSet().size)
        // Nothing else is a blind box.
        val tote = FakeCatalog.products.first { it.name == "Everyday Leather Tote" }
        assertEquals(null, FakeCatalog.mopplingPullsFor(11, tote))
    }

    @Test
    fun `trading-card grids lead with the entry-price pack, not the sealed display`() {
        val groups = FakeCatalog.products
            .filter { it.category == "Trading Cards" && it.variantGroup != null }
            .groupBy { it.variantGroup!! }
        assertTrue("expected several card series", groups.size >= 6)
        groups.forEach { (group, members) ->
            // Formats span pack → display → tin/box on the Format axis...
            assertTrue("$group has too few formats", members.size >= 3)
            assertTrue("$group is not on the Format axis", members.all { it.variantAxis == "Format" })
            // ...and the grid representative (lowest id) is the cheapest way in.
            val rep = members.minByOrNull { it.id }!!
            assertEquals("$group leads with the wrong format", members.minOf { it.priceCents }, rep.priceCents)
        }
    }

    @Test
    fun `currencies convert and format a USD-cent base correctly`() {
        // $1,299.00 base across the three currencies.
        assertEquals("$1,299.00", Currency.USD.format(129_900))
        assertEquals("€1,195.08", Currency.EUR.format(129_900)) // 1299 * 0.92
        assertEquals("₩1,753,650", Currency.KRW.format(129_900)) // 1299 * 1350
        assertEquals("£1,026.21", Currency.GBP.format(129_900)) // 1299 * 0.79
        assertEquals("¥194,850", Currency.JPY.format(129_900)) // 1299 * 150, no decimals
        assertEquals("CN¥9,352.80", Currency.CNY.format(129_900)) // 1299 * 7.2
        assertEquals("CA$1,766.64", Currency.CAD.format(129_900)) // 1299 * 1.36
        assertEquals("A$1,974.48", Currency.AUD.format(129_900)) // 1299 * 1.52

        // INR groups the Indian way (lakh): 1299 * 83 = 107,817 -> ₹1,07,817.00.
        assertEquals("₹1,07,817.00", Currency.INR.format(129_900))
        // BRL flips the separators: 1299 * 5 = 6,495 -> R$6.495,00.
        assertEquals("R$6.495,00", Currency.BRL.format(129_900))

        // KRW carries no decimals and rounds to a tidy figure (47,115 -> 47,120).
        assertEquals("₩47,120", Currency.KRW.format(3_490))
        assertFalse("KRW must not show decimals", Currency.KRW.format(3_490).contains("."))

        // Zero is still a clean price, not a stray "-" or blank.
        assertEquals("$0.00", Currency.USD.format(0))
        assertEquals("₩0", Currency.KRW.format(0))
    }

    @Test
    fun `whole-unit formatting drops the cents for count-up animations`() {
        // $279.00 base, the count-up the success and tracking screens tick to.
        assertEquals(279L, Currency.USD.majorUnits(27_900))
        assertEquals(256L, Currency.EUR.majorUnits(27_900)) // 256.68 truncates
        assertEquals(376_650L, Currency.KRW.majorUnits(27_900)) // whole won

        assertEquals("$279", Currency.USD.formatMajorUnits(279))
        assertEquals("€256", Currency.EUR.formatMajorUnits(256))
        assertEquals("₩376,650", Currency.KRW.formatMajorUnits(376_650))
        assertEquals("$0", Currency.USD.formatMajorUnits(0))
        assertEquals("₩0", Currency.KRW.formatMajorUnits(0))
    }

    @Test
    fun `currency code lookup round-trips and defaults to USD`() {
        Currency.entries.forEach { assertEquals(it, Currency.fromCode(it.code)) }
        assertEquals(Currency.USD, Currency.fromCode(null))
        assertEquals(Currency.USD, Currency.fromCode("not a currency"))
    }

    @Test
    fun `the low-star satire actually appears somewhere in the catalog`() {
        val allShown = FakeCatalog.products.flatMap { it.reviews }
        assertTrue(allShown.any { it.rating <= 3 })
    }

    @Test
    fun `listing and review copy carries no em-dashes`() {
        // The shop copy is held to the no-slop register: the em-dash-as-aside
        // tell stays out of every tagline, description, and review a shopper
        // reads. (Card type lines like "Legendary Creature - Wizard" keep their
        // dash, but those live on CardPull.type, not in any field checked here.)
        val emDash = '—'
        FakeCatalog.products.forEach { product ->
            assertFalse("${product.name} tagline has an em-dash", emDash in product.tagline)
            assertFalse("${product.name} description has an em-dash", emDash in product.description)
            product.reviews.forEach { review ->
                assertFalse(
                    "review by ${review.author} on ${product.name} has an em-dash: ${review.text}",
                    emDash in review.text,
                )
            }
        }
    }

    @Test
    fun `user review codec round-trips, including hostile text`() {
        val nasty = UserReview(42, 3, "lines\nand|pipes and \"quotes\" and 🦖", 1_765_000_000_000)
        assertEquals(nasty, decodeUserReview(encodeUserReview(nasty)))
        val empty = UserReview(0, 5, "", 1L)
        assertEquals(empty, decodeUserReview(encodeUserReview(empty)))
    }

    @Test
    fun `user review codec rejects garbage instead of crashing`() {
        assertEquals(null, decodeUserReview(""))
        assertEquals(null, decodeUserReview("not a review"))
        assertEquals(null, decodeUserReview("12\u00019"))
        assertEquals(null, decodeUserReview("x\u00015\u00011\u0001text"))
        assertEquals(null, decodeUserReview("1\u00019\u00011\u0001rating out of range"))
    }

    @Test
    fun `quiet hours cover the night and only the night`() {
        listOf(22, 23, 0, 3, 7).forEach { assertTrue("$it should be quiet", NotificationPolicy.isQuietHour(it)) }
        listOf(8, 12, 18, 21).forEach { assertFalse("$it should be awake", NotificationPolicy.isQuietHour(it)) }
    }

    @Test
    fun `delivery pings only reach a backgrounded app`() {
        assertTrue(NotificationPolicy.shouldPingDelivery(appVisible = false))
        assertFalse(NotificationPolicy.shouldPingDelivery(appVisible = true))
    }

    @Test
    fun `price-drop ping fires once the app is away, it is daytime, and the cooldown passed`() {
        val cooldown = NotificationPolicy.DROP_PING_COOLDOWN_MILLIS
        assertTrue(
            NotificationPolicy.shouldPingPriceDrop(
                appVisible = false, hourOfDay = 14, lastPingMillis = 0, nowMillis = cooldown,
            ),
        )
    }

    @Test
    fun `price-drop ping stays silent while the app is open`() {
        assertFalse(
            NotificationPolicy.shouldPingPriceDrop(
                appVisible = true, hourOfDay = 14, lastPingMillis = 0,
                nowMillis = NotificationPolicy.DROP_PING_COOLDOWN_MILLIS,
            ),
        )
    }

    @Test
    fun `price-drop ping stays silent at night`() {
        assertFalse(
            NotificationPolicy.shouldPingPriceDrop(
                appVisible = false, hourOfDay = 23, lastPingMillis = 0,
                nowMillis = NotificationPolicy.DROP_PING_COOLDOWN_MILLIS,
            ),
        )
    }

    @Test
    fun `price-drop ping respects the cooldown window`() {
        val cooldown = NotificationPolicy.DROP_PING_COOLDOWN_MILLIS
        assertFalse(
            NotificationPolicy.shouldPingPriceDrop(
                appVisible = false, hourOfDay = 14, lastPingMillis = 1_000, nowMillis = cooldown,
            ),
        )
        assertTrue(
            NotificationPolicy.shouldPingPriceDrop(
                appVisible = false, hourOfDay = 14, lastPingMillis = 1_000, nowMillis = cooldown + 1_000,
            ),
        )
    }

    @Test
    fun `price formatting groups thousands and pads cents`() {
        assertEquals("$0.00", formatPrice(0))
        assertEquals("$3.07", formatPrice(307))
        assertEquals("$1,299.00", formatPrice(129_900))
        assertEquals("$3,999.00", formatPrice(399_900))
    }

    @Test
    fun `tracking codes are stable, courier-shaped, and free of ambiguous glyphs`() {
        val shape = Regex("""#[A-HJ-NP-Z2-9]{5}-CT\d{3}""")
        (0..200).forEach { id ->
            val code = trackingCode(id)
            assertTrue("'$code' breaks the courier shape", shape.matches(code))
            // Stable: a revisit to the same order shows the same code.
            assertEquals(code, trackingCode(id))
        }
        // No I/O/0/1 anywhere in the seeded block — the carrier-style omission.
        (0..200).forEach { id ->
            val block = trackingCode(id).substringAfter('#').substringBefore('-')
            assertTrue("'$block' has an ambiguous glyph", block.none { it in "IO01" })
        }
    }

    @Test
    fun `every order status carries a short uppercase badge`() {
        OrderStatus.entries.forEach { status ->
            assertTrue("${status.name} badge is blank", status.badge.isNotBlank())
            assertEquals("${status.name} badge not uppercase", status.badge.uppercase(), status.badge)
            assertTrue("${status.name} badge too long for a pill", status.badge.length <= 10)
        }
        assertEquals("TRANSIT", OrderStatus.ON_THE_WAY.badge)
        assertEquals("ARRIVED", OrderStatus.DELIVERED.badge)
    }

    @Test
    fun `courier assignment is deterministic, in-roster, regular-dominant, rocket-rare`() {
        val regular = Couriers.dario.id
        val picks = (1..5000).map { Couriers.forOrder(it, regular) }
        // A revisit to the same order shows the same courier.
        assertEquals(Couriers.forOrder(42, regular), Couriers.forOrder(42, regular))
        assertTrue("assigned a courier off the roster", picks.all { it in Couriers.all })
        val byId = picks.groupingBy { it.id }.eachCount()
        // The regular shows up more than anyone, and more than half the time.
        assertEquals(regular, byId.maxByOrNull { it.value }!!.key)
        assertTrue("regular should dominate", byId.getValue(regular) > picks.size / 2)
        // The rocket courier is a real but rare guest.
        val rocket = byId[Couriers.vega.id] ?: 0
        assertTrue("rocket courier too common", rocket < picks.size / 6)
        assertTrue("rocket courier never appeared", rocket > 0)
        // The other regulars turn up as occasional guests.
        Couriers.commons.filter { it.id != regular }.forEach {
            assertTrue("${it.name} never guested", (byId[it.id] ?: 0) > 0)
        }
    }

    @Test
    fun `every courier is a well-formed person`() {
        val ids = Couriers.all.map { it.id }
        assertEquals("courier ids not unique", ids.size, ids.toSet().size)
        val names = Couriers.all.map { it.name }
        assertEquals("courier names not unique", names.size, names.toSet().size)
        Couriers.all.forEach { c ->
            assertTrue("${c.id} has blank fields", c.name.isNotBlank() && c.avatar.isNotBlank())
            assertTrue(
                "${c.name} missing ride/bio/note",
                c.vehicle.isNotBlank() && c.tagline.isNotBlank() && c.signoff.isNotBlank(),
            )
            assertTrue("${c.name} has too few moments", c.moments.size >= 4)
            assertTrue("${c.name} has a blank moment", c.moments.all { it.isNotBlank() })
            assertTrue("${c.name} rating out of range", c.rating in 4.0..5.0)
        }
        // pickRegular only ever returns a regular, never the rare rocket courier.
        (0L..20L).forEach { assertTrue(Couriers.pickRegular(it) in Couriers.commons) }
        assertEquals(Couriers.minjun, Couriers.byId("minjun"))
        assertEquals(Couriers.minjun, Couriers.byId("not-a-courier")) // safe fallback
    }

    @Test
    fun `ordinal and the relationship line read right`() {
        listOf(
            1 to "1st", 2 to "2nd", 3 to "3rd", 4 to "4th", 11 to "11th", 12 to "12th", 13 to "13th", 21 to "21st",
            22 to "22nd", 23 to "23rd",
        )
            .forEach { (n, s) -> assertEquals(s, ordinal(n)) }
        // The first meeting reads warmly; later ones count up.
        assertEquals("Your first delivery with Min-jun", deliveriesTogetherLine("Min-jun", 0))
        assertEquals("Your first delivery with Min-jun", deliveriesTogetherLine("Min-jun", 1))
        assertEquals("Your 4th delivery with Aria", deliveriesTogetherLine("Aria", 4))
    }
}
