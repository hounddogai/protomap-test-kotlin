package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled fuel burn allocations by code. */
interface ScheduledFuelBurnAllocationStore {
    fun find(code: String): ScheduledFuelBurnAllocation?
    fun save(item: ScheduledFuelBurnAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledFuelBurnAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledFuelBurnAllocationStore(
    var items: MutableMap<String, ScheduledFuelBurnAllocation>,
    var order: MutableList<String>,
) : ScheduledFuelBurnAllocationStore {
    override fun find(code: String): ScheduledFuelBurnAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledFuelBurnAllocation) {
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

    override fun list(): MutableList<ScheduledFuelBurnAllocation> {
        val out = mutableListOf<ScheduledFuelBurnAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledFuelBurnAllocationStore(): ScheduledFuelBurnAllocationStore {
    return MemoryScheduledFuelBurnAllocationStore(
        mutableMapOf<String, ScheduledFuelBurnAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledFuelBurnAllocationStore(
    var inner: ScheduledFuelBurnAllocationStore,
    var cache: TtlCache<ScheduledFuelBurnAllocation>,
    var hits: Int,
    var misses: Int,
) : ScheduledFuelBurnAllocationStore {
    override fun find(code: String): ScheduledFuelBurnAllocation? {
        val hit = (cache.get(code) as ScheduledFuelBurnAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledFuelBurnAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledFuelBurnAllocation> = inner.list()
}

fun newCachedScheduledFuelBurnAllocationStore(inner: ScheduledFuelBurnAllocationStore, ttl: Int, clock: Clock): ScheduledFuelBurnAllocationStore {
    return CachedScheduledFuelBurnAllocationStore(
        inner = inner,
        cache = newTtlCache<ScheduledFuelBurnAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled fuel burn allocations. Empty fields match all. */
data class ScheduledFuelBurnAllocationQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledFuelBurnAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledFuelBurnAllocation>): MutableList<ScheduledFuelBurnAllocation> {
        val matched = filter(items) { candidate: ScheduledFuelBurnAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledFuelBurnAllocations(store: ScheduledFuelBurnAllocationStore, query: ScheduledFuelBurnAllocationQuery): MutableList<ScheduledFuelBurnAllocation> {
    return query.apply(store.list())
}
