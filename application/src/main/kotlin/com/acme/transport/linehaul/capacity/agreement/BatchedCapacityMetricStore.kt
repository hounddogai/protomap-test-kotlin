package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched capacity metrics by code. */
interface BatchedCapacityMetricStore {
    fun find(code: String): BatchedCapacityMetric?
    fun save(item: BatchedCapacityMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCapacityMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCapacityMetricStore(
    var items: MutableMap<String, BatchedCapacityMetric>,
    var order: MutableList<String>,
) : BatchedCapacityMetricStore {
    override fun find(code: String): BatchedCapacityMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCapacityMetric) {
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

    override fun list(): MutableList<BatchedCapacityMetric> {
        val out = mutableListOf<BatchedCapacityMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCapacityMetricStore(): BatchedCapacityMetricStore {
    return MemoryBatchedCapacityMetricStore(
        mutableMapOf<String, BatchedCapacityMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCapacityMetricStore(
    var inner: BatchedCapacityMetricStore,
    var cache: TtlCache<BatchedCapacityMetric>,
    var hits: Int,
    var misses: Int,
) : BatchedCapacityMetricStore {
    override fun find(code: String): BatchedCapacityMetric? {
        val hit = (cache.get(code) as BatchedCapacityMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCapacityMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCapacityMetric> = inner.list()
}

fun newCachedBatchedCapacityMetricStore(inner: BatchedCapacityMetricStore, ttl: Int, clock: Clock): BatchedCapacityMetricStore {
    return CachedBatchedCapacityMetricStore(
        inner = inner,
        cache = newTtlCache<BatchedCapacityMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched capacity metrics. Empty fields match all. */
data class BatchedCapacityMetricQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCapacityMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCapacityMetric>): MutableList<BatchedCapacityMetric> {
        val matched = filter(items) { candidate: BatchedCapacityMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCapacityMetrics(store: BatchedCapacityMetricStore, query: BatchedCapacityMetricQuery): MutableList<BatchedCapacityMetric> {
    return query.apply(store.list())
}
