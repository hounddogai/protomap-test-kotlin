package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient dwell forecasts by code. */
interface LenientDwellForecastStore {
    fun find(code: String): LenientDwellForecast?
    fun save(item: LenientDwellForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDwellForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDwellForecastStore(
    var items: MutableMap<String, LenientDwellForecast>,
    var order: MutableList<String>,
) : LenientDwellForecastStore {
    override fun find(code: String): LenientDwellForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDwellForecast) {
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

    override fun list(): MutableList<LenientDwellForecast> {
        val out = mutableListOf<LenientDwellForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDwellForecastStore(): LenientDwellForecastStore {
    return MemoryLenientDwellForecastStore(
        mutableMapOf<String, LenientDwellForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDwellForecastStore(
    var inner: LenientDwellForecastStore,
    var cache: TtlCache<LenientDwellForecast>,
    var hits: Int,
    var misses: Int,
) : LenientDwellForecastStore {
    override fun find(code: String): LenientDwellForecast? {
        val hit = (cache.get(code) as LenientDwellForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDwellForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDwellForecast> = inner.list()
}

fun newCachedLenientDwellForecastStore(inner: LenientDwellForecastStore, ttl: Int, clock: Clock): LenientDwellForecastStore {
    return CachedLenientDwellForecastStore(
        inner = inner,
        cache = newTtlCache<LenientDwellForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dwell forecasts. Empty fields match all. */
data class LenientDwellForecastQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDwellForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDwellForecast>): MutableList<LenientDwellForecast> {
        val matched = filter(items) { candidate: LenientDwellForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDwellForecasts(store: LenientDwellForecastStore, query: LenientDwellForecastQuery): MutableList<LenientDwellForecast> {
    return query.apply(store.list())
}
