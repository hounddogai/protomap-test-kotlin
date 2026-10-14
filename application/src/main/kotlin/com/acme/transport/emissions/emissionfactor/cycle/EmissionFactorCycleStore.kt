package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor cycles by code. */
interface EmissionFactorCycleStore {
    fun find(code: String): EmissionFactorCycle?
    fun save(item: EmissionFactorCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorCycleStore(
    var items: MutableMap<String, EmissionFactorCycle>,
    var order: MutableList<String>,
) : EmissionFactorCycleStore {
    override fun find(code: String): EmissionFactorCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorCycle) {
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

    override fun list(): MutableList<EmissionFactorCycle> {
        val out = mutableListOf<EmissionFactorCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorCycleStore(): EmissionFactorCycleStore {
    return MemoryEmissionFactorCycleStore(mutableMapOf<String, EmissionFactorCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorCycleStore(
    var inner: EmissionFactorCycleStore,
    var cache: TtlCache<EmissionFactorCycle>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorCycleStore {
    override fun find(code: String): EmissionFactorCycle? {
        val hit = (cache.get(code) as EmissionFactorCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorCycle> = inner.list()
}

fun newCachedEmissionFactorCycleStore(inner: EmissionFactorCycleStore, ttl: Int, clock: Clock): EmissionFactorCycleStore {
    return CachedEmissionFactorCycleStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor cycles. Empty fields match all. */
data class EmissionFactorCycleQuery(
    var unit: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorCycle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorCycle>): MutableList<EmissionFactorCycle> {
        val matched = filter(items) { candidate: EmissionFactorCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorCycles(store: EmissionFactorCycleStore, query: EmissionFactorCycleQuery): MutableList<EmissionFactorCycle> {
    return query.apply(store.list())
}
