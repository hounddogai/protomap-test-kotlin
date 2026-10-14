package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal ev charge forecasts by code. */
interface SeasonalEvChargeForecastStore {
    fun find(code: String): SeasonalEvChargeForecast?
    fun save(item: SeasonalEvChargeForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalEvChargeForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalEvChargeForecastStore(
    var items: MutableMap<String, SeasonalEvChargeForecast>,
    var order: MutableList<String>,
) : SeasonalEvChargeForecastStore {
    override fun find(code: String): SeasonalEvChargeForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalEvChargeForecast) {
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

    override fun list(): MutableList<SeasonalEvChargeForecast> {
        val out = mutableListOf<SeasonalEvChargeForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalEvChargeForecastStore(): SeasonalEvChargeForecastStore {
    return MemorySeasonalEvChargeForecastStore(
        mutableMapOf<String, SeasonalEvChargeForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalEvChargeForecastStore(
    var inner: SeasonalEvChargeForecastStore,
    var cache: TtlCache<SeasonalEvChargeForecast>,
    var hits: Int,
    var misses: Int,
) : SeasonalEvChargeForecastStore {
    override fun find(code: String): SeasonalEvChargeForecast? {
        val hit = (cache.get(code) as SeasonalEvChargeForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalEvChargeForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalEvChargeForecast> = inner.list()
}

fun newCachedSeasonalEvChargeForecastStore(inner: SeasonalEvChargeForecastStore, ttl: Int, clock: Clock): SeasonalEvChargeForecastStore {
    return CachedSeasonalEvChargeForecastStore(
        inner = inner,
        cache = newTtlCache<SeasonalEvChargeForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal ev charge forecasts. Empty fields match all. */
data class SeasonalEvChargeForecastQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalEvChargeForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SeasonalEvChargeForecast>): MutableList<SeasonalEvChargeForecast> {
        val matched = filter(items) { candidate: SeasonalEvChargeForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalEvChargeForecasts(store: SeasonalEvChargeForecastStore, query: SeasonalEvChargeForecastQuery): MutableList<SeasonalEvChargeForecast> {
    return query.apply(store.list())
}
