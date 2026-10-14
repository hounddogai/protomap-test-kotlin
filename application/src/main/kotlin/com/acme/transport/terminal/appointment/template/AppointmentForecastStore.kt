package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment forecasts by code. */
interface AppointmentForecastStore {
    fun find(code: String): AppointmentForecast?
    fun save(item: AppointmentForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentForecastStore(
    var items: MutableMap<String, AppointmentForecast>,
    var order: MutableList<String>,
) : AppointmentForecastStore {
    override fun find(code: String): AppointmentForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentForecast) {
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

    override fun list(): MutableList<AppointmentForecast> {
        val out = mutableListOf<AppointmentForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentForecastStore(): AppointmentForecastStore {
    return MemoryAppointmentForecastStore(mutableMapOf<String, AppointmentForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentForecastStore(
    var inner: AppointmentForecastStore,
    var cache: TtlCache<AppointmentForecast>,
    var hits: Int,
    var misses: Int,
) : AppointmentForecastStore {
    override fun find(code: String): AppointmentForecast? {
        val hit = (cache.get(code) as AppointmentForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentForecast> = inner.list()
}

fun newCachedAppointmentForecastStore(inner: AppointmentForecastStore, ttl: Int, clock: Clock): AppointmentForecastStore {
    return CachedAppointmentForecastStore(
        inner = inner,
        cache = newTtlCache<AppointmentForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment forecasts. Empty fields match all. */
data class AppointmentForecastQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentForecast>): MutableList<AppointmentForecast> {
        val matched = filter(items) { candidate: AppointmentForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentForecasts(store: AppointmentForecastStore, query: AppointmentForecastQuery): MutableList<AppointmentForecast> {
    return query.apply(store.list())
}
