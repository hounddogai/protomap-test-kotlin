package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered service plan allocations by code. */
interface TieredServicePlanAllocationStore {
    fun find(code: String): TieredServicePlanAllocation?
    fun save(item: TieredServicePlanAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredServicePlanAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredServicePlanAllocationStore(
    var items: MutableMap<String, TieredServicePlanAllocation>,
    var order: MutableList<String>,
) : TieredServicePlanAllocationStore {
    override fun find(code: String): TieredServicePlanAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredServicePlanAllocation) {
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

    override fun list(): MutableList<TieredServicePlanAllocation> {
        val out = mutableListOf<TieredServicePlanAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredServicePlanAllocationStore(): TieredServicePlanAllocationStore {
    return MemoryTieredServicePlanAllocationStore(
        mutableMapOf<String, TieredServicePlanAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredServicePlanAllocationStore(
    var inner: TieredServicePlanAllocationStore,
    var cache: TtlCache<TieredServicePlanAllocation>,
    var hits: Int,
    var misses: Int,
) : TieredServicePlanAllocationStore {
    override fun find(code: String): TieredServicePlanAllocation? {
        val hit = (cache.get(code) as TieredServicePlanAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredServicePlanAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredServicePlanAllocation> = inner.list()
}

fun newCachedTieredServicePlanAllocationStore(inner: TieredServicePlanAllocationStore, ttl: Int, clock: Clock): TieredServicePlanAllocationStore {
    return CachedTieredServicePlanAllocationStore(
        inner = inner,
        cache = newTtlCache<TieredServicePlanAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered service plan allocations. Empty fields match all. */
data class TieredServicePlanAllocationQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredServicePlanAllocation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredServicePlanAllocation>): MutableList<TieredServicePlanAllocation> {
        val matched = filter(items) { candidate: TieredServicePlanAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredServicePlanAllocations(store: TieredServicePlanAllocationStore, query: TieredServicePlanAllocationQuery): MutableList<TieredServicePlanAllocation> {
    return query.apply(store.list())
}
