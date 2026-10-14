package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched utilization metrics by code. */
interface BatchedUtilizationMetricStore {
    fun find(code: String): BatchedUtilizationMetric?
    fun save(item: BatchedUtilizationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedUtilizationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedUtilizationMetricStore(
    var items: MutableMap<String, BatchedUtilizationMetric>,
    var order: MutableList<String>,
) : BatchedUtilizationMetricStore {
    override fun find(code: String): BatchedUtilizationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedUtilizationMetric) {
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

    override fun list(): MutableList<BatchedUtilizationMetric> {
        val out = mutableListOf<BatchedUtilizationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedUtilizationMetricStore(): BatchedUtilizationMetricStore {
    return MemoryBatchedUtilizationMetricStore(
        mutableMapOf<String, BatchedUtilizationMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedUtilizationMetricStore(
    var inner: BatchedUtilizationMetricStore,
    var cache: TtlCache<BatchedUtilizationMetric>,
    var hits: Int,
    var misses: Int,
) : BatchedUtilizationMetricStore {
    override fun find(code: String): BatchedUtilizationMetric? {
        val hit = (cache.get(code) as BatchedUtilizationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedUtilizationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedUtilizationMetric> = inner.list()
}

fun newCachedBatchedUtilizationMetricStore(inner: BatchedUtilizationMetricStore, ttl: Int, clock: Clock): BatchedUtilizationMetricStore {
    return CachedBatchedUtilizationMetricStore(
        inner = inner,
        cache = newTtlCache<BatchedUtilizationMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched utilization metrics. Empty fields match all. */
data class BatchedUtilizationMetricQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedUtilizationMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedUtilizationMetric>): MutableList<BatchedUtilizationMetric> {
        val matched = filter(items) { candidate: BatchedUtilizationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedUtilizationMetrics(store: BatchedUtilizationMetricStore, query: BatchedUtilizationMetricQuery): MutableList<BatchedUtilizationMetric> {
    return query.apply(store.list())
}
