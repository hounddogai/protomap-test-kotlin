package com.acme.transport.network.node.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched node assignments by code. */
interface BatchedNodeAssignmentStore {
    fun find(code: String): BatchedNodeAssignment?
    fun save(item: BatchedNodeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedNodeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedNodeAssignmentStore(
    var items: MutableMap<String, BatchedNodeAssignment>,
    var order: MutableList<String>,
) : BatchedNodeAssignmentStore {
    override fun find(code: String): BatchedNodeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedNodeAssignment) {
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

    override fun list(): MutableList<BatchedNodeAssignment> {
        val out = mutableListOf<BatchedNodeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedNodeAssignmentStore(): BatchedNodeAssignmentStore {
    return MemoryBatchedNodeAssignmentStore(
        mutableMapOf<String, BatchedNodeAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedNodeAssignmentStore(
    var inner: BatchedNodeAssignmentStore,
    var cache: TtlCache<BatchedNodeAssignment>,
    var hits: Int,
    var misses: Int,
) : BatchedNodeAssignmentStore {
    override fun find(code: String): BatchedNodeAssignment? {
        val hit = (cache.get(code) as BatchedNodeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedNodeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedNodeAssignment> = inner.list()
}

fun newCachedBatchedNodeAssignmentStore(inner: BatchedNodeAssignmentStore, ttl: Int, clock: Clock): BatchedNodeAssignmentStore {
    return CachedBatchedNodeAssignmentStore(
        inner = inner,
        cache = newTtlCache<BatchedNodeAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched node assignments. Empty fields match all. */
data class BatchedNodeAssignmentQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedNodeAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BatchedNodeAssignment>): MutableList<BatchedNodeAssignment> {
        val matched = filter(items) { candidate: BatchedNodeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedNodeAssignments(store: BatchedNodeAssignmentStore, query: BatchedNodeAssignmentQuery): MutableList<BatchedNodeAssignment> {
    return query.apply(store.list())
}
