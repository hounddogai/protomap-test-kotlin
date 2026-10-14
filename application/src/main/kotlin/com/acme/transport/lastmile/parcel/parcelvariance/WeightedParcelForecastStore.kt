package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted parcel forecasts by code. */
interface WeightedParcelForecastStore {
    fun find(code: String): WeightedParcelForecast?
    fun save(item: WeightedParcelForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedParcelForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedParcelForecastStore(
    var items: MutableMap<String, WeightedParcelForecast>,
    var order: MutableList<String>,
) : WeightedParcelForecastStore {
    override fun find(code: String): WeightedParcelForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedParcelForecast) {
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

    override fun list(): MutableList<WeightedParcelForecast> {
        val out = mutableListOf<WeightedParcelForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedParcelForecastStore(): WeightedParcelForecastStore {
    return MemoryWeightedParcelForecastStore(
        mutableMapOf<String, WeightedParcelForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedParcelForecastStore(
    var inner: WeightedParcelForecastStore,
    var cache: TtlCache<WeightedParcelForecast>,
    var hits: Int,
    var misses: Int,
) : WeightedParcelForecastStore {
    override fun find(code: String): WeightedParcelForecast? {
        val hit = (cache.get(code) as WeightedParcelForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedParcelForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedParcelForecast> = inner.list()
}

fun newCachedWeightedParcelForecastStore(inner: WeightedParcelForecastStore, ttl: Int, clock: Clock): WeightedParcelForecastStore {
    return CachedWeightedParcelForecastStore(
        inner = inner,
        cache = newTtlCache<WeightedParcelForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted parcel forecasts. Empty fields match all. */
data class WeightedParcelForecastQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedParcelForecast): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<WeightedParcelForecast>): MutableList<WeightedParcelForecast> {
        val matched = filter(items) { candidate: WeightedParcelForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedParcelForecasts(store: WeightedParcelForecastStore, query: WeightedParcelForecastQuery): MutableList<WeightedParcelForecast> {
    return query.apply(store.list())
}
