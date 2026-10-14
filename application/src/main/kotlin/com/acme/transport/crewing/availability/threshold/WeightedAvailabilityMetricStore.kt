package com.acme.transport.crewing.availability.threshold

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted availability metrics by code. */
interface WeightedAvailabilityMetricStore {
    fun find(code: String): WeightedAvailabilityMetric?
    fun save(item: WeightedAvailabilityMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAvailabilityMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAvailabilityMetricStore(
    var items: MutableMap<String, WeightedAvailabilityMetric>,
    var order: MutableList<String>,
) : WeightedAvailabilityMetricStore {
    override fun find(code: String): WeightedAvailabilityMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAvailabilityMetric) {
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

    override fun list(): MutableList<WeightedAvailabilityMetric> {
        val out = mutableListOf<WeightedAvailabilityMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAvailabilityMetricStore(): WeightedAvailabilityMetricStore {
    return MemoryWeightedAvailabilityMetricStore(
        mutableMapOf<String, WeightedAvailabilityMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAvailabilityMetricStore(
    var inner: WeightedAvailabilityMetricStore,
    var cache: TtlCache<WeightedAvailabilityMetric>,
    var hits: Int,
    var misses: Int,
) : WeightedAvailabilityMetricStore {
    override fun find(code: String): WeightedAvailabilityMetric? {
        val hit = (cache.get(code) as WeightedAvailabilityMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAvailabilityMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAvailabilityMetric> = inner.list()
}

fun newCachedWeightedAvailabilityMetricStore(inner: WeightedAvailabilityMetricStore, ttl: Int, clock: Clock): WeightedAvailabilityMetricStore {
    return CachedWeightedAvailabilityMetricStore(
        inner = inner,
        cache = newTtlCache<WeightedAvailabilityMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted availability metrics. Empty fields match all. */
data class WeightedAvailabilityMetricQuery(
    var locale: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAvailabilityMetric): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAvailabilityMetric>): MutableList<WeightedAvailabilityMetric> {
        val matched = filter(items) { candidate: WeightedAvailabilityMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAvailabilityMetrics(store: WeightedAvailabilityMetricStore, query: WeightedAvailabilityMetricQuery): MutableList<WeightedAvailabilityMetric> {
    return query.apply(store.list())
}
