package com.acme.transport.customs.hold.forecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled hold forecasts by code. */
interface ScheduledHoldForecastStore {
    fun find(code: String): ScheduledHoldForecast?
    fun save(item: ScheduledHoldForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHoldForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHoldForecastStore(
    var items: MutableMap<String, ScheduledHoldForecast>,
    var order: MutableList<String>,
) : ScheduledHoldForecastStore {
    override fun find(code: String): ScheduledHoldForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHoldForecast) {
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

    override fun list(): MutableList<ScheduledHoldForecast> {
        val out = mutableListOf<ScheduledHoldForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHoldForecastStore(): ScheduledHoldForecastStore {
    return MemoryScheduledHoldForecastStore(
        mutableMapOf<String, ScheduledHoldForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHoldForecastStore(
    var inner: ScheduledHoldForecastStore,
    var cache: TtlCache<ScheduledHoldForecast>,
    var hits: Int,
    var misses: Int,
) : ScheduledHoldForecastStore {
    override fun find(code: String): ScheduledHoldForecast? {
        val hit = (cache.get(code) as ScheduledHoldForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHoldForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHoldForecast> = inner.list()
}

fun newCachedScheduledHoldForecastStore(inner: ScheduledHoldForecastStore, ttl: Int, clock: Clock): ScheduledHoldForecastStore {
    return CachedScheduledHoldForecastStore(
        inner = inner,
        cache = newTtlCache<ScheduledHoldForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hold forecasts. Empty fields match all. */
data class ScheduledHoldForecastQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHoldForecast): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledHoldForecast>): MutableList<ScheduledHoldForecast> {
        val matched = filter(items) { candidate: ScheduledHoldForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHoldForecasts(store: ScheduledHoldForecastStore, query: ScheduledHoldForecastQuery): MutableList<ScheduledHoldForecast> {
    return query.apply(store.list())
}
