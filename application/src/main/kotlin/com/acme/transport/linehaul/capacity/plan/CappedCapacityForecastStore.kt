package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped capacity forecasts by code. */
interface CappedCapacityForecastStore {
    fun find(code: String): CappedCapacityForecast?
    fun save(item: CappedCapacityForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCapacityForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCapacityForecastStore(
    var items: MutableMap<String, CappedCapacityForecast>,
    var order: MutableList<String>,
) : CappedCapacityForecastStore {
    override fun find(code: String): CappedCapacityForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCapacityForecast) {
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

    override fun list(): MutableList<CappedCapacityForecast> {
        val out = mutableListOf<CappedCapacityForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCapacityForecastStore(): CappedCapacityForecastStore {
    return MemoryCappedCapacityForecastStore(
        mutableMapOf<String, CappedCapacityForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCapacityForecastStore(
    var inner: CappedCapacityForecastStore,
    var cache: TtlCache<CappedCapacityForecast>,
    var hits: Int,
    var misses: Int,
) : CappedCapacityForecastStore {
    override fun find(code: String): CappedCapacityForecast? {
        val hit = (cache.get(code) as CappedCapacityForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCapacityForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCapacityForecast> = inner.list()
}

fun newCachedCappedCapacityForecastStore(inner: CappedCapacityForecastStore, ttl: Int, clock: Clock): CappedCapacityForecastStore {
    return CachedCappedCapacityForecastStore(
        inner = inner,
        cache = newTtlCache<CappedCapacityForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped capacity forecasts. Empty fields match all. */
data class CappedCapacityForecastQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCapacityForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCapacityForecast>): MutableList<CappedCapacityForecast> {
        val matched = filter(items) { candidate: CappedCapacityForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCapacityForecasts(store: CappedCapacityForecastStore, query: CappedCapacityForecastQuery): MutableList<CappedCapacityForecast> {
    return query.apply(store.list())
}
