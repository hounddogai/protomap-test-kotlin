package com.acme.transport.fleetops.defect.metric

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending defect metrics by code. */
interface PendingDefectMetricStore {
    fun find(code: String): PendingDefectMetric?
    fun save(item: PendingDefectMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDefectMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDefectMetricStore(
    var items: MutableMap<String, PendingDefectMetric>,
    var order: MutableList<String>,
) : PendingDefectMetricStore {
    override fun find(code: String): PendingDefectMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDefectMetric) {
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

    override fun list(): MutableList<PendingDefectMetric> {
        val out = mutableListOf<PendingDefectMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDefectMetricStore(): PendingDefectMetricStore {
    return MemoryPendingDefectMetricStore(mutableMapOf<String, PendingDefectMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDefectMetricStore(
    var inner: PendingDefectMetricStore,
    var cache: TtlCache<PendingDefectMetric>,
    var hits: Int,
    var misses: Int,
) : PendingDefectMetricStore {
    override fun find(code: String): PendingDefectMetric? {
        val hit = (cache.get(code) as PendingDefectMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDefectMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDefectMetric> = inner.list()
}

fun newCachedPendingDefectMetricStore(inner: PendingDefectMetricStore, ttl: Int, clock: Clock): PendingDefectMetricStore {
    return CachedPendingDefectMetricStore(
        inner = inner,
        cache = newTtlCache<PendingDefectMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending defect metrics. Empty fields match all. */
data class PendingDefectMetricQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDefectMetric): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDefectMetric>): MutableList<PendingDefectMetric> {
        val matched = filter(items) { candidate: PendingDefectMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDefectMetrics(store: PendingDefectMetricStore, query: PendingDefectMetricQuery): MutableList<PendingDefectMetric> {
    return query.apply(store.list())
}
