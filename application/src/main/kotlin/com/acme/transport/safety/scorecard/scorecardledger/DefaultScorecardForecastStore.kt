package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default scorecard forecasts by code. */
interface DefaultScorecardForecastStore {
    fun find(code: String): DefaultScorecardForecast?
    fun save(item: DefaultScorecardForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultScorecardForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultScorecardForecastStore(
    var items: MutableMap<String, DefaultScorecardForecast>,
    var order: MutableList<String>,
) : DefaultScorecardForecastStore {
    override fun find(code: String): DefaultScorecardForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultScorecardForecast) {
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

    override fun list(): MutableList<DefaultScorecardForecast> {
        val out = mutableListOf<DefaultScorecardForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultScorecardForecastStore(): DefaultScorecardForecastStore {
    return MemoryDefaultScorecardForecastStore(
        mutableMapOf<String, DefaultScorecardForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultScorecardForecastStore(
    var inner: DefaultScorecardForecastStore,
    var cache: TtlCache<DefaultScorecardForecast>,
    var hits: Int,
    var misses: Int,
) : DefaultScorecardForecastStore {
    override fun find(code: String): DefaultScorecardForecast? {
        val hit = (cache.get(code) as DefaultScorecardForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultScorecardForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultScorecardForecast> = inner.list()
}

fun newCachedDefaultScorecardForecastStore(inner: DefaultScorecardForecastStore, ttl: Int, clock: Clock): DefaultScorecardForecastStore {
    return CachedDefaultScorecardForecastStore(
        inner = inner,
        cache = newTtlCache<DefaultScorecardForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default scorecard forecasts. Empty fields match all. */
data class DefaultScorecardForecastQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultScorecardForecast): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultScorecardForecast>): MutableList<DefaultScorecardForecast> {
        val matched = filter(items) { candidate: DefaultScorecardForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultScorecardForecasts(store: DefaultScorecardForecastStore, query: DefaultScorecardForecastQuery): MutableList<DefaultScorecardForecast> {
    return query.apply(store.list())
}
