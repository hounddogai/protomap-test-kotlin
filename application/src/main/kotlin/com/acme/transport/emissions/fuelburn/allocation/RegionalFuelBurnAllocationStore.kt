package com.acme.transport.emissions.fuelburn.allocation

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel burn allocations by code. */
interface RegionalFuelBurnAllocationStore {
    fun find(code: String): RegionalFuelBurnAllocation?
    fun save(item: RegionalFuelBurnAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelBurnAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelBurnAllocationStore(
    var items: MutableMap<String, RegionalFuelBurnAllocation>,
    var order: MutableList<String>,
) : RegionalFuelBurnAllocationStore {
    override fun find(code: String): RegionalFuelBurnAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelBurnAllocation) {
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

    override fun list(): MutableList<RegionalFuelBurnAllocation> {
        val out = mutableListOf<RegionalFuelBurnAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelBurnAllocationStore(): RegionalFuelBurnAllocationStore {
    return MemoryRegionalFuelBurnAllocationStore(
        mutableMapOf<String, RegionalFuelBurnAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelBurnAllocationStore(
    var inner: RegionalFuelBurnAllocationStore,
    var cache: TtlCache<RegionalFuelBurnAllocation>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelBurnAllocationStore {
    override fun find(code: String): RegionalFuelBurnAllocation? {
        val hit = (cache.get(code) as RegionalFuelBurnAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelBurnAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelBurnAllocation> = inner.list()
}

fun newCachedRegionalFuelBurnAllocationStore(inner: RegionalFuelBurnAllocationStore, ttl: Int, clock: Clock): RegionalFuelBurnAllocationStore {
    return CachedRegionalFuelBurnAllocationStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelBurnAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel burn allocations. Empty fields match all. */
data class RegionalFuelBurnAllocationQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelBurnAllocation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<RegionalFuelBurnAllocation>): MutableList<RegionalFuelBurnAllocation> {
        val matched = filter(items) { candidate: RegionalFuelBurnAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelBurnAllocations(store: RegionalFuelBurnAllocationStore, query: RegionalFuelBurnAllocationQuery): MutableList<RegionalFuelBurnAllocation> {
    return query.apply(store.list())
}
