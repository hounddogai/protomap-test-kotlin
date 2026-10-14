package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global drop forecasts by code. */
interface GlobalDropForecastStore {
    fun find(code: String): GlobalDropForecast?
    fun save(item: GlobalDropForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDropForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDropForecastStore(
    var items: MutableMap<String, GlobalDropForecast>,
    var order: MutableList<String>,
) : GlobalDropForecastStore {
    override fun find(code: String): GlobalDropForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDropForecast) {
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

    override fun list(): MutableList<GlobalDropForecast> {
        val out = mutableListOf<GlobalDropForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDropForecastStore(): GlobalDropForecastStore {
    return MemoryGlobalDropForecastStore(mutableMapOf<String, GlobalDropForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDropForecastStore(
    var inner: GlobalDropForecastStore,
    var cache: TtlCache<GlobalDropForecast>,
    var hits: Int,
    var misses: Int,
) : GlobalDropForecastStore {
    override fun find(code: String): GlobalDropForecast? {
        val hit = (cache.get(code) as GlobalDropForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDropForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDropForecast> = inner.list()
}

fun newCachedGlobalDropForecastStore(inner: GlobalDropForecastStore, ttl: Int, clock: Clock): GlobalDropForecastStore {
    return CachedGlobalDropForecastStore(
        inner = inner,
        cache = newTtlCache<GlobalDropForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global drop forecasts. Empty fields match all. */
data class GlobalDropForecastQuery(
    var region: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDropForecast): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDropForecast>): MutableList<GlobalDropForecast> {
        val matched = filter(items) { candidate: GlobalDropForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDropForecasts(store: GlobalDropForecastStore, query: GlobalDropForecastQuery): MutableList<GlobalDropForecast> {
    return query.apply(store.list())
}
