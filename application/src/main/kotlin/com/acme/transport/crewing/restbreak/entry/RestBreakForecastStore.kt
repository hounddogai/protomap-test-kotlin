package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break forecasts by code. */
interface RestBreakForecastStore {
    fun find(code: String): RestBreakForecast?
    fun save(item: RestBreakForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakForecastStore(
    var items: MutableMap<String, RestBreakForecast>,
    var order: MutableList<String>,
) : RestBreakForecastStore {
    override fun find(code: String): RestBreakForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakForecast) {
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

    override fun list(): MutableList<RestBreakForecast> {
        val out = mutableListOf<RestBreakForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakForecastStore(): RestBreakForecastStore {
    return MemoryRestBreakForecastStore(mutableMapOf<String, RestBreakForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakForecastStore(
    var inner: RestBreakForecastStore,
    var cache: TtlCache<RestBreakForecast>,
    var hits: Int,
    var misses: Int,
) : RestBreakForecastStore {
    override fun find(code: String): RestBreakForecast? {
        val hit = (cache.get(code) as RestBreakForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakForecast> = inner.list()
}

fun newCachedRestBreakForecastStore(inner: RestBreakForecastStore, ttl: Int, clock: Clock): RestBreakForecastStore {
    return CachedRestBreakForecastStore(
        inner = inner,
        cache = newTtlCache<RestBreakForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break forecasts. Empty fields match all. */
data class RestBreakForecastQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RestBreakForecast>): MutableList<RestBreakForecast> {
        val matched = filter(items) { candidate: RestBreakForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakForecasts(store: RestBreakForecastStore, query: RestBreakForecastQuery): MutableList<RestBreakForecast> {
    return query.apply(store.list())
}
