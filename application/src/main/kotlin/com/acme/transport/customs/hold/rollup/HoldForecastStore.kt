package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold forecasts by code. */
interface HoldForecastStore {
    fun find(code: String): HoldForecast?
    fun save(item: HoldForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldForecastStore(
    var items: MutableMap<String, HoldForecast>,
    var order: MutableList<String>,
) : HoldForecastStore {
    override fun find(code: String): HoldForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldForecast) {
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

    override fun list(): MutableList<HoldForecast> {
        val out = mutableListOf<HoldForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldForecastStore(): HoldForecastStore {
    return MemoryHoldForecastStore(mutableMapOf<String, HoldForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldForecastStore(
    var inner: HoldForecastStore,
    var cache: TtlCache<HoldForecast>,
    var hits: Int,
    var misses: Int,
) : HoldForecastStore {
    override fun find(code: String): HoldForecast? {
        val hit = (cache.get(code) as HoldForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldForecast> = inner.list()
}

fun newCachedHoldForecastStore(inner: HoldForecastStore, ttl: Int, clock: Clock): HoldForecastStore {
    return CachedHoldForecastStore(
        inner = inner,
        cache = newTtlCache<HoldForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold forecasts. Empty fields match all. */
data class HoldForecastQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldForecast>): MutableList<HoldForecast> {
        val matched = filter(items) { candidate: HoldForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldForecasts(store: HoldForecastStore, query: HoldForecastQuery): MutableList<HoldForecast> {
    return query.apply(store.list())
}
