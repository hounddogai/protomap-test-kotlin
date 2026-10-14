package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched inspection metrics by code. */
interface BatchedInspectionMetricStore {
    fun find(code: String): BatchedInspectionMetric?
    fun save(item: BatchedInspectionMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedInspectionMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedInspectionMetricStore(
    var items: MutableMap<String, BatchedInspectionMetric>,
    var order: MutableList<String>,
) : BatchedInspectionMetricStore {
    override fun find(code: String): BatchedInspectionMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedInspectionMetric) {
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

    override fun list(): MutableList<BatchedInspectionMetric> {
        val out = mutableListOf<BatchedInspectionMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedInspectionMetricStore(): BatchedInspectionMetricStore {
    return MemoryBatchedInspectionMetricStore(
        mutableMapOf<String, BatchedInspectionMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedInspectionMetricStore(
    var inner: BatchedInspectionMetricStore,
    var cache: TtlCache<BatchedInspectionMetric>,
    var hits: Int,
    var misses: Int,
) : BatchedInspectionMetricStore {
    override fun find(code: String): BatchedInspectionMetric? {
        val hit = (cache.get(code) as BatchedInspectionMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedInspectionMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedInspectionMetric> = inner.list()
}

fun newCachedBatchedInspectionMetricStore(inner: BatchedInspectionMetricStore, ttl: Int, clock: Clock): BatchedInspectionMetricStore {
    return CachedBatchedInspectionMetricStore(
        inner = inner,
        cache = newTtlCache<BatchedInspectionMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched inspection metrics. Empty fields match all. */
data class BatchedInspectionMetricQuery(
    var lane: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedInspectionMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedInspectionMetric>): MutableList<BatchedInspectionMetric> {
        val matched = filter(items) { candidate: BatchedInspectionMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedInspectionMetrics(store: BatchedInspectionMetricStore, query: BatchedInspectionMetricQuery): MutableList<BatchedInspectionMetric> {
    return query.apply(store.list())
}
