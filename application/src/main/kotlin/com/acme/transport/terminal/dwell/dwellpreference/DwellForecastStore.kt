package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell forecasts by code. */
interface DwellForecastStore {
    fun find(code: String): DwellForecast?
    fun save(item: DwellForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellForecastStore(
    var items: MutableMap<String, DwellForecast>,
    var order: MutableList<String>,
) : DwellForecastStore {
    override fun find(code: String): DwellForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellForecast) {
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

    override fun list(): MutableList<DwellForecast> {
        val out = mutableListOf<DwellForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellForecastStore(): DwellForecastStore {
    return MemoryDwellForecastStore(mutableMapOf<String, DwellForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellForecastStore(
    var inner: DwellForecastStore,
    var cache: TtlCache<DwellForecast>,
    var hits: Int,
    var misses: Int,
) : DwellForecastStore {
    override fun find(code: String): DwellForecast? {
        val hit = (cache.get(code) as DwellForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellForecast> = inner.list()
}

fun newCachedDwellForecastStore(inner: DwellForecastStore, ttl: Int, clock: Clock): DwellForecastStore {
    return CachedDwellForecastStore(
        inner = inner,
        cache = newTtlCache<DwellForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell forecasts. Empty fields match all. */
data class DwellForecastQuery(
    var country: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellForecast): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<DwellForecast>): MutableList<DwellForecast> {
        val matched = filter(items) { candidate: DwellForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellForecasts(store: DwellForecastStore, query: DwellForecastQuery): MutableList<DwellForecast> {
    return query.apply(store.list())
}
