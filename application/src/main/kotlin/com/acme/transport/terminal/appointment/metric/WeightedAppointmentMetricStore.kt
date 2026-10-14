package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted appointment metrics by code. */
interface WeightedAppointmentMetricStore {
    fun find(code: String): WeightedAppointmentMetric?
    fun save(item: WeightedAppointmentMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAppointmentMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAppointmentMetricStore(
    var items: MutableMap<String, WeightedAppointmentMetric>,
    var order: MutableList<String>,
) : WeightedAppointmentMetricStore {
    override fun find(code: String): WeightedAppointmentMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAppointmentMetric) {
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

    override fun list(): MutableList<WeightedAppointmentMetric> {
        val out = mutableListOf<WeightedAppointmentMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAppointmentMetricStore(): WeightedAppointmentMetricStore {
    return MemoryWeightedAppointmentMetricStore(
        mutableMapOf<String, WeightedAppointmentMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAppointmentMetricStore(
    var inner: WeightedAppointmentMetricStore,
    var cache: TtlCache<WeightedAppointmentMetric>,
    var hits: Int,
    var misses: Int,
) : WeightedAppointmentMetricStore {
    override fun find(code: String): WeightedAppointmentMetric? {
        val hit = (cache.get(code) as WeightedAppointmentMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAppointmentMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAppointmentMetric> = inner.list()
}

fun newCachedWeightedAppointmentMetricStore(inner: WeightedAppointmentMetricStore, ttl: Int, clock: Clock): WeightedAppointmentMetricStore {
    return CachedWeightedAppointmentMetricStore(
        inner = inner,
        cache = newTtlCache<WeightedAppointmentMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted appointment metrics. Empty fields match all. */
data class WeightedAppointmentMetricQuery(
    var region: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAppointmentMetric): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<WeightedAppointmentMetric>): MutableList<WeightedAppointmentMetric> {
        val matched = filter(items) { candidate: WeightedAppointmentMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAppointmentMetrics(store: WeightedAppointmentMetricStore, query: WeightedAppointmentMetricQuery): MutableList<WeightedAppointmentMetric> {
    return query.apply(store.list())
}
