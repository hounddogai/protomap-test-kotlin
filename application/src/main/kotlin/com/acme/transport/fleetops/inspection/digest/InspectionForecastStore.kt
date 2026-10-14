package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection forecasts by code. */
interface InspectionForecastStore {
    fun find(code: String): InspectionForecast?
    fun save(item: InspectionForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionForecastStore(
    var items: MutableMap<String, InspectionForecast>,
    var order: MutableList<String>,
) : InspectionForecastStore {
    override fun find(code: String): InspectionForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionForecast) {
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

    override fun list(): MutableList<InspectionForecast> {
        val out = mutableListOf<InspectionForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionForecastStore(): InspectionForecastStore {
    return MemoryInspectionForecastStore(mutableMapOf<String, InspectionForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionForecastStore(
    var inner: InspectionForecastStore,
    var cache: TtlCache<InspectionForecast>,
    var hits: Int,
    var misses: Int,
) : InspectionForecastStore {
    override fun find(code: String): InspectionForecast? {
        val hit = (cache.get(code) as InspectionForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionForecast> = inner.list()
}

fun newCachedInspectionForecastStore(inner: InspectionForecastStore, ttl: Int, clock: Clock): InspectionForecastStore {
    return CachedInspectionForecastStore(
        inner = inner,
        cache = newTtlCache<InspectionForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection forecasts. Empty fields match all. */
data class InspectionForecastQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionForecast>): MutableList<InspectionForecast> {
        val matched = filter(items) { candidate: InspectionForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionForecasts(store: InspectionForecastStore, query: InspectionForecastQuery): MutableList<InspectionForecast> {
    return query.apply(store.list())
}
