package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched parcel adjustments by code. */
interface BatchedParcelAdjustmentStore {
    fun find(code: String): BatchedParcelAdjustment?
    fun save(item: BatchedParcelAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedParcelAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedParcelAdjustmentStore(
    var items: MutableMap<String, BatchedParcelAdjustment>,
    var order: MutableList<String>,
) : BatchedParcelAdjustmentStore {
    override fun find(code: String): BatchedParcelAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedParcelAdjustment) {
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

    override fun list(): MutableList<BatchedParcelAdjustment> {
        val out = mutableListOf<BatchedParcelAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedParcelAdjustmentStore(): BatchedParcelAdjustmentStore {
    return MemoryBatchedParcelAdjustmentStore(
        mutableMapOf<String, BatchedParcelAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedParcelAdjustmentStore(
    var inner: BatchedParcelAdjustmentStore,
    var cache: TtlCache<BatchedParcelAdjustment>,
    var hits: Int,
    var misses: Int,
) : BatchedParcelAdjustmentStore {
    override fun find(code: String): BatchedParcelAdjustment? {
        val hit = (cache.get(code) as BatchedParcelAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedParcelAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedParcelAdjustment> = inner.list()
}

fun newCachedBatchedParcelAdjustmentStore(inner: BatchedParcelAdjustmentStore, ttl: Int, clock: Clock): BatchedParcelAdjustmentStore {
    return CachedBatchedParcelAdjustmentStore(
        inner = inner,
        cache = newTtlCache<BatchedParcelAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched parcel adjustments. Empty fields match all. */
data class BatchedParcelAdjustmentQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedParcelAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<BatchedParcelAdjustment>): MutableList<BatchedParcelAdjustment> {
        val matched = filter(items) { candidate: BatchedParcelAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedParcelAdjustments(store: BatchedParcelAdjustmentStore, query: BatchedParcelAdjustmentQuery): MutableList<BatchedParcelAdjustment> {
    return query.apply(store.list())
}
