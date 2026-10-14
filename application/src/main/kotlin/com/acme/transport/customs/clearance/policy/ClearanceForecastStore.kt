package com.acme.transport.customs.clearance.policy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance forecasts by code. */
interface ClearanceForecastStore {
    fun find(code: String): ClearanceForecast?
    fun save(item: ClearanceForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceForecastStore(
    var items: MutableMap<String, ClearanceForecast>,
    var order: MutableList<String>,
) : ClearanceForecastStore {
    override fun find(code: String): ClearanceForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceForecast) {
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

    override fun list(): MutableList<ClearanceForecast> {
        val out = mutableListOf<ClearanceForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceForecastStore(): ClearanceForecastStore {
    return MemoryClearanceForecastStore(mutableMapOf<String, ClearanceForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceForecastStore(
    var inner: ClearanceForecastStore,
    var cache: TtlCache<ClearanceForecast>,
    var hits: Int,
    var misses: Int,
) : ClearanceForecastStore {
    override fun find(code: String): ClearanceForecast? {
        val hit = (cache.get(code) as ClearanceForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceForecast> = inner.list()
}

fun newCachedClearanceForecastStore(inner: ClearanceForecastStore, ttl: Int, clock: Clock): ClearanceForecastStore {
    return CachedClearanceForecastStore(
        inner = inner,
        cache = newTtlCache<ClearanceForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance forecasts. Empty fields match all. */
data class ClearanceForecastQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceForecast>): MutableList<ClearanceForecast> {
        val matched = filter(items) { candidate: ClearanceForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceForecasts(store: ClearanceForecastStore, query: ClearanceForecastQuery): MutableList<ClearanceForecast> {
    return query.apply(store.list())
}
