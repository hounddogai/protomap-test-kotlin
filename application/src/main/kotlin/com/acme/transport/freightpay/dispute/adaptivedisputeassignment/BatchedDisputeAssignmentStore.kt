package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched dispute assignments by code. */
interface BatchedDisputeAssignmentStore {
    fun find(code: String): BatchedDisputeAssignment?
    fun save(item: BatchedDisputeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDisputeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDisputeAssignmentStore(
    var items: MutableMap<String, BatchedDisputeAssignment>,
    var order: MutableList<String>,
) : BatchedDisputeAssignmentStore {
    override fun find(code: String): BatchedDisputeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDisputeAssignment) {
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

    override fun list(): MutableList<BatchedDisputeAssignment> {
        val out = mutableListOf<BatchedDisputeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDisputeAssignmentStore(): BatchedDisputeAssignmentStore {
    return MemoryBatchedDisputeAssignmentStore(
        mutableMapOf<String, BatchedDisputeAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDisputeAssignmentStore(
    var inner: BatchedDisputeAssignmentStore,
    var cache: TtlCache<BatchedDisputeAssignment>,
    var hits: Int,
    var misses: Int,
) : BatchedDisputeAssignmentStore {
    override fun find(code: String): BatchedDisputeAssignment? {
        val hit = (cache.get(code) as BatchedDisputeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDisputeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDisputeAssignment> = inner.list()
}

fun newCachedBatchedDisputeAssignmentStore(inner: BatchedDisputeAssignmentStore, ttl: Int, clock: Clock): BatchedDisputeAssignmentStore {
    return CachedBatchedDisputeAssignmentStore(
        inner = inner,
        cache = newTtlCache<BatchedDisputeAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dispute assignments. Empty fields match all. */
data class BatchedDisputeAssignmentQuery(
    var depot: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDisputeAssignment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDisputeAssignment>): MutableList<BatchedDisputeAssignment> {
        val matched = filter(items) { candidate: BatchedDisputeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDisputeAssignments(store: BatchedDisputeAssignmentStore, query: BatchedDisputeAssignmentQuery): MutableList<BatchedDisputeAssignment> {
    return query.apply(store.list())
}
