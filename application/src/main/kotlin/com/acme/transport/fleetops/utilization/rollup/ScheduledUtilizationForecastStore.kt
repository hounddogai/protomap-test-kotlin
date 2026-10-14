package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled utilization forecasts by code. */
interface ScheduledUtilizationForecastStore {
    fun find(code: String): ScheduledUtilizationForecast?
    fun save(item: ScheduledUtilizationForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledUtilizationForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledUtilizationForecastStore(
    var items: MutableMap<String, ScheduledUtilizationForecast>,
    var order: MutableList<String>,
) : ScheduledUtilizationForecastStore {
    override fun find(code: String): ScheduledUtilizationForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledUtilizationForecast) {
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

    override fun list(): MutableList<ScheduledUtilizationForecast> {
        val out = mutableListOf<ScheduledUtilizationForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledUtilizationForecastStore(): ScheduledUtilizationForecastStore {
    return MemoryScheduledUtilizationForecastStore(
        mutableMapOf<String, ScheduledUtilizationForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledUtilizationForecastStore(
    var inner: ScheduledUtilizationForecastStore,
    var cache: TtlCache<ScheduledUtilizationForecast>,
    var hits: Int,
    var misses: Int,
) : ScheduledUtilizationForecastStore {
    override fun find(code: String): ScheduledUtilizationForecast? {
        val hit = (cache.get(code) as ScheduledUtilizationForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledUtilizationForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledUtilizationForecast> = inner.list()
}

fun newCachedScheduledUtilizationForecastStore(inner: ScheduledUtilizationForecastStore, ttl: Int, clock: Clock): ScheduledUtilizationForecastStore {
    return CachedScheduledUtilizationForecastStore(
        inner = inner,
        cache = newTtlCache<ScheduledUtilizationForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled utilization forecasts. Empty fields match all. */
data class ScheduledUtilizationForecastQuery(
    var region: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledUtilizationForecast): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<ScheduledUtilizationForecast>): MutableList<ScheduledUtilizationForecast> {
        val matched = filter(items) { candidate: ScheduledUtilizationForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledUtilizationForecasts(store: ScheduledUtilizationForecastStore, query: ScheduledUtilizationForecastQuery): MutableList<ScheduledUtilizationForecast> {
    return query.apply(store.list())
}
