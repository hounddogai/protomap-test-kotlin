package com.acme.transport.customs.clearance.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched clearance allocations by code. */
interface BatchedClearanceAllocationStore {
    fun find(code: String): BatchedClearanceAllocation?
    fun save(item: BatchedClearanceAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedClearanceAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedClearanceAllocationStore(
    var items: MutableMap<String, BatchedClearanceAllocation>,
    var order: MutableList<String>,
) : BatchedClearanceAllocationStore {
    override fun find(code: String): BatchedClearanceAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedClearanceAllocation) {
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

    override fun list(): MutableList<BatchedClearanceAllocation> {
        val out = mutableListOf<BatchedClearanceAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedClearanceAllocationStore(): BatchedClearanceAllocationStore {
    return MemoryBatchedClearanceAllocationStore(
        mutableMapOf<String, BatchedClearanceAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedClearanceAllocationStore(
    var inner: BatchedClearanceAllocationStore,
    var cache: TtlCache<BatchedClearanceAllocation>,
    var hits: Int,
    var misses: Int,
) : BatchedClearanceAllocationStore {
    override fun find(code: String): BatchedClearanceAllocation? {
        val hit = (cache.get(code) as BatchedClearanceAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedClearanceAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedClearanceAllocation> = inner.list()
}

fun newCachedBatchedClearanceAllocationStore(inner: BatchedClearanceAllocationStore, ttl: Int, clock: Clock): BatchedClearanceAllocationStore {
    return CachedBatchedClearanceAllocationStore(
        inner = inner,
        cache = newTtlCache<BatchedClearanceAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched clearance allocations. Empty fields match all. */
data class BatchedClearanceAllocationQuery(
    var locale: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedClearanceAllocation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<BatchedClearanceAllocation>): MutableList<BatchedClearanceAllocation> {
        val matched = filter(items) { candidate: BatchedClearanceAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedClearanceAllocations(store: BatchedClearanceAllocationStore, query: BatchedClearanceAllocationQuery): MutableList<BatchedClearanceAllocation> {
    return query.apply(store.list())
}
