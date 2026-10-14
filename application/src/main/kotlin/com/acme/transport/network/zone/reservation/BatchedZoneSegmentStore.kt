package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched zone segments by code. */
interface BatchedZoneSegmentStore {
    fun find(code: String): BatchedZoneSegment?
    fun save(item: BatchedZoneSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedZoneSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedZoneSegmentStore(
    var items: MutableMap<String, BatchedZoneSegment>,
    var order: MutableList<String>,
) : BatchedZoneSegmentStore {
    override fun find(code: String): BatchedZoneSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedZoneSegment) {
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

    override fun list(): MutableList<BatchedZoneSegment> {
        val out = mutableListOf<BatchedZoneSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedZoneSegmentStore(): BatchedZoneSegmentStore {
    return MemoryBatchedZoneSegmentStore(mutableMapOf<String, BatchedZoneSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedZoneSegmentStore(
    var inner: BatchedZoneSegmentStore,
    var cache: TtlCache<BatchedZoneSegment>,
    var hits: Int,
    var misses: Int,
) : BatchedZoneSegmentStore {
    override fun find(code: String): BatchedZoneSegment? {
        val hit = (cache.get(code) as BatchedZoneSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedZoneSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedZoneSegment> = inner.list()
}

fun newCachedBatchedZoneSegmentStore(inner: BatchedZoneSegmentStore, ttl: Int, clock: Clock): BatchedZoneSegmentStore {
    return CachedBatchedZoneSegmentStore(
        inner = inner,
        cache = newTtlCache<BatchedZoneSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched zone segments. Empty fields match all. */
data class BatchedZoneSegmentQuery(
    var depot: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedZoneSegment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedZoneSegment>): MutableList<BatchedZoneSegment> {
        val matched = filter(items) { candidate: BatchedZoneSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedZoneSegments(store: BatchedZoneSegmentStore, query: BatchedZoneSegmentQuery): MutableList<BatchedZoneSegment> {
    return query.apply(store.list())
}
