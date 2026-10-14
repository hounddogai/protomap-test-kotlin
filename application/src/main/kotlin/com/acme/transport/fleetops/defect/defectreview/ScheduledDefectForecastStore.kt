package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled defect forecasts by code. */
interface ScheduledDefectForecastStore {
    fun find(code: String): ScheduledDefectForecast?
    fun save(item: ScheduledDefectForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDefectForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDefectForecastStore(
    var items: MutableMap<String, ScheduledDefectForecast>,
    var order: MutableList<String>,
) : ScheduledDefectForecastStore {
    override fun find(code: String): ScheduledDefectForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDefectForecast) {
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

    override fun list(): MutableList<ScheduledDefectForecast> {
        val out = mutableListOf<ScheduledDefectForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDefectForecastStore(): ScheduledDefectForecastStore {
    return MemoryScheduledDefectForecastStore(
        mutableMapOf<String, ScheduledDefectForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDefectForecastStore(
    var inner: ScheduledDefectForecastStore,
    var cache: TtlCache<ScheduledDefectForecast>,
    var hits: Int,
    var misses: Int,
) : ScheduledDefectForecastStore {
    override fun find(code: String): ScheduledDefectForecast? {
        val hit = (cache.get(code) as ScheduledDefectForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDefectForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDefectForecast> = inner.list()
}

fun newCachedScheduledDefectForecastStore(inner: ScheduledDefectForecastStore, ttl: Int, clock: Clock): ScheduledDefectForecastStore {
    return CachedScheduledDefectForecastStore(
        inner = inner,
        cache = newTtlCache<ScheduledDefectForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled defect forecasts. Empty fields match all. */
data class ScheduledDefectForecastQuery(
    var lane: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDefectForecast): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<ScheduledDefectForecast>): MutableList<ScheduledDefectForecast> {
        val matched = filter(items) { candidate: ScheduledDefectForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDefectForecasts(store: ScheduledDefectForecastStore, query: ScheduledDefectForecastQuery): MutableList<ScheduledDefectForecast> {
    return query.apply(store.list())
}
