package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy service plan forecasts by code. */
interface LegacyServicePlanForecastStore {
    fun find(code: String): LegacyServicePlanForecast?
    fun save(item: LegacyServicePlanForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyServicePlanForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyServicePlanForecastStore(
    var items: MutableMap<String, LegacyServicePlanForecast>,
    var order: MutableList<String>,
) : LegacyServicePlanForecastStore {
    override fun find(code: String): LegacyServicePlanForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyServicePlanForecast) {
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

    override fun list(): MutableList<LegacyServicePlanForecast> {
        val out = mutableListOf<LegacyServicePlanForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyServicePlanForecastStore(): LegacyServicePlanForecastStore {
    return MemoryLegacyServicePlanForecastStore(
        mutableMapOf<String, LegacyServicePlanForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyServicePlanForecastStore(
    var inner: LegacyServicePlanForecastStore,
    var cache: TtlCache<LegacyServicePlanForecast>,
    var hits: Int,
    var misses: Int,
) : LegacyServicePlanForecastStore {
    override fun find(code: String): LegacyServicePlanForecast? {
        val hit = (cache.get(code) as LegacyServicePlanForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyServicePlanForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyServicePlanForecast> = inner.list()
}

fun newCachedLegacyServicePlanForecastStore(inner: LegacyServicePlanForecastStore, ttl: Int, clock: Clock): LegacyServicePlanForecastStore {
    return CachedLegacyServicePlanForecastStore(
        inner = inner,
        cache = newTtlCache<LegacyServicePlanForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy service plan forecasts. Empty fields match all. */
data class LegacyServicePlanForecastQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyServicePlanForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LegacyServicePlanForecast>): MutableList<LegacyServicePlanForecast> {
        val matched = filter(items) { candidate: LegacyServicePlanForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyServicePlanForecasts(store: LegacyServicePlanForecastStore, query: LegacyServicePlanForecastQuery): MutableList<LegacyServicePlanForecast> {
    return query.apply(store.list())
}
