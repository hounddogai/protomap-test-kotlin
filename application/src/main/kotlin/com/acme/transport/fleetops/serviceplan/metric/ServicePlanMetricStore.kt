package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan metrics by code. */
interface ServicePlanMetricStore {
    fun find(code: String): ServicePlanMetric?
    fun save(item: ServicePlanMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanMetricStore(
    var items: MutableMap<String, ServicePlanMetric>,
    var order: MutableList<String>,
) : ServicePlanMetricStore {
    override fun find(code: String): ServicePlanMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanMetric) {
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

    override fun list(): MutableList<ServicePlanMetric> {
        val out = mutableListOf<ServicePlanMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanMetricStore(): ServicePlanMetricStore {
    return MemoryServicePlanMetricStore(mutableMapOf<String, ServicePlanMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanMetricStore(
    var inner: ServicePlanMetricStore,
    var cache: TtlCache<ServicePlanMetric>,
    var hits: Int,
    var misses: Int,
) : ServicePlanMetricStore {
    override fun find(code: String): ServicePlanMetric? {
        val hit = (cache.get(code) as ServicePlanMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanMetric> = inner.list()
}

fun newCachedServicePlanMetricStore(inner: ServicePlanMetricStore, ttl: Int, clock: Clock): ServicePlanMetricStore {
    return CachedServicePlanMetricStore(
        inner = inner,
        cache = newTtlCache<ServicePlanMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan metrics. Empty fields match all. */
data class ServicePlanMetricQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanMetric): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<ServicePlanMetric>): MutableList<ServicePlanMetric> {
        val matched = filter(items) { candidate: ServicePlanMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanMetrics(store: ServicePlanMetricStore, query: ServicePlanMetricQuery): MutableList<ServicePlanMetric> {
    return query.apply(store.list())
}
