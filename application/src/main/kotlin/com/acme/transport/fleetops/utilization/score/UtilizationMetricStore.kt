package com.acme.transport.fleetops.utilization.score

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization metrics by code. */
interface UtilizationMetricStore {
    fun find(code: String): UtilizationMetric?
    fun save(item: UtilizationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationMetricStore(
    var items: MutableMap<String, UtilizationMetric>,
    var order: MutableList<String>,
) : UtilizationMetricStore {
    override fun find(code: String): UtilizationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationMetric) {
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

    override fun list(): MutableList<UtilizationMetric> {
        val out = mutableListOf<UtilizationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationMetricStore(): UtilizationMetricStore {
    return MemoryUtilizationMetricStore(mutableMapOf<String, UtilizationMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationMetricStore(
    var inner: UtilizationMetricStore,
    var cache: TtlCache<UtilizationMetric>,
    var hits: Int,
    var misses: Int,
) : UtilizationMetricStore {
    override fun find(code: String): UtilizationMetric? {
        val hit = (cache.get(code) as UtilizationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationMetric> = inner.list()
}

fun newCachedUtilizationMetricStore(inner: UtilizationMetricStore, ttl: Int, clock: Clock): UtilizationMetricStore {
    return CachedUtilizationMetricStore(
        inner = inner,
        cache = newTtlCache<UtilizationMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization metrics. Empty fields match all. */
data class UtilizationMetricQuery(
    var country: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationMetric>): MutableList<UtilizationMetric> {
        val matched = filter(items) { candidate: UtilizationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationMetrics(store: UtilizationMetricStore, query: UtilizationMetricQuery): MutableList<UtilizationMetric> {
    return query.apply(store.list())
}
