package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive trailer yard rollups by code. */
interface AdaptiveTrailerYardRollupStore {
    fun find(code: String): AdaptiveTrailerYardRollup?
    fun save(item: AdaptiveTrailerYardRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTrailerYardRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTrailerYardRollupStore(
    var items: MutableMap<String, AdaptiveTrailerYardRollup>,
    var order: MutableList<String>,
) : AdaptiveTrailerYardRollupStore {
    override fun find(code: String): AdaptiveTrailerYardRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTrailerYardRollup) {
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

    override fun list(): MutableList<AdaptiveTrailerYardRollup> {
        val out = mutableListOf<AdaptiveTrailerYardRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTrailerYardRollupStore(): AdaptiveTrailerYardRollupStore {
    return MemoryAdaptiveTrailerYardRollupStore(
        mutableMapOf<String, AdaptiveTrailerYardRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTrailerYardRollupStore(
    var inner: AdaptiveTrailerYardRollupStore,
    var cache: TtlCache<AdaptiveTrailerYardRollup>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTrailerYardRollupStore {
    override fun find(code: String): AdaptiveTrailerYardRollup? {
        val hit = (cache.get(code) as AdaptiveTrailerYardRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTrailerYardRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTrailerYardRollup> = inner.list()
}

fun newCachedAdaptiveTrailerYardRollupStore(inner: AdaptiveTrailerYardRollupStore, ttl: Int, clock: Clock): AdaptiveTrailerYardRollupStore {
    return CachedAdaptiveTrailerYardRollupStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTrailerYardRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive trailer yard rollups. Empty fields match all. */
data class AdaptiveTrailerYardRollupQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTrailerYardRollup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTrailerYardRollup>): MutableList<AdaptiveTrailerYardRollup> {
        val matched = filter(items) { candidate: AdaptiveTrailerYardRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTrailerYardRollups(store: AdaptiveTrailerYardRollupStore, query: AdaptiveTrailerYardRollupQuery): MutableList<AdaptiveTrailerYardRollup> {
    return query.apply(store.list())
}
