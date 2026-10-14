package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched dispute estimates by code. */
interface BatchedDisputeEstimateStore {
    fun find(code: String): BatchedDisputeEstimate?
    fun save(item: BatchedDisputeEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDisputeEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDisputeEstimateStore(
    var items: MutableMap<String, BatchedDisputeEstimate>,
    var order: MutableList<String>,
) : BatchedDisputeEstimateStore {
    override fun find(code: String): BatchedDisputeEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDisputeEstimate) {
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

    override fun list(): MutableList<BatchedDisputeEstimate> {
        val out = mutableListOf<BatchedDisputeEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDisputeEstimateStore(): BatchedDisputeEstimateStore {
    return MemoryBatchedDisputeEstimateStore(
        mutableMapOf<String, BatchedDisputeEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDisputeEstimateStore(
    var inner: BatchedDisputeEstimateStore,
    var cache: TtlCache<BatchedDisputeEstimate>,
    var hits: Int,
    var misses: Int,
) : BatchedDisputeEstimateStore {
    override fun find(code: String): BatchedDisputeEstimate? {
        val hit = (cache.get(code) as BatchedDisputeEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDisputeEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDisputeEstimate> = inner.list()
}

fun newCachedBatchedDisputeEstimateStore(inner: BatchedDisputeEstimateStore, ttl: Int, clock: Clock): BatchedDisputeEstimateStore {
    return CachedBatchedDisputeEstimateStore(
        inner = inner,
        cache = newTtlCache<BatchedDisputeEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dispute estimates. Empty fields match all. */
data class BatchedDisputeEstimateQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDisputeEstimate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDisputeEstimate>): MutableList<BatchedDisputeEstimate> {
        val matched = filter(items) { candidate: BatchedDisputeEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDisputeEstimates(store: BatchedDisputeEstimateStore, query: BatchedDisputeEstimateQuery): MutableList<BatchedDisputeEstimate> {
    return query.apply(store.list())
}
