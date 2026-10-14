package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual inspection forecasts by code. */
interface ManualInspectionForecastStore {
    fun find(code: String): ManualInspectionForecast?
    fun save(item: ManualInspectionForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualInspectionForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualInspectionForecastStore(
    var items: MutableMap<String, ManualInspectionForecast>,
    var order: MutableList<String>,
) : ManualInspectionForecastStore {
    override fun find(code: String): ManualInspectionForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualInspectionForecast) {
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

    override fun list(): MutableList<ManualInspectionForecast> {
        val out = mutableListOf<ManualInspectionForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualInspectionForecastStore(): ManualInspectionForecastStore {
    return MemoryManualInspectionForecastStore(
        mutableMapOf<String, ManualInspectionForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualInspectionForecastStore(
    var inner: ManualInspectionForecastStore,
    var cache: TtlCache<ManualInspectionForecast>,
    var hits: Int,
    var misses: Int,
) : ManualInspectionForecastStore {
    override fun find(code: String): ManualInspectionForecast? {
        val hit = (cache.get(code) as ManualInspectionForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualInspectionForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualInspectionForecast> = inner.list()
}

fun newCachedManualInspectionForecastStore(inner: ManualInspectionForecastStore, ttl: Int, clock: Clock): ManualInspectionForecastStore {
    return CachedManualInspectionForecastStore(
        inner = inner,
        cache = newTtlCache<ManualInspectionForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual inspection forecasts. Empty fields match all. */
data class ManualInspectionForecastQuery(
    var lane: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualInspectionForecast): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualInspectionForecast>): MutableList<ManualInspectionForecast> {
        val matched = filter(items) { candidate: ManualInspectionForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualInspectionForecasts(store: ManualInspectionForecastStore, query: ManualInspectionForecastQuery): MutableList<ManualInspectionForecast> {
    return query.apply(store.list())
}
