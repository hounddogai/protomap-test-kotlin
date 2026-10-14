package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched camera event allocations by code. */
interface BatchedCameraEventAllocationStore {
    fun find(code: String): BatchedCameraEventAllocation?
    fun save(item: BatchedCameraEventAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCameraEventAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCameraEventAllocationStore(
    var items: MutableMap<String, BatchedCameraEventAllocation>,
    var order: MutableList<String>,
) : BatchedCameraEventAllocationStore {
    override fun find(code: String): BatchedCameraEventAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCameraEventAllocation) {
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

    override fun list(): MutableList<BatchedCameraEventAllocation> {
        val out = mutableListOf<BatchedCameraEventAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCameraEventAllocationStore(): BatchedCameraEventAllocationStore {
    return MemoryBatchedCameraEventAllocationStore(
        mutableMapOf<String, BatchedCameraEventAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCameraEventAllocationStore(
    var inner: BatchedCameraEventAllocationStore,
    var cache: TtlCache<BatchedCameraEventAllocation>,
    var hits: Int,
    var misses: Int,
) : BatchedCameraEventAllocationStore {
    override fun find(code: String): BatchedCameraEventAllocation? {
        val hit = (cache.get(code) as BatchedCameraEventAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCameraEventAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCameraEventAllocation> = inner.list()
}

fun newCachedBatchedCameraEventAllocationStore(inner: BatchedCameraEventAllocationStore, ttl: Int, clock: Clock): BatchedCameraEventAllocationStore {
    return CachedBatchedCameraEventAllocationStore(
        inner = inner,
        cache = newTtlCache<BatchedCameraEventAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched camera event allocations. Empty fields match all. */
data class BatchedCameraEventAllocationQuery(
    var lane: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCameraEventAllocation): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCameraEventAllocation>): MutableList<BatchedCameraEventAllocation> {
        val matched = filter(items) { candidate: BatchedCameraEventAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCameraEventAllocations(store: BatchedCameraEventAllocationStore, query: BatchedCameraEventAllocationQuery): MutableList<BatchedCameraEventAllocation> {
    return query.apply(store.list())
}
