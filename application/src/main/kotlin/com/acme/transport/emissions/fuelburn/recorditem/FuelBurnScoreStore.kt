package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn scores by code. */
interface FuelBurnScoreStore {
    fun find(code: String): FuelBurnScore?
    fun save(item: FuelBurnScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnScoreStore(
    var items: MutableMap<String, FuelBurnScore>,
    var order: MutableList<String>,
) : FuelBurnScoreStore {
    override fun find(code: String): FuelBurnScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnScore) {
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

    override fun list(): MutableList<FuelBurnScore> {
        val out = mutableListOf<FuelBurnScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnScoreStore(): FuelBurnScoreStore {
    return MemoryFuelBurnScoreStore(mutableMapOf<String, FuelBurnScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnScoreStore(
    var inner: FuelBurnScoreStore,
    var cache: TtlCache<FuelBurnScore>,
    var hits: Int,
    var misses: Int,
) : FuelBurnScoreStore {
    override fun find(code: String): FuelBurnScore? {
        val hit = (cache.get(code) as FuelBurnScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnScore> = inner.list()
}

fun newCachedFuelBurnScoreStore(inner: FuelBurnScoreStore, ttl: Int, clock: Clock): FuelBurnScoreStore {
    return CachedFuelBurnScoreStore(
        inner = inner,
        cache = newTtlCache<FuelBurnScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn scores. Empty fields match all. */
data class FuelBurnScoreQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnScore>): MutableList<FuelBurnScore> {
        val matched = filter(items) { candidate: FuelBurnScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnScores(store: FuelBurnScoreStore, query: FuelBurnScoreQuery): MutableList<FuelBurnScore> {
    return query.apply(store.list())
}
