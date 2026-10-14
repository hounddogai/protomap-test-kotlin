package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time forecasts by code. */
interface TransitTimeForecastStore {
    fun find(code: String): TransitTimeForecast?
    fun save(item: TransitTimeForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeForecastStore(
    var items: MutableMap<String, TransitTimeForecast>,
    var order: MutableList<String>,
) : TransitTimeForecastStore {
    override fun find(code: String): TransitTimeForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeForecast) {
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

    override fun list(): MutableList<TransitTimeForecast> {
        val out = mutableListOf<TransitTimeForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeForecastStore(): TransitTimeForecastStore {
    return MemoryTransitTimeForecastStore(mutableMapOf<String, TransitTimeForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeForecastStore(
    var inner: TransitTimeForecastStore,
    var cache: TtlCache<TransitTimeForecast>,
    var hits: Int,
    var misses: Int,
) : TransitTimeForecastStore {
    override fun find(code: String): TransitTimeForecast? {
        val hit = (cache.get(code) as TransitTimeForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeForecast> = inner.list()
}

fun newCachedTransitTimeForecastStore(inner: TransitTimeForecastStore, ttl: Int, clock: Clock): TransitTimeForecastStore {
    return CachedTransitTimeForecastStore(
        inner = inner,
        cache = newTtlCache<TransitTimeForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time forecasts. Empty fields match all. */
data class TransitTimeForecastQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeForecast>): MutableList<TransitTimeForecast> {
        val matched = filter(items) { candidate: TransitTimeForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeForecasts(store: TransitTimeForecastStore, query: TransitTimeForecastQuery): MutableList<TransitTimeForecast> {
    return query.apply(store.list())
}
