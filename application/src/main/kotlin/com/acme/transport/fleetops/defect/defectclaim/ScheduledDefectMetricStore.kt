package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled defect metrics by code. */
interface ScheduledDefectMetricStore {
    fun find(code: String): ScheduledDefectMetric?
    fun save(item: ScheduledDefectMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDefectMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDefectMetricStore(
    var items: MutableMap<String, ScheduledDefectMetric>,
    var order: MutableList<String>,
) : ScheduledDefectMetricStore {
    override fun find(code: String): ScheduledDefectMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDefectMetric) {
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

    override fun list(): MutableList<ScheduledDefectMetric> {
        val out = mutableListOf<ScheduledDefectMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDefectMetricStore(): ScheduledDefectMetricStore {
    return MemoryScheduledDefectMetricStore(
        mutableMapOf<String, ScheduledDefectMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDefectMetricStore(
    var inner: ScheduledDefectMetricStore,
    var cache: TtlCache<ScheduledDefectMetric>,
    var hits: Int,
    var misses: Int,
) : ScheduledDefectMetricStore {
    override fun find(code: String): ScheduledDefectMetric? {
        val hit = (cache.get(code) as ScheduledDefectMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDefectMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDefectMetric> = inner.list()
}

fun newCachedScheduledDefectMetricStore(inner: ScheduledDefectMetricStore, ttl: Int, clock: Clock): ScheduledDefectMetricStore {
    return CachedScheduledDefectMetricStore(
        inner = inner,
        cache = newTtlCache<ScheduledDefectMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled defect metrics. Empty fields match all. */
data class ScheduledDefectMetricQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDefectMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDefectMetric>): MutableList<ScheduledDefectMetric> {
        val matched = filter(items) { candidate: ScheduledDefectMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDefectMetrics(store: ScheduledDefectMetricStore, query: ScheduledDefectMetricQuery): MutableList<ScheduledDefectMetric> {
    return query.apply(store.list())
}
