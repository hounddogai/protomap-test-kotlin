package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal dispute tiers by code. */
interface SeasonalDisputeTierStore {
    fun find(code: String): SeasonalDisputeTier?
    fun save(item: SeasonalDisputeTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDisputeTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDisputeTierStore(
    var items: MutableMap<String, SeasonalDisputeTier>,
    var order: MutableList<String>,
) : SeasonalDisputeTierStore {
    override fun find(code: String): SeasonalDisputeTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDisputeTier) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<SeasonalDisputeTier> {
        val out = mutableListOf<SeasonalDisputeTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDisputeTierStore(): SeasonalDisputeTierStore {
    return MemorySeasonalDisputeTierStore(mutableMapOf<String, SeasonalDisputeTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDisputeTierStore(
    var inner: SeasonalDisputeTierStore,
    var cache: TtlCache<SeasonalDisputeTier>,
    var hits: Int,
    var misses: Int,
) : SeasonalDisputeTierStore {
    override fun find(code: String): SeasonalDisputeTier? {
        val hit = (cache.get(code) as SeasonalDisputeTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDisputeTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDisputeTier> = inner.list()
}

fun newCachedSeasonalDisputeTierStore(inner: SeasonalDisputeTierStore, ttl: Int, clock: Clock): SeasonalDisputeTierStore {
    return CachedSeasonalDisputeTierStore(
        inner = inner,
        cache = newTtlCache<SeasonalDisputeTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal dispute tiers. Empty fields match all. */
data class SeasonalDisputeTierQuery(
    var region: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDisputeTier): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDisputeTier>): MutableList<SeasonalDisputeTier> {
        val matched = filter(items) { candidate: SeasonalDisputeTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDisputeTiers(store: SeasonalDisputeTierStore, query: SeasonalDisputeTierQuery): MutableList<SeasonalDisputeTier> {
    return query.apply(store.list())
}
