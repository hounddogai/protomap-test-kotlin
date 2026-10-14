package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn cycles by code. */
interface FuelBurnCycleStore {
    fun find(code: String): FuelBurnCycle?
    fun save(item: FuelBurnCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnCycleStore(
    var items: MutableMap<String, FuelBurnCycle>,
    var order: MutableList<String>,
) : FuelBurnCycleStore {
    override fun find(code: String): FuelBurnCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnCycle) {
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

    override fun list(): MutableList<FuelBurnCycle> {
        val out = mutableListOf<FuelBurnCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnCycleStore(): FuelBurnCycleStore {
    return MemoryFuelBurnCycleStore(mutableMapOf<String, FuelBurnCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnCycleStore(
    var inner: FuelBurnCycleStore,
    var cache: TtlCache<FuelBurnCycle>,
    var hits: Int,
    var misses: Int,
) : FuelBurnCycleStore {
    override fun find(code: String): FuelBurnCycle? {
        val hit = (cache.get(code) as FuelBurnCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnCycle> = inner.list()
}

fun newCachedFuelBurnCycleStore(inner: FuelBurnCycleStore, ttl: Int, clock: Clock): FuelBurnCycleStore {
    return CachedFuelBurnCycleStore(
        inner = inner,
        cache = newTtlCache<FuelBurnCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn cycles. Empty fields match all. */
data class FuelBurnCycleQuery(
    var unit: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnCycle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnCycle>): MutableList<FuelBurnCycle> {
        val matched = filter(items) { candidate: FuelBurnCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnCycles(store: FuelBurnCycleStore, query: FuelBurnCycleQuery): MutableList<FuelBurnCycle> {
    return query.apply(store.list())
}
