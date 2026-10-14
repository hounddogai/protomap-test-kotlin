package com.acme.transport.crewing.roster.forecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal roster forecasts by code. */
interface SeasonalRosterForecastStore {
    fun find(code: String): SeasonalRosterForecast?
    fun save(item: SeasonalRosterForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRosterForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRosterForecastStore(
    var items: MutableMap<String, SeasonalRosterForecast>,
    var order: MutableList<String>,
) : SeasonalRosterForecastStore {
    override fun find(code: String): SeasonalRosterForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRosterForecast) {
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

    override fun list(): MutableList<SeasonalRosterForecast> {
        val out = mutableListOf<SeasonalRosterForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRosterForecastStore(): SeasonalRosterForecastStore {
    return MemorySeasonalRosterForecastStore(
        mutableMapOf<String, SeasonalRosterForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRosterForecastStore(
    var inner: SeasonalRosterForecastStore,
    var cache: TtlCache<SeasonalRosterForecast>,
    var hits: Int,
    var misses: Int,
) : SeasonalRosterForecastStore {
    override fun find(code: String): SeasonalRosterForecast? {
        val hit = (cache.get(code) as SeasonalRosterForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRosterForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRosterForecast> = inner.list()
}

fun newCachedSeasonalRosterForecastStore(inner: SeasonalRosterForecastStore, ttl: Int, clock: Clock): SeasonalRosterForecastStore {
    return CachedSeasonalRosterForecastStore(
        inner = inner,
        cache = newTtlCache<SeasonalRosterForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal roster forecasts. Empty fields match all. */
data class SeasonalRosterForecastQuery(
    var source: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRosterForecast): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRosterForecast>): MutableList<SeasonalRosterForecast> {
        val matched = filter(items) { candidate: SeasonalRosterForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRosterForecasts(store: SeasonalRosterForecastStore, query: SeasonalRosterForecastQuery): MutableList<SeasonalRosterForecast> {
    return query.apply(store.list())
}
