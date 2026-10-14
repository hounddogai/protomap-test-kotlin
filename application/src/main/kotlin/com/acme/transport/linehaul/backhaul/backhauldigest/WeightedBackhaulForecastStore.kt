package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted backhaul forecasts by code. */
interface WeightedBackhaulForecastStore {
    fun find(code: String): WeightedBackhaulForecast?
    fun save(item: WeightedBackhaulForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedBackhaulForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedBackhaulForecastStore(
    var items: MutableMap<String, WeightedBackhaulForecast>,
    var order: MutableList<String>,
) : WeightedBackhaulForecastStore {
    override fun find(code: String): WeightedBackhaulForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedBackhaulForecast) {
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

    override fun list(): MutableList<WeightedBackhaulForecast> {
        val out = mutableListOf<WeightedBackhaulForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedBackhaulForecastStore(): WeightedBackhaulForecastStore {
    return MemoryWeightedBackhaulForecastStore(
        mutableMapOf<String, WeightedBackhaulForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedBackhaulForecastStore(
    var inner: WeightedBackhaulForecastStore,
    var cache: TtlCache<WeightedBackhaulForecast>,
    var hits: Int,
    var misses: Int,
) : WeightedBackhaulForecastStore {
    override fun find(code: String): WeightedBackhaulForecast? {
        val hit = (cache.get(code) as WeightedBackhaulForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedBackhaulForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedBackhaulForecast> = inner.list()
}

fun newCachedWeightedBackhaulForecastStore(inner: WeightedBackhaulForecastStore, ttl: Int, clock: Clock): WeightedBackhaulForecastStore {
    return CachedWeightedBackhaulForecastStore(
        inner = inner,
        cache = newTtlCache<WeightedBackhaulForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted backhaul forecasts. Empty fields match all. */
data class WeightedBackhaulForecastQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedBackhaulForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedBackhaulForecast>): MutableList<WeightedBackhaulForecast> {
        val matched = filter(items) { candidate: WeightedBackhaulForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedBackhaulForecasts(store: WeightedBackhaulForecastStore, query: WeightedBackhaulForecastQuery): MutableList<WeightedBackhaulForecast> {
    return query.apply(store.list())
}
