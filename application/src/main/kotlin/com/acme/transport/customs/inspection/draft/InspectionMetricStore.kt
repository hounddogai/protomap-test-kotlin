package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection metrics by code. */
interface InspectionMetricStore {
    fun find(code: String): InspectionMetric?
    fun save(item: InspectionMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionMetricStore(
    var items: MutableMap<String, InspectionMetric>,
    var order: MutableList<String>,
) : InspectionMetricStore {
    override fun find(code: String): InspectionMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionMetric) {
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

    override fun list(): MutableList<InspectionMetric> {
        val out = mutableListOf<InspectionMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionMetricStore(): InspectionMetricStore {
    return MemoryInspectionMetricStore(mutableMapOf<String, InspectionMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionMetricStore(
    var inner: InspectionMetricStore,
    var cache: TtlCache<InspectionMetric>,
    var hits: Int,
    var misses: Int,
) : InspectionMetricStore {
    override fun find(code: String): InspectionMetric? {
        val hit = (cache.get(code) as InspectionMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionMetric> = inner.list()
}

fun newCachedInspectionMetricStore(inner: InspectionMetricStore, ttl: Int, clock: Clock): InspectionMetricStore {
    return CachedInspectionMetricStore(
        inner = inner,
        cache = newTtlCache<InspectionMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection metrics. Empty fields match all. */
data class InspectionMetricQuery(
    var country: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionMetric>): MutableList<InspectionMetric> {
        val matched = filter(items) { candidate: InspectionMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionMetrics(store: InspectionMetricStore, query: InspectionMetricQuery): MutableList<InspectionMetric> {
    return query.apply(store.list())
}
