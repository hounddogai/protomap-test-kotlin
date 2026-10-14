package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched route offset allocations by code. */
interface BatchedRouteOffsetAllocationStore {
    fun find(code: String): BatchedRouteOffsetAllocation?
    fun save(item: BatchedRouteOffsetAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRouteOffsetAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRouteOffsetAllocationStore(
    var items: MutableMap<String, BatchedRouteOffsetAllocation>,
    var order: MutableList<String>,
) : BatchedRouteOffsetAllocationStore {
    override fun find(code: String): BatchedRouteOffsetAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRouteOffsetAllocation) {
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

    override fun list(): MutableList<BatchedRouteOffsetAllocation> {
        val out = mutableListOf<BatchedRouteOffsetAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRouteOffsetAllocationStore(): BatchedRouteOffsetAllocationStore {
    return MemoryBatchedRouteOffsetAllocationStore(
        mutableMapOf<String, BatchedRouteOffsetAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRouteOffsetAllocationStore(
    var inner: BatchedRouteOffsetAllocationStore,
    var cache: TtlCache<BatchedRouteOffsetAllocation>,
    var hits: Int,
    var misses: Int,
) : BatchedRouteOffsetAllocationStore {
    override fun find(code: String): BatchedRouteOffsetAllocation? {
        val hit = (cache.get(code) as BatchedRouteOffsetAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRouteOffsetAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRouteOffsetAllocation> = inner.list()
}

fun newCachedBatchedRouteOffsetAllocationStore(inner: BatchedRouteOffsetAllocationStore, ttl: Int, clock: Clock): BatchedRouteOffsetAllocationStore {
    return CachedBatchedRouteOffsetAllocationStore(
        inner = inner,
        cache = newTtlCache<BatchedRouteOffsetAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched route offset allocations. Empty fields match all. */
data class BatchedRouteOffsetAllocationQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRouteOffsetAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRouteOffsetAllocation>): MutableList<BatchedRouteOffsetAllocation> {
        val matched = filter(items) { candidate: BatchedRouteOffsetAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRouteOffsetAllocations(store: BatchedRouteOffsetAllocationStore, query: BatchedRouteOffsetAllocationQuery): MutableList<BatchedRouteOffsetAllocation> {
    return query.apply(store.list())
}
