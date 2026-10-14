package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled appointment forecasts by code. */
interface ScheduledAppointmentForecastStore {
    fun find(code: String): ScheduledAppointmentForecast?
    fun save(item: ScheduledAppointmentForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAppointmentForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAppointmentForecastStore(
    var items: MutableMap<String, ScheduledAppointmentForecast>,
    var order: MutableList<String>,
) : ScheduledAppointmentForecastStore {
    override fun find(code: String): ScheduledAppointmentForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAppointmentForecast) {
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

    override fun list(): MutableList<ScheduledAppointmentForecast> {
        val out = mutableListOf<ScheduledAppointmentForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAppointmentForecastStore(): ScheduledAppointmentForecastStore {
    return MemoryScheduledAppointmentForecastStore(
        mutableMapOf<String, ScheduledAppointmentForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAppointmentForecastStore(
    var inner: ScheduledAppointmentForecastStore,
    var cache: TtlCache<ScheduledAppointmentForecast>,
    var hits: Int,
    var misses: Int,
) : ScheduledAppointmentForecastStore {
    override fun find(code: String): ScheduledAppointmentForecast? {
        val hit = (cache.get(code) as ScheduledAppointmentForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAppointmentForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAppointmentForecast> = inner.list()
}

fun newCachedScheduledAppointmentForecastStore(inner: ScheduledAppointmentForecastStore, ttl: Int, clock: Clock): ScheduledAppointmentForecastStore {
    return CachedScheduledAppointmentForecastStore(
        inner = inner,
        cache = newTtlCache<ScheduledAppointmentForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled appointment forecasts. Empty fields match all. */
data class ScheduledAppointmentForecastQuery(
    var segment: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAppointmentForecast): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledAppointmentForecast>): MutableList<ScheduledAppointmentForecast> {
        val matched = filter(items) { candidate: ScheduledAppointmentForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAppointmentForecasts(store: ScheduledAppointmentForecastStore, query: ScheduledAppointmentForecastQuery): MutableList<ScheduledAppointmentForecast> {
    return query.apply(store.list())
}
