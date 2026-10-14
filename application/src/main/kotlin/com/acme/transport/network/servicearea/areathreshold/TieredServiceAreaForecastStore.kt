package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered service area forecasts by code. */
interface TieredServiceAreaForecastStore {
    fun find(code: String): TieredServiceAreaForecast?
    fun save(item: TieredServiceAreaForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredServiceAreaForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredServiceAreaForecastStore(
    var items: MutableMap<String, TieredServiceAreaForecast>,
    var order: MutableList<String>,
) : TieredServiceAreaForecastStore {
    override fun find(code: String): TieredServiceAreaForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredServiceAreaForecast) {
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

    override fun list(): MutableList<TieredServiceAreaForecast> {
        val out = mutableListOf<TieredServiceAreaForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredServiceAreaForecastStore(): TieredServiceAreaForecastStore {
    return MemoryTieredServiceAreaForecastStore(
        mutableMapOf<String, TieredServiceAreaForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredServiceAreaForecastStore(
    var inner: TieredServiceAreaForecastStore,
    var cache: TtlCache<TieredServiceAreaForecast>,
    var hits: Int,
    var misses: Int,
) : TieredServiceAreaForecastStore {
    override fun find(code: String): TieredServiceAreaForecast? {
        val hit = (cache.get(code) as TieredServiceAreaForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredServiceAreaForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredServiceAreaForecast> = inner.list()
}

fun newCachedTieredServiceAreaForecastStore(inner: TieredServiceAreaForecastStore, ttl: Int, clock: Clock): TieredServiceAreaForecastStore {
    return CachedTieredServiceAreaForecastStore(
        inner = inner,
        cache = newTtlCache<TieredServiceAreaForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered service area forecasts. Empty fields match all. */
data class TieredServiceAreaForecastQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredServiceAreaForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredServiceAreaForecast>): MutableList<TieredServiceAreaForecast> {
        val matched = filter(items) { candidate: TieredServiceAreaForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredServiceAreaForecasts(store: TieredServiceAreaForecastStore, query: TieredServiceAreaForecastQuery): MutableList<TieredServiceAreaForecast> {
    return query.apply(store.list())
}
