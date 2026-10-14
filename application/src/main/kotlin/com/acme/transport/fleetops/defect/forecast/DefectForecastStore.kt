package com.acme.transport.fleetops.defect.forecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect forecasts by code. */
interface DefectForecastStore {
    fun find(code: String): DefectForecast?
    fun save(item: DefectForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectForecastStore(
    var items: MutableMap<String, DefectForecast>,
    var order: MutableList<String>,
) : DefectForecastStore {
    override fun find(code: String): DefectForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectForecast) {
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

    override fun list(): MutableList<DefectForecast> {
        val out = mutableListOf<DefectForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectForecastStore(): DefectForecastStore {
    return MemoryDefectForecastStore(mutableMapOf<String, DefectForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectForecastStore(
    var inner: DefectForecastStore,
    var cache: TtlCache<DefectForecast>,
    var hits: Int,
    var misses: Int,
) : DefectForecastStore {
    override fun find(code: String): DefectForecast? {
        val hit = (cache.get(code) as DefectForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectForecast> = inner.list()
}

fun newCachedDefectForecastStore(inner: DefectForecastStore, ttl: Int, clock: Clock): DefectForecastStore {
    return CachedDefectForecastStore(
        inner = inner,
        cache = newTtlCache<DefectForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect forecasts. Empty fields match all. */
data class DefectForecastQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectForecast>): MutableList<DefectForecast> {
        val matched = filter(items) { candidate: DefectForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectForecasts(store: DefectForecastStore, query: DefectForecastQuery): MutableList<DefectForecast> {
    return query.apply(store.list())
}
