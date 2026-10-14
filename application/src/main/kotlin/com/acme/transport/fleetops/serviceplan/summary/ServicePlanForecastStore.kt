package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan forecasts by code. */
interface ServicePlanForecastStore {
    fun find(code: String): ServicePlanForecast?
    fun save(item: ServicePlanForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanForecastStore(
    var items: MutableMap<String, ServicePlanForecast>,
    var order: MutableList<String>,
) : ServicePlanForecastStore {
    override fun find(code: String): ServicePlanForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanForecast) {
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

    override fun list(): MutableList<ServicePlanForecast> {
        val out = mutableListOf<ServicePlanForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanForecastStore(): ServicePlanForecastStore {
    return MemoryServicePlanForecastStore(mutableMapOf<String, ServicePlanForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanForecastStore(
    var inner: ServicePlanForecastStore,
    var cache: TtlCache<ServicePlanForecast>,
    var hits: Int,
    var misses: Int,
) : ServicePlanForecastStore {
    override fun find(code: String): ServicePlanForecast? {
        val hit = (cache.get(code) as ServicePlanForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanForecast> = inner.list()
}

fun newCachedServicePlanForecastStore(inner: ServicePlanForecastStore, ttl: Int, clock: Clock): ServicePlanForecastStore {
    return CachedServicePlanForecastStore(
        inner = inner,
        cache = newTtlCache<ServicePlanForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan forecasts. Empty fields match all. */
data class ServicePlanForecastQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanForecast): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanForecast>): MutableList<ServicePlanForecast> {
        val matched = filter(items) { candidate: ServicePlanForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanForecasts(store: ServicePlanForecastStore, query: ServicePlanForecastQuery): MutableList<ServicePlanForecast> {
    return query.apply(store.list())
}
