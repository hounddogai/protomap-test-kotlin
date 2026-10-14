package com.acme.transport.terminal.appointment.appointmentsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive appointment forecasts by code. */
interface AdaptiveAppointmentForecastStore {
    fun find(code: String): AdaptiveAppointmentForecast?
    fun save(item: AdaptiveAppointmentForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAppointmentForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAppointmentForecastStore(
    var items: MutableMap<String, AdaptiveAppointmentForecast>,
    var order: MutableList<String>,
) : AdaptiveAppointmentForecastStore {
    override fun find(code: String): AdaptiveAppointmentForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAppointmentForecast) {
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

    override fun list(): MutableList<AdaptiveAppointmentForecast> {
        val out = mutableListOf<AdaptiveAppointmentForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAppointmentForecastStore(): AdaptiveAppointmentForecastStore {
    return MemoryAdaptiveAppointmentForecastStore(
        mutableMapOf<String, AdaptiveAppointmentForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAppointmentForecastStore(
    var inner: AdaptiveAppointmentForecastStore,
    var cache: TtlCache<AdaptiveAppointmentForecast>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAppointmentForecastStore {
    override fun find(code: String): AdaptiveAppointmentForecast? {
        val hit = (cache.get(code) as AdaptiveAppointmentForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAppointmentForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAppointmentForecast> = inner.list()
}

fun newCachedAdaptiveAppointmentForecastStore(inner: AdaptiveAppointmentForecastStore, ttl: Int, clock: Clock): AdaptiveAppointmentForecastStore {
    return CachedAdaptiveAppointmentForecastStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAppointmentForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive appointment forecasts. Empty fields match all. */
data class AdaptiveAppointmentForecastQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAppointmentForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAppointmentForecast>): MutableList<AdaptiveAppointmentForecast> {
        val matched = filter(items) { candidate: AdaptiveAppointmentForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAppointmentForecasts(store: AdaptiveAppointmentForecastStore, query: AdaptiveAppointmentForecastQuery): MutableList<AdaptiveAppointmentForecast> {
    return query.apply(store.list())
}
