package com.acme.transport.crewing.availability.ledger

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending availability forecasts by code. */
interface PendingAvailabilityForecastStore {
    fun find(code: String): PendingAvailabilityForecast?
    fun save(item: PendingAvailabilityForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAvailabilityForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAvailabilityForecastStore(
    var items: MutableMap<String, PendingAvailabilityForecast>,
    var order: MutableList<String>,
) : PendingAvailabilityForecastStore {
    override fun find(code: String): PendingAvailabilityForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAvailabilityForecast) {
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

    override fun list(): MutableList<PendingAvailabilityForecast> {
        val out = mutableListOf<PendingAvailabilityForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAvailabilityForecastStore(): PendingAvailabilityForecastStore {
    return MemoryPendingAvailabilityForecastStore(
        mutableMapOf<String, PendingAvailabilityForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAvailabilityForecastStore(
    var inner: PendingAvailabilityForecastStore,
    var cache: TtlCache<PendingAvailabilityForecast>,
    var hits: Int,
    var misses: Int,
) : PendingAvailabilityForecastStore {
    override fun find(code: String): PendingAvailabilityForecast? {
        val hit = (cache.get(code) as PendingAvailabilityForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAvailabilityForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAvailabilityForecast> = inner.list()
}

fun newCachedPendingAvailabilityForecastStore(inner: PendingAvailabilityForecastStore, ttl: Int, clock: Clock): PendingAvailabilityForecastStore {
    return CachedPendingAvailabilityForecastStore(
        inner = inner,
        cache = newTtlCache<PendingAvailabilityForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending availability forecasts. Empty fields match all. */
data class PendingAvailabilityForecastQuery(
    var channel: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAvailabilityForecast): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<PendingAvailabilityForecast>): MutableList<PendingAvailabilityForecast> {
        val matched = filter(items) { candidate: PendingAvailabilityForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAvailabilityForecasts(store: PendingAvailabilityForecastStore, query: PendingAvailabilityForecastQuery): MutableList<PendingAvailabilityForecast> {
    return query.apply(store.list())
}
