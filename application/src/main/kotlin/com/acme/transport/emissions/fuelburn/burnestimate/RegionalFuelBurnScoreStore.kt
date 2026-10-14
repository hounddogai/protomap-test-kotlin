package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel burn scores by code. */
interface RegionalFuelBurnScoreStore {
    fun find(code: String): RegionalFuelBurnScore?
    fun save(item: RegionalFuelBurnScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelBurnScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelBurnScoreStore(
    var items: MutableMap<String, RegionalFuelBurnScore>,
    var order: MutableList<String>,
) : RegionalFuelBurnScoreStore {
    override fun find(code: String): RegionalFuelBurnScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelBurnScore) {
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

    override fun list(): MutableList<RegionalFuelBurnScore> {
        val out = mutableListOf<RegionalFuelBurnScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelBurnScoreStore(): RegionalFuelBurnScoreStore {
    return MemoryRegionalFuelBurnScoreStore(
        mutableMapOf<String, RegionalFuelBurnScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelBurnScoreStore(
    var inner: RegionalFuelBurnScoreStore,
    var cache: TtlCache<RegionalFuelBurnScore>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelBurnScoreStore {
    override fun find(code: String): RegionalFuelBurnScore? {
        val hit = (cache.get(code) as RegionalFuelBurnScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelBurnScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelBurnScore> = inner.list()
}

fun newCachedRegionalFuelBurnScoreStore(inner: RegionalFuelBurnScoreStore, ttl: Int, clock: Clock): RegionalFuelBurnScoreStore {
    return CachedRegionalFuelBurnScoreStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelBurnScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel burn scores. Empty fields match all. */
data class RegionalFuelBurnScoreQuery(
    var unit: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelBurnScore): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<RegionalFuelBurnScore>): MutableList<RegionalFuelBurnScore> {
        val matched = filter(items) { candidate: RegionalFuelBurnScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelBurnScores(store: RegionalFuelBurnScoreStore, query: RegionalFuelBurnScoreQuery): MutableList<RegionalFuelBurnScore> {
    return query.apply(store.list())
}
