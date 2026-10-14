package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff forecasts by code. */
interface CutoffForecastStore {
    fun find(code: String): CutoffForecast?
    fun save(item: CutoffForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffForecastStore(
    var items: MutableMap<String, CutoffForecast>,
    var order: MutableList<String>,
) : CutoffForecastStore {
    override fun find(code: String): CutoffForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffForecast) {
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

    override fun list(): MutableList<CutoffForecast> {
        val out = mutableListOf<CutoffForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffForecastStore(): CutoffForecastStore {
    return MemoryCutoffForecastStore(mutableMapOf<String, CutoffForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffForecastStore(
    var inner: CutoffForecastStore,
    var cache: TtlCache<CutoffForecast>,
    var hits: Int,
    var misses: Int,
) : CutoffForecastStore {
    override fun find(code: String): CutoffForecast? {
        val hit = (cache.get(code) as CutoffForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffForecast> = inner.list()
}

fun newCachedCutoffForecastStore(inner: CutoffForecastStore, ttl: Int, clock: Clock): CutoffForecastStore {
    return CachedCutoffForecastStore(
        inner = inner,
        cache = newTtlCache<CutoffForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff forecasts. Empty fields match all. */
data class CutoffForecastQuery(
    var depot: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffForecast): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffForecast>): MutableList<CutoffForecast> {
        val matched = filter(items) { candidate: CutoffForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffForecasts(store: CutoffForecastStore, query: CutoffForecastQuery): MutableList<CutoffForecast> {
    return query.apply(store.list())
}
