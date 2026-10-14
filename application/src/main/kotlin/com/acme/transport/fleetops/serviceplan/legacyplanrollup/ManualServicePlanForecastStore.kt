package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual service plan forecasts by code. */
interface ManualServicePlanForecastStore {
    fun find(code: String): ManualServicePlanForecast?
    fun save(item: ManualServicePlanForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualServicePlanForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualServicePlanForecastStore(
    var items: MutableMap<String, ManualServicePlanForecast>,
    var order: MutableList<String>,
) : ManualServicePlanForecastStore {
    override fun find(code: String): ManualServicePlanForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualServicePlanForecast) {
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

    override fun list(): MutableList<ManualServicePlanForecast> {
        val out = mutableListOf<ManualServicePlanForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualServicePlanForecastStore(): ManualServicePlanForecastStore {
    return MemoryManualServicePlanForecastStore(
        mutableMapOf<String, ManualServicePlanForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualServicePlanForecastStore(
    var inner: ManualServicePlanForecastStore,
    var cache: TtlCache<ManualServicePlanForecast>,
    var hits: Int,
    var misses: Int,
) : ManualServicePlanForecastStore {
    override fun find(code: String): ManualServicePlanForecast? {
        val hit = (cache.get(code) as ManualServicePlanForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualServicePlanForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualServicePlanForecast> = inner.list()
}

fun newCachedManualServicePlanForecastStore(inner: ManualServicePlanForecastStore, ttl: Int, clock: Clock): ManualServicePlanForecastStore {
    return CachedManualServicePlanForecastStore(
        inner = inner,
        cache = newTtlCache<ManualServicePlanForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual service plan forecasts. Empty fields match all. */
data class ManualServicePlanForecastQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualServicePlanForecast): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualServicePlanForecast>): MutableList<ManualServicePlanForecast> {
        val matched = filter(items) { candidate: ManualServicePlanForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualServicePlanForecasts(store: ManualServicePlanForecastStore, query: ManualServicePlanForecastQuery): MutableList<ManualServicePlanForecast> {
    return query.apply(store.list())
}
