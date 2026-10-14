package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional trailer yard forecasts by code. */
interface RegionalTrailerYardForecastStore {
    fun find(code: String): RegionalTrailerYardForecast?
    fun save(item: RegionalTrailerYardForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTrailerYardForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTrailerYardForecastStore(
    var items: MutableMap<String, RegionalTrailerYardForecast>,
    var order: MutableList<String>,
) : RegionalTrailerYardForecastStore {
    override fun find(code: String): RegionalTrailerYardForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTrailerYardForecast) {
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

    override fun list(): MutableList<RegionalTrailerYardForecast> {
        val out = mutableListOf<RegionalTrailerYardForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTrailerYardForecastStore(): RegionalTrailerYardForecastStore {
    return MemoryRegionalTrailerYardForecastStore(
        mutableMapOf<String, RegionalTrailerYardForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTrailerYardForecastStore(
    var inner: RegionalTrailerYardForecastStore,
    var cache: TtlCache<RegionalTrailerYardForecast>,
    var hits: Int,
    var misses: Int,
) : RegionalTrailerYardForecastStore {
    override fun find(code: String): RegionalTrailerYardForecast? {
        val hit = (cache.get(code) as RegionalTrailerYardForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTrailerYardForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTrailerYardForecast> = inner.list()
}

fun newCachedRegionalTrailerYardForecastStore(inner: RegionalTrailerYardForecastStore, ttl: Int, clock: Clock): RegionalTrailerYardForecastStore {
    return CachedRegionalTrailerYardForecastStore(
        inner = inner,
        cache = newTtlCache<RegionalTrailerYardForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional trailer yard forecasts. Empty fields match all. */
data class RegionalTrailerYardForecastQuery(
    var category: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTrailerYardForecast): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalTrailerYardForecast>): MutableList<RegionalTrailerYardForecast> {
        val matched = filter(items) { candidate: RegionalTrailerYardForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTrailerYardForecasts(store: RegionalTrailerYardForecastStore, query: RegionalTrailerYardForecastQuery): MutableList<RegionalTrailerYardForecast> {
    return query.apply(store.list())
}
