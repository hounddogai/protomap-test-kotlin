package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending service plan metrics by code. */
interface PendingServicePlanMetricStore {
    fun find(code: String): PendingServicePlanMetric?
    fun save(item: PendingServicePlanMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingServicePlanMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingServicePlanMetricStore(
    var items: MutableMap<String, PendingServicePlanMetric>,
    var order: MutableList<String>,
) : PendingServicePlanMetricStore {
    override fun find(code: String): PendingServicePlanMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingServicePlanMetric) {
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

    override fun list(): MutableList<PendingServicePlanMetric> {
        val out = mutableListOf<PendingServicePlanMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingServicePlanMetricStore(): PendingServicePlanMetricStore {
    return MemoryPendingServicePlanMetricStore(
        mutableMapOf<String, PendingServicePlanMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingServicePlanMetricStore(
    var inner: PendingServicePlanMetricStore,
    var cache: TtlCache<PendingServicePlanMetric>,
    var hits: Int,
    var misses: Int,
) : PendingServicePlanMetricStore {
    override fun find(code: String): PendingServicePlanMetric? {
        val hit = (cache.get(code) as PendingServicePlanMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingServicePlanMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingServicePlanMetric> = inner.list()
}

fun newCachedPendingServicePlanMetricStore(inner: PendingServicePlanMetricStore, ttl: Int, clock: Clock): PendingServicePlanMetricStore {
    return CachedPendingServicePlanMetricStore(
        inner = inner,
        cache = newTtlCache<PendingServicePlanMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending service plan metrics. Empty fields match all. */
data class PendingServicePlanMetricQuery(
    var unit: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingServicePlanMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingServicePlanMetric>): MutableList<PendingServicePlanMetric> {
        val matched = filter(items) { candidate: PendingServicePlanMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingServicePlanMetrics(store: PendingServicePlanMetricStore, query: PendingServicePlanMetricQuery): MutableList<PendingServicePlanMetric> {
    return query.apply(store.list())
}
