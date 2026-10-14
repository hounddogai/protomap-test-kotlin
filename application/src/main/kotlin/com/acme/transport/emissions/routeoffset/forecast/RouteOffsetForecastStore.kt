package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset forecasts by code. */
interface RouteOffsetForecastStore {
    fun find(code: String): RouteOffsetForecast?
    fun save(item: RouteOffsetForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetForecastStore(
    var items: MutableMap<String, RouteOffsetForecast>,
    var order: MutableList<String>,
) : RouteOffsetForecastStore {
    override fun find(code: String): RouteOffsetForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetForecast) {
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

    override fun list(): MutableList<RouteOffsetForecast> {
        val out = mutableListOf<RouteOffsetForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetForecastStore(): RouteOffsetForecastStore {
    return MemoryRouteOffsetForecastStore(mutableMapOf<String, RouteOffsetForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetForecastStore(
    var inner: RouteOffsetForecastStore,
    var cache: TtlCache<RouteOffsetForecast>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetForecastStore {
    override fun find(code: String): RouteOffsetForecast? {
        val hit = (cache.get(code) as RouteOffsetForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetForecast> = inner.list()
}

fun newCachedRouteOffsetForecastStore(inner: RouteOffsetForecastStore, ttl: Int, clock: Clock): RouteOffsetForecastStore {
    return CachedRouteOffsetForecastStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset forecasts. Empty fields match all. */
data class RouteOffsetForecastQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetForecast>): MutableList<RouteOffsetForecast> {
        val matched = filter(items) { candidate: RouteOffsetForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetForecasts(store: RouteOffsetForecastStore, query: RouteOffsetForecastQuery): MutableList<RouteOffsetForecast> {
    return query.apply(store.list())
}
