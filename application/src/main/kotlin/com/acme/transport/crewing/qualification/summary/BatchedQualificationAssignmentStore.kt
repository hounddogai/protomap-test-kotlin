package com.acme.transport.crewing.qualification.summary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched qualification assignments by code. */
interface BatchedQualificationAssignmentStore {
    fun find(code: String): BatchedQualificationAssignment?
    fun save(item: BatchedQualificationAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedQualificationAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedQualificationAssignmentStore(
    var items: MutableMap<String, BatchedQualificationAssignment>,
    var order: MutableList<String>,
) : BatchedQualificationAssignmentStore {
    override fun find(code: String): BatchedQualificationAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedQualificationAssignment) {
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

    override fun list(): MutableList<BatchedQualificationAssignment> {
        val out = mutableListOf<BatchedQualificationAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedQualificationAssignmentStore(): BatchedQualificationAssignmentStore {
    return MemoryBatchedQualificationAssignmentStore(
        mutableMapOf<String, BatchedQualificationAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedQualificationAssignmentStore(
    var inner: BatchedQualificationAssignmentStore,
    var cache: TtlCache<BatchedQualificationAssignment>,
    var hits: Int,
    var misses: Int,
) : BatchedQualificationAssignmentStore {
    override fun find(code: String): BatchedQualificationAssignment? {
        val hit = (cache.get(code) as BatchedQualificationAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedQualificationAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedQualificationAssignment> = inner.list()
}

fun newCachedBatchedQualificationAssignmentStore(inner: BatchedQualificationAssignmentStore, ttl: Int, clock: Clock): BatchedQualificationAssignmentStore {
    return CachedBatchedQualificationAssignmentStore(
        inner = inner,
        cache = newTtlCache<BatchedQualificationAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched qualification assignments. Empty fields match all. */
data class BatchedQualificationAssignmentQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedQualificationAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedQualificationAssignment>): MutableList<BatchedQualificationAssignment> {
        val matched = filter(items) { candidate: BatchedQualificationAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedQualificationAssignments(store: BatchedQualificationAssignmentStore, query: BatchedQualificationAssignmentQuery): MutableList<BatchedQualificationAssignment> {
    return query.apply(store.list())
}
