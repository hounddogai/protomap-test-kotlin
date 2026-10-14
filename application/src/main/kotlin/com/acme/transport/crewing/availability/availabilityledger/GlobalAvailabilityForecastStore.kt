package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global availability forecasts by code. */
interface GlobalAvailabilityForecastStore {
    fun find(code: String): GlobalAvailabilityForecast?
    fun save(item: GlobalAvailabilityForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAvailabilityForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAvailabilityForecastStore(
    var items: MutableMap<String, GlobalAvailabilityForecast>,
    var order: MutableList<String>,
) : GlobalAvailabilityForecastStore {
    override fun find(code: String): GlobalAvailabilityForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAvailabilityForecast) {
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

    override fun list(): MutableList<GlobalAvailabilityForecast> {
        val out = mutableListOf<GlobalAvailabilityForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAvailabilityForecastStore(): GlobalAvailabilityForecastStore {
    return MemoryGlobalAvailabilityForecastStore(
        mutableMapOf<String, GlobalAvailabilityForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAvailabilityForecastStore(
    var inner: GlobalAvailabilityForecastStore,
    var cache: TtlCache<GlobalAvailabilityForecast>,
    var hits: Int,
    var misses: Int,
) : GlobalAvailabilityForecastStore {
    override fun find(code: String): GlobalAvailabilityForecast? {
        val hit = (cache.get(code) as GlobalAvailabilityForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAvailabilityForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAvailabilityForecast> = inner.list()
}

fun newCachedGlobalAvailabilityForecastStore(inner: GlobalAvailabilityForecastStore, ttl: Int, clock: Clock): GlobalAvailabilityForecastStore {
    return CachedGlobalAvailabilityForecastStore(
        inner = inner,
        cache = newTtlCache<GlobalAvailabilityForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global availability forecasts. Empty fields match all. */
data class GlobalAvailabilityForecastQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAvailabilityForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalAvailabilityForecast>): MutableList<GlobalAvailabilityForecast> {
        val matched = filter(items) { candidate: GlobalAvailabilityForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAvailabilityForecasts(store: GlobalAvailabilityForecastStore, query: GlobalAvailabilityForecastQuery): MutableList<GlobalAvailabilityForecast> {
    return query.apply(store.list())
}
