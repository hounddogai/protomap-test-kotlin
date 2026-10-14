package com.acme.transport.customs.hold.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched hold allocations by code. */
interface BatchedHoldAllocationStore {
    fun find(code: String): BatchedHoldAllocation?
    fun save(item: BatchedHoldAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedHoldAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedHoldAllocationStore(
    var items: MutableMap<String, BatchedHoldAllocation>,
    var order: MutableList<String>,
) : BatchedHoldAllocationStore {
    override fun find(code: String): BatchedHoldAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedHoldAllocation) {
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

    override fun list(): MutableList<BatchedHoldAllocation> {
        val out = mutableListOf<BatchedHoldAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedHoldAllocationStore(): BatchedHoldAllocationStore {
    return MemoryBatchedHoldAllocationStore(
        mutableMapOf<String, BatchedHoldAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedHoldAllocationStore(
    var inner: BatchedHoldAllocationStore,
    var cache: TtlCache<BatchedHoldAllocation>,
    var hits: Int,
    var misses: Int,
) : BatchedHoldAllocationStore {
    override fun find(code: String): BatchedHoldAllocation? {
        val hit = (cache.get(code) as BatchedHoldAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedHoldAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedHoldAllocation> = inner.list()
}

fun newCachedBatchedHoldAllocationStore(inner: BatchedHoldAllocationStore, ttl: Int, clock: Clock): BatchedHoldAllocationStore {
    return CachedBatchedHoldAllocationStore(
        inner = inner,
        cache = newTtlCache<BatchedHoldAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched hold allocations. Empty fields match all. */
data class BatchedHoldAllocationQuery(
    var source: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedHoldAllocation): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<BatchedHoldAllocation>): MutableList<BatchedHoldAllocation> {
        val matched = filter(items) { candidate: BatchedHoldAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedHoldAllocations(store: BatchedHoldAllocationStore, query: BatchedHoldAllocationQuery): MutableList<BatchedHoldAllocation> {
    return query.apply(store.list())
}
