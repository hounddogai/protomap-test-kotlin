package com.acme.transport.emissions.idling.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling forecasts by code. */
interface IdlingForecastStore {
    fun find(code: String): IdlingForecast?
    fun save(item: IdlingForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingForecastStore(
    var items: MutableMap<String, IdlingForecast>,
    var order: MutableList<String>,
) : IdlingForecastStore {
    override fun find(code: String): IdlingForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingForecast) {
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

    override fun list(): MutableList<IdlingForecast> {
        val out = mutableListOf<IdlingForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingForecastStore(): IdlingForecastStore {
    return MemoryIdlingForecastStore(mutableMapOf<String, IdlingForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingForecastStore(
    var inner: IdlingForecastStore,
    var cache: TtlCache<IdlingForecast>,
    var hits: Int,
    var misses: Int,
) : IdlingForecastStore {
    override fun find(code: String): IdlingForecast? {
        val hit = (cache.get(code) as IdlingForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingForecast> = inner.list()
}

fun newCachedIdlingForecastStore(inner: IdlingForecastStore, ttl: Int, clock: Clock): IdlingForecastStore {
    return CachedIdlingForecastStore(
        inner = inner,
        cache = newTtlCache<IdlingForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling forecasts. Empty fields match all. */
data class IdlingForecastQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingForecast): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingForecast>): MutableList<IdlingForecast> {
        val matched = filter(items) { candidate: IdlingForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingForecasts(store: IdlingForecastStore, query: IdlingForecastQuery): MutableList<IdlingForecast> {
    return query.apply(store.list())
}
