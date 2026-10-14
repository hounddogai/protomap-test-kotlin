package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal transit time metrics by code. */
interface SeasonalTransitTimeMetricStore {
    fun find(code: String): SeasonalTransitTimeMetric?
    fun save(item: SeasonalTransitTimeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTransitTimeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTransitTimeMetricStore(
    var items: MutableMap<String, SeasonalTransitTimeMetric>,
    var order: MutableList<String>,
) : SeasonalTransitTimeMetricStore {
    override fun find(code: String): SeasonalTransitTimeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTransitTimeMetric) {
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

    override fun list(): MutableList<SeasonalTransitTimeMetric> {
        val out = mutableListOf<SeasonalTransitTimeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTransitTimeMetricStore(): SeasonalTransitTimeMetricStore {
    return MemorySeasonalTransitTimeMetricStore(
        mutableMapOf<String, SeasonalTransitTimeMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTransitTimeMetricStore(
    var inner: SeasonalTransitTimeMetricStore,
    var cache: TtlCache<SeasonalTransitTimeMetric>,
    var hits: Int,
    var misses: Int,
) : SeasonalTransitTimeMetricStore {
    override fun find(code: String): SeasonalTransitTimeMetric? {
        val hit = (cache.get(code) as SeasonalTransitTimeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTransitTimeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTransitTimeMetric> = inner.list()
}

fun newCachedSeasonalTransitTimeMetricStore(inner: SeasonalTransitTimeMetricStore, ttl: Int, clock: Clock): SeasonalTransitTimeMetricStore {
    return CachedSeasonalTransitTimeMetricStore(
        inner = inner,
        cache = newTtlCache<SeasonalTransitTimeMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal transit time metrics. Empty fields match all. */
data class SeasonalTransitTimeMetricQuery(
    var locale: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTransitTimeMetric): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTransitTimeMetric>): MutableList<SeasonalTransitTimeMetric> {
        val matched = filter(items) { candidate: SeasonalTransitTimeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTransitTimeMetrics(store: SeasonalTransitTimeMetricStore, query: SeasonalTransitTimeMetricQuery): MutableList<SeasonalTransitTimeMetric> {
    return query.apply(store.list())
}
