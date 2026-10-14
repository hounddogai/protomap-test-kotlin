package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal accessorial metrics by code. */
interface SeasonalAccessorialMetricStore {
    fun find(code: String): SeasonalAccessorialMetric?
    fun save(item: SeasonalAccessorialMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAccessorialMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAccessorialMetricStore(
    var items: MutableMap<String, SeasonalAccessorialMetric>,
    var order: MutableList<String>,
) : SeasonalAccessorialMetricStore {
    override fun find(code: String): SeasonalAccessorialMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAccessorialMetric) {
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

    override fun list(): MutableList<SeasonalAccessorialMetric> {
        val out = mutableListOf<SeasonalAccessorialMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAccessorialMetricStore(): SeasonalAccessorialMetricStore {
    return MemorySeasonalAccessorialMetricStore(
        mutableMapOf<String, SeasonalAccessorialMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAccessorialMetricStore(
    var inner: SeasonalAccessorialMetricStore,
    var cache: TtlCache<SeasonalAccessorialMetric>,
    var hits: Int,
    var misses: Int,
) : SeasonalAccessorialMetricStore {
    override fun find(code: String): SeasonalAccessorialMetric? {
        val hit = (cache.get(code) as SeasonalAccessorialMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAccessorialMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAccessorialMetric> = inner.list()
}

fun newCachedSeasonalAccessorialMetricStore(inner: SeasonalAccessorialMetricStore, ttl: Int, clock: Clock): SeasonalAccessorialMetricStore {
    return CachedSeasonalAccessorialMetricStore(
        inner = inner,
        cache = newTtlCache<SeasonalAccessorialMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal accessorial metrics. Empty fields match all. */
data class SeasonalAccessorialMetricQuery(
    var unit: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAccessorialMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<SeasonalAccessorialMetric>): MutableList<SeasonalAccessorialMetric> {
        val matched = filter(items) { candidate: SeasonalAccessorialMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAccessorialMetrics(store: SeasonalAccessorialMetricStore, query: SeasonalAccessorialMetricQuery): MutableList<SeasonalAccessorialMetric> {
    return query.apply(store.list())
}
