package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door forecasts by code. */
interface DockDoorForecastStore {
    fun find(code: String): DockDoorForecast?
    fun save(item: DockDoorForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorForecastStore(
    var items: MutableMap<String, DockDoorForecast>,
    var order: MutableList<String>,
) : DockDoorForecastStore {
    override fun find(code: String): DockDoorForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorForecast) {
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

    override fun list(): MutableList<DockDoorForecast> {
        val out = mutableListOf<DockDoorForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorForecastStore(): DockDoorForecastStore {
    return MemoryDockDoorForecastStore(mutableMapOf<String, DockDoorForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorForecastStore(
    var inner: DockDoorForecastStore,
    var cache: TtlCache<DockDoorForecast>,
    var hits: Int,
    var misses: Int,
) : DockDoorForecastStore {
    override fun find(code: String): DockDoorForecast? {
        val hit = (cache.get(code) as DockDoorForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorForecast> = inner.list()
}

fun newCachedDockDoorForecastStore(inner: DockDoorForecastStore, ttl: Int, clock: Clock): DockDoorForecastStore {
    return CachedDockDoorForecastStore(
        inner = inner,
        cache = newTtlCache<DockDoorForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door forecasts. Empty fields match all. */
data class DockDoorForecastQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<DockDoorForecast>): MutableList<DockDoorForecast> {
        val matched = filter(items) { candidate: DockDoorForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorForecasts(store: DockDoorForecastStore, query: DockDoorForecastQuery): MutableList<DockDoorForecast> {
    return query.apply(store.list())
}
