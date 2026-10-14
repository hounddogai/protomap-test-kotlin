package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation forecasts by code. */
interface ViolationForecastStore {
    fun find(code: String): ViolationForecast?
    fun save(item: ViolationForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationForecastStore(
    var items: MutableMap<String, ViolationForecast>,
    var order: MutableList<String>,
) : ViolationForecastStore {
    override fun find(code: String): ViolationForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationForecast) {
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

    override fun list(): MutableList<ViolationForecast> {
        val out = mutableListOf<ViolationForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationForecastStore(): ViolationForecastStore {
    return MemoryViolationForecastStore(mutableMapOf<String, ViolationForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationForecastStore(
    var inner: ViolationForecastStore,
    var cache: TtlCache<ViolationForecast>,
    var hits: Int,
    var misses: Int,
) : ViolationForecastStore {
    override fun find(code: String): ViolationForecast? {
        val hit = (cache.get(code) as ViolationForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationForecast> = inner.list()
}

fun newCachedViolationForecastStore(inner: ViolationForecastStore, ttl: Int, clock: Clock): ViolationForecastStore {
    return CachedViolationForecastStore(
        inner = inner,
        cache = newTtlCache<ViolationForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation forecasts. Empty fields match all. */
data class ViolationForecastQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationForecast): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationForecast>): MutableList<ViolationForecast> {
        val matched = filter(items) { candidate: ViolationForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationForecasts(store: ViolationForecastStore, query: ViolationForecastQuery): MutableList<ViolationForecast> {
    return query.apply(store.list())
}
