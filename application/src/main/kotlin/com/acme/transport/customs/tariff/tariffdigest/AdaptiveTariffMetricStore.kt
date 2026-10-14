package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive tariff metrics by code. */
interface AdaptiveTariffMetricStore {
    fun find(code: String): AdaptiveTariffMetric?
    fun save(item: AdaptiveTariffMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTariffMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTariffMetricStore(
    var items: MutableMap<String, AdaptiveTariffMetric>,
    var order: MutableList<String>,
) : AdaptiveTariffMetricStore {
    override fun find(code: String): AdaptiveTariffMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTariffMetric) {
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

    override fun list(): MutableList<AdaptiveTariffMetric> {
        val out = mutableListOf<AdaptiveTariffMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTariffMetricStore(): AdaptiveTariffMetricStore {
    return MemoryAdaptiveTariffMetricStore(
        mutableMapOf<String, AdaptiveTariffMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTariffMetricStore(
    var inner: AdaptiveTariffMetricStore,
    var cache: TtlCache<AdaptiveTariffMetric>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTariffMetricStore {
    override fun find(code: String): AdaptiveTariffMetric? {
        val hit = (cache.get(code) as AdaptiveTariffMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTariffMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTariffMetric> = inner.list()
}

fun newCachedAdaptiveTariffMetricStore(inner: AdaptiveTariffMetricStore, ttl: Int, clock: Clock): AdaptiveTariffMetricStore {
    return CachedAdaptiveTariffMetricStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTariffMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive tariff metrics. Empty fields match all. */
data class AdaptiveTariffMetricQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTariffMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTariffMetric>): MutableList<AdaptiveTariffMetric> {
        val matched = filter(items) { candidate: AdaptiveTariffMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTariffMetrics(store: AdaptiveTariffMetricStore, query: AdaptiveTariffMetricQuery): MutableList<AdaptiveTariffMetric> {
    return query.apply(store.list())
}
