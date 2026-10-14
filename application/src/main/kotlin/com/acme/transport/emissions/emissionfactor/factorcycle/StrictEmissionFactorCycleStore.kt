package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict emission factor cycles by code. */
interface StrictEmissionFactorCycleStore {
    fun find(code: String): StrictEmissionFactorCycle?
    fun save(item: StrictEmissionFactorCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictEmissionFactorCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictEmissionFactorCycleStore(
    var items: MutableMap<String, StrictEmissionFactorCycle>,
    var order: MutableList<String>,
) : StrictEmissionFactorCycleStore {
    override fun find(code: String): StrictEmissionFactorCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictEmissionFactorCycle) {
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

    override fun list(): MutableList<StrictEmissionFactorCycle> {
        val out = mutableListOf<StrictEmissionFactorCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictEmissionFactorCycleStore(): StrictEmissionFactorCycleStore {
    return MemoryStrictEmissionFactorCycleStore(
        mutableMapOf<String, StrictEmissionFactorCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictEmissionFactorCycleStore(
    var inner: StrictEmissionFactorCycleStore,
    var cache: TtlCache<StrictEmissionFactorCycle>,
    var hits: Int,
    var misses: Int,
) : StrictEmissionFactorCycleStore {
    override fun find(code: String): StrictEmissionFactorCycle? {
        val hit = (cache.get(code) as StrictEmissionFactorCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictEmissionFactorCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictEmissionFactorCycle> = inner.list()
}

fun newCachedStrictEmissionFactorCycleStore(inner: StrictEmissionFactorCycleStore, ttl: Int, clock: Clock): StrictEmissionFactorCycleStore {
    return CachedStrictEmissionFactorCycleStore(
        inner = inner,
        cache = newTtlCache<StrictEmissionFactorCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict emission factor cycles. Empty fields match all. */
data class StrictEmissionFactorCycleQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictEmissionFactorCycle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<StrictEmissionFactorCycle>): MutableList<StrictEmissionFactorCycle> {
        val matched = filter(items) { candidate: StrictEmissionFactorCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictEmissionFactorCycles(store: StrictEmissionFactorCycleStore, query: StrictEmissionFactorCycleQuery): MutableList<StrictEmissionFactorCycle> {
    return query.apply(store.list())
}
