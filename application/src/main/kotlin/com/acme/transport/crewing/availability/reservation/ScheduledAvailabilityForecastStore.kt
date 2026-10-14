package com.acme.transport.crewing.availability.reservation

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled availability forecasts by code. */
interface ScheduledAvailabilityForecastStore {
    fun find(code: String): ScheduledAvailabilityForecast?
    fun save(item: ScheduledAvailabilityForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAvailabilityForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAvailabilityForecastStore(
    var items: MutableMap<String, ScheduledAvailabilityForecast>,
    var order: MutableList<String>,
) : ScheduledAvailabilityForecastStore {
    override fun find(code: String): ScheduledAvailabilityForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAvailabilityForecast) {
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

    override fun list(): MutableList<ScheduledAvailabilityForecast> {
        val out = mutableListOf<ScheduledAvailabilityForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAvailabilityForecastStore(): ScheduledAvailabilityForecastStore {
    return MemoryScheduledAvailabilityForecastStore(
        mutableMapOf<String, ScheduledAvailabilityForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAvailabilityForecastStore(
    var inner: ScheduledAvailabilityForecastStore,
    var cache: TtlCache<ScheduledAvailabilityForecast>,
    var hits: Int,
    var misses: Int,
) : ScheduledAvailabilityForecastStore {
    override fun find(code: String): ScheduledAvailabilityForecast? {
        val hit = (cache.get(code) as ScheduledAvailabilityForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAvailabilityForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAvailabilityForecast> = inner.list()
}

fun newCachedScheduledAvailabilityForecastStore(inner: ScheduledAvailabilityForecastStore, ttl: Int, clock: Clock): ScheduledAvailabilityForecastStore {
    return CachedScheduledAvailabilityForecastStore(
        inner = inner,
        cache = newTtlCache<ScheduledAvailabilityForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled availability forecasts. Empty fields match all. */
data class ScheduledAvailabilityForecastQuery(
    var unit: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAvailabilityForecast): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledAvailabilityForecast>): MutableList<ScheduledAvailabilityForecast> {
        val matched = filter(items) { candidate: ScheduledAvailabilityForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAvailabilityForecasts(store: ScheduledAvailabilityForecastStore, query: ScheduledAvailabilityForecastQuery): MutableList<ScheduledAvailabilityForecast> {
    return query.apply(store.list())
}
