package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional ev charge metrics by code. */
interface RegionalEvChargeMetricStore {
    fun find(code: String): RegionalEvChargeMetric?
    fun save(item: RegionalEvChargeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalEvChargeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalEvChargeMetricStore(
    var items: MutableMap<String, RegionalEvChargeMetric>,
    var order: MutableList<String>,
) : RegionalEvChargeMetricStore {
    override fun find(code: String): RegionalEvChargeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalEvChargeMetric) {
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

    override fun list(): MutableList<RegionalEvChargeMetric> {
        val out = mutableListOf<RegionalEvChargeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalEvChargeMetricStore(): RegionalEvChargeMetricStore {
    return MemoryRegionalEvChargeMetricStore(
        mutableMapOf<String, RegionalEvChargeMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalEvChargeMetricStore(
    var inner: RegionalEvChargeMetricStore,
    var cache: TtlCache<RegionalEvChargeMetric>,
    var hits: Int,
    var misses: Int,
) : RegionalEvChargeMetricStore {
    override fun find(code: String): RegionalEvChargeMetric? {
        val hit = (cache.get(code) as RegionalEvChargeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalEvChargeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalEvChargeMetric> = inner.list()
}

fun newCachedRegionalEvChargeMetricStore(inner: RegionalEvChargeMetricStore, ttl: Int, clock: Clock): RegionalEvChargeMetricStore {
    return CachedRegionalEvChargeMetricStore(
        inner = inner,
        cache = newTtlCache<RegionalEvChargeMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional ev charge metrics. Empty fields match all. */
data class RegionalEvChargeMetricQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalEvChargeMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalEvChargeMetric>): MutableList<RegionalEvChargeMetric> {
        val matched = filter(items) { candidate: RegionalEvChargeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalEvChargeMetrics(store: RegionalEvChargeMetricStore, query: RegionalEvChargeMetricQuery): MutableList<RegionalEvChargeMetric> {
    return query.apply(store.list())
}
