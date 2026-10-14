package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return forecasts by code. */
interface LeaseReturnForecastStore {
    fun find(code: String): LeaseReturnForecast?
    fun save(item: LeaseReturnForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnForecastStore(
    var items: MutableMap<String, LeaseReturnForecast>,
    var order: MutableList<String>,
) : LeaseReturnForecastStore {
    override fun find(code: String): LeaseReturnForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnForecast) {
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

    override fun list(): MutableList<LeaseReturnForecast> {
        val out = mutableListOf<LeaseReturnForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnForecastStore(): LeaseReturnForecastStore {
    return MemoryLeaseReturnForecastStore(mutableMapOf<String, LeaseReturnForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnForecastStore(
    var inner: LeaseReturnForecastStore,
    var cache: TtlCache<LeaseReturnForecast>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnForecastStore {
    override fun find(code: String): LeaseReturnForecast? {
        val hit = (cache.get(code) as LeaseReturnForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnForecast> = inner.list()
}

fun newCachedLeaseReturnForecastStore(inner: LeaseReturnForecastStore, ttl: Int, clock: Clock): LeaseReturnForecastStore {
    return CachedLeaseReturnForecastStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return forecasts. Empty fields match all. */
data class LeaseReturnForecastQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnForecast>): MutableList<LeaseReturnForecast> {
        val matched = filter(items) { candidate: LeaseReturnForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnForecasts(store: LeaseReturnForecastStore, query: LeaseReturnForecastQuery): MutableList<LeaseReturnForecast> {
    return query.apply(store.list())
}
