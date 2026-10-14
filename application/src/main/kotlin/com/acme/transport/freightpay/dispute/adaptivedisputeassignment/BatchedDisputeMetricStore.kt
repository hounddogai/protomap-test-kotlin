package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched dispute metrics by code. */
interface BatchedDisputeMetricStore {
    fun find(code: String): BatchedDisputeMetric?
    fun save(item: BatchedDisputeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDisputeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDisputeMetricStore(
    var items: MutableMap<String, BatchedDisputeMetric>,
    var order: MutableList<String>,
) : BatchedDisputeMetricStore {
    override fun find(code: String): BatchedDisputeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDisputeMetric) {
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

    override fun list(): MutableList<BatchedDisputeMetric> {
        val out = mutableListOf<BatchedDisputeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDisputeMetricStore(): BatchedDisputeMetricStore {
    return MemoryBatchedDisputeMetricStore(
        mutableMapOf<String, BatchedDisputeMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDisputeMetricStore(
    var inner: BatchedDisputeMetricStore,
    var cache: TtlCache<BatchedDisputeMetric>,
    var hits: Int,
    var misses: Int,
) : BatchedDisputeMetricStore {
    override fun find(code: String): BatchedDisputeMetric? {
        val hit = (cache.get(code) as BatchedDisputeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDisputeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDisputeMetric> = inner.list()
}

fun newCachedBatchedDisputeMetricStore(inner: BatchedDisputeMetricStore, ttl: Int, clock: Clock): BatchedDisputeMetricStore {
    return CachedBatchedDisputeMetricStore(
        inner = inner,
        cache = newTtlCache<BatchedDisputeMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dispute metrics. Empty fields match all. */
data class BatchedDisputeMetricQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDisputeMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDisputeMetric>): MutableList<BatchedDisputeMetric> {
        val matched = filter(items) { candidate: BatchedDisputeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDisputeMetrics(store: BatchedDisputeMetricStore, query: BatchedDisputeMetricQuery): MutableList<BatchedDisputeMetric> {
    return query.apply(store.list())
}
