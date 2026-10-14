package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled incident forecasts by code. */
interface ScheduledIncidentForecastStore {
    fun find(code: String): ScheduledIncidentForecast?
    fun save(item: ScheduledIncidentForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledIncidentForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledIncidentForecastStore(
    var items: MutableMap<String, ScheduledIncidentForecast>,
    var order: MutableList<String>,
) : ScheduledIncidentForecastStore {
    override fun find(code: String): ScheduledIncidentForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledIncidentForecast) {
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

    override fun list(): MutableList<ScheduledIncidentForecast> {
        val out = mutableListOf<ScheduledIncidentForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledIncidentForecastStore(): ScheduledIncidentForecastStore {
    return MemoryScheduledIncidentForecastStore(
        mutableMapOf<String, ScheduledIncidentForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledIncidentForecastStore(
    var inner: ScheduledIncidentForecastStore,
    var cache: TtlCache<ScheduledIncidentForecast>,
    var hits: Int,
    var misses: Int,
) : ScheduledIncidentForecastStore {
    override fun find(code: String): ScheduledIncidentForecast? {
        val hit = (cache.get(code) as ScheduledIncidentForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledIncidentForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledIncidentForecast> = inner.list()
}

fun newCachedScheduledIncidentForecastStore(inner: ScheduledIncidentForecastStore, ttl: Int, clock: Clock): ScheduledIncidentForecastStore {
    return CachedScheduledIncidentForecastStore(
        inner = inner,
        cache = newTtlCache<ScheduledIncidentForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled incident forecasts. Empty fields match all. */
data class ScheduledIncidentForecastQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledIncidentForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ScheduledIncidentForecast>): MutableList<ScheduledIncidentForecast> {
        val matched = filter(items) { candidate: ScheduledIncidentForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledIncidentForecasts(store: ScheduledIncidentForecastStore, query: ScheduledIncidentForecastQuery): MutableList<ScheduledIncidentForecast> {
    return query.apply(store.list())
}
