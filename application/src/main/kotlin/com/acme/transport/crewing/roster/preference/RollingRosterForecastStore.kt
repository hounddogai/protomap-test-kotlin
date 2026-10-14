package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling roster forecasts by code. */
interface RollingRosterForecastStore {
    fun find(code: String): RollingRosterForecast?
    fun save(item: RollingRosterForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRosterForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRosterForecastStore(
    var items: MutableMap<String, RollingRosterForecast>,
    var order: MutableList<String>,
) : RollingRosterForecastStore {
    override fun find(code: String): RollingRosterForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRosterForecast) {
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

    override fun list(): MutableList<RollingRosterForecast> {
        val out = mutableListOf<RollingRosterForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRosterForecastStore(): RollingRosterForecastStore {
    return MemoryRollingRosterForecastStore(
        mutableMapOf<String, RollingRosterForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRosterForecastStore(
    var inner: RollingRosterForecastStore,
    var cache: TtlCache<RollingRosterForecast>,
    var hits: Int,
    var misses: Int,
) : RollingRosterForecastStore {
    override fun find(code: String): RollingRosterForecast? {
        val hit = (cache.get(code) as RollingRosterForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRosterForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRosterForecast> = inner.list()
}

fun newCachedRollingRosterForecastStore(inner: RollingRosterForecastStore, ttl: Int, clock: Clock): RollingRosterForecastStore {
    return CachedRollingRosterForecastStore(
        inner = inner,
        cache = newTtlCache<RollingRosterForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling roster forecasts. Empty fields match all. */
data class RollingRosterForecastQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRosterForecast): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRosterForecast>): MutableList<RollingRosterForecast> {
        val matched = filter(items) { candidate: RollingRosterForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRosterForecasts(store: RollingRosterForecastStore, query: RollingRosterForecastQuery): MutableList<RollingRosterForecast> {
    return query.apply(store.list())
}
