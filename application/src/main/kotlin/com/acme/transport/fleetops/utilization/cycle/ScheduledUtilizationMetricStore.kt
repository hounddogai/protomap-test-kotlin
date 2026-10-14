package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled utilization metrics by code. */
interface ScheduledUtilizationMetricStore {
    fun find(code: String): ScheduledUtilizationMetric?
    fun save(item: ScheduledUtilizationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledUtilizationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledUtilizationMetricStore(
    var items: MutableMap<String, ScheduledUtilizationMetric>,
    var order: MutableList<String>,
) : ScheduledUtilizationMetricStore {
    override fun find(code: String): ScheduledUtilizationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledUtilizationMetric) {
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

    override fun list(): MutableList<ScheduledUtilizationMetric> {
        val out = mutableListOf<ScheduledUtilizationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledUtilizationMetricStore(): ScheduledUtilizationMetricStore {
    return MemoryScheduledUtilizationMetricStore(
        mutableMapOf<String, ScheduledUtilizationMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledUtilizationMetricStore(
    var inner: ScheduledUtilizationMetricStore,
    var cache: TtlCache<ScheduledUtilizationMetric>,
    var hits: Int,
    var misses: Int,
) : ScheduledUtilizationMetricStore {
    override fun find(code: String): ScheduledUtilizationMetric? {
        val hit = (cache.get(code) as ScheduledUtilizationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledUtilizationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledUtilizationMetric> = inner.list()
}

fun newCachedScheduledUtilizationMetricStore(inner: ScheduledUtilizationMetricStore, ttl: Int, clock: Clock): ScheduledUtilizationMetricStore {
    return CachedScheduledUtilizationMetricStore(
        inner = inner,
        cache = newTtlCache<ScheduledUtilizationMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled utilization metrics. Empty fields match all. */
data class ScheduledUtilizationMetricQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledUtilizationMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledUtilizationMetric>): MutableList<ScheduledUtilizationMetric> {
        val matched = filter(items) { candidate: ScheduledUtilizationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledUtilizationMetrics(store: ScheduledUtilizationMetricStore, query: ScheduledUtilizationMetricQuery): MutableList<ScheduledUtilizationMetric> {
    return query.apply(store.list())
}
