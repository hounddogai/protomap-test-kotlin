package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted drop forecasts by code. */
interface WeightedDropForecastStore {
    fun find(code: String): WeightedDropForecast?
    fun save(item: WeightedDropForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDropForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDropForecastStore(
    var items: MutableMap<String, WeightedDropForecast>,
    var order: MutableList<String>,
) : WeightedDropForecastStore {
    override fun find(code: String): WeightedDropForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDropForecast) {
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

    override fun list(): MutableList<WeightedDropForecast> {
        val out = mutableListOf<WeightedDropForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDropForecastStore(): WeightedDropForecastStore {
    return MemoryWeightedDropForecastStore(
        mutableMapOf<String, WeightedDropForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDropForecastStore(
    var inner: WeightedDropForecastStore,
    var cache: TtlCache<WeightedDropForecast>,
    var hits: Int,
    var misses: Int,
) : WeightedDropForecastStore {
    override fun find(code: String): WeightedDropForecast? {
        val hit = (cache.get(code) as WeightedDropForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDropForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDropForecast> = inner.list()
}

fun newCachedWeightedDropForecastStore(inner: WeightedDropForecastStore, ttl: Int, clock: Clock): WeightedDropForecastStore {
    return CachedWeightedDropForecastStore(
        inner = inner,
        cache = newTtlCache<WeightedDropForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted drop forecasts. Empty fields match all. */
data class WeightedDropForecastQuery(
    var depot: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDropForecast): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDropForecast>): MutableList<WeightedDropForecast> {
        val matched = filter(items) { candidate: WeightedDropForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDropForecasts(store: WeightedDropForecastStore, query: WeightedDropForecastQuery): MutableList<WeightedDropForecast> {
    return query.apply(store.list())
}
