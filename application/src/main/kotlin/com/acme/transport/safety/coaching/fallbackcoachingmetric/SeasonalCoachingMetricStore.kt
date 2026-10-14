package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal coaching metrics by code. */
interface SeasonalCoachingMetricStore {
    fun find(code: String): SeasonalCoachingMetric?
    fun save(item: SeasonalCoachingMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCoachingMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCoachingMetricStore(
    var items: MutableMap<String, SeasonalCoachingMetric>,
    var order: MutableList<String>,
) : SeasonalCoachingMetricStore {
    override fun find(code: String): SeasonalCoachingMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCoachingMetric) {
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

    override fun list(): MutableList<SeasonalCoachingMetric> {
        val out = mutableListOf<SeasonalCoachingMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCoachingMetricStore(): SeasonalCoachingMetricStore {
    return MemorySeasonalCoachingMetricStore(
        mutableMapOf<String, SeasonalCoachingMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCoachingMetricStore(
    var inner: SeasonalCoachingMetricStore,
    var cache: TtlCache<SeasonalCoachingMetric>,
    var hits: Int,
    var misses: Int,
) : SeasonalCoachingMetricStore {
    override fun find(code: String): SeasonalCoachingMetric? {
        val hit = (cache.get(code) as SeasonalCoachingMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCoachingMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCoachingMetric> = inner.list()
}

fun newCachedSeasonalCoachingMetricStore(inner: SeasonalCoachingMetricStore, ttl: Int, clock: Clock): SeasonalCoachingMetricStore {
    return CachedSeasonalCoachingMetricStore(
        inner = inner,
        cache = newTtlCache<SeasonalCoachingMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal coaching metrics. Empty fields match all. */
data class SeasonalCoachingMetricQuery(
    var category: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCoachingMetric): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<SeasonalCoachingMetric>): MutableList<SeasonalCoachingMetric> {
        val matched = filter(items) { candidate: SeasonalCoachingMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCoachingMetrics(store: SeasonalCoachingMetricStore, query: SeasonalCoachingMetricQuery): MutableList<SeasonalCoachingMetric> {
    return query.apply(store.list())
}
