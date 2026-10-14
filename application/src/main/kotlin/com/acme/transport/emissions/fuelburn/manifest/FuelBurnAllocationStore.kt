package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn allocations by code. */
interface FuelBurnAllocationStore {
    fun find(code: String): FuelBurnAllocation?
    fun save(item: FuelBurnAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnAllocationStore(
    var items: MutableMap<String, FuelBurnAllocation>,
    var order: MutableList<String>,
) : FuelBurnAllocationStore {
    override fun find(code: String): FuelBurnAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnAllocation) {
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

    override fun list(): MutableList<FuelBurnAllocation> {
        val out = mutableListOf<FuelBurnAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnAllocationStore(): FuelBurnAllocationStore {
    return MemoryFuelBurnAllocationStore(mutableMapOf<String, FuelBurnAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnAllocationStore(
    var inner: FuelBurnAllocationStore,
    var cache: TtlCache<FuelBurnAllocation>,
    var hits: Int,
    var misses: Int,
) : FuelBurnAllocationStore {
    override fun find(code: String): FuelBurnAllocation? {
        val hit = (cache.get(code) as FuelBurnAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnAllocation> = inner.list()
}

fun newCachedFuelBurnAllocationStore(inner: FuelBurnAllocationStore, ttl: Int, clock: Clock): FuelBurnAllocationStore {
    return CachedFuelBurnAllocationStore(
        inner = inner,
        cache = newTtlCache<FuelBurnAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn allocations. Empty fields match all. */
data class FuelBurnAllocationQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnAllocation>): MutableList<FuelBurnAllocation> {
        val matched = filter(items) { candidate: FuelBurnAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnAllocations(store: FuelBurnAllocationStore, query: FuelBurnAllocationQuery): MutableList<FuelBurnAllocation> {
    return query.apply(store.list())
}
