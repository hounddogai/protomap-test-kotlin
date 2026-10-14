package com.acme.transport.fleetops.utilization.utilizationtarget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization forecasts by code. */
interface UtilizationForecastStore {
    fun find(code: String): UtilizationForecast?
    fun save(item: UtilizationForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationForecastStore(
    var items: MutableMap<String, UtilizationForecast>,
    var order: MutableList<String>,
) : UtilizationForecastStore {
    override fun find(code: String): UtilizationForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationForecast) {
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

    override fun list(): MutableList<UtilizationForecast> {
        val out = mutableListOf<UtilizationForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationForecastStore(): UtilizationForecastStore {
    return MemoryUtilizationForecastStore(mutableMapOf<String, UtilizationForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationForecastStore(
    var inner: UtilizationForecastStore,
    var cache: TtlCache<UtilizationForecast>,
    var hits: Int,
    var misses: Int,
) : UtilizationForecastStore {
    override fun find(code: String): UtilizationForecast? {
        val hit = (cache.get(code) as UtilizationForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationForecast> = inner.list()
}

fun newCachedUtilizationForecastStore(inner: UtilizationForecastStore, ttl: Int, clock: Clock): UtilizationForecastStore {
    return CachedUtilizationForecastStore(
        inner = inner,
        cache = newTtlCache<UtilizationForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization forecasts. Empty fields match all. */
data class UtilizationForecastQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationForecast>): MutableList<UtilizationForecast> {
        val matched = filter(items) { candidate: UtilizationForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationForecasts(store: UtilizationForecastStore, query: UtilizationForecastQuery): MutableList<UtilizationForecast> {
    return query.apply(store.list())
}
