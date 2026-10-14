package com.acme.transport.crewing.roster.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched roster adjustments by code. */
interface BatchedRosterAdjustmentStore {
    fun find(code: String): BatchedRosterAdjustment?
    fun save(item: BatchedRosterAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRosterAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRosterAdjustmentStore(
    var items: MutableMap<String, BatchedRosterAdjustment>,
    var order: MutableList<String>,
) : BatchedRosterAdjustmentStore {
    override fun find(code: String): BatchedRosterAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRosterAdjustment) {
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

    override fun list(): MutableList<BatchedRosterAdjustment> {
        val out = mutableListOf<BatchedRosterAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRosterAdjustmentStore(): BatchedRosterAdjustmentStore {
    return MemoryBatchedRosterAdjustmentStore(
        mutableMapOf<String, BatchedRosterAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRosterAdjustmentStore(
    var inner: BatchedRosterAdjustmentStore,
    var cache: TtlCache<BatchedRosterAdjustment>,
    var hits: Int,
    var misses: Int,
) : BatchedRosterAdjustmentStore {
    override fun find(code: String): BatchedRosterAdjustment? {
        val hit = (cache.get(code) as BatchedRosterAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRosterAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRosterAdjustment> = inner.list()
}

fun newCachedBatchedRosterAdjustmentStore(inner: BatchedRosterAdjustmentStore, ttl: Int, clock: Clock): BatchedRosterAdjustmentStore {
    return CachedBatchedRosterAdjustmentStore(
        inner = inner,
        cache = newTtlCache<BatchedRosterAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched roster adjustments. Empty fields match all. */
data class BatchedRosterAdjustmentQuery(
    var country: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRosterAdjustment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRosterAdjustment>): MutableList<BatchedRosterAdjustment> {
        val matched = filter(items) { candidate: BatchedRosterAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRosterAdjustments(store: BatchedRosterAdjustmentStore, query: BatchedRosterAdjustmentQuery): MutableList<BatchedRosterAdjustment> {
    return query.apply(store.list())
}
