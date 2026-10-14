package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict violation forecasts by code. */
interface StrictViolationForecastStore {
    fun find(code: String): StrictViolationForecast?
    fun save(item: StrictViolationForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictViolationForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictViolationForecastStore(
    var items: MutableMap<String, StrictViolationForecast>,
    var order: MutableList<String>,
) : StrictViolationForecastStore {
    override fun find(code: String): StrictViolationForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictViolationForecast) {
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

    override fun list(): MutableList<StrictViolationForecast> {
        val out = mutableListOf<StrictViolationForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictViolationForecastStore(): StrictViolationForecastStore {
    return MemoryStrictViolationForecastStore(
        mutableMapOf<String, StrictViolationForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictViolationForecastStore(
    var inner: StrictViolationForecastStore,
    var cache: TtlCache<StrictViolationForecast>,
    var hits: Int,
    var misses: Int,
) : StrictViolationForecastStore {
    override fun find(code: String): StrictViolationForecast? {
        val hit = (cache.get(code) as StrictViolationForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictViolationForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictViolationForecast> = inner.list()
}

fun newCachedStrictViolationForecastStore(inner: StrictViolationForecastStore, ttl: Int, clock: Clock): StrictViolationForecastStore {
    return CachedStrictViolationForecastStore(
        inner = inner,
        cache = newTtlCache<StrictViolationForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict violation forecasts. Empty fields match all. */
data class StrictViolationForecastQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictViolationForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictViolationForecast>): MutableList<StrictViolationForecast> {
        val matched = filter(items) { candidate: StrictViolationForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictViolationForecasts(store: StrictViolationForecastStore, query: StrictViolationForecastQuery): MutableList<StrictViolationForecast> {
    return query.apply(store.list())
}
