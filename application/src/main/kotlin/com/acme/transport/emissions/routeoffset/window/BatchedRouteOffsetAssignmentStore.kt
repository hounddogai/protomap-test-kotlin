package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched route offset assignments by code. */
interface BatchedRouteOffsetAssignmentStore {
    fun find(code: String): BatchedRouteOffsetAssignment?
    fun save(item: BatchedRouteOffsetAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRouteOffsetAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRouteOffsetAssignmentStore(
    var items: MutableMap<String, BatchedRouteOffsetAssignment>,
    var order: MutableList<String>,
) : BatchedRouteOffsetAssignmentStore {
    override fun find(code: String): BatchedRouteOffsetAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRouteOffsetAssignment) {
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

    override fun list(): MutableList<BatchedRouteOffsetAssignment> {
        val out = mutableListOf<BatchedRouteOffsetAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRouteOffsetAssignmentStore(): BatchedRouteOffsetAssignmentStore {
    return MemoryBatchedRouteOffsetAssignmentStore(
        mutableMapOf<String, BatchedRouteOffsetAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRouteOffsetAssignmentStore(
    var inner: BatchedRouteOffsetAssignmentStore,
    var cache: TtlCache<BatchedRouteOffsetAssignment>,
    var hits: Int,
    var misses: Int,
) : BatchedRouteOffsetAssignmentStore {
    override fun find(code: String): BatchedRouteOffsetAssignment? {
        val hit = (cache.get(code) as BatchedRouteOffsetAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRouteOffsetAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRouteOffsetAssignment> = inner.list()
}

fun newCachedBatchedRouteOffsetAssignmentStore(inner: BatchedRouteOffsetAssignmentStore, ttl: Int, clock: Clock): BatchedRouteOffsetAssignmentStore {
    return CachedBatchedRouteOffsetAssignmentStore(
        inner = inner,
        cache = newTtlCache<BatchedRouteOffsetAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched route offset assignments. Empty fields match all. */
data class BatchedRouteOffsetAssignmentQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRouteOffsetAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BatchedRouteOffsetAssignment>): MutableList<BatchedRouteOffsetAssignment> {
        val matched = filter(items) { candidate: BatchedRouteOffsetAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRouteOffsetAssignments(store: BatchedRouteOffsetAssignmentStore, query: BatchedRouteOffsetAssignmentQuery): MutableList<BatchedRouteOffsetAssignment> {
    return query.apply(store.list())
}
