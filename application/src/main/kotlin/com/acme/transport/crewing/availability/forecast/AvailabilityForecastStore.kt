package com.acme.transport.crewing.availability.forecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability forecasts by code. */
interface AvailabilityForecastStore {
    fun find(code: String): AvailabilityForecast?
    fun save(item: AvailabilityForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityForecastStore(
    var items: MutableMap<String, AvailabilityForecast>,
    var order: MutableList<String>,
) : AvailabilityForecastStore {
    override fun find(code: String): AvailabilityForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityForecast) {
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

    override fun list(): MutableList<AvailabilityForecast> {
        val out = mutableListOf<AvailabilityForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityForecastStore(): AvailabilityForecastStore {
    return MemoryAvailabilityForecastStore(
        mutableMapOf<String, AvailabilityForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityForecastStore(
    var inner: AvailabilityForecastStore,
    var cache: TtlCache<AvailabilityForecast>,
    var hits: Int,
    var misses: Int,
) : AvailabilityForecastStore {
    override fun find(code: String): AvailabilityForecast? {
        val hit = (cache.get(code) as AvailabilityForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityForecast> = inner.list()
}

fun newCachedAvailabilityForecastStore(inner: AvailabilityForecastStore, ttl: Int, clock: Clock): AvailabilityForecastStore {
    return CachedAvailabilityForecastStore(
        inner = inner,
        cache = newTtlCache<AvailabilityForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability forecasts. Empty fields match all. */
data class AvailabilityForecastQuery(
    var locale: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityForecast): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityForecast>): MutableList<AvailabilityForecast> {
        val matched = filter(items) { candidate: AvailabilityForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityForecasts(store: AvailabilityForecastStore, query: AvailabilityForecastQuery): MutableList<AvailabilityForecast> {
    return query.apply(store.list())
}
