package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled emission factor metrics by code. */
interface ScheduledEmissionFactorMetricStore {
    fun find(code: String): ScheduledEmissionFactorMetric?
    fun save(item: ScheduledEmissionFactorMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledEmissionFactorMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledEmissionFactorMetricStore(
    var items: MutableMap<String, ScheduledEmissionFactorMetric>,
    var order: MutableList<String>,
) : ScheduledEmissionFactorMetricStore {
    override fun find(code: String): ScheduledEmissionFactorMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledEmissionFactorMetric) {
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

    override fun list(): MutableList<ScheduledEmissionFactorMetric> {
        val out = mutableListOf<ScheduledEmissionFactorMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledEmissionFactorMetricStore(): ScheduledEmissionFactorMetricStore {
    return MemoryScheduledEmissionFactorMetricStore(
        mutableMapOf<String, ScheduledEmissionFactorMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledEmissionFactorMetricStore(
    var inner: ScheduledEmissionFactorMetricStore,
    var cache: TtlCache<ScheduledEmissionFactorMetric>,
    var hits: Int,
    var misses: Int,
) : ScheduledEmissionFactorMetricStore {
    override fun find(code: String): ScheduledEmissionFactorMetric? {
        val hit = (cache.get(code) as ScheduledEmissionFactorMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledEmissionFactorMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledEmissionFactorMetric> = inner.list()
}

fun newCachedScheduledEmissionFactorMetricStore(inner: ScheduledEmissionFactorMetricStore, ttl: Int, clock: Clock): ScheduledEmissionFactorMetricStore {
    return CachedScheduledEmissionFactorMetricStore(
        inner = inner,
        cache = newTtlCache<ScheduledEmissionFactorMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled emission factor metrics. Empty fields match all. */
data class ScheduledEmissionFactorMetricQuery(
    var unit: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledEmissionFactorMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ScheduledEmissionFactorMetric>): MutableList<ScheduledEmissionFactorMetric> {
        val matched = filter(items) { candidate: ScheduledEmissionFactorMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledEmissionFactorMetrics(store: ScheduledEmissionFactorMetricStore, query: ScheduledEmissionFactorMetricQuery): MutableList<ScheduledEmissionFactorMetric> {
    return query.apply(store.list())
}
