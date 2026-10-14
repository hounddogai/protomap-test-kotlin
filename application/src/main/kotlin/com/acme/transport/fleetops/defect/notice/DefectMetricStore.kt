package com.acme.transport.fleetops.defect.notice

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect metrics by code. */
interface DefectMetricStore {
    fun find(code: String): DefectMetric?
    fun save(item: DefectMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectMetricStore(
    var items: MutableMap<String, DefectMetric>,
    var order: MutableList<String>,
) : DefectMetricStore {
    override fun find(code: String): DefectMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectMetric) {
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

    override fun list(): MutableList<DefectMetric> {
        val out = mutableListOf<DefectMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectMetricStore(): DefectMetricStore {
    return MemoryDefectMetricStore(mutableMapOf<String, DefectMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectMetricStore(
    var inner: DefectMetricStore,
    var cache: TtlCache<DefectMetric>,
    var hits: Int,
    var misses: Int,
) : DefectMetricStore {
    override fun find(code: String): DefectMetric? {
        val hit = (cache.get(code) as DefectMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectMetric> = inner.list()
}

fun newCachedDefectMetricStore(inner: DefectMetricStore, ttl: Int, clock: Clock): DefectMetricStore {
    return CachedDefectMetricStore(
        inner = inner,
        cache = newTtlCache<DefectMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect metrics. Empty fields match all. */
data class DefectMetricQuery(
    var country: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectMetric>): MutableList<DefectMetric> {
        val matched = filter(items) { candidate: DefectMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectMetrics(store: DefectMetricStore, query: DefectMetricQuery): MutableList<DefectMetric> {
    return query.apply(store.list())
}
