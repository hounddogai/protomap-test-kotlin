package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling dock door forecasts by code. */
interface RollingDockDoorForecastStore {
    fun find(code: String): RollingDockDoorForecast?
    fun save(item: RollingDockDoorForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDockDoorForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDockDoorForecastStore(
    var items: MutableMap<String, RollingDockDoorForecast>,
    var order: MutableList<String>,
) : RollingDockDoorForecastStore {
    override fun find(code: String): RollingDockDoorForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDockDoorForecast) {
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

    override fun list(): MutableList<RollingDockDoorForecast> {
        val out = mutableListOf<RollingDockDoorForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDockDoorForecastStore(): RollingDockDoorForecastStore {
    return MemoryRollingDockDoorForecastStore(
        mutableMapOf<String, RollingDockDoorForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDockDoorForecastStore(
    var inner: RollingDockDoorForecastStore,
    var cache: TtlCache<RollingDockDoorForecast>,
    var hits: Int,
    var misses: Int,
) : RollingDockDoorForecastStore {
    override fun find(code: String): RollingDockDoorForecast? {
        val hit = (cache.get(code) as RollingDockDoorForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDockDoorForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDockDoorForecast> = inner.list()
}

fun newCachedRollingDockDoorForecastStore(inner: RollingDockDoorForecastStore, ttl: Int, clock: Clock): RollingDockDoorForecastStore {
    return CachedRollingDockDoorForecastStore(
        inner = inner,
        cache = newTtlCache<RollingDockDoorForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dock door forecasts. Empty fields match all. */
data class RollingDockDoorForecastQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDockDoorForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDockDoorForecast>): MutableList<RollingDockDoorForecast> {
        val matched = filter(items) { candidate: RollingDockDoorForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDockDoorForecasts(store: RollingDockDoorForecastStore, query: RollingDockDoorForecastQuery): MutableList<RollingDockDoorForecast> {
    return query.apply(store.list())
}
