package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched availability segments by code. */
interface BatchedAvailabilitySegmentStore {
    fun find(code: String): BatchedAvailabilitySegment?
    fun save(item: BatchedAvailabilitySegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAvailabilitySegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAvailabilitySegmentStore(
    var items: MutableMap<String, BatchedAvailabilitySegment>,
    var order: MutableList<String>,
) : BatchedAvailabilitySegmentStore {
    override fun find(code: String): BatchedAvailabilitySegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAvailabilitySegment) {
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

    override fun list(): MutableList<BatchedAvailabilitySegment> {
        val out = mutableListOf<BatchedAvailabilitySegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAvailabilitySegmentStore(): BatchedAvailabilitySegmentStore {
    return MemoryBatchedAvailabilitySegmentStore(
        mutableMapOf<String, BatchedAvailabilitySegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAvailabilitySegmentStore(
    var inner: BatchedAvailabilitySegmentStore,
    var cache: TtlCache<BatchedAvailabilitySegment>,
    var hits: Int,
    var misses: Int,
) : BatchedAvailabilitySegmentStore {
    override fun find(code: String): BatchedAvailabilitySegment? {
        val hit = (cache.get(code) as BatchedAvailabilitySegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAvailabilitySegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAvailabilitySegment> = inner.list()
}

fun newCachedBatchedAvailabilitySegmentStore(inner: BatchedAvailabilitySegmentStore, ttl: Int, clock: Clock): BatchedAvailabilitySegmentStore {
    return CachedBatchedAvailabilitySegmentStore(
        inner = inner,
        cache = newTtlCache<BatchedAvailabilitySegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched availability segments. Empty fields match all. */
data class BatchedAvailabilitySegmentQuery(
    var channel: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAvailabilitySegment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAvailabilitySegment>): MutableList<BatchedAvailabilitySegment> {
        val matched = filter(items) { candidate: BatchedAvailabilitySegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAvailabilitySegments(store: BatchedAvailabilitySegmentStore, query: BatchedAvailabilitySegmentQuery): MutableList<BatchedAvailabilitySegment> {
    return query.apply(store.list())
}
