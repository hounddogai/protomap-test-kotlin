package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted roster tiers by code. */
interface WeightedRosterTierStore {
    fun find(code: String): WeightedRosterTier?
    fun save(item: WeightedRosterTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRosterTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRosterTierStore(
    var items: MutableMap<String, WeightedRosterTier>,
    var order: MutableList<String>,
) : WeightedRosterTierStore {
    override fun find(code: String): WeightedRosterTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRosterTier) {
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

    override fun list(): MutableList<WeightedRosterTier> {
        val out = mutableListOf<WeightedRosterTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRosterTierStore(): WeightedRosterTierStore {
    return MemoryWeightedRosterTierStore(mutableMapOf<String, WeightedRosterTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRosterTierStore(
    var inner: WeightedRosterTierStore,
    var cache: TtlCache<WeightedRosterTier>,
    var hits: Int,
    var misses: Int,
) : WeightedRosterTierStore {
    override fun find(code: String): WeightedRosterTier? {
        val hit = (cache.get(code) as WeightedRosterTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRosterTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRosterTier> = inner.list()
}

fun newCachedWeightedRosterTierStore(inner: WeightedRosterTierStore, ttl: Int, clock: Clock): WeightedRosterTierStore {
    return CachedWeightedRosterTierStore(
        inner = inner,
        cache = newTtlCache<WeightedRosterTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted roster tiers. Empty fields match all. */
data class WeightedRosterTierQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRosterTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRosterTier>): MutableList<WeightedRosterTier> {
        val matched = filter(items) { candidate: WeightedRosterTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRosterTiers(store: WeightedRosterTierStore, query: WeightedRosterTierQuery): MutableList<WeightedRosterTier> {
    return query.apply(store.list())
}
