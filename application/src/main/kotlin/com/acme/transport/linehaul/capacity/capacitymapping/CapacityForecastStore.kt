package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity forecasts by code. */
interface CapacityForecastStore {
    fun find(code: String): CapacityForecast?
    fun save(item: CapacityForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityForecastStore(
    var items: MutableMap<String, CapacityForecast>,
    var order: MutableList<String>,
) : CapacityForecastStore {
    override fun find(code: String): CapacityForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityForecast) {
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

    override fun list(): MutableList<CapacityForecast> {
        val out = mutableListOf<CapacityForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityForecastStore(): CapacityForecastStore {
    return MemoryCapacityForecastStore(mutableMapOf<String, CapacityForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityForecastStore(
    var inner: CapacityForecastStore,
    var cache: TtlCache<CapacityForecast>,
    var hits: Int,
    var misses: Int,
) : CapacityForecastStore {
    override fun find(code: String): CapacityForecast? {
        val hit = (cache.get(code) as CapacityForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityForecast> = inner.list()
}

fun newCachedCapacityForecastStore(inner: CapacityForecastStore, ttl: Int, clock: Clock): CapacityForecastStore {
    return CachedCapacityForecastStore(
        inner = inner,
        cache = newTtlCache<CapacityForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity forecasts. Empty fields match all. */
data class CapacityForecastQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityForecast): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityForecast>): MutableList<CapacityForecast> {
        val matched = filter(items) { candidate: CapacityForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityForecasts(store: CapacityForecastStore, query: CapacityForecastQuery): MutableList<CapacityForecast> {
    return query.apply(store.list())
}
