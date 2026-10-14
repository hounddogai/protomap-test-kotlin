package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback roster forecasts by code. */
interface FallbackRosterForecastStore {
    fun find(code: String): FallbackRosterForecast?
    fun save(item: FallbackRosterForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRosterForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRosterForecastStore(
    var items: MutableMap<String, FallbackRosterForecast>,
    var order: MutableList<String>,
) : FallbackRosterForecastStore {
    override fun find(code: String): FallbackRosterForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRosterForecast) {
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

    override fun list(): MutableList<FallbackRosterForecast> {
        val out = mutableListOf<FallbackRosterForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRosterForecastStore(): FallbackRosterForecastStore {
    return MemoryFallbackRosterForecastStore(
        mutableMapOf<String, FallbackRosterForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRosterForecastStore(
    var inner: FallbackRosterForecastStore,
    var cache: TtlCache<FallbackRosterForecast>,
    var hits: Int,
    var misses: Int,
) : FallbackRosterForecastStore {
    override fun find(code: String): FallbackRosterForecast? {
        val hit = (cache.get(code) as FallbackRosterForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRosterForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRosterForecast> = inner.list()
}

fun newCachedFallbackRosterForecastStore(inner: FallbackRosterForecastStore, ttl: Int, clock: Clock): FallbackRosterForecastStore {
    return CachedFallbackRosterForecastStore(
        inner = inner,
        cache = newTtlCache<FallbackRosterForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback roster forecasts. Empty fields match all. */
data class FallbackRosterForecastQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRosterForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRosterForecast>): MutableList<FallbackRosterForecast> {
        val matched = filter(items) { candidate: FallbackRosterForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRosterForecasts(store: FallbackRosterForecastStore, query: FallbackRosterForecastQuery): MutableList<FallbackRosterForecast> {
    return query.apply(store.list())
}
