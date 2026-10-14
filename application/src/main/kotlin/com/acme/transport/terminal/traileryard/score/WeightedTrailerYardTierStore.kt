package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted trailer yard tiers by code. */
interface WeightedTrailerYardTierStore {
    fun find(code: String): WeightedTrailerYardTier?
    fun save(item: WeightedTrailerYardTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTrailerYardTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTrailerYardTierStore(
    var items: MutableMap<String, WeightedTrailerYardTier>,
    var order: MutableList<String>,
) : WeightedTrailerYardTierStore {
    override fun find(code: String): WeightedTrailerYardTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTrailerYardTier) {
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

    override fun list(): MutableList<WeightedTrailerYardTier> {
        val out = mutableListOf<WeightedTrailerYardTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTrailerYardTierStore(): WeightedTrailerYardTierStore {
    return MemoryWeightedTrailerYardTierStore(
        mutableMapOf<String, WeightedTrailerYardTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTrailerYardTierStore(
    var inner: WeightedTrailerYardTierStore,
    var cache: TtlCache<WeightedTrailerYardTier>,
    var hits: Int,
    var misses: Int,
) : WeightedTrailerYardTierStore {
    override fun find(code: String): WeightedTrailerYardTier? {
        val hit = (cache.get(code) as WeightedTrailerYardTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTrailerYardTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTrailerYardTier> = inner.list()
}

fun newCachedWeightedTrailerYardTierStore(inner: WeightedTrailerYardTierStore, ttl: Int, clock: Clock): WeightedTrailerYardTierStore {
    return CachedWeightedTrailerYardTierStore(
        inner = inner,
        cache = newTtlCache<WeightedTrailerYardTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted trailer yard tiers. Empty fields match all. */
data class WeightedTrailerYardTierQuery(
    var channel: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTrailerYardTier): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTrailerYardTier>): MutableList<WeightedTrailerYardTier> {
        val matched = filter(items) { candidate: WeightedTrailerYardTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTrailerYardTiers(store: WeightedTrailerYardTierStore, query: WeightedTrailerYardTierQuery): MutableList<WeightedTrailerYardTier> {
    return query.apply(store.list())
}
