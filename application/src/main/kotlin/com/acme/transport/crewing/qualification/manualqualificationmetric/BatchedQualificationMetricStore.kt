package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched qualification metrics by code. */
interface BatchedQualificationMetricStore {
    fun find(code: String): BatchedQualificationMetric?
    fun save(item: BatchedQualificationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedQualificationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedQualificationMetricStore(
    var items: MutableMap<String, BatchedQualificationMetric>,
    var order: MutableList<String>,
) : BatchedQualificationMetricStore {
    override fun find(code: String): BatchedQualificationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedQualificationMetric) {
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

    override fun list(): MutableList<BatchedQualificationMetric> {
        val out = mutableListOf<BatchedQualificationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedQualificationMetricStore(): BatchedQualificationMetricStore {
    return MemoryBatchedQualificationMetricStore(
        mutableMapOf<String, BatchedQualificationMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedQualificationMetricStore(
    var inner: BatchedQualificationMetricStore,
    var cache: TtlCache<BatchedQualificationMetric>,
    var hits: Int,
    var misses: Int,
) : BatchedQualificationMetricStore {
    override fun find(code: String): BatchedQualificationMetric? {
        val hit = (cache.get(code) as BatchedQualificationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedQualificationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedQualificationMetric> = inner.list()
}

fun newCachedBatchedQualificationMetricStore(inner: BatchedQualificationMetricStore, ttl: Int, clock: Clock): BatchedQualificationMetricStore {
    return CachedBatchedQualificationMetricStore(
        inner = inner,
        cache = newTtlCache<BatchedQualificationMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched qualification metrics. Empty fields match all. */
data class BatchedQualificationMetricQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedQualificationMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedQualificationMetric>): MutableList<BatchedQualificationMetric> {
        val matched = filter(items) { candidate: BatchedQualificationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedQualificationMetrics(store: BatchedQualificationMetricStore, query: BatchedQualificationMetricQuery): MutableList<BatchedQualificationMetric> {
    return query.apply(store.list())
}
