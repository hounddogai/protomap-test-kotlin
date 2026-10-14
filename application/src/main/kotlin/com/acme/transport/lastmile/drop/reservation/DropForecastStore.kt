package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop forecasts by code. */
interface DropForecastStore {
    fun find(code: String): DropForecast?
    fun save(item: DropForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropForecastStore(
    var items: MutableMap<String, DropForecast>,
    var order: MutableList<String>,
) : DropForecastStore {
    override fun find(code: String): DropForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropForecast) {
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

    override fun list(): MutableList<DropForecast> {
        val out = mutableListOf<DropForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropForecastStore(): DropForecastStore {
    return MemoryDropForecastStore(mutableMapOf<String, DropForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropForecastStore(
    var inner: DropForecastStore,
    var cache: TtlCache<DropForecast>,
    var hits: Int,
    var misses: Int,
) : DropForecastStore {
    override fun find(code: String): DropForecast? {
        val hit = (cache.get(code) as DropForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropForecast> = inner.list()
}

fun newCachedDropForecastStore(inner: DropForecastStore, ttl: Int, clock: Clock): DropForecastStore {
    return CachedDropForecastStore(
        inner = inner,
        cache = newTtlCache<DropForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop forecasts. Empty fields match all. */
data class DropForecastQuery(
    var country: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropForecast): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<DropForecast>): MutableList<DropForecast> {
        val matched = filter(items) { candidate: DropForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropForecasts(store: DropForecastStore, query: DropForecastQuery): MutableList<DropForecast> {
    return query.apply(store.list())
}
