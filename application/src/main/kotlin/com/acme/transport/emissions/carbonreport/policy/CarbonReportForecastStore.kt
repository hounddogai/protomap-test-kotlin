package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report forecasts by code. */
interface CarbonReportForecastStore {
    fun find(code: String): CarbonReportForecast?
    fun save(item: CarbonReportForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportForecastStore(
    var items: MutableMap<String, CarbonReportForecast>,
    var order: MutableList<String>,
) : CarbonReportForecastStore {
    override fun find(code: String): CarbonReportForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportForecast) {
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

    override fun list(): MutableList<CarbonReportForecast> {
        val out = mutableListOf<CarbonReportForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportForecastStore(): CarbonReportForecastStore {
    return MemoryCarbonReportForecastStore(
        mutableMapOf<String, CarbonReportForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportForecastStore(
    var inner: CarbonReportForecastStore,
    var cache: TtlCache<CarbonReportForecast>,
    var hits: Int,
    var misses: Int,
) : CarbonReportForecastStore {
    override fun find(code: String): CarbonReportForecast? {
        val hit = (cache.get(code) as CarbonReportForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportForecast> = inner.list()
}

fun newCachedCarbonReportForecastStore(inner: CarbonReportForecastStore, ttl: Int, clock: Clock): CarbonReportForecastStore {
    return CachedCarbonReportForecastStore(
        inner = inner,
        cache = newTtlCache<CarbonReportForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report forecasts. Empty fields match all. */
data class CarbonReportForecastQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportForecast): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportForecast>): MutableList<CarbonReportForecast> {
        val matched = filter(items) { candidate: CarbonReportForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportForecasts(store: CarbonReportForecastStore, query: CarbonReportForecastQuery): MutableList<CarbonReportForecast> {
    return query.apply(store.list())
}
