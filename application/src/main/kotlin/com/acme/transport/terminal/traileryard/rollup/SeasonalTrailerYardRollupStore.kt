package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal trailer yard rollups by code. */
interface SeasonalTrailerYardRollupStore {
    fun find(code: String): SeasonalTrailerYardRollup?
    fun save(item: SeasonalTrailerYardRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerYardRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerYardRollupStore(
    var items: MutableMap<String, SeasonalTrailerYardRollup>,
    var order: MutableList<String>,
) : SeasonalTrailerYardRollupStore {
    override fun find(code: String): SeasonalTrailerYardRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerYardRollup) {
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

    override fun list(): MutableList<SeasonalTrailerYardRollup> {
        val out = mutableListOf<SeasonalTrailerYardRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerYardRollupStore(): SeasonalTrailerYardRollupStore {
    return MemorySeasonalTrailerYardRollupStore(
        mutableMapOf<String, SeasonalTrailerYardRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerYardRollupStore(
    var inner: SeasonalTrailerYardRollupStore,
    var cache: TtlCache<SeasonalTrailerYardRollup>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerYardRollupStore {
    override fun find(code: String): SeasonalTrailerYardRollup? {
        val hit = (cache.get(code) as SeasonalTrailerYardRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerYardRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerYardRollup> = inner.list()
}

fun newCachedSeasonalTrailerYardRollupStore(inner: SeasonalTrailerYardRollupStore, ttl: Int, clock: Clock): SeasonalTrailerYardRollupStore {
    return CachedSeasonalTrailerYardRollupStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerYardRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer yard rollups. Empty fields match all. */
data class SeasonalTrailerYardRollupQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerYardRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerYardRollup>): MutableList<SeasonalTrailerYardRollup> {
        val matched = filter(items) { candidate: SeasonalTrailerYardRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerYardRollups(store: SeasonalTrailerYardRollupStore, query: SeasonalTrailerYardRollupQuery): MutableList<SeasonalTrailerYardRollup> {
    return query.apply(store.list())
}
