package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub forecasts by code. */
interface HubForecastStore {
    fun find(code: String): HubForecast?
    fun save(item: HubForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubForecastStore(
    var items: MutableMap<String, HubForecast>,
    var order: MutableList<String>,
) : HubForecastStore {
    override fun find(code: String): HubForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubForecast) {
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

    override fun list(): MutableList<HubForecast> {
        val out = mutableListOf<HubForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubForecastStore(): HubForecastStore {
    return MemoryHubForecastStore(mutableMapOf<String, HubForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubForecastStore(
    var inner: HubForecastStore,
    var cache: TtlCache<HubForecast>,
    var hits: Int,
    var misses: Int,
) : HubForecastStore {
    override fun find(code: String): HubForecast? {
        val hit = (cache.get(code) as HubForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubForecast> = inner.list()
}

fun newCachedHubForecastStore(inner: HubForecastStore, ttl: Int, clock: Clock): HubForecastStore {
    return CachedHubForecastStore(
        inner = inner,
        cache = newTtlCache<HubForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub forecasts. Empty fields match all. */
data class HubForecastQuery(
    var source: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubForecast): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubForecast>): MutableList<HubForecast> {
        val matched = filter(items) { candidate: HubForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubForecasts(store: HubForecastStore, query: HubForecastQuery): MutableList<HubForecast> {
    return query.apply(store.list())
}
