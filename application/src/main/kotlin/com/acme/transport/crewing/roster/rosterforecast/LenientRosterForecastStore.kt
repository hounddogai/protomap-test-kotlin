package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient roster forecasts by code. */
interface LenientRosterForecastStore {
    fun find(code: String): LenientRosterForecast?
    fun save(item: LenientRosterForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRosterForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRosterForecastStore(
    var items: MutableMap<String, LenientRosterForecast>,
    var order: MutableList<String>,
) : LenientRosterForecastStore {
    override fun find(code: String): LenientRosterForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRosterForecast) {
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

    override fun list(): MutableList<LenientRosterForecast> {
        val out = mutableListOf<LenientRosterForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRosterForecastStore(): LenientRosterForecastStore {
    return MemoryLenientRosterForecastStore(
        mutableMapOf<String, LenientRosterForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRosterForecastStore(
    var inner: LenientRosterForecastStore,
    var cache: TtlCache<LenientRosterForecast>,
    var hits: Int,
    var misses: Int,
) : LenientRosterForecastStore {
    override fun find(code: String): LenientRosterForecast? {
        val hit = (cache.get(code) as LenientRosterForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRosterForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRosterForecast> = inner.list()
}

fun newCachedLenientRosterForecastStore(inner: LenientRosterForecastStore, ttl: Int, clock: Clock): LenientRosterForecastStore {
    return CachedLenientRosterForecastStore(
        inner = inner,
        cache = newTtlCache<LenientRosterForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient roster forecasts. Empty fields match all. */
data class LenientRosterForecastQuery(
    var locale: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRosterForecast): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<LenientRosterForecast>): MutableList<LenientRosterForecast> {
        val matched = filter(items) { candidate: LenientRosterForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRosterForecasts(store: LenientRosterForecastStore, query: LenientRosterForecastQuery): MutableList<LenientRosterForecast> {
    return query.apply(store.list())
}
