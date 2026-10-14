package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched locker adjustments by code. */
interface BatchedLockerAdjustmentStore {
    fun find(code: String): BatchedLockerAdjustment?
    fun save(item: BatchedLockerAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedLockerAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedLockerAdjustmentStore(
    var items: MutableMap<String, BatchedLockerAdjustment>,
    var order: MutableList<String>,
) : BatchedLockerAdjustmentStore {
    override fun find(code: String): BatchedLockerAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedLockerAdjustment) {
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

    override fun list(): MutableList<BatchedLockerAdjustment> {
        val out = mutableListOf<BatchedLockerAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedLockerAdjustmentStore(): BatchedLockerAdjustmentStore {
    return MemoryBatchedLockerAdjustmentStore(
        mutableMapOf<String, BatchedLockerAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedLockerAdjustmentStore(
    var inner: BatchedLockerAdjustmentStore,
    var cache: TtlCache<BatchedLockerAdjustment>,
    var hits: Int,
    var misses: Int,
) : BatchedLockerAdjustmentStore {
    override fun find(code: String): BatchedLockerAdjustment? {
        val hit = (cache.get(code) as BatchedLockerAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedLockerAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedLockerAdjustment> = inner.list()
}

fun newCachedBatchedLockerAdjustmentStore(inner: BatchedLockerAdjustmentStore, ttl: Int, clock: Clock): BatchedLockerAdjustmentStore {
    return CachedBatchedLockerAdjustmentStore(
        inner = inner,
        cache = newTtlCache<BatchedLockerAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched locker adjustments. Empty fields match all. */
data class BatchedLockerAdjustmentQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedLockerAdjustment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedLockerAdjustment>): MutableList<BatchedLockerAdjustment> {
        val matched = filter(items) { candidate: BatchedLockerAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedLockerAdjustments(store: BatchedLockerAdjustmentStore, query: BatchedLockerAdjustmentQuery): MutableList<BatchedLockerAdjustment> {
    return query.apply(store.list())
}
