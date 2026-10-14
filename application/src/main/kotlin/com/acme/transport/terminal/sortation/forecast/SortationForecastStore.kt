package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation forecasts by code. */
interface SortationForecastStore {
    fun find(code: String): SortationForecast?
    fun save(item: SortationForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationForecastStore(
    var items: MutableMap<String, SortationForecast>,
    var order: MutableList<String>,
) : SortationForecastStore {
    override fun find(code: String): SortationForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationForecast) {
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

    override fun list(): MutableList<SortationForecast> {
        val out = mutableListOf<SortationForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationForecastStore(): SortationForecastStore {
    return MemorySortationForecastStore(mutableMapOf<String, SortationForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationForecastStore(
    var inner: SortationForecastStore,
    var cache: TtlCache<SortationForecast>,
    var hits: Int,
    var misses: Int,
) : SortationForecastStore {
    override fun find(code: String): SortationForecast? {
        val hit = (cache.get(code) as SortationForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationForecast> = inner.list()
}

fun newCachedSortationForecastStore(inner: SortationForecastStore, ttl: Int, clock: Clock): SortationForecastStore {
    return CachedSortationForecastStore(
        inner = inner,
        cache = newTtlCache<SortationForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation forecasts. Empty fields match all. */
data class SortationForecastQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationForecast>): MutableList<SortationForecast> {
        val matched = filter(items) { candidate: SortationForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationForecasts(store: SortationForecastStore, query: SortationForecastQuery): MutableList<SortationForecast> {
    return query.apply(store.list())
}
