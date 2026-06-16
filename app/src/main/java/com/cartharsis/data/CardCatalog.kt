package com.cartharsis.data

/** A fantasy card "pulled" from a delivered trading-card order. */
data class CardPull(
    val emoji: String,
    val name: String,
    val rarity: String,
    /** The one-line flavor text under the art, deadpan, like the listings. */
    val flavor: String = "",
    /**
     * The type line between art and text box, in the idiom of the real game
     * each invented one homages: Pocket Critters wears Pokémon stage lines
     * ("Basic Flame Critter"), Duelbound wears Yu-Gi-Oh brackets
     * ("[Spellcaster / Effect]"), Manaforge wears Magic's em-dash
     * ("Legendary Creature, Human Wizard").
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

/**
 * The trading-card engine, lifted out of [FakeCatalog] when that file outgrew
 * a comfortable read. Pure and self-contained: every function reads only a
 * product's [Product.variantGroup], never the catalog's product list, so it
 * has no reason to share a file with the listings. [FakeCatalog] forwards this
 * object's public surface, so existing call sites still go through the facade.
 */
object CardCatalog {
    /**
     * The chase cards, keyed by series (variant group), each series is a
     * themed set with its own content language, the way real expansions
     * have their own world. The wrapper already names the series; the cards
     * inside belong to it.
     *
     * The six content languages:
     * - Emberglow (critters): hearth, dawn, kept warmth, critters with
     *   cozy habits. Flavor: gentle, domestic, one wink per card.
     * - Abyssal Tides (critters): deep water, night currents, soft glow in
     *   the dark. Flavor: serene, drifting, unhurried.
     * - Forbidden Archive (duelbound): a haunted library. The dread is
     *   bureaucratic, indexes, late fees, shushing.
     * - Crimson Eclipse (duelbound): a blood-moon vigil. Apocalyptic but
     *   unbothered; everything is on schedule.
     * - Ashveil (manaforge): the volcanic forge. Flavor reads as smithing
     *   proverbs, work and patience.
     * - The Verdant Throne (manaforge): a fallen court overgrown, royal
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
     * Every chase card a game can pull, the binder's checklist, in series
     * order.
     */
    fun chaseCardsOf(game: String): List<CardPull> = seriesGroupsOf(game).flatMap { cardPullPools[it].orEmpty() }

    /** The same checklist grouped by series title, the binder's set pages. */
    fun chaseChecklistOf(game: String): List<Pair<String, List<CardPull>>> =
        seriesGroupsOf(game).map { cardSeriesTitles.getValue(it) to cardPullPools[it].orEmpty() }

    /**
     * The tiny collector print in the card's bottom corner, in each genre's
     * idiom, and now per series, the way real expansions number their own
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
            // valley, 198 of the trench), the copy and the card agree.
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
     * the chase card dealt last, commons first, the payoff at the back, the
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
}
