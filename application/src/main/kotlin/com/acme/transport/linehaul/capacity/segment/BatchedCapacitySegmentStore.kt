package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched capacity segments by code. */
interface BatchedCapacitySegmentStore {
    fun find(code: String): BatchedCapacitySegment?
    fun save(item: BatchedCapacitySegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCapacitySegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCapacitySegmentStore(
    var items: MutableMap<String, BatchedCapacitySegment>,
    var order: MutableList<String>,
) : BatchedCapacitySegmentStore {
    override fun find(code: String): BatchedCapacitySegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCapacitySegment) {
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

    override fun list(): MutableList<BatchedCapacitySegment> {
        val out = mutableListOf<BatchedCapacitySegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCapacitySegmentStore(): BatchedCapacitySegmentStore {
    return MemoryBatchedCapacitySegmentStore(
        mutableMapOf<String, BatchedCapacitySegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCapacitySegmentStore(
    var inner: BatchedCapacitySegmentStore,
    var cache: TtlCache<BatchedCapacitySegment>,
    var hits: Int,
    var misses: Int,
) : BatchedCapacitySegmentStore {
    override fun find(code: String): BatchedCapacitySegment? {
        val hit = (cache.get(code) as BatchedCapacitySegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCapacitySegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCapacitySegment> = inner.list()
}

fun newCachedBatchedCapacitySegmentStore(inner: BatchedCapacitySegmentStore, ttl: Int, clock: Clock): BatchedCapacitySegmentStore {
    return CachedBatchedCapacitySegmentStore(
        inner = inner,
        cache = newTtlCache<BatchedCapacitySegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched capacity segments. Empty fields match all. */
data class BatchedCapacitySegmentQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCapacitySegment): Boolean {
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

    fun apply(items: MutableList<BatchedCapacitySegment>): MutableList<BatchedCapacitySegment> {
        val matched = filter(items) { candidate: BatchedCapacitySegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCapacitySegments(store: BatchedCapacitySegmentStore, query: BatchedCapacitySegmentQuery): MutableList<BatchedCapacitySegment> {
    return query.apply(store.list())
}
