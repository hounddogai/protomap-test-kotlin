package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched accessorial metrics by code. */
interface BatchedAccessorialMetricStore {
    fun find(code: String): BatchedAccessorialMetric?
    fun save(item: BatchedAccessorialMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAccessorialMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAccessorialMetricStore(
    var items: MutableMap<String, BatchedAccessorialMetric>,
    var order: MutableList<String>,
) : BatchedAccessorialMetricStore {
    override fun find(code: String): BatchedAccessorialMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAccessorialMetric) {
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

    override fun list(): MutableList<BatchedAccessorialMetric> {
        val out = mutableListOf<BatchedAccessorialMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAccessorialMetricStore(): BatchedAccessorialMetricStore {
    return MemoryBatchedAccessorialMetricStore(
        mutableMapOf<String, BatchedAccessorialMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAccessorialMetricStore(
    var inner: BatchedAccessorialMetricStore,
    var cache: TtlCache<BatchedAccessorialMetric>,
    var hits: Int,
    var misses: Int,
) : BatchedAccessorialMetricStore {
    override fun find(code: String): BatchedAccessorialMetric? {
        val hit = (cache.get(code) as BatchedAccessorialMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAccessorialMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAccessorialMetric> = inner.list()
}

fun newCachedBatchedAccessorialMetricStore(inner: BatchedAccessorialMetricStore, ttl: Int, clock: Clock): BatchedAccessorialMetricStore {
    return CachedBatchedAccessorialMetricStore(
        inner = inner,
        cache = newTtlCache<BatchedAccessorialMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched accessorial metrics. Empty fields match all. */
data class BatchedAccessorialMetricQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAccessorialMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAccessorialMetric>): MutableList<BatchedAccessorialMetric> {
        val matched = filter(items) { candidate: BatchedAccessorialMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAccessorialMetrics(store: BatchedAccessorialMetricStore, query: BatchedAccessorialMetricQuery): MutableList<BatchedAccessorialMetric> {
    return query.apply(store.list())
}
