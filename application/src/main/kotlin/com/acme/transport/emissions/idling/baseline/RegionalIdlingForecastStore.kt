package com.acme.transport.emissions.idling.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional idling forecasts by code. */
interface RegionalIdlingForecastStore {
    fun find(code: String): RegionalIdlingForecast?
    fun save(item: RegionalIdlingForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalIdlingForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalIdlingForecastStore(
    var items: MutableMap<String, RegionalIdlingForecast>,
    var order: MutableList<String>,
) : RegionalIdlingForecastStore {
    override fun find(code: String): RegionalIdlingForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalIdlingForecast) {
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

    override fun list(): MutableList<RegionalIdlingForecast> {
        val out = mutableListOf<RegionalIdlingForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalIdlingForecastStore(): RegionalIdlingForecastStore {
    return MemoryRegionalIdlingForecastStore(
        mutableMapOf<String, RegionalIdlingForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalIdlingForecastStore(
    var inner: RegionalIdlingForecastStore,
    var cache: TtlCache<RegionalIdlingForecast>,
    var hits: Int,
    var misses: Int,
) : RegionalIdlingForecastStore {
    override fun find(code: String): RegionalIdlingForecast? {
        val hit = (cache.get(code) as RegionalIdlingForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalIdlingForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalIdlingForecast> = inner.list()
}

fun newCachedRegionalIdlingForecastStore(inner: RegionalIdlingForecastStore, ttl: Int, clock: Clock): RegionalIdlingForecastStore {
    return CachedRegionalIdlingForecastStore(
        inner = inner,
        cache = newTtlCache<RegionalIdlingForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional idling forecasts. Empty fields match all. */
data class RegionalIdlingForecastQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalIdlingForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalIdlingForecast>): MutableList<RegionalIdlingForecast> {
        val matched = filter(items) { candidate: RegionalIdlingForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalIdlingForecasts(store: RegionalIdlingForecastStore, query: RegionalIdlingForecastQuery): MutableList<RegionalIdlingForecast> {
    return query.apply(store.list())
}
