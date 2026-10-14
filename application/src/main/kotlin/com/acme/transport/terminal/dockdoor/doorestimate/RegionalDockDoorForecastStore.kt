package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional dock door forecasts by code. */
interface RegionalDockDoorForecastStore {
    fun find(code: String): RegionalDockDoorForecast?
    fun save(item: RegionalDockDoorForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDockDoorForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDockDoorForecastStore(
    var items: MutableMap<String, RegionalDockDoorForecast>,
    var order: MutableList<String>,
) : RegionalDockDoorForecastStore {
    override fun find(code: String): RegionalDockDoorForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDockDoorForecast) {
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

    override fun list(): MutableList<RegionalDockDoorForecast> {
        val out = mutableListOf<RegionalDockDoorForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDockDoorForecastStore(): RegionalDockDoorForecastStore {
    return MemoryRegionalDockDoorForecastStore(
        mutableMapOf<String, RegionalDockDoorForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDockDoorForecastStore(
    var inner: RegionalDockDoorForecastStore,
    var cache: TtlCache<RegionalDockDoorForecast>,
    var hits: Int,
    var misses: Int,
) : RegionalDockDoorForecastStore {
    override fun find(code: String): RegionalDockDoorForecast? {
        val hit = (cache.get(code) as RegionalDockDoorForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDockDoorForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDockDoorForecast> = inner.list()
}

fun newCachedRegionalDockDoorForecastStore(inner: RegionalDockDoorForecastStore, ttl: Int, clock: Clock): RegionalDockDoorForecastStore {
    return CachedRegionalDockDoorForecastStore(
        inner = inner,
        cache = newTtlCache<RegionalDockDoorForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional dock door forecasts. Empty fields match all. */
data class RegionalDockDoorForecastQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDockDoorForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RegionalDockDoorForecast>): MutableList<RegionalDockDoorForecast> {
        val matched = filter(items) { candidate: RegionalDockDoorForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDockDoorForecasts(store: RegionalDockDoorForecastStore, query: RegionalDockDoorForecastQuery): MutableList<RegionalDockDoorForecast> {
    return query.apply(store.list())
}
