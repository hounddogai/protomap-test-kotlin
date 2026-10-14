package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment metrics by code. */
interface AppointmentMetricStore {
    fun find(code: String): AppointmentMetric?
    fun save(item: AppointmentMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentMetricStore(
    var items: MutableMap<String, AppointmentMetric>,
    var order: MutableList<String>,
) : AppointmentMetricStore {
    override fun find(code: String): AppointmentMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentMetric) {
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

    override fun list(): MutableList<AppointmentMetric> {
        val out = mutableListOf<AppointmentMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentMetricStore(): AppointmentMetricStore {
    return MemoryAppointmentMetricStore(mutableMapOf<String, AppointmentMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentMetricStore(
    var inner: AppointmentMetricStore,
    var cache: TtlCache<AppointmentMetric>,
    var hits: Int,
    var misses: Int,
) : AppointmentMetricStore {
    override fun find(code: String): AppointmentMetric? {
        val hit = (cache.get(code) as AppointmentMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentMetric> = inner.list()
}

fun newCachedAppointmentMetricStore(inner: AppointmentMetricStore, ttl: Int, clock: Clock): AppointmentMetricStore {
    return CachedAppointmentMetricStore(
        inner = inner,
        cache = newTtlCache<AppointmentMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment metrics. Empty fields match all. */
data class AppointmentMetricQuery(
    var category: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentMetric): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentMetric>): MutableList<AppointmentMetric> {
        val matched = filter(items) { candidate: AppointmentMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentMetrics(store: AppointmentMetricStore, query: AppointmentMetricQuery): MutableList<AppointmentMetric> {
    return query.apply(store.list())
}
