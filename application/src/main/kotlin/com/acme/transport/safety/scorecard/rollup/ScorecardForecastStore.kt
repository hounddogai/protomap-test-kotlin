package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard forecasts by code. */
interface ScorecardForecastStore {
    fun find(code: String): ScorecardForecast?
    fun save(item: ScorecardForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardForecastStore(
    var items: MutableMap<String, ScorecardForecast>,
    var order: MutableList<String>,
) : ScorecardForecastStore {
    override fun find(code: String): ScorecardForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardForecast) {
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

    override fun list(): MutableList<ScorecardForecast> {
        val out = mutableListOf<ScorecardForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardForecastStore(): ScorecardForecastStore {
    return MemoryScorecardForecastStore(mutableMapOf<String, ScorecardForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardForecastStore(
    var inner: ScorecardForecastStore,
    var cache: TtlCache<ScorecardForecast>,
    var hits: Int,
    var misses: Int,
) : ScorecardForecastStore {
    override fun find(code: String): ScorecardForecast? {
        val hit = (cache.get(code) as ScorecardForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardForecast> = inner.list()
}

fun newCachedScorecardForecastStore(inner: ScorecardForecastStore, ttl: Int, clock: Clock): ScorecardForecastStore {
    return CachedScorecardForecastStore(
        inner = inner,
        cache = newTtlCache<ScorecardForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard forecasts. Empty fields match all. */
data class ScorecardForecastQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardForecast>): MutableList<ScorecardForecast> {
        val matched = filter(items) { candidate: ScorecardForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardForecasts(store: ScorecardForecastStore, query: ScorecardForecastQuery): MutableList<ScorecardForecast> {
    return query.apply(store.list())
}
