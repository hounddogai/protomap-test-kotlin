package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal emission factor cycles by code. */
interface SeasonalEmissionFactorCycleStore {
    fun find(code: String): SeasonalEmissionFactorCycle?
    fun save(item: SeasonalEmissionFactorCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalEmissionFactorCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalEmissionFactorCycleStore(
    var items: MutableMap<String, SeasonalEmissionFactorCycle>,
    var order: MutableList<String>,
) : SeasonalEmissionFactorCycleStore {
    override fun find(code: String): SeasonalEmissionFactorCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalEmissionFactorCycle) {
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

    override fun list(): MutableList<SeasonalEmissionFactorCycle> {
        val out = mutableListOf<SeasonalEmissionFactorCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalEmissionFactorCycleStore(): SeasonalEmissionFactorCycleStore {
    return MemorySeasonalEmissionFactorCycleStore(
        mutableMapOf<String, SeasonalEmissionFactorCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalEmissionFactorCycleStore(
    var inner: SeasonalEmissionFactorCycleStore,
    var cache: TtlCache<SeasonalEmissionFactorCycle>,
    var hits: Int,
    var misses: Int,
) : SeasonalEmissionFactorCycleStore {
    override fun find(code: String): SeasonalEmissionFactorCycle? {
        val hit = (cache.get(code) as SeasonalEmissionFactorCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalEmissionFactorCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalEmissionFactorCycle> = inner.list()
}

fun newCachedSeasonalEmissionFactorCycleStore(inner: SeasonalEmissionFactorCycleStore, ttl: Int, clock: Clock): SeasonalEmissionFactorCycleStore {
    return CachedSeasonalEmissionFactorCycleStore(
        inner = inner,
        cache = newTtlCache<SeasonalEmissionFactorCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal emission factor cycles. Empty fields match all. */
data class SeasonalEmissionFactorCycleQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalEmissionFactorCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalEmissionFactorCycle>): MutableList<SeasonalEmissionFactorCycle> {
        val matched = filter(items) { candidate: SeasonalEmissionFactorCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalEmissionFactorCycles(store: SeasonalEmissionFactorCycleStore, query: SeasonalEmissionFactorCycleQuery): MutableList<SeasonalEmissionFactorCycle> {
    return query.apply(store.list())
}
