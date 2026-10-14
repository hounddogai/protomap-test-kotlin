package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal accessorial tiers by code. */
interface SeasonalAccessorialTierStore {
    fun find(code: String): SeasonalAccessorialTier?
    fun save(item: SeasonalAccessorialTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAccessorialTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAccessorialTierStore(
    var items: MutableMap<String, SeasonalAccessorialTier>,
    var order: MutableList<String>,
) : SeasonalAccessorialTierStore {
    override fun find(code: String): SeasonalAccessorialTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAccessorialTier) {
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

    override fun list(): MutableList<SeasonalAccessorialTier> {
        val out = mutableListOf<SeasonalAccessorialTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAccessorialTierStore(): SeasonalAccessorialTierStore {
    return MemorySeasonalAccessorialTierStore(
        mutableMapOf<String, SeasonalAccessorialTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAccessorialTierStore(
    var inner: SeasonalAccessorialTierStore,
    var cache: TtlCache<SeasonalAccessorialTier>,
    var hits: Int,
    var misses: Int,
) : SeasonalAccessorialTierStore {
    override fun find(code: String): SeasonalAccessorialTier? {
        val hit = (cache.get(code) as SeasonalAccessorialTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAccessorialTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAccessorialTier> = inner.list()
}

fun newCachedSeasonalAccessorialTierStore(inner: SeasonalAccessorialTierStore, ttl: Int, clock: Clock): SeasonalAccessorialTierStore {
    return CachedSeasonalAccessorialTierStore(
        inner = inner,
        cache = newTtlCache<SeasonalAccessorialTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal accessorial tiers. Empty fields match all. */
data class SeasonalAccessorialTierQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAccessorialTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAccessorialTier>): MutableList<SeasonalAccessorialTier> {
        val matched = filter(items) { candidate: SeasonalAccessorialTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAccessorialTiers(store: SeasonalAccessorialTierStore, query: SeasonalAccessorialTierQuery): MutableList<SeasonalAccessorialTier> {
    return query.apply(store.list())
}
