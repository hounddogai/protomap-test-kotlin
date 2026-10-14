package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted camera event forecasts by code. */
interface WeightedCameraEventForecastStore {
    fun find(code: String): WeightedCameraEventForecast?
    fun save(item: WeightedCameraEventForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCameraEventForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCameraEventForecastStore(
    var items: MutableMap<String, WeightedCameraEventForecast>,
    var order: MutableList<String>,
) : WeightedCameraEventForecastStore {
    override fun find(code: String): WeightedCameraEventForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCameraEventForecast) {
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

    override fun list(): MutableList<WeightedCameraEventForecast> {
        val out = mutableListOf<WeightedCameraEventForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCameraEventForecastStore(): WeightedCameraEventForecastStore {
    return MemoryWeightedCameraEventForecastStore(
        mutableMapOf<String, WeightedCameraEventForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCameraEventForecastStore(
    var inner: WeightedCameraEventForecastStore,
    var cache: TtlCache<WeightedCameraEventForecast>,
    var hits: Int,
    var misses: Int,
) : WeightedCameraEventForecastStore {
    override fun find(code: String): WeightedCameraEventForecast? {
        val hit = (cache.get(code) as WeightedCameraEventForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCameraEventForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCameraEventForecast> = inner.list()
}

fun newCachedWeightedCameraEventForecastStore(inner: WeightedCameraEventForecastStore, ttl: Int, clock: Clock): WeightedCameraEventForecastStore {
    return CachedWeightedCameraEventForecastStore(
        inner = inner,
        cache = newTtlCache<WeightedCameraEventForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted camera event forecasts. Empty fields match all. */
data class WeightedCameraEventForecastQuery(
    var title: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCameraEventForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCameraEventForecast>): MutableList<WeightedCameraEventForecast> {
        val matched = filter(items) { candidate: WeightedCameraEventForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCameraEventForecasts(store: WeightedCameraEventForecastStore, query: WeightedCameraEventForecastQuery): MutableList<WeightedCameraEventForecast> {
    return query.apply(store.list())
}
