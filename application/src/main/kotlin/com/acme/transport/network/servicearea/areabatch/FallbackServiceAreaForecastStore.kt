package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback service area forecasts by code. */
interface FallbackServiceAreaForecastStore {
    fun find(code: String): FallbackServiceAreaForecast?
    fun save(item: FallbackServiceAreaForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackServiceAreaForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackServiceAreaForecastStore(
    var items: MutableMap<String, FallbackServiceAreaForecast>,
    var order: MutableList<String>,
) : FallbackServiceAreaForecastStore {
    override fun find(code: String): FallbackServiceAreaForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackServiceAreaForecast) {
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

    override fun list(): MutableList<FallbackServiceAreaForecast> {
        val out = mutableListOf<FallbackServiceAreaForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackServiceAreaForecastStore(): FallbackServiceAreaForecastStore {
    return MemoryFallbackServiceAreaForecastStore(
        mutableMapOf<String, FallbackServiceAreaForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackServiceAreaForecastStore(
    var inner: FallbackServiceAreaForecastStore,
    var cache: TtlCache<FallbackServiceAreaForecast>,
    var hits: Int,
    var misses: Int,
) : FallbackServiceAreaForecastStore {
    override fun find(code: String): FallbackServiceAreaForecast? {
        val hit = (cache.get(code) as FallbackServiceAreaForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackServiceAreaForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackServiceAreaForecast> = inner.list()
}

fun newCachedFallbackServiceAreaForecastStore(inner: FallbackServiceAreaForecastStore, ttl: Int, clock: Clock): FallbackServiceAreaForecastStore {
    return CachedFallbackServiceAreaForecastStore(
        inner = inner,
        cache = newTtlCache<FallbackServiceAreaForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback service area forecasts. Empty fields match all. */
data class FallbackServiceAreaForecastQuery(
    var category: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackServiceAreaForecast): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackServiceAreaForecast>): MutableList<FallbackServiceAreaForecast> {
        val matched = filter(items) { candidate: FallbackServiceAreaForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackServiceAreaForecasts(store: FallbackServiceAreaForecastStore, query: FallbackServiceAreaForecastQuery): MutableList<FallbackServiceAreaForecast> {
    return query.apply(store.list())
}
