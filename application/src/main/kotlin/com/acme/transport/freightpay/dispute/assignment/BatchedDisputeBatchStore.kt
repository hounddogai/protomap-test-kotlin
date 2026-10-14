package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched dispute batches by code. */
interface BatchedDisputeBatchStore {
    fun find(code: String): BatchedDisputeBatch?
    fun save(item: BatchedDisputeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDisputeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDisputeBatchStore(
    var items: MutableMap<String, BatchedDisputeBatch>,
    var order: MutableList<String>,
) : BatchedDisputeBatchStore {
    override fun find(code: String): BatchedDisputeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDisputeBatch) {
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

    override fun list(): MutableList<BatchedDisputeBatch> {
        val out = mutableListOf<BatchedDisputeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDisputeBatchStore(): BatchedDisputeBatchStore {
    return MemoryBatchedDisputeBatchStore(mutableMapOf<String, BatchedDisputeBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDisputeBatchStore(
    var inner: BatchedDisputeBatchStore,
    var cache: TtlCache<BatchedDisputeBatch>,
    var hits: Int,
    var misses: Int,
) : BatchedDisputeBatchStore {
    override fun find(code: String): BatchedDisputeBatch? {
        val hit = (cache.get(code) as BatchedDisputeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDisputeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDisputeBatch> = inner.list()
}

fun newCachedBatchedDisputeBatchStore(inner: BatchedDisputeBatchStore, ttl: Int, clock: Clock): BatchedDisputeBatchStore {
    return CachedBatchedDisputeBatchStore(
        inner = inner,
        cache = newTtlCache<BatchedDisputeBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dispute batches. Empty fields match all. */
data class BatchedDisputeBatchQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDisputeBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDisputeBatch>): MutableList<BatchedDisputeBatch> {
        val matched = filter(items) { candidate: BatchedDisputeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDisputeBatches(store: BatchedDisputeBatchStore, query: BatchedDisputeBatchQuery): MutableList<BatchedDisputeBatch> {
    return query.apply(store.list())
}
