package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled service plan metrics by code. */
interface ScheduledServicePlanMetricStore {
    fun find(code: String): ScheduledServicePlanMetric?
    fun save(item: ScheduledServicePlanMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledServicePlanMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledServicePlanMetricStore(
    var items: MutableMap<String, ScheduledServicePlanMetric>,
    var order: MutableList<String>,
) : ScheduledServicePlanMetricStore {
    override fun find(code: String): ScheduledServicePlanMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledServicePlanMetric) {
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

    override fun list(): MutableList<ScheduledServicePlanMetric> {
        val out = mutableListOf<ScheduledServicePlanMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledServicePlanMetricStore(): ScheduledServicePlanMetricStore {
    return MemoryScheduledServicePlanMetricStore(
        mutableMapOf<String, ScheduledServicePlanMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledServicePlanMetricStore(
    var inner: ScheduledServicePlanMetricStore,
    var cache: TtlCache<ScheduledServicePlanMetric>,
    var hits: Int,
    var misses: Int,
) : ScheduledServicePlanMetricStore {
    override fun find(code: String): ScheduledServicePlanMetric? {
        val hit = (cache.get(code) as ScheduledServicePlanMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledServicePlanMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledServicePlanMetric> = inner.list()
}

fun newCachedScheduledServicePlanMetricStore(inner: ScheduledServicePlanMetricStore, ttl: Int, clock: Clock): ScheduledServicePlanMetricStore {
    return CachedScheduledServicePlanMetricStore(
        inner = inner,
        cache = newTtlCache<ScheduledServicePlanMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled service plan metrics. Empty fields match all. */
data class ScheduledServicePlanMetricQuery(
    var country: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledServicePlanMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledServicePlanMetric>): MutableList<ScheduledServicePlanMetric> {
        val matched = filter(items) { candidate: ScheduledServicePlanMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledServicePlanMetrics(store: ScheduledServicePlanMetricStore, query: ScheduledServicePlanMetricQuery): MutableList<ScheduledServicePlanMetric> {
    return query.apply(store.list())
}
