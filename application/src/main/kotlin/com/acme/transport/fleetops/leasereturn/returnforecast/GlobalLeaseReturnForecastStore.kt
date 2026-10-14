package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global lease return forecasts by code. */
interface GlobalLeaseReturnForecastStore {
    fun find(code: String): GlobalLeaseReturnForecast?
    fun save(item: GlobalLeaseReturnForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalLeaseReturnForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalLeaseReturnForecastStore(
    var items: MutableMap<String, GlobalLeaseReturnForecast>,
    var order: MutableList<String>,
) : GlobalLeaseReturnForecastStore {
    override fun find(code: String): GlobalLeaseReturnForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalLeaseReturnForecast) {
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

    override fun list(): MutableList<GlobalLeaseReturnForecast> {
        val out = mutableListOf<GlobalLeaseReturnForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalLeaseReturnForecastStore(): GlobalLeaseReturnForecastStore {
    return MemoryGlobalLeaseReturnForecastStore(
        mutableMapOf<String, GlobalLeaseReturnForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalLeaseReturnForecastStore(
    var inner: GlobalLeaseReturnForecastStore,
    var cache: TtlCache<GlobalLeaseReturnForecast>,
    var hits: Int,
    var misses: Int,
) : GlobalLeaseReturnForecastStore {
    override fun find(code: String): GlobalLeaseReturnForecast? {
        val hit = (cache.get(code) as GlobalLeaseReturnForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalLeaseReturnForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalLeaseReturnForecast> = inner.list()
}

fun newCachedGlobalLeaseReturnForecastStore(inner: GlobalLeaseReturnForecastStore, ttl: Int, clock: Clock): GlobalLeaseReturnForecastStore {
    return CachedGlobalLeaseReturnForecastStore(
        inner = inner,
        cache = newTtlCache<GlobalLeaseReturnForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global lease return forecasts. Empty fields match all. */
data class GlobalLeaseReturnForecastQuery(
    var lane: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalLeaseReturnForecast): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalLeaseReturnForecast>): MutableList<GlobalLeaseReturnForecast> {
        val matched = filter(items) { candidate: GlobalLeaseReturnForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalLeaseReturnForecasts(store: GlobalLeaseReturnForecastStore, query: GlobalLeaseReturnForecastQuery): MutableList<GlobalLeaseReturnForecast> {
    return query.apply(store.list())
}
