package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted rest break allocations by code. */
interface WeightedRestBreakAllocationStore {
    fun find(code: String): WeightedRestBreakAllocation?
    fun save(item: WeightedRestBreakAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRestBreakAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRestBreakAllocationStore(
    var items: MutableMap<String, WeightedRestBreakAllocation>,
    var order: MutableList<String>,
) : WeightedRestBreakAllocationStore {
    override fun find(code: String): WeightedRestBreakAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRestBreakAllocation) {
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

    override fun list(): MutableList<WeightedRestBreakAllocation> {
        val out = mutableListOf<WeightedRestBreakAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRestBreakAllocationStore(): WeightedRestBreakAllocationStore {
    return MemoryWeightedRestBreakAllocationStore(
        mutableMapOf<String, WeightedRestBreakAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRestBreakAllocationStore(
    var inner: WeightedRestBreakAllocationStore,
    var cache: TtlCache<WeightedRestBreakAllocation>,
    var hits: Int,
    var misses: Int,
) : WeightedRestBreakAllocationStore {
    override fun find(code: String): WeightedRestBreakAllocation? {
        val hit = (cache.get(code) as WeightedRestBreakAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRestBreakAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRestBreakAllocation> = inner.list()
}

fun newCachedWeightedRestBreakAllocationStore(inner: WeightedRestBreakAllocationStore, ttl: Int, clock: Clock): WeightedRestBreakAllocationStore {
    return CachedWeightedRestBreakAllocationStore(
        inner = inner,
        cache = newTtlCache<WeightedRestBreakAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted rest break allocations. Empty fields match all. */
data class WeightedRestBreakAllocationQuery(
    var region: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRestBreakAllocation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<WeightedRestBreakAllocation>): MutableList<WeightedRestBreakAllocation> {
        val matched = filter(items) { candidate: WeightedRestBreakAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRestBreakAllocations(store: WeightedRestBreakAllocationStore, query: WeightedRestBreakAllocationQuery): MutableList<WeightedRestBreakAllocation> {
    return query.apply(store.list())
}
