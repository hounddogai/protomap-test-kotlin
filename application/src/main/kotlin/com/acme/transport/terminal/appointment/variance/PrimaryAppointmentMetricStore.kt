package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary appointment metrics by code. */
interface PrimaryAppointmentMetricStore {
    fun find(code: String): PrimaryAppointmentMetric?
    fun save(item: PrimaryAppointmentMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAppointmentMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAppointmentMetricStore(
    var items: MutableMap<String, PrimaryAppointmentMetric>,
    var order: MutableList<String>,
) : PrimaryAppointmentMetricStore {
    override fun find(code: String): PrimaryAppointmentMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAppointmentMetric) {
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

    override fun list(): MutableList<PrimaryAppointmentMetric> {
        val out = mutableListOf<PrimaryAppointmentMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAppointmentMetricStore(): PrimaryAppointmentMetricStore {
    return MemoryPrimaryAppointmentMetricStore(
        mutableMapOf<String, PrimaryAppointmentMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAppointmentMetricStore(
    var inner: PrimaryAppointmentMetricStore,
    var cache: TtlCache<PrimaryAppointmentMetric>,
    var hits: Int,
    var misses: Int,
) : PrimaryAppointmentMetricStore {
    override fun find(code: String): PrimaryAppointmentMetric? {
        val hit = (cache.get(code) as PrimaryAppointmentMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAppointmentMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAppointmentMetric> = inner.list()
}

fun newCachedPrimaryAppointmentMetricStore(inner: PrimaryAppointmentMetricStore, ttl: Int, clock: Clock): PrimaryAppointmentMetricStore {
    return CachedPrimaryAppointmentMetricStore(
        inner = inner,
        cache = newTtlCache<PrimaryAppointmentMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary appointment metrics. Empty fields match all. */
data class PrimaryAppointmentMetricQuery(
    var locale: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAppointmentMetric): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<PrimaryAppointmentMetric>): MutableList<PrimaryAppointmentMetric> {
        val matched = filter(items) { candidate: PrimaryAppointmentMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAppointmentMetrics(store: PrimaryAppointmentMetricStore, query: PrimaryAppointmentMetricQuery): MutableList<PrimaryAppointmentMetric> {
    return query.apply(store.list())
}
