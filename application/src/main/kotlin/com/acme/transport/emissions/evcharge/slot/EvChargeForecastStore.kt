package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge forecasts by code. */
interface EvChargeForecastStore {
    fun find(code: String): EvChargeForecast?
    fun save(item: EvChargeForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeForecastStore(
    var items: MutableMap<String, EvChargeForecast>,
    var order: MutableList<String>,
) : EvChargeForecastStore {
    override fun find(code: String): EvChargeForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeForecast) {
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

    override fun list(): MutableList<EvChargeForecast> {
        val out = mutableListOf<EvChargeForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeForecastStore(): EvChargeForecastStore {
    return MemoryEvChargeForecastStore(mutableMapOf<String, EvChargeForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeForecastStore(
    var inner: EvChargeForecastStore,
    var cache: TtlCache<EvChargeForecast>,
    var hits: Int,
    var misses: Int,
) : EvChargeForecastStore {
    override fun find(code: String): EvChargeForecast? {
        val hit = (cache.get(code) as EvChargeForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeForecast> = inner.list()
}

fun newCachedEvChargeForecastStore(inner: EvChargeForecastStore, ttl: Int, clock: Clock): EvChargeForecastStore {
    return CachedEvChargeForecastStore(
        inner = inner,
        cache = newTtlCache<EvChargeForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge forecasts. Empty fields match all. */
data class EvChargeForecastQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeForecast): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeForecast>): MutableList<EvChargeForecast> {
        val matched = filter(items) { candidate: EvChargeForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeForecasts(store: EvChargeForecastStore, query: EvChargeForecastQuery): MutableList<EvChargeForecast> {
    return query.apply(store.list())
}
