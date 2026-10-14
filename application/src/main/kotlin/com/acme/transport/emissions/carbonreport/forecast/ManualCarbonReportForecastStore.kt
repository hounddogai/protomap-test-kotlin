package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual carbon report forecasts by code. */
interface ManualCarbonReportForecastStore {
    fun find(code: String): ManualCarbonReportForecast?
    fun save(item: ManualCarbonReportForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCarbonReportForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCarbonReportForecastStore(
    var items: MutableMap<String, ManualCarbonReportForecast>,
    var order: MutableList<String>,
) : ManualCarbonReportForecastStore {
    override fun find(code: String): ManualCarbonReportForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCarbonReportForecast) {
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

    override fun list(): MutableList<ManualCarbonReportForecast> {
        val out = mutableListOf<ManualCarbonReportForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCarbonReportForecastStore(): ManualCarbonReportForecastStore {
    return MemoryManualCarbonReportForecastStore(
        mutableMapOf<String, ManualCarbonReportForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCarbonReportForecastStore(
    var inner: ManualCarbonReportForecastStore,
    var cache: TtlCache<ManualCarbonReportForecast>,
    var hits: Int,
    var misses: Int,
) : ManualCarbonReportForecastStore {
    override fun find(code: String): ManualCarbonReportForecast? {
        val hit = (cache.get(code) as ManualCarbonReportForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCarbonReportForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCarbonReportForecast> = inner.list()
}

fun newCachedManualCarbonReportForecastStore(inner: ManualCarbonReportForecastStore, ttl: Int, clock: Clock): ManualCarbonReportForecastStore {
    return CachedManualCarbonReportForecastStore(
        inner = inner,
        cache = newTtlCache<ManualCarbonReportForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual carbon report forecasts. Empty fields match all. */
data class ManualCarbonReportForecastQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCarbonReportForecast): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCarbonReportForecast>): MutableList<ManualCarbonReportForecast> {
        val matched = filter(items) { candidate: ManualCarbonReportForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCarbonReportForecasts(store: ManualCarbonReportForecastStore, query: ManualCarbonReportForecastQuery): MutableList<ManualCarbonReportForecast> {
    return query.apply(store.list())
}
