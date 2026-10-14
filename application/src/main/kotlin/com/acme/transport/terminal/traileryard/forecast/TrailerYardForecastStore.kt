package com.acme.transport.terminal.traileryard.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard forecasts by code. */
interface TrailerYardForecastStore {
    fun find(code: String): TrailerYardForecast?
    fun save(item: TrailerYardForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardForecastStore(
    var items: MutableMap<String, TrailerYardForecast>,
    var order: MutableList<String>,
) : TrailerYardForecastStore {
    override fun find(code: String): TrailerYardForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardForecast) {
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

    override fun list(): MutableList<TrailerYardForecast> {
        val out = mutableListOf<TrailerYardForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardForecastStore(): TrailerYardForecastStore {
    return MemoryTrailerYardForecastStore(mutableMapOf<String, TrailerYardForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardForecastStore(
    var inner: TrailerYardForecastStore,
    var cache: TtlCache<TrailerYardForecast>,
    var hits: Int,
    var misses: Int,
) : TrailerYardForecastStore {
    override fun find(code: String): TrailerYardForecast? {
        val hit = (cache.get(code) as TrailerYardForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardForecast> = inner.list()
}

fun newCachedTrailerYardForecastStore(inner: TrailerYardForecastStore, ttl: Int, clock: Clock): TrailerYardForecastStore {
    return CachedTrailerYardForecastStore(
        inner = inner,
        cache = newTtlCache<TrailerYardForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard forecasts. Empty fields match all. */
data class TrailerYardForecastQuery(
    var segment: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardForecast): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TrailerYardForecast>): MutableList<TrailerYardForecast> {
        val matched = filter(items) { candidate: TrailerYardForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardForecasts(store: TrailerYardForecastStore, query: TrailerYardForecastQuery): MutableList<TrailerYardForecast> {
    return query.apply(store.list())
}
