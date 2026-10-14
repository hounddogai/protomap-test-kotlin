package com.acme.transport.emissions.carbonreport.tier

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict carbon report metrics by code. */
interface StrictCarbonReportMetricStore {
    fun find(code: String): StrictCarbonReportMetric?
    fun save(item: StrictCarbonReportMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCarbonReportMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCarbonReportMetricStore(
    var items: MutableMap<String, StrictCarbonReportMetric>,
    var order: MutableList<String>,
) : StrictCarbonReportMetricStore {
    override fun find(code: String): StrictCarbonReportMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCarbonReportMetric) {
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

    override fun list(): MutableList<StrictCarbonReportMetric> {
        val out = mutableListOf<StrictCarbonReportMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCarbonReportMetricStore(): StrictCarbonReportMetricStore {
    return MemoryStrictCarbonReportMetricStore(
        mutableMapOf<String, StrictCarbonReportMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCarbonReportMetricStore(
    var inner: StrictCarbonReportMetricStore,
    var cache: TtlCache<StrictCarbonReportMetric>,
    var hits: Int,
    var misses: Int,
) : StrictCarbonReportMetricStore {
    override fun find(code: String): StrictCarbonReportMetric? {
        val hit = (cache.get(code) as StrictCarbonReportMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCarbonReportMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCarbonReportMetric> = inner.list()
}

fun newCachedStrictCarbonReportMetricStore(inner: StrictCarbonReportMetricStore, ttl: Int, clock: Clock): StrictCarbonReportMetricStore {
    return CachedStrictCarbonReportMetricStore(
        inner = inner,
        cache = newTtlCache<StrictCarbonReportMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict carbon report metrics. Empty fields match all. */
data class StrictCarbonReportMetricQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCarbonReportMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictCarbonReportMetric>): MutableList<StrictCarbonReportMetric> {
        val matched = filter(items) { candidate: StrictCarbonReportMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCarbonReportMetrics(store: StrictCarbonReportMetricStore, query: StrictCarbonReportMetricQuery): MutableList<StrictCarbonReportMetric> {
    return query.apply(store.list())
}
