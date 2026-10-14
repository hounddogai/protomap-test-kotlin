package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched backhaul assignments by code. */
interface BatchedBackhaulAssignmentStore {
    fun find(code: String): BatchedBackhaulAssignment?
    fun save(item: BatchedBackhaulAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedBackhaulAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedBackhaulAssignmentStore(
    var items: MutableMap<String, BatchedBackhaulAssignment>,
    var order: MutableList<String>,
) : BatchedBackhaulAssignmentStore {
    override fun find(code: String): BatchedBackhaulAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedBackhaulAssignment) {
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

    override fun list(): MutableList<BatchedBackhaulAssignment> {
        val out = mutableListOf<BatchedBackhaulAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedBackhaulAssignmentStore(): BatchedBackhaulAssignmentStore {
    return MemoryBatchedBackhaulAssignmentStore(
        mutableMapOf<String, BatchedBackhaulAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedBackhaulAssignmentStore(
    var inner: BatchedBackhaulAssignmentStore,
    var cache: TtlCache<BatchedBackhaulAssignment>,
    var hits: Int,
    var misses: Int,
) : BatchedBackhaulAssignmentStore {
    override fun find(code: String): BatchedBackhaulAssignment? {
        val hit = (cache.get(code) as BatchedBackhaulAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedBackhaulAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedBackhaulAssignment> = inner.list()
}

fun newCachedBatchedBackhaulAssignmentStore(inner: BatchedBackhaulAssignmentStore, ttl: Int, clock: Clock): BatchedBackhaulAssignmentStore {
    return CachedBatchedBackhaulAssignmentStore(
        inner = inner,
        cache = newTtlCache<BatchedBackhaulAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched backhaul assignments. Empty fields match all. */
data class BatchedBackhaulAssignmentQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedBackhaulAssignment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedBackhaulAssignment>): MutableList<BatchedBackhaulAssignment> {
        val matched = filter(items) { candidate: BatchedBackhaulAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedBackhaulAssignments(store: BatchedBackhaulAssignmentStore, query: BatchedBackhaulAssignmentQuery): MutableList<BatchedBackhaulAssignment> {
    return query.apply(store.list())
}
