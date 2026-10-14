package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback drop forecasts by code. */
interface FallbackDropForecastStore {
    fun find(code: String): FallbackDropForecast?
    fun save(item: FallbackDropForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDropForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDropForecastStore(
    var items: MutableMap<String, FallbackDropForecast>,
    var order: MutableList<String>,
) : FallbackDropForecastStore {
    override fun find(code: String): FallbackDropForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDropForecast) {
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

    override fun list(): MutableList<FallbackDropForecast> {
        val out = mutableListOf<FallbackDropForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDropForecastStore(): FallbackDropForecastStore {
    return MemoryFallbackDropForecastStore(
        mutableMapOf<String, FallbackDropForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDropForecastStore(
    var inner: FallbackDropForecastStore,
    var cache: TtlCache<FallbackDropForecast>,
    var hits: Int,
    var misses: Int,
) : FallbackDropForecastStore {
    override fun find(code: String): FallbackDropForecast? {
        val hit = (cache.get(code) as FallbackDropForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDropForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDropForecast> = inner.list()
}

fun newCachedFallbackDropForecastStore(inner: FallbackDropForecastStore, ttl: Int, clock: Clock): FallbackDropForecastStore {
    return CachedFallbackDropForecastStore(
        inner = inner,
        cache = newTtlCache<FallbackDropForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback drop forecasts. Empty fields match all. */
data class FallbackDropForecastQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDropForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDropForecast>): MutableList<FallbackDropForecast> {
        val matched = filter(items) { candidate: FallbackDropForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDropForecasts(store: FallbackDropForecastStore, query: FallbackDropForecastQuery): MutableList<FallbackDropForecast> {
    return query.apply(store.list())
}
