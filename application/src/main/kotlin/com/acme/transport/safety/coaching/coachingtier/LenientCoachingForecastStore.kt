package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient coaching forecasts by code. */
interface LenientCoachingForecastStore {
    fun find(code: String): LenientCoachingForecast?
    fun save(item: LenientCoachingForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCoachingForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCoachingForecastStore(
    var items: MutableMap<String, LenientCoachingForecast>,
    var order: MutableList<String>,
) : LenientCoachingForecastStore {
    override fun find(code: String): LenientCoachingForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCoachingForecast) {
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

    override fun list(): MutableList<LenientCoachingForecast> {
        val out = mutableListOf<LenientCoachingForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCoachingForecastStore(): LenientCoachingForecastStore {
    return MemoryLenientCoachingForecastStore(
        mutableMapOf<String, LenientCoachingForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCoachingForecastStore(
    var inner: LenientCoachingForecastStore,
    var cache: TtlCache<LenientCoachingForecast>,
    var hits: Int,
    var misses: Int,
) : LenientCoachingForecastStore {
    override fun find(code: String): LenientCoachingForecast? {
        val hit = (cache.get(code) as LenientCoachingForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCoachingForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCoachingForecast> = inner.list()
}

fun newCachedLenientCoachingForecastStore(inner: LenientCoachingForecastStore, ttl: Int, clock: Clock): LenientCoachingForecastStore {
    return CachedLenientCoachingForecastStore(
        inner = inner,
        cache = newTtlCache<LenientCoachingForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient coaching forecasts. Empty fields match all. */
data class LenientCoachingForecastQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCoachingForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LenientCoachingForecast>): MutableList<LenientCoachingForecast> {
        val matched = filter(items) { candidate: LenientCoachingForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCoachingForecasts(store: LenientCoachingForecastStore, query: LenientCoachingForecastQuery): MutableList<LenientCoachingForecast> {
    return query.apply(store.list())
}
